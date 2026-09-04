package com.iwhalecloud.bote.mapper.skill;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.skill.SkillInstallLogVO;
import com.iwhalecloud.bote.entity.skill.AgentSkillInstallLogEntity;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 技能安装记录 Mapper
 *
 * @author skill-square
 * @since 2026-03-18
 */
public interface AgentSkillInstallLogMapper {

  int insert(AgentSkillInstallLogEntity entity);

  int countByTenantBotSkill(@Param("tenantId") Long tenantId,
                            @Param("botId") Long botId,
                            @Param("skillId") Long skillId);

  Page<SkillInstallLogVO> selectInstallLogPage(@Param("skillId") Long skillId,
                                               @Param("keyword") String keyword,
                                               @Param("tenantId") Long tenantId,
                                               @Param("installSource") String installSource,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               RowBounds rowBounds);
}
