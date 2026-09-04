package com.iwhalecloud.bote.doc.module.document.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

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
public class DocumentFileLogInfoDTO {

  @Schema(description = "分组名称：当天为「今天」，其余为 uuuu-MM-dd")
  private String revisionName;

  @Schema(description = "该分组下的上传文件日志明细，组内按创建时间倒序")
  private List<DocumentFileLogDTO> data;
}
