package com.iwhalecloud.bote.dto.base;

import com.iwhalecloud.bote.entity.base.FileInfoEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件信息 DTO
 *
 * @author auto
 * @since 2024-09-24
 */
@Getter
@Setter
@ToString(callSuper = true)
public class FileInfoDTO extends FileInfoEntity {
  @Schema(description = "文件大小")
  private Long fileSize;
  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "文件类型")
  private String fileType;
  @Schema(description = "存储类型")
  private String storeType;
  @Schema(description = "存储路径")
  private String filePathInServer;
  @Schema(description = "原始文件名称")
  private String originalFileName;
}
