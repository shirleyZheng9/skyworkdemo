package com.iwhalecloud.bote.dto.knowledge.access.platform.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识召回响应
 *
 * @author lxs
 * @since 2025/7/12
 */
@Getter
@Setter
@ToString
@Schema(description = "知识召回响应")
public class KnowledgeSearchResponse {

  @Schema(description = "文本类型检索结果列表")
  private List<ResultItem> text;

  @Schema(description = "表格类型检索结果列表")
  private List<ResultItem> table;

  @Schema(description = "图片类型检索结果列表")
  private List<ResultItem> image;

  /**
   * 检索结果项
   */
  @Getter
  @Setter
  @ToString
  @Schema(description = "检索结果项")
  public static class ResultItem {

    @Schema(description = "相似度分数")
    private Float score;

    @Schema(description = "文档块的具体内容")
    private DataBlock data;
  }

  /**
   * 文档块数据
   */
  @Getter
  @Setter
  @ToString
  @Schema(description = "文档块数据")
  public static class DataBlock {

    @Schema(description = "文档ID")
    private Integer docId;

    @Schema(description = "标题链")
    private String headingChain;

    @Schema(description = "文档内容")
    private String content;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "页码")
    private Integer pageNum;

    @Schema(description = "路径")
    private String path;

    @Schema(description = "类型，包括：text，image，table，code")
    private String type;

    @Schema(description = "URL")
    private String url;

    @Schema(description = "上下文内容列表")
    private List<Object> context;

    @Schema(description = "HTML内容")
    private String htmlContent;

    @Schema(description = "JSON内容")
    private String jsonContent;

    @Schema(description = "描述")
    private String description;
  }
}
