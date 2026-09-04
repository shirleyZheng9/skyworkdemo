package com.iwhalecloud.bote.dto.beyond;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应文件对象
 *
 * @author bianjp
 * @since 2025-07-17
 */
@Getter
@Setter
@ToString
public class BeyondFileDTO {
  /** 文件 ID */
  private Long fileId;
  /** 文件名称 */
  private String fileName;
  /** 文件下载地址(完整地址) */
  private String fileUrl;
  /** 文件大小 */
  private Long fileSize;
  /** 文件类型 */
  private String fileType;
}
