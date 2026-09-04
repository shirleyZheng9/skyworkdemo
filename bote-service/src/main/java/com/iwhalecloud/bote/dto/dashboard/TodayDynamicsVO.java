package com.iwhalecloud.bote.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 首页看板今日动态。
 *
 * @author zhengxueli
 * @since 2026-08-31
 */
@Getter
@ToString
@AllArgsConstructor
@Schema(description = "首页看板今日动态")
public class TodayDynamicsVO {

  @Schema(description = "今日新增智能体数量", example = "12")
  private final Long newAgentCount;

  @Schema(description = "今日新增知识库数量", example = "3")
  private final Long newKnowledgeCount;

  @Schema(description = "今日大模型调用次数", example = "86899")
  private final Long modelInvokeCount;

  @Schema(description = "今日知识构建完成文档数", example = "12")
  private final Long builtDocumentCount;

  @Schema(description = "统计时间", example = "2026-08-31T10:30:00")
  private final LocalDateTime statTime;
}
