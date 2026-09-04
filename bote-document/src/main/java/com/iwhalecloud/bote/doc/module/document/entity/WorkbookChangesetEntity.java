package com.iwhalecloud.bote.doc.module.document.entity;

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
 * 文档中心表格修改动作
 *
 * @author Aiqing
 * @since 2025-08-15
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_dc_workbook_changeset")
@Schema(hidden = true)
public class WorkbookChangesetEntity extends BaseEntity {
  @Id
  @DiffId
  private Long id;

  @DiffField(name = "document_id")
  @Schema(description = "文档唯一标识")
  private String documentId;

  @DiffField(name = "revision")
  @Schema(description = "变更版本号")
  private Long revision;

  @DiffField(name = "base_rev")
  @Schema(description = "基础版本号")
  private Long baseRev;

  @DiffField(name = "user_id")
  @Schema(description = "用户ID")
  private Long userId;

  @DiffField(name = "mutations")
  @Schema(description = "操作JSON数据")
  private String mutations;

  @DiffField(name = "orig_mutations")
  @Schema(description = "原始操作JSON数据")
  private String origMutations;

  @DiffField(name = "sid")
  @Schema(description = "WebSocket连接ID")
  private String sid;

  @DiffField(name = "req_id")
  @Schema(description = "请求递增数值")
  private Long reqId;

  @DiffField(name = "tenant_id")
  @Schema(description = "租户ID")
  private Long tenantId;
}
