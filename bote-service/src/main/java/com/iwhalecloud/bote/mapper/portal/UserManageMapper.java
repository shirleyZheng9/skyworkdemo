package com.iwhalecloud.bote.mapper.portal;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.dto.portal.SimpleUserDTO;
import com.iwhalecloud.bote.dto.portal.TenantUserDTO;
import com.iwhalecloud.bote.dto.portal.UserDTO;
import com.iwhalecloud.bote.dto.portal.query.UserQueryParams;
import com.iwhalecloud.bote.entity.portal.UserEntity;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;

/**
 * 用户管理
 *
 * @author auto
 * @since 2024-09-13
 */
public interface UserManageMapper {
  /**
   * 校验用户名是否已存在
   */
  boolean existsUserName(@Param("systemCode") String systemCode, @Param("userName") String userName);

  /**
   * 根据主键获取用户
   *
   * @param userId 用户主键
   * @return 用户
   */
  UserDTO getUser(@Param("id") Long userId);

  /**
   * 根据系统编码 + 外系统用户 ID 查询用户
   */
  @Nullable
  UserEntity getUserBySystemCodeAndExtUserId(@Param("systemCode") String systemCode, @Param("extUserId") String extUserId);

  /**
   * 根据用户主键更新密码
   *
   * @param password 密码
   * @param userId 用户主键
   * @return 结果
   */
  int updateUserPassword(@Param("password") String password, @Param("userId") Long userId);

  /**
   * 新增用户；同系统下已存在相同登录名或（非空）外系统用户 ID 的有效用户时跳过插入。
   *
   * @param user 用户
   * @return 插入行数，已存在时返回 0
   */
  int insertUser(@Param("dto") UserEntity user);

  /**
   * 修改用户
   *
   * @param user 用户
   * @return 结果
   */
  int updateUser(@Param("dto") UserDTO user);

  /**
   * 修改用户基本信息
   */
  int updateUserBasicInfo(@Param("dto") UserEntity user);

  /**
   * 删除属性
   *
   * @param userId 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteUser(@Param("userId") Long userId, @Param("updatorId") Long updatorId);

  /**
   * 获取用户列表（分页）
   */
  Page<SimpleUserDTO> selectUserPage(@Param("query") UserQueryParams queryParams, @Param("tenantId") Long tenantId, RowBounds rowBounds);

  /**
   * 获取用户列表
   *
   * @param queryParams 查询条件
   * @return 用户列表
   */
  List<SimpleUserDTO> selectUserList(@Param("query") UserQueryParams queryParams);

  /**
   * 获取用户完整信息列表
   *
   * @param queryParams 查询条件
   * @return 用户完整信息列表
   */
  List<UserDTO> selectUserDTOList(@Param("query") UserQueryParams queryParams);

  /**
   * 根据用户编码查询用户信息
   *
   * @param userName 用户名
   * @return 结果
   */
  UserEntity getUserByCode(@Param("systemCode") String systemCode, @Param("userName") String userName);

  /**
   * 根据用户编码查询用户信息（只返回一条数据）
   *
   * @param systemCode 系统编码
   * @param userName 用户名
   * @return 用户信息，如果不存在返回null
   */
  UserEntity getOneUserByCode(@Param("systemCode") String systemCode, @Param("userName") String userName);

  /**
   * 根据用户ID更新默认租户ID
   *
   * @return 结果
   */
  int updateTenantIdByUserId(@Param("userId") Long userId, @Param("tenantId") Long tenantId, @Param("updatorId") Long updatorId);

  /**
   * 根据用户ID查询用户信息
   *
   * @param defaultTenantId 默认租户ID
   * @param userIds 用户ID列表
   * @return 结果
   */
  List<SimpleUserDTO> selectUserListByUserIds(@Param("defaultTenantId") Long defaultTenantId, @Param("userIds") List<Long> userIds);

  /**
   * 根据用户ID列表查询用户信息
   *
   * @param userIds 用户ID列表
   * @return 用户信息列表
   */
  List<UserDTO> selectUserDTOListByUserIds(@Param("userIds") List<Long> userIds);

  /**
   * 批量更新用户默认租户ID
   *
   * @param userIds 用户ID列表
   * @return 结果
   */
  int deleteUserDefaultTenantId(@Param("userIds") List<Long> userIds, @Param("updatorId") Long updatorId);

  /**
   * 查询全部有效的用户数量
   *
   * @return 用户数量
   */
  long countAllUser();



  int batchInsertUsers(@Param("list") List<UserEntity> userList);

  int batchInsertTenantUsers(@Param("list") List<TenantUserDTO> tenantUserList);

  /**
   * 检查哪些用户名已经存在
   *
   * @param userNames 用户名集合
   * @return 已存在的用户名列表
   */
  List<String> checkUserNamesExist(@Param("userNames") Collection<String> userNames);

  /**
   * 统计需要检查过期的用户数
   */
  long countForUserExpCheck();

  /**
   * 查询需要检查过期的用户列表
   */
  Page<UserEntity> selectPageForUserExpCheck(@Param("lastUserId") Long lastUserId, RowBounds rowBounds);

  /**
   * 修改用户登录失败次数
   */
  int updateUserLoginFailCount(@Param("userId") Long userId, @Param("loginFailCount") Integer loginFailCount);

  /**
   * 修改用户锁定状态
   *
   * @param userId 用户ID
   * @param isLocked 锁定状态
   * @return 结果
   */
  int updateUserLockState(@Param("userId") Long userId, @Param("isLocked") String isLocked);

  /**
   * 修改用户状态：启用/禁用
   *
   * @param userId 用户ID
   * @param userState 用户状态
   * @return 结果
   */
  int updateUserState(@Param("userId") Long userId, @Param("userState") String userState);

  /**
   * 根据组织ID查询组织成员信息
   *
   * @param orgId 组织ID
   * @return 组织成员列表
   */
  List<PortalUserDTO> selectUserListByOrgId(@Param("orgId") Long orgId);

  /**
   * 查询用户头像
   *
   * @param userId 用户ID
   * @return 用户头像
   */
  String selectUserIcon(@Param("userId") Long userId);

  /**
   * 清除用户默认的租户
   */
  int deleteDefaultTenantId(@Param("tenantId") Long tenantId);

  /**
   * 分页获取用户基本信息列表
   */
  Page<SimpleUserDTO> pageUserBasicInfo(@Param("query") UserQueryParams queryParams, RowBounds rowBounds);

  /**
   * 通过用户id查询用户简单信息
   * @param userId 用户id
   * @return 用户信息
   */
  SimpleUserDTO getSimpleUserDTO(@Param("userId") Long userId);

  /**
   * 通过用户id列表查询用户简单信息
   * @param userIdList 用户id列表
   * @return 用户信息
   */
  List<SimpleUserDTO> getSimpleUserList(@Param("userIdList") List<Long> userIdList);
}
