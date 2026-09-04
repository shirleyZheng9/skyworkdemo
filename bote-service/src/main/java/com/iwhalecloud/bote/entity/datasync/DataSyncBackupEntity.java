package com.iwhalecloud.bote.entity.datasync;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 数据同步备份 Entity
 *
 * @author qian.sisheng
 * @since 2026-04-17
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_data_sync_backup")
public class DataSyncBackupEntity extends BaseEntity {
  @DiffId
  @Schema(description = "备份记录 ID")
  private Long backupId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
  @DiffField(name = "BACKUP_TYPE")
  @Schema(description = "备份类型")
  private String backupType;
  @DiffField(name = "BACKUP_FILE_ID")
  @Schema(description = "备份文件 ID")
  private Long backupFileId;
  @DiffField(name = "RECORD_ID")
  @Schema(description = "发布记录 ID")
  private Long recordId;
  @DiffField(name = "BACKUP_NAME")
  @Schema(description = "备份名称")
  private String backupName;
  @DiffField(name = "BACKUP_STATUS")
  @Schema(description = "备份状态：0未开始、1运行中、10成功、-1失败")
  private Integer backupStatus;
}
