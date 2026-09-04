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
 * 新增评论请求
 *
 * @author Aiqing
 * @since 2025-10-16
 */
@Getter
@Setter
@ToString
public class AddCommentRequest extends TenantBaseRO {

  @NotNull(message = "文档ID不能为空")
  @Schema(description = "文档ID", requiredMode = RequiredMode.REQUIRED)
  private String documentId;

  @Size(max = 5000, message = "内容长度不能超过5000字符")
  @Schema(description = "评论内容", requiredMode = RequiredMode.REQUIRED, maxLength = 5000)
  private String content;

  @Schema(description = "父评论ID，用于回复评论")
  private Long parentId;

  @Schema(description = "记录类型：CORRECTION-修订，COMMENT-评论", requiredMode = RequiredMode.REQUIRED)
  @NotBlank(message = "记录类型不能为空")
  private String recordType;

  @Schema(description = "修订类型：MODIFY-修正，ADD-新增，DELETE-删除, SET-设置（仅修订记录使用）")
  private String correctionType;

  @Schema(description = "修订原因（仅修订记录使用）")
  @Size(max = 500, message = "修订原因长度不能超过500字符")
  private String correctionReason;

  @Schema(description = "旧内容（仅修订记录使用）")
  private String oldContent;

  @Schema(description = "修订UUID")
  private String uuid;
}
