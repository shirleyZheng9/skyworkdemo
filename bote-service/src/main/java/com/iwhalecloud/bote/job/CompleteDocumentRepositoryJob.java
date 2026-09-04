package com.iwhalecloud.bote.job;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.module.knowledge.service.IDocumentManageService;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bss.litchi.job.AbstractSimpleJob;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

/**
 * 定时任务：全员文档库
 *
 */
public class CompleteDocumentRepositoryJob extends AbstractSimpleJob {
  public CompleteDocumentRepositoryJob() {
    super("全员文档库");
  }

  @Override
  protected void doExecute(ShardingContext shardingContext) {
    try {
      LoginInfo loginInfo = new LoginInfo();
      List<Long> userIds = Arrays.stream(StringUtils.split(SystemParameter.SUPER_ADMIN.getValueFromDb(), ","))
        .map(p -> Arrays.asList(p.split("\\|")).get(0)).map(Long::valueOf).toList();
      loginInfo.setUserId(userIds.get(0)); // 后面的信息需要这个数据 这里需要动态取一个超级管理员的账号
      SessionUtil.setLoginInfo(loginInfo);
      SpringUtil.getBean(IDocumentManageService.class).createDocumentNodesFromFileInfo();
    }
    finally {
      SessionUtil.clearThreadLocal();
    }
  }
}
