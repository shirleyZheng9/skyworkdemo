package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

/**
 * 知识检索步骤
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
public class KnowledgeRetrievalStep extends AbstractStep {
  /** 问题（模板字符串，支持引用变量） */
  private String question;
  /** 知识库 ID（支持引用变量，只能是单个知识库 ID) */
  private String knowledgeId;
  /** 文档 ID（支持引用变量，支持逗号分隔的多个文档 ID) */
  private String documentId;
  /** 召回数量（默认为 10） */
  private Integer topK;
  /** 召回精度（默认为 0.1）*/
  private Float minScore;
  /** 知识库列表(外系统接入-百应-知识中台) 需支持动态参数,所以设置为字符串类型 */
  private String knowledgeExt;
  /** 文档列表(外系统接入-知识中台) 需支持动态参数,所以设置为字符串类型*/
  private String resourceExt;

  public KnowledgeRetrievalStep() {
    super(StepType.KNOWLEDGE_RETRIEVAL);
  }
}
