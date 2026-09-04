package com.iwhalecloud.bote.entity.datasync;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 数据回退记录 Entity
 *
 * @author qian.sisheng
 * @since 2026-04-17
 */
@Getter
@Setter
@ToString
public class DataSyncReturnRecordEntity extends BaseEntity {

  @DiffId
  @Schema(description = "回退日志 ID")
  private Long returnId;
  @DiffField(name = "BACKUP_ID")
  @Schema(description = "备份 ID")
  private Long backupId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
  @DiffField(name = "RETURN_FILE_ID")
  @Schema(description = "回退版本文件 ID")
  private Long returnFileId;
  @DiffField(name = "RETURN_FILE_NAME")
  @Schema(description = "回退版本名称")
  private String returnName;
  @DiffField(name = "RECORD_ID")
  @Schema(description = "回退发布记录 ID")
  private Long recordId;
  @DiffField(name = "RETURN_STATUS")
  @Schema(description = "回退状态: -1-失败, 1-进行中, 10-成功")
  private Integer returnStatus;
}
