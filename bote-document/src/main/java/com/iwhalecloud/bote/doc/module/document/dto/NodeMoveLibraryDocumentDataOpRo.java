package com.iwhalecloud.bote.doc.module.document.dto;

import java.util.List;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 发布移动移除事件的数据
 */
@Getter
@Setter
@ToString
@Schema(description = "发布移动移除事件的数据")
public class NodeMoveLibraryDocumentDataOpRo extends TenantBaseRO {

  @Schema(description = "文档ID，移动的文件id或者文件夹id", requiredMode = RequiredMode.REQUIRED, example = "nod10")
  private String documentId;

  @Schema(description = "子节点的文档或者文件夹", requiredMode = RequiredMode.REQUIRED, example = "nod10")
  private List<String> subIds;

  @Schema(description = "文档库ID，目标文档库id")
  private String targetLibraryId;

  @Schema(description = "源文件对应的原文档库id")
  private String libraryId;
}
