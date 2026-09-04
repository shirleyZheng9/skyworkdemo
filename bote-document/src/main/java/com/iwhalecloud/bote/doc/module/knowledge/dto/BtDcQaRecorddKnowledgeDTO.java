package com.iwhalecloud.bote.doc.module.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单知识
 *
 * @author bianjp
 * @since 2024-12-18
 */
@Getter
@Setter
@ToString
public class BtDcQaRecorddKnowledgeDTO {
  @Schema(description = "问答ID（用于批量查询分组）")
  private Long qaId;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "知识库 ID")
  private Long knowledgeId;
  @Schema(description = "知识库名称")
  private String knowledgeName;
  @Schema(description = "主题 ID")
  private Long topicId;
  @Schema(description = "知识库类型")
  private String knowledgeType;
  @Schema(description = "颜色")
  private String color;
  @Schema(description = "知识库图标")
  private String knowledgeIcon;

}
