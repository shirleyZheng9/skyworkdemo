package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.doc.common.utils.KnowledgeClientUtil;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallParamDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallResponse;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.KnowledgeGraphKnowledgeRetrievalStep;
import com.iwhalecloud.bote.doc.module.knowledge.adapter.client.KnowledgeClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * knowledgeGraph 知识检索步骤执行器
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
public class KnowledgeGraphKnowledgeRetrievalStepRunner extends AbstractKnowledgeStepRunner<KnowledgeGraphKnowledgeRetrievalStep> {

  @Override
  protected void doRun(SceneOrchestrationContext context, KnowledgeGraphKnowledgeRetrievalStep step) {
    String question = resolveTemplate(step.getQuestion());
    Assert.hasText(question, "知识检索问题不能为空");
    Assert.isTrue(StringUtils.isNotEmpty(step.getKnowledgeGraphName()), "knowledgeGraph 知识库不能为空");
    int topK = ObjectUtils.getIfNull(step.getTopK(), 10);
    float minScore = ObjectUtils.firstNonNull(step.getMinScore(), 0.1F);
    context.setStepInputLog(ImmutableMap.of("knowledgeGraphName", step.getKnowledgeGraphName(), "question", question));
    KnowledgeClient client = KnowledgeClientUtil.getClient("knowledgeGraph");
    KnowledgeRecallParamDTO params = KnowledgeRecallParamDTO.builder()
      .tenantId(context.getTenantId())
      .query(question)
      .maxNum(topK)
      .minScore(minScore)
      .knowledgeGraphName(step.getKnowledgeGraphName())
      .build();
    KnowledgeRecallResponse response = client.recall(params);
    Map<String, Object> result = JsonUtil.convert(response, new TypeReference<>() {
    });
    result.put("json", JsonUtil.toJsonString(result));
    context.setStepOutput(step, result);
  }

}
