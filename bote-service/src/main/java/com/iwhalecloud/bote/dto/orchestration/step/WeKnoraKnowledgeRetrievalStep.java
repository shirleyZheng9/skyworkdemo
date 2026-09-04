package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

/**
 * WeKnora 知识检索步骤
 *
 * @author huangyunming
 * @since 2026-04-01
 */
@Getter
@Setter
public class WeKnoraKnowledgeRetrievalStep extends AbstractStep {

  /** 问题（模板字符串，支持引用变量） */
  private String question;
  /** WeKnora 知识库 ID（支持模板变量，多个用英文逗号分隔；编排侧不再解析博特知识库缓存） */
  private String weKnoraKnowledgeBaseIds;
  /** 知识库 ID（支持引用变量，支持逗号分隔的多个知识库 ID) */
  private String knowledgeId;
  /** 文档 ID（支持引用变量，支持逗号分隔的多个文档 ID) */
  private String documentId;
  /** 召回数量（默认为 10） */
  private Integer topK;
  /** 召回精度（默认为 0.1） */
  private Float minScore;
  /** 知识库列表（JSON 格式，支持动态参数） */
  private String knowledgeExt;
  /** 文档列表（JSON 格式，支持动态参数） */
  private String resourceExt;

  public WeKnoraKnowledgeRetrievalStep() {
    super(StepType.WEKNORA_RETRIEVAL);
  }
}
