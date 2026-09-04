package com.iwhalecloud.bote.controller.model;

import com.github.pagehelper.PageInfo;
import com.google.common.base.Suppliers;
import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.annotation.RequestCacheable;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.IconUtil;
import com.iwhalecloud.bote.common.util.ModelClientUtil;
import com.iwhalecloud.bote.common.util.ModelConfigUtil;
import com.iwhalecloud.bote.dto.model.FunctionCallingTestResult;
import com.iwhalecloud.bote.dto.model.GatewayModelSyncResult;
import com.iwhalecloud.bote.dto.model.LargeModelDTO;
import com.iwhalecloud.bote.dto.model.ModelProtocolDTO;
import com.iwhalecloud.bote.dto.model.SimpleLargeModelDTO;
import com.iwhalecloud.bote.dto.model.query.LargeModelQueryParams;
import com.iwhalecloud.bote.dto.model.query.LargeModelTestParams;
import com.iwhalecloud.bote.dto.workspace.WorkspaceDTO;
import com.iwhalecloud.bote.llm.client.consts.FunctionCallMode;
import com.iwhalecloud.bote.llm.client.util.ModelValidationUtil;
import com.iwhalecloud.bote.service.model.ILargeModelManageService;
import com.iwhalecloud.bote.service.model.IModelTestService;
import com.iwhalecloud.bote.service.workspace.helper.WorkspaceHelper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 大模型管理 controller
 *
 * @author auto
 * @since 2024-09-20
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/largeModel", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "基础：大模型管理")
public class LargeModelManageController {

  private final ILargeModelManageService modelManageService;
  private final IModelTestService modelTestService;
  private final IRefreshCacheService refreshCacheService;
  private final WorkspaceHelper workspaceHelper;
  private final TenantSettingInfoCache tenantSettingInfoCache;

  /** 大模型协议列表缓存 */
  private final Supplier<List<ModelProtocolDTO>> llmProtocolsCache = Suppliers.memoize(this::loadLlmProtocols);

  @Operation(summary = "查询单个大模型")
  @GetMapping("getLargeModel")
  public ResultVO<LargeModelDTO> getLargeModel(@RequestParam(value = "tenantId", required = false) Long tenantId,
                                               @RequestParam(name = "modelId") Long modelId) {
    Assert.notNull(modelId, "模型 ID 不能为空");
    LargeModelDTO model = modelManageService.getLargeModel(tenantId, modelId);
    ModelConfigUtil.parseExtConfig(model);
    return ResultVO.success(model);
  }

  @Operation(summary = "保存大模型")
  @PostMapping("saveLargeModel")
  public ResultVO<LargeModelDTO> saveLargeModel(@RequestBody LargeModelDTO model) {
    if (model.getSpaceId() != null) {
      WorkspaceDTO workspaceDTO = workspaceHelper.initAiTenantId(model.getSpaceId());
      model.setTenantId(workspaceDTO.getSpaceTenantId());
    }
    ModelConfigUtil.saveExtConfig(model);
    boolean isUpdate = model.getModelId() != null;
    ResultVO<LargeModelDTO> result = modelManageService.saveLargeModel(model);
    // 更新时需要刷新客户端缓存
    if (isUpdate && result.isSuccess()) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_MODEL_CLIENT, model.getTenantId() + CacheConsts.COLON + model.getModelId());
    }
    return result;
  }

  @Operation(summary = "删除大模型")
  @GetMapping("deleteLargeModel")
  public ResultVO<Void> deleteLargeModel(@RequestParam(name = "modelId") Long modelId,
                                         @RequestParam(value = "tenantId", required = false) Long tenantId,
                                         @RequestParam(value = "spaceId", required = false) Long spaceId) {
    Assert.notNull(modelId, "模型 ID 不能为空");
    if (spaceId != null) {
      WorkspaceDTO workspaceDTO = workspaceHelper.initAiTenantId(spaceId);
      tenantId = workspaceDTO.getSpaceTenantId();
    }
    return modelManageService.deleteLargeModel(tenantId, modelId);
  }

  @Operation(summary = "修改大模型是否启用状态")
  @GetMapping("updateLargeModelStatus")
  public ResultVO<Void> updateLargeModelStatus(@RequestParam("modelId") Long modelId,
                                               @RequestParam(value = "spaceId", required = false) Long spaceId,
                                               @RequestParam(value = "tenantId", required = false) Long tenantId,
                                               @RequestParam("enabled") String enabled) {
    Assert.notNull(modelId, "模型 ID 不能为空");
    Assert.hasText(enabled, "模型状态不能为空");
    if (spaceId != null) {
      WorkspaceDTO workspaceDTO = workspaceHelper.initAiTenantId(spaceId);
      tenantId = workspaceDTO.getSpaceTenantId();
    }
    modelManageService.modifyLargeModelStatus(tenantId, modelId, enabled);
    return ResultVO.success();
  }

  @Operation(summary = "根据空间ID查询已启用的空间大模型和已启用的平台大模型")
  @GetMapping("queryEnabledLargeModel")
  public ResultVO<Map<String, SimpleLargeModelDTO>> queryEnabledLargeModel(@RequestParam(value = "spaceId", required = false) Long spaceId) {
    Long spaceTenantId = null;
    if (spaceId != null) {
      WorkspaceDTO workspaceDTO = workspaceHelper.initAiTenantId(spaceId);
      spaceTenantId = workspaceDTO.getSpaceTenantId();
    }
    return ResultVO.success(modelManageService.queryPlatformAndSpaceEnabledLargeModel(spaceTenantId));
  }

  @Operation(summary = "分页查询大模型")
  @PostMapping("queryLargeModelPage")
  public ResultVO<PageInfo<LargeModelDTO>> queryLargeModelPage(@RequestBody LargeModelQueryParams queryParams) {
    if (queryParams.getSpaceId() != null) {
      WorkspaceDTO workspaceDTO = workspaceHelper.initAiTenantId(queryParams.getSpaceId());
      queryParams.setTenantId(workspaceDTO.getSpaceTenantId());
      queryParams.setSourceFrom(BaseConsts.MODEL_SOURCE_TENANT);
    }
    Assert.notNull(queryParams.getTenantId(), "租户 ID 不能为空");
    PageInfo<LargeModelDTO> pageInfo = modelManageService.queryLargeModelPage(queryParams);
    for (LargeModelDTO model : pageInfo.getList()) {
      ModelConfigUtil.parseExtConfig(model);
    }
    // 对于租户的模型管理页面，去掉平台模型的密钥，避免泄露
    if (!BaseConsts.PLATFORM_TENANT_ID.equals(queryParams.getTenantId())) {
      for (LargeModelDTO model : pageInfo.getList()) {
        if (BaseConsts.MODEL_SOURCE_PLATFORM.equals(model.getSourceFrom())) {
          model.setAccessKey(null);
        }
      }
    }
    return ResultVO.success(pageInfo);
  }

  @Operation(summary = "同步天工AI网关模型到项目（spaceId 为 Bote 空间，内部映射 extSpaceId 查 AI Key）")
  @PostMapping("syncGatewayModels")
  public ResultVO<GatewayModelSyncResult> syncGatewayModels(@RequestParam("tenantId") Long tenantId,
                                                            @RequestParam("spaceId") Long spaceId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(spaceId, "空间 ID 不能为空");
    return ResultVO.success(modelManageService.syncGatewayModels(tenantId, spaceId));
  }

  @GetMapping("getLlmProtocols")
  @Operation(summary = "获取大语言模型的协议列表")
  public ResultVO<List<ModelProtocolDTO>> getLlmProtocols() {
    return ResultVO.success(llmProtocolsCache.get());
  }

  /**
   * 加载大模型协议列表
   */
  private List<ModelProtocolDTO> loadLlmProtocols() {
    List<String> protocolTypes = ModelClientUtil.getLlmProtocolTypes();
    List<ModelProtocolDTO> protocols = new ArrayList<>(protocolTypes.size());
    for (String protocolType : protocolTypes) {
      try (InputStream inputStream = new ClassPathResource("protocol/config/llm/" + protocolType + ".json").getInputStream()) {
        protocols.add(JsonUtil.parseJsonRequired(inputStream, ModelProtocolDTO.class));
      }
      catch (Exception e) {
        throw new BssException("加载大模型接口协议配置失败: protocolType=" + protocolType, e);
      }
    }
    return protocols;
  }

  @PostMapping("testConfig")
  @Operation(summary = "测试大模型配置连通性（仅校验 baseUrl + 模型名称 + apiKey 是否可用）")
  public ResultVO<String> testConfig(@RequestBody LargeModelTestParams params) {
    // 未指定已保存的模型时，必须传入完整的连接配置
    if (params.getModelId() == null) {
      Assert.hasLength(params.getProtocolType(), "协议类型不能为空");
      Assert.hasText(params.getAccessUrl(), "访问地址不能为空");
      Assert.hasText(params.getAccessKey(), "访问密钥不能为空");
      Assert.hasText(params.getModelCode(), "模型名称不能为空");
      ModelValidationUtil.validateUrl(params.getAccessUrl().trim(), "访问地址");
    }
    // 问题为空时使用默认探活问题
    if (StringUtils.isBlank(params.getQuestion())) {
      params.setQuestion("ping");
    }
    try {
      // 实际发起一次大模型调用，只关心是否能正常返回
      modelTestService.testLlm(params);
      return ResultVO.success("测试通过");
    }
    catch (Exception e) {
      return ResultVO.fail(ExpUtil.getMsg(e));
    }
  }

  @PostMapping("testEmbedding")
  @Operation(summary = "测试文本嵌入模型")
  public ResultVO<String> testEmbedding(@RequestBody LargeModelTestParams params) {
    if (params.getModelId() == null) {
      Assert.hasText(params.getAccessUrl(), "访问地址不能为空");
      Assert.hasText(params.getQuestion(), "问题不能为空");
    }
    float[] embedding = modelTestService.testEmbedding(params);
    return ResultVO.success(Arrays.toString(embedding));
  }

  @PostMapping(path = "testLlm", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
  @Operation(summary = "测试大语言模型")
  public Object testLlm(@RequestBody LargeModelTestParams params) {
    // 非流式
    if (!Boolean.TRUE.equals(params.getStream())) {
      validateTestLlmParams(params);
      try {
        return ResultVO.success(modelTestService.testLlm(params));
      }
      catch (Exception e) {
        return ResultVO.fail(ExpUtil.getMsg(e));
      }
    }

    // 流式输出
    return SseUtil.createSseEmitter(params.getClientId(), emitter -> {
      try {
        validateTestLlmParams(params);
        modelTestService.testLlmStream(params, response -> SseUtil.sendJson(emitter, "chunk", null, response));
      }
      catch (Exception e) {
        SseUtil.sendJson(emitter, ChatMessageType.ERROR, ExpUtil.getMsg(e));
      }
      finally {
        SseUtil.completeQuietly(emitter);
      }
    });
  }

  /**
   * 校验测试大语言模型参数
   */
  private void validateTestLlmParams(LargeModelTestParams params) {
    if (params.getModelId() == null) {
      Assert.hasLength(params.getProtocolType(), "协议类型不能为空");
    }
    if (CollectionUtils.isEmpty(params.getFileIds())) {
      Assert.hasText(params.getQuestion(), "问题不能为空");
    }
    // 存在图片文件时，校验模型是否支持视觉
    if (CollectionUtils.isNotEmpty(params.getFileIds())) {
      validateVisionSupport(params);
    }
    if (StringUtils.isNotBlank(params.getAccessUrl())) {
      ModelValidationUtil.validateUrl(params.getAccessUrl().trim(), "访问地址");
    }
  }

  /**
   * 校验模型是否支持视觉功能
   */
  private void validateVisionSupport(LargeModelTestParams params) {
    if (params.getModelId() != null) {
      LargeModelDTO model = modelManageService.getLargeModel(params.getTenantId(), params.getModelId());
      Assert.isTrue(BaseConsts.TRUE.equals(model.getSupportsVision()), "该模型不支持视觉功能，请开启支持视觉");
    }
    else {
      Assert.isTrue(Boolean.TRUE.equals(params.getSupportsVision()), "该模型不支持视觉功能，请开启支持视觉");
    }
  }

  @Operation(summary = "测试函数调用")
  @PostMapping("testFunctionCalling")
  public ResultVO<FunctionCallingTestResult> testFunctionCalling(@RequestBody LargeModelTestParams params) {
    Assert.notNull(params.getFunctionCallMode(), "函数调用模式不能为空");
    SseUtil.setClientId(params.getClientId());
    try {
      return ResultVO.success(modelTestService.testFunctionCalling(params));
    }
    finally {
      SseUtil.removeClientId();
    }
  }

  @Operation(summary = "查询大模型列表，用于其他模块")
  @GetMapping("queryLargeModels")
  public ResultVO<List<SimpleLargeModelDTO>> queryLargeModels(@RequestParam("tenantId") Long tenantId,
                                                              @RequestParam(name = "modelType", required = false) String modelType) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    return ResultVO.success(modelManageService.queryLargeModelList(tenantId, modelType, null));
  }

  @Operation(summary = "查询大模型列表，用于查询平台模型列表或者空间模型列表")
  @GetMapping("queryLargeModelList")
  public ResultVO<List<SimpleLargeModelDTO>> queryLargeModelList(
    @RequestParam("tenantId") Long tenantId,
    @RequestParam(name = "modelType", required = false) String modelType,
    @RequestParam(name = "spaceId", required = false) Long spaceId,
    @RequestParam(name = "sourceFrom", required = false) String sourceFrom
  ) {
    Assert.notNull(tenantId, "租户 ID 不能为空");

    Long curTenantId = tenantId;

    if (spaceId != null) {
      WorkspaceDTO workspaceDTO = workspaceHelper.initAiTenantId(spaceId);
      curTenantId = workspaceDTO.getSpaceTenantId();
    }

    return ResultVO.success(modelManageService.queryLargeModelList(curTenantId, modelType, sourceFrom));
  }

  @Operation(summary = "查询大模型列表，用于其他模块")
  @GetMapping("queryAiLargeModels")
  public ResultVO<List<SimpleLargeModelDTO>> queryAiLargeModels(@RequestParam("tenantId") Long tenantId,
    @RequestParam(name = "botId", required = false) Long botId,
    @RequestParam(name = "modelType", required = false) String modelType) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    if (botId == null) {
      botId = BaseConsts.BOTE_AI_ID;
    }
    return ResultVO.success(modelManageService.queryAiLargeModelList(tenantId, botId, modelType));
  }

  @IgnoreSign
  @IgnoreSession
  @Operation(summary = "获取模型图标")
  @GetMapping("getLargeModelIcon")
  @RequestCacheable(sql = "SELECT updated_time FROM bt_library_large_model WHERE model_id = #{param1} and tenant_id in(#{param2},-1)", cacheOnNotFound = true)
  public void getLargeModelIcon(@RequestParam("modelId") Long modelId, @RequestParam("tenantId") Long tenantId, HttpServletResponse response)
    throws IOException {
    Assert.notNull(modelId, "模型 ID 不能为空");
    Assert.notNull(tenantId, "租户 ID 不能为空");
    String largeModelIcon = modelManageService.getLargeModelIcon(modelId, tenantId);
    if (StringUtils.isEmpty(largeModelIcon)) {
      // 返回默认图标数据
      String path = "assets/images/model/ico-model-6.png";
      ClassPathResource resource = new ClassPathResource(path);
      IconUtil.sendPathResourceIcon(response, resource);
    }
    else {
      if (largeModelIcon.startsWith("./images/model")) {
        largeModelIcon = largeModelIcon.replace("./images/model", "assets/images/model");
        ClassPathResource resource = new ClassPathResource(largeModelIcon);
        IconUtil.sendPathResourceIcon(response, resource);
      }
      else {
        IconUtil.sendBase64Icon(response, largeModelIcon);
      }
    }
  }

  @Operation(summary = "获取租户的默认大模型")
  @GetMapping("getDefaultLargeModel")
  public ResultVO<Long> getDefaultLargeModel(@RequestParam("tenantId") Long tenantId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    return ResultVO.success(modelManageService.getDefaultLargeModel(tenantId));
  }

  @Operation(summary = "获取租户的默认大模型信息")
  @GetMapping("getDefaultLargeModelInfo")
  public ResultVO<LargeModelDTO> getDefaultLargeModelInfo(@RequestParam("tenantId") Long tenantId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Long modelId = tenantSettingInfoCache.getModelIdOrNull(tenantId);
    if (modelId == null) {
      return ResultVO.success(null);
    }
    LargeModelDTO model = modelManageService.getLargeModel(tenantId, modelId);
    model.setSourceFrom(
      BaseConsts.PLATFORM_TENANT_ID.equals(model.getTenantId()) ? BaseConsts.MODEL_SOURCE_PLATFORM : BaseConsts.MODEL_SOURCE_TENANT);
    model.setStatusCd(BaseConsts.STATUS_CD_VALID);
    ModelConfigUtil.parseExtConfig(model);
    // 对于租户的模型管理页面，去掉平台模型的密钥，避免泄露
    if (!BaseConsts.PLATFORM_TENANT_ID.equals(tenantId) && BaseConsts.PLATFORM_TENANT_ID.equals(model.getTenantId())) {
      model.setAccessKey(null);
    }
    return ResultVO.success(model);
  }

  @Operation(summary = "设置租户的默认大模型")
  @PostMapping("setDefaultLargeModel")
  public ResultVO<Void> setDefaultLargeModel(@RequestParam("tenantId") Long tenantId, @RequestParam("modelId") Long modelId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(modelId, "模型 ID 不能为空");
    modelManageService.setDefaultLargeModel(tenantId, modelId);
    // 刷新缓存
    refreshCacheService.refresh(CacheConsts.CACHE_NAME_TENANT_SETTING, tenantId.toString());
    return ResultVO.success();
  }

  @Operation(summary = "查询个人模型分页列表")
  @PostMapping("queryPersonalLargeModelPage")
  public ResultVO<PageInfo<LargeModelDTO>> queryPersonalLargeModelPage(@RequestBody LargeModelQueryParams queryParams) {
    Assert.notNull(queryParams.getSpaceId(), "空间 ID 不能为空");
    queryParams.setTenantId(queryParams.getSpaceId());
    return ResultVO.success(modelManageService.queryPersonalLargeModelPage(queryParams));
  }

  @Operation(summary = "保存个人大模型")
  @PostMapping("savePersonalLargeModel")
  public ResultVO<LargeModelDTO> savePersonalLargeModel(@RequestBody LargeModelDTO model) {
    Assert.notNull(model.getSpaceId(), "空间 ID 不能为空");
    model.setTenantId(model.getSpaceId());
    boolean isUpdate = model.getModelId() != null;
    // 默认支持函数调用、流式输出
    model.setFunctionCallMode(FunctionCallMode.TOOL.name().toLowerCase());
    model.setStreamingFunctionCall(BaseConsts.TRUE);
    ResultVO<LargeModelDTO> result = modelManageService.saveLargeModel(model);
    // 更新时需要刷新客户端缓存
    if (isUpdate && result.isSuccess()) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_MODEL_CLIENT, model.getTenantId() + CacheConsts.COLON + model.getModelId());
    }
    return result;
  }

  @Operation(summary = "删除个人大模型")
  @GetMapping("deletePersonalLargeModel")
  public ResultVO<Void> deletePersonalLargeModel(@RequestParam("modelId") Long modelId,
                                                 @RequestParam("spaceId") Long spaceId) {
    Assert.notNull(modelId, "模型 ID 不能为空");
    Assert.notNull(spaceId, "空间 ID 不能为空");
    return modelManageService.deleteLargeModel(spaceId, modelId);
  }

  @Operation(summary = "修改个人大模型是否启用状态")
  @GetMapping("updatePersonalLargeModelStatus")
  public ResultVO<Void> updatePersonalLargeModelStatus(@RequestParam("spaceId") Long spaceId, @RequestParam("modelId") Long modelId,
                                                      @RequestParam("enabled") String enabled) {
    Assert.notNull(spaceId, "空间 ID 不能为空");
    Assert.notNull(modelId, "模型 ID 不能为空");
    Assert.hasText(enabled, "模型状态不能为空");
    modelManageService.modifyPersonalLargeModelStatus(spaceId, modelId, enabled);
    return ResultVO.success();
  }
}
