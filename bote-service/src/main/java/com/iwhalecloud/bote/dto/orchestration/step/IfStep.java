package com.iwhalecloud.bote.dto.orchestration.step;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.dto.orchestration.IfCondition;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 如果步骤
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
public class IfStep extends AbstractStep {
  /** 分支列表 */
  private List<BranchSpec> branches;

  public IfStep() {
    super(StepType.IF);
  }

  /**
   * 分支配置
   */
  @Getter
  @Setter
  @ToString
  @JsonInclude(Include.NON_NULL)
  public static class BranchSpec {
    /** 分支编码 */
    private String branchCode;
    /** 分支名称 */
    private String branchName;
    /** 条件 */
    private IfCondition condition;
    /** 下一步的步骤编码 */
    private String next;
  }
}
