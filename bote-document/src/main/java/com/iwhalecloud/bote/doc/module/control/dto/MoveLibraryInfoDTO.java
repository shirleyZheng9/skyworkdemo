package com.iwhalecloud.bote.doc.module.control.dto;

import com.iwhalecloud.bote.doc.module.document.entity.DcDocumentEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 移动文档库的信息类
 */
@Getter
@Setter
@ToString
@Schema(description = "移动文档库的信息类")
public class MoveLibraryInfoDTO {
  @Schema(description = "移动的文件")
  private DcDocumentEntity srcDocument;
  @Schema(description = "选中的文件夹或者跟节点")
  private DcDocumentEntity preDocument;
}
