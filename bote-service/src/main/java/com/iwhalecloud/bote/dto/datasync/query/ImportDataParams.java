package com.iwhalecloud.bote.dto.datasync.query;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 导入数据入参
 *
 * @author chen.linfa
 * @since 2024-10-21
 */
@Getter
@Setter
@ToString
public class ImportDataParams {
  /** 数据包 ID  */
  private Long fileId;
  /** 数据包名称 */
  private String fileName;
  /** 租户 ID */
  private Long tenantId;
  /** 工作空间 ID */
  private Long spaceId;
  /** 是否自动确认导入，默认为 false */
  private Boolean autoConfirm;
}
