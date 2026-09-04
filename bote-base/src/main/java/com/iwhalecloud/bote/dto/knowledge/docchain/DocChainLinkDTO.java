package com.iwhalecloud.bote.dto.knowledge.docchain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * DocChain 文档切片信息
 */
@Getter
@Setter
@ToString
public class DocChainLinkDTO {
  /** 文档切片内容 */
  private String description;
  /** 切片内容评分 */
  private Double score;
  /** 切片内容链接 */
  private String url;
}
