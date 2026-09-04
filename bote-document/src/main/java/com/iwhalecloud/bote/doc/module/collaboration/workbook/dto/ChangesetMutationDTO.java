package com.iwhalecloud.bote.doc.module.collaboration.workbook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 表格变更信息
 *
 * @author Aiqing
 * @since 2025/9/2
 */
@Getter
@Setter
@ToString
@Schema(description = "变更信息")
public class ChangesetMutationDTO {
  @Schema(description = "变更ID")
  private String id;

  @Schema(description = "变更数据")
  private String data;
}
