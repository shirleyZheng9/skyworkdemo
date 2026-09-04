package com.iwhalecloud.bote.dto.orchestration.step.database;

import com.iwhalecloud.bote.common.consts.StepType;
import lombok.Getter;
import lombok.Setter;

/**
 * 删除记录步骤
 *
 * @author bianjp
 * @since 2025-11-25
 */
@Getter
@Setter
public class DeleteRecordsStep extends AbstractDatabaseStep {
  public DeleteRecordsStep() {
    super(StepType.DELETE_RECORDS);
  }
}
