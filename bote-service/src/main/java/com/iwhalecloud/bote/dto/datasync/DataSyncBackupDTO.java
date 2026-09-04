package com.iwhalecloud.bote.dto.datasync;

import com.iwhalecloud.bote.entity.datasync.DataSyncBackupEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 备份分页列表 DTO
 */
@Getter
@Setter
@ToString
@Schema(description = "备份记录列表项")
public class DataSyncBackupDTO extends DataSyncBackupEntity {
  @Schema(description = "创建人名称")
  private String creatorName;

}
