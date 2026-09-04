package com.iwhalecloud.bote.doc.module.document.dto.request;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 查询评论记录请求
 *
 * @author Aiqing
 * @since 2025-10-16
 */
@Getter
@Setter
@ToString
public class QueryCommentsRequest extends TenantBaseRO {

  @NotBlank(message = "文档ID不能为空")
  @Schema(description = "文档ID", requiredMode = RequiredMode.REQUIRED)
  private String documentId;

  @Schema(description = "记录类型：CORRECTION-修订，COMMENT-评论", hidden = true)
  private String recordType;

  @Schema(description = "审核状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝", hidden = true)
  private String reviewStatus;
}
