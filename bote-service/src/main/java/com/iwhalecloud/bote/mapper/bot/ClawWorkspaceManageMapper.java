package com.iwhalecloud.bote.mapper.bot;

import com.iwhalecloud.bote.dto.bot.ClawWorkspaceDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * claw 工作区管理
 *
 * @author chen.linfa
 * @since 2026-04-23
 */
public interface ClawWorkspaceManageMapper {

  int insertClawWorkspace(@Param("dto") ClawWorkspaceDTO dto);

  int batchInsertClawWorkspace(@Param("list") List<ClawWorkspaceDTO> list);

  int updateClawWorkspace(@Param("dto") ClawWorkspaceDTO dto);

  List<ClawWorkspaceDTO> selectClawWorkspaceList(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId,
    @Param("fileNames") List<String> files);
}
