package com.iwhalecloud.bote.doc.module.document.dto.request;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 编辑评论请求
 *
 * @author Aiqing
 * @since 2025-10-16
 */
@Getter
@Setter
@ToString
public class UpdateCommentRequest extends TenantBaseRO {

  @NotNull(message = "评论ID不能为空")
  @Schema(description = "评论ID", requiredMode = RequiredMode.REQUIRED)
  private Long commentId;

  @NotBlank(message = "内容不能为空")
  @Size(max = 2000, message = "内容长度不能超过2000字符")
  @Schema(description = "评论内容", requiredMode = RequiredMode.REQUIRED, maxLength = 2000)
  private String content;
}
