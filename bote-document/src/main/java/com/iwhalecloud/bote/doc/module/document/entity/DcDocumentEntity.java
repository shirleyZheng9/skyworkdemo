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
 * 文档
 *
 * @author yangran
 * @since 2025-08-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_dc_document")
@Schema(hidden = true)
public class DcDocumentEntity extends BaseEntity {
  @Id
  @DiffId
  private Long id;
  @DiffField(name = "document_id")
  @Schema(description = "文档唯一编码")
  private String documentId;
  @DiffField(name = "document_name")
  @Schema(description = "文档名称")
  private String documentName;
  @DiffField(name = "library_id")
  @Schema(description = "所属文档库唯一编码")
  private String libraryId;
  @DiffField(name = "parent_id")
  @Schema(description = "父文件夹ID，NULL表示根目录")
  private String parentId;
  @DiffField(name = "document_type")
  @Schema(description = "文档类型：WORD-Word文档，EXCEL-Excel表格,FOLDER-文件夹等")
  private String documentType;
  @DiffField(name = "content_source")
  @Schema(description = "内容来源：ONLINE-在线文档，UPLOAD-上传文件")
  private String contentSource;
  @DiffField(name = "file_info_id")
  @Schema(description = "文件信息表ID")
  private Long fileInfoId;
  @DiffField(name = "revision")
  @Schema(description = "版本号, 上传的文件文档使用")
  private Long revision;
  @DiffField(name = "word_count")
  @Schema(description = "字数统计")
  private Integer wordCount;
  @DiffField(name = "view_count")
  @Schema(description = "查看次数")
  private Integer viewCount;
  @DiffField(name = "is_convert")
  @Schema(description = "是否是转换的文档：T-是，F-否")
  private String isConvert;
  @DiffField(name = "is_builtin")
  @Schema(description = "是否系统内置：T-是（不可删除/重命名），F-否")
  private String isBuiltin;
  @DiffField(name = "builtin_type")
  @Schema(description = "内置类型：DIALOG_FOLDER、APP_FOLDER、USER_FOLDER、NULL")
  private String builtinType;
  @DiffField(name = "prev_document_id")
  @Schema(description = "前一个文档ID（单向链表结构，NULL表示链表头节点）")
  private String prevDocumentId;

  @DiffField(name = "permission_mode")
  @Schema(description = "权限模式，0继承，1指定")
  private Integer permissionMode;

  @DiffField(name = "tenant_id")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "space_id")
  @Schema(description = "空间ID")
  private Long spaceId;
  @DiffField(name = "released")
  @Schema(description = "是否需要发布：T为需要发布更新应用过的知识库文档")
  private String released;
  @DiffField(name = "data_url")
  @Schema(description = "在线网页地址")
  private String dataUrl;
}
