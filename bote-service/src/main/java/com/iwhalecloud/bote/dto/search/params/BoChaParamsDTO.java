package com.iwhalecloud.bote.dto.search.params;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 博查配置参数
 *
 * @author qian.sisheng
 * @since 2026/01/20
 */
@Getter
@Setter
@ToString
public class BoChaParamsDTO {
  /** 新鲜度 */
  private String freshness;
  /** 是否总结内容 */
  private Boolean summary;
  /** 召回页面数量 */
  private Integer count;
  /** 访问令牌 */
  private String token;
  /** 请求地址 */
  private String url;
}
