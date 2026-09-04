package com.iwhalecloud.bote.doc.module.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class DoKbIDAndDocIdDTO {
  @Schema(description = "主键")
  private Long documentId;
  @Schema(description = "知识库标识")
  private Long knowledgeId;
  @Schema(description = "外系统 ID")
  private Long extSystemId;
  @Schema(description = "空间 ID")
  private Long spaceId;
}
