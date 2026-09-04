package com.iwhalecloud.bote.dto.orchestration.step.database;

import com.iwhalecloud.bote.common.consts.StepType;
import lombok.Getter;
import lombok.Setter;

/**
 * 查询多条记录步骤
 *
 * @author bianjp
 * @since 2025-11-25
 */
@Getter
@Setter
public class QueryRecordsStep extends AbstractDatabaseStep {
  /** 最大查询数量 */
  private Integer limit;

  public QueryRecordsStep() {
    super(StepType.QUERY_RECORDS);
  }
}
