package com.iwhalecloud.bote.dto.knowledge.docchain.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档块信息
 *
 * @author qian.sisheng
 * @since 2025-07-03
 */
@Getter
@Setter
@ToString
@JsonNaming(SnakeCaseStrategy.class)
public class QueryChunkResponse {
  /** 上一个文档块信息 */
  private ChunkInfo preChunk;
  /** 文档块信息 */
  private ChunkInfo chunk;
  /** 下一个文档块信息 */
  private ChunkInfo nextChunk;
  /** 文档块操作项 */
  private List<String> opts;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonNaming(SnakeCaseStrategy.class)
  public static class ChunkInfo {
    /** 文档 ID */
    private Long docId;
    /** 文档块 ID */
    private Long chunkId;
    /** 文档块名称  格式: 文档名称#文档块名称 */
    private String headingChain;
    /** 文档块内容 */
    private String content;
    /** 文档块类型 text,image */
    private String type;
    /** 图片 */
    private String url;

    @JsonIgnore
    public boolean isImage() {
      return "image".equals(type);
    }
  }
}
