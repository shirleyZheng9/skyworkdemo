package com.iwhalecloud.bote.doc.module.document.dto.request;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 查询修正记录请求
 *
 * @author Aiqing
 * @since 2025-10-16
 */
@Getter
@Setter
@ToString
public class QueryCorrectionsRequest extends TenantBaseRO {

  @NotEmpty(message = "UUID列表不能为空")
  @Schema(description = "UUID列表", requiredMode = RequiredMode.REQUIRED)
  private List<String> uuids;
  @Schema(description = "文档ID")
  @NotEmpty(message = "文档ID不能为空")
  private String documentId;
}
