package com.iwhalecloud.bote.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.iwhalecloud.bote.doc.module.knowledge.service.IKnowledgeBaseManageService;
import com.iwhalecloud.bss.litchi.job.AbstractSimpleJob;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

/**
 * 定时任务：清理临时知识
 *
 * @author chen.linfa
 * @since 2025-03-31
 */
public class ClearTemporaryKnowledgeJob extends AbstractSimpleJob {
  private IKnowledgeBaseManageService knowledgeBaseManageService;

  public ClearTemporaryKnowledgeJob() {
    super("清理临时知识定时任务");
  }

  @Override
  protected void doExecute(ShardingContext shardingContext) {
    if (knowledgeBaseManageService == null) {
      knowledgeBaseManageService = SpringUtil.getBean(IKnowledgeBaseManageService.class);
    }
    knowledgeBaseManageService.clearTemporaryKnowledge();
  }
}
