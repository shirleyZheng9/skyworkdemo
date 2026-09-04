package com.iwhalecloud.bote.dto.knowledge.access;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库目录 DTO
 *
 * @author auto
 * @since 2024-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
public class KnowledgeCatalogDTO {
  @Schema(description = "目录ID")
  private Long catalogId;

  @Schema(description = "目录名称")
  private String catalogName;

  @Schema(description = "父目录 ID")
  private Long parCatalogId;

  @Schema(description = "目录类型")
  private String catalogType;

  @Schema(description = "目录路径，逗号分割。由后端计算")
  private String catalogPath;

  @Schema(description = "子节点")
  private List<KnowledgeCatalogDTO> children;
}
