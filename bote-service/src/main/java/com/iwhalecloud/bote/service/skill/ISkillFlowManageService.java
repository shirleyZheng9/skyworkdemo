package com.iwhalecloud.bote.service.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.base.SimpleFlowStepDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSkillFlowDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bote.dto.skill.SkillFlowDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowNodeConfigResponseDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowParamDTO;
import com.iwhalecloud.bote.dto.skill.StandardServiceDiffViewDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 技能：流程 服务
 *
 * @author auto
 * @since 2024-09-18
 */
public interface ISkillFlowManageService {

  /**
   * 查询单个流程
   *
   * @param flowId 流程主键
   * @return 流程
   */
  SkillFlowDTO findSkillFlow(Long tenantId, Long flowId);

  /**
   * 保存流程基本信息
   *
   * @param flow 流程
   */
  ResultVO<SkillFlowDTO> saveSkillFlowBasicInfo(SkillFlowDTO flow);

  /**
   * 保存流程
   *
   * @param flow 流程
   * @return 结果
   */
  ResultVO<SkillFlowDTO> saveSkillFlow(SkillFlowDTO flow);

  /**
   * 删除流程
   *
   * @param tenantId 租户 ID
   * @param flowId 流程主键
   * @return 结果
   */
  ResultVO<Void> deleteSkillFlow(Long tenantId, Long flowId);

  /**
   * 查询流程列表
   *
   * @param queryParams 查询条件
   * @return 流程列表
   */
  List<SimpleSkillFlowDTO> querySkillFlowList(SkillQueryParams queryParams);

  /**
   * 查询流程列表（分页）
   *
   * @param queryParams 查询条件
   * @return 流程分页列表
   */
  PageInfo<SkillFlowDTO> querySkillFlowPage(SkillQueryParams queryParams);

  /**
   * 查询流程列表（分页）
   *
   * @param queryParams 查询条件
   * @return 流程分页列表
   */
  PageInfo<SimpleSkillFlowDTO> querySimpleSkillFlowPage(SkillQueryParams queryParams);

  /**
   * 查询流程参数
   */
  @Nullable
  SkillFlowParamDTO findSkillFlowParam(Long tenantId, Long flowId);

  /**
   * 查询流程历史版本详情
   *
   * @param logId 日志ID
   * @param tenantId 租户ID
   * @return 流程
   */
  ResultVO<StandardServiceDiffViewDTO> findSkillFlowVersion(Long logId, Long tenantId);

  /**
   * 比较两次修改记录差异
   *
   * @param tenantId 租户ID
   * @param oldOperLogId 旧版本日志ID
   * @param operLogId 新版本日志ID
   * @return 差异
   */
  ResultVO<StandardServiceDiffViewDTO> diffSkillFlowOperLog(Long tenantId, Long oldOperLogId, Long operLogId);

  /**
   * 自动生成流程步骤
   *
   * @param flow 智能体
   * @return 结果
   */
  ResultVO<List<SimpleFlowStepDTO>> generateFlowStep(SkillFlowDTO flow);

  /**
   * 获取流程节点配置
   *
   * @return 节点配置列表，按根节点 tip 中定义的顺序返回
   */
  List<SkillFlowNodeConfigResponseDTO> getFlowNodeConfigs();
}
