package com.iwhalecloud.bote.doc.module.knowledge.dto;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库问答测试参数
 *
 * @author bianjp
 * @since 2024-11-15
 */
@Getter
@Setter
@ToString
@Schema(description = "知识库问答测试参数")
public class KnowledgeChatTestDTO extends TenantBaseRO {
  @Schema(description = "知识库 ID", requiredMode = RequiredMode.REQUIRED)
  private Long knowledgeId;
  @Schema(description = "问句", requiredMode = RequiredMode.REQUIRED)
  private String query;
  @Schema(description = "是否流式(默认否)")
  private Boolean stream;
  @Schema(description = "是否返回参考文档(默认否)")
  private Boolean withReferences;
  @Schema(description = "客户端 ID(流式接口中断请求使用)")
  private String clientId;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "大模型 ID")
  private Long modelId;
}
