package com.iwhalecloud.bote.doc.module.document.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.doc.module.document.entity.DcDocumentEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档
 *
 * @author yangran
 * @since 2025-08-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "文档")
public class DcDocumentDTO extends DcDocumentEntity {
  @Schema(description = "文档库名称")
  private String libraryName;
  @Schema(description = "文件Id")
  private Long fileId;
}
