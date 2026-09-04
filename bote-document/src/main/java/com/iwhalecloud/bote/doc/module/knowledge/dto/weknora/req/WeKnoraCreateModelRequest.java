package com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.req;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * WeKnora 新增模型请求
 *
 * <p>对应 API：{@code POST /v1/models}。</p>
 *
 * @author huangyunming
 * @since 2026-04-02
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeKnoraCreateModelRequest {

  /** 模型名称，如 qwen-plus */
  @JsonProperty("name")
  private String name;

  /** 模型类型：KnowledgeQA / Embedding / Rerank / VLLM */
  @JsonProperty("type")
  private String type;

  /** 来源：remote / local */
  @JsonProperty("source")
  private String source;

  /** 描述 */
  @JsonProperty("description")
  private String description;

  /**
   * 连接参数，如 {@code base_url}、{@code api_key}、{@code provider}（aliyun、generic 等）
   */
  @JsonProperty("parameters")
  private Map<String, Object> parameters;
}
