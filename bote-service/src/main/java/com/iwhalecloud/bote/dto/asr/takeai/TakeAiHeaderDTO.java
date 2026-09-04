package com.iwhalecloud.bote.dto.asr.takeai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * TakeAi 协议消息头 DTO
 *
 * @author qian.sisheng
 * @since 2026-01-09
 */
@Getter
@Setter
@ToString
public class TakeAiHeaderDTO {
  /** 应用 Key */
  private String appkey;
  /** 认证 Token */
  private String token;
  /** 命名空间 */
  private String namespace;
  /** 消息名称 */
  private String name;
  /** 消息 ID */
  @JsonProperty("message_id")
  private String messageId;
  /** 任务 ID */
  @JsonProperty("task_id")
  private String taskId;
  /** 状态码 */
  private Integer status;
  /** 状态描述 */
  @JsonProperty("status_text")
  private String statusText;
}
