package com.iwhalecloud.bote.service.agent;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.agent.AiSkillDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import com.iwhalecloud.bote.dto.skill.AgentSkillDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

import java.util.List;

/**
 * 新增表记录启用技能管理服务
 *
 * @author linmengfan
 * @since 2026-03-05
 */
public interface IAiSkillManageService {

  /**
   * 启用技能
   * @param skill 技能信息
   * @return 返回技能信息
   */
  ResultVO<AiSkillDTO> saveAiSkill(AiSkillDTO skill);

  /**
   *  查询通用智能体的技能列表
   * @param params 查询技能列表
   * @return 返回技能列表
   */
  PageInfo<AgentSkillDTO> queryAiAgentSkillPage(AiQueryParams params);

  /**
   *  查询通用智能体的技能列表
   * @param params 查询技能列表
   * @return 返回技能列表
   */
  List<AgentSkillDTO> queryAiAgentSkillList(AiQueryParams params);

  /**
   * 通用智能体的技能新增方法
   * @param skill 技能的新增或者修改入参
   * @return 返回技能的实体数据
   */
  ResultVO<AgentSkillDTO> saveAiAgentSkill(AgentSkillDTO skill);

  /**
   * 删除技能
   * @param spaceId 空间id
   * @param skillId 技能id
   * @return 返回值
   */
  ResultVO<Boolean> deleteAgentSkill(Long spaceId, Long skillId);

  /**
   * 禁用通用技能
   * @param skill 启用的技能数据
   * @return 返回技能数据
   */
  ResultVO<AiSkillDTO> disabledBtAiSkill(AiSkillDTO skill);

  /**
   * 查询技能所安装的应用列表
   *
   * @param spaceId 空间id
   * @param skillId 技能id
   * @return 返回应用信息列表
   */
  List<AiSkillDTO> queryBotListBySkillId(Long spaceId, Long skillId);

  /**
   * 批量保存 AI 技能
   *
   * @param skill 技能信息
   * @return 返回新增和移除的应用 ID 列表
   */
  ResultVO<List<Long>> batchSaveAiAgentSkill(AiSkillDTO skill);

  /**
   * 查询技能所安装的应用列表
   *
   * @param spaceId 空间id
   * @param botId 应用id
   * @return 返回应用信息列表
   */
  List<AiSkillDTO> queryListByBotId(Long spaceId, Long botId);
}
