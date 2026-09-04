package com.iwhalecloud.bote.doc.module.base.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 合并分片请求 DTO
 * 继承分片基础请求，增加合并分片特有的属性
 *
 * @author yangran
 * @since 2025-08-25
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "合并分片请求")
public class MergeChunkRequest extends BaseChunkRequest {

  @NotNull(message = "原文件大小不能为空")
  @Schema(description = "原文件大小")
  private Long fileSize;

  @Schema(description = "文件的相对路径（相对于文件夹根目录）")
  private String relativePath;

  @Schema(description = "是否文件夹文件上传：T-是，F-否")
  private String isFolderUpload = "F";

  @Schema(description = "文件夹上传任务ID（文件夹上传时必填）")
  private String folderUploadTaskId;

  @Schema(description = "文档ID，覆盖上传使用")
  private String documentId;
}
