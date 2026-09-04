package com.iwhalecloud.bote.mapper.skill;

import com.iwhalecloud.bote.dto.skill.AgentSkillFileDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * Agent Skill 文件 Mapper
 *
 * @author qian.sisheng
 * @since 2026-04-28
 */
public interface AgentSkillFileMapper {

  /**
   * 根据DTO条件查询列表
   * @param dto Agent Skill 文件DTO
   */
  int insertAgentSkillFile(@Param("dto") AgentSkillFileDTO dto);

  /**
   * 根据DTO条件更新
   * @param dto Agent Skill 文件DTO
   */
  int updateAgentSkillFile(@Param("dto") AgentSkillFileDTO dto);

  /**
   * 根据技能文件ID查询
   * @param tenantId 租户ID
   * @param skillFileId 技能文件ID
   * @return Agent Skill 文件DTO
   */
  AgentSkillFileDTO selectBySkillFileId(@Param("tenantId") Long tenantId, @Param("skillFileId") Long skillFileId);

  /**
   * 根据目录ID查询技能文件列表
   * @param tenantId 租户ID
   * @param dirId 目录ID
   * @return 技能文件列表
   */
  List<AgentSkillFileDTO> selectActiveByDirId(@Param("tenantId") Long tenantId, @Param("dirId") Long dirId);

  /**
   * 查询目录下文件元数据
   */
  List<AgentSkillFileDTO> selectActiveMetaByDirId(@Param("tenantId") Long tenantId, @Param("dirId") Long dirId);

  /**
   * 根据技能ID查询技能文件列表
   * @param tenantId 租户ID
   * @param skillId 技能ID
   * @return 技能文件列表
   */
  List<AgentSkillFileDTO> selectActiveBySkillId(@Param("tenantId") Long tenantId, @Param("skillId") Long skillId);

  /**
   * 查询技能根目录下的 SKILL.md 文件
   *
   * @param tenantId 租户ID
   * @param skillId 技能ID
   * @return SKILL.md 文件
   */
  AgentSkillFileDTO selectSkillMdBySkillId(@Param("tenantId") Long tenantId, @Param("skillId") Long skillId);

  /**
   * 根据目录ID和文件名统计技能文件数量（排除指定技能文件）
   *
   * @param tenantId 租户ID
   * @param dirId 目录ID
   * @param fileName 文件名
   * @param excludeSkillFileId 排除的技能文件ID
   * @return 技能文件数量
   */
  int existByFileNameInDir(@Param("tenantId") Long tenantId, @Param("dirId") Long dirId, @Param("fileName") String fileName,
    @Param("excludeSkillFileId") Long excludeSkillFileId);

  /**
   * 根据技能文件ID进行删除
   *
   * @param tenantId 租户ID
   * @param skillFileId 技能文件ID
   * @param updatorId 更新者ID
   * @return 影响行数
   */
  int deleteBySkillFileId(@Param("tenantId") Long tenantId, @Param("skillFileId") Long skillFileId, @Param("updatorId") Long updatorId);

  /**
   * 根据目录ID进行删除
   *
   * @param tenantId 租户ID
   * @param dirId 目录ID
   * @param updatorId 更新者ID
   * @return 影响行数
   */
  int deleteAllByDirId(@Param("tenantId") Long tenantId, @Param("dirId") Long dirId, @Param("updatorId") Long updatorId);

  /**
   * 根据技能ID进行删除
   *
   * @param tenantId 租户ID
   * @param skillId 技能ID
   * @param updatorId 更新者ID
   * @return 影响行数
   */
  int deleteAllBySkillId(@Param("tenantId") Long tenantId, @Param("skillId") Long skillId, @Param("updatorId") Long updatorId);
}

