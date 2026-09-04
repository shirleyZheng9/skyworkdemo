package com.iwhalecloud.bote.dto.knowledge.docchain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author qian.sisheng
 * @since 2025-07-04
 */
@Getter
@Setter
@ToString
public class DocChainChunkDTO {
  @Schema(description = "文档名称")
  private String docName;
  @Schema(description = "文档ID")
  private Long docId;
  @Schema(description = "第一个文档块")
  private DocChainChunkDetailDTO preChunk;
  @Schema(description = "文档块")
  private DocChainChunkDetailDTO chunk;
  @Schema(description = "最后一个文档块")
  private DocChainChunkDetailDTO nextChunk;
}
