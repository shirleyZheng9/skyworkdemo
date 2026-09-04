package com.iwhalecloud.bote.doc.module.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * docchain 主题参数DTO
 *
 * @author qian.sisheng
 * @since 2026/03/10
 */
@Getter
@Setter
@ToString
@JsonNaming(SnakeCaseStrategy.class)
public class DocChainTopicParamsDTO {
  /** 公共配置 */
  private List<DocChainConfigParamsDTO> commonConfig;
  /** PDF配置 */
  private List<DocChainConfigParamsDTO> pdfConfig;
  /** 图片配置 */
  private List<DocChainConfigParamsDTO> imageConfig;
  /** Excel配置 */
  private List<DocChainConfigParamsDTO> excelConfig;
  /** Word配置 */
  private List<DocChainConfigParamsDTO> wordConfig;
  /** PPT配置 */
  private List<DocChainConfigParamsDTO> pptConfig;
  /** 扩展配置 */
  private List<DocChainConfigParamsDTO> extraConfig;
  /** 召回配置 */
  private List<DocChainConfigParamsDTO> retrieveConfig;
  /** 知识图谱配置 */
  @JsonAlias("KnowledgeGraph_config")
  private List<DocChainConfigParamsDTO> knowledgeGraphConfig;

  /**
   * 获取所有配置
   */
  public List<DocChainConfigParamsDTO> getAllConfig() {
    return Stream.of(commonConfig, pdfConfig, imageConfig, excelConfig, wordConfig,
        pptConfig, extraConfig, retrieveConfig, knowledgeGraphConfig)
      .filter(Objects::nonNull)
      .flatMap(List::stream)
      .toList();
  }
}
