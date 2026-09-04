package com.iwhalecloud.bote.mapper.portal;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.portal.SimpleTenantDTO;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import com.iwhalecloud.bote.dto.portal.TenantUserDTO;
import com.iwhalecloud.bote.dto.portal.query.TenantQueryParams;
import com.iwhalecloud.bote.dto.portal.query.UserQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;

/**
 * 租户管理
 *
 * @author auto
 * @since 2024-09-13
 */
public interface TenantManageMapper {
  /**
   * 校验租户的编码唯一性
   *
   * @param tenant 租户
   * @return 结果
   */
  boolean existsTenantCode(@Param("dto") TenantDTO tenant);

  /**
   * 根据编码和名称获取租户
   *
   * @param code 租户编码
   * @param name 租户名称
   * @return 租户
   */
  @Nullable
  TenantDTO getTenantByCodeAndName(@Param("code") String code, @Param("name") String name);

  /**
   * 按外系统项目 ID(ext_tenant_id)+ 空间查租户,用于自动建租户判重。
   * 含软删(00X),优先返回 00A。
   */
  @Nullable
  TenantDTO getTenantByExtTenantIdAndSpace(@Param("extTenantId") Long extTenantId,
                                           @Param("spaceId") @Nullable Long spaceId);

  /** 复活软删租户并改名:status_cd → 00A,tenant_name 更新。 */
  int reactivateTenant(@Param("tenantId") Long tenantId,
                       @Param("tenantName") String tenantName,
                       @Param("updatorId") Long updatorId);

  /** 仅更新租户名称(命中 00A 且名称变化时)。专用方法,绕开 DataDifference flag。 */
  int updateTenantName(@Param("tenantId") Long tenantId,
                       @Param("tenantName") String tenantName,
                       @Param("updatorId") Long updatorId);

  /**
   * 根据主键获取租户
   *
   * @param tenantId 租户主键
   * @return 租户
   */
  TenantDTO getTenant(@Param("id") Long tenantId);

  /**
   * 查询租户图标
   */
  String getTenantIcon(@Param("tenantId") Long tenantId);

  /**
   * 新增租户
   *
   * @param tenant 租户
   * @return 结果
   */
  int insertTenant(@Param("dto") TenantDTO tenant);

  /**
   * 修改租户
   *
   * @param tenant 租户
   * @return 结果
   */
  int updateTenant(@Param("dto") TenantDTO tenant);

  /**
   * 删除租户
   *
   * @param tenantId 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteTenant(@Param("tenantId") Long tenantId, @Param("updatorId") Long updatorId);

  /**
   * 获取租户列表（分页）
   *
   * @param queryParams 查询条件
   * @return 租户分页列表
   */
  Page<TenantDTO> selectTenantPage(@Param("query") TenantQueryParams queryParams, RowBounds rowBounds);

  /**
   * 获取租户列表
   *
   * @param queryParams 查询条件
   * @return 租户分页列表
   */
  List<TenantDTO> selectTenantList(@Param("query") TenantQueryParams queryParams);

  /**
   * 根据主键获取租户成员
   *
   * @param tenantUserId 租户成员主键
   * @return 租户成员
   */
  TenantUserDTO getTenantUser(@Param("id") Long tenantUserId);

  /**
   * 根据租户ID和用户ID取租户成员
   *
   * @param tenantId 租户ID
   * @param userId 用户ID
   * @return 租户成员
   */
  TenantUserDTO getTenantUserByTenantIdAndUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

  /**
   * 新增租户成员；同一租户下同一有效成员已存在时跳过插入。
   *
   * @param tenantUser 租户成员
   * @return 插入行数，已存在时返回 0
   */
  int insertTenantUser(@Param("dto") TenantUserDTO tenantUser);

  /**
   * 批量新增租户成员
   *
   * @param list 租户成员列表
   * @return 结果
   */
  int batchInsertTenantUser(@Param("list") List<TenantUserDTO> list);

  /**
   * 修改租户成员
   *
   * @param tenantUser 租户成员
   * @return 结果
   */
  int updateTenantUser(@Param("dto") TenantUserDTO tenantUser);

  /**
   * 删除租户成员
   *
   * @param tenantId 租户 ID
   * @param userIds 用户 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteTenantUser(@Param("tenantId") Long tenantId, @Param("userIds") List<Long> userIds, @Param("updatorId") Long updatorId);

  /**
   * 删除租户成员
   *
   * @param userId 用户 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteTenantUserByUserId(@Param("userId") Long userId, @Param("updatorId") Long updatorId);

  /**
   * 获取租户成员列表（分页）
   *
   * @param queryParams 查询条件
   * @return 租户成员分页列表
   */
  Page<TenantUserDTO> selectTenantUserPage(@Param("query") TenantQueryParams queryParams, RowBounds rowBounds);

  /**
   * 获取租户成员列表
   *
   * @param tenantIds 租户ID列表
   * @return 租户成员分页列表
   */
  List<TenantUserDTO> selectTenantUserList(@Param("tenantIds") List<Long> tenantIds);

  /**
   * 根据用户 ID 查询租户成员列表
   *
   * @param userIds 用户 ID列表
   * @return 租户成员列表
   */
  List<TenantUserDTO> selectTenantUserListByUserIds(@Param("userIds") List<Long> userIds);

  /**
   * 查询用户加入的租户列表
   */
  List<SimpleTenantDTO> selectSimpleTenantsByUserId(@Param("spaceId") @Nullable Long spaceId, @Param("userId") Long userId);

  /**
   * 查询所有租户的简单信息
   */
  List<SimpleTenantDTO> selectSimpleTenants(@Param("spaceId") @Nullable Long spaceId);

  /**
   * 查询用户加入的租户（分页）
   *
   * @param queryParams 查询条件
   * @return 租户列表
   */
  Page<TenantDTO> selectTenantPageByUserId(@Param("query") UserQueryParams queryParams, RowBounds rowBounds);

  /**
   * 用户是否已存在当前租户
   *
   * @param tenantId 租户ID
   * @param userId 用户ID
   * @return 结果
   */
  boolean existsTenantUser(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

  /**
   * 检查用户是否有关联的租户
   */
  boolean existsTenantByUserId(@Param("userId") Long userId);

  /**
   * 获取用户在当前租户下的角色
   *
   * @param tenantId 租户 ID
   * @param userId 用户 ID
   * @return 用户角色
   */
  String getUserRole(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

  /**
   * 根据企业空间ID查询租户ID列表
   *
   * @param spaceId 企业空间ID
   * @return 空间下的租户ID列表吗
   */
  List<Long> getTenantIdsBySpaceId(@Param("spaceId") Long spaceId);

  /**
   * 查询租户ID对应的企业空间ID
   *
   * @param tenantId 企业空间ID
   * @return 空间下的租户ID列表吗
   */
  Long getSpaceIdByTenantId(@Param("tenantId") Long tenantId);

  /**
   * 查询空间的项目列表
   * @param spaceId
   * @return
   */
  List<SimpleTenantDTO> queryTenantList(@Param("spaceId") Long spaceId);

  /**
   * 获取用户在当前空间租户下的角色
   *
   * @param spaceId 空间 ID
   * @param tenantId 租户 ID，非必填
   * @param userId 用户 ID
   * @return 用户角色
   */
  List<String> queryUserRole(@Param("spaceId") Long spaceId, @Param("tenantId") @Nullable Long tenantId, @Param("userId") Long userId);
}
