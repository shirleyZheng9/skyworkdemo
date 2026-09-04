package com.iwhalecloud.bote.mapper.bot;

import com.iwhalecloud.bote.dto.bot.ClawEnvVariableDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * claw 环境变量关联管理
 *
 * @author chen.linfa
 * @since 2026-04-23
 */
public interface ClawEnvVariableManageMapper {

  int batchInsertClawEnvVariable(@Param("list") List<ClawEnvVariableDTO> list);

  int updateClawEnvVariable(@Param("dto") ClawEnvVariableDTO dto);

  List<ClawEnvVariableDTO> selectClawEnvVariableList(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId);
}
