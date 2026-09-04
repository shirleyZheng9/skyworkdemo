package com.iwhalecloud.bote.dto.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档 DTO
 *
 * @author auto
 * @since 2024-09-20
 */
@Getter
@Setter
@ToString(callSuper = true)
public class KnowledgeBaseOperateDTO {
  @Schema(description = "主键")
  private Long documentId;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "条件内容")
  private Map<String, Object> content;
}
