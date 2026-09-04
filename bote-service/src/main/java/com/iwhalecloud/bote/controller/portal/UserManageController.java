package com.iwhalecloud.bote.controller.portal;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.annotation.RequestCacheable;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.AesUtil;
import com.iwhalecloud.bote.dto.portal.SimpleUserDTO;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import com.iwhalecloud.bote.dto.portal.UserDTO;
import com.iwhalecloud.bote.dto.portal.UserImportDTO;
import com.iwhalecloud.bote.dto.portal.query.ModifyPasswordParams;
import com.iwhalecloud.bote.dto.portal.query.UserQueryParams;
import com.iwhalecloud.bote.service.portal.IUserManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户管理服务
 *
 * @author qian.sisheng
 * @since 2024/8/12
 */

@RequestMapping(path = BaseConsts.API_PREFIX + "manager/user", name = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor
@Tag(name = "门户：用户管理服务")
public class UserManageController {
  private final IUserManageService userManageService;

  @PostMapping("saveUser")
  @Operation(summary = "保存用户")
  public ResultVO<UserDTO> saveUser(@RequestBody UserDTO user) {
    ResultVO<UserDTO> result = userManageService.saveUser(user);
    if (result.isSuccess() && result.getResultObject() != null) {
      // 避免将密码返回给前端
      result.getResultObject().setPassword(null);
    }
    return result;
  }

  @GetMapping("findUser")
  @Operation(summary = "查找用户")
  public ResultVO<UserDTO> findUser(@RequestParam("userId") Long userId) {
    Assert.notNull(userId, "用户 ID 不能为空");
    UserDTO user = userManageService.findUser(userId, true);
    // 避免将密码返回给前端
    user.setPassword(null);
    return ResultVO.success(user);
  }

  @GetMapping("findSimpleUser")
  @Operation(summary = "查找用户")
  public ResultVO<SimpleUserDTO> findSimpleUser(@RequestParam("userId") Long userId) {
    Assert.notNull(userId, "用户 ID 不能为空");
    return ResultVO.success(userManageService.findSimpleUser(userId));
  }

  @PostMapping("queryUserPage")
  @Operation(summary = "查找用户列表(分页)")
  public ResultVO<PageInfo<SimpleUserDTO>> queryUserPage(@RequestBody UserQueryParams params) {
    return ResultVO.success(userManageService.queryUserPage(params));
  }

  @PostMapping("queryUserList")
  @Operation(summary = "查找用户列表")
  public ResultVO<List<SimpleUserDTO>> queryUserList(@RequestBody UserQueryParams queryParams) {
    return ResultVO.success(userManageService.queryUserList(queryParams));
  }

  @GetMapping("deleteUser")
  @Operation(summary = "删除用户")
  public ResultVO<Void> deleteUser(@RequestParam("userId") Long userId) {
    Assert.notNull(userId, "用户 ID 不能为空");
    return userManageService.deleteUser(userId);
  }

  @PostMapping("modifyPassword")
  @Operation(summary = "修改用户密码")
  public ResultVO<Void> modifyPassword(@RequestBody ModifyPasswordParams params) {
    Assert.hasText(params.getOldPassword(), "旧密码不能为空");
    Assert.hasText(params.getNewPassword(), "新密码不能为空");
    String encryptionKey = SystemParameter.ENCRYPTION_AES.getValueFromDb();
    params.setOldPassword(AesUtil.aesDecrypt(params.getOldPassword(), encryptionKey));
    params.setNewPassword(AesUtil.aesDecrypt(params.getNewPassword(), encryptionKey));
    return userManageService.modifyPassword(params);
  }

  @IgnoreSign
  @IgnoreSession
  @PostMapping("modifyPasswordByCode")
  @Operation(summary = "根据用户编码修改密码:免鉴权使用")
  public ResultVO<Void> modifyPasswordByCode(@RequestBody ModifyPasswordParams params) {
    Assert.hasText(params.getUserName(), "用户名不能为空");
    Assert.hasText(params.getOldPassword(), "旧密码不能为空");
    Assert.hasText(params.getNewPassword(), "新密码不能为空");
    String encryptionKey = SystemParameter.ENCRYPTION_AES.getValueFromDb();
    params.setOldPassword(AesUtil.aesDecrypt(params.getOldPassword(), encryptionKey));
    params.setNewPassword(AesUtil.aesDecrypt(params.getNewPassword(), encryptionKey));
    return userManageService.modifyPasswordByCode(params);
  }

  @PostMapping("resetPassword")
  @Operation(summary = "重置用户密码")
  public ResultVO<Void> resetPassword(@RequestBody ModifyPasswordParams params) {
    Assert.hasText(params.getNewPassword(), "新密码不能为空");
    String encryptionKey = SystemParameter.ENCRYPTION_AES.getValueFromDb();
    params.setNewPassword(AesUtil.aesDecrypt(params.getNewPassword(), encryptionKey));
    return userManageService.resetPassword(params);
  }

  @IgnoreSession
  @IgnoreSign
  @PostMapping("registerUser")
  @Operation(summary = "注册用户")
  public ResultVO<Void> registerUser(@RequestBody UserDTO user) {
    Assert.hasText(user.getPassword(), "用户密码不能为空");
    Assert.hasText(user.getUserName(), "用户名不能为空");
    if (!SystemParameter.REGISTER_ENABLED.getBooleanValueFromDb()) {
      return ResultVO.fail("未开放用户注册");
    }
    return userManageService.registerUser(user);
  }

  @PostMapping("queryTenantPageByUserId")
  @Operation(summary = "根据用户ID查询用户关联的租户列表")
  public ResultVO<PageInfo<TenantDTO>> queryTenantPageByUserId(@RequestBody UserQueryParams queryParams) {
    return userManageService.queryTenantPageByUserId(queryParams);
  }

  @PostMapping("batchImportUsers")
  @Operation(summary = "批量导入用户")
  public ResultVO<UserImportDTO> batchImportUsers(@RequestParam("file") MultipartFile file, @RequestParam String userRole,
    @RequestParam(value = "tenantId", required = false) Long tenantId) {
    return userManageService.batchImportUsers(file, userRole, tenantId);
  }

  @GetMapping("lock")
  @Operation(summary = "锁定用户")
  public ResultVO<Boolean> lockUser(@RequestParam("userId") Long userId) {
    return userManageService.lockUser(userId);
  }

  @GetMapping("unLock")
  @Operation(summary = "解锁用户")
  public ResultVO<Boolean> unLockUser(@RequestParam("userId") Long userId) {
    return userManageService.unLockUser(userId);
  }

  @GetMapping("disable")
  @Operation(summary = "禁用用户")
  public ResultVO<Boolean> disableUser(@RequestParam("userId") Long userId) {
    return userManageService.disableUser(userId);
  }

  @GetMapping("enable")
  @Operation(summary = "启用用户")
  public ResultVO<Boolean> enableUser(@RequestParam("userId") Long userId) {
    return userManageService.enableUser(userId);
  }

  @IgnoreSign
  @IgnoreSession
  @Operation(summary = "获取用户头像")
  @GetMapping(value = "userIcon", produces = MediaType.ALL_VALUE)
  @RequestCacheable(sql = "SELECT updated_time FROM bt_user WHERE user_id = #{param1}", cacheOnNotFound = true)
  public void getUserIcon(@RequestParam("userId") Long userId, HttpServletResponse response) throws IOException {
    Assert.notNull(userId, "用户ID不能为空");
    userManageService.sendUserIcon(response, userId);
  }

  @PostMapping("pageUserBasicInfo")
  @Operation(summary = "分页获取用户基本信息列表")
  public ResultVO<PageInfo<SimpleUserDTO>> pageUserBasicInfo(@RequestBody UserQueryParams params) {
    return ResultVO.success(userManageService.pageUserBasicInfo(params));
  }

}
