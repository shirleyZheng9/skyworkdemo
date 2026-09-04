package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 反馈信息
 *
 * @author qian.sisheng
 * @since 2025-12-11
 */
@Setter
@Getter
@ToString
public class FeedbackMessageDTO {
  @Schema(description = "消息ID")
  private Long messageId;
  @Schema(description = "反馈信息")
  private String feedbackReason;
  @Schema(description = "点赞：LIKE 点踩：DISLIKE")
  private String userFeedback;
  @Schema(description = "反馈类型")
  private String feedbackType;
}
