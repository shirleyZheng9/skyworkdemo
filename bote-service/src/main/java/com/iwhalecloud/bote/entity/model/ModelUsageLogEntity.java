package com.iwhalecloud.bote.entity.model;

import com.iwhalecloud.bss.litchi.disruptor.DisruptorObject;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 大模型使用量日志
 *
 * @author chen.linfa
 * @since 2026-04-07
 */
@Getter
@Setter
@ToString
public class ModelUsageLogEntity implements DisruptorObject {
  /** 日志 ID */
  private Long logId;
  /** 租户 ID */
  private Long tenantId;
  /** 应用 ID */
  private Long botId;
  /** 机器人 ID */
  private Long sceneId;
  /** 流程 ID */
  private Long flowId;
  /** 用户 ID */
  private Long userId;
  /** 大模型 ID */
  private Long modelId;
  /** 模型名称 */
  private String modelName;
  /** 输入 token 数 */
  private Integer inputToken;
  /** 输出 token 数 */
  private Integer outputToken;
  /** 开始时间 */
  private Date startTime;
  /** 耗时(ms) */
  private Integer timeSpent;
  /** 请求原始报文 */
  private String requestJson;
  /** 响应报文 */
  private String responseJson;
  /** 链路追踪标识 */
  private String traceId;
  /** 会话 ID */
  private Long sessionId;
  /** 对话来源 */
  private String sourceFrom;
}
