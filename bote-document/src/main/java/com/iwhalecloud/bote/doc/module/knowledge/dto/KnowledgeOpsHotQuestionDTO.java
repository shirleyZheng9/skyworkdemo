package com.iwhalecloud.bote.doc.module.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库热门问题DTO
 *
 * @author qian.sisheng
 * @since 2026/02/28
 */
@Getter
@Setter
@ToString
public class KnowledgeOpsHotQuestionDTO {
  @Schema(description = "ID")
  private String id;
  @Schema(description = "问题")
  private String question;
  @Schema(description = "数量")
  private Integer count;
}
