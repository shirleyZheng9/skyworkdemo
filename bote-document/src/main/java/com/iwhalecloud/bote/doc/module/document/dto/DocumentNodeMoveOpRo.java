package com.iwhalecloud.bote.doc.module.document.dto;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档节点拖动请求参数
 *
 * @author Aiqing
 * @since 2025-08-25
 */
@Getter
@Setter
@ToString
@Schema(description = "文档节点拖动请求参数")
public class DocumentNodeMoveOpRo extends TenantBaseRO {

  @Schema(description = "文档ID", requiredMode = RequiredMode.REQUIRED, example = "nod10")
  @NotBlank(message = "文档ID不能为空")
  private String documentId;

  @Schema(description = "父节点的ID", requiredMode = RequiredMode.REQUIRED, example = "nod10")
  private String parentId;

  @Schema(description = "拖动的目标位置的前一个文档节点的ID， 如果是拖动到第一位置传空", example = "nod10")
  private String preNodeId;

  @Schema(description = "文档库ID")
  @NotBlank(message = "文档库ID不能为空")
  private String libraryId;
}
