package com.iwhalecloud.bote.dto.orchestration.context;

import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 并行分支上下文
 *
 * @author bianjp
 * @since 2024-11-06
 */
@Getter
@Setter
@ToString
public class ParallelBranchContext {
  /** 分支编码 */
  private final String branch;
  /** 步骤执行日志列表 */
  private final List<OrchestrationStepRunLog> stepLogs;

  /**
   * 构造并行分支上下文
   *
   * @param branch 分支编码
   * @param enableStepLog 是否启用步骤执行日志
   */
  public ParallelBranchContext(String branch, boolean enableStepLog) {
    this.branch = branch;
    this.stepLogs = enableStepLog ? new ArrayList<>() : null;
  }
}
