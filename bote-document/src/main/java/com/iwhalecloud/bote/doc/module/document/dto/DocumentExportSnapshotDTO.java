package com.iwhalecloud.bote.doc.module.document.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.doc.module.document.entity.DocumentExportSnapshotEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档导出快照表
 *
 * @author system
 * @since 2025-09-26
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "文档导出快照")
public class DocumentExportSnapshotDTO extends DocumentExportSnapshotEntity {

  @Schema(description = "创建人名称")
  private String creatorName;

  @Schema(description = "更新人名称")
  private String updatorName;

  @Schema(description = "文档名称")
  private String documentName;

  @Schema(description = "文件名称")
  private String fileName;
}
