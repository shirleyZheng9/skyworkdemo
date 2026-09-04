package com.iwhalecloud.bote.doc.module.dtable.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Aiqing
 * @since 2026/1/9
 */
@Getter
@Setter
@ToString
public class DimTableCreateResultDTO {

  @Schema(description = "空间ID")
  private String spaceId;
  @Schema(description = "根节点ID")
  private String rootNodeId;
}
