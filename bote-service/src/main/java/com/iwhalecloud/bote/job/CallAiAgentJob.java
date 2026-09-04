package com.iwhalecloud.bote.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.iwhalecloud.bote.dto.channel.SimpleChannelJobDTO;
import com.iwhalecloud.bote.service.chat.IChatChannelService;
import com.iwhalecloud.bss.litchi.job.AbstractSimpleJob;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * 调用通用智能体定时任务
 *
 * @author wangtingyun
 * @since 2026-03-09
 */
public class CallAiAgentJob extends AbstractSimpleJob {

  public CallAiAgentJob() {
    super("调用通用智能体定时任务");
  }

  @Override
  protected void doExecute(ShardingContext shardingContext) {
    SimpleChannelJobDTO channel = JsonUtil.parseJson(shardingContext.getJobParameter(), SimpleChannelJobDTO.class);
    if (channel == null || channel.getUserId() == null || channel.getTenantId() == null || StringUtils.isEmpty(channel.getRequestInput())) {
      if (logger.isWarnEnabled()) {
        logger.warn("Failed to execute job. illegal params={}", shardingContext.getJobParameter());
      }
    }
    else {
      if (channel.getSpaceId() == null) {
        // 兼容存量数据
        channel.setSpaceId(channel.getTenantId());
      }
      SpringUtil.getBean(IChatChannelService.class).execute(channel);
    }
  }

}
