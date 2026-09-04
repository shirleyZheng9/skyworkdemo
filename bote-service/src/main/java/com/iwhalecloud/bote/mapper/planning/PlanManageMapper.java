package com.iwhalecloud.bote.mapper.planning;

import com.iwhalecloud.bote.dto.planning.PlanRecordDTO;
import com.iwhalecloud.bote.dto.planning.PlanStepDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 计划管理
 *
 * @author auto
 * @since 2025-05-17
 */
public interface PlanManageMapper {

  /**
   * 根据主键获取计划
   *
   * @param tenantId 租户 ID
   * @param planId 主键
   * @return 计划
   */
  PlanRecordDTO getPlanRecord(@Param("tenantId") Long tenantId, @Param("planId") Long planId);

  /**
   * 新增计划
   *
   * @param planRecord 计划
   * @return 结果
   */
  int insertPlanRecord(@Param("dto") PlanRecordDTO planRecord);

  /**
   * 修改计划
   *
   * @param planRecord 计划
   * @return 结果
   */
  int updatePlanRecord(@Param("dto") PlanRecordDTO planRecord);

  /**
   * 删除计划
   *
   * @param tenantId 租户 ID
   * @param planId 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deletePlanRecord(@Param("tenantId") Long tenantId, @Param("planId") Long planId, @Param("updatorId") Long updatorId);

  /**
   * 根据主键获取计划步骤
   *
   * @param tenantId 租户 ID
   * @param stepId 计划步骤主键
   * @return 计划步骤
   */
  PlanStepDTO getPlanStep(@Param("tenantId") Long tenantId, @Param("stepId") Long stepId);

  /**
   * 批量新增计划步骤
   *
   * @param planSteps 计划步骤列表
   * @return 结果
   */
  int batchInsertPlanStep(@Param("list") List<PlanStepDTO> planSteps);

  /**
   * 修改计划步骤
   *
   * @param planStep 计划步骤
   * @return 结果
   */
  int updatePlanStep(@Param("dto") PlanStepDTO planStep);

  /**
   * 更新步骤执行结果
   *
   * @param step 步骤
   * @return 结果
   */
  int updateStepStatus(@Param("dto") PlanStepDTO step);

  /**
   * 删除属性
   *
   * @param stepId 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deletePlanStep(@Param("tenantId") Long tenantId, @Param("stepId") Long stepId, @Param("updatorId") Long updatorId);

  /**
   * 获取计划步骤列表
   *
   * @param tenantId 租户 ID
   * @param planId 计划 ID
   * @return 计划步骤列表
   */
  List<PlanStepDTO> selectPlanStepList(@Param("tenantId") Long tenantId, @Param("planId") Long planId);

  /**
   * 根据上下文 ID 获取计划步骤
   *
   * @param tenantId 租户 ID
   * @param planId 计划 ID
   * @param contextId 上下文 ID
   * @return 计划步骤
   */
  PlanStepDTO getPlanStepByContextId(@Param("tenantId") Long tenantId, @Param("planId") Long planId, @Param("contextId") String contextId);
}
