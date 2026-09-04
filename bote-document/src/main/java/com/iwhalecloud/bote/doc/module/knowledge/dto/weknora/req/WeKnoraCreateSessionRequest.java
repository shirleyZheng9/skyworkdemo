package com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.req;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * WeKnora 创建会话请求
 *
 * <p>单库时传 {@code knowledge_base_id}；多库时传 {@code knowledge_base_ids}（与 WeKnora 侧约定一致）。</p>
 *
 * @author huangyunming
 * @since 2026-03-31
 */
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeKnoraCreateSessionRequest {

  /** 单个知识库 ID */
  @JsonProperty("knowledge_base_id")
  private String knowledgeBaseId;

  /** 多个知识库 ID（与 {@code knowledge_base_id} 二选一） */
  @JsonProperty("knowledge_base_ids")
  private List<String> knowledgeBaseIds;

  /** 会话策略 */
  @JsonProperty("session_strategy")
  private SessionStrategy sessionStrategy;

  @Getter
  @Setter
  @Builder
  public static class SessionStrategy {
    /** 最大对话轮数 */
    @JsonProperty("max_rounds")
    private Integer maxRounds;
    /** 是否开启查询改写 */
    @JsonProperty("enable_rewrite")
    private Boolean enableRewrite;
    /** 未命中策略：FIXED_RESPONSE / CONTINUE_WITHOUT_KNOWLEDGE */
    @JsonProperty("fallback_strategy")
    private String fallbackStrategy;
    /** 未命中时固定回复 */
    @JsonProperty("fallback_response")
    private String fallbackResponse;
    /** 向量检索 Top-K */
    @JsonProperty("embedding_top_k")
    private Integer embeddingTopK;
    /** 向量相似度阈值 */
    @JsonProperty("vector_threshold")
    private Double vectorThreshold;
    /** 关键词匹配阈值 */
    @JsonProperty("keyword_threshold")
    private Double keywordThreshold;
    /** Rerank 模型 ID */
    @JsonProperty("rerank_model_id")
    private String rerankModelId;
    /** 重排序后保留数量 */
    @JsonProperty("rerank_top_k")
    private Integer rerankTopK;
    /** 重排序阈值 */
    @JsonProperty("rerank_threshold")
    private Double rerankThreshold;
    /** WeKnora 侧对话模型 ID */
    @JsonProperty("summary_model_id")
    private String summaryModelId;
    /** 对话模型参数 */
    @JsonProperty("summary_parameters")
    private SummaryParameters summaryParameters;
  }

  @Getter
  @Setter
  @Builder
  public static class SummaryParameters {
    @JsonProperty("temperature")
    private Double temperature;
    @JsonProperty("max_completion_tokens")
    private Integer maxCompletionTokens;
    @JsonProperty("prompt")
    private String prompt;
    @JsonProperty("context_template")
    private String contextTemplate;
  }
}
