package com.iwhalecloud.bote.dto.knowledge;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识召回响应
 *
 * @author bianjp
 * @since 2024-08-30
 */
@Getter
@Setter
@ToString
public class SearchKnowledgeResponse {
  /** 文本知识 */
  private List<KnowledgeScoreItem> text;
  /** 图片知识 */
  private List<KnowledgeScoreItem> image;
  /** chatExcel 类型知识库召回内容 */
  private List<Map<String, Object>> data;

  /**
   * 知识评分项
   */
  @Getter
  @Setter
  @ToString
  public static class KnowledgeScoreItem {
    /** 评分 */
    private Double score;
    /** 知识数据 */
    private KnowledgeItem data;
  }

  /**
   * 知识数据
   */
  @Getter
  @Setter
  @ToString
  public static class KnowledgeItem {
    /** 文档 ID */
    @JsonAlias("doc_id")
    private Long docId;
    /** 标题 */
    @JsonAlias("heading_chain")
    private String heading;
    /** 文档块 ID */
    @JsonAlias("chunk_id")
    private String chunkId;
    /** 内容 */
    private String content;
    /** 总结 */
    private String summary;
    /** 类型(text, image) */
    private String type;
    /** 图片路径，不是完整链接 */
    private String url;
    /** 关联文档 ID */
    private Long fileInfoId;
  }
}
