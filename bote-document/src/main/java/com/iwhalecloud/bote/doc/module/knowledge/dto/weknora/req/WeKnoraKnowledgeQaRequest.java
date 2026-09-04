package com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.req;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * WeKnora 知识问答请求（仅 SSE 流式）
 *
 * <p>须继承 {@link TenantBaseRO}，以便 {@code TenantSecurityAspect} 从 JSON 体解析租户 ID（与 {@link WeKnoraSearchRequest} 一致）。</p>
 *
 * @author auto
 * @since 2026-04-02
 */
@Getter
@Setter
@ToString(callSuper = true)
public class WeKnoraKnowledgeQaRequest extends TenantBaseRO {

  @Schema(description = "WeKnora 知识库 ID 列表，至少一个；支持多库联合问答", requiredMode = Schema.RequiredMode.REQUIRED)
  private List<String> knowledgeBaseIds;

  @Schema(description = "用户问题", requiredMode = Schema.RequiredMode.REQUIRED)
  private String query;

  @Schema(description = "博特侧会话标识，多轮对话时传入同一值；不传则每次请求独立会话")
  private String boteSessionId;

  @Schema(description = "创建人 ID，可选；不传时优先使用当前登录用户，写入 bt_weknora_chat_session.creator_id")
  @JsonAlias("userId")
  private Long creatorId;

  @Schema(description = "客户端 ID，用于中断 SSE 请求")
  private String clientId;

  @Schema(description = "模型 ID")
  private Long modelId;
}
