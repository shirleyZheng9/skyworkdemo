package com.iwhalecloud.bote.doc.module.knowledge.dto;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 更新问答记录
 *
 * @author linmengfan
 * @since 2025-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
public class UpdateBtDcQaRecordDTO extends TenantBaseRO {
  @Schema(description = "客户端id，当前作为前端对话的一问一答的交付id")
  private String clientId;

  @Schema(description = "用户反馈：LIKE-点赞，DISLIKE-点踩")
  private String userFeedback;

  @Schema(description = "反馈原因")
  private String feedbackReason;
}
