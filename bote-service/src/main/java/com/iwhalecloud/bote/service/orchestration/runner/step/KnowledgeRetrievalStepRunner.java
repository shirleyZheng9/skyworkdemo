package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.doc.module.knowledge.cache.KnowledgeCache;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.doc.common.utils.KnowledgeClientUtil;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.BtDcKbPermissionHelper;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeInfoDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallParamDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallResponse;
import com.iwhalecloud.bote.dto.knowledge.ResourceExtItem;
import com.iwhalecloud.bote.dto.knowledge.SimpleKnowledgeDTO;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.KnowledgeRetrievalStep;
import com.iwhalecloud.bote.doc.module.knowledge.adapter.client.KnowledgeClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 知识检索步骤执行器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class KnowledgeRetrievalStepRunner extends AbstractKnowledgeStepRunner<KnowledgeRetrievalStep> {
  /** 默认分数阈值 */
  private static final float DEFAULT_SCORE_THRESHOLD = 0.1F;
  private static final KnowledgeCache knowledgeCache = SpringUtil.getBean(KnowledgeCache.class);
  private final TenantSettingInfoCache tenantSettingInfoCache = SpringUtil.getBean(TenantSettingInfoCache.class);
  private final BtDcKbPermissionHelper btDcKbPermissionHelper = SpringUtil.getBean(BtDcKbPermissionHelper.class);

  @Override
  protected void doRun(SceneOrchestrationContext context, KnowledgeRetrievalStep step) {
    KnowledgeInfoDTO knowledgeInfo = tenantSettingInfoCache.getKnowledgeInfo(context.getTenantId());
    String question = resolveTemplate(step.getQuestion());
    Assert.notNull(question, "知识库查询语句不能为空");
    int topK = ObjectUtils.getIfNull(step.getTopK(), 10);
    Assert.isTrue(topK > 0, "召回数量必须是正数");
    float minScore =  ObjectUtils.firstNonNull(step.getMinScore(), DEFAULT_SCORE_THRESHOLD);
    if (StringUtils.isNotEmpty(knowledgeInfo.getKnowledgeType())) {
      List<Long> knowledgeIds = resolveKnowledgeIdsFormExt(StringUtils.defaultString(step.getKnowledgeExt()));
      List<ResourceExtItem> resourceItems = resolveResourceIdsFormExt(StringUtils.defaultString(step.getResourceExt()));
      Assert.isTrue(!knowledgeIds.isEmpty() || !resourceItems.isEmpty(), "知识库和文档不能同时为空");
      context.setStepInputLog(ImmutableMap.of("knowledgeExtItems", StringUtils.defaultString(step.getKnowledgeExt()), "resourceExtItems", StringUtils.defaultString(step.getResourceExt()), "question", StringUtils.defaultString(question)));
      context.setStepOutput(step, searchKnowledge(context.getTenantId(), knowledgeIds, resourceItems, question, topK, minScore, knowledgeInfo.getKnowledgeType()));
    }
    else {
      List<Long> knowledgeIds = resolveKnowledgeIds(step.getKnowledgeId());
      List<Long> documentIds = resolveDocumentIds(step.getDocumentId());
      context.setStepInputLog(ImmutableMap.of("knowledgeIds", knowledgeIds, "question", StringUtils.defaultString(question)));
      context.setStepOutput(step, searchKnowledge(context.getTenantId(), knowledgeIds, documentIds, question, topK, minScore));
    }
  }

  /**
   * 调用知识检索接口
   * <p>注意: 需要使用知识召回接口，而非问答接口。如果想要问答的效果，可以使用大模型节点对召回结果做增强。</p>
   */
  private Map<String, Object> searchKnowledge(Long tenantId, List<Long> knowledgeIds, @Nullable List<Long> documentIds, @Nullable String question, int topK, float minScore) {
    List<SimpleKnowledgeDTO> knowledges = new ArrayList<>(knowledgeIds.size());
    for (Long knowledgeId : knowledgeIds) {
      SimpleKnowledgeDTO knowledge = knowledgeCache.get(tenantId, knowledgeId);
      Assert.notNull(knowledge, () -> "知识库不存在: " + knowledgeId);
      knowledges.add(knowledge);
    }
    KnowledgeClient knowledgeClient = KnowledgeClientUtil.getClient(knowledges.getFirst().getKnowledgeType());
    KnowledgeRecallParamDTO params = KnowledgeRecallParamDTO.builder()
      .tenantId(tenantId)
      .knowledgeIds(knowledgeIds)
      .documentIds(documentIds)
      .knowledge(knowledges.getFirst())
      .knowledgeList(knowledges)
      .query(question)
      .maxNum(topK)
      .minScore(minScore)
      .build();
    Pair<List<String>, List<String>> topicAndDocIds = btDcKbPermissionHelper.getTopicAndDocIds(params.getKnowledgeList(), params.getDocumentIds(), params.getTenantId()); // 文档库权限判断
    params.setTopicIds(topicAndDocIds.getLeft());
    params.setDocIds(topicAndDocIds.getRight());
    KnowledgeRecallResponse response = knowledgeClient.recall(params);
    Map<String, Object> result = JsonUtil.convert(response, new TypeReference<>() {
    });
    // 返回召回结果的 JSON 形式，以方便用户在大模型节点中使用
    String json = JsonUtil.toJsonString(result);
    result.put("json", json);
    return result;
  }

  /**
   * 调用知识检索接口,对接外系统 百应 知识中台,可支持多个知识库检索
   * <p>注意: 需要使用知识召回接口，而非问答接口。如果想要问答的效果，可以使用大模型节点对召回结果做增强。</p>
   */
  private Map<String, Object> searchKnowledge(Long tenantId, List<Long> knowledgeIds, List<ResourceExtItem> resourceItems, @Nullable String question, int topK, float minScore, String knowledgeType) {
    KnowledgeClient knowledgeClient = KnowledgeClientUtil.getClient(knowledgeType);
    KnowledgeRecallParamDTO params = KnowledgeRecallParamDTO.builder()
      .tenantId(tenantId)
      .knowledgeIds(knowledgeIds)
      .resourceItems(resourceItems)
      .query(question)
      .maxNum(topK)
      .minScore(minScore)
      .build();
    KnowledgeRecallResponse response = knowledgeClient.recall(params);
    Map<String, Object> result = JsonUtil.convert(response, new TypeReference<Map<String, Object>>() {
    });
    // 返回召回结果的 JSON 形式，以方便用户在大模型节点中使用
    String json = JsonUtil.toJsonString(result);
    result.put("json", json);
    return result;
  }

}
