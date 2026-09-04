package com.iwhalecloud.bote.doc.module.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.doc.consts.KnowledgeConsts;
import com.iwhalecloud.bote.doc.module.knowledge.entity.DocumentEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 文档 DTO
 *
 * @author auto
 * @since 2024-09-20
 */
@Getter
@Setter
@ToString(callSuper = true)
public class DocumentDTO extends DocumentEntity {
  @Schema(description = "文件Id")
  private Long fileId;
  @Schema(description = "文件名称")
  private String fileName;
  @Schema(description = "文件大小", hidden = true)
  private Long fileSize;
  @Schema(description = "文件路径")
  private String filePath;
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "知识库类型")
  private String knowledgeType;
  @Schema(description = "主题 ID")
  private Long topicId;
  @Schema(description = "文档参数列表")
  private List<DocumentParameterDTO> parameters;
  @Schema(description = "文档内容列表")
  private List<DocumentContentDTO> contents;
  @Schema(description = "文档内容ID列表")
  private List<Long> contentIds;
  @Schema(description = "文档内容操作列")
  private Map<String, String> contentCellValue;
  @Schema(description = "文档内容（Markdown 格式，仅用于 DocChain）")
  private String content;
  @Schema(description = "文档类型：WORD-Word文档，EXCEL-Excel表格,FOLDER-文件夹等")
  private String dcDocumentType;
  @Schema(description = "资源路径，仅文档有值")
  private String resourcePath;
  @Schema(description = "主键")
  private Long knowledgeId;
  @Schema(description = "知识库所有者")
  private Long ownerId;
  @Schema(description = "文档库文档名称")
  private String documentName;
  @Schema(description = "文档库id")
  private String libraryId;
  @Schema(description = "内容来源：ONLINE-在线文档，UPLOAD-上传文件")
  private String contentSource;
  @Schema(description = "文档库文档状态：00A有效，00R已归档")
  private String documentStatus;
  @Schema(description = "文档库文档编辑权限：T为具有，其他则为没有")
  private String documentEdit;
  @Schema(description = "文档库文档下载权限：T为具有，其他则为没有")
  private String documentDownload;
  @Schema(description = "是否是我的文档库文档：T为是")
  private String myDoc;
  /**
   * 是否是DocChain知识库类型
   */
  @JsonIgnore
  public boolean isDocChainType() {
    return StringUtils.isEmpty(knowledgeType) || KnowledgeConsts.KNOWLEDGE_TYPE_DOC_CHAIN.equals(knowledgeType);
  }
}
