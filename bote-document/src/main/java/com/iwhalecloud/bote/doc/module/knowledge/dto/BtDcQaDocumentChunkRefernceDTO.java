package com.iwhalecloud.bote.doc.module.knowledge.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档 DTO
 *
 * @author auto
 * @since 2024-09-20
 */
@Getter
@Setter
@ToString(callSuper = true)
public class BtDcQaDocumentChunkRefernceDTO  {
  @Schema(description = "文档名字")
  private String docName;
  @Schema(description = "引用片段数量")
  private Long chunkCount;
  @Schema(description = "资源路径，仅文档有值")
  private String resourcePath;
  @Schema(description = "文档id")
  private Long documentId;
  @Schema(description = "bt_dc_document.document_id文档唯一编码")
  private String dcDocumentId;
  @Schema(description = "所属文档库唯一编码")
  private String libraryId;
  @Schema(description = "知识库文档ID")
  private Long docId;
  @Schema(description = "知识库名称")
  private String knowledgeName;
  @Schema(description = "知识库ID")
  private Long kbId;
  @Schema(description = "Dochain 知识库是否已存在")
  private String isExist;
  @Schema(description = "内容来源：ONLINE-在线文档，UPLOAD-上传文件")
  private String contentSource;
  @Schema(description = "引用片段")
  private List<BtDcQaChunkReferenceDTO> chunkReferences;


}
