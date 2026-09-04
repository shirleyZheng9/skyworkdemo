package com.iwhalecloud.bote.dto.orchestration.step.database;

import com.iwhalecloud.bote.common.consts.StepType;
import lombok.Getter;
import lombok.Setter;

/**
 * 添加记录步骤
 *
 * @author bianjp
 * @since 2025-11-25
 */
@Getter
@Setter
public class InsertRecordStep extends AbstractDatabaseStep {

  public InsertRecordStep() {
    super(StepType.INSERT_RECORD);
  }
}
