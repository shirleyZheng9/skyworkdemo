package com.iwhalecloud.bote.doc.module.knowledge.controller;

import com.github.pagehelper.PageInfo;
import com.google.common.base.Suppliers;
import com.iwhalecloud.bote.common.annotation.RequestCacheable;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.common.lock.DistributedLock;
import com.iwhalecloud.bote.common.lock.DistributedLockFactory;
import com.iwhalecloud.bote.common.lock.LockHelper;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.doc.cache.PinnedKnowledgeCache;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.space.SpaceContextHolder;
import com.iwhalecloud.bote.doc.common.tenant.annotation.IgnoreTenant;
import com.iwhalecloud.bote.doc.common.utils.KnowledgeClientUtil;
import com.iwhalecloud.bote.doc.consts.DocCacheConsts;
import com.iwhalecloud.bote.doc.consts.DocLockConsts;
import com.iwhalecloud.bote.doc.consts.KnowledgeConsts;
import com.iwhalecloud.bote.doc.module.knowledge.adapter.DocChainAdapter;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseSimpleDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeChatTestDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeComboboxDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeEnabledTypeDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeTypeDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.SimpleKnowledgeBaseDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.UpdateKnowlegeVisibilityScopeDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocKnowledgeBaseQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.KnowledgeComboboxQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.service.IKnowledgeBaseManageService;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocChainConfigHelper;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocChainTopicHelper;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.KnowledgeAnswerHelper;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeChatResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeInfoDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallParamDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallResponse;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainChatLogDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainExtraDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * 知识库管理 controller
 *
 * @author auto
 * @since 2024-09-20
 */
@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "manager/knowledge", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "知识库：基础管理")
@SuppressWarnings("PMD.GuardLogStatement")
public class KnowledgeBaseManageController {

  private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseManageController.class);

  // @formatter:off
  private final IKnowledgeBaseManageService knowledgeManageService;
  private final DocChainConfigHelper docChainConfigHelper;
  private final DocChainTopicHelper docChainTopicHelper;
  private final IRefreshCacheService refreshCacheService;
  private final PinnedKnowledgeCache pinnedKnowledgeCache;
  private final KnowledgeAnswerHelper knowledgeAnswerHelper;
  private final DocChainAdapter docChainAdapter;
  private final DistributedLockFactory distributedLockFactory;
  // @formatter:on

  /**
   * 知识库类型缓存
   */
  private final Supplier<List<KnowledgeTypeDTO>> knowledgeTypesCache = Suppliers.memoize(this::loadKnowledgeTypes);

  @Operation(summary = "获取知识库类型列表")
  @GetMapping("getKnowledgeTypes")
  public ResultVO<List<KnowledgeTypeDTO>> getKnowledgeTypes() {
    return ResultVO.success(knowledgeTypesCache.get());
  }

  /**
   * 加载知识库类型列表
   */
  private List<KnowledgeTypeDTO> loadKnowledgeTypes() {

    List<String> knowledgeTypeNames = KnowledgeClientUtil.getKnowledgeTypes();
    // 过滤第三方知识库
    knowledgeTypeNames.removeAll(KnowledgeConsts.OTHER_KNOWLEDGE_TYPES);
    List<KnowledgeTypeDTO> knowledgeTypes = new ArrayList<>(knowledgeTypeNames.size());
    for (String name : knowledgeTypeNames) {
      try (InputStream inputStream = new ClassPathResource("protocol/config/knowledge/" + name + ".json").getInputStream()) {
        knowledgeTypes.add(JsonUtil.parseJsonRequired(inputStream, KnowledgeTypeDTO.class));
      }
      catch (Exception e) {
        throw new BssException("加载知识库类型配置失败: knowledgeType=" + name, e);
      }
    }
    return knowledgeTypes;
  }

  @Operation(summary = "查询单个知识库")
  @GetMapping("findKnowledgeBase")
  public ResultVO<KnowledgeBaseDTO> findKnowledgeBase(@RequestParam(name = "tenantId") Long tenantId,
                                                      @RequestParam(name = "knowledgeId") Long knowledgeId) {
    Assert.notNull(knowledgeId, "主键 ID 不能为空");
    KnowledgeBaseDTO knowledge = knowledgeManageService.findKnowledgeBase(tenantId, knowledgeId);
    // 解析扩展配置
    knowledge.parseExtConfig();
    return ResultVO.success(knowledge);
  }

  @Operation(summary = "保存知识库基本信息")
  @PostMapping("saveKnowledgeBase")
  public ResultVO<KnowledgeBaseDTO> saveKnowledgeBase(@RequestBody @Valid KnowledgeBaseDTO knowledge) {
    // 保存扩展配置
    knowledge.saveExtConfig();
    Long knowledgeId = knowledge.getKnowledgeId();

    if (knowledge.getSpaceId() == null) {
      knowledge.setSpaceId(TenantIdUtil.getSpaceId(knowledge.getTenantId()));
    }

    // 保存知识库时均加锁：新建按名称锁，更新按知识库ID锁，避免并发重复提交
    boolean isCreate = knowledgeId == null;
    DistributedLock saveLock;
    String bizId;
    if (isCreate) {
      String nameKey = LockHelper.sanitizeLockKey(StringUtils.defaultString(knowledge.getKnowledgeName()).trim().toLowerCase());
      bizId = knowledge.getTenantId() + ":" + knowledge.getSpaceId() + ":" + nameKey;
    }
    else {
      bizId = knowledge.getTenantId() + ":" + knowledge.getSpaceId() + ":" + knowledgeId;
    }
    saveLock = distributedLockFactory.getBizLock(DocLockConsts.KNOWLEDGE_BASE_CREATE_LOCK, bizId);
    boolean saveLocked = saveLock.tryLock();
    if (!saveLocked) {
      return ResultVO.fail("正在处理中，请勿重复提交");
    }

    try {
      // 创建 DocChain 账号
      if (knowledge.isDocChainType() && !DocBaseConsts.TRUE.equals(knowledge.getIsExist()) && docChainConfigHelper.createDocChainAccount(
        knowledge.getTenantId())) {
        refreshCacheService.refresh(DocCacheConsts.CACHE_NAME_TENANT_SETTING, knowledge.getTenantId().toString());
      }
      ResultVO<KnowledgeBaseDTO> result = knowledgeManageService.saveKnowledgeBase(knowledge);
      if (!result.isSuccess()) {
        return new ResultVO<>(result);
      }
      // 刷新缓存
      refreshCache(knowledge, knowledgeId);
      return ResultVO.success(result.getResultObject());
    }
    finally {
      if (saveLocked) {
        saveLock.unlock();
      }
    }
  }

  /**
   * 刷新缓存
   */
  private void refreshCache(KnowledgeBaseDTO knowledge, Long knowledgeId) {
    // 修改时刷新缓存
    if (knowledgeId != null) {
      refreshCacheService.refresh(DocCacheConsts.CACHE_NAME_KNOWLEDGE, knowledge.getTenantId() + ":" + knowledge.getKnowledgeId());
    }
    // if (knowledge.getIsTopPinned() != null && knowledge.getIsTopPinned()) {
    pinnedKnowledgeCache.clearTenantCache(knowledge.getTenantId(), knowledge.getSpaceId());
    Long spaceTenantId = TenantIdUtil.getSpaceTenantId(knowledge.getSpaceId());
    if (spaceTenantId != null) {
      pinnedKnowledgeCache.clearTenantCache(spaceTenantId, knowledge.getSpaceId());
    }
  }

  @Operation(summary = "删除知识库")
  @GetMapping("deleteKnowledgeBase")
  public ResultVO<KnowledgeBaseDTO> deleteKnowledgeBase(@RequestParam(name = "tenantId") Long tenantId,
                                                        @RequestParam(name = "knowledgeId") Long knowledgeId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(knowledgeId, "主键 ID 不能为空");
    if (SpaceContextHolder.getRequiredSpaceId() == null) {
      SpaceContextHolder.setSpaceId(TenantIdUtil.getSpaceId(tenantId));
    }
    ResultVO<KnowledgeBaseDTO> result = knowledgeManageService.deleteKnowledgeBase(tenantId, knowledgeId);
    if (result.isSuccess()) {
      refreshCacheService.refresh(DocCacheConsts.CACHE_NAME_KNOWLEDGE, tenantId + ":" + knowledgeId);
      pinnedKnowledgeCache.clearTenantCache(tenantId, SpaceContextHolder.getSpaceId());
      Long spaceTenantId = TenantIdUtil.getSpaceTenantId(SpaceContextHolder.getSpaceId());
      if (spaceTenantId != null) {
        pinnedKnowledgeCache.clearTenantCache(spaceTenantId, SpaceContextHolder.getSpaceId());
      }
    }
    return result;
  }

  @Operation(summary = "重构知识库")
  @GetMapping("rebuild")
  public ResultVO<String> rebuild(@RequestParam(name = "tenantId") Long tenantId, @RequestParam(name = "knowledgeId") Long knowledgeId,
                                  @RequestParam(name = "newDoc", required = false) Boolean newDoc) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(knowledgeId, "主键 ID 不能为空");
    ResultVO<String> result = knowledgeManageService.rebuild(tenantId, knowledgeId, newDoc);
    if (result.isSuccess()) {
      refreshCacheService.refresh(DocCacheConsts.CACHE_NAME_KNOWLEDGE, tenantId + ":" + knowledgeId);
    }
    return result;
  }

  @Operation(summary = "查询知识库列表", description = "用于其他模块引用")
  @PostMapping("queryKnowledgeBaseList")
  public ResultVO<List<SimpleKnowledgeBaseDTO>> queryKnowledgeBaseList(@RequestBody DocKnowledgeBaseQueryParams queryParams) {
    return ResultVO.success(knowledgeManageService.queryKnowledgeBaseList(queryParams));
  }

  @Operation(summary = "查询知识库列表,知识飞轮", description = "知识飞轮")
  @PostMapping("querySimpleKnowledgeBaseList")
  public ResultVO<List<SimpleKnowledgeBaseDTO>> querySimpleKnowledgeBaseList(@RequestBody DocKnowledgeBaseQueryParams queryParams) {
    return ResultVO.success(knowledgeManageService.querySimpleKnowledgeBaseList(queryParams));
  }

  @Operation(summary = "分页查询知识库")
  @PostMapping("queryKnowledgeBasePage")
  public ResultVO<PageInfo<KnowledgeBaseDTO>> queryKnowledgeBasePage(@RequestBody DocKnowledgeBaseQueryParams queryParams) {
    return ResultVO.success(knowledgeManageService.queryKnowledgeBasePage(queryParams));
  }

  @Operation(summary = "根据知识库类型分页查询知识库")
  @PostMapping("queryKnowledgeBasePageByType")
  public ResultVO<PageInfo<KnowledgeBaseSimpleDTO>> queryKnowledgeBasePageByType(@RequestBody DocKnowledgeBaseQueryParams queryParams) {
    return ResultVO.success(knowledgeManageService.queryKnowledgeBasePageByType(queryParams));
  }

  @Operation(summary = "分页查询知识库", description = "用于其他模块引用")
  @PostMapping("querySimpleKnowledgeBasePage")
  public ResultVO<PageInfo<SimpleKnowledgeBaseDTO>> querySimpleKnowledgeBasePage(@RequestBody DocKnowledgeBaseQueryParams queryParams) {
    return ResultVO.success(knowledgeManageService.querySimpleKnowledgeBasePage(queryParams));
  }

  @Operation(summary = "分页查询知识库下拉列表")
  @PostMapping("queryKnowledgeComboboxPage")
  public ResultVO<PageInfo<KnowledgeComboboxDTO>> queryKnowledgeComboboxPage(@RequestBody KnowledgeComboboxQueryParams queryParams) {
    Assert.hasLength(queryParams.getKnowledgeType(), "knowledgeType 不能为空");
    return ResultVO.success(knowledgeManageService.queryKnowledgeComboboxPage(queryParams));
  }

  @Operation(summary = "查询策略设置中的大模型列表")
  @GetMapping("getModelList")
  public ResultVO<List<String>> getModelList() {
    return ResultVO.success(docChainTopicHelper.getModelList(DocBaseConsts.COPILOT_TENANT_ID));
  }

  @Operation(summary = "查询知识库最近的召回测试记录")
  @GetMapping("queryLatestQueryRecords")
  public ResultVO<List<String>> queryLatestQueryRecords(@RequestParam("knowledgeId") Long knowledgeId,
                                                        @RequestParam("querySource") String querySource, @RequestParam("tenantId") Long tenantId) {
    Assert.notNull(knowledgeId, "knowledgeId 不能为空");
    Assert.hasLength(querySource, "querySource 不能为空");
    Assert.notNull(tenantId, "租户 ID 不能为空");
    return ResultVO.success(knowledgeManageService.queryLatestQueryRecords(tenantId, knowledgeId, querySource));
  }

  @Operation(summary = "召回测试")
  @PostMapping("testRecall")
  public ResultVO<KnowledgeRecallResponse> testRecall(@RequestBody KnowledgeRecallParamDTO params) {
    Assert.notNull(params.getKnowledgeId(), "knowledgeId 不能为空");
    Assert.hasLength(params.getQuery(), "query 不能为空");
    if (params.getMinScore() != null) {
      Assert.isTrue(params.getMinScore() >= 0 && params.getMinScore() <= 1, "minScore 值范围为 0~1");
    }
    return ResultVO.success(knowledgeManageService.testRecall(params));
  }

  @Operation(summary = "docChain文档召回")
  @PostMapping("docChainBatchRecall")
  public ResultVO<KnowledgeRecallResponse> docChainBatchRecall(@RequestBody KnowledgeRecallParamDTO params) {
    Assert.notNull(params.getKnowledgeIds(), "knowledgeIds 不能为空");
    Assert.hasLength(params.getQuery(), "query 不能为空");
    if (params.getMinScore() != null) {
      Assert.isTrue(params.getMinScore() >= 0 && params.getMinScore() <= 1, "minScore 值范围为 0~1");
    }
    return ResultVO.success(knowledgeManageService.docChainBatchRecall(params));
  }

  @Operation(summary = "问答测试")
  @PostMapping(path = "testChat", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE
  })
  public Object testChat(@RequestBody KnowledgeChatTestDTO params) {
    Long knowledgeId = params.getKnowledgeId();
    String query = params.getQuery();
    Assert.notNull(knowledgeId, "knowledgeId 不能为空");
    Assert.hasLength(query, "query 不能为空");
    boolean withReferences = params.getWithReferences() != null ? params.getWithReferences() : false;
    // 记录查询记录
    knowledgeManageService.addKnowledgeQueryRecord(params.getTenantId(), knowledgeId, KnowledgeConsts.QUERY_SOURCE_CHAT_TEST, query);
    KnowledgeChatParamsDTO chatParams = KnowledgeChatParamsDTO.builder().tenantId(params.getTenantId()).spaceId(params.getSpaceId())
      .knowledgeIds(Collections.singletonList(knowledgeId)).question(query).modelId(params.getModelId()).withReferences(withReferences).build();
    // 非流式
    if (!Boolean.TRUE.equals(params.getStream())) {
      try {
        KnowledgeChatResponse response = knowledgeAnswerHelper.chatByKnowledge(chatParams);
        return ResultVO.success(response);
      }
      catch (RuntimeException e) {
        return ResultVO.fail(ExpUtil.getMsg(e));
      }
    }

    // 流式
    return SseUtil.createSseEmitter(params.getClientId(), emitter -> {
      try {
        Consumer<SseEvent> eventHandler = event -> SseUtil.sendJson(emitter, event.getMsgType(), event.getMsgContent());
        knowledgeAnswerHelper.chatStreamByKnowledgeBlocking(chatParams, eventHandler, SseUtil.requestListener);
      }
      catch (BssException e) {
        logger.error("Failed to test knowledge chat: params={}, error={}", params, e.getMessage());
        SseUtil.sendJson(emitter, ChatMessageType.ERROR, e.getMessage());
      }
      catch (Exception e) {
        logger.error("Failed to test knowledge chat: params={}", params, e);
        SseUtil.sendJson(emitter, ChatMessageType.ERROR, ExpUtil.getMsg(e));
      }
      finally {
        SseUtil.completeQuietly(emitter);
      }
    });
  }

  @Operation(summary = "查询对话日志")
  @GetMapping("queryChatLog")
  public ResultVO<DocChainChatLogDTO> queryChatLog(@RequestParam("tenantId") Long tenantId, @RequestParam("chatLogId") String chatLogId) {
    Assert.notNull(tenantId, "tenantId 不能为空");
    Assert.hasLength(chatLogId, "chatLogId 不能为空");
    return ResultVO.success(docChainAdapter.queryChatLog(tenantId, chatLogId));
  }

  @IgnoreTenant
  @Operation(summary = "查询 DocChain 知识信息策略设置")
  @GetMapping("getDocChainExtraConfig")
  @RequestCacheable(sql = "SELECT updated_time FROM bt_docchain_extra_cfg WHERE id = 1", cacheOnNotFound = true)
  public ResultVO<List<DocChainExtraDTO>> getDocChainExtraConfig() {
    return ResultVO.success(knowledgeManageService.getDocChainExtraConfig());
  }

  @Operation(summary = "获取租户的知识库配置")
  @GetMapping("getTenantKnowledgeConfig")
  public ResultVO<KnowledgeInfoDTO> getTenantKnowledgeConfig(@RequestParam(value = "tenantId", required = false) Long tenantId) {
    return knowledgeManageService.getTenantKnowledgeConfig(tenantId);
  }

  @Operation(summary = "获取知识库启用类型")
  @GetMapping("getKnowledgeEnabledType")
  public ResultVO<KnowledgeEnabledTypeDTO> getKnowledgeEnabledType() {
    return knowledgeManageService.getKnowledgeEnabledType();
  }

  @Operation(summary = "更新知识库可见范围")
  @PostMapping("updateKonwledgeVisibilityScope")
  public ResultVO<Void> updateKonwledgeVisibilityScope(@RequestBody UpdateKnowlegeVisibilityScopeDTO params) {
    Assert.notNull(params.getKnowledgeId(), "knowledgeId 不能为空");
    Assert.notNull(params.getTenantId(), "tenantId 不能为空");
    Assert.notNull(params.getVisibilityScope(), "visibilityScope 不能为空");
    ResultVO<Void> result = knowledgeManageService.updateKonwledgeVisibilityScope(params);
    if (!result.isSuccess()) {
      return new ResultVO<>(result);
    }
    // 修改时刷新缓存
    if (params.getKnowledgeId() != null) {
      refreshCacheService.refresh(DocCacheConsts.CACHE_NAME_KNOWLEDGE, params.getTenantId() + ":" + params.getKnowledgeId());
    }
    return result;
  }

  @Operation(summary = "查询动态配置策略")
  @GetMapping("queryDynamicConfigParams")
  public ResultVO<Map<String, Object>> queryDynamicConfigParams(@RequestParam("tenantId") Long tenantId) {
    return ResultVO.success(knowledgeManageService.queryDynamicConfigParams(tenantId));
  }
}
