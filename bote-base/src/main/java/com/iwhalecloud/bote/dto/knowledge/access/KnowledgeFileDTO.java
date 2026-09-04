package com.iwhalecloud.bote.dto.knowledge.access;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库文档 DTO
 *
 * @author auto
 * @since 2024-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
public class KnowledgeFileDTO {
  @Schema(description = "知识库标识")
  private String knowledgeId;

  @Schema(description = "资源ID")
  private String resourceWid;

  @Schema(description = "资源类型")
  private String resourceType;

  @Schema(description = "文件ID")
  private String docId;

  @Schema(description = "文件名称")
  private String docName;

  @Schema(description = "文件类型")
  private String doctype;

  @Schema(description = "文件大小")
  private Integer docSize;
}
