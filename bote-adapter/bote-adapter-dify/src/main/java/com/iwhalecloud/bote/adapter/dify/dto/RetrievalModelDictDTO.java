package com.iwhalecloud.bote.adapter.dify.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 模型字典
 *
 * @author qian.sisheng
 * @since 2025-10-14
 */
@Getter
@Setter
@ToString
@JsonNaming(SnakeCaseStrategy.class)
public class RetrievalModelDictDTO {
  /** 用于检索的搜索方法。hybrid_search, semantic_search, full_text_search, keyword_search */
  private String searchMethod;
  /** 是否启用重新排序模型以改善搜索结果 */
  private Boolean rerankingEnable;
  /** 重新排序模型的配置 */
  private RerankingMode rerankingModel;
  /** 搜索结果返回的条数 */
  private Integer topK;
  /** 是否应用分数阈值来过滤结果 */
  private Boolean scoreThresholdEnabled;
  /** 结果包含的最低分数 */
  private Float scoreThreshold;
  /** 混合搜索模式中语义搜索的权重 */
  private Float weights;

  /**
   * 重新排序模型的配置。
   */
  @Getter
  @Setter
  @ToString
  @JsonNaming(SnakeCaseStrategy.class)
  public static class RerankingMode {
    /** 重新排序模型的提供商 */
    private String rerankingProviderName;
    /** 重新排序模型的名称 */
    private String rerankingModelName;
  }
}
