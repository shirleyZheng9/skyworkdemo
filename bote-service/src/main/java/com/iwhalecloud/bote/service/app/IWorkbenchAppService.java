package com.iwhalecloud.bote.service.app;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.app.WorkbenchAppDTO;
import com.iwhalecloud.bote.dto.app.WorkbenchAppRelDTO;
import com.iwhalecloud.bote.dto.app.query.WorkbenchAppQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * 工作台应用服务
 *
 * @author tingyun.wang
 * @since 2025-09-08
 */
public interface IWorkbenchAppService {

  /**
   * 保存工作台应用
   *
   * @param appDTO 工作台应用DTO
   * @return 结果
   */
  ResultVO<WorkbenchAppDTO> saveWorkbenchApp(WorkbenchAppDTO appDTO);

  /**
   * 查询工作台应用能力配置
   *
   * @param workbenchAppId 工作台应用ID
   * @param spaceId 企业空间ID
   * @return 应用能力配置
   */
  WorkbenchAppDTO queryAppSettings(Long workbenchAppId, Long spaceId);

  /**
   * 保存工作台应用能力配置
   *
   * @param appDTO 工作台应用DTO
   */
  void saveAppSettings(WorkbenchAppDTO appDTO);

  /**
   * 查询工作台应用授权
   *
   * @param workbenchAppId 工作台应用ID
   * @param spaceId 企业空间ID
   * @return 应用能力配置
   */
  WorkbenchAppDTO queryAuthInfo(Long workbenchAppId, Long spaceId);

  /**
   * 保存工作台应用授权
   *
   * @param appDTO 工作台应用DTO
   */
  void saveAuthInfo(WorkbenchAppDTO appDTO);

  /**
   * 查询工作台应用列表（分页）
   *
   * @param queryParams 查询条件
   * @return 工作台应用分页列表
   */
  PageInfo<WorkbenchAppDTO> queryWorkbenchAppPage(WorkbenchAppQueryParams queryParams);

  /**
   * 获取工作台应用
   *
   * @param workbenchAppId 应用ID
   * @param spaceId 企业空间ID
   * @return 工作台应用详情
   */
  WorkbenchAppDTO getWorkbenchApp(Long workbenchAppId, Long spaceId);

  /**
   * 删除工作台应用
   *
   * @param workbenchAppId 应用ID
   * @param spaceId 企业空间ID
   * @return 结果
   */
  ResultVO<Void> deleteWorkbenchApp(Long workbenchAppId, Long spaceId);

  /**
   * 启用工作台应用
   *
   * @param workbenchAppId 应用ID
   * @param spaceId 企业空间ID
   */
  void enabledWorkbenchApp(Long workbenchAppId, Long spaceId);

  /**
   * 停用工作台应用
   *
   * @param workbenchAppId 应用ID
   * @param spaceId 企业空间ID
   */
  void disabledWorkbenchApp(Long workbenchAppId, Long spaceId);

  /**
   * 获取工作台应用图标
   *
   * @param workbenchAppId 应用ID
   * @param spaceId 企业空间ID
   * @return 工作台应用图标
   */
  String getWorkbenchAppIcon(Long workbenchAppId, Long spaceId);

  /**
   * 查询用户授权的工作台应用列表（分页）
   *
   * @param queryParams 查询条件
   * @return 工作台应用分页列表
   */
  PageInfo<WorkbenchAppDTO> queryAuthWorkbenchAppPage(WorkbenchAppQueryParams queryParams);

  /**
   * 获取授权工作台应用详情
   *
   * @param workbenchAppId 应用ID
   * @param spaceId 企业空间ID
   * @return 工作台应用详情
   */
  WorkbenchAppDTO getAuthWorkbenchAppDetail(Long workbenchAppId, Long spaceId);

  /**
   * 查询工作台应用关联的应用信息
   *
   * @param workbenchAppId 工作台应用ID
   * @param spaceId 企业空间ID
   * @return 关联的应用信息
   */
  WorkbenchAppDTO queryWorkbenchAppRelInfo(Long workbenchAppId, Long spaceId);

  /**
   * 检查应用是否已发布到运行态
   *
   * @param tenantId 租户ID
   * @param botId 应用ID
   * @return 发布关联信息，如果未发布则返回null
   */
  WorkbenchAppRelDTO checkAppPublishedStatus(Long tenantId, Long botId);

  /**
   * 解除发布应用
   *
   * @param tenantId 租户ID
   * @param botId 应用ID
   */
  void unpublishApp(Long tenantId, Long botId);

}
