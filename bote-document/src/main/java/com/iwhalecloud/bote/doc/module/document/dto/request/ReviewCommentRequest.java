package com.iwhalecloud.bote.doc.module.document.dto.request;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 审核评论请求
 *
 * @author Aiqing
 * @since 2025-10-16
 */
@Getter
@Setter
@ToString
public class ReviewCommentRequest extends TenantBaseRO {

  @NotNull(message = "评论ID不能为空")
  @Schema(description = "评论ID", requiredMode = RequiredMode.REQUIRED)
  private Long commentId;

  @Schema(description = "审核状态：APPROVED-已通过，REJECTED-已拒绝", hidden = true)
  private String reviewStatus;

  @Schema(description = "审核意见")
  private String reviewOpinion;
}
