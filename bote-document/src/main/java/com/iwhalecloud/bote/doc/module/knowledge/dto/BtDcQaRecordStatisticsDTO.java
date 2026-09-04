package com.iwhalecloud.bote.doc.module.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 问答记录统计DTO
 *
 * @author linmengfan
 * @since 2025-09-13
 */
@Getter
@Setter
@ToString
@Schema(description = "问答记录统计DTO")
public class BtDcQaRecordStatisticsDTO {
  @Schema(description = "问答总数")
  private Long questionsAndAnswersTotal;
  
  @Schema(description = "命中率")
  private BigDecimal hitRate;
  
  @Schema(description = "点赞数")
  private Long numberOfLikesTotal;
  
  @Schema(description = "踩数")
  private Long stepTotal;
  
  @Schema(description = "问答人数")
  private Long questionAnswerUserTotal;
  
  @Schema(description = "反馈原因不为空的记录数")
  private Long feedbackReasonCount;
}
