package com.iwhalecloud.bote.doc.module.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档引用频次排名 DTO
 *
 * @author qian.sisheng
 * @since 2026/02/27
 */
@Getter
@Setter
@ToString
@Schema(description = "文档引用频次排名DTO")
public class DocumentReferenceDTO {

  /** 排名序号 */
  @Schema(description = "排名序号（全局，从1开始）")
  private Integer rankNo;

  @Schema(description = "文档ID")
  private String docId;
  @Schema(description = "文档名称")
  private String documentName;
  @Schema(description = "所属知识库名称")
  private String knowledgeName;
  @Schema(description = "引用次数")
  private Long refCount;
}
