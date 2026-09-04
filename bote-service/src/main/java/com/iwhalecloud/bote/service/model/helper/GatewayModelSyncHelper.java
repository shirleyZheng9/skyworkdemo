package com.iwhalecloud.bote.service.model.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iwhalecloud.bote.cache.AttrSpecCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.util.ModelConfigUtil;
import com.iwhalecloud.bote.config.properties.TiangongAiGatewayProperties;
import com.iwhalecloud.bote.dto.base.SimpleAttrDTO;
import com.iwhalecloud.bote.dto.model.GatewayModelSyncResult;
import com.iwhalecloud.bote.dto.model.LargeModelDTO;
import com.iwhalecloud.bote.dto.model.gateway.AimarketModelRecord;
import com.iwhalecloud.bote.mapper.model.LargeModelManageMapper;
import com.iwhalecloud.bote.mapper.portal.TenantQueryMapper;
import com.iwhalecloud.bote.mapper.workspace.WorkspaceManageMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 天工 AI 网关模型同步
 *
 * @author jiangm
 * @since 2026-08-06
 */
@Component
@RequiredArgsConstructor
public class GatewayModelSyncHelper {
  private static final Logger logger = LoggerFactory.getLogger(GatewayModelSyncHelper.class);

  private static final String ATTR_MODEL_SOURCE_FROM = "MODEL_SOURCE_FROM";
  private static final String EXT_FROM_GATEWAY = "fromGateway";
  private static final String EXT_GATEWAY_MODEL_ID = "gatewayModelId";
  private static final String EXT_GATEWAY_OFFLINE = "gatewayOffline";
  private static final String EXT_AIMARKET = "aimarket";
  /** 网关同步默认产品系列：OpenAI（字典 MODEL_PRODUCT_TYPE） */
  private static final String DEFAULT_PRODUCT_TYPE = "1400";
  private static final int MODEL_DESC_MAX_LEN = 255;
  private static final int ACCESS_URL_MAX_LEN = 255;
  private static final int ACCESS_KEY_MAX_LEN = 255;

  private final TiangongAiMarketClient aiMarketClient;
  private final TiangongAiGatewayProperties properties;
  private final LargeModelManageMapper modelManageMapper;
  private final WorkspaceManageMapper workspaceManageMapper;
  private final TenantQueryMapper tenantQueryMapper;
  private final AttrSpecCache attrSpecCache;
  /** 租户最近一次自动同步时间戳（毫秒） */
  private final ConcurrentHashMap<Long, Long> lastAutoSyncAt = new ConcurrentHashMap<>();

  /**
   * 查询列表前自动同步（字典含 gateway 才启用；失败不影响查询）。
   *
   * @param tenantId 项目租户
   * @param spaceId Bote 空间 ID，可空（空则按 tenantId 反查）
   */
  public void tryAutoSync(Long tenantId, @Nullable Long spaceId) {
    if (tenantId == null || BaseConsts.PLATFORM_TENANT_ID.equals(tenantId)) {
      return;
    }
    if (!isGatewaySourceEnabled()) {
      return;
    }
    if (StringUtils.isBlank(properties.getBaseUrl()) || StringUtils.isBlank(properties.getAccessUrl())) {
      logger.debug("Skip gateway auto sync: ai-gateway baseUrl/accessUrl not configured");
      return;
    }
    long now = System.currentTimeMillis();
    long intervalMs = Math.max(0, properties.getSyncIntervalSeconds()) * 1000L;
    // 先占位节流时间，避免并发请求同时插入重复网关模型
    AtomicBoolean claimed = new AtomicBoolean(false);
    AtomicReference<Long> previous = new AtomicReference<>();
    lastAutoSyncAt.compute(tenantId, (id, last) -> {
      if (last != null && now - last < intervalMs) {
        return last;
      }
      previous.set(last);
      claimed.set(true);
      return now;
    });
    if (!claimed.get()) {
      return;
    }
    Long resolvedSpaceId = spaceId;
    if (resolvedSpaceId == null) {
      resolvedSpaceId = tenantQueryMapper.getSpaceId(tenantId);
    }
    if (resolvedSpaceId == null) {
      // 未真正同步，回滚占位，允许后续重试
      restoreAutoSyncStamp(tenantId, now, previous.get());
      logger.debug("Skip gateway auto sync: cannot resolve spaceId for tenantId={}", tenantId);
      return;
    }
    try {
      sync(tenantId, resolvedSpaceId);
    }
    catch (Exception e) {
      restoreAutoSyncStamp(tenantId, now, previous.get());
      logger.warn("Gateway auto sync failed, tenantId={}, spaceId={}: {}", tenantId, resolvedSpaceId, e.getMessage());
    }
  }

  private void restoreAutoSyncStamp(Long tenantId, long claimedAt, @Nullable Long previous) {
    if (previous == null) {
      lastAutoSyncAt.remove(tenantId, claimedAt);
    }
    else {
      lastAutoSyncAt.replace(tenantId, claimedAt, previous);
    }
  }

  /**
   * 字典 MODEL_SOURCE_FROM 是否配置了 gateway（作为功能开关）
   */
  public boolean isGatewaySourceEnabled() {
    List<SimpleAttrDTO> attrs = attrSpecCache.get(BaseConsts.PLATFORM_TENANT_ID, ATTR_MODEL_SOURCE_FROM);
    if (CollectionUtils.isEmpty(attrs)) {
      return false;
    }
    return attrs.stream().anyMatch(a -> BaseConsts.MODEL_SOURCE_GATEWAY.equals(a.getAttrValue()));
  }

  /**
   * 同步网关模型到项目租户。
   *
   * @param tenantId 项目租户 ID（模型写入）
   * @param spaceId Bote 空间 ID（需映射为外系统 extSpaceId 后再查 AI Key）
   */
  @Transactional
  public GatewayModelSyncResult sync(Long tenantId, Long spaceId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(spaceId, "空间 ID 不能为空");
    Assert.hasText(properties.getAccessUrl(), "未配置 bote.tg.ai-gateway.access-url");

    Long enterpriseId = resolveEnterpriseId(spaceId);
    // 先取 AI Key：取不到则整次同步跳过，不再拉模型列表
    String accessKey;
    try {
      List<String> aiKeys = aiMarketClient.listAiKeys(enterpriseId);
      accessKey = CollectionUtils.isEmpty(aiKeys) ? "" : aiKeys.get(0);
    }
    catch (Exception e) {
      logger.warn("Skip gateway sync: query AI key failed. spaceId={}, enterpriseId(extSpaceId)={}, error={}",
        spaceId, enterpriseId, e.getMessage());
      return new GatewayModelSyncResult();
    }
    if (StringUtils.isBlank(accessKey)) {
      logger.warn("Skip gateway sync: no AI key found. spaceId={}, enterpriseId(extSpaceId)={}",
        spaceId, enterpriseId);
      return new GatewayModelSyncResult();
    }

    List<AimarketModelRecord> remoteModels = aiMarketClient.listAllActiveModels();

    List<LargeModelDTO> localGatewayModels = modelManageMapper.selectGatewayLargeModels(tenantId);
    Map<String, LargeModelDTO> localByGatewayId = new HashMap<>();
    for (LargeModelDTO local : CollectionUtils.emptyIfNull(localGatewayModels)) {
      String gatewayId = readGatewayModelId(local.getExtAttrJson());
      if (StringUtils.isNotBlank(gatewayId)) {
        localByGatewayId.put(gatewayId, local);
      }
    }

    GatewayModelSyncResult result = new GatewayModelSyncResult();
    Set<String> remoteIds = new HashSet<>();
    String accessUrl = truncate(properties.getAccessUrl().trim(), ACCESS_URL_MAX_LEN);
    String trimmedKey = truncate(accessKey, ACCESS_KEY_MAX_LEN);

    for (AimarketModelRecord remote : remoteModels) {
      if (remote == null || StringUtils.isBlank(remote.getId())) {
        continue;
      }
      String mappedType = mapModelType(remote.getModelType());
      if (mappedType == null) {
        logger.info("Skip aimarket model id={}, unsupported modelType={}", remote.getId(), remote.getModelType());
        continue;
      }
      if (StringUtils.isBlank(remote.getApiModelId())) {
        logger.info("Skip aimarket model id={}, apiModelId is blank", remote.getId());
        continue;
      }
      remoteIds.add(remote.getId());
      LargeModelDTO existing = localByGatewayId.get(remote.getId());
      if (existing == null) {
        LargeModelDTO created = buildModel(tenantId, remote, mappedType, accessUrl, trimmedKey, false);
        if (persist(created)) {
          result.setAdded(result.getAdded() + 1);
        }
        continue;
      }

      boolean needUpdate = isCoreChanged(existing, remote, mappedType)
        || isExtOrKeyChanged(existing, remote, accessUrl, trimmedKey)
        || isGatewayOffline(existing.getExtAttrJson())
        || !BaseConsts.STATUS_CD_VALID.equals(existing.getStatusCd());
      if (!needUpdate) {
        continue;
      }

      applyRemoteFields(existing, remote, mappedType, accessUrl, trimmedKey, false);
      existing.setStatusCd(BaseConsts.STATUS_CD_VALID);
      if (persist(existing)) {
        result.setUpdated(result.getUpdated() + 1);
      }
    }

    for (Map.Entry<String, LargeModelDTO> entry : localByGatewayId.entrySet()) {
      if (remoteIds.contains(entry.getKey())) {
        continue;
      }
      LargeModelDTO offline = entry.getValue();
      boolean alreadyOffline = isGatewayOffline(offline.getExtAttrJson())
        && BaseConsts.STATUS_CD_INVALID.equals(offline.getStatusCd());
      if (alreadyOffline) {
        continue;
      }
      ObjectNode root = parseExtObject(offline.getExtAttrJson());
      ObjectNode protocolExt = root.has("protocolExt") && root.get("protocolExt").isObject()
        ? (ObjectNode) root.get("protocolExt")
        : root.putObject("protocolExt");
      protocolExt.put(EXT_FROM_GATEWAY, true);
      protocolExt.put(EXT_GATEWAY_MODEL_ID, entry.getKey());
      protocolExt.put(EXT_GATEWAY_OFFLINE, true);
      offline.setExtAttrJson(JsonUtil.toJsonStringCompact(root));
      // 软删：页面列表只查 00A，改成 00X 后不可见；远端恢复后再改回 00A
      offline.setStatusCd(BaseConsts.STATUS_CD_INVALID);
      if (persist(offline)) {
        result.setOfflineMarked(result.getOfflineMarked() + 1);
      }
    }
    return result;
  }

  private boolean persist(LargeModelDTO model) {
    try {
      ModelConfigUtil.validate(model);
    }
    catch (IllegalArgumentException | IllegalStateException e) {
      logger.warn("Skip invalid gateway model code={}: {}", model.getModelCode(), e.getMessage());
      return false;
    }
    model.setSourceFrom(BaseConsts.MODEL_SOURCE_GATEWAY);
    if (StringUtils.isBlank(model.getStatusCd())) {
      model.setStatusCd(BaseConsts.STATUS_CD_VALID);
    }
    // 含 00X：网关下架软删后再上架时需读到旧记录做 UPDATE，不能走只查 00A 的 getLargeModel
    LargeModelDTO old = model.getModelId() == null ? null
      : modelManageMapper.getLargeModelIgnoreStatus(model.getTenantId(), model.getModelId());
    DataDifference<LargeModelDTO> difference = DataDifferenceStarter.computeSave(old, model, false, model.getTenantId());
    if (difference == null) {
      logger.debug("No difference for gateway model code={}", model.getModelCode());
      return false;
    }
    return true;
  }

  /**
   * Bote spaceId → 外系统 extSpaceId（作为 enterpriseId）
   */
  private Long resolveEnterpriseId(Long spaceId) {
    String extSpaceId = workspaceManageMapper.getExtSpaceIdBySpaceId(spaceId);
    Assert.hasText(extSpaceId, () -> "空间未关联外系统空间 ID，无法查询企业 AI Key: spaceId=" + spaceId);
    try {
      return Long.valueOf(extSpaceId.trim());
    }
    catch (NumberFormatException e) {
      throw new BssException("外系统空间 ID 非法，无法作为 enterpriseId: spaceId=" + spaceId + ", extSpaceId=" + extSpaceId);
    }
  }

  private LargeModelDTO buildModel(Long tenantId, AimarketModelRecord remote, String modelType,
                                   String accessUrl, String accessKey, boolean offline) {
    LargeModelDTO model = new LargeModelDTO();
    model.setTenantId(tenantId);
    model.setIsPublic(BaseConsts.FALSE);
    model.setIsEnabled(BaseConsts.FALSE);
    applyRemoteFields(model, remote, modelType, accessUrl, accessKey, offline);
    return model;
  }

  private void applyRemoteFields(LargeModelDTO model, AimarketModelRecord remote, String modelType,
                                 String accessUrl, String accessKey, boolean offline) {
    model.setModelCode(remote.getApiModelId());
    model.setModelName(StringUtils.defaultIfBlank(remote.getName(), remote.getApiModelId()));
    model.setModelDesc(truncate(StringUtils.defaultString(remote.getBrief()), MODEL_DESC_MAX_LEN));
    model.setModelIcon(remote.getLogoUrl());
    model.setModelType(modelType);
    model.setProtocolType(ModelConsts.MODEL_PROTOCOL_OPENAI);
    model.setProductType(DEFAULT_PRODUCT_TYPE);
    model.setAccessUrl(accessUrl);
    model.setAccessKey(accessKey);
    model.setContextLength(parseContextLength(remote.getContextWindow()));
    model.setExtAttrJson(buildExtAttrJson(remote, offline));
  }

  private boolean isCoreChanged(LargeModelDTO local, AimarketModelRecord remote, String mappedType) {
    return !Objects.equals(local.getModelCode(), remote.getApiModelId())
      || !Objects.equals(local.getModelName(), StringUtils.defaultIfBlank(remote.getName(), remote.getApiModelId()))
      || !Objects.equals(StringUtils.defaultString(local.getModelDesc()),
      truncate(StringUtils.defaultString(remote.getBrief()), MODEL_DESC_MAX_LEN))
      || !Objects.equals(StringUtils.defaultString(local.getModelIcon()), StringUtils.defaultString(remote.getLogoUrl()))
      || !Objects.equals(local.getModelType(), mappedType)
      || !Objects.equals(local.getContextLength(), parseContextLength(remote.getContextWindow()))
      || !Objects.equals(StringUtils.defaultString(local.getProductType()), DEFAULT_PRODUCT_TYPE);
  }

  private boolean isExtOrKeyChanged(LargeModelDTO local, AimarketModelRecord remote, String accessUrl, String accessKey) {
    String newExt = buildExtAttrJson(remote, isGatewayOffline(local.getExtAttrJson()));
    return !Objects.equals(normalizeJson(local.getExtAttrJson()), normalizeJson(newExt))
      || !Objects.equals(StringUtils.defaultString(local.getAccessUrl()), accessUrl)
      || !Objects.equals(StringUtils.defaultString(local.getAccessKey()), StringUtils.defaultString(accessKey));
  }

  private static String buildExtAttrJson(AimarketModelRecord remote, boolean offline) {
    Map<String, Object> protocolExt = new HashMap<>();
    protocolExt.put(EXT_FROM_GATEWAY, true);
    protocolExt.put(EXT_GATEWAY_MODEL_ID, remote.getId());
    protocolExt.put(EXT_GATEWAY_OFFLINE, offline);
    Map<String, Object> aimarket = new HashMap<>();
    if (remote.getParamSpec() != null) {
      aimarket.put("paramSpec", remote.getParamSpec());
    }
    if (remote.getMarketTag() != null) {
      aimarket.put("marketTag", remote.getMarketTag());
    }
    if (remote.getModelTag() != null) {
      aimarket.put("modelTag", remote.getModelTag());
    }
    if (remote.getModelProductPriceVo() != null) {
      aimarket.put("modelProductPriceVo", remote.getModelProductPriceVo());
    }
    if (!aimarket.isEmpty()) {
      protocolExt.put(EXT_AIMARKET, aimarket);
    }
    Map<String, Object> root = new HashMap<>(2);
    root.put("protocolExt", protocolExt);
    return JsonUtil.toJsonStringCompact(root);
  }

  @Nullable
  private static String mapModelType(String aimarketType) {
    if (StringUtils.isBlank(aimarketType)) {
      return null;
    }
    // 仅同步对话类模型；embedding / language / video 等一律跳过
    if ("chat".equalsIgnoreCase(aimarketType) || ModelConsts.MODEL_TYPE_LLM.equalsIgnoreCase(aimarketType)) {
      return ModelConsts.MODEL_TYPE_LLM;
    }
    return null;
  }

  @Nullable
  private static Integer parseContextLength(String contextWindow) {
    if (StringUtils.isBlank(contextWindow)) {
      return null;
    }
    try {
      return Integer.parseInt(contextWindow.trim());
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  @Nullable
  private static String readGatewayModelId(String extAttrJson) {
    JsonNode protocolExt = readProtocolExt(extAttrJson);
    if (protocolExt == null || !protocolExt.has(EXT_GATEWAY_MODEL_ID)) {
      return null;
    }
    String id = protocolExt.path(EXT_GATEWAY_MODEL_ID).asText(null);
    return StringUtils.isBlank(id) ? null : id;
  }

  private static boolean isGatewayOffline(String extAttrJson) {
    JsonNode protocolExt = readProtocolExt(extAttrJson);
    return protocolExt != null && protocolExt.path(EXT_GATEWAY_OFFLINE).asBoolean(false);
  }

  @Nullable
  private static JsonNode readProtocolExt(String extAttrJson) {
    if (StringUtils.isBlank(extAttrJson)) {
      return null;
    }
    JsonNode node = JsonUtil.readTree(extAttrJson);
    if (node == null) {
      return null;
    }
    JsonNode protocolExt = node.get("protocolExt");
    return protocolExt != null && !protocolExt.isNull() ? protocolExt : null;
  }

  private static ObjectNode parseExtObject(String extAttrJson) {
    if (StringUtils.isBlank(extAttrJson)) {
      return JsonUtil.getObjectMapper().createObjectNode();
    }
    JsonNode node = JsonUtil.readTree(extAttrJson);
    if (node instanceof ObjectNode objectNode) {
      return objectNode.deepCopy();
    }
    return JsonUtil.getObjectMapper().createObjectNode();
  }

  private static String normalizeJson(String json) {
    if (StringUtils.isBlank(json)) {
      return "";
    }
    JsonNode node = JsonUtil.readTree(json);
    return node == null ? json : JsonUtil.toJsonStringCompact(node);
  }

  private static String truncate(String value, int maxLen) {
    if (value == null) {
      return null;
    }
    if (value.length() <= maxLen) {
      return value;
    }
    return value.substring(0, maxLen);
  }
}
