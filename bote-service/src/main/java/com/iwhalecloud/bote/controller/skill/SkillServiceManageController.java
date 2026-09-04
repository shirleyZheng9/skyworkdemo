package com.iwhalecloud.bote.controller.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.sse.event.TextSseEvent;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.ParamSpecUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.skill.ApiServiceParams;
import com.iwhalecloud.bote.dto.skill.CurlParamsDTO;
import com.iwhalecloud.bote.dto.skill.CurlParseResultDTO;
import com.iwhalecloud.bote.dto.skill.OpenApiInfoDTO;
import com.iwhalecloud.bote.dto.skill.ServiceApiDocsParams;
import com.iwhalecloud.bote.dto.skill.ServiceMockParams;
import com.iwhalecloud.bote.dto.skill.ServiceTestRequest;
import com.iwhalecloud.bote.dto.skill.SimpleServiceDTO;
import com.iwhalecloud.bote.dto.skill.SimpleServiceMockDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSkillServiceDTO;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.service.engine.ApiSkillEngine;
import com.iwhalecloud.bote.service.skill.ISkillServiceManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * 技能：API controller
 *
 * @author auto
 * @since 2024-09-15
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/skill/service", name = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor
@Tag(name = "技能：API 管理")
public class SkillServiceManageController {
  private static final Logger logger = LoggerFactory.getLogger(SkillServiceManageController.class);

  private final ISkillServiceManageService serviceManageService;
  private final IRefreshCacheService refreshCacheService;
  private final ApiSkillEngine apiSkillEngine;

  @Operation(summary = "保存 API")
  @PostMapping("saveSkillService")
  public ResultVO<SkillServiceDTO> saveSkillService(@RequestBody @Valid SkillServiceDTO service) {
    Long serviceId = service.getServiceId();
    ResultVO<SkillServiceDTO> result = serviceManageService.saveSkillService(service);
    // 修改服务时自动刷新缓存
    if (serviceId != null && result.isSuccess()) {
      String key = service.getTenantId() + CacheConsts.COLON + serviceId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_API, key);
    }
    return result;
  }

  @Operation(summary = "查询单个 API")
  @GetMapping("findSkillService")
  public ResultVO<SkillServiceDTO> findSkillService(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam("serviceId") Long serviceId) {
    Assert.notNull(serviceId, "API ID 不能为空");
    return ResultVO.success(serviceManageService.findSkillService(tenantId, serviceId));
  }

  @Operation(summary = "查询 API 列表", description = "用于其他模块引用")
  @PostMapping("querySkillServiceList")
  public ResultVO<List<SimpleSkillServiceDTO>> querySkillServiceList(@RequestBody SkillQueryParams params) {
    return ResultVO.success(serviceManageService.querySkillServiceList(params));
  }

  @Operation(summary = "分页查询 API")
  @PostMapping("querySkillServicePage")
  public ResultVO<PageInfo<SkillServiceDTO>> querySkillServicePage(@RequestBody SkillQueryParams params) {
    return ResultVO.success(serviceManageService.querySkillServicePage(params));
  }

  @Operation(summary = "分页查询 API", description = "用于其他模块引用")
  @PostMapping("querySimpleSkillServicePage")
  public ResultVO<PageInfo<SimpleSkillServiceDTO>> querySimpleSkillServicePage(@RequestBody SkillQueryParams params) {
    return ResultVO.success(serviceManageService.querySimpleSkillServicePage(params));
  }

  @Operation(summary = "删除 API")
  @GetMapping("deleteSkillService")
  public ResultVO<Void> deleteSkillService(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam("serviceId") Long serviceId) {
    Assert.notNull(serviceId, "API ID 不能为空");
    ResultVO<Void> result = serviceManageService.deleteSkillService(tenantId, serviceId);
    if (result.isSuccess()) {
      String key = tenantId + CacheConsts.COLON + serviceId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_API, key);
    }
    return result;
  }

  @PostMapping("pareServiceParams")
  @Operation(summary = "解析参数")
  public ResultVO<ParameterSpec> pareServiceParams(@RequestBody Object params) {
    Assert.notNull(params, "参数不能为空");
    return ResultVO.success(ParamSpecUtil.buildParamSpec(params));
  }

  @PostMapping("test")
  @Operation(summary = "服务测试")
  public Object test(@RequestBody ServiceTestRequest request) {
    // 测试的服务配置可能尚未保存，不能从数据库查询
    SimpleServiceDTO service = request.getService();
    Assert.notNull(service, "服务不能为空");
    Assert.notNull(request.getParams(), "请求参数不能为空");
    Assert.notNull(service.getPlatformId(), () -> "请选择服务网关地址");
    Assert.hasLength(service.getReqMethod(), () -> "请选择请求方法");
    Assert.hasLength(service.getRelativePath(), () -> "请填写请求地址");
    // 开启接口模拟时，解析模拟报文 JSON
    // 后端解析 JSON 比前端解析好，可以避免 JavaScript 无法精确表示太大的整数的问题
    if (Boolean.TRUE.equals(service.getMockEnabled())) {
      ResultVO<Object> failure = parseServiceMocks(service);
      if (failure != null) {
        return failure;
      }
    }

    // SSE 接口
    if (Boolean.TRUE.equals(request.getService().getSse())) {
      return SseUtil.createSseEmitter(request.getClientId(), sseEmitter -> {
        try {
          SseInvoker sseInvoker = (SseInvoker) apiSkillEngine.execute(service, request.getParams());
          Assert.notNull(sseInvoker, "SseInvoker 不能为空");
          sseInvoker.invoke(event -> {
            Assert.isTrue(event instanceof TextSseEvent, "SSE 接口只支持文本事件");
            SseUtil.sendJson(sseEmitter, event.getMsgType(), event.getMsgContent());
          });
        }
        catch (Exception e) {
          logger.error("Failed to test sse api", e);
          SseUtil.sendJson(sseEmitter, ChatMessageType.ERROR, ExpUtil.getMsg(e));
        }
        finally {
          SseUtil.completeQuietly(sseEmitter);
        }
      });
    }

    try {
      Object response = apiSkillEngine.executeForTest(service, request.getParams());
      return ResultVO.success(response);
    }
    catch (Exception e) {
      return ResultVO.fail(e);
    }
  }

  @PostMapping(value = "test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "服务测试(支持文件表单)")
  public Object test(@RequestPart("serviceTestRequest") ServiceTestRequest serviceTestRequest, @RequestParam Map<String, Object> parameters,
    HttpServletRequest httpServletRequest) {
    serviceTestRequest.setParams(fillMultipartParams(parameters, httpServletRequest));
    return test(serviceTestRequest);
  }

  /**
   * 提取参数
   *
   * @param parameters 参数
   * @param httpServletRequest 请求
   * @return API 技能执行请求参数
   */
  private ApiServiceParams fillMultipartParams(Map<String, Object> parameters, HttpServletRequest httpServletRequest) {
    Map<String, Object> header = filterAndParseRequestParams(parameters, "PARAMS.HEADER.");
    Map<String, Object> query = filterAndParseRequestParams(parameters, "PARAMS.QUERY.");
    Map<String, Object> path = filterAndParseRequestParams(parameters, "PARAMS.PATH.");
    Map<String, Object> body = filterAndParseRequestParams(parameters, "PARAMS.BODY.");
    if (httpServletRequest instanceof MultipartHttpServletRequest) {
      body.putAll(((MultipartHttpServletRequest) httpServletRequest).getFileMap());
    }
    ApiServiceParams params = new ApiServiceParams();
    params.setPath(path);
    params.setHeader(header);
    params.setQuery(query);
    params.setBody(body);
    return params;
  }

  private Map<String, Object> filterAndParseRequestParams(Map<String, Object> parameters, String keyPrefix) {
    return MapUtils.emptyIfNull(parameters).entrySet().stream().filter(item -> item.getKey().startsWith(keyPrefix))
      .collect(Collectors.toMap(each -> Strings.CS.removeStart(each.getKey(), keyPrefix), Map.Entry::getValue));
  }

  /**
   * 解析模拟报文
   */
  @Nullable
  private ResultVO<Object> parseServiceMocks(SimpleServiceDTO service) {
    if (service.getDefaultMockResponse() instanceof String) {
      try {
        service.setDefaultMockResponse(JsonUtil.parseJsonRequired((String) service.getDefaultMockResponse(), Object.class));
      }
      catch (RuntimeException e) {
        return ResultVO.fail("缺省模拟报文不合法: " + e.getMessage());
      }
    }
    for (SimpleServiceMockDTO mock : ListUtils.emptyIfNull(service.getMocks())) {
      if (mock.getResponse() instanceof String) {
        try {
          mock.setResponse(JsonUtil.parseJsonRequired((String) mock.getResponse(), Object.class));
        }
        catch (RuntimeException e) {
          return ResultVO.fail("模拟报文【" + mock.getMockName() + "】的报文不合法: " + e.getMessage());
        }
      }
    }
    return null;
  }

  @Operation(summary = "在线解析Swagger API")
  @GetMapping("parseServiceFromSwaggerUrl")
  public ResultVO<List<OpenApiInfoDTO>> parseServiceFromSwaggerUrl(@RequestParam("serviceDocUrl") String serviceDocUrl) {
    return serviceManageService.parseServiceFromSwaggerUrl(serviceDocUrl);
  }

  @Operation(summary = "离线解析Swagger API")
  @PostMapping("parseServiceFromSwaggerFile")
  public ResultVO<List<OpenApiInfoDTO>> parseServiceFromSwaggerFile(@RequestPart("file") MultipartFile file) {
    return serviceManageService.parseServiceFromSwaggerFile(file);
  }

  @Operation(summary = "从OpenAPI JSON解析API信息")
  @PostMapping("parseServiceFromOpenApiJson")
  public ResultVO<List<OpenApiInfoDTO>> parseServiceFromOpenApiJson(@RequestBody ServiceApiDocsParams apiDocsParams) {
    Assert.hasText(apiDocsParams.getOpenApiJson(), "OpenAPI JSON数据不能为空");
    return serviceManageService.parseServiceFromOpenApiJson(apiDocsParams.getOpenApiJson());
  }

  @Operation(summary = "导入Swagger API")
  @PostMapping("importSwaggerServiceList")
  public ResultVO<List<SkillServiceDTO>> importSwaggerServiceList(@RequestPart(value = "file", required = false) MultipartFile file,
    @RequestPart("apiDocsParams") ServiceApiDocsParams apiDocsParams) {
    Assert.isTrue(StringUtils.isNotEmpty(apiDocsParams.getServiceDocUrl()) || file != null || StringUtils.isNotEmpty(apiDocsParams.getOpenApiJson()),
      "api-docs地址、文件或openApiJson不能同时为空");
    Assert.notEmpty(apiDocsParams.getOpenApiInfoList(), "api详情列表不能为空");
    return serviceManageService.importSwaggerServiceList(file, apiDocsParams);
  }

  @Operation(summary = "启用/关闭服务模拟")
  @PostMapping("toggleServiceMock")
  public ResultVO<Void> toggleServiceMock(@RequestBody ServiceMockParams params) {
    Assert.notNull(params.getTenantId(), "租户ID不能为空");
    Assert.hasText(params.getIsMock(), "isMock不能为空");
    return serviceManageService.toggleServiceMock(params);
  }

  @Operation(summary = "解析curl命令")
  @PostMapping("parseCurlCommand")
  public ResultVO<CurlParseResultDTO> parseCurlCommand(@RequestBody CurlParamsDTO curlParams) {
    return serviceManageService.parseCurlCommand(curlParams);
  }

  @Operation(summary = "解析curl命令")
  @PostMapping("parseCurlCommandw")
  public ResultVO<CurlParseResultDTO> parseCurlCommandw(@RequestParam("list") List<CurlParamsDTO> curlParamsList) {
    return ResultVO.success();
  }
}
