package com.iwhalecloud.bote.service.model.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.cache.GeneraAgentIdCache;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.util.ModelConfigUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.agent.AiModelDTO;
import com.iwhalecloud.bote.dto.model.GatewayModelSyncResult;
import com.iwhalecloud.bote.dto.model.LargeModelDTO;
import com.iwhalecloud.bote.dto.model.LargeModelExtConfigDTO;
import com.iwhalecloud.bote.dto.model.SimpleLargeModelDTO;
import com.iwhalecloud.bote.dto.model.query.LargeModelQueryParams;
import com.iwhalecloud.bote.dto.portal.TenantSettingInfoDTO;
import com.iwhalecloud.bote.dto.tenant.setting.TenantLargeModelSettingDTO;
import com.iwhalecloud.bote.llm.client.dto.HeaderItem;
import com.iwhalecloud.bote.mapper.agent.AiModelManageMapper;
import com.iwhalecloud.bote.mapper.model.LargeModelManageMapper;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bote.service.model.ILargeModelManageService;
import com.iwhalecloud.bote.service.model.helper.GatewayModelSyncHelper;
import com.iwhalecloud.bote.service.portal.ITenantSettingInfoManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.JsonUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 大模型管理服务
 *
 * @author qian.sisheng
 * @since 2024/8/7
 */

@Service
@RequiredArgsConstructor
public class LargeModelManageServiceImpl implements ILargeModelManageService {
  private final LargeModelManageMapper modelManageMapper;
  private final AiModelManageMapper aiModelManageMapper;
  private final ITenantSettingInfoManageService tenantSettingService;
  private final IResourceElementService resourceElementService;
  private final TenantSettingInfoCache tenantSettingInfoCache;
  private final GeneraAgentIdCache generaAgentIdCache;
  private final GatewayModelSyncHelper gatewayModelSyncHelper;

  @Override
  public LargeModelDTO getLargeModel(Long tenantId, Long modelId) {
    LargeModelDTO model = modelManageMapper.getLargeModel(tenantId, modelId);
    Assert.notNull(model, () -> "大模型不存在: modelId=" + modelId);
    return model;
  }

  @Override
  @Transactional
  public ResultVO<LargeModelDTO> saveLargeModel(LargeModelDTO model) {
    fillPersonalModelData(model);
    validateModel(model);
    LargeModelDTO old = model.getModelId() == null ? null : getLargeModel(model.getTenantId(), model.getModelId());
    // 网关来源靠 protocolExt.fromGateway 判定；前端现传 sourceFrom=gateway，需写回扩展字段，避免保存后变成「项目」
    applySourceFromOnSave(model, old);
    model.setStatusCd(BaseConsts.STATUS_CD_VALID);
    DataDifference<LargeModelDTO> difference = DataDifferenceStarter.computeSave(old, model, false, model.getTenantId());
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  /**
   * 解析并落库模型来源。gateway 不是库字段，依赖 ext_attr_json.protocolExt.fromGateway。
   */
  private void applySourceFromOnSave(LargeModelDTO model, @Nullable LargeModelDTO old) {
    if (BaseConsts.PLATFORM_TENANT_ID.equals(model.getTenantId())) {
      model.setSourceFrom(BaseConsts.MODEL_SOURCE_PLATFORM);
      return;
    }
    String requested = model.getSourceFrom();
    boolean requestGateway = BaseConsts.MODEL_SOURCE_GATEWAY.equals(requested);
    boolean requestTenant = BaseConsts.MODEL_SOURCE_TENANT.equals(requested);
    LargeModelExtConfigDTO oldExt = readExtConfig(old != null ? old.getExtAttrJson() : null);
    Map<String, Object> oldProtocolExt = oldExt != null ? oldExt.getProtocolExt() : null;
    boolean oldGateway = isFromGateway(oldProtocolExt) || isFromGateway(model.getProtocolExt());
    if (requestGateway || (!requestTenant && oldGateway)) {
      model.setSourceFrom(BaseConsts.MODEL_SOURCE_GATEWAY);
      ensureGatewayExtOnSave(model, oldExt);
      ModelConfigUtil.saveExtConfig(model);
      return;
    }
    model.setSourceFrom(BaseConsts.MODEL_SOURCE_TENANT);
  }

  private static void ensureGatewayExtOnSave(LargeModelDTO model, @Nullable LargeModelExtConfigDTO oldExt) {
    // 编辑表单常丢扩展字段：用库里旧值兜底，再强制 fromGateway
    if (CollectionUtils.isEmpty(model.getHeaders()) && oldExt != null && CollectionUtils.isNotEmpty(oldExt.getHeaders())) {
      model.setHeaders(oldExt.getHeaders());
    }
    if (StringUtils.isEmpty(model.getExtReqParamsJson()) && oldExt != null && StringUtils.isNotEmpty(oldExt.getExtReqParamsJson())) {
      model.setExtReqParamsJson(oldExt.getExtReqParamsJson());
    }
    Map<String, Object> protocolExt = model.getProtocolExt();
    if (protocolExt == null) {
      protocolExt = new HashMap<>();
      model.setProtocolExt(protocolExt);
    }
    Map<String, Object> oldProtocolExt = oldExt != null ? oldExt.getProtocolExt() : null;
    if (oldProtocolExt != null) {
      for (String key : List.of("gatewayModelId", "gatewayOffline", "aimarket")) {
        if (!protocolExt.containsKey(key) && oldProtocolExt.containsKey(key)) {
          protocolExt.put(key, oldProtocolExt.get(key));
        }
      }
    }
    protocolExt.put("fromGateway", true);
  }

  private static boolean isFromGateway(@Nullable Map<String, Object> protocolExt) {
    if (protocolExt == null || protocolExt.isEmpty()) {
      return false;
    }
    Object flag = protocolExt.get("fromGateway");
    if (flag instanceof Boolean) {
      return (Boolean) flag;
    }
    return flag != null && Boolean.parseBoolean(String.valueOf(flag));
  }

  @Nullable
  private static LargeModelExtConfigDTO readExtConfig(@Nullable String extAttrJson) {
    if (StringUtils.isBlank(extAttrJson)) {
      return null;
    }
    try {
      return JsonUtil.parseJson(extAttrJson, LargeModelExtConfigDTO.class);
    }
    catch (RuntimeException ignored) {
      return null;
    }
  }

  /**
   * 填充个人模型缺省数据
   */
  private void fillPersonalModelData(LargeModelDTO model) {
    if (!BaseConsts.DATA_FROM_MODEL_PERSON.equals(model.getDataFrom())) {
      return;
    }
    // 模型名称: 取模型编码
    if (StringUtils.isEmpty(model.getModelName())) {
      model.setModelName(model.getModelCode());
    }
    // 模型图标
    if (StringUtils.isEmpty(model.getModelIcon())) {
      model.setModelIcon("./images/model/ico-model-1.png");
    }
    // 模型类型
    if (StringUtils.isEmpty(model.getModelType())) {
      model.setModelType(ModelConsts.MODEL_TYPE_LLM);
    }
    // 模型协议类型
    if (StringUtils.isEmpty(model.getProtocolType())) {
      model.setProtocolType(ModelConsts.MODEL_PROTOCOL_OPENAI);
    }
    // 默认不启用
    if (StringUtils.isEmpty(model.getIsEnabled())) {
      model.setIsEnabled(BaseConsts.FALSE);
    }
  }

  /**
   * 校验模型配置
   */
  private void validateModel(LargeModelDTO model) {
    Assert.hasLength(model.getModelName(), "名称不能为空");
    Assert.hasLength(model.getModelType(), "模型分类不能为空");
    Assert.isTrue(ModelConsts.MODEL_TYPES.contains(model.getModelType()), () -> "未知的模型分类: " + model.getModelType());

    ModelConfigUtil.validate(model);

    for (HeaderItem item : ListUtils.emptyIfNull(model.getHeaders())) {
      Assert.hasLength(item.getName(), "请求头的名称不能为空");
      Assert.isTrue(!StringUtils.containsWhitespace(item.getName()), "请求头的名称不能包含空白字符");
    }
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteLargeModel(Long tenantId, Long modelId) {
    if (resourceElementService.existsRelatedResource(tenantId, modelId, DataSyncCodeEnum.MODEL.getCode())) {
      return ResultVO.fail("大模型已存在关联配置数据，不允许删除");
    }
    modelManageMapper.deleteLargeModel(tenantId, modelId, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  public PageInfo<LargeModelDTO> queryLargeModelPage(LargeModelQueryParams queryParams) {
    gatewayModelSyncHelper.tryAutoSync(queryParams.getTenantId(), queryParams.getSpaceId());
    RowBounds rowBounds = queryParams.buildRowBounds();
    //noinspection resource
    return modelManageMapper.selectLargeModelPage(queryParams, rowBounds).toPageInfo();
  }

  @Override
  public List<SimpleLargeModelDTO> queryLargeModelList(Long tenantId, @Nullable String modelType, @Nullable String sourceFrom) {
    gatewayModelSyncHelper.tryAutoSync(tenantId, null);
    List<SimpleLargeModelDTO> models = modelManageMapper.selectLargeModelList(tenantId, modelType, sourceFrom);
    if (CollectionUtils.isEmpty(models)) {
      return models;
    }
    Long modelId = tenantSettingInfoCache.getModelIdOrNull(tenantId);
    if (modelId != null) {
      // 找到默认模型
      SimpleLargeModelDTO defaultModel = models.stream()
        .filter(model -> modelId.equals(model.getModelId()))
        .findFirst()
        .orElse(null);
      // 如果找到了默认模型，则将其移到列表首位
      if (defaultModel != null) {
        models.remove(defaultModel);
        models.addFirst(defaultModel);
      }
    }
    return models;
  }

  @Override
  public List<SimpleLargeModelDTO> queryAiLargeModelList(Long tenantId, Long botId, @Nullable String modelType) {
    Long userId = generaAgentIdCache.getBotOnwerUserId(tenantId, botId, SessionUtil.getLoginInfo().getUserId());
    List<SimpleLargeModelDTO> models = modelManageMapper.selectAiLargeModelList(tenantId, botId, modelType, userId);
    if (CollectionUtils.isEmpty(models)) {
      return models;
    }
    if (modelType != null) {
      AiModelDTO btAiModelDTO = aiModelManageMapper.getAiModel(tenantId, null, userId, modelType);
      if (btAiModelDTO != null) {
        // 找到默认模型
        SimpleLargeModelDTO defaultModel = models.stream()
          .filter(model -> btAiModelDTO.getModelId().equals(model.getModelId())).findFirst().orElse(null);
        // 如果找到了默认模型，则将其移到列表首位
        if (defaultModel != null) {
          models.remove(defaultModel);
          models.addFirst(defaultModel);
        }
      }
    }
    return models;
  }

  @Nullable
  @Override
  public Long getDefaultLargeModel(Long tenantId) {
    TenantSettingInfoDTO settingInfo = tenantSettingService.findTenantSettingInfo(tenantId, BaseConsts.FUNC_DEFAULT_TYPE_LARGE_MODEL);
    if (settingInfo != null && StringUtils.isNotEmpty(settingInfo.getSettingInfo())) {
      TenantLargeModelSettingDTO dto = JsonUtil.parseJsonRequired(settingInfo.getSettingInfo(), TenantLargeModelSettingDTO.class);
      return dto.getLargeModelId();
    }
    return null;
  }

  @Transactional
  @Override
  public void setDefaultLargeModel(Long tenantId, Long modelId) {
    TenantSettingInfoDTO tenantSettingInfo = tenantSettingService.findTenantSettingInfo(tenantId, BaseConsts.FUNC_DEFAULT_TYPE_LARGE_MODEL);
    if (tenantSettingInfo == null) {
      tenantSettingInfo = new TenantSettingInfoDTO();
      tenantSettingInfo.setTenantId(tenantId);
      tenantSettingInfo.setFuncType(BaseConsts.FUNC_DEFAULT_TYPE_LARGE_MODEL);
    }
    TenantLargeModelSettingDTO dto = new TenantLargeModelSettingDTO();
    dto.setLargeModelId(modelId);
    tenantSettingInfo.setSettingInfo(JsonUtil.toJsonString(dto));
    tenantSettingService.saveTenantSettingInfo(tenantSettingInfo);
  }

  @Override
  public String getLargeModelIcon(Long modelId, Long tenantId) {
    return modelManageMapper.getLargeModelIcon(modelId, tenantId);
  }

  @Override
  @Transactional
  public void modifyPersonalLargeModelStatus(Long spaceId, Long modelId, String enabled) {
    modelManageMapper.updateLargeModelEnabledStatus(spaceId, modelId, enabled, SessionUtil.getLoginInfo().getUserId());
  }

  @Override
  public PageInfo<LargeModelDTO> queryPersonalLargeModelPage(LargeModelQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    queryParams.setUserId(SessionUtil.getLoginInfo().getUserId());
    //noinspection resource
    return modelManageMapper.selectPersonalLargeModelPage(queryParams, rowBounds).toPageInfo();
  }

  @Override
  @Nullable
  public SimpleLargeModelDTO queryEnabledLargeModel(Long tenantId) {
    return modelManageMapper.selectEnabledLargeModelIgnoreDataFrom(tenantId);
  }

  @Override
  public Map<String, SimpleLargeModelDTO> queryPlatformAndSpaceEnabledLargeModel(@Nullable Long spaceTenantId) {
    Map<String, SimpleLargeModelDTO> modelMap = new HashMap<>();
    // 获取已启用的平台大模型
    modelMap.put("platform", queryEnabledLargeModel(BaseConsts.PLATFORM_TENANT_ID));
    if (spaceTenantId != null) {
      // 获取已启用的空间大模型
      modelMap.put("space", queryEnabledLargeModel(spaceTenantId));
    }
    return modelMap;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void modifyLargeModelStatus(Long tenantId, Long modelId, String enabled) {
    // 如果是启用状态，则检查是否已存在启用的大模型
    if (BaseConsts.TRUE.equals(enabled)) {
      SimpleLargeModelDTO enabledLargeModel = queryEnabledLargeModel(tenantId);
      // 如果已经存在启用的大模型，需要先取消启用
      if (enabledLargeModel != null) {
        modelManageMapper.updateLargeModelEnabledStatus(tenantId, enabledLargeModel.getModelId(), BaseConsts.FALSE, SessionUtil.getLoginInfo().getUserId());
      }
    }
    modelManageMapper.updateLargeModelEnabledStatus(tenantId, modelId, enabled, SessionUtil.getLoginInfo().getUserId());
  }

  @Override
  public GatewayModelSyncResult syncGatewayModels(Long tenantId, Long spaceId) {
    return gatewayModelSyncHelper.sync(tenantId, spaceId);
  }
}
