package com.iwhalecloud.bote.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.iwhalecloud.bote.doc.module.knowledge.service.IDocumentManageService;
import com.iwhalecloud.bss.litchi.job.AbstractSimpleJob;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

/**
 * 定时任务：同步 DocChain 文档状态
 *
 * @author chen.linfa
 * @since 2024-10-17
 */
public class UpdateDocChainDocStatusJob extends AbstractSimpleJob {
  private IDocumentManageService documentManageService;

  public UpdateDocChainDocStatusJob() {
    super("同步 DocChain 文档状态定时任务");
  }

  @Override
  protected void doExecute(ShardingContext shardingContext) {
    if (documentManageService == null) {
      documentManageService = SpringUtil.getBean(IDocumentManageService.class);
    }
    documentManageService.updateDocChainDocStatus();
  }
}
