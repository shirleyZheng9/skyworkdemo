package com.iwhalecloud.bote.llm.client.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文本嵌入请求
 *
 * @author bianjp
 * @since 2024-12-23
 */
@Getter
@Setter
@ToString
public class EmbeddingRequest {
  /** 模型名称 */
  private String model;
  /** 输入文本 */
  private List<String> input;
}
