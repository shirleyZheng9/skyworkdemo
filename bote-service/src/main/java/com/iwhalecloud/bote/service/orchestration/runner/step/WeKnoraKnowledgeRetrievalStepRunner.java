package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.doc.module.knowledge.adapter.WeKnoraKnowledgeClient;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallParamDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallResponse;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.WeKnoraKnowledgeRetrievalStep;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * WeKnora 知识检索步骤执行器
 * <p>
 * 直接调用 {@link WeKnoraKnowledgeClient#recall}；WeKnora 知识库 ID 由步骤配置
 * {@link WeKnoraKnowledgeRetrievalStep#getWeKnoraKnowledgeBaseIds()} 传入，不查询博特知识库缓存。
 * </p>
 *
 * @author huangyunming
 * @since 2026-04-01
 */
public class WeKnoraKnowledgeRetrievalStepRunner extends AbstractKnowledgeStepRunner<WeKnoraKnowledgeRetrievalStep> {

  /** 默认分数阈值 */
  private static final float DEFAULT_SCORE_THRESHOLD = 0.1F;

  @Override
  protected void doRun(SceneOrchestrationContext context, WeKnoraKnowledgeRetrievalStep step) {
    String question = resolveTemplate(step.getQuestion());
    Assert.notNull(question, "知识库查询语句不能为空");
    int topK = ObjectUtils.getIfNull(step.getTopK(), 10);
    Assert.isTrue(topK > 0, "召回数量必须是正数");
    float minScore = ObjectUtils.firstNonNull(step.getMinScore(), DEFAULT_SCORE_THRESHOLD);

    List<String> weKnoraKbIds = resolveWeKnoraKnowledgeBaseIds(step);
    Assert.isTrue(CollectionUtils.isNotEmpty(weKnoraKbIds), "WeKnora 知识检索：WeKnora 知识库 ID 不能为空");

    context.setStepInputLog(
      ImmutableMap.of("weKnoraKnowledgeBaseIds", weKnoraKbIds, "question", StringUtils.defaultString(question)));

    context.setStepOutput(step, searchKnowledge(context.getTenantId(), weKnoraKbIds, question, topK, minScore));
  }

  /**
   * 调用 WeKnora 知识检索接口
   */
  private Map<String, Object> searchKnowledge(Long tenantId, List<String> weKnoraKbIds, String question, int topK,
    float minScore) {
    WeKnoraKnowledgeClient weKnoraBusinessHelper = SpringUtil.getBean(WeKnoraKnowledgeClient.class);
    KnowledgeRecallParamDTO params = KnowledgeRecallParamDTO.builder().tenantId(tenantId)
      .knowledgeIds(Collections.emptyList()).knowledgeList(Collections.emptyList())
      .weKnoraKnowledgeBaseIds(weKnoraKbIds).query(question).maxNum(topK).minScore(minScore).build();
    KnowledgeRecallResponse response = weKnoraBusinessHelper.recall(params);
    Map<String, Object> result = JsonUtil.convert(response, new TypeReference<Map<String, Object>>() {
    });
    result.put("json", JsonUtil.toJsonString(result));
    return result;
  }

  private List<String> resolveWeKnoraKnowledgeBaseIds(WeKnoraKnowledgeRetrievalStep step) {
    String raw = resolveTemplate(StringUtils.trimToEmpty(step.getWeKnoraKnowledgeBaseIds()));
    if (StringUtils.isBlank(raw)) {
      return Collections.emptyList();
    }
    return Arrays.stream(raw.split(",")).map(String::trim).filter(StringUtils::isNotEmpty).distinct()
      .collect(Collectors.toList());
  }
}
