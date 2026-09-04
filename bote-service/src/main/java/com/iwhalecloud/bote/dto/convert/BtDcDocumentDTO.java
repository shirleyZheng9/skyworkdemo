package com.iwhalecloud.bote.dto.convert;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档 DTO
 *
 * @author qian.sisheng
 * @since 2026/03/07
 */
@Getter
@Setter
@ToString
public class BtDcDocumentDTO {
  @Schema(description = "文档ID")
  private String documentId;
  @Schema(description = "创建者ID")
  private Long creatorId;
  @Schema(description = "租户ID")
  private Long tenantId;
}
