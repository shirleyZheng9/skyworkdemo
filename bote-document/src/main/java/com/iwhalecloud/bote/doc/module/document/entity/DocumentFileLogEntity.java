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
 * 文档中心-文档表-上传文件日志记录
 *
 * @author bote-doc
 * @since 2026-04-15
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_dc_document_file_log")
@Schema(hidden = true)
public class DocumentFileLogEntity extends BaseEntity {

  @Id
  @DiffId
  @Schema(description = "主键")
  private Long id;

  @DiffField(name = "document_id")
  @Schema(description = "文档id")
  private String documentId;

  @DiffField(name = "file_id")
  @Schema(description = "文件id")
  private Long fileId;

  @DiffField(name = "revision")
  @Schema(description = "版本号")
  private Long revision;

  @DiffField(name = "tenant_id")
  @Schema(description = "租户id")
  private Long tenantId;

}
