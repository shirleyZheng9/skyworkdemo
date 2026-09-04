package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

/**
 * knowledgeGraph 知识检索步骤
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
@Getter
@Setter
public class KnowledgeGraphKnowledgeRetrievalStep extends AbstractStep {
  /** 问题 */
  private String question;
  /** 知识库名称 */
  private String knowledgeGraphName;
  /** topK */
  private Integer topK;
  /** minScore */
  private Float minScore;

  public KnowledgeGraphKnowledgeRetrievalStep() {
    super(StepType.KNOWLEDGE_GRAPH_RETRIEVAL);
  }
}
