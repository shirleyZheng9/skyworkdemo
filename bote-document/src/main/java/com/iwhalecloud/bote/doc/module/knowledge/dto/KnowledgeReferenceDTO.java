package com.iwhalecloud.bote.doc.module.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库引用频次排名 DTO
 *
 * @author qian.sisheng
 * @since 2026/02/27
 */
@Getter
@Setter
@ToString
@Schema(description = "知识库引用频次排名DTO")
public class KnowledgeReferenceDTO {

  @Schema(description = "排名序号（全局，从1开始）")
  private Integer rankNo;
  @Schema(description = "知识库ID")
  private Long knowledgeId;
  @Schema(description = "知识库名称")
  private String knowledgeName;
  @Schema(description = "引用次数")
  private Long refCount;
}
