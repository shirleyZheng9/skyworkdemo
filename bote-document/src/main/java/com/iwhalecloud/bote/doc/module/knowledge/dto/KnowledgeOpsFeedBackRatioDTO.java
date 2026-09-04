package com.iwhalecloud.bote.doc.module.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识反馈比例DTO
 *
 * @author qian.sisheng
 * @since 2026/02/27
 */
@Getter
@Setter
@ToString
public class KnowledgeOpsFeedBackRatioDTO {
  @Schema(description = "点赞数")
  private Long likeCount;

  @Schema(description = "点踩数")
  private Long dislikeCount;

  @Schema(description = "点赞占比（百分比）")
  private BigDecimal likeRate;

  @Schema(description = "点踩占比（百分比）")
  private BigDecimal dislikeRate;
}
