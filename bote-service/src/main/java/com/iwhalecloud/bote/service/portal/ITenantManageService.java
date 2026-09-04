package com.iwhalecloud.bote.service.portal;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.portal.SimpleTenantDTO;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import com.iwhalecloud.bote.dto.portal.TenantUserDTO;
import com.iwhalecloud.bote.dto.portal.query.TenantQueryParams;
import com.iwhalecloud.bote.dto.workspace.SimpleWorkspaceDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 租户配置服务
 *
 * @author auto
 * @since 2024-09-13
 */
public interface ITenantManageService {

  /**
   * 查询租户图标
   */
  String getTenantIcon(Long tenantId);

  /**
   * 获取租户
   *
   * @param tenantId 租户 ID
   * @return 结果
   */
  @Nullable
  TenantDTO getTenant(Long tenantId);

  /**
   * 查询租户列表（分页）
   *
   * @param queryParams 查询条件
   * @return 租户列表
   */
  PageInfo<TenantDTO> queryTenantPage(TenantQueryParams queryParams);

  /**
   * 查询租户列表
   *
   * @param queryParams 查询条件
   * @return 租户列表
   */
  List<TenantDTO> queryTenantList(TenantQueryParams queryParams);

  /**
   * 保存租户
   *
   * @param tenant 租户
   * @return 结果
   */
  ResultVO<TenantDTO> saveTenant(TenantDTO tenant);

  /**
   * 保存租户设置
   *
   * @param tenant 租户
   * @return 结果
   */
  ResultVO<TenantDTO> saveTenantDetail(TenantDTO tenant);

  /**
   * 删除租户
   *
   * @param tenantId 租户 ID
   * @return 结果
   */
  ResultVO<Void> deteleTenant(Long tenantId);

  /**
   * 查询租户成员列表（分页）
   *
   * @param queryParams 查询条件
   * @return 租户成员列表（分页）
   */
  PageInfo<TenantUserDTO> queryTenantUserPage(TenantQueryParams queryParams);

  /**
   * 保存租户成员
   *
   * @param tenantUserList 租户成员列表
   * @return 结果
   */
  ResultVO<List<TenantUserDTO>> saveTenantUser(List<TenantUserDTO> tenantUserList);

  /**
   * 添加租户成员
   *
   * @param tenantId 租户 ID
   * @param userId 用户 ID
   * @param userRole 角色
   * @return 结果
   */
  ResultVO<TenantUserDTO> addTenantUser(Long tenantId, Long userId, String userRole);

  /**
   * 移除租户成员
   *
   * @param tenantId 租户 ID
   * @param userIds 用户 ID
   * @return 结果
   */
  ResultVO<Void> deleteTenantUser(Long tenantId, List<Long> userIds);

  /**
   * 保存租户成员（通过userCode，用于Beyond系统）
   *
   * @param tenantUserList 租户成员列表（包含userCode）
   * @return 结果
   */
  ResultVO<List<TenantUserDTO>> saveTenantUserForBeyond(List<TenantUserDTO> tenantUserList);

  /**
   * 移除租户成员（通过userCode，用于Beyond系统）
   *
   * @param tenantId 租户 ID
   * @param userCodes 用户编码列表
   * @return 结果
   */
  ResultVO<Void> removeTenantUserForBeyond(Long tenantId, List<String> userCodes);

  /**
   * 查询企业授权信息
   *
   * @param userId 用户 ID
   * @return 企业授权信息
   */
  List<SimpleWorkspaceDTO> qryAuthorizedWorkspaces(Long userId);

  /**
   * 查询用户在空间或租户下的角色
   */
  String getUserRole(Long spaceId, @Nullable Long tenantId, Long userId);

  /**
   * 查询企业空间下的租户列表
   *
   * @param spaceId 企业空间ID
   * @return 空间下的租户列表
   */
  List<SimpleTenantDTO> querySpaceTenantList(Long spaceId);

  /**
   * 根据空间 ID 获取当前用户可访问的一个租户 ID
   *
   * @param spaceId 空间 ID
   * @param userId 用户 ID
   * @return 第一个租户 ID
   */
  @Nullable
  Long getFirstTenantId(Long spaceId, Long userId);

  /**
   * 保存租户（用于外部平台，通过spaceCode获取spaceId）
   *
   * @param tenant 租户（包含spaceCode字段）
   * @return 结果
   */
  ResultVO<TenantDTO> saveTenantForExternal(TenantDTO tenant);
}
