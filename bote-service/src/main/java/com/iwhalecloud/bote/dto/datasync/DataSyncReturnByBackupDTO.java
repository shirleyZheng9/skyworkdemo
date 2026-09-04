package com.iwhalecloud.bote.dto.datasync;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 按备份回退入参
 */
@Getter
@Setter
@ToString
@Schema(description = "按备份回退请求")
public class DataSyncReturnByBackupDTO {
  @Schema(description = "备份记录 ID")
  private Long backupId;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "回退说明")
  private String remark;
}
