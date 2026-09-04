package com.iwhalecloud.bote.doc.module.base.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 分片上传基础请求 DTO
 * 继承基础上传请求，增加分片相关的公共属性
 *
 * @author yangran
 * @since 2025-08-29
 */
@Getter
@Setter
@Schema(description = "分片上传基础请求")
public class BaseChunkRequest extends BaseUploadRequest {

  @Schema(description = "文件哈希值")
  private String fileHash;

  @Schema(description = "文件名")
  private String fileName;

  @Schema(description = "分片索引")
  private Integer chunkIndex;

  @Schema(description = "总分片数")
  private Integer totalChunks;

  @Schema(description = "是否启用分片上传")
  private String enableChunk;
}
