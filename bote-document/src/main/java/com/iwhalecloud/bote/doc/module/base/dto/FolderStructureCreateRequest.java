package com.iwhalecloud.bote.doc.module.base.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件夹结构创建请求 DTO
 * 继承基础上传请求，用于在所有文件上传完成后，创建完整的文件夹结构和文档关系
 *
 * @author yangran
 * @since 2025-08-29
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "文件夹结构创建请求")
public class FolderStructureCreateRequest extends BaseUploadRequest {

  @NotBlank(message = "文件夹名称不能为空")
  @Schema(description = "文件夹名称")
  private String folderName;

  @Schema(description = "文件夹上传任务ID（可选，如果提供则从缓存中获取文件信息）")
  private String folderUploadTaskId;

  @Schema(description = "上传成功的文件列表（与folderUploadTaskId二选一）")
  private List<UploadedFileInfo> uploadedFiles;


  /**
   * 上传成功的文件信息
   */
  @Getter
  @Setter
  @ToString
  public static class UploadedFileInfo {
    @NotBlank(message = "文件哈希不能为空")
    @Schema(description = "文件哈希值")
    private String fileHash;

    @NotBlank(message = "相对路径不能为空")
    @Schema(description = "文件的相对路径")
    private String relativePath;

    @NotBlank(message = "原始文件名不能为空")
    @Schema(description = "原始文件名")
    private String originalFileName;

    @NotNull(message = "文件大小不能为空")
    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @NotBlank(message = "业务类型不能为空")
    @Schema(description = "业务类型")
    private String busiType;

    @Schema(description = "是否转换为在线文档：T-是，F-否")
    private String convertToOnline;
  }
}
