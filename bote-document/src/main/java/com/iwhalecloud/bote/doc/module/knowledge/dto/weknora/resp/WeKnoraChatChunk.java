package com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * WeKnora SSE 流式响应数据块
 *
 * @author huangyunming
 * @since 2026-03-31
 */
@Getter
@Setter
public class WeKnoraChatChunk {

  /** 消息 ID */
  @JsonProperty("id")
  private String id;

  /**
   * 响应类型：
   * <ul>
   *   <li>{@code references} - 参考文档（先于答案推送）</li>
   *   <li>{@code answer} - 回答文本片段</li>
   * </ul>
   */
  @JsonProperty("response_type")
  private String responseType;

  /** 回答文本片段（response_type=answer 时有值） */
  @JsonProperty("content")
  private String content;

  /** 是否对话结束 */
  @JsonProperty("done")
  private Boolean done;

  /** 参考文档列表（response_type=references 时有值） */
  @JsonProperty("knowledge_references")
  private List<KnowledgeReference> knowledgeReferences;

  @Getter
  @Setter
  public static class KnowledgeReference {
    @JsonProperty("id")
    private String id;
    @JsonProperty("content")
    private String content;
    @JsonProperty("knowledge_id")
    private String knowledgeId;
    @JsonProperty("knowledge_title")
    private String knowledgeTitle;
    @JsonProperty("knowledge_filename")
    private String knowledgeFilename;
    @JsonProperty("score")
    private Double score;
    @JsonProperty("chunk_index")
    private Integer chunkIndex;
    @JsonProperty("chunk_type")
    private String chunkType;
    @JsonProperty("match_type")
    private Integer matchType;
  }
}
