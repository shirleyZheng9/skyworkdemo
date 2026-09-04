package com.iwhalecloud.bote.doc.module.base.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件分片信息 DTO
 * 用于存储文件分片的详细信息，包括文件ID、路径、大小等
 *
 * @author yangran
 * @since 2025-08-25
 */
@Getter
@Setter
@ToString
@Schema(description = "分片信息")
public class FileChunkInfo {

  /**
   * 文件ID（文件服务器返回的ID）
   */
  @Schema(description = "文件ID")
  private Long fileId;

  /**
   * 文件路径（文件服务器中的路径）
   */
  @Schema(description = "文件路径")
  private String filePath;

  /**
   * 分片路径（原始分片路径）
   */
  @Schema(description = "分片路径")
  private String chunkPath;

  /**
   * 分片大小（字节）
   */
  @Schema(description = "分片大小")
  private Long chunkSize;

  /**
   * 上传时间
   */
  @Schema(description = "上传时间")
  private Date uploadTime = new Date();

  /**
   * 分片索引
   */
  @Schema(description = "分片索引")
  private Integer chunkIndex;

  /**
   * 文件哈希值
   */
  @Schema(description = "文件哈希值")
  private String fileHash;

  /**
   * 原始文件名
   */
  @Schema(description = "原始文件名")
  private String originalFileName;

  /**
   * 分片批次号
   */
  @Schema(description = "分片批次号")
  private String chunkBatchId;

}
