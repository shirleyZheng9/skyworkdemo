package com.iwhalecloud.bote.doc.module.base.dto;

import com.iwhalecloud.bote.doc.module.document.entity.DcDocumentEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件夹上传缓存实体 用于分布式缓存，替代内存缓存
 *
 * @author yangran
 * @since 2025-08-29
 */
@Getter
@Setter
@ToString
@Schema(description = "文件夹上传缓存实体")
public class FolderUploadCacheDTO {

  @Schema(description = "任务ID")
  private String taskId;

  @Schema(description = "文档库ID")
  private String libraryId;

  @Schema(description = "父文件夹ID")
  private String parentId;

  @Schema(description = "业务类型")
  private String busiType;

  @Schema(description = "是否转换为在线文档")
  private String convertToOnline;

  @Schema(description = "上传任务ID（兼容旧版本）")
  private String uploadTaskId;

  @Schema(description = "文件夹名称")
  private String folderName;

  @Schema(description = "总文件数量")
  private Integer totalFiles;

  @Schema(description = "已上传文件数量")
  private Integer uploadedFiles = 0;

  @Schema(description = "上传进度百分比")
  private Integer progress = 0;

  @Schema(description = "上传状态：UPLOADING-上传中，COMPLETED-完成，FAILED-失败，PAUSED-暂停")
  private String status = "UPLOADING";

  @Schema(description = "已上传文件信息列表")
  private List<UploadedFileInfo> uploadedFileInfos = new ArrayList<>();

  @Schema(description = "已创建的文档列表")
  private List<DcDocumentEntity> createdDocuments = new ArrayList<>();

  @Schema(description = "失败的文件列表")
  private List<String> failedFiles = new ArrayList<>();

  @Schema(description = "开始时间")
  private Long startTime;

  @Schema(description = "结束时间")
  private Long endTime;

  @Schema(description = "预计剩余时间（秒）")
  private Long estimatedTimeRemaining;

  @Schema(description = "当前上传速度（字节/秒）")
  private Long uploadSpeed = 0L;

  @Schema(description = "创建时间")
  private Date createTime = new Date();

  @Schema(description = "最后更新时间")
  private Date updateTime = new Date();

  /**
   * 添加已创建的文档
   *
   * @param document 文档实体
   */
  public void addCreatedDocument(DcDocumentEntity document) {
    if (document != null) {
      this.createdDocuments.add(document);
      updateTime = new Date();
    }
  }

  /**
   * 添加失败的文件
   *
   * @param fileName 失败的文件名
   */
  public void addFailedFile(String fileName) {
    if (fileName != null && !fileName.isEmpty()) {
      this.failedFiles.add(fileName);
      updateTime = new Date();
    }
  }

  /**
   * 更新上传进度
   *
   * @param uploadedFiles 已上传文件数
   * @param uploadSpeed 上传速度
   * @param estimatedTimeRemaining 预计剩余时间
   */
  public void updateProgress(int uploadedFiles, Long uploadSpeed, Long estimatedTimeRemaining) {
    this.uploadedFiles = uploadedFiles;
    this.progress = calculateProgress(uploadedFiles, this.totalFiles);
    this.uploadSpeed = uploadSpeed;
    this.estimatedTimeRemaining = estimatedTimeRemaining;
    this.updateTime = new Date();

    // 如果上传完成，更新状态
    if (uploadedFiles >= this.totalFiles) {
      this.status = "COMPLETED";
      this.progress = 100;
      this.endTime = System.currentTimeMillis();
    }
  }

  /**
   * 暂停上传
   */
  public void pauseUpload() {
    this.status = "PAUSED";
    this.updateTime = new Date();
  }

  /**
   * 恢复上传
   */
  public void resumeUpload() {
    this.status = "UPLOADING";
    this.updateTime = new Date();
  }

  /**
   * 取消上传
   */
  public void cancelUpload() {
    this.status = "CANCELLED";
    this.endTime = System.currentTimeMillis();
    this.updateTime = new Date();
  }

  /**
   * 标记上传失败
   *
   * @param failedFiles 失败的文件列表
   */
  public void markFailed(List<String> failedFiles) {
    this.status = "FAILED";
    this.failedFiles = failedFiles != null ? failedFiles : new ArrayList<>();
    this.endTime = System.currentTimeMillis();
    this.updateTime = new Date();
  }


  /**
   * 计算上传进度百分比
   *
   * @param uploaded 已上传数量
   * @param total 总数量
   * @return 进度百分比
   */
  private int calculateProgress(int uploaded, int total) {
    if (total == 0) {
      return 0;
    }
    return Math.min(100, (int) Math.round((double) uploaded / total * 100));
  }

  /**
   * 已上传文件信息
   */
  @Getter
  @Setter
  @ToString
  public static class UploadedFileInfo {
    @Schema(description = "文件哈希值")
    private String fileHash;

    @Schema(description = "文件信息ID")
    private Long fileInfoId;

    @Schema(description = "文件的相对路径")
    private String relativePath;

    @Schema(description = "原始文件名")
    private String originalFileName;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "业务类型")
    private String busiType;

    @Schema(description = "是否转换为在线文档")
    private String convertToOnline;

    @Schema(description = "上传时间")
    private Date uploadTime;

  }

}
