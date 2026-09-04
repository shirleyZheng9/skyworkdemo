package com.iwhalecloud.bote.dto.orchestration.step.database;

import com.iwhalecloud.bote.common.consts.StepType;
import lombok.Getter;
import lombok.Setter;

/**
 * 查询单条记录步骤
 *
 * @author bianjp
 * @since 2025-11-25
 */
@Getter
@Setter
public class QuerySingleRecordStep extends AbstractDatabaseStep {

  public QuerySingleRecordStep() {
    super(StepType.QUERY_SINGLE_RECORD);
  }
}
