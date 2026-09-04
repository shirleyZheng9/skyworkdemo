package com.iwhalecloud.bote.doc.module.collaboration.workbook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作表信息
 *
 * @author Aiqing
 * @since 2025/9/2
 */
@Getter
@Setter
@ToString
@Schema(description = "工作表信息")
public class SheetDTO {

  @Schema(description = "类型")
  private Integer type;

  @Schema(description = "sheet表ID")
  private String id;

  @Schema(description = "sheet名称")
  private String name;

  @Schema(description = "行数")
  private Integer rowCount;

  @Schema(description = "列数")
  private Integer columnCount;

  @Schema(description = "原始元数据")
  private String originalMeta;
}
