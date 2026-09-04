package com.iwhalecloud.bote.doc.module.document.dto.request;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 删除评论请求
 *
 * @author Aiqing
 * @since 2025-10-16
 */
@Getter
@Setter
@ToString
public class CommentDeleteRequest extends TenantBaseRO {

  @NotBlank(message = "文档ID不能为空")
  @Schema(description = "文档ID", requiredMode = RequiredMode.REQUIRED)
  private String documentId;

  @NotNull(message = "评论ID不能为空")
  @Schema(description = "评论ID", requiredMode = RequiredMode.REQUIRED)
  private Long commentId;
}
