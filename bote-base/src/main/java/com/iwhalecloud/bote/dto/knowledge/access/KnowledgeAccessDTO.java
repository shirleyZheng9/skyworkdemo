package com.iwhalecloud.bote.dto.knowledge.access;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 接入知识库信息 DTO
 *
 * @author auto
 * @since 2024-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
public class KnowledgeAccessDTO {
  @Schema(description = "知识库标识")
  private Long knowledgeId;

  @Schema(description = "知识库名称")
  private String knowledgeName;

  @Schema(description = "知识库类型")
  private String knowledgeType;

  @Schema(description = "知识库描述")
  private String knowledgeDesc;

  @Schema(description = "知识库图标")
  private String knowledgeIcon;

  @Schema(description = "主题 ID")
  private String topicId;

  @Schema(description = "文件数")
  private Integer fileCounts;
}
