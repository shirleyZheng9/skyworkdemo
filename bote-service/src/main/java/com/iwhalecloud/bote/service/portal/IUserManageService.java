package com.iwhalecloud.bote.service.portal;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.organization.CreateOrgMemberDTO;
import com.iwhalecloud.bote.dto.portal.SimpleUserDTO;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import com.iwhalecloud.bote.dto.portal.TenantUserDTO;
import com.iwhalecloud.bote.dto.portal.UserDTO;
import com.iwhalecloud.bote.dto.portal.UserImportDTO;
import com.iwhalecloud.bote.dto.portal.query.ModifyPasswordParams;
import com.iwhalecloud.bote.dto.portal.query.UserQueryParams;
import com.iwhalecloud.bote.entity.portal.UserEntity;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;


/**
 * 用户管理服务
 *
 * @author auto
 * @since 2024-09-13
 */
public interface IUserManageService {

  /**
   * 查询单个用户
   *
   * @param userId 用户主键
   * @param desensitize 是否对用户敏感信息脱敏
   * @return 用户
   */
  UserDTO findUser(Long userId, boolean desensitize);

  /**
   * 查询批量用户
   *
   * @param userIdList 用户ID集合
   * @return 用户
   */
  List<UserDTO> findBatchUser(List<Long> userIdList);

  /**
   * 保存用户
   *
   * @param user 用户
   * @return 结果
   */
  ResultVO<UserDTO> saveUser(UserDTO user);

  /**
   * 删除用户
   *
   * @param userId 用户主键
   * @return 结果
   */
  ResultVO<Void> deleteUser(Long userId);

  /**
   * 查询用户列表（分页）
   *
   * @param queryParams 查询条件
   * @return 用户分页列表
   */
  PageInfo<SimpleUserDTO> queryUserPage(UserQueryParams queryParams);

  /**
   * 查询用户列表
   *
   * @param queryParams 查询条件
   * @return 用户列表
   */
  List<SimpleUserDTO> queryUserList(UserQueryParams queryParams);

  /**
   * 修改密码
   *
   * @param params 入参
   * @return 结果
   */
  ResultVO<Void> modifyPassword(ModifyPasswordParams params);

  /**
   * 根据用户编码修改密码
   *
   * @param params 入参
   * @return 结果
   */
  ResultVO<Void> modifyPasswordByCode(ModifyPasswordParams params);

  /**
   * 重置密码
   *
   * @param params 入参
   * @return 结果
   */
  ResultVO<Void> resetPassword(ModifyPasswordParams params);

  /**
   * 用户注册
   *
   * @param user 用户
   * @return 结果
   */
  ResultVO<Void> registerUser(UserDTO user);

  /**
   * 根据用户ID查询租户列表
   *
   * @param queryParams 查询参数
   * @return 租户列表
   */
  ResultVO<PageInfo<TenantDTO>> queryTenantPageByUserId(UserQueryParams queryParams);

  /**
   * 批量导入用户
   *
   * @param file 文件
   * @param userRole 权限等级
   * @param tenantId 租户 ID
   * @return 导入结果
   */
  ResultVO<UserImportDTO> batchImportUsers(MultipartFile file, String userRole, Long tenantId);

  /**
   * 批量导入用户
   *
   * @param userEntityList 用户列表
   * @param tenantUserDTOList 租户用户列表
   * @param result 导入结果
   */
  void batchInsertUsersIO(List<UserEntity> userEntityList, List<TenantUserDTO> tenantUserDTOList, UserImportDTO result);

  /**
   * 锁定用户
   *
   * @param userId 用户id
   * @return 锁定结果
   */
  ResultVO<Boolean> lockUser(Long userId);

  /**
   * 解锁用户
   *
   * @param userId 用户id
   * @return 解锁结果
   */
  ResultVO<Boolean> unLockUser(Long userId);

  /**
   * 禁用用户
   *
   * @param userId 用户id
   * @return 禁用结果
   */
  ResultVO<Boolean> disableUser(Long userId);

  /**
   * 启用用户
   *
   * @param userId 用户id
   * @return 启用结果
   */
  ResultVO<Boolean> enableUser(Long userId);

  /**
   * 检查账号是否过期任务
   */
  void checkUserExpTask();

  /**
   * 发送用户头像
   *
   * @param userId 用户ID
   */
  void sendUserIcon(HttpServletResponse response, Long userId) throws IOException;

  /**
   * 分页获取用户基本信息列表
   *
   * @param queryParams 查询条件
   * @return 用户分页列表
   */
  PageInfo<SimpleUserDTO> pageUserBasicInfo(UserQueryParams queryParams);

  /**
   * 创建新用户已经组织成员
   *
   * @param orgMemberDTO 组织成员信息
   */
  ResultVO<CreateOrgMemberDTO> createUserAndMember(CreateOrgMemberDTO orgMemberDTO);

  /**
   * 查询简单用户信息给前端
   * @param userId 用户主键
   * @return 用户信息
   */
  SimpleUserDTO findSimpleUser(Long userId);
}
