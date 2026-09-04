package com.iwhalecloud.bote.doc.module.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * docchain 主题配置项参数DTO
 *
 * @author qian.sisheng
 * @since 2026/03/10
 */
@Setter
@Getter
@ToString
public class DocChainConfigParamsDTO {
  /** 键 */
  private String key;
  /** 标签 */
  private String label;
  /** 值 */
  private Object values;
  /** 默认值 */
  @JsonProperty("default")
  private Object defaultValue;
}
