package com.iwhalecloud.bote.service.app.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.dto.app.WebAppDTO;
import com.iwhalecloud.bote.dto.app.WorkbenchAppAuthDTO;
import com.iwhalecloud.bote.dto.app.WorkbenchAppDTO;
import com.iwhalecloud.bote.dto.app.WorkbenchAppRelDTO;
import com.iwhalecloud.bote.dto.app.query.WorkbenchAppQueryParams;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationUserDTO;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import com.iwhalecloud.bote.mapper.app.WebAppMapper;
import com.iwhalecloud.bote.mapper.app.WorkbenchAppAuthMapper;
import com.iwhalecloud.bote.mapper.app.WorkbenchAppMapper;
import com.iwhalecloud.bote.mapper.app.WorkbenchAppRelMapper;
import com.iwhalecloud.bote.mapper.bot.BotManageMapper;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bote.mapper.organization.OrgUserRoleMapper;
import com.iwhalecloud.bote.service.app.IWorkbenchAppService;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.service.organization.IOrganizationMemberService;
import com.iwhalecloud.bote.service.portal.ITenantManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工作台应用服务实现
 *
 * @author tingyun.wang
 * @since 2025-09-08
 */
@Service
@RequiredArgsConstructor
public class WorkbenchAppServiceImpl implements IWorkbenchAppService {

  private final WorkbenchAppMapper workbenchAppMapper;
  private final WorkbenchAppRelMapper appRelMapper;
  private final BotQueryMapper botQueryMapper;
  private final BotManageMapper botManageMapper;
  private final WebAppMapper webAppMapper;
  private final WorkbenchAppAuthMapper appAuthMapper;
  private final IOrganizationMemberService orgMemberService;
  private final ITenantManageService tenantManageService;
  private final OrgUserRoleMapper orgUserRoleMapper;

  @Override
  @Transactional
  public ResultVO<WorkbenchAppDTO> saveWorkbenchApp(WorkbenchAppDTO appDTO) {
    Assert.notNull(appDTO.getSpaceId(), "企业空间 ID 不能为空");
    // 补充数据
    Long userId = SessionUtil.getLoginInfo().getUserId();
    appDTO.setUpdatorId(userId);
    if (appDTO.getWorkbenchAppId() == null) {
      appDTO.setCreatorId(userId);
      appDTO.setStatusCd(BaseConsts.STATUS_CD_VALID);
      appDTO.setAppStatus(BaseConsts.STATE_DISABLE);
      appDTO.setAppSettingStatus(BaseConsts.FALSE);
      appDTO.setAppScene(BaseConsts.WORKBENCH_APP_SCENE_DIALOGUE);
    }

    // 处理更新操作
    WorkbenchAppDTO oldApp = null;
    if (appDTO.getWorkbenchAppId() != null) {
      oldApp = getWorkbenchApp(appDTO.getWorkbenchAppId(), appDTO.getSpaceId());
      Assert.notNull(oldApp, "应用不存在，workbenchAppId=" + appDTO.getWorkbenchAppId());
      // 检查应用是否已启用
      if (BaseConsts.STATE_ENABLE.equals(oldApp.getAppStatus())) {
        throw new BssException("应用已启用，请停用后再修改。");
      }
    }

    // 保存应用数据
    DataDifference<WorkbenchAppDTO> difference = DataDifferenceStarter.computeSaveAndLog(oldApp, appDTO, false,
        appDTO.getSpaceId(), OperClassEnum.WORKBENCH_APP);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }

    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  public WorkbenchAppDTO queryAppSettings(Long workbenchAppId, Long spaceId) {
    // 查询工作应用相关信息
    WorkbenchAppDTO appDTO = workbenchAppMapper.selectAppSetting(workbenchAppId, spaceId);
    if (appDTO == null) {
      return null;
    }
    // 查询关联的应用
    List<WorkbenchAppRelDTO> relDTOList = appRelMapper.selectByWorkbenchAppId(workbenchAppId, spaceId);
    if (CollectionUtils.isNotEmpty(relDTOList)) {
      // 获取AI助理应用信息列表
      List<WorkbenchAppRelDTO> aiBotRelList = relDTOList.stream().filter(dto -> BaseConsts.APP_TYPE_AI_BOT.equals(dto.getRelAppType()))
        .toList();
      if (CollectionUtils.isNotEmpty(aiBotRelList)) {
        appDTO.setRelAiAppList(queryAiBotList(aiBotRelList));
      }
      // 获取网页应用信息
      Long webAppId = relDTOList.stream().filter(dto -> BaseConsts.APP_TYPE_WEP_APP.equals(dto.getRelAppType()))
          .map(WorkbenchAppRelDTO::getRelAppId).findFirst().orElse(null);
      if (webAppId != null) {
        WebAppDTO webAppDTO = webAppMapper.selectWebAppBasicInfo(webAppId, spaceId);
        appDTO.setRelWebApp(webAppDTO);
        appDTO.setRelWebAppId(webAppId);
      }
    }
    // 处理自定义授权列表
    if (BaseConsts.WORKBENCH_APP_AUTH_TYPE_CUSTOM.equals(appDTO.getAuthType())) {
      setCustomAuthInfo(appDTO);
    }
    return appDTO;
  }

  /**
   * 查询关联的AI助理列表
   */
  private List<SimpleBotDTO> queryAiBotList(List<WorkbenchAppRelDTO> relAiBotList) {
    if (CollectionUtils.isEmpty(relAiBotList)) {
      return Collections.emptyList();
    }
    // 由于关联的AI助理可能来自不同租户，所以需要单个查询
    return relAiBotList.stream()
      .map(appRelDTO -> botQueryMapper.selectSimpleBot(appRelDTO.getTenantId(), appRelDTO.getRelAppId()))
      .toList();
  }

  /**
   * 设置工作台应用的自定义授权信息
   */
  private void setCustomAuthInfo(WorkbenchAppDTO appDTO) {
    List<OrganizationUserDTO> authOrgUserList = appAuthMapper.selectByWorkbenchAppId(appDTO.getWorkbenchAppId(), appDTO.getSpaceId());
    if (CollectionUtils.isNotEmpty(authOrgUserList)) {
      appDTO.setAuthUserList(authOrgUserList.stream()
          .filter(authInfo -> authInfo.getUserId() != null && authInfo.getUserId() != -1L).toList());
      appDTO.setAuthOrgList(authOrgUserList.stream()
          .filter(authInfo -> authInfo.getOrgId() != null).toList());
    }
  }

  @Override
  @Transactional
  public void saveAppSettings(WorkbenchAppDTO appDTO) {
    Assert.notNull(appDTO.getWorkbenchAppId(), "应用ID不能为空");
    Assert.notNull(appDTO.getSpaceId(), "企业空间ID不能为空");
    Assert.hasText(appDTO.getAppScene(), "应用场景不能为空");

    // 已启用状态下不允许修改
    WorkbenchAppDTO oldApp = workbenchAppMapper.selectAppBasicInfo(appDTO.getWorkbenchAppId(), appDTO.getSpaceId());
    Assert.notNull(oldApp, "应用不存在，workbenchAppId=" + appDTO.getWorkbenchAppId());
    if (BaseConsts.STATE_ENABLE.equals(oldApp.getAppStatus())) {
      throw new BssException("应用已启用，请停用后再修改。");
    }

    // 修改关联的AI助理的更新时间
    modifyRelAiBotTime(appDTO.getWorkbenchAppId(), appDTO.getSpaceId());

    Long userId = SessionUtil.getLoginInfo().getUserId();
    // 移除旧的应用能力关联配置 todo 优化处理逻辑
    appRelMapper.deleteByWorkbenchAppId(appDTO.getWorkbenchAppId(), appDTO.getSpaceId(), userId);
    // 保存新的应用能力关联配置
    boolean appSettingStatus = false;
    switch (appDTO.getAppScene()) {
      case BaseConsts.WORKBENCH_APP_SCENE_DIALOGUE:
        appSettingStatus = saveDialogScene(appDTO);
        break;
      case BaseConsts.WORKBENCH_APP_SCENE_WEBAPP:
        appSettingStatus = saveWebAppScene(appDTO);
        break;
      case BaseConsts.WORKBENCH_APP_SCENE_GROUPCHAT:
        appSettingStatus = saveGroupChatScene(appDTO);
        break;
      case BaseConsts.WORKBENCH_APP_SCENE_SMARTAPP:
        appSettingStatus = saveSmartAppScene(appDTO);
        break;
      default: break;
    }

    // 处理应用授权
    doSaveAppAuth(appDTO);

    // 保存应用能力基本配置信息
    appDTO.setUpdatorId(userId);
    appDTO.setAppSettingStatus(appSettingStatus ? BaseConsts.TRUE : BaseConsts.FALSE);
    workbenchAppMapper.updateAppSettingInfo(appDTO);
  }

  /**
   * 保存单对话应用场景能力配置
   */
  private boolean saveDialogScene(WorkbenchAppDTO appDTO) {
    // 单对话应用场景下，只需配置一个AI助理
    if (appDTO.getAppRelObj() != null && CollectionUtils.isNotEmpty(appDTO.getAppRelObj().getAiAppList())) {
      SimpleBotDTO aiBot = appDTO.getAppRelObj().getAiAppList().getFirst();
      // 检查目标AI助理是否已被关联
      if (appRelMapper.existByRelAppId(aiBot.getBotId(), appDTO.getSpaceId(), aiBot.getTenantId())) {
        throw new BssException("AI助理已被其它应用关联");
      }
      // 保存应用关联关系
      WorkbenchAppRelDTO appRelDTO = createWorkbenchAppRelDTO(appDTO.getWorkbenchAppId(), aiBot.getBotId(), BaseConsts.APP_TYPE_AI_BOT,
        appDTO.getSpaceId(), aiBot.getTenantId());
      appRelMapper.insertAppRelBatch(Collections.singletonList(appRelDTO));
      // 单对话应用场景下配置了AI助理，则返回应用为已配置状态
      return true;
    }
    return false;
  }

  /**
   * 保存网页应用场景能力配置
   */
  private boolean saveWebAppScene(WorkbenchAppDTO appDTO) {
    // 网页应用场景下，只需配置网页应用
    if (appDTO.getAppRelObj() != null && appDTO.getAppRelObj().getWebAppId() != null) {
      // 保存应用关联关系
      WorkbenchAppRelDTO appRelDTO = createWorkbenchAppRelDTO(appDTO.getWorkbenchAppId(), appDTO.getAppRelObj().getWebAppId(),
          BaseConsts.APP_TYPE_WEP_APP, appDTO.getSpaceId(), null);
      appRelMapper.insertAppRelBatch(Collections.singletonList(appRelDTO));
      // 网页应用场景下配置了网页应用，返回应用为已配置状态
      return true;
    }
    return false;
  }

  /**
   * 保存群聊场景能力配置
   */
  private boolean saveGroupChatScene(WorkbenchAppDTO appDTO) {
    // 群聊应用场景下，需配置多个AI助理
    if (appDTO.getAppRelObj() != null && CollectionUtils.isNotEmpty(appDTO.getAppRelObj().getAiAppList())) {
      // 保存应用关联关系
      List<WorkbenchAppRelDTO> appRelList = new ArrayList<>();
      for (SimpleBotDTO aiBot : appDTO.getAppRelObj().getAiAppList()) {
        // 检查目标AI助理是否已被关联
        if (appRelMapper.existByRelAppId(aiBot.getBotId(), appDTO.getSpaceId(), aiBot.getTenantId())) {
          throw new BssException(String.format("AI助理【%s-%d】已被其它应用关联", aiBot.getBotName(), aiBot.getBotId()));
        }
        WorkbenchAppRelDTO appRelDTO = createWorkbenchAppRelDTO(appDTO.getWorkbenchAppId(), aiBot.getBotId(), BaseConsts.APP_TYPE_AI_BOT,
          appDTO.getSpaceId(), aiBot.getTenantId());
        appRelList.add(appRelDTO);
      }
      appRelMapper.insertAppRelBatch(appRelList);
      // 群聊应用场景下，如果配置的AI助理2个及以上才返回应用为已配置状态
      return CollectionUtils.size(appDTO.getAppRelObj().getAiAppList()) > 1;
    }
    return false;
  }

  /**
   * 保存智能应用场景能力配置
   */
  private boolean saveSmartAppScene(WorkbenchAppDTO appDTO) {
    // 智能应用场景需要配置AI助理+网页应用，AI助理支持多个
    if (appDTO.getAppRelObj() != null) {
      List<WorkbenchAppRelDTO> appRelList = new ArrayList<>();
      // 添加AI助理应用关联对象
      if (CollectionUtils.isNotEmpty(appDTO.getAppRelObj().getAiAppList())) {
        for (SimpleBotDTO aiBot : appDTO.getAppRelObj().getAiAppList()) {
          // 检查目标AI助理是否已被关联
          if (appRelMapper.existByRelAppId(aiBot.getBotId(), appDTO.getSpaceId(), aiBot.getTenantId())) {
            throw new BssException("AI助理已被其它应用关联");
          }
          WorkbenchAppRelDTO appRelDTO = createWorkbenchAppRelDTO(appDTO.getWorkbenchAppId(), aiBot.getBotId(), BaseConsts.APP_TYPE_AI_BOT,
            appDTO.getSpaceId(), aiBot.getTenantId());
          appRelList.add(appRelDTO);
        }
      }
      // 添加网页应用关联对象
      if (appDTO.getAppRelObj().getWebAppId() != null) {
        WorkbenchAppRelDTO appRelDTO = createWorkbenchAppRelDTO(appDTO.getWorkbenchAppId(), appDTO.getAppRelObj().getWebAppId(),
            BaseConsts.APP_TYPE_WEP_APP, appDTO.getSpaceId(), null);
        appRelList.add(appRelDTO);
      }
      // 保存应用关联对象关系
      if (!appRelList.isEmpty()) {
        appRelMapper.insertAppRelBatch(appRelList);
      }
    }

    // 只有同时配置了AI助理+网页应用，才返回应用为已配置状态
    return appDTO.getAppRelObj() != null && CollectionUtils.isNotEmpty(appDTO.getAppRelObj().getAiAppList())
        && appDTO.getAppRelObj().getWebAppId() != null;
  }

  /**
   * 构建应用能力关联对象
   */
  private WorkbenchAppRelDTO createWorkbenchAppRelDTO(Long workBenchAppId, Long relAppId, String relAppType, Long spaceId, Long tenantId) {
    WorkbenchAppRelDTO appRelDTO = new WorkbenchAppRelDTO();
    appRelDTO.setRelId(Sequences.WORKBENCH_APP_REL_ID.next());
    appRelDTO.setWorkbenchAppId(workBenchAppId);
    appRelDTO.setRelAppId(relAppId);
    appRelDTO.setRelAppType(relAppType);
    appRelDTO.setSpaceId(spaceId);
    appRelDTO.setTenantId(tenantId);
    appRelDTO.setStatusCd(BaseConsts.STATUS_CD_VALID);
    appRelDTO.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    appRelDTO.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    return appRelDTO;
  }

  /**
   * 执行保存应用授权信息
   */
  private void doSaveAppAuth(WorkbenchAppDTO appDTO) {
    // 处理应用授权 todo 优化逻辑
    appAuthMapper.deleteByWorkbenchAppId(appDTO.getWorkbenchAppId(), appDTO.getSpaceId(), SessionUtil.getLoginInfo().getUserId());
    if (BaseConsts.WORKBENCH_APP_AUTH_TYPE_ALL.equals(appDTO.getAuthType())) {
      // 全部成员授权
      WorkbenchAppAuthDTO authDTO = buildAppAuthDTO(appDTO.getWorkbenchAppId(), -1L, null, appDTO.getSpaceId());
      appAuthMapper.insertAppAuth(authDTO);
    }
    else {
      // 自定义授权
      saveCustomAuth(appDTO);
    }
  }

  /**
   * 处理自定义授权
   */
  private void saveCustomAuth(WorkbenchAppDTO appDTO) {
    if (appDTO == null || appDTO.getAppAuthObj() == null) {
      return;
    }
    List<WorkbenchAppAuthDTO> authDTOList = new ArrayList<>();
    // 授权用户列表
    if (CollectionUtils.isNotEmpty(appDTO.getAppAuthObj().getUserIdList())) {
      for (Long userId : appDTO.getAppAuthObj().getUserIdList()) {
        authDTOList.add(buildAppAuthDTO(appDTO.getWorkbenchAppId(), userId, null, appDTO.getSpaceId()));
      }
    }
    // 授权组织列表
    if (CollectionUtils.isNotEmpty(appDTO.getAppAuthObj().getOrgIdList())) {
      for (Long orgId : appDTO.getAppAuthObj().getOrgIdList()) {
        authDTOList.add(buildAppAuthDTO(appDTO.getWorkbenchAppId(), null, orgId, appDTO.getSpaceId()));
      }
    }
    // 保存授权列表
    if (!authDTOList.isEmpty()) {
      appAuthMapper.insertAppAuthBatch(authDTOList);
    }
  }

  @Override
  public WorkbenchAppDTO queryAuthInfo(Long workbenchAppId, Long spaceId) {
    // 查询工作应用相关信息
    WorkbenchAppDTO appDTO = workbenchAppMapper.selectAppSetting(workbenchAppId, spaceId);
    if (appDTO == null) {
      return null;
    }
    // 处理自定义授权列表
    if (BaseConsts.WORKBENCH_APP_AUTH_TYPE_CUSTOM.equals(appDTO.getAuthType())) {
      setCustomAuthInfo(appDTO);
    }
    return appDTO;
  }

  @Override
  @Transactional
  public void saveAuthInfo(WorkbenchAppDTO appDTO) {
    Assert.notNull(appDTO.getWorkbenchAppId(), "应用ID不能为空");
    Assert.notNull(appDTO.getSpaceId(), "企业空间ID不能为空");

    // 已启用状态下不允许修改
    WorkbenchAppDTO oldApp = workbenchAppMapper.selectAppBasicInfo(appDTO.getWorkbenchAppId(), appDTO.getSpaceId());
    Assert.notNull(oldApp, "应用不存在，workbenchAppId=" + appDTO.getWorkbenchAppId());

    Long userId = SessionUtil.getLoginInfo().getUserId();
    doSaveAppAuth(appDTO);

    // 保存应用的授权类型相关信息
    appDTO.setUpdatorId(userId);
    workbenchAppMapper.updateAppSettingInfo(appDTO);
  }

  /**
   * 构建应用授权DTO
   */
  private WorkbenchAppAuthDTO buildAppAuthDTO(Long workbenchAppId, Long authUserId, Long orgId, Long spaceId) {
    WorkbenchAppAuthDTO authDTO = new WorkbenchAppAuthDTO();
    authDTO.setAuthId(Sequences.WORKBENCH_APP_AUTH_ID.next());
    authDTO.setWorkbenchAppId(workbenchAppId);
    authDTO.setUserId(authUserId);
    authDTO.setOrgId(orgId);
    authDTO.setSpaceId(spaceId);
    authDTO.setStatusCd(BaseConsts.STATUS_CD_VALID);
    authDTO.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    authDTO.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    return authDTO;
  }

  @Override
  public PageInfo<WorkbenchAppDTO> queryWorkbenchAppPage(WorkbenchAppQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    //noinspection resource
    return workbenchAppMapper.selectWorkbenchAppPage(queryParams, rowBounds).toPageInfo();
  }

  @Override
  public WorkbenchAppDTO getWorkbenchApp(Long workbenchAppId, Long spaceId) {
    return workbenchAppMapper.selectWorkbenchApp(workbenchAppId, spaceId);
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteWorkbenchApp(Long workbenchAppId, Long spaceId) {
    workbenchAppMapper.deleteWorkbenchApp(workbenchAppId, spaceId, SessionUtil.getLoginInfo().getUserId());
    // 修改关联的AI助理的更新时间
    modifyRelAiBotTime(workbenchAppId, spaceId);
    return ResultVO.success();
  }

  @Override
  @Transactional
  public void enabledWorkbenchApp(Long workbenchAppId, Long spaceId) {
    // 检查是否配置完应用能力
    String appSettingStatus = workbenchAppMapper.getAppSettingStatus(workbenchAppId, spaceId);
    if (!BaseConsts.TRUE.equals(appSettingStatus)) {
      throw new BssException("当前应用能力信息还未配置完成，请先配置完成后再启用！");
    }
    // 更新应用状态为启用
    WorkbenchAppDTO updateDto = new WorkbenchAppDTO();
    updateDto.setWorkbenchAppId(workbenchAppId);
    updateDto.setSpaceId(spaceId);
    updateDto.setAppStatus(BaseConsts.STATE_ENABLE);
    updateDto.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    workbenchAppMapper.updateWorkbenchAppStatus(updateDto);
    // 修改关联的AI助理的更新时间
    modifyRelAiBotTime(workbenchAppId, spaceId);
  }

  @Override
  @Transactional
  public void disabledWorkbenchApp(Long workbenchAppId, Long spaceId) {
    // 更新应用状态为停用
    WorkbenchAppDTO updateDto = new WorkbenchAppDTO();
    updateDto.setWorkbenchAppId(workbenchAppId);
    updateDto.setSpaceId(spaceId);
    updateDto.setAppStatus(BaseConsts.STATE_DISABLE);
    updateDto.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    workbenchAppMapper.updateWorkbenchAppStatus(updateDto);
    // 修改关联的AI助理的更新时间
    modifyRelAiBotTime(workbenchAppId, spaceId);
  }

  /**
   * 修改关联的AI助理应用的更新时间（刷新Bot的请求缓存）
   */
  private void modifyRelAiBotTime(Long workbenchAppId, Long spaceId) {
    // 查询关联的应用
    List<WorkbenchAppRelDTO> relDTOList = appRelMapper.selectByWorkbenchAppId(workbenchAppId, spaceId);
    if (CollectionUtils.isNotEmpty(relDTOList)) {
      // 获取AI助理应用信息列表
      List<WorkbenchAppRelDTO> aiAppList = relDTOList.stream().filter(dto -> BaseConsts.APP_TYPE_AI_BOT.equals(dto.getRelAppType())).toList();
      if (CollectionUtils.isNotEmpty(aiAppList)) {
        for (WorkbenchAppRelDTO relAiBot : aiAppList) {
          botManageMapper.modifyBotUpdateTime(relAiBot.getTenantId(), relAiBot.getRelAppId(), SessionUtil.getLoginInfo().getUserId());
        }
      }
    }
  }

  @Override
  public String getWorkbenchAppIcon(Long workbenchAppId, Long spaceId) {
    String appIcon = workbenchAppMapper.selectWorkbenchAppIcon(spaceId, workbenchAppId);
    return appIcon == null ? "" : appIcon;
  }

  @Override
  public PageInfo<WorkbenchAppDTO> queryAuthWorkbenchAppPage(WorkbenchAppQueryParams params) {
    Assert.notNull(params.getSpaceId(), "企业空间ID不能为空");
    Long userId = SessionUtil.getLoginInfo().getUserId();
    PageInfo<WorkbenchAppDTO> pageInfo;
    // 平台管理员和企业管理员查询全部已启用的工作台应用
    if (SessionUtil.isSuperAdmin(userId) || orgUserRoleMapper.checkIsOrgAdmin(params.getSpaceId(), userId)) {
      params.setAppStatus(BaseConsts.STATE_ENABLE);
      // noinspection resource
      pageInfo = workbenchAppMapper.selectWorkbenchAppPage(params, params.buildRowBounds()).toPageInfo();
    }
    else {
      // 获取用户所属的组织及其父组织ID列表
      params.setUserId(userId);
      params.setOrgIdList(orgMemberService.queryUserOrgAndParentOrgIds(params.getSpaceId(), params.getUserId()));
      //noinspection resource
      pageInfo = workbenchAppMapper.selectAuthWorkbenchAppPage(params, params.buildRowBounds()).toPageInfo();
    }
    if (CollectionUtils.isNotEmpty(pageInfo.getList())) {
      // 查询关联应用
      List<Long> appIdList = pageInfo.getList().stream().map(WorkbenchAppDTO::getWorkbenchAppId).toList();
      List<WorkbenchAppRelDTO> relDTOList = appRelMapper.selectByWorkbenchAppIds(appIdList, params.getSpaceId());
      if (CollectionUtils.isNotEmpty(relDTOList)) {
        // 设置AI助理应用信息
        fillAiBotInfo(pageInfo.getList(), relDTOList);
      }
    }

    return pageInfo;
  }

  /**
   * 填充关联的AI助理信息
   */
  private void fillAiBotInfo(List<WorkbenchAppDTO> appDTOList, List<WorkbenchAppRelDTO> relDTOList) {
    // 过滤AI助理列表
    Map<Long, List<WorkbenchAppRelDTO>> aiAppIdMap = relDTOList.stream()
        .filter(dto -> BaseConsts.APP_TYPE_AI_BOT.equals(dto.getRelAppType()))
        .collect(Collectors.groupingBy(
            WorkbenchAppRelDTO::getWorkbenchAppId
        ));
    // 填充AI助理应用信息
    for (WorkbenchAppDTO appDTO : appDTOList) {
      List<WorkbenchAppRelDTO> relAiBotList = aiAppIdMap.get(appDTO.getWorkbenchAppId());
      if (CollectionUtils.isNotEmpty(relAiBotList)) {
        // 查询AI助理应用信息
        appDTO.setRelAiAppList(queryAiBotList(relAiBotList));
      }
    }
  }

  /**
   * 填充关联的网页应用信息
   */
  private void fillWebAppInfo(List<WorkbenchAppDTO> appDTOList, List<WorkbenchAppRelDTO> relDTOList, Long spaceId) {
    // 过滤网页应用列表
    Map<Long, Long> webAppIdMap = relDTOList.stream()
        .filter(dto -> BaseConsts.APP_TYPE_WEP_APP.equals(dto.getRelAppType()))
        .collect(Collectors.toMap(
            WorkbenchAppRelDTO::getWorkbenchAppId,
            WorkbenchAppRelDTO::getRelAppId
        ));
    // 填充网页应用信息
    for (WorkbenchAppDTO appDTO : appDTOList) {
      Long webAppId = webAppIdMap.get(appDTO.getWorkbenchAppId());
      if (webAppId != null) {
        WebAppDTO webAppDTO = webAppMapper.selectWebAppBasicInfo(webAppId, spaceId);
        appDTO.setRelWebApp(webAppDTO);
        appDTO.setRelWebAppId(webAppId);
      }
    }
  }

  @Override
  public WorkbenchAppDTO getAuthWorkbenchAppDetail(Long workbenchAppId, Long spaceId) {
    return workbenchAppMapper.selectAuthWorkbenchAppDetail(workbenchAppId, spaceId);
  }

  @Override
  public WorkbenchAppDTO queryWorkbenchAppRelInfo(Long workbenchAppId, Long spaceId) {
    // 查询工作台应用基本信息
    WorkbenchAppDTO appDTO = workbenchAppMapper.selectAppBasicInfo(workbenchAppId, spaceId);
    Assert.notNull(appDTO, "工作台应用不存在");
    // 查询关联的应用能力信息
    List<WorkbenchAppRelDTO> relDTOList = appRelMapper.selectByWorkbenchAppId(workbenchAppId, spaceId);
    if (CollectionUtils.isNotEmpty(relDTOList)) {
      // 设置AI助理应用信息
      fillAiBotInfo(Collections.singletonList(appDTO), relDTOList);
      // 设置网页应用信息
      fillWebAppInfo(Collections.singletonList(appDTO), relDTOList, spaceId);
    }
    return appDTO;
  }

  @Override
  public WorkbenchAppRelDTO checkAppPublishedStatus(Long tenantId, Long botId) {
    return appRelMapper.selectByRelAppIdAndType(tenantId, botId, BaseConsts.APP_TYPE_AI_BOT);
  }

  @Override
  @Transactional
  public void unpublishApp(Long tenantId, Long botId) {
    // 先查询关联信息，获取工作台应用ID
    WorkbenchAppRelDTO relInfo = appRelMapper.selectByRelAppIdAndType(tenantId, botId, BaseConsts.APP_TYPE_AI_BOT);
    if (relInfo != null && relInfo.getWorkbenchAppId() != null) {
      // 将bt_workbench_app_rel表中对应的状态修改为00X
      appRelMapper.unpublishByRelAppIdAndType(tenantId, botId, BaseConsts.APP_TYPE_AI_BOT);
      TenantDTO tenant = tenantManageService.getTenant(tenantId);
      Assert.notNull(tenant, "租户不存在");
      // 将bt_workbench_app表中对应的app_status修改为X
      appRelMapper.updateAppStatusToDisabled(tenant.getSpaceId(), relInfo.getWorkbenchAppId());
    }
  }

}
