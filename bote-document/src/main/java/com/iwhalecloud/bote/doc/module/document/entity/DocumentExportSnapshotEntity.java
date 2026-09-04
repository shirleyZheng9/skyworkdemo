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
 * 文档导出快照表
 *
 * @author system
 * @since 2025-09-26
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_dc_document_export_snap")
@Schema(hidden = true)
public class DocumentExportSnapshotEntity extends BaseEntity {

  @Id
  @DiffId
  @Schema(description = "主键ID")
  private Long id;

  @DiffField(name = "document_id")
  @Schema(description = "文档ID")
  private String documentId;

  @DiffField(name = "revision")
  @Schema(description = "文档版本号")
  private Long revision;

  @DiffField(name = "file_id")
  @Schema(description = "文件ID")
  private Long fileId;

  @DiffField(name = "file_extension")
  @Schema(description = "文件扩展名")
  private String fileExtension;

  @DiffField(name = "file_size")
  @Schema(description = "文件大小")
  private Long fileSize;

  @DiffField(name = "tenant_id")
  @Schema(description = "租户ID")
  private Long tenantId;
}
