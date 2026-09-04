package com.iwhalecloud.bote.doc.module.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档贡献DTO
 *
 * @author qian.sisheng
 * @since 2026/03/02
 */
@Getter
@Setter
@ToString
public class DocumentContributeRequestDTO {
  @Schema(description = "文档ID")
  private String documentId;
  @Schema(description = "用户ID列表")
  private List<Long> userIds;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "组织ID列表")
  private List<Long> orgIds;
  @Schema(description = "空间ID")
  private Long spaceId;
}

