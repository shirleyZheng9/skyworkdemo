package com.iwhalecloud.bote.service.portal.tiangong;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import com.iwhalecloud.bote.dto.portal.TenantUserDTO;
import com.iwhalecloud.bote.dto.portal.UserDTO;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserItem;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserRemoveRequest;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserSyncResponse;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserSyncResponse.UserAction;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserUpsertRequest;
import com.iwhalecloud.bote.entity.portal.UserEntity;
import com.iwhalecloud.bote.mapper.portal.TenantManageMapper;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import com.iwhalecloud.bote.portal.config.properties.TiangongProjectSyncProperties;
import com.iwhalecloud.bote.service.portal.ITenantManageService;
import com.iwhalecloud.bote.service.portal.IUserManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 天工项目成员同步事务工作器
 *
 * @author zhengxueli
 * @since 2024-09-23
 * <p>
 * 在一个事务内完成项目定位、天工业务用户解析或创建、成员授权和创建人绑定；由门面服务在事务外持锁。
 */
@Service
@RequiredArgsConstructor
public class TiangongProjectUserSyncWorker {
  private final TenantManageMapper tenantManageMapper;
  private final UserManageMapper userManageMapper;
  private final IUserManageService userManageService;
  private final ITenantManageService tenantManageService;
  private final TiangongProjectSyncProperties properties;

  /**
   * 一条新增消息在同一事务内完成用户创建、成员授权和创建人绑定。
   * 任一步骤失败均抛出异常回滚，避免只创建用户却没有项目成员关系。
   */
  @Transactional
  public TiangongProjectUserSyncResponse upsert(TiangongProjectUserUpsertRequest request) {
    //在 BOTE 里对应哪个租户
    TenantDTO tenant = resolveTenant(request.getExternalSpaceId(), request.getExternalProjectId(), request.getBoteTenantId());
    Map<String, UserEntity> users = new LinkedHashMap<>();
    // 遍历所有天工用户 先解析本批用户，再写成员关系；创建人绑定复用同一批已确认的业务用户身份。
    for (TiangongProjectUserItem item : request.getUsers()) {
      users.put(item.getExtUserId(), resolveOrCreateUser(item));
    }

    TiangongProjectUserSyncResponse response = responseOf(tenant, request.getRequestId());
    //确保用户和 tenant 的成员关系存在
    for (TiangongProjectUserItem item : request.getUsers()) {
      UserEntity user = users.get(item.getExtUserId());
      UserAction action = new UserAction();
      action.setExtUserId(item.getExtUserId());
      action.setUserId(user.getUserId());
      //这个 BOTE 用户是否已经属于这个 tenant
      if (tenantManageMapper.getTenantUserByTenantIdAndUserId(tenant.getTenantId(), user.getUserId()) == null) {
        // 只有首次加入项目才授予 EDIT；原有成员权限交由已有记录保持。
        TenantUserDTO member = new TenantUserDTO();
        member.setTenantId(tenant.getTenantId());
        member.setUserId(user.getUserId());
        member.setUserRole(BaseConsts.ROLE_EDIT);
        //调用已有 tenant 成员管理逻辑，把用户加进去
        ResultVO<List<TenantUserDTO>> result = tenantManageService.saveTenantUserForInternalIntegration(Collections.singletonList(member));
        if (!result.isSuccess()) {
          throw new TiangongProjectSyncException("TG_MEMBER_UPSERT_FAILED", result.getResultMsg());
        }
        action.setMemberAction("CREATED");
      }
      else {
        // 已有 EDIT、MANAGE 等任何角色都不覆盖，尤其不能将项目管理员降级。
        action.setMemberAction("EXISTS");
      }
      response.getUsers().add(action);
    }
    //项目创建人绑定
    response.setCreatorAction(bindCreator(tenant, request.getCreatorExtUserId(), users));
    return response;
  }

  /**
   * 按天工业务用户身份移除成员；不存在的用户或关系按幂等语义返回 NOT_FOUND。
   * 创建人不能通过成员删除消息移除，以免项目归属与成员关系冲突。
   */
  @Transactional
  public TiangongProjectUserSyncResponse remove(TiangongProjectUserRemoveRequest request) {
    TenantDTO tenant = resolveTenant(request.getExternalSpaceId(), request.getExternalProjectId(), request.getBoteTenantId());
    TiangongProjectUserSyncResponse response = responseOf(tenant, request.getRequestId());
    response.setCreatorAction("NOT_REQUESTED");
    for (String extUserId : request.getExtUserIds()) {
      UserAction action = new UserAction();
      action.setExtUserId(extUserId);
      UserEntity user = findExistingUser(extUserId);
      if (user == null) {
        action.setMemberAction("NOT_FOUND");
      }
      else {
        action.setUserId(user.getUserId());
        if (tenantManageMapper.getTenantUserByTenantIdAndUserId(tenant.getTenantId(), user.getUserId()) == null) {
          action.setMemberAction("NOT_FOUND");
        }
        else if (user.getUserId().equals(tenant.getCreatorId())) {
          throw new TiangongProjectSyncException("TG_CREATOR_REMOVE_FORBIDDEN", "不允许通过成员移除接口删除项目创建人");
        }
        else {
          ResultVO<Void> result = tenantManageService.deleteTenantUserForInternalIntegration(tenant.getTenantId(),
            Collections.singletonList(user.getUserId()));
          if (!result.isSuccess()) {
            throw new TiangongProjectSyncException("TG_MEMBER_REMOVE_FAILED", result.getResultMsg());
          }
          action.setMemberAction("REMOVED");
        }
      }
      response.getUsers().add(action);
    }
    return response;
  }

  /**
   * 天工项目 ID 必须唯一映射到一个有效租户；外部空间和内部租户 ID 仅用于归属交叉校验。
   */
  private TenantDTO resolveTenant(String externalSpaceId, Long externalProjectId, Long boteTenantId) {
    List<TenantDTO> tenants = tenantManageMapper.selectActiveTiangongTenantsByProjectId(externalProjectId);
    if (tenants.isEmpty()) {
      throw new TiangongProjectSyncException("TG_PROJECT_NOT_FOUND", "未找到天工项目对应的 AADP 租户");
    }
    if (tenants.size() > 1) {
      throw new TiangongProjectSyncException("TG_PROJECT_AMBIGUOUS", "天工项目映射到多个 AADP 租户");
    }
    TenantDTO tenant = tenants.getFirst();
    if (boteTenantId != null && !boteTenantId.equals(tenant.getTenantId())) {
      throw new TiangongProjectSyncException("TG_TENANT_MISMATCH", "请求中的 boteTenantId 与项目映射不一致");
    }
    String mappedExternalSpaceId = tenantManageMapper.selectActiveExternalSpaceIdByTenantId(tenant.getTenantId());
    if (!Objects.equals(externalSpaceId, mappedExternalSpaceId)) {
      throw new TiangongProjectSyncException("TG_PROJECT_SPACE_MISMATCH", "请求中的 externalSpaceId 与项目所属空间不一致");
    }
    return tenant;
  }

  /**
   * 新用户只能写入天工业务门户。技术账号 Session 保持调用者身份，不能作为新用户的 systemCode。
   * 根据天工的 extUserId 找 BOTE 用户；有就直接复用，没有就创建，创建后再重新查一遍确认
   */
  private UserEntity resolveOrCreateUser(TiangongProjectUserItem item) {
    UserEntity user = findExistingUser(item.getExtUserId());
    if (user != null) {
      return user;
    }
    if (userManageMapper.existsUserName(properties.getBusinessUserSystemCode(), item.getExtUserId())) {
      throw new TiangongProjectSyncException("TG_USER_NAME_CONFLICT",
        "天工外部用户 ID 与已有正式用户登录名冲突");
    }
    UserDTO newUser = new UserDTO();
    newUser.setUserName(item.getExtUserId());
    newUser.setRealName(StringUtils.defaultIfBlank(item.getDisplayName(), item.getExtUserId()));
    newUser.setPhoneNo(StringUtils.defaultIfBlank(item.getPhoneNo(), null));
    newUser.setExtUserId(item.getExtUserId());
    newUser.setSystemCode(properties.getBusinessUserSystemCode());
    // 用户创建阶段不直接携带项目关系；后续通过受控成员逻辑补全默认租户和审计副作用。
    //当前这里只负责“创建用户本人”，不在创建用户的时候顺便把他塞到某个项目/租户里
    newUser.setTenantUserList(new ArrayList<>());
    ResultVO<UserDTO> result = userManageService.saveUser(newUser);
    if (!result.isSuccess()) {
      throw new TiangongProjectSyncException("TG_USER_CREATE_FAILED", result.getResultMsg());
    }
    // 重新按 systemCode + extUserId 查询，统一校验新旧用户的唯一性与启用状态。
    user = findExistingUser(item.getExtUserId());
    if (user == null) {
      throw new TiangongProjectSyncException("TG_USER_CREATE_FAILED", "创建天工业务用户后未查询到用户记录");
    }
    return user;
  }

  /**
   * 查询包含软删和禁用记录的全部匹配项。存在歧义或失效账号时拒绝同步，不任意选取一条。
   */
  private UserEntity findExistingUser(String extUserId) {
    List<UserEntity> users = userManageMapper.selectUsersBySystemCodeAndExtUserId(properties.getBusinessUserSystemCode(), extUserId);
    if (users.isEmpty()) {
      return null;
    }
    if (users.size() > 1) {
      throw new TiangongProjectSyncException("TG_USER_AMBIGUOUS", "天工外部用户 ID 对应多条 AADP 用户记录");
    }
    UserEntity user = users.getFirst();
    if (!BaseConsts.STATUS_CD_VALID.equals(user.getStatusCd()) || !BaseConsts.USER_STATE_ENABLE.equals(user.getUserState())) {
      throw new TiangongProjectSyncException("TG_USER_DISABLED", "天工业务用户已禁用或删除");
    }
    return user;
  }

  /**
   * 仅当项目创建人为空或仍为技术账号时改绑；已有其他业务创建人由条件更新保护。
   */
  private String bindCreator(TenantDTO tenant, String creatorExtUserId, Map<String, UserEntity> users) {
    if (StringUtils.isBlank(creatorExtUserId)) {
      return "NOT_REQUESTED";
    }
    Long creatorId = users.get(creatorExtUserId).getUserId();
    if (creatorId.equals(tenant.getCreatorId())) {
      return "UNCHANGED";
    }
    int updated = tenantManageMapper.bindCreatorIfAllowed(tenant.getTenantId(), creatorId, properties.getSyncUserId());
    if (updated != 1) {
      throw new TiangongProjectSyncException("TG_CREATOR_CONFLICT", "项目已有其他业务创建人，不允许覆盖");
    }
    return "BOUND";
  }

  /** 响应只回传定位到的内部租户 ID，便于 MQ 消费端追踪具体项目。 */
  private TiangongProjectUserSyncResponse responseOf(TenantDTO tenant, String requestId) {
    TiangongProjectUserSyncResponse response = new TiangongProjectUserSyncResponse();
    response.setRequestId(requestId);
    response.setTenantId(tenant.getTenantId());
    return response;
  }
}
