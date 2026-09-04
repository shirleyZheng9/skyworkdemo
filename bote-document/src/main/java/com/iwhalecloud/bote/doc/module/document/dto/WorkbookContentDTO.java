package com.iwhalecloud.bote.doc.module.document.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.doc.module.document.entity.WorkbookContentEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作簿内容表
 *
 * @author system
 * @since 2025-09-26
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "工作簿内容")
public class WorkbookContentDTO extends WorkbookContentEntity {

  @Schema(description = "创建人名称")
  private String creatorName;

  @Schema(description = "更新人名称")
  private String updatorName;

  @Schema(description = "文档库ID", example = "lbr09")
  private String libraryId;
}
