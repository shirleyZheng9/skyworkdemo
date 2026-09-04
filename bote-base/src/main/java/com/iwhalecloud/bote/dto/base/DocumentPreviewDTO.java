package com.iwhalecloud.bote.dto.base;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档预览DTO
 *
 * @author qian.sisheng
 * @since 2025-11-26
 */
@Getter
@Setter
@ToString
public class DocumentPreviewDTO {
  /** 文档ID */
  private String documentId;
  /** 文档名称 */
  private String documentName;
  /** 文件信息ID */
  private Long fileInfoId;
  /** 租户ID */
  private Long tenantId;
  /** 用户名称 */
  private String userName;
  /** 文档版本 */
  private Long revision;
  /** 文件D */
  private Long fileId;
}
