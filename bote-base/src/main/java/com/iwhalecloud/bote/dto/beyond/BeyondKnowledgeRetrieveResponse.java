package com.iwhalecloud.bote.dto.beyond;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应知识库检索响应
 *
 * @author zhao.xu104
 * @since 2025/07/22
 */
@Getter
@Setter
@ToString
public class BeyondKnowledgeRetrieveResponse {
  /** 检索结果列表 每项为一个文档片段集合 */
  private List<ResultItem> resultObject;
  /** 返回信息 如"success" */
  private String resultMsg;
  /** 状态码 200为成功 */
  private String resultCode;

  /**
   * 检索结果项
   */
  @Getter
  @Setter
  @ToString
  public static class ResultItem {
    /** 文档片段列表 */
    private List<TextItem> text;
  }

  /**
   * 文档片段详细信息
   */
  @Getter
  @Setter
  @ToString
  public static class TextItem {
    /** 相关性得分 0-1之间，越高越相关 */
    private Double score;
    /** 文档片段详细信息 */
    private DocumentData data;
  }

  /**
   * 文档片段数据
   */
  @Getter
  @Setter
  @ToString
  public static class DocumentData {
    /** 文档ID */
    @JsonProperty("document_id")
    private Long documentId;
    /** 文档名称 */
    private String documentName;
    /** 文档结构层级链 */
    @JsonProperty("heading_chain")
    private String headingChain;
    /** 文档片段ID */
    @JsonProperty("chunk_id")
    private Long chunkId;
    /** 片段相关性得分 */
    private Double score;
    /** 文档片段内容 */
    private String content;
    /** 文档ID 同document_id */
    @JsonProperty("doc_id")
    private Long docId;
    /** 片段类型 如"text" */
    private String type;
    /** 片段原文链接 如有 */
    private String url;
  }
}
