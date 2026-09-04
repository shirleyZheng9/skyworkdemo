package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 同步文件信息请求
 *
 * @author auto
 * @since 2025-01-XX
 */
@Getter
@Setter
@ToString
@Schema(description = "同步文件信息请求")
public class SyncFileInfoRequest {

  @Schema(description = "文件ID")
  private Long fileId;

  @Schema(description = "存储类型")
  private String storeType;

  @Schema(description = "服务器文件路径")
  private String filePathInServer;

  @Schema(description = "文件名称")
  private String fileName;

  @Schema(description = "文件大小")
  private String fileSize;

  @Schema(description = "租户ID")
  private Long tenantId;

  @Schema(description = "业务类型（写入 bt_file_info.busi_type，可选）")
  private String busiType;
}

