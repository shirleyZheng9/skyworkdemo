package com.iwhalecloud.bote.llm.client.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文本嵌入响应项
 *
 * @author bianjp
 * @since 2024-12-23
 */
@Getter
@Setter
@ToString
public class EmbeddingResponseItem {
  /** 对象类型(固定为 embedding) */
  private String object;
  /** 编号(从 0 开始) */
  private Integer index;
  /** 向量值 */
  private float[] embedding;
}
