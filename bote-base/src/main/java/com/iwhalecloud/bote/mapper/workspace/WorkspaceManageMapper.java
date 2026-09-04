package com.iwhalecloud.bote.mapper.workspace;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.workspace.SimpleWorkspaceDTO;
import com.iwhalecloud.bote.dto.workspace.WorkspaceDTO;
import com.iwhalecloud.bote.dto.workspace.query.WorkspaceQueryParams;

/**
 * 工作空间
 *
 * @author chen.linfa
 * @since 2025-10-16
 */
public interface WorkspaceManageMapper {

  /**
   * 根据 ID 查询工作空间
   */
  WorkspaceDTO selectWorkspaceById(@Param("spaceId") Long spaceId);

  /**
   * 新增工作空间；有效数据中已存在同名或同外系统空间 ID 时跳过插入。
   *
   * @return 插入行数，已存在时返回 0
   */
  int insertWorkspace(@Param("dto") WorkspaceDTO workspace);

  int batchInsertWorkspace(@Param("list") List<WorkspaceDTO> workspaces);

  /**
   * 更新工作空间
   */
  int updateWorkspace(@Param("dto") WorkspaceDTO workspace);

  /**
   * 删除工作空间
   */
  int deleteWorkspace(@Param("spaceId") Long spaceId, @Param("updatorId") Long updatorId);

  /**
   * 检查名称是否存在
   *
   * @param spaceId 空间 ID
   * @param spaceName 名称
   * @return 是否存在
   */
  boolean existsSpaceName(@Param("spaceId") Long spaceId, @Param("spaceName") String spaceName);

  /**
   * 分页查询工作空间
   */
  Page<WorkspaceDTO> selectWorkspacePage(@Param("query") WorkspaceQueryParams queryParams, RowBounds rowBounds);

  /**
   * 获取工作空间图标
   */
  String getWorkspcaeIcon(@Param("spaceId") Long spaceId);

  /**
   * 查询所有工作空间列表
   */
  List<SimpleWorkspaceDTO> selectSimpleWorkspaces();

  /**
   * 查询用户授权的工作空间列表
   */
  List<SimpleWorkspaceDTO> selectSimpleWorkspaceByUserId(@Param("userId") Long userId);

  /**
   * 根据空间 ID 查询虚拟的租户 ID
   */
  Long getVirtualTenantId(@Param("spaceId") Long spaceId);

  /**
   * 根据外系统SpaceId判断是否存在企业空间
   *
   * @param extSpaceId 外系统SpaceId
   * @return 是否存在企业空间
   */
  SimpleWorkspaceDTO existWorkSpaceByExtSpaceId(@Param("extSpaceId") String extSpaceId);

  /**
   * 根据有效空间名称查询工作空间（用于插入冲突后解析已有空间）
   */
  SimpleWorkspaceDTO existWorkspaceBySpaceName(@Param("spaceName") String spaceName);

  /**
   * 根据空间 ID 列表查询工作空间
   *
   * @param spaceIds 空间 ID 列表
   * @return 工作空间列表
   */
  List<SimpleWorkspaceDTO> selectWorkspacesByIds(@Param("spaceIds") List<Long> spaceIds);

  /**
   * 根据外系统空间ID查询空间ID
   *
   * @param extSpaceId 外系统空间ID
   * @return 空间ID，不存在返回 null
   */
  Long getSpaceIdByExtSpaceId(@Param("extSpaceId") String extSpaceId);

  /**
   * 根据空间ID查询外系统空间ID
   *
   * @param spaceId 空间ID
   * @return 外系统空间ID，不存在返回 null
   */
  String getExtSpaceIdBySpaceId(@Param("spaceId") Long spaceId);

}
