package com.iwhalecloud.bote.doc.module.document.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.doc.module.document.entity.DocumentFileLogEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档上传文件日志（含关联文档名、文件名、内容来源）
 *
 * @author bote-doc
 * @since 2026-04-15
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "文档上传文件日志明细")
public class DocumentFileLogDTO extends DocumentFileLogEntity {

  @Schema(description = "文档名称（bt_dc_document.document_name）")
  private String documentName;

  @Schema(description = "文件名称（bt_file.file_name）")
  private String fileName;

  @Schema(description = "文档内容来源类型（bt_dc_document.content_source），如 ONLINE、UPLOAD")
  private String contentSource;

  @Schema(description = "创建人名称（bt_user.real_name）")
  private String userName;
}
