package com.iwhalecloud.bote.doc.module.base.dto;

import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 文件上传请求入参
 *
 * @author yangran
 * @since 2025-08-29
 */
@Getter
@Setter
@Schema(description = "文件上传请求入参")
public class UnifiedUploadRequest extends TenantBaseRO {

  @Schema(description = "文档库ID")
  private String libraryId;

  @Schema(description = "父文件夹ID")
  private String parentId;

  @Schema(description = "是否转换为在线文档，默认false")
  private String convertToOnline = DocBaseConsts.FALSE;

  @Schema(description = "是否上传为在线表格，默认false")
  private String uploadAsOnlineWorkbook = DocBaseConsts.FALSE;

  @Schema(description = "业务类型")
  private String busiType;

  @Schema(description = "是否启用分片上传，默认false")
  private Boolean enableChunk = false;

  @Schema(description = "文件哈希值")
  private String fileHash;

  @Schema(description = "文件名")
  private String fileName;

  @Schema(description = "原始文件名")
  private String originalFileName;

  @Schema(description = "当前分片索引")
  private Integer chunkIndex;

  @Schema(description = "总分片数")
  private Integer totalChunks;

  @Schema(description = "原始文件大小（字节）")
  private Long originalFileSize;

  @Schema(description = "是否文件夹文件上传：T-是，F-否")
  private String isFolderUpload = "F";

  @Schema(description = "文件夹上传任务ID（文件夹上传时必填）")
  private String folderUploadTaskId;

  @Schema(description = "文件的相对路径（文件夹上传时必填）")
  private String relativePath;

  @Schema(description = "文档ID，覆盖上传使用")
  private String documentId;
}
