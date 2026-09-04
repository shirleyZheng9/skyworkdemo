package com.iwhalecloud.bote.doc.module.document.dto;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作簿保存请求DTO
 *
 * @author Aiqing
 * @since 2025-09-26
 */
@Getter
@Setter
@ToString
@Schema(description = "工作簿保存请求")
public class WorkbookSaveRequestDTO extends TenantBaseRO {

  @Schema(description = "文档ID", example = "doc_123456789", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotEmpty(message = "文档ID不能为空")
  private String documentId;

  @Schema(description = "工作簿内容（JSON格式）", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotEmpty(message = "工作簿内容不能为空")
  private String content;

  @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "版本号不能为空")
  private Long revision;
}
