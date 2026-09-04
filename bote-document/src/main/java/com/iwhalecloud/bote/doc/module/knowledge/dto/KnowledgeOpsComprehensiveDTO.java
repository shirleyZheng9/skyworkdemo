package com.iwhalecloud.bote.doc.module.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 综合排名 DTO
 *
 * @author qian.sisheng
 * @since 2026/02/27
 */
@Getter
@Setter
@ToString
@Schema(description = "综合排名DTO")
public class KnowledgeOpsComprehensiveDTO {

  @Schema(description = "序号")
  private Integer rankNo;
  @Schema(description = "用户ID")
  private Long userId;
  @Schema(description = "真实姓名")
  private String realName;
  @Schema(description = "上传文档数")
  private Long uploadCount;
  @Schema(description = "点赞数")
  private Long likeCount;
  @Schema(description = "点踩数")
  private Long dislikeCount;
  @Schema(description = "被引用次数")
  private Long referenceCount;
  @Schema(description = "综合得分")
  private Long comprehensiveScore;
}
