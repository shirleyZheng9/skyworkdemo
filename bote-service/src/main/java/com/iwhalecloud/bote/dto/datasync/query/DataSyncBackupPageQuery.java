package com.iwhalecloud.bote.dto.datasync.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "备份记录分页查询条件")
public class DataSyncBackupPageQuery extends PagingQueryParams {
  @Schema(description = "租户 ID", example = "10001")
  private Long tenantId;
  @Schema(description = "备份类型", example = "manual")
  private String backupType;
  @Schema(description = "操作人名称，支持模糊查询", example = "admin")
  private String operatorName;
  @Schema(description = "关键字，匹配文件名或备注", example = "发版前")
  private String searchContent;
  @Schema(description = "开始时间")
  private Date startTime;
  @Schema(description = "结束时间")
  private Date endTime;
}
