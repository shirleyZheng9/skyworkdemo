package com.iwhalecloud.bote.doc.module.knowledge.entity;

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
 * 文档 Entity
 *
 * @author auto
 * @since 2024-09-20
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_document")
public class DocumentEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long documentId;
  @DiffField(name = "KNOWLEDGE_ID", parent = true, required = false)
  @Schema(description = "知识库标识")
  private Long knowledgeId;
  @DiffField(name = "FILE_INFO_ID")
  @Schema(description = "文件标识")
  private Long fileInfoId;
  @DiffField(name = "DATA_FROM")
  @Schema(description = "文档来源：10A上传文档 10B网页爬取")
  private String dataFrom;
  @DiffField(name = "DOC_NAME")
  @Schema(description = "文档名字")
  private String docName;
  @DiffField(name = "PARSE_STARTED_TIME")
  @Schema(description = "解析开始时间")
  private Date parseStartedTime;
  @DiffField(name = "EXTRACT_STARTED_TIME")
  @Schema(description = "要素提取时间")
  private Date extractStartedTime;
  @DiffField(name = "PROCESS_COMPLETED_AT")
  @Schema(description = "构建完成时间")
  private Date processCompletedAt;
  @DiffField(name = "DOC_STATUS")
  @Schema(description = "状态: 10A未处理 10B解析中 10C要素抽取中 10D构建完成 10E构建失败")
  private String docStatus;
  @DiffField(name = "ERROR_MESSAGE")
  @Schema(description = "错误信息")
  private String errorMessage;
  @DiffField(name = "EXT_SYSTEM_ID")
  @Schema(description = "外系统 ID")
  private Long extSystemId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
  @DiffField(name = "SPACE_ID")
  @Schema(description = "空间ID")
  private Long spaceId;
  @DiffField(name = "DOCUMENT_TYPE")
  @Schema(description = "文档类型：结构化文档structDoc 非结构化文档 unstructDoc")
  private String documentType;

  @DiffField(name = "FILE_MD5")
  @Schema(description = "文件MD5校验值")
  private String fileMd5;

  @DiffField(name = "VERSION_NUMBER")
  @Schema(description = "版本号")
  private String versionNumber;

  @DiffField(name = "FILE_SIZE")
  @Schema(description = "文档文件大小（字节）")
  private Long fileSize;

  @DiffField(name = "LAST_SYNC_TIME")
  @Schema(description = "最后同步时间")
  private Date lastSyncTime;

  @DiffField(name = "LAST_CHECK_TIME")
  @Schema(description = "最后检查时间")
  private Date lastCheckTime;

  @DiffField(name = "HAS_UPDATE")
  @Schema(description = "是否有更新：0-无更新，1-有更新")
  private String hasUpdate;

  @DiffField(name = "DC_DOCUMENT_ID")
  @Schema(description = "bt_dc_document.document_id文档唯一编码")
  private String dcDocumentId;
}
