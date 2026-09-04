package com.iwhalecloud.bote.dto.base.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeleteCatalogDTO {
  @Schema(description = "主键")
  private Long catalogId;
  @Schema(description = "租户 ID")
  private Long tenantId;
}
