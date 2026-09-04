package com.iwhalecloud.bote.service.bot.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.bot.BotAuthDTO;
import com.iwhalecloud.bote.dto.bot.BotAuthApplyDTO;
import com.iwhalecloud.bote.dto.bot.query.BotAuthApplyParams;
import com.iwhalecloud.bote.dto.organization.OrganizationDTO;
import com.iwhalecloud.bote.dto.portal.SimpleUserDTO;
import com.iwhalecloud.bote.dto.workspace.SimpleWorkspaceDTO;
import com.iwhalecloud.bote.mapper.bot.BotAuthApplyMapper;
import com.iwhalecloud.bote.mapper.organization.OrganizationManageMapper;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import com.iwhalecloud.bote.mapper.workspace.WorkspaceManageMapper;
import com.iwhalecloud.bote.service.base.IToDoItemService;
import com.iwhalecloud.bote.service.bot.IBotManageService;
import com.iwhalecloud.bote.service.bot.IBotAuthApplyService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能应用授权申请 Service
 *
 * @author wang.tingyun
 * @since 2025-08-28
 */
@Service
@RequiredArgsConstructor
public class BotAuthApplyServiceImpl implements IBotAuthApplyService, IToDoItemService {

  private final IBotManageService botManageService;
  private final BotAuthApplyMapper botAuthApplyMapper;
  private final UserManageMapper userManageMapper;
  private final OrganizationManageMapper organizationManageMapper;
  private final WorkspaceManageMapper workspaceManageMapper;

  @Override
  @Transactional
  public Boolean applyBotAuth(BotAuthApplyDTO applyDTO) {
    // 未开启授权审核则走直接授权逻辑
    Boolean enableAudit = SystemParameter.BOT_PUBLISH_SQUARE_AUDIT_ENABLED.getBooleanValueFromDb();
    if (!enableAudit) {
      BotAuthDTO botAuthDTO = new BotAuthDTO();
      BeanUtils.copyProperties(applyDTO, botAuthDTO);
      botAuthDTO.setAuthPublisher(SessionUtil.getLoginInfo().getUserId());
      botManageService.authBot(botAuthDTO);
      return false;
    }
    // 补充参数
    fillApplyData(applyDTO);
    // 添加审核记录
    botAuthApplyMapper.insertBotAuthApply(applyDTO);
    return true;
  }

  /**
   * 补充授权申请必要的参数
   */
  private void fillApplyData(BotAuthApplyDTO applyDTO) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    applyDTO.setApplyId(Sequences.SEQ_BOT_AUTH_APPLY_ID.next());
    applyDTO.setUserId(userId);
    applyDTO.setCreatorId(userId);
    applyDTO.setUpdatorId(userId);
    applyDTO.setAuditStatus(BaseConsts.BOT_AUTH_APPLY_AUDIT_WAIT);
    applyDTO.setStatusCd(BaseConsts.STATUS_CD_VALID);
    // 转换自定义授权范围数据
    if (BaseConsts.BOT_AUTH_TYPE_CUSTOM.equals(applyDTO.getAuthType())) {
      if (applyDTO.getTenantIds() != null) {
        applyDTO.setAuthTenantIds(JsonUtil.toJsonString(applyDTO.getTenantIds()));
      }
      if (applyDTO.getUserIds() != null) {
        applyDTO.setAuthUserIds(JsonUtil.toJsonString(applyDTO.getUserIds()));
      }
      if (applyDTO.getAuthOrgIds() != null) {
        applyDTO.setOrgIds(JsonUtil.toJsonString(applyDTO.getAuthOrgIds()));
      }
    }
  }

  @Override
  public PageInfo<BotAuthApplyDTO> getBotAuditPage(BotAuthApplyParams params) {
    // noinspection resource
    return botAuthApplyMapper.selectBotAuditPage(params, params.buildRowBounds()).toPageInfo();
  }

  @Override
  public PageInfo<BotAuthApplyDTO> getBotApplyPage(BotAuthApplyParams params) {
    // noinspection resource
    PageInfo<BotAuthApplyDTO> pageInfo = botAuthApplyMapper.selectBotApplyPage(params, params.buildRowBounds()).toPageInfo();
    if (CollectionUtils.isNotEmpty(pageInfo.getList())) {
      pageInfo.getList().forEach(appDTO -> appDTO.setApplyTitle("应用发布到广场申请"));
    }
    return pageInfo;
  }

  @Override
  @Transactional
  public void auditBotAuthApply(BotAuthApplyParams params) {
    // 查询待审批的应用授权申请记录
    BotAuthApplyDTO applyDTO = botAuthApplyMapper.selectByApplyId(params.getApplyId());
    if (applyDTO == null) {
      throw new BssException("未找到待审批的应用授权申请记录，applyId=" + params.getApplyId());
    }
    // 更新审核状态
    params.setAuditUserId(SessionUtil.getLoginInfo().getUserId());
    int result = botAuthApplyMapper.updateBotAuditStatus(params);
    // 如果审核通过则走授权流程
    if (result == 1 && BaseConsts.BOT_AUTH_APPLY_AUDIT_APPROVE.equals(params.getAuditStatus())) {
      BotAuthDTO botAuthDTO = new BotAuthDTO();
      BeanUtils.copyProperties(applyDTO, botAuthDTO);
      botAuthDTO.setAuthPublisher(applyDTO.getUserId());
      // 处理自定义授权范围
      if (BaseConsts.BOT_AUTH_TYPE_CUSTOM.equals(applyDTO.getAuthType())) {
        if (StringUtils.isNotEmpty(applyDTO.getAuthTenantIds())) {
          botAuthDTO.setTenantIds(JsonUtil.parseJsonRequired(applyDTO.getAuthTenantIds(), new TypeReference<>() {
          }));
        }
        if (StringUtils.isNotEmpty(applyDTO.getAuthUserIds())) {
          botAuthDTO.setUserIds(JsonUtil.parseJsonRequired(applyDTO.getAuthUserIds(), new TypeReference<>() {
          }));
        }
        if (StringUtils.isNotEmpty(applyDTO.getOrgIds())) {
          botAuthDTO.setAuthOrgIds(JsonUtil.parseJsonRequired(applyDTO.getOrgIds(), new TypeReference<>() {
          }));
        }
      }
      botManageService.authBot(botAuthDTO);
    }
  }

  @Override
  public BotAuthApplyDTO getBotAuthApplyDetail(Long applyId) {
    return botAuthApplyMapper.selectDetailByApplyId(applyId);
  }

  @Override
  public BotAuthApplyDTO getBotAuthApply(Long applyId) {
    BotAuthApplyDTO applyDTO = botAuthApplyMapper.selectBasicInfoByApplyId(applyId);
    if (applyDTO != null) {
      Map<String, List<BotAuthDTO>> botAuthInfoMap = new HashMap<>();
      List<BotAuthDTO> botAuthAllList = new ArrayList<>();
      List<BotAuthDTO> botAuthCustomList = new ArrayList<>();
      // 授权所有人
      if (BaseConsts.BOT_AUTH_TYPE_ALL.equals(applyDTO.getAuthType())) {
        BotAuthDTO botAuthDTO = new BotAuthDTO();
        botAuthDTO.setAuthType(BaseConsts.BOT_AUTH_TYPE_ALL);
        botAuthDTO.setCatalogItemId(applyDTO.getCatalogItemId());
        botAuthDTO.setCatalogName(applyDTO.getCatalogName());
        botAuthAllList.add(botAuthDTO);
      }
      // 自定义授权：用户
      if (StringUtils.isNotEmpty(applyDTO.getAuthUserIds())) {
        List<Long> userIds = JsonUtil.parseJson(applyDTO.getAuthUserIds(), new TypeReference<>() { });
        if (CollectionUtils.isNotEmpty(userIds)) {
          List<SimpleUserDTO> userInfoList = userManageMapper.getSimpleUserList(userIds);
          userInfoList.forEach(user -> {
            BotAuthDTO botAuthDTO = new BotAuthDTO();
            botAuthDTO.setAuthType(BaseConsts.BOT_AUTH_TYPE_CUSTOM);
            botAuthDTO.setAuthSubType(BaseConsts.BOT_AUTH_TYPE_PART_USER);
            botAuthDTO.setUserId(user.getUserId());
            botAuthDTO.setUserName(user.getUserName());
            botAuthCustomList.add(botAuthDTO);
          });
        }
      }
      // 自定义授权：空间
      if (StringUtils.isNotEmpty(applyDTO.getAuthTenantIds())) {
        List<Long> tenantIds = JsonUtil.parseJson(applyDTO.getAuthTenantIds(), new TypeReference<>() { });
        if (CollectionUtils.isNotEmpty(tenantIds)) {
          List<SimpleWorkspaceDTO> workspaceList = workspaceManageMapper.selectWorkspacesByIds(tenantIds);
          workspaceList.forEach(tenant -> {
            BotAuthDTO botAuthDTO = new BotAuthDTO();
            botAuthDTO.setAuthType(BaseConsts.BOT_AUTH_TYPE_CUSTOM);
            botAuthDTO.setAuthSubType(BaseConsts.BOT_AUTH_TYPE_PART_TENANT);
            botAuthDTO.setAuthTenantId(tenant.getSpaceId());
            botAuthDTO.setTenantName(tenant.getSpaceName());
            botAuthCustomList.add(botAuthDTO);
          });
        }
      }
      // 自定义授权：组织
      if (StringUtils.isNotEmpty(applyDTO.getOrgIds())) {
        List<Long> orgIds = JsonUtil.parseJson(applyDTO.getOrgIds(), new TypeReference<>() { });
        if (CollectionUtils.isNotEmpty(orgIds)) {
          List<OrganizationDTO> orgInfoList = organizationManageMapper.getOrgBasicInfoByIds(orgIds);
          orgInfoList.forEach(org -> {
            BotAuthDTO botAuthDTO = new BotAuthDTO();
            botAuthDTO.setAuthType(BaseConsts.BOT_AUTH_TYPE_CUSTOM);
            botAuthDTO.setAuthSubType(BaseConsts.BOT_AUTH_TYPE_PART_ORG);
            botAuthDTO.setOrgId(org.getOrgId());
            botAuthDTO.setOrgName(org.getOrgName());
            botAuthCustomList.add(botAuthDTO);
          });
        }
      }
      // 第一条自定义授权记录补充目录信息
      if (!botAuthCustomList.isEmpty()) {
        BotAuthDTO firstCustomAuth = botAuthCustomList.getFirst();
        firstCustomAuth.setCatalogItemId(applyDTO.getCatalogItemId());
        firstCustomAuth.setCatalogName(applyDTO.getCatalogName());
      }
      // 填充授权信息
      botAuthInfoMap.put(BaseConsts.BOT_AUTH_TYPE_ALL, botAuthAllList);
      botAuthInfoMap.put(BaseConsts.BOT_AUTH_TYPE_CUSTOM, botAuthCustomList);
      applyDTO.setBotAuthInfoMap(botAuthInfoMap);
    }
    return applyDTO;
  }

  @Override
  public String getToDoItemType() {
    return "botAuthAudit";
  }

  @Override
  public Integer getToDoItemCount() {
    // 只有平台管理员才有应用审核待办
    Long userId = SessionUtil.getLoginInfo().getUserId();
    if (!SessionUtil.isSuperAdmin(userId)) {
      return 0;
    }
    return botAuthApplyMapper.countByAuthStatus(BaseConsts.BOT_AUTH_APPLY_AUDIT_WAIT);
  }

}
