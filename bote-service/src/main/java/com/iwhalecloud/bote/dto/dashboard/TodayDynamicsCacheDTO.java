package com.iwhalecloud.bote.dto.dashboard;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 首页看板今日动态缓存值。
 *
 * @author zhengxueli
 * @since 2026-09-01
 */
@Getter
@Setter
public class TodayDynamicsCacheDTO {

  /** 今日新增智能体数量。 */
  private Long newAgentCount;

  /** 今日新增知识库数量。 */
  private Long newKnowledgeCount;

  /** 今日大模型调用次数。 */
  private Long modelInvokeCount;

  /** 今日知识构建完成文档数。 */
  private Long builtDocumentCount;

  /** 本次统计生成时间。 */
  private LocalDateTime statTime;

  /** 逻辑过期时间戳，过期后接口先返回旧值再异步刷新。 */
  private Long refreshAfterEpochMillis;
}
