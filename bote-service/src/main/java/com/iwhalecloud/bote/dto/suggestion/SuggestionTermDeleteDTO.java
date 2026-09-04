package com.iwhalecloud.bote.dto.suggestion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 联想术语删除参数 DTO
 *
 * @author lizuyin
 * @since 2025-06-11
 */
@Getter
@Setter
@ToString
@Schema(description = "联想术语删除参数")
public class SuggestionTermDeleteDTO {
  @Schema(description = "术语ID")
  private Long termId;

  @Schema(description = "租户ID")
  private Long tenantId;
}
