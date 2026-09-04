package com.iwhalecloud.bote.mapper.skill;

import com.iwhalecloud.bote.dto.skill.AgentSkillDirDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * Agent Skill 目录 Mapper
 *
 * @author qian.sisheng
 * @since 2026-04-28
 */
public interface AgentSkillDirMapper {
  /**
   * 插入数据
   *
   * @param dto 数据传输对象
   * @return 影响行数
   */
  int insertAgentSkillDir(@Param("dto") AgentSkillDirDTO dto);

  /**
   * 更新数据
   *
   * @param dto 数据传输对象
   * @return 影响行数
   */
  int updateAgentSkillDir(@Param("dto") AgentSkillDirDTO dto);

  /**
   * 根据目录ID查询数据
   *
   * @param tenantId 租户ID
   * @param dirId 目录ID
   * @return 数据传输对象
   */
  AgentSkillDirDTO selectByDirId(@Param("tenantId") Long tenantId, @Param("dirId") Long dirId);

  /**
   * 根据技能ID检测是否存在目录数据
   *
   * @param tenantId 租户ID
   * @param skillId 技能ID
   * @return 活跃目录数量
   */
  int existDir(@Param("tenantId") Long tenantId, @Param("skillId") Long skillId);

  /**
   * 根据技能ID和目录名统计活跃目录数量
   *
   * @param tenantId 租户ID
   * @param skillId 技能ID
   * @param dirName 目录名
   * @param excludeDirId 排除的目录ID
   * @return 活跃目录数量
   */
  int existDirName(@Param("tenantId") Long tenantId, @Param("skillId") Long skillId, @Param("dirName") String dirName,
    @Param("excludeDirId") Long excludeDirId);

  /**
   * 根据技能ID查询活跃目录列表
   *
   * @param tenantId 租户ID
   * @param skillId 技能ID
   * @return 数据传输对象列表
   */
  List<AgentSkillDirDTO> listActiveBySkillId(@Param("tenantId") Long tenantId, @Param("skillId") Long skillId);

  /**
   * 查询技能根目录（parent_dir_id = -1）
   *
   * @param tenantId 租户ID
   * @param skillId 技能ID
   * @return 根目录
   */
  AgentSkillDirDTO selectRootDirBySkillId(@Param("tenantId") Long tenantId, @Param("skillId") Long skillId);

  /**
   * 根据目录ID删除数据
   *
   * @param tenantId 租户ID
   * @param dirId 目录ID
   * @param updatorId 更新者ID
   * @return 影响行数
   */
  int deleteByDirId(@Param("tenantId") Long tenantId, @Param("dirId") Long dirId, @Param("updatorId") Long updatorId);

  /**
   * 根据技能ID删除数据
   *
   * @param tenantId 租户ID
   * @param skillId 技能ID
   * @param updatorId 更新者ID
   * @return 影响行数
   */
  int deleteAllBySkillId(@Param("tenantId") Long tenantId, @Param("skillId") Long skillId, @Param("updatorId") Long updatorId);
}

