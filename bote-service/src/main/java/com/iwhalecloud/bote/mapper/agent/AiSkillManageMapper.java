package com.iwhalecloud.bote.mapper.agent;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.agent.AiSkillDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import com.iwhalecloud.bote.dto.skill.AgentSkillDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * 新增表记录启用技能管理
 *
 * @author linmengfan
 * @since 2026-03-05
 */
public interface AiSkillManageMapper {

  AiSkillDTO getAiSkill(@Param("spaceId") Long spaceId, @Param("botId") Long botId, @Param("skillId") Long skillId, @Param("userId") Long userId);

  int insertAiSkill(@Param("dto") AiSkillDTO skill);

  int updateAiSkill(@Param("dto") AiSkillDTO skill);

  int deleteAiSkill(@Param("id") Long id, @Param("updatorId") Long updatorId);

  Page<AgentSkillDTO> selectAiAgentSkillPage(@Param("query") AiQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询技能列表
   *
   * @param queryParams 查询参数
   * @return 技能列表
   */
  Page<AgentSkillDTO> selectAiAgentSkillList(@Param("query") AiQueryParams queryParams);

  /**
   * 根据技能 ID 查询技能信息列表
   *
   * @param spaceId 空间 ID
   * @param skillId 技能 ID
   * @return 技能列表
   */
  List<AiSkillDTO> selectListBySkillId(@Param("spaceId") Long spaceId, @Param("skillId") Long skillId);

  /**
   * 根据应用 ID 查询技能信息列表
   *
   * @param spaceId 空间 ID
   * @param botId 应用 ID
   * @return 技能列表
   */
  List<AiSkillDTO> selectListByBotId(@Param("spaceId") Long spaceId, @Param("botId") Long botId);

  /**
   * 批量插入技能应用关联记录
   *
   * @param list 技能应用关联列表
   * @return 插入的记录数
   */
  int batchInsertAiSkill(@Param("list") List<AiSkillDTO> list);

  /**
   * 批量更新技能应用关联记录状态
   *
   * @param ids 需要更新的记录 ID 列表
   * @param updatorId 更新人 ID
   * @return 更新的记录数
   */
  int batchUpdateAiSkillStatus(@Param("ids") List<Long> ids, @Param("updatorId") Long updatorId);

  /**
   * 判断是否存在已启用的技能
   *
   * @param spaceId 空间 ID
   * @param skillId 技能 ID
   * @param userId 用户 ID
   * @return 是否存在
   */
  boolean existAiSkill(@Param("spaceId") Long spaceId, @Param("skillId") Long skillId, @Param("userId") Long userId);
}
