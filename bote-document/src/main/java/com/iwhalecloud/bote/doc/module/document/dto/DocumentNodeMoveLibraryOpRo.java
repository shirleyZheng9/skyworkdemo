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
public class DocumentNodeMoveLibraryOpRo extends TenantBaseRO {

  @Schema(description = "文档ID，移动的文件id或者文件夹id", requiredMode = RequiredMode.REQUIRED, example = "nod10")
  @NotBlank(message = "文档ID不能为空")
  private String documentId;

  @Schema(description = "父节点的ID，目标文件夹id，并且该文件夹需要跟选中的文档库是同一个", requiredMode = RequiredMode.REQUIRED, example = "nod10")
  private String parentId;

  @Schema(description = "文档库ID，目标文档库id")
  @NotBlank(message = "文档库ID不能为空")
  private String libraryId;

  @Schema(description = "同级还是子级，T为子级，F值为同级的后面")
  @NotBlank(message = "移动位置不能为空，如果父级节点为空那么默认移动到文档库下的顶级")
  private String subType;
}
