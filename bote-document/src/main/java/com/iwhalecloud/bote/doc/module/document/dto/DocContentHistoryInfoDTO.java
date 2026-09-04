package com.iwhalecloud.bote.doc.module.document.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档版本
 *
 * @author yangran
 * @since 2025-08-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "文档版本")
public class DocContentHistoryInfoDTO {
  @Schema(description = "分组名称：当天为「今天」，其余为 uuuu-MM-dd")
  private String revisionName;

  @Schema(description = "该分组下的文档版本明细；组内按创建时间倒序，当前编辑内容（若有）固定为第一条")
  private List<DocContentHistoryDTO> data;
}
