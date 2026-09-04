package com.iwhalecloud.bote.mapper.agent;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.agent.AiWorkspaceDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 用户级的提示词管理
 *
 * @author linmengfan
 * @since 2026-03-05
 */
public interface AiWorkspaceManageMapper {

  /**
   * 根据主键获取用户级的提示词
   */
  AiWorkspaceDTO getAiWorkspace(@Param("id") Long id);

  /**
   * 新增用户级的提示词
   */
  int insertAiWorkspace(@Param("dto") AiWorkspaceDTO dto);

  /**
   * 批量新增用户级的提示词
   */
  int batchInsertAiWorkspace(@Param("list") List<AiWorkspaceDTO> list);

  /**
   * 修改用户级的提示词
   */
  int updateAiWorkspace(@Param("dto") AiWorkspaceDTO dto);

  /**
   * 获取用户级的提示词列表
   */
  List<AiWorkspaceDTO> selectAiWorkspaceList(@Param("query") AiQueryParams queryParams);

  /**
   * 根据空间 ID、应用 ID、用户 ID 和文件名获取用户提示词
   */
  AiWorkspaceDTO selectFileContentByFileName(@Param("spaceId") Long spaceId, @Param("botId") Long botId, @Param("userId") Long userId, @Param("fileName") String fileName);

  /**
   * 根据空间 ID、应用 ID、用户 ID 和文件名获取用户提示词 ID
   */
  Long selectIdByFileName(@Param("spaceId") Long spaceId, @Param("botId") Long botId, @Param("userId") Long userId, @Param("fileName") String fileName);

  /**
   * 根据 ID 更新用户提示词内容
   */
  int updateFileContentById(@Param("id") Long id, @Param("fileContent") String fileContent);

  /**
   * 根据空间、应用、用户和记忆类型查询记忆文件列表
   *
   * @param spaceId   空间 ID
   * @param botId     应用 ID
   * @param userId    用户 ID（creator_id）
   * @param memoryType 记忆类型（LONG_TERM / DAILY）
   */
  List<AiWorkspaceDTO> selectMemoryFileList(@Param("spaceId") Long spaceId, @Param("tenantId") Long tenantId, @Param("botId") Long botId,
      @Param("userId") Long userId, @Param("memoryType") String memoryType);

  /**
   * 根据空间、应用、用户和文件名查询记忆文件（含 memory_type 过滤）
   *
   * @param spaceId    空间 ID
   * @param botId      应用 ID
   * @param userId     用户 ID（creator_id）
   * @param fileName   文件名（如 MEMORY.md、MEMORY-2026-04-18.md）
   */
  AiWorkspaceDTO selectMemoryFileByName(@Param("spaceId") Long spaceId, @Param("tenantId") Long tenantId, @Param("botId") Long botId,
                                        @Param("sceneId") Long sceneId, @Param("userId") Long userId, @Param("fileName") String fileName);

  /**
   * 分页查询应用的记忆文件列表
   */
  Page<AiWorkspaceDTO> selectMemoryAiWorkspacePage(@Param("query") AiQueryParams queryParams, RowBounds rowBounds);
}
