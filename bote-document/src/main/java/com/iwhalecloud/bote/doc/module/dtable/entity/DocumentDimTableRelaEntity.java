package com.iwhalecloud.bote.doc.module.dtable.entity;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档中心-文档与多维表格关联关系表
 *
 * @author auto
 * @since 2026-01-09
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_dc_document_dtable_rela")
@Schema(hidden = true)
public class DocumentDimTableRelaEntity extends BaseEntity {

  @Id
  @DiffId
  @Schema(description = "主键")
  private Long id;

  @DiffField(name = "document_id")
  @Schema(description = "文档唯一编码")
  private String documentId;

  @DiffField(name = "table_space_id")
  @Schema(description = "多维表格空间ID")
  private String tableSpaceId;

  @DiffField(name = "root_node_id")
  @Schema(description = "多维表格根节点ID")
  private String rootNodeId;

  @DiffField(name = "status_cd")
  @Schema(description = "状态：00A-正常，00X-删除")
  private String statusCd;

  @DiffField(name = "tenant_id")
  @Schema(description = "租户ID")
  private Long tenantId;
}
