package com.iwhalecloud.bote.dto.knowledge.docchain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档块详情
 *
 * @author qian.sisheng
 * @since 2025-07-04
 */

@Getter
@Setter
@ToString
public class DocChainChunkDetailDTO {
  @Schema(description = "文档块ID")
  private Long chunkId;
  @Schema(description = "文档块标题")
  private String chunkTitle;
  @Schema(description = "文档块内容")
  private String chunkContent;
  @Schema(description = "文档块类型")
  private String chunkType;
  @Schema(description = "文档块链接")
  private String chunkUrl;
}
