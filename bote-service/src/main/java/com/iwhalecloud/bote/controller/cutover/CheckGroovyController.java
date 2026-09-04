package com.iwhalecloud.bote.controller.cutover;

import com.github.pagehelper.PageHelper;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.SceneDslUtil;
import com.iwhalecloud.bote.dto.orchestration.SceneDslDTO;
import com.iwhalecloud.bote.dto.orchestration.step.ScriptStep;
import com.iwhalecloud.bote.dto.portal.SimpleTenantDTO;
import com.iwhalecloud.bote.entity.bot.BotSceneEntity;
import com.iwhalecloud.bote.entity.portal.ExternalPortalEntity;
import com.iwhalecloud.bote.entity.skill.SkillFlowEntity;
import com.iwhalecloud.bote.entity.skill.SkillFunctionEntity;
import com.iwhalecloud.bote.mapper.cutover.CutOverMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import groovy.lang.GroovyClassLoader;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 检查 Groovy 脚本是否有编译错误
 *
 * @author bianjp
 * @since 2025-09-21
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "cutOver/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Hidden
@IgnoreSign
public class CheckGroovyController {
  /** 分页查询时的每页数量 */
  private static final int PAGE_SIZE = 100;

  private final CutOverMapper cutOverMapper;

  /**
   * 检查 Groovy 脚本是否有编译错误
   */
  @GetMapping("checkGroovy")
  @SuppressFBWarnings("DP_CREATE_CLASSLOADER_INSIDE_DO_PRIVILEGED")
  public void checkGroovy(HttpServletResponse response,
                          @RequestParam(name = "type", required = false) String type) throws IOException {
    response.setContentType("text/plain;charset=UTF-8");
    //noinspection UastIncorrectHttpHeaderInspection
    response.setHeader("X-Accel-Buffering", "no");
    PrintWriter writer = response.getWriter(); //NOPMD - suppressed CloseResource - HTTP 输出流不需要手动关闭
    try (GroovyClassLoader classLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader())) {
      // tenant_id -> tenant_name 映射
      Map<Long, String> tenantNameMap = cutOverMapper.selectAllTenants(false).stream()
        .collect(Collectors.toMap(SimpleTenantDTO::getTenantId, SimpleTenantDTO::getTenantName));
      if (StringUtils.isEmpty(type) || "scene".equals(type)) {
        // 智能体
        Function<BotSceneEntity, Boolean> sceneChecker = scene -> checkScene(writer, classLoader, tenantNameMap, scene);
        check(writer, "智能体", cutOverMapper::selectScenesWithGroovy, sceneChecker);
      }
      if (StringUtils.isEmpty(type) || "flow".equals(type)) {
        // 工作流
        Function<SkillFlowEntity, Boolean> flowChecker = flow -> checkFlow(writer, classLoader, tenantNameMap, flow);
        check(writer, "工作流", cutOverMapper::selectFlowsWithGroovy, flowChecker);
      }
      if (StringUtils.isEmpty(type) || "function".equals(type)) {
        // 服务函数
        Function<SkillFunctionEntity, Boolean> serviceFunctionChecker = func -> checkServiceFunction(writer, classLoader, tenantNameMap, func);
        check(writer, "服务函数", cutOverMapper::selectFunctionsWithGroovy, serviceFunctionChecker);
      }
      if (StringUtils.isEmpty(type) || "portal".equals(type)) {
        // 门户配置
        Function<ExternalPortalEntity, Boolean> portalChecker = portal -> checkExternalPortal(writer, classLoader, tenantNameMap, portal);
        check(writer, "门户配置", cutOverMapper::selectExternalPortalsWithGroovy, portalChecker);
      }
    }
    finally {
      PageHelper.clearPage();
    }
  }

  /**
   * 检查单个功能模块的 Groovy 脚本
   */
  private <T> void check(PrintWriter writer, String name, Supplier<List<T>> selectFunc, Function<T, Boolean> checkFunc) {
    long startTime = System.currentTimeMillis();
    addLog(writer, "开始检查%s\n", name);
    long total = PageHelper.count(selectFunc::get);
    addLog(writer, "\t找到 %s 个\n", total);
    int failedCount = 0;
    for (int offset = 0; offset < total; offset += PAGE_SIZE) {
      //noinspection resource
      PageHelper.offsetPage(offset, PAGE_SIZE, false);
      List<T> flows = selectFunc.get();
      for (T flow : flows) {
        if (!checkFunc.apply(flow)) {
          failedCount++;
        }
      }
    }
    long spentTime = System.currentTimeMillis() - startTime;
    addLog(writer, "检查%s完成, 耗时: %sms, 总数: %s, 失败: %s\n\n", name, spentTime, total, failedCount);
  }

  /**
   * 检查智能体的脚本节点
   */
  private boolean checkScene(PrintWriter writer, GroovyClassLoader classLoader, Map<Long, String> tenantNameMap, BotSceneEntity scene) {
    SceneDslDTO dsl = SceneDslUtil.parse(scene.getSceneDsl());
    boolean success = true;
    for (ScriptStep step : getGroovyScriptSteps(dsl)) {
      try {
        classLoader.parseClass(step.getScriptContent());
      }
      catch (CompilationFailedException e) {
        success = false;
        addLog(writer, "\t编译失败. 租户: %s, 智能体: %s, 节点: %s, 链接: /manager/#/bot/sceneManage/complexScene?sceneId=%s&tenantId=%s, 错误: %s\n",
          tenantNameMap.getOrDefault(scene.getTenantId(), ""), scene.getSceneName(), step.getName(),
          scene.getSceneId(), scene.getTenantId(), e.getMessage());
      }
    }
    return success;
  }

  /**
   * 检查工作流的脚本节点
   */
  private boolean checkFlow(PrintWriter writer, GroovyClassLoader classLoader, Map<Long, String> tenantNameMap, SkillFlowEntity flow) {
    SceneDslDTO dsl = SceneDslUtil.parse(flow.getFlowDsl());
    boolean success = true;
    for (ScriptStep step : getGroovyScriptSteps(dsl)) {
      try {
        classLoader.parseClass(step.getScriptContent());
      }
      catch (CompilationFailedException e) {
        success = false;
        addLog(writer, "\t编译失败. 租户: %s, 工作流: %s, 节点: %s, 链接: /manager/#/skill/workFlow?flowId=%s&tenantId=%s, 错误: %s\n",
          tenantNameMap.getOrDefault(flow.getTenantId(), ""), flow.getFlowName(), step.getName(),
          flow.getFlowId(), flow.getTenantId(), e.getMessage());
      }
    }
    return success;
  }

  /**
   * 筛选智能体、工作流中的 Groovy 脚本节点
   */
  private List<ScriptStep> getGroovyScriptSteps(SceneDslDTO dsl) {
    return dsl.getSteps().stream()
      .filter(step -> step instanceof ScriptStep)
      .map(step -> (ScriptStep) step)
      .filter(step -> BaseConsts.SCRIPT_TYPE_GROOVY.equalsIgnoreCase(step.getScriptType()))
      .filter(step -> StringUtils.isNotEmpty(step.getScriptContent()))
      .toList();
  }

  /**
   * 检查服务函数
   */
  private boolean checkServiceFunction(PrintWriter writer, GroovyClassLoader classLoader, Map<Long, String> tenantNameMap, SkillFunctionEntity func) {
    if (StringUtils.isEmpty(func.getScriptJson())) {
      return true;
    }
    try {
      classLoader.parseClass(func.getScriptJson());
      return true;
    }
    catch (CompilationFailedException e) {
      addLog(writer, "\t编译失败. 租户: %s/%s, 服务函数: %s/%s, 错误: %s\n",
        tenantNameMap.getOrDefault(func.getTenantId(), ""), func.getTenantId(),
        func.getFuncName(), func.getFuncId(), e.getMessage());
      return false;
    }
  }

  /**
   * 检查门户配置中的扩展脚本
   */
  private boolean checkExternalPortal(PrintWriter writer, GroovyClassLoader classLoader, Map<Long, String> tenantNameMap, ExternalPortalEntity portal) {
    if (StringUtils.isEmpty(portal.getSsoScript())) {
      return true;
    }
    try {
      classLoader.parseClass(portal.getSsoScript());
      return true;
    }
    catch (CompilationFailedException e) {
      addLog(writer, "\t编译失败: 租户: %s/%s, 门户: %s/%s, 错误: %s\n",
        tenantNameMap.getOrDefault(portal.getDefaultTenantId(), ""), portal.getDefaultTenantId(),
        portal.getPortalName(), portal.getId(), e.getMessage());
      return false;
    }
  }

  /**
   * 打印日志到 HTTP 响应
   */
  @SuppressFBWarnings("XSS_SERVLET")
  private void addLog(PrintWriter writer, String msg, Object... args) {
    writer.printf(msg, args);
    writer.flush();
  }
}
