package com.iwhalecloud.bote.llm.client.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文本嵌入响应
 *
 * @author bianjp
 * @since 2024-12-23
 */
@Getter
@Setter
@ToString
public class EmbeddingResponse {
  /** 模型名称 */
  private String model;
  /** 对象类型(固定为 list) */
  private String object;
  /** 向量结果列表（与请求中的 input 列表一一对应） */
  private List<EmbeddingResponseItem> data;
  /** 计量信息 */
  private Usage usage;
}
