package com.iwhalecloud.bote.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.iwhalecloud.bote.service.portal.IUserManageService;
import com.iwhalecloud.bss.litchi.job.AbstractSimpleJob;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

/**
 * 检查账号是否失效定时任务：
 * 1. 每天凌晨一点执行扫描任务，扫描全部未过期的用户数据。
 *
 * @author tingyun.wang
 * @since 2025-07-04
 */
public class CheckUserExpJob extends AbstractSimpleJob {
  private IUserManageService userManageService;

  public CheckUserExpJob() {
    super("检查账号是否失效");
  }

  @Override
  protected void doExecute(ShardingContext shardingContext) {
    if (userManageService == null) {
      userManageService = SpringUtil.getBean(IUserManageService.class);
    }
    userManageService.checkUserExpTask();
  }
}
