package com.iwhalecloud.bote.doc.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.iwhalecloud.bote.doc.module.knowledge.semantic.IBtDcQaKnowledgeVectorService;
import com.iwhalecloud.bss.litchi.job.AbstractSimpleJob;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

/**
 * 知识库问答记录向量化与 standard_question 回填
 *
 * @author qian.sisheng
 * @since 2026/04/02
 */
public class KnowledgeQuestionVectorJob extends AbstractSimpleJob {

  private IBtDcQaKnowledgeVectorService knowledgeVector;

  public KnowledgeQuestionVectorJob() {
    super("问答记录标准提问回填");
  }

  @Override
  protected void doExecute(ShardingContext shardingContext) {
    if (knowledgeVector == null) {
      knowledgeVector = SpringUtil.getBean(IBtDcQaKnowledgeVectorService.class);
    }
    knowledgeVector.backfillPendingStandardQuestions(200);
  }
}
