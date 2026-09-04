package com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * WeKnora 知识检索结果项
 *
 * @author huangyunming
 * @since 2026-03-31
 */
@Getter
@Setter
public class WeKnoraSearchResultDTO {

  /** Chunk ID */
  @JsonProperty("id")
  private String id;

  /** Chunk 文本内容 */
  @JsonProperty("content")
  private String content;

  /** 文件 ID */
  @JsonProperty("knowledge_id")
  private String knowledgeId;

  /** 文档标题 */
  @JsonProperty("knowledge_title")
  private String knowledgeTitle;

  /** 原始文件名 */
  @JsonProperty("knowledge_filename")
  private String knowledgeFilename;

  /** 相关性评分 */
  @JsonProperty("score")
  private Double score;

  /** Chunk 在文档中的索引 */
  @JsonProperty("chunk_index")
  private Integer chunkIndex;

  /** Chunk 类型：text / summary */
  @JsonProperty("chunk_type")
  private String chunkType;
}
