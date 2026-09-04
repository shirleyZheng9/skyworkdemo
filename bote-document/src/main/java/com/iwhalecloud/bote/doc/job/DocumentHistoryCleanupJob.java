package com.iwhalecloud.bote.doc.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentHistoryCleanupService;
import com.iwhalecloud.bss.litchi.job.AbstractSimpleJob;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

/**
 * 定时任务：清理文档历史记录
 *
 * @author lizuyin
 * @since 2025-10-16
 */
public class DocumentHistoryCleanupJob extends AbstractSimpleJob {

  public DocumentHistoryCleanupJob() {
    super("清理文档历史记录定时任务");
  }

  @Override
  protected void doExecute(ShardingContext shardingContext) {
    TenantContextHolder.setIgnore(true);
    SpringUtil.getBean(IDocumentHistoryCleanupService.class).cleanupDocumentHistory();
    TenantContextHolder.clear();
  }
}
