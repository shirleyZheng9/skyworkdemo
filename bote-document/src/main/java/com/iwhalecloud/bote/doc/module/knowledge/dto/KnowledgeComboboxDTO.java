package com.iwhalecloud.bote.doc.module.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库下拉框
 *
 * <p>提供给前端的下拉框组件使用，只需要基本信息，兼容不同知识库类型</p>
 *
 * @author bianjp
 * @since 2024-10-08
 */
@Getter
@Setter
@ToString
@Schema(description = "知识库下拉框")
public class KnowledgeComboboxDTO {
  @Schema(description = "知识库 ID")
  private Long knowledgeId;
  @Schema(description = "知识库名称")
  private String knowledgeName;
  @Schema(description = "知识库描述")
  private String knowledgeDesc;
  @Schema(description = "知识库类型")
  private String knowledgeType;
}
