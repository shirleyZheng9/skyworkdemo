package com.iwhalecloud.bote.doc.module.base.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件分片上传缓存实体
 * 用于替代Map<String, Object>，提供更安全的类型支持
 *
 * @author yangran
 * @since 2025-08-25
 */
@Getter
@Setter
@ToString
@Schema(description = "文件分片上传缓存实体")
public class FileChunkUploadCacheDTO {

  /**
   * 分片信息映射：key为分片索引，value为分片详细信息
   */
  @Schema(description = "分片信息映射")
  private Map<String, FileChunkInfo> chunks = new HashMap<>();

  /**
   * 是否已完成上传
   */
  @Schema(description = "是否已完成上传")
  private Boolean completed = false;

  /**
   * 文件URL（上传完成后）
   */
  @Schema(description = "文件URL")
  private String fileUrl;

  /**
   * 创建时间
   */
  @Schema(description = "创建时间")
  private Date createTime = new Date();

  /**
   * 最后更新时间
   */
  @Schema(description = "最后更新时间")
  private Date updateTime = new Date();

  /**
   * 添加分片信息
   *
   * @param chunkIndex 分片索引
   * @param fileChunkInfo 分片详细信息
   */
  public void addChunk(Integer chunkIndex, FileChunkInfo fileChunkInfo) {
    if (chunkIndex != null && fileChunkInfo != null) {
      chunks.put(chunkIndex.toString(), fileChunkInfo);
      updateTime = new Date();
    }
  }

  /**
   * 获取分片信息
   *
   * @param chunkIndex 分片索引
   * @return 分片详细信息
   */
  public FileChunkInfo getChunk(Integer chunkIndex) {
    if (chunkIndex == null) {
      return null;
    }
    return chunks.get(chunkIndex.toString());
  }

  /**
   * 检查分片是否存在
   *
   * @param chunkIndex 分片索引
   * @return 是否存在
   */
  public boolean hasChunk(Integer chunkIndex) {
    if (chunkIndex == null) {
      return false;
    }
    return chunks.containsKey(chunkIndex.toString());
  }

  /**
   * 获取已上传的分片索引列表
   *
   * @return 已上传的分片索引列表
   */
  public List<Integer> getUploadedChunkIndexes() {
    return chunks.keySet().stream()
      .map(Integer::valueOf)
      .sorted()
      .collect(Collectors.toList());
  }

  /**
   * 获取分片数量
   *
   * @return 分片数量
   */
  public int getChunkCount() {
    return chunks.size();
  }

  /**
   * 获取所有分片路径
   *
   * @return 分片路径列表
   */
  public List<String> getAllChunkPaths() {
    return chunks.values().stream()
      .map(FileChunkInfo::getFilePath)
      .filter(path -> path != null && !path.isEmpty())
      .collect(Collectors.toList());
  }

  /**
   * 获取所有分片详细信息
   *
   * @return 分片详细信息列表
   */
  public List<FileChunkInfo> getAllChunkDetails() {
    return chunks.values().stream()
      .sorted((c1, c2) -> {
        if (c1.getChunkIndex() == null || c2.getChunkIndex() == null) {
          return 0;
        }
        return c1.getChunkIndex().compareTo(c2.getChunkIndex());
      })
      .collect(Collectors.toList());
  }

  /**
   * 标记完成状态
   *
   * @param fileUrl 文件URL
   */
  public void markCompleted(String fileUrl) {
    this.completed = true;
    this.fileUrl = fileUrl;
    this.updateTime = new Date();
  }

}
