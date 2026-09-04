package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单的元素信息
 *
 * @author chen.linfa
 * @since 2025-07-26
 */
@Getter
@Setter
@ToString
public class SimpleElementDTO {
  @Schema(description = "主键")
  private Long id;

  @Schema(description = "名称")
  private String name;

  @Schema(description = "类型")
  private String type;
}
