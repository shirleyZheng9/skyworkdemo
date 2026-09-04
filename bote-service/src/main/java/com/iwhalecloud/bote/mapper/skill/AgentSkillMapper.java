package com.iwhalecloud.bote.mapper.skill;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.bot.SquareBotSkillRowDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillDTO;
import com.iwhalecloud.bote.dto.skill.SimpleAgentSkillDTO;
import com.iwhalecloud.bote.dto.skill.query.AgentSkillQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * Agent Skill 相关数据库操作
 *
 * @author bianjp
 * @since 2026-02-03
 */
public interface AgentSkillMapper {
  /**
   * 插入 Agent Skill
   */
  int insertAgentSkill(@Param("dto") AgentSkillDTO dto);

  /**
   * 更新 Agent Skill
   */
  void updateAgentSkill(@Param("dto") AgentSkillDTO dto);

  /**
   * 删除 Agent Skill
   */
  int deleteAgentSkill(@Param("tenantId") Long tenantId, @Param("skillId") Long skillId, @Param("updatorId") Long updatorId);

  /**
   * 校验技能编码是否已存在
   *
   * @param tenantId 租户 ID
   * @param skillName 技能编码
   * @param dataFrom 数据来源（当为 "10A" 时，按创建人维度校验唯一性）
   * @param creatorId 创建人（登录用户 ID）
   */
  boolean existsSkillName(@Param("tenantId") Long tenantId,
                          @Param("botId") Long botId,
                          @Param("skillName") String skillName,
                          @Param("dataFrom") String dataFrom,
                          @Param("creatorId") Long creatorId);

  /**
   * 根据 ID 查询 Agent Skill
   */
  AgentSkillDTO selectAgentSkillById(@Param("tenantId") Long tenantId, @Param("skillId") Long skillId);

  /**
   * 根据 ID 查询 Agent Skill 简单信息
   */
  SimpleAgentSkillDTO selectSimpleAgentSkillById(@Param("tenantId") Long tenantId, @Param("skillId") Long skillId);

  /**
   * 批量查询 Agent Skill 简单信息
   */
  List<SimpleAgentSkillDTO> selectSimpleAgentSkillsByIds(@Param("tenantId") Long tenantId, @Param("skillIds") List<Long> skillIds);

  /**
   * 分页查询 Agent Skill
   */
  Page<AgentSkillDTO> selectAgentSkillPage(@Param("query") AgentSkillQueryParams queryParams, RowBounds rowBounds);

  /**
   * 按租户、用户、广场技能ID查询是否已安装
   */
  AgentSkillDTO selectByTenantBotSkillSquareId(@Param("tenantId") Long tenantId,
                                               @Param("skillSquareId") Long skillSquareId,
                                               @Param("creatorId") Long creatorId,
                                               @Param("installSource") String installSource);

  /**
   * 批量查询：指定租户下各 bot 对已上架广场技能的安装版本（仅 data_from=10A 有效安装）
   */
  List<SquareBotSkillRowDTO> selectInstalledSkillVersionForSquareBots(@Param("tenantId") Long tenantId,
                                                                      @Param("skillSquareId") Long skillSquareId,
                                                                      @Param("botIds") List<Long> botIds);

  /**
   * 查询指定租户、Bot 下已安装的技能广场记录 ID 列表（去重，仅 data_from=10A 的有效安装）
   */
  List<Long> selectInstalledSkillSquareIdsByBot(@Param("tenantId") Long tenantId, @Param("botId") Long botId);

  /**
   * 查询当前空间下用户创建的技能列表
   *
   * @param params 查询参数
   * @return 技能列表
   */
  Page<AgentSkillDTO> selectUserSpaceCreatedSkillPage(@Param("params") AgentSkillQueryParams params, RowBounds rowBounds);

  /**
   * 统计用当前空间下用户创建的技能数量
   *
   * @param spaceId 空间 ID
   * @param tenantId 租户 ID
   * @param userId 用户 ID
   * @return 数量
   */
  int countUserSpaceCreatedSkills(@Param("spaceId") Long spaceId, @Param("tenantId") Long tenantId, @Param("userId") Long userId);

  /**
   * 查询当前空间下用户已安装的技能列表
   *
   * @param params 查询参数
   * @return 技能列表
   */
  Page<AgentSkillDTO> selectUserSpaceInstalledSkillPage(@Param("params") AgentSkillQueryParams params, RowBounds rowBounds);

  /**
   * 统计当前空间下用户已安装的技能数量
   *
   * @param spaceId 空间 ID
   * @param tenantId 租户 ID
   * @param userId 用户 ID
   * @return 数量
   */
  int countUserSpaceInstalledSkills(@Param("spaceId") Long spaceId, @Param("tenantId") Long tenantId, @Param("userId") Long userId);

  /**
   * 统计平台预置的技能数量
   *
   * @return 数量
   */
  int countPlatformSkills();

  /**
   * 更新技能的发布 zip 文件信息
   */
  int updateAgentSkillFileInfo(@Param("tenantId") Long tenantId, @Param("skillId") Long skillId,
    @Param("fileInfoId") Long fileInfoId, @Param("updatorId") Long updatorId);

  /**
   * 回填技能文件名
   */
  int updateAgentSkillFileName(@Param("tenantId") Long tenantId, @Param("skillId") Long skillId,
    @Param("skillFileName") String skillFileName, @Param("updatorId") Long updatorId);

  /**
   * 更新技能的扩展 JSON 信息
   */
  int updateAgentSkillExtJson(@Param("tenantId") Long tenantId, @Param("skillId") Long skillId, @Param("skillExtJson") String skillExtJson,
    @Param("updatorId") Long updatorId);
}
