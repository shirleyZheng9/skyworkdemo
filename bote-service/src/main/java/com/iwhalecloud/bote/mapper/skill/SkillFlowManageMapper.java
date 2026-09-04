package com.iwhalecloud.bote.mapper.skill;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.skill.SkillFlowDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowParamDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowWithParamDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 技能：流程 Mapper
 *
 * @author auto
 * @since 2024-09-18
 */
public interface SkillFlowManageMapper {
  /**
   * 校验流程的编码唯一性
   */
  boolean existsSkillFlowCode(@Param("tenantId") Long tenantId, @Param("flowCode") String flowCode);

  /**
   * 根据主键获取流程
   *
   * @param flowId 流程主键
   * @return 流程
   */
  SkillFlowDTO getSkillFlow(@Param("tenantId") Long tenantId, @Param("flowId") Long flowId);

  /**
   * 新增流程
   *
   * @param flow 流程
   * @return 结果
   */
  int insertSkillFlow(@Param("dto") SkillFlowDTO flow);

  /**
   * 修改流程
   *
   * @param flow 流程
   * @return 结果
   */
  int updateSkillFlow(@Param("dto") SkillFlowDTO flow);

  /**
   * 修改流程基本信息
   *
   * @param flow 流程
   * @return 结果
   */
  int updateSkillFlowBasicInfo(@Param("dto") SkillFlowDTO flow);

  /**
   * 删除工作流
   */
  int deleteSkillFlow(@Param("tenantId") Long tenantId, @Param("flowId") Long flowId, @Param("updatorId") Long updatorId);

  /**
   * 获取流程列表（分页）
   *
   * @param queryParams 查询条件
   * @return 流程分页列表
   */
  Page<SkillFlowDTO> selectSkillFlowPage(@Param("query") SkillQueryParams queryParams, RowBounds rowBounds);

  /**
   * 新增流程参数
   *
   * @param flowParam 流程参数
   * @return 结果
   */
  int insertSkillFlowParam(@Param("dto") SkillFlowParamDTO flowParam);

  /**
   * 修改流程参数
   *
   * @param flowParam 流程参数
   * @return 结果
   */
  int updateSkillFlowParam(@Param("dto") SkillFlowParamDTO flowParam);

  /**
   * 删除流程参数
   */
  int deleteSkillFlowParam(@Param("tenantId") Long tenantId, @Param("flowId") Long flowId, @Param("updatorId") Long updatorId);

  /**
   * 获取流程参数
   *
   * @param flowId 流程 ID
   * @return 流程参数
   */
  SkillFlowParamDTO getSkillFlowParam(@Param("tenantId") Long tenantId, @Param("flowId") Long flowId);

  /**
   * 查询流程修改时间
   *
   * @param tenantId 租户 ID
   * @param flowId 流程 ID
   * @return 流程修改时间
   */
  Date selectUpdatedTime(@Param("tenantId") Long tenantId, @Param("flowId") Long flowId);

  /**
   * 批量查询工作流
   *
   * @param tenantId 租户 ID
   * @param flowIds 工作流 ID 列表
   * @return 工作流列表
   */
  List<SkillFlowWithParamDTO> getFlowsByIds(@Param("tenantId") Long tenantId, @Param("flowIds") List<Long> flowIds);
}
