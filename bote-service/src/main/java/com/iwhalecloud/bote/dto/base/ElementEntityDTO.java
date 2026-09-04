package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 实体数据传输对象
 *
 * @author qian.sisheng
 * @since 2025-12-04
 */
@Getter
@Setter
@ToString
public class ElementEntityDTO {
  @Schema(description = "实体ID")
  private Long entityId;
  @Schema(description = "实体名称")
  private String entityName;
  @Schema(description = "实体编码")
  private String entityCode;
}
