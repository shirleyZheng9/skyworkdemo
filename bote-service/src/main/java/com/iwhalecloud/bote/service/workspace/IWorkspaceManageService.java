package com.iwhalecloud.bote.service.workspace;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.workspace.SimpleWorkspaceDTO;
import com.iwhalecloud.bote.dto.workspace.SpaceIdMappingDTO;
import com.iwhalecloud.bote.dto.workspace.WorkspaceDTO;
import com.iwhalecloud.bote.dto.workspace.query.WorkspaceQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

import java.util.List;

/**
 * 工作空间服务
 *
 * @author chen.linfa
 * @since 2025-10-16
 */
public interface IWorkspaceManageService {
  /**
   * 根据 ID 查询工作空间详情
   *
   * @param spaceId 空间 ID
   * @return 工作空间
   */
  WorkspaceDTO getWorkspace(Long spaceId);

  /**
   * 保存工作空间
   *
   * @param workspace 工作空间
   * @return 工作空间 ID
   */
  ResultVO<Long> saveWorkspace(WorkspaceDTO workspace);

  /**
   * 删除工作空间
   *
   * @param spaceId 工作空间 ID
   */
  ResultVO<Void> deleteWorkspace(Long spaceId);

  /**
   * 分页查询工作空间
   *
   * @param queryParams 查询条件
   * @return 工作空间分页数据
   */
  PageInfo<WorkspaceDTO> queryWorkspacePage(WorkspaceQueryParams queryParams);

  /**
   * 查询工作空间图标
   */
  String getWorkspaceIcon(Long spaceId);

  /**
   * 调用外部环境查询工作空间列表（分页）
   *
   * @param gatewayId 网关 ID
   * @param queryParams 查询条件
   * @param tenantId 租户 ID
   * @return 工作空间分页数据
   */
  ResultVO<PageInfo<WorkspaceDTO>> queryWorkspacePageFromExternal(Long gatewayId, WorkspaceQueryParams queryParams, Long tenantId);

  /**
   * 查询所有工作空间列表(用于选择工作空间场景)
   *
   * @return 工作空间列表
   */
  List<SimpleWorkspaceDTO> querySimpleWorkspaceList();

  /**
   * 查询空间ID映射信息
   * <p>spaceId 和 extSpaceId 至少传一个，传 spaceId 则查询 extSpaceId，传 extSpaceId 则查询 spaceId</p>
   *
   * @param spaceId 空间ID
   * @param extSpaceId 外系统空间ID
   * @return 空间ID映射信息，不存在返回 null
   */
  SpaceIdMappingDTO querySpaceId(Long spaceId, String extSpaceId);
}
