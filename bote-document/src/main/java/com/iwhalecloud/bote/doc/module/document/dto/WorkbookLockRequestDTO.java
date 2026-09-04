package com.iwhalecloud.bote.doc.module.document.dto;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作簿锁定请求DTO
 *
 * @author Aiqing
 * @since 2025-09-26
 */
@Getter
@Setter
@ToString
@Schema(description = "工作簿锁定请求")
public class WorkbookLockRequestDTO extends TenantBaseRO {

  @Schema(description = "文档ID", example = "doc_123456789", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotEmpty(message = "文档ID不能为空")
  private String documentId;

  @Schema(description = "锁定操作类型：LOCK-锁定，UNLOCK-解锁", example = "LOCK", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotEmpty(message = "锁定操作类型不能为空")
  private String lockAction;
}
