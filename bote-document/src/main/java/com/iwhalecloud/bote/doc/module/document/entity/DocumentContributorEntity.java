package com.iwhalecloud.bote.doc.module.document.entity;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档贡献者表
 *
 * @author Aiqing
 * @since 2025-09-18
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_dc_document_contributor")
@Schema(hidden = true)
public class DocumentContributorEntity extends BaseEntity {

  @Id
  @DiffId
  @Schema(description = "主键")
  private Long id;

  @DiffField(name = "document_id")
  @Schema(description = "文档唯一编码")
  private String documentId;

  @DiffField(name = "user_id")
  @Schema(description = "用户ID")
  private Long userId;

  @DiffField(name = "contributor_score")
  @Schema(description = "贡献者得分")
  private BigDecimal contributorScore;

  @DiffField(name = "tenant_id")
  @Schema(description = "租户ID")
  private Long tenantId;

  @DiffField(name = "status_cd")
  @Schema(description = "状态：00A-正常，00X-删除")
  private String statusCd;
}
