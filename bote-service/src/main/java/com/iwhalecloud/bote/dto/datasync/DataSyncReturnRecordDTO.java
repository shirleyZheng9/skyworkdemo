package com.iwhalecloud.bote.dto.datasync;

import com.iwhalecloud.bote.entity.datasync.DataSyncReturnRecordEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 回退分页列表 DTO
 */
@Getter
@Setter
@ToString
@Schema(description = "回退记录列表项")
public class DataSyncReturnRecordDTO extends DataSyncReturnRecordEntity {
  @Schema(description = "操作人名称")
  private String creatorName;
}
