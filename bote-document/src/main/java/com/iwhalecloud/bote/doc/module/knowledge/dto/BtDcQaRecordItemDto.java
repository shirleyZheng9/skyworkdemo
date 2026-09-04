package com.iwhalecloud.bote.doc.module.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 每月统计量
 *
 * @author linmengfan
 * @since 2025-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
public class BtDcQaRecordItemDto {
  @Schema(description = "一个key")
  private String name;
  @Schema(description = "value数")
  private String value;
}
