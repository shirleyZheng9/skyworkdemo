package com.iwhalecloud.bote.dto.datasync;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 创建备份入参
 */
@Getter
@Setter
@ToString
@Schema(description = "创建数据备份请求")
public class DataSyncBackupCreateDTO {
  @Schema(description = "租户 ID")
  private Long tenantId;

  @Schema(description = "备份描述")
  private String remark;
}
