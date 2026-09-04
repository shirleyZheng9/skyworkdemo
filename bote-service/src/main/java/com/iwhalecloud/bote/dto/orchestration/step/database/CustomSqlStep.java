package com.iwhalecloud.bote.dto.orchestration.step.database;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 自定义 SQL 步骤
 *
 * @author chen.linfa
 * @since 2025-11-28
 */
@Getter
@Setter
public class CustomSqlStep extends AbstractStep {
  /** 数据源 ID */
  private Long dataSourceId;
  /** 表 ID */
  private List<Long> tableIds;
  /** 数据库操作权限 */
  private List<String> allowed;
  /** SQL */
  private String sql;

  public CustomSqlStep() {
    super(StepType.DATA_TABLE);
  }
}
