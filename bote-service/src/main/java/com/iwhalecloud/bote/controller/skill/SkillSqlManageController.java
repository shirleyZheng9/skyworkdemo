package com.iwhalecloud.bote.controller.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.dto.skill.ParseSqlResult;
import com.iwhalecloud.bote.dto.skill.SimpleSkillSqlDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSqlSkillDTO;
import com.iwhalecloud.bote.dto.skill.SkillSqlDTO;
import com.iwhalecloud.bote.dto.skill.SqlOperaParams;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.service.engine.SqlSkillEngine;
import com.iwhalecloud.bote.service.skill.ISkillSqlManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 技能：SQL controller
 *
 * @author auto
 * @since 2024-09-15
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/skill/sql", name = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor
@Tag(name = "技能：SQL 管理")
public class SkillSqlManageController {
  private final Logger logger = LoggerFactory.getLogger(SkillSqlManageController.class);
  private final ISkillSqlManageService sqlManageService;
  private final IRefreshCacheService refreshCacheService;
  private final SqlSkillEngine sqlSkillEngine;

  @Operation(summary = "保存 SQL")
  @PostMapping("saveSkillSql")
  public ResultVO<SkillSqlDTO> saveSkillSql(@RequestBody @Valid SkillSqlDTO sql) {
    Long serviceId = sql.getServiceId();
    Assert.hasText(sql.getServiceName(), "SQL 名称不能为空");
    Assert.hasText(sql.getServiceCode(), "SQL 编码不能为空");
    ResultVO<SkillSqlDTO> result = sqlManageService.saveSkillSql(sql);
    // 修改服务时自动刷新缓存
    if (serviceId != null && result.isSuccess()) {
      String key = sql.getTenantId() + CacheConsts.COLON + serviceId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_SQL, key);
    }
    return result;
  }

  @Operation(summary = "查询单个 SQL")
  @GetMapping("findSkillSql")
  public ResultVO<SkillSqlDTO> findSkillSql(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam("serviceId") Long serviceId) {
    Assert.notNull(serviceId, "SQL ID 不能为空");
    return ResultVO.success(sqlManageService.findSkillSql(tenantId, serviceId));
  }

  @Operation(summary = "查找 SQL 列表", description = "用于其他模块引用")
  @PostMapping("querySkillSqlList")
  public ResultVO<List<SimpleSkillSqlDTO>> querySkillSqlList(@RequestBody SkillQueryParams params) {
    return ResultVO.success(sqlManageService.querySkillSqlList(params));
  }

  @Operation(summary = "分页查询 SQL")
  @PostMapping("querySkillSqlPage")
  public ResultVO<PageInfo<SkillSqlDTO>> querySkillSqlPage(@RequestBody SkillQueryParams params) {
    return ResultVO.success(sqlManageService.querySkillSqlPage(params));
  }

  @Operation(summary = "分页查询 SQL", description = "用于其他模块引用")
  @PostMapping("querySimpleSkillSqlPage")
  public ResultVO<PageInfo<SimpleSkillSqlDTO>> querySimpleSkillSqlPage(@RequestBody SkillQueryParams params) {
    return ResultVO.success(sqlManageService.querySimpleSkillSqlPage(params));
  }

  @Operation(summary = "删除 SQL")
  @GetMapping("deleteSkillSql")
  public ResultVO<Void> deleteSkillSql(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam("serviceId") Long serviceId) {
    Assert.notNull(serviceId, "SQL ID 不能为空");
    ResultVO<Void> result = sqlManageService.deleteSkillSql(tenantId, serviceId);
    if (result.isSuccess()) {
      String key = tenantId + CacheConsts.COLON + serviceId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_SQL, key);
    }
    return result;
  }

  @PostMapping("parseSql")
  @Operation(summary = "解析sql")
  public ResultVO<ParseSqlResult> parseSql(@RequestBody SqlOperaParams params) {
    Assert.hasText(params.getSql(), "sql不能为空");
    return ResultVO.success(sqlManageService.parseSql(params));
  }

  @PostMapping("sqlTest")
  @Operation(summary = "sql测试")
  public ResultVO<Object> sqlTest(@RequestBody SqlTestRequest request) {
    // 测试的服务配置可能尚未保存，不能从数据库查询
    SimpleSqlSkillDTO skill = request.getService();
    skill.setTenantId(request.getTenantId());
    Assert.notNull(skill, "服务不能为空");
    Assert.notNull(skill.getDataSourceId(), "请选择数据源");
    Assert.hasLength(skill.getSql(), "请输入 SQL");
    Assert.hasLength(skill.getResultType(), "请输入选择返回结果类型");
    try {
      //noinspection JvmTaintAnalysis
      Object result = sqlSkillEngine.execute(skill, request.getParams());
      return ResultVO.success(result);
    }
    catch (Exception e) {
      logger.error("sql测试失败", e);
      throw BaseErrorConstant.SQL_ERROR.toException(e);
    }
  }

  /**
   * @deprecated 请改用 /bote/sql/execute 接口
   */
  @PostMapping("executeSql")
  @Operation(summary = "执行sql", description = "请改用 /bote/sql/execute 接口")
  @Deprecated
  public ResultVO<Object> executeSql(@RequestBody SqlOperaParams params) {
    //noinspection JvmTaintAnalysis
    Object result = sqlSkillEngine.execute(params.getTenantId(), params.getServiceId(), params.getParams());
    return ResultVO.success(result);
  }

  /**
   * 测试 SQL 服务请求
   */
  @Getter
  @Setter
  @ToString
  @Schema(description = "测试 SQL 服务请求")
  public static class SqlTestRequest {
    @Schema(description = "SQL 服务")
    private SimpleSqlSkillDTO service;

    @Schema(description = "参数")
    private Map<String, Object> params;

    @Schema(description = "租户ID")
    private Long tenantId;
  }
}
