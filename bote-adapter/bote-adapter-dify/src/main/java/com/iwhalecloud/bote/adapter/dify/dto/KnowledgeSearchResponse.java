package com.iwhalecloud.bote.adapter.dify.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识检索响应
 *
 * @author qian.sisheng
 * @since 2025-10-14
 */
@Getter
@Setter
@ToString
public class KnowledgeSearchResponse {
  /** 检索模型 */
  private List<Record> records;

  @Getter
  @Setter
  @ToString
  public static class Record {
    /** 检索模型 */
    private Segment segment;
    /** 评分 */
    private Double score;
  }

  @Getter
  @Setter
  @ToString
  public static class Segment {
    /** 检索内容 */
    private String content;
    /** 文档 */
    private Document document;
  }

  @Getter
  @Setter
  @ToString
  public static class Document {
    /** 文档ID */
    private String id;
    /** 文档名称 */
    private String name;
  }
}
