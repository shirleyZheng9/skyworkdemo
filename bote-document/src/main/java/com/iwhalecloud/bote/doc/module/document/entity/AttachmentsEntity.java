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
 * 文档附件表
 *
 * @author Aiqing
 * @since 2025-08-15
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_dc_attachments")
@Schema(hidden = true)
public class AttachmentsEntity extends BaseEntity {
  @Id
  @DiffId
  private Long id;

  @DiffField(name = "document_id")
  @Schema(description = "文档唯一标识")
  private String documentId;

  @DiffField(name = "file_id")
  @Schema(description = "文件唯一标识")
  private Long fileId;

  @DiffField(name = "file_name")
  @Schema(description = "文件名称")
  private String fileName;

  @DiffField(name = "file_size")
  @Schema(description = "文件大小")
  private Long fileSize;

  @DiffField(name = "file_extension")
  @Schema(description = "文件扩展名")
  private String fileExtension;

  @DiffField(name = "tenant_id")
  @Schema(description = "租户ID")
  private Long tenantId;
}
