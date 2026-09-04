package com.iwhalecloud.bote.dto.base;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件预览DTO
 *
 * @author qian.sisheng
 * @since 2025-11-26
 */
@Getter
@Setter
@ToString
public class FilePreviewRequestParams {
  /** 文件信息ID */
  private Long fileInfoId;
  /** 租户ID */
  private Long tenantId;
  /** 文件URL */
  private String fileUrl;
}
