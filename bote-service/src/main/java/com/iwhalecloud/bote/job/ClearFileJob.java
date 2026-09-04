package com.iwhalecloud.bote.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.iwhalecloud.bote.service.base.impl.FileInfoServiceImpl;
import com.iwhalecloud.bss.litchi.job.AbstractSimpleJob;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

/**
 * 清理文件定时任务
 *
 * <p>清理失效的文件</p>
 *
 * @author bianjp
 * @since 2025-08-25
 */
public class ClearFileJob extends AbstractSimpleJob {
  private FileInfoServiceImpl fileInfoService;

  public ClearFileJob() {
    super("清理文件");
  }

  @Override
  protected void doExecute(ShardingContext shardingContext) {
    if (fileInfoService == null) {
      fileInfoService = SpringUtil.getBean(FileInfoServiceImpl.class);
    }
    fileInfoService.clearFile();
  }
}
