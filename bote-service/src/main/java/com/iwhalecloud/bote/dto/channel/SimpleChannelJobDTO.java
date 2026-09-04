package com.iwhalecloud.bote.dto.channel;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单渠道定时任务配置信息
 *
 * @author chen.linfa
 * @since 2026-03-11
 */
@Getter
@Setter
@ToString
public class SimpleChannelJobDTO {
  /** 用户 ID */
  private Long userId;
  /** 空间 ID */
  private Long spaceId;
  /** 租户 ID */
  private Long tenantId;
  /** 应用 ID */
  private Long botId;
  /** 渠道 ID */
  private Long channelId;
  /** 渠道类型 */
  private String channelType;
  /** 任务类型 */
  private String taskType;
  /** 文本输入 */
  private String requestInput;
  /** 渠道对应的回调地址 */
  private String webhook;
  /** 标题 */
  private String title;
}
