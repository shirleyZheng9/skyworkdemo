package com.iwhalecloud.bote.doc.module.knowledge.dto;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 看板信息
 *
 * @author linmengfan
 * @since 2025-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
public class BtDcQaRecordNoticeBoardDTO {
  @Schema(description = "问答总数")
  private Long questionsAndAnswersTotal;
  @Schema(description = "命中率")
  private BigDecimal hitRate;
  @Schema(description = "提问人数")
  private Long questioners;
  @Schema(description = "每天问答次数")
  private List<BtDcQaRecordItemDto> qaList;
  @Schema(description = "评价分布数据")
  private List<BtDcQaRecordItemDto> feedbackData;
  @Schema(description = "不满意分布原因数据")
  private List<BtDcQaRecordItemDto> dissatisfactionData;
}
