package com.iwhalecloud.bote.doc.module.document.entity;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档修订和评论记录表
 *
 * @author Aiqing
 * @since 2025-10-16
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_dc_document_comment")
public class DocumentCommentEntity extends BaseEntity {

  @DiffId
  @Schema(description = "记录ID")
  private Long commentId;

  @DiffField(name = "document_id")
  @Schema(description = "文档ID")
  private String documentId;

  @DiffField(name = "uuid")
  @Schema(description = "记录UUID")
  private String uuid;

  @DiffField(name = "content")
  @Schema(description = "内容")
  private String content;

  @DiffField(name = "old_content")
  @Schema(description = "旧内容（仅修订记录使用）")
  private String oldContent;

  @DiffField(name = "record_type")
  @Schema(description = "记录类型：CORRECTION-修订，COMMENT-评论")
  private String recordType;

  @DiffField(name = "correction_type")
  @Schema(description = "修订类型：MODIFY-修正，ADD-新增，DELETE-删除, SET-设置（仅修订记录使用）")
  private String correctionType;

  @DiffField(name = "correction_reason")
  @Schema(description = "修订原因（仅修订记录使用）")
  private String correctionReason;

  @DiffField(name = "review_status")
  @Schema(description = "审核状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝（仅修订记录使用）")
  private String reviewStatus;

  @DiffField(name = "reviewer_id")
  @Schema(description = "审核人ID（仅修订记录使用）")
  private Long reviewerId;

  @DiffField(name = "review_time")
  @Schema(description = "审核时间（仅修订记录使用）")
  private Date reviewTime;

  @DiffField(name = "parent_id")
  @Schema(description = "父记录ID，用于评论回复（仅评论记录使用）")
  private Long parentId;

  @DiffField(name = "tenant_id")
  @Schema(description = "租户ID")
  private Long tenantId;

  @DiffField(name = "edit")
  @Schema(description = "是否编辑过：T为编辑过")
  private String edit;
}
