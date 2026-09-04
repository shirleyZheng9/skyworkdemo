package com.iwhalecloud.bote.dto.orchestration.step.database;

import com.iwhalecloud.bote.common.consts.StepType;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新记录步骤
 *
 * @author bianjp
 * @since 2025-11-25
 */
@Getter
@Setter
public class UpdateRecordsStep extends AbstractDatabaseStep {

  public UpdateRecordsStep() {
    super(StepType.UPDATE_RECORDS);
  }
}
