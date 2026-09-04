package com.iwhalecloud.bote.doc.module.knowledge.dto;

import java.math.BigDecimal;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 问答记录表 Entity
 *
 * @author linmengfan
 * @since 2025-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SimpleBtDcQaRecordDTO {
  @Schema(description = "主键")
  private Long qaId;

  @Schema(description = "整体置信度分数")
  private BigDecimal confidenceScore;

  @Schema(description = "用户反馈：LIKE-点赞，DISLIKE-点踩")
  private String userFeedback;

  @Schema(description = "创建时间")
  private Date createdTime;
}
