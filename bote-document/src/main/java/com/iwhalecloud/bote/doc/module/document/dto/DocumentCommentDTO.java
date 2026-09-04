package com.iwhalecloud.bote.doc.module.document.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.doc.module.document.entity.DocumentCommentEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档修订和评论记录DTO
 *
 * @author Aiqing
 * @since 2025-10-16
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class DocumentCommentDTO extends DocumentCommentEntity {

  @Schema(description = "创建人名称")
  private String creatorName;

  @Schema(description = "审核人名称")
  private String reviewerName;

  @Schema(description = "是否可编辑")
  private Boolean canEdit;

  @Schema(description = "是否可删除")
  private Boolean canDelete;

  @Schema(description = "是否可审核")
  private Boolean canReview;
}
