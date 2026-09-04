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
 * 文档动态记录表
 *
 * @author Aiqing
 * @since 2025-08-21
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_dc_document_activity")
@Schema(hidden = true)
public class DocumentActivityEntity extends BaseEntity {

  @Id
  @DiffId
  private Long activityId;

  @DiffField(name = "document_id")
  @Schema(description = "文档ID")
  private String documentId;

  @DiffField(name = "library_id")
  @Schema(description = "文档库ID")
  private String libraryId;

  @DiffField(name = "user_id")
  @Schema(description = "操作用户ID")
  private Long userId;

  @DiffField(name = "action_type")
  @Schema(description = "操作类型：VIEW-查看，EDIT-编辑，DOWNLOAD-下载，DELETE-删除，CREATE-创建")
  private String actionType;

  @DiffField(name = "ip_address")
  @Schema(description = "IP地址")
  private String ipAddress;

  @DiffField(name = "user_agent")
  @Schema(description = "用户代理")
  private String userAgent;

  @DiffField(name = "tenant_id")
  @Schema(description = "租户ID")
  private Long tenantId;

  @DiffField(name = "old_document_name")
  @Schema(description = "老的文件名")
  private String oldDocumentName;

  @DiffField(name = "target_library_id")
  @Schema(description = "目标仓库id")
  private String targetLibraryId;
}
