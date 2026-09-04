package com.iwhalecloud.bote.doc.module.document.dto;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档上传请求参数
 *
 * @author Aiqing
 * @since 2025/9/10
 */
@Getter
@Setter
@ToString
@Schema(description = "文档上传请求参数")
public class DocumentAttachmentUploadRO extends TenantBaseRO {

  @Schema(description = "文档ID", example = "doc_123456789", requiredMode = RequiredMode.REQUIRED)
  @NotEmpty(message = "文档ID不能为空")
  private String documentId;
}
