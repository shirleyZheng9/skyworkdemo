package com.iwhalecloud.bote.service.portal.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bassc.basiccenter.dto.LitchiLogDTO;
import com.iwhalecloud.bassc.basiccenter.service.ILitchiAuthLogService;
import com.iwhalecloud.bassc.basiccenter.service.impl.LitchiAuthLogServiceImpl;
import com.iwhalecloud.bassc.basiccenter.util.PasswordRuleUtil;
import com.iwhalecloud.bote.common.consts.AccountEventTypeEnum;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.AesUtil;
import com.iwhalecloud.bote.common.util.DataMaskUtil;
import com.iwhalecloud.bote.common.util.EnvUtil;
import com.iwhalecloud.bote.common.util.ExcelUtil;
import com.iwhalecloud.bote.common.util.IconUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.organization.CreateOrgMemberDTO;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.dto.portal.SimpleUserDTO;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import com.iwhalecloud.bote.dto.portal.TenantUserDTO;
import com.iwhalecloud.bote.dto.portal.UserDTO;
import com.iwhalecloud.bote.dto.portal.UserImportDTO;
import com.iwhalecloud.bote.dto.portal.UserPwdHisDTO;
import com.iwhalecloud.bote.dto.portal.query.ModifyPasswordParams;
import com.iwhalecloud.bote.dto.portal.query.UserQueryParams;
import com.iwhalecloud.bote.entity.portal.UserEntity;
import com.iwhalecloud.bote.entity.portal.UserPwdHisEntity;
import com.iwhalecloud.bote.mapper.portal.TenantManageMapper;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import com.iwhalecloud.bote.mapper.portal.UserPwdHisMapper;
import com.iwhalecloud.bote.portal.adapter.DefaultPortalAuthProvider;
import com.iwhalecloud.bote.service.base.IAccountEventLogService;
import com.iwhalecloud.bote.service.organization.IOrganizationMemberService;
import com.iwhalecloud.bote.service.portal.IUserManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.DataDifferenceStarter;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户管理服务实现
 *
 * @author auto
 * @since 2024-09-13
 */
@Service
@SuppressFBWarnings("REDOS")
@RequiredArgsConstructor
public class UserManageServiceImpl implements IUserManageService {

  private static final Logger log = LoggerFactory.getLogger(UserManageServiceImpl.class);

  // @formatter:off
  private final UserManageMapper userManageMapper;
  private final PasswordEncoder passwordEncoder;
  private final TenantManageMapper tenantManageMapper;
  private final ILitchiAuthLogService authLogService;
  private final IAccountEventLogService accountEventLogService;
  private final UserPwdHisMapper userPwdHisMapper;
  private final IOrganizationMemberService orgMemberService;
  private final DefaultPortalAuthProvider defaultPortalAuthProvider;

  private static final Integer BATCH_SIZE = 200;
  private static final Integer INITIAL_ROW = 2;
  private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{11}$");
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
  private static final Pattern USER_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\p{Punct}]+$");
  private static final String USER_CODE_REGEX = "^[a-zA-Z0-9_-]+$";
  private static final long DAY_TIME = 24 * 3600 * 1000L;
  private static final String AVATAR_PATH = "assets/avatar/";
  private static final String DEFAULT_AVATAR_PATH = AVATAR_PATH + "avatar-default.png";

  // 字段长度限制常量
  private static final Integer MAX_USER_NAME_LENGTH = 255;
  private static final Integer MAX_REAL_NAME_LENGTH = 50;
  private static final Integer MAX_PHONE_NO_LENGTH = 255;
  private static final Integer MAX_EMAIL_LENGTH = 4000;
  private static final Integer MAX_REMARK_LENGTH = 255;
  // @formatter:on

  @Override
  public UserDTO findUser(Long userId, boolean desensitize) {
    UserDTO user = userManageMapper.getUser(userId);
    Assert.notNull(user, () -> "用户不存在: " + userId);
    user.setTenantUserList(tenantManageMapper.selectTenantUserListByUserIds(Collections.singletonList(userId)));

    if (desensitize) {
      // 用户数据脱敏
      if (StringUtils.isNotEmpty(user.getPhoneNo())) {
        user.setPhoneNo(DataMaskUtil.maskPhone(user.getPhoneNo()));
      }
      if (StringUtils.isNotEmpty(user.getEmail())) {
        user.setEmail(DataMaskUtil.maskEmail(user.getEmail()));
      }
    }
    return user;
  }

  @Override
  public List<UserDTO> findBatchUser(List<Long> userIdList) {
    if (CollectionUtils.isEmpty(userIdList)) {
      return Collections.emptyList();
    }
    return userManageMapper.selectUserDTOListByUserIds(userIdList);
  }

  @Override
  @Transactional
  @SuppressWarnings("PMD.GuardLogStatement")
  public void checkUserExpTask() {
    // 查询在线用户过期时间（多久未登录账号自动过期）
    String timeout = SystemParameter.USER_INVALID_TIME.getValueFromDb();
    if (StringUtils.isBlank(timeout)) {
      return;
    }
    // 查询统计需要检查过期的用户数
    long userCount = userManageMapper.countForUserExpCheck();
    if (userCount <= 0) {
      return;
    }
    // 循环分页查询用户数据校验是否需要自动过期: 每次分页查询300条数据
    List<UserEntity> userList;
    long lastUserId = 0;
    long now = System.currentTimeMillis();
    long timeoutDay = Long.parseLong(timeout);
    RowBounds rowBounds = new RowBounds(0, 300);
    do {
      // 查询用户数据
      //noinspection resource
      PageInfo<UserEntity> userPage = userManageMapper.selectPageForUserExpCheck(lastUserId, rowBounds).toPageInfo();
      userList = userPage.getList();
      if (CollectionUtils.isNotEmpty(userList)) {
        // 设置最后一个用户ID，作为查询的 offset
        lastUserId = userList.get(userList.size() - 1).getUserId();
        for (UserEntity user : userList) {
          // 检查用户最后一次登录时间至今是否超过未登录时间上限
          LitchiLogDTO logDTO = getUserLastLoginLog(user.getUserId());
          if (logDTO != null && (now - logDTO.getLogDate().getTime() > timeoutDay * DAY_TIME)) {
            log.info("Disable user:{}-{}, user invalid time:{}, user last login time:{}", user.getUserId(), user.getUserName(), timeoutDay, logDTO.getLogDate());
            userManageMapper.updateUserState(user.getUserId(), BaseConsts.USER_STATE_DISABLE);
          }
        }
      }
    }
    while (CollectionUtils.isNotEmpty(userList));
  }

  @Override
  @Transactional
  public ResultVO<UserDTO> saveUser(UserDTO user) {
    Assert.hasLength(user.getUserName(), "用户名不能为空");
    checkUserCode(user.getUserName());
    user.setStatusCd(BaseConsts.STATUS_CD_VALID);
    UserDTO old = user.getUserId() == null ? null : findUser(user.getUserId());
    // 初始化新用户的相关数据
    if (old == null) {
      initNewUserForSave(user);
    }
    else {
      initExistingUserForSave(old, user);
    }
    // 校验名称唯一性
    if ((old == null || !old.getUserName().equals(user.getUserName())) && userManageMapper.existsUserName(
      user.getSystemCode(), user.getUserName())) {
      return BaseErrorConstant.CHECK_CODE.toResult(user.getUserName());
    }
    DataDifference<UserDTO> difference = DataDifferenceStarter.computeSave(old, user, false, true);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }

    // 记录用户密码历史
    saveUserPwdHis(user.getUserId(), user.getPassword(), SessionUtil.getLoginInfo().getUserId());
    // 处理组织用户成员
    processOrgUserMember(old, user);

    setPortalAuthProvide(user);

    // 记录保存账号事件日志
    saveEventLog(old == null, difference);

    return ResultVO.success(difference.getToSaveData());
  }

  /**
   * 设置用户权限提供者
   *
   * @param user 用户
   */
  private void initNewUserForSave(UserDTO user) {
    user.setSystemCode(
      StringUtils.isEmpty(user.getSystemCode()) ? BaseConsts.PORTAL_SYSTEM_CODE_DEFAULT : user.getSystemCode());
    user.setIsLocked(BaseConsts.IS_LOCKED_FALSE);
    user.setLoginFailCount(0);
    user.setUserState(BaseConsts.USER_STATE_ENABLE);
    user.setPassword(passwordEncoder.encode(SystemParameter.USER_INIT_PASSWORD.getValueFromDb()));
    if (user.getCreateTenantId() == null) {
      user.setCreateTenantId(getCreateTenantId(user.getTenantUserList()));
    }
    if (EnvUtil.isRuntime()) {
      // 标记运行态注册的用户
      user.setUserType(BaseConsts.USER_TYPE_USE);
    }
  }

  /**
   * 获取用户修改信息
   *
   * @param old  旧用户信息
   * @param user 新用户信息
   */
  private void initExistingUserForSave(UserDTO old, UserDTO user) {
    // 用户修改个人信息时会传头像、默认租户，管理员修改其他用户时不会传
    user.setUserIcon(StringUtils.defaultIfEmpty(user.getUserIcon(), old.getUserIcon()));
    user.setDefaultTenantId(ObjectUtils.getIfNull(user.getDefaultTenantId(), old.getDefaultTenantId()));
    user.setSystemCode(user.getSystemCode() != null ? user.getSystemCode() : old.getSystemCode());
    user.setExtUserId(user.getExtUserId() != null ? user.getExtUserId() : old.getExtUserId());
    // 不允许修改密码（需要使用单独的修改密码接口）
    user.setPassword(old.getPassword());
    user.setTenantUserList(compareTenantUserList(old.getTenantUserList(), user.getTenantUserList()));
    user.setCreateTenantId(old.getCreateTenantId());
    user.setUpdatedTime(new Date());
  }


  /**
   *
   * @param user 用户信息
   */
  private void setPortalAuthProvide(UserDTO user) {
    LoginInfo loginInfo = SessionUtil.getLoginInfo();
    if (Objects.equals(loginInfo.getUserId(), user.getUserId()) && BaseConsts.PORTAL_SYSTEM_CODE_DEFAULT.equals(user.getSystemCode())) {
      // 修改了当前登录账号基本信息，需要更新 session 信息
      loginInfo.setRealName(user.getRealName());
      loginInfo.setPhoneNo(user.getPhoneNo());
      loginInfo.setDefaultTenantId(user.getDefaultTenantId());
      String sessionId = SessionUtil.getSessionId();
      if (StringUtils.isNotEmpty(sessionId)) {
        defaultPortalAuthProvider.updateLoginInfo(sessionId, loginInfo);
      }
    }
  }

  /**
   * 获取用户的创建租户ID
   */
  @Nullable
  private Long getCreateTenantId(List<TenantUserDTO> tenantUserList) {
    if (CollectionUtils.isEmpty(tenantUserList)) {
      return null;
    }
    // 取第一个租户作为用户的创建租户
    TenantUserDTO tenantUserDTO = tenantUserList.getFirst();
    return tenantUserDTO.getTenantId();
  }

  /**
   * 保存事件相关日志
   */
  private void saveEventLog(boolean isAddAccount, DataDifference<UserDTO> difference) {
    AccountEventTypeEnum eventType = isAddAccount ? AccountEventTypeEnum.ADD_ACCOUNT : AccountEventTypeEnum.MOD_ACCOUNT;
    saveAccountEventLog(eventType, difference.getToSaveData());
  }

  /**
   * 处理组织用户成员
   */
  private void processOrgUserMember(@Nullable UserDTO old, UserDTO user) {
    // 添加新用户到对应空间的根组织下
    if (old == null && user.getSpaceId() != null) {
      orgMemberService.addUserToRootOrg(user.getSpaceId(), user.getUserId());
    }
  }

  private List<TenantUserDTO> compareTenantUserList(List<TenantUserDTO> oldList, List<TenantUserDTO> newList) {
    if (CollectionUtils.isEmpty(oldList) && CollectionUtils.isEmpty(newList)) {
      return Collections.emptyList();
    }
    if (CollectionUtils.isEmpty(oldList)) {
      return newList;
    }
    if (CollectionUtils.isEmpty(newList)) {
      return oldList;
    }
    for (TenantUserDTO oldTenantUser : oldList) {
      TenantUserDTO newTenantUser = IterableUtils.find(newList,
        tenantUser -> Objects.equals(tenantUser.getTenantUserId(), oldTenantUser.getTenantUserId()));
      if (newTenantUser == null) {
        newList.add(oldTenantUser);
      }
    }
    return newList;
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteUser(Long userId) {
    UserDTO userDTO = findUser(userId);
    userManageMapper.deleteUser(userId, SessionUtil.getLoginInfo().getUserId());
    tenantManageMapper.deleteTenantUserByUserId(userId, SessionUtil.getLoginInfo().getUserId());
    saveAccountEventLog(AccountEventTypeEnum.DEL_ACCOUNT, userDTO);
    return ResultVO.success();
  }

  @Override
  public PageInfo<SimpleUserDTO> queryUserPage(UserQueryParams queryParams) {
    //noinspection resource
    PageInfo<SimpleUserDTO> pageInfo = userManageMapper.selectUserPage(queryParams, queryParams.getTenantId(), queryParams.buildRowBounds())
      .toPageInfo();
    // 获取当前分页中的用户列表
    List<SimpleUserDTO> userList = pageInfo.getList();
    if (CollectionUtils.isNotEmpty(userList)) {
      // 提取用户ID列表，用于批量查询租户用户信息
      List<Long> userIds = userList.stream().map(SimpleUserDTO::getUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(userIds)) {
        Map<Long, List<TenantUserDTO>> tenantUserMap = tenantManageMapper.selectTenantUserListByUserIds(userIds).stream()
          .collect(Collectors.groupingBy(TenantUserDTO::getUserId));
        userList.forEach(user -> user.setTenantUserList(tenantUserMap.getOrDefault(user.getUserId(), Collections.emptyList())));
      }

      // 用户数据脱敏
      userList.forEach(user -> {
        if (StringUtils.isNotEmpty(user.getPhoneNo())) {
          user.setPhoneNo(DataMaskUtil.maskPhone(user.getPhoneNo()));
        }
        if (StringUtils.isNotEmpty(user.getEmail())) {
          user.setEmail(DataMaskUtil.maskEmail(user.getEmail()));
        }
      });
    }
    return pageInfo;
  }

  @Override
  public List<SimpleUserDTO> queryUserList(UserQueryParams queryParams) {
    return userManageMapper.selectUserList(queryParams);
  }

  @Override
  @Transactional
  public ResultVO<Void> modifyPassword(ModifyPasswordParams params) {
    // 校验密码复杂度
    checkPassword(params.getNewPassword());

    Long userId = SessionUtil.getLoginInfo().getUserId();
    UserEntity user = userManageMapper.getUser(userId);
    if (user == null) {
      return BaseErrorConstant.USER_NAME_NOT_EXISTS.toResult(userId);
    }
    if (!passwordEncoder.matches(params.getOldPassword(), user.getPassword())) {
      return BaseErrorConstant.USER_PASSWORD_FAIL.toResult();
    }

    // 用户密码历史重复校验
    checkUserPwdHisRepeat(user.getUserId(), params.getNewPassword());
    // 更新用户密码
    userManageMapper.updateUserPassword(passwordEncoder.encode(params.getNewPassword()), user.getUserId());
    // 记录用户密码历史
    saveUserPwdHis(user.getUserId(), passwordEncoder.encode(params.getNewPassword()), SessionUtil.getLoginInfo().getUserId());

    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> modifyPasswordByCode(ModifyPasswordParams params) {
    // 校验密码复杂度
    checkPassword(params.getNewPassword());
    // 查询用户信息
    UserEntity user = userManageMapper.getUserByCode(BaseConsts.PORTAL_SYSTEM_CODE_DEFAULT, params.getUserName());
    if (user == null) {
      return BaseErrorConstant.USER_NAME_NOT_EXISTS.toResult(params.getUserName());
    }
    // 旧密码校验
    if (!passwordEncoder.matches(params.getOldPassword(), user.getPassword())) {
      return BaseErrorConstant.USER_PASSWORD_FAIL.toResult();
    }
    // 新旧密码不能相同
    if (passwordEncoder.matches(params.getNewPassword(), user.getPassword())) {
      return BaseErrorConstant.USER_MODIFY_PWD_SAME.toResult();
    }
    // 用户密码历史重复校验
    checkUserPwdHisRepeat(user.getUserId(), params.getNewPassword());
    // 修改用户密码
    userManageMapper.updateUserPassword(passwordEncoder.encode(params.getNewPassword()), user.getUserId());
    // 记录用户密码历史
    saveUserPwdHis(user.getUserId(), passwordEncoder.encode(params.getNewPassword()), user.getUserId());
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> resetPassword(ModifyPasswordParams params) {
    // 校验密码复杂度
    checkPassword(params.getNewPassword());

    UserEntity user = userManageMapper.getUser(params.getUserId());
    if (user == null) {
      return BaseErrorConstant.USER_NAME_NOT_EXISTS.toResult(params.getUserId());
    }
    // 用户密码历史重复校验
    checkUserPwdHisRepeat(user.getUserId(), params.getNewPassword());
    // 修改用户密码
    userManageMapper.updateUserPassword(passwordEncoder.encode(params.getNewPassword()), user.getUserId());
    // 记录用户密码历史
    saveUserPwdHis(user.getUserId(), passwordEncoder.encode(params.getNewPassword()), SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> registerUser(UserDTO user) {
    // 校验用户编码
    checkUserCode(user.getUserName());

    user.setStatusCd(BaseConsts.STATUS_CD_VALID);
    user.setSystemCode(BaseConsts.PORTAL_SYSTEM_CODE_DEFAULT);
    user.setExtUserId(null);
    // 校验名称唯一性
    if (userManageMapper.existsUserName(user.getSystemCode(), user.getUserName())) {
      return ResultVO.fail("用户编码已存在");
    }
    List<TenantUserDTO> tenantUserList = new ArrayList<>();
    TenantUserDTO tenantUser = new TenantUserDTO();
    TenantDTO tenant = null;
    // 开启租户注册模式
    if (BaseConsts.TRUE.equals(SystemParameter.USER_REGISTER_MODE_ENABLED.getValueFromDb()) && !EnvUtil.isRuntime()) {
      Long tenantId = Sequences.TENANT_ID.next();
      user.setDefaultTenantId(tenantId);
      user.setCreateTenantId(tenantId);
      tenantUser.setUserRole(SystemParameter.USER_REGISTER_ROLE.getValueFromDb());
      tenant = registerTenant(user, tenantId);
    }
    else {
      // 运行态注册的用户，授予租户使用角色
      String userRole = Objects.equals(BaseConsts.USER_TYPE_USE, user.getUserType()) ? BaseConsts.ROLE_USE : BaseConsts.ROLE_EDIT;
      user.setDefaultTenantId(BaseConsts.DEFAULT_TENANT_ID);
      tenantUser.setUserRole(userRole);
    }
    tenantUser.setTenantId(user.getDefaultTenantId());
    tenantUserList.add(tenantUser);
    user.setTenantUserList(tenantUserList);

    // 解密密码
    String encryptionKey = SystemParameter.ENCRYPTION_AES.getValueFromDb();
    String password = AesUtil.aesDecrypt(user.getPassword(), encryptionKey);
    // 校验密码复杂度
    checkPassword(password);

    // 补充账号管理相关参数
    user.setIsLocked(BaseConsts.IS_LOCKED_FALSE);
    user.setLoginFailCount(0);
    user.setUserState(BaseConsts.USER_STATE_ENABLE);

    user.setPassword(passwordEncoder.encode(password));
    DataDifferenceStarter.computeSave(null, user, false, true);
    updateUpdatorId(user, tenant, tenantUser);

    // 记录用户密码历史
    saveUserPwdHis(user.getUserId(), user.getPassword(), user.getUserId());

    return ResultVO.success();
  }

  private void updateUpdatorId(UserDTO user, @Nullable TenantDTO tenant, @Nullable TenantUserDTO tenantUser) {
    user.setUpdatorId(user.getUserId());
    userManageMapper.updateUser(user);
    if (tenant != null) {
      tenant.setUpdatorId(user.getUserId());
      tenantManageMapper.updateTenant(tenant);
    }
    if (tenantUser != null) {
      tenantUser.setUpdatorId(user.getUserId());
      tenantManageMapper.updateTenantUser(tenantUser);
    }
  }

  private TenantDTO registerTenant(UserDTO user, Long tenantId) {
    TenantDTO tenant = new TenantDTO();
    tenant.setTenantId(tenantId);
    tenant.setTenantCode(user.getUserName());
    tenant.setTenantName(user.getUserName());
    tenant.setSpaceId(0L);
    tenant.setStatusCd(BaseConsts.STATUS_CD_VALID);
    // 校验编码唯一性
    if (tenantManageMapper.existsTenantCode(tenant)) {
      throw new BssException("租户编码已存在");
    }
    tenantManageMapper.insertTenant(tenant);
    return tenant;
  }

  @Override
  public ResultVO<PageInfo<TenantDTO>> queryTenantPageByUserId(UserQueryParams queryParams) {
    //noinspection resource
    return ResultVO.success(tenantManageMapper.selectTenantPageByUserId(queryParams, queryParams.buildRowBounds()).toPageInfo());
  }

  @Override
  public ResultVO<UserImportDTO> batchImportUsers(MultipartFile file, String userRole, Long tenantId) {
    try {
      // 1. 解析Excel文件
      List<Map<String, Object>> dataList = parseExcel(file);
      if (!StringUtils.isEmpty((String) dataList.get(0).get("errMsg"))) {
        return ResultVO.fail("批量导入用户失败，请使用正确的模板进行导入");
      }

      // 2. 数据校验
      if (!isValidUserRole(userRole)) {
        return ResultVO.fail("批量导入用户失败，权限等级非法");
      }
      UserImportDTO importResult = validateData(dataList);

      // 3. IO操作
      if (importResult.getSuccessCount() > 0) {
        batchInsert(importResult, userRole, tenantId);
      }

      return ResultVO.success(importResult);
    }
    catch (BssException | IOException e) {
      log.error("Excel导入失败", e);
      return ResultVO.fail("批量导入用户失败: " + e.getMessage());
    }
  }

  private List<Map<String, Object>> parseExcel(MultipartFile file) throws IOException {
    // 使用现有的ExcelUtil类解析Excel文件
    String[] colName = {"rowIndex", "userName", "realName", "phoneNo", "email", "remark"};
    boolean isXls = !Strings.CI.endsWith(file.getOriginalFilename(), ".xlsx");
    try (InputStream inputStream = file.getInputStream()) {
      return ExcelUtil.getExcelData(inputStream, INITIAL_ROW, isXls, colName);
    }
  }

  private UserImportDTO validateData(List<Map<String, Object>> dataList) {
    UserImportDTO result = new UserImportDTO();
    result.setSuccessList(new ArrayList<>());
    result.setFailList(new ArrayList<>());

    Set<String> seenUserNames = new HashSet<>();
    int row = INITIAL_ROW;

    for (Map<String, Object> userData : dataList) {
      row++;
      // 检查行是否全为空
      if (isRowEmpty(userData)) {
        continue;
      }

      String rowIndex = (String) userData.get("rowIndex");
      if (!validateRowIndex(result, userData, rowIndex, row)) {
        continue;
      }

      String userName = (String) userData.get("userName");
      if (!validateUserName(result, userData, userName, rowIndex)) {
        continue;
      }

      String realName = (String) userData.get("realName");
      if (!validateRealName(result, userData, realName, rowIndex)) {
        continue;
      }

      if (!validatePhoneNumber(result, userData, rowIndex)) {
        continue;
      }

      if (!validateEmail(result, userData, rowIndex)) {
        continue;
      }

      if (!validateRemark(result, userData, rowIndex)) {
        continue;
      }

      if (!validateDuplicateUserName(result, userData, userName, rowIndex, seenUserNames)) {
        continue;
      }

      // 如果通过所有校验，添加到成功列表
      result.getSuccessList().add(userData);
    }

    result.setSuccessCount(result.getSuccessList().size());
    result.setFailCount(dataList.size() - result.getSuccessCount());
    result.setTotalCount(dataList.size());

    return result;
  }

  /**
   * 校验行号
   */
  private boolean validateRowIndex(UserImportDTO result, Map<String, Object> userData, String rowIndex, int row) {
    if (StringUtils.isEmpty(rowIndex)) {
      addErrorToImportResult(result, "行号: " + row, "缺少序号，已跳过此数据", userData);
      return false;
    }
    return true;
  }

  /**
   * 校验用户名
   */
  private boolean validateUserName(UserImportDTO result, Map<String, Object> userData, String userName, String rowIndex) {
    if (StringUtils.isEmpty(userName)) {
      addErrorToImportResult(result, "序号: " + rowIndex + ", 第2列", "用户编码不能为空", userData);
      return false;
    }

    if (userName.length() > MAX_USER_NAME_LENGTH) {
      addErrorToImportResult(result, "序号: " + rowIndex + ", 第2列", "用户编码长度不能超过" + MAX_USER_NAME_LENGTH + "字符", userData);
      return false;
    }

    if (!USER_NAME_PATTERN.matcher(userName).matches()) {
      addErrorToImportResult(result, "序号: " + rowIndex + ", 第2列", "用户编码不能为空，只能包含字母、数字或符号，不允许有空格", userData);
      return false;
    }

    return true;
  }

  /**
   * 校验真实姓名
   */
  private boolean validateRealName(UserImportDTO result, Map<String, Object> userData, String realName, String rowIndex) {
    if (StringUtils.isEmpty(realName)) {
      addErrorToImportResult(result, "序号: " + rowIndex + ", 第3列", "用户名不能为空", userData);
      return false;
    }

    if (realName.length() > MAX_REAL_NAME_LENGTH) {
      addErrorToImportResult(result, "序号: " + rowIndex + ", 第3列", "用户名长度不能超过" + MAX_REAL_NAME_LENGTH + "字符", userData);
      return false;
    }

    return true;
  }

  /**
   * 校验手机号码
   */
  private boolean validatePhoneNumber(UserImportDTO result, Map<String, Object> userData, String rowIndex) {
    if (!isValidPhoneNumber(userData.get("phoneNo"))) {
      addErrorToImportResult(result, "序号: " + rowIndex + ", 第4列", "手机号码格式不正确", userData);
      return false;
    }

    String phoneNo = (String) userData.get("phoneNo");
    if (StringUtils.isNotEmpty(phoneNo) && phoneNo.length() > MAX_PHONE_NO_LENGTH) {
      addErrorToImportResult(result, "序号: " + rowIndex + ", 第4列", "手机号码长度不能超过" + MAX_PHONE_NO_LENGTH + "字符", userData);
      return false;
    }

    return true;
  }

  /**
   * 校验邮箱
   */
  private boolean validateEmail(UserImportDTO result, Map<String, Object> userData, String rowIndex) {
    if (!isValidEmail(userData.get("email"))) {
      addErrorToImportResult(result, "序号: " + rowIndex + ", 第5列", "邮箱格式不正确", userData);
      return false;
    }

    String email = (String) userData.get("email");
    if (StringUtils.isNotEmpty(email) && email.length() > MAX_EMAIL_LENGTH) {
      addErrorToImportResult(result, "序号: " + rowIndex + ", 第5列", "邮箱长度不能超过" + MAX_EMAIL_LENGTH + "字符", userData);
      return false;
    }

    return true;
  }

  /**
   * 校验备注
   */
  private boolean validateRemark(UserImportDTO result, Map<String, Object> userData, String rowIndex) {
    String remark = (String) userData.get("remark");
    if (StringUtils.isNotEmpty(remark) && remark.length() > MAX_REMARK_LENGTH) {
      addErrorToImportResult(result, "序号: " + rowIndex + ", 第6列", "备注长度不能超过" + MAX_REMARK_LENGTH + "字符", userData);
      return false;
    }
    return true;
  }

  /**
   * 校验用户名重复
   */
  private boolean validateDuplicateUserName(UserImportDTO result, Map<String, Object> userData, String userName, String rowIndex, Set<String> seenUserNames) {
    if (!seenUserNames.add(userName)) {
      addErrorToImportResult(result, "序号: " + rowIndex, "当前批次中存在重复的用户名：" + userName + "，已跳过", userData);
      return false;
    }
    return true;
  }

  private boolean isRowEmpty(Map<String, Object> rowData) {
    return rowData.values().stream().noneMatch(value -> value != null && !String.valueOf(value).trim().isEmpty());
  }

  private void addErrorToImportResult(UserImportDTO result, String rowIndex, String errorMessage, Map<String, Object> data) {
    result.failPut(rowIndex, errorMessage, data);
  }

  private boolean isValidPhoneNumber(Object phoneNoObj) {
    String phoneNo = (String) phoneNoObj;
    if (StringUtils.isEmpty(phoneNo)) {
      return true; // 空值由其他校验处理
    }
    // 简单的手机号码格式验证
    return PHONE_PATTERN.matcher(phoneNo).matches();
  }

  private boolean isValidEmail(Object emailObj) {
    String email = (String) emailObj;
    if (StringUtils.isEmpty(email)) {
      return true; // 空值由其他校验处理
    }
    // 简单的邮箱格式验证
    return EMAIL_PATTERN.matcher(email).matches();
  }

  private boolean isValidUserRole(Object userRoleObj) {
    String userRole = (String) userRoleObj;
    if (StringUtils.isEmpty(userRole)) {
      return false;
    }
    return BaseConsts.ROLES.contains(userRole) || "TEST".equals(userRole);
  }

  private void batchInsert(UserImportDTO result, String userRole, Long tenantId) {
    List<Map<String, Object>> successList = result.getSuccessList();
    if (CollectionUtils.isEmpty(successList)) {
      return;
    }

    Long currentUserId = SessionUtil.getLoginInfo().getUserId();
    List<UserEntity> userEntityList = new ArrayList<>();
    List<TenantUserDTO> tenantUserDTOList = new ArrayList<>();

    for (Map<String, Object> data : successList) {
      String userName = (String) data.get("userName");
      String rowIndex = (String) data.get("rowIndex");

      if (StringUtils.isBlank(userName)) {
        addErrorToImportResult(result, rowIndex, "用户名不能为空", data);
        continue;
      }

      Long userId = Sequences.USER_ID.next();

      // 构造 UserEntity
      UserEntity user = new UserEntity();
      user.setUserId(userId);
      user.setUserName(userName);
      user.setRealName((String) data.get("realName"));
      user.setPhoneNo((String) data.get("phoneNo"));
      user.setEmail((String) data.get("email"));
      user.setPassword(passwordEncoder.encode(SystemParameter.USER_INIT_PASSWORD.getValueFromDb()));
      user.setSystemCode(BaseConsts.PORTAL_SYSTEM_CODE_DEFAULT);
      user.setDefaultTenantId(tenantId);
      user.setCreateTenantId(tenantId);
      user.setStatusCd(BaseConsts.STATUS_CD_VALID);
      user.setCreatorId(currentUserId);
      user.setUpdatorId(currentUserId);
      user.setRemark((String) data.get("remark"));

      // 构造 TenantUserDTO
      TenantUserDTO tenantUser = new TenantUserDTO();
      tenantUser.setTenantUserId(Sequences.TENANT_USER_ID.next());
      tenantUser.setUserId(userId);
      tenantUser.setTenantId(tenantId);
      tenantUser.setUserRole(userRole);
      tenantUser.setStatusCd(BaseConsts.STATUS_CD_VALID);
      tenantUser.setCreatorId(currentUserId);
      tenantUser.setUpdatorId(currentUserId);
      tenantUser.setUserName(userName);
      tenantUser.setRealName((String) data.get("realName"));
      tenantUser.setRemark((String) data.get("remark"));

      userEntityList.add(user);
      tenantUserDTOList.add(tenantUser);
    }

    IUserManageService iUserManageService = SpringUtil.getBean(IUserManageService.class);
    iUserManageService.batchInsertUsersIO(userEntityList, tenantUserDTOList, result);

  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void batchInsertUsersIO(List<UserEntity> userEntityList, List<TenantUserDTO> tenantUserDTOList, UserImportDTO result) {
    // 先进行存在性检查
    List<UserEntity> filteredUserList = new ArrayList<>();
    List<TenantUserDTO> filteredTenantUserList = new ArrayList<>();

    existedCheck(userEntityList, tenantUserDTOList, result, filteredUserList, filteredTenantUserList);

    // 再进行批量插入
    if (CollectionUtils.isNotEmpty(filteredUserList)) {
      for (int i = 0; i < filteredUserList.size(); i += BATCH_SIZE) {
        int end = Math.min(i + BATCH_SIZE, filteredUserList.size());

        List<UserEntity> userBatch = filteredUserList.subList(i, end);
        List<TenantUserDTO> tenantUserBatch = filteredTenantUserList.subList(i, end);

        userManageMapper.batchInsertUsers(userBatch);
        userManageMapper.batchInsertTenantUsers(tenantUserBatch);
      }
    }
  }

  private UserDTO findUser(Long userId) {
    return this.findUser(userId, false);
  }

  @Override
  @Transactional
  public ResultVO<Boolean> lockUser(Long userId) {
    int result = userManageMapper.updateUserLockState(userId, BaseConsts.IS_LOCKED_TRUE);
    return ResultVO.success(result == 1);
  }

  @Override
  @Transactional
  public ResultVO<Boolean> unLockUser(Long userId) {
    int result = userManageMapper.updateUserLockState(userId, BaseConsts.IS_LOCKED_FALSE);
    return ResultVO.success(result == 1);
  }

  @Override
  @Transactional
  public ResultVO<Boolean> disableUser(Long userId) {
    int result = userManageMapper.updateUserState(userId, BaseConsts.USER_STATE_DISABLE);
    return ResultVO.success(result == 1);
  }

  @Override
  @Transactional
  public ResultVO<Boolean> enableUser(Long userId) {
    int result = userManageMapper.updateUserState(userId, BaseConsts.USER_STATE_ENABLE);
    return ResultVO.success(result == 1);
  }

  /**
   * 检查用户是否存在，过滤掉已存在的用户
   *
   * @param userEntityList 用户实体列表
   * @param tenantUserDTOList 租户用户DTO列表
   * @param result 导入结果
   * @param filteredUserList 过滤后的用户列表
   * @param filteredTenantUserList 过滤后的租户用户列表
   */
  private void existedCheck(List<UserEntity> userEntityList, List<TenantUserDTO> tenantUserDTOList, UserImportDTO result,
    List<UserEntity> filteredUserList, List<TenantUserDTO> filteredTenantUserList) {

    if (CollectionUtils.isEmpty(userEntityList)) {
      return;
    }

    // 获取所有用户名进行批量检查
    List<String> allUserNames = userEntityList.stream()
      .map(UserEntity::getUserName)
      .collect(Collectors.toList());

    List<String> existingUserNames = userManageMapper.checkUserNamesExist(allUserNames);

    // 重新构建 successList 和 failList
    List<Map<String, Object>> newSuccessList = new ArrayList<>();
    List<Map<String, Object>> originalSuccessList = result.getSuccessList();

    // 过滤出不存在的用户
    for (int i = 0; i < userEntityList.size(); i++) {
      UserEntity user = userEntityList.get(i);
      TenantUserDTO tenantUser = tenantUserDTOList.get(i);

      if (!existingUserNames.contains(user.getUserName())) {
        filteredUserList.add(user);
        filteredTenantUserList.add(tenantUser);
        if (i < originalSuccessList.size()) {
          newSuccessList.add(originalSuccessList.get(i));
        }
      }
      else {
        // 记录已存在的用户错误信息
        String rowIndex = String.valueOf(i + 1);
        addErrorToImportResult(result, "序号: " + rowIndex + ", UserName: " + user.getUserName(),
          "系统中已存在同一用户编码", originalSuccessList.get(i));
      }
    }

    result.setSuccessList(newSuccessList);
    result.setSuccessCount(filteredUserList.size());
    result.setFailCount(result.getTotalCount() - result.getSuccessCount());
  }

  @Override
  public void sendUserIcon(HttpServletResponse response, Long userId) throws IOException {
    String userIcon = userManageMapper.selectUserIcon(userId);
    if (StringUtils.isEmpty(userIcon)) {
      // 没有设置用户头像则返回默认头像数据
      ClassPathResource resource = new ClassPathResource(DEFAULT_AVATAR_PATH);
      IconUtil.sendPathResourceIcon(response, resource);
    }
    else if (userIcon.startsWith("./images")) {
      // 根据头像相对路径返回对应的头像数据
      String[] parts = userIcon.split("/");
      String iconName = parts[parts.length - 1];
      ClassPathResource resource = new ClassPathResource(AVATAR_PATH + iconName);
      IconUtil.sendPathResourceIcon(response, resource);
    }
    else {
      // 发送Base64格式头像数据
      IconUtil.sendBase64Icon(response, userIcon);
    }
  }

  @Override
  public PageInfo<SimpleUserDTO> pageUserBasicInfo(UserQueryParams queryParams) {
    // noinspection resource
    return userManageMapper.pageUserBasicInfo(queryParams, queryParams.buildRowBounds()).toPageInfo();
  }

  @Override
  @Transactional
  public ResultVO<CreateOrgMemberDTO> createUserAndMember(CreateOrgMemberDTO orgMemberDTO) {
    Assert.notNull(orgMemberDTO.getUserInfo(), "成员用户信息不能为空");
    Assert.hasLength(orgMemberDTO.getUserInfo().getRealName(), "用户名称不能为空");
    Assert.hasLength(orgMemberDTO.getUserInfo().getUserName(), "用户编码不能为空");
    // 新增用户
    ResultVO<UserDTO> userSaveResult = saveUser(orgMemberDTO.getUserInfo());
    if (!"0".equals(userSaveResult.getResultCode())) {
      return new ResultVO<>(userSaveResult);
    }
    // 新增组织成员
    orgMemberService.createOrganizationMember(orgMemberDTO.getSpaceId(), orgMemberDTO.getOrgId(), orgMemberDTO.getUserInfo().getUserId());
    return ResultVO.success(orgMemberDTO);
  }

  /**
   * 获取最后一次登录成功日志
   */
  @Nullable
  private LitchiLogDTO getUserLastLoginLog(Long userId) {
    LitchiLogDTO queryParam = new LitchiLogDTO();
    queryParam.setEventCode(LitchiAuthLogServiceImpl.LitchiLogEvent.LOGIN_SUCCESS.getEventCode());
    queryParam.setPartyId(userId);
    PageInfo<LitchiLogDTO> page = authLogService.queryAuthLogPage(queryParam, 1, 1);
    return CollectionUtils.isNotEmpty(page.getList()) ? page.getList().get(0) : null;
  }

  @Override
  public SimpleUserDTO findSimpleUser(Long userId) {
    SimpleUserDTO user = userManageMapper.getSimpleUserDTO(userId);
    Assert.notNull(user, () -> "用户不存在: " + userId);
    return user;
  }

  /**
   * 校验用户编码
   */
  private void checkUserCode(String userCode) {
    boolean checkSuccess = StringUtils.isNotEmpty(userCode) && userCode.matches(USER_CODE_REGEX);
    Assert.isTrue(checkSuccess, "用户编码请使用字母、数字、下划线、中线或它们的组合");
  }

  /**
   * 校验密码复杂度
   */
  private void checkPassword(@Nullable String password) {
    boolean checkSuccess = StringUtils.isNotEmpty(password) && PasswordRuleUtil.additionalCredentialsChecks(password);
    Assert.isTrue(checkSuccess, "密码不符合安全规则");
  }

  /**
   * 保存用户账号事件日志
   */
  private void saveAccountEventLog(AccountEventTypeEnum eventType, UserDTO userDTO) {
    Map<String, Object> map = new HashMap<>();
    map.put("userId", userDTO.getUserId());
    map.put("userCode", userDTO.getUserName());
    map.put("userName", userDTO.getRealName());
    accountEventLogService.addAccountEventLog(eventType, JsonUtil.toJsonString(map));
  }

  /**
   * 保存用户密码历史
   */
  private void saveUserPwdHis(Long userId, String password, Long operatorId) {
    UserPwdHisEntity pwdHisEntity = new UserPwdHisEntity();
    pwdHisEntity.setHisId(Sequences.USER_PWD_HIST_ID.next());
    pwdHisEntity.setUserId(userId);
    pwdHisEntity.setPassword(password);
    pwdHisEntity.setStatusCd(BaseConsts.STATUS_CD_VALID);
    pwdHisEntity.setCreatorId(operatorId);
    pwdHisEntity.setUpdatorId(operatorId);
    userPwdHisMapper.insertUserPwdHis(pwdHisEntity);
  }

  /**
   * 校验用户密码历史重复
   */
  private void checkUserPwdHisRepeat(Long userId, String password) {
    Integer pwdHisNum = SystemParameter.PWD_HIS_NUM.getIntegerValueFromDb();
    if (pwdHisNum != null && pwdHisNum > 0) {
      RowBounds rowBounds = new RowBounds(0, pwdHisNum > 20 ? 20 : pwdHisNum);
      // noinspection resource
      PageInfo<UserPwdHisDTO> pageInfo = userPwdHisMapper.selectPageByUserId(userId, rowBounds).toPageInfo();
      // 校验密码历史，防止用户使用近期使用过的密码
      if (StringUtils.isNotBlank(password) && CollectionUtils.isNotEmpty(pageInfo.getList())) {
        for (UserPwdHisDTO pwdHisDTO : pageInfo.getList()) {
          if (passwordEncoder.matches(password, pwdHisDTO.getPassword())) {
            throw BaseErrorConstant.USER_MODIFY_PWD_USED.toException();
          }
        }
      }
    }
  }

}
