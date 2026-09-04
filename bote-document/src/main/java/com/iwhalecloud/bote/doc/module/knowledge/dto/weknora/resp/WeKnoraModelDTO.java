package com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * WeKnora 模型信息
 *
 * <p>对应 WeKnora API {@code GET /v1/models} 响应中的单条模型记录。</p>
 *
 * <p>模型类型说明：</p>
 * <ul>
 *   <li>{@code KnowledgeQA} — 对话模型，用于 Session summary_model_id</li>
 *   <li>{@code Embedding} — 嵌入模型，知识库文档向量化</li>
 *   <li>{@code Rerank} — 排序模型，检索结果重排序</li>
 *   <li>{@code VLLM} — 视觉语言模型，多模态文档处理</li>
 * </ul>
 *
 * @author huangyunming
 * @since 2026-04-02
 */
@Getter
@Setter
public class WeKnoraModelDTO {

  /** 模型 UUID */
  @JsonProperty("id")
  private String id;

  /** WeKnora 侧租户 ID */
  @JsonProperty("tenant_id")
  private Long tenantId;

  /** 模型名称（如 qwen-plus、text-embedding-v3） */
  @JsonProperty("name")
  private String name;

  /** 模型类型：KnowledgeQA / Embedding / Rerank / VLLM */
  @JsonProperty("type")
  private String type;

  /** 来源：remote / local */
  @JsonProperty("source")
  private String source;

  /** 模型描述 */
  @JsonProperty("description")
  private String description;

  /** 是否为默认模型 */
  @JsonProperty("is_default")
  private Boolean isDefault;

  /** 状态：active / inactive */
  @JsonProperty("status")
  private String status;
}
