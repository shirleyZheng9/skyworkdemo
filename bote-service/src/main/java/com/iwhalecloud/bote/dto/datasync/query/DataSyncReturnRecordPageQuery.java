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
@Schema(description = "回退记录分页查询条件")
public class DataSyncReturnRecordPageQuery extends PagingQueryParams {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "操作人名称，支持模糊查询")
  private String operatorName;
  @Schema(description = "关键字，匹配文件名或回退描述")
  private String searchContent;
  @Schema(description = "开始时间")
  private Date startTime;
  @Schema(description = "结束时间")
  private Date endTime;
}
