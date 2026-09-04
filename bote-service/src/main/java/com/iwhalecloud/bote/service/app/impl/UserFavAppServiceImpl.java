package com.iwhalecloud.bote.service.app.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.app.BatchSaveUserFavAppDTO;
import com.iwhalecloud.bote.dto.app.UserFavAppDTO;
import com.iwhalecloud.bote.dto.app.query.UserFavAppQueryParams;
import com.iwhalecloud.bote.mapper.app.UserFavAppMapper;
import com.iwhalecloud.bote.mapper.app.WebAppMapper;
import com.iwhalecloud.bote.mapper.app.WorkbenchAppMapper;
import com.iwhalecloud.bote.mapper.app.WorkbenchAppRelMapper;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bote.mapper.organization.OrgUserRoleMapper;
import com.iwhalecloud.bote.service.app.IUserFavAppService;
import com.iwhalecloud.bote.service.organization.IOrganizationMemberService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 用户常用应用服务实现类
 *
 * @author wang.tingyun
 * @since 2025-09-12
 */
@Service
@RequiredArgsConstructor
public class UserFavAppServiceImpl implements IUserFavAppService {

  private final UserFavAppMapper userFavAppMapper;
  private final WorkbenchAppMapper workbenchAppMapper;
  private final WorkbenchAppRelMapper appRelMapper;
  private final BotQueryMapper botQueryMapper;
  private final WebAppMapper webAppMapper;
  private final OrgUserRoleMapper orgUserRoleMapper;
  private final IOrganizationMemberService orgMemberService;

  @Override
  public PageInfo<UserFavAppDTO> queryUserFavAppPage(UserFavAppQueryParams params) {
    Assert.notNull(params.getSpaceId(), "企业空间ID不能为空");
    params.setUserId(SessionUtil.getLoginInfo().getUserId());
    // noinspection resource
    return userFavAppMapper.selectUserFavAppPage(params, params.buildRowBounds()).toPageInfo();
  }

  @Override
  public List<UserFavAppDTO> queryUserFavAppList(Long spaceId) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    UserFavAppQueryParams params = new UserFavAppQueryParams();
    params.setUserId(userId);
    params.setSpaceId(spaceId);
    // 非平台管理员或非企业管理员，需要过滤授权
    if (!SessionUtil.isSuperAdmin(userId) && !orgUserRoleMapper.checkIsOrgAdmin(spaceId, userId)) {
      params.setCheckAuth(true);
      params.setOrgIds(orgMemberService.queryUserOrgAndParentOrgIds(spaceId, userId));
    }
    return userFavAppMapper.selectUserFavAppList(params);
  }

  @Override
  public ResultVO<Void> addUserFavApp(UserFavAppDTO favAppDTO) {
    Assert.notNull(favAppDTO.getAppId(), "应用ID不能为空");
    Long userId = SessionUtil.getLoginInfo().getUserId();
    // 检查应用是否已被添加
    boolean exists = userFavAppMapper.checkExistsByAppId(favAppDTO.getAppId(), favAppDTO.getSpaceId(), userId);
    if (exists) {
      throw new BssException("应用已添加到常用应用");
    }
    // 补充必要参数
    favAppDTO.setFavId(Sequences.USER_FAV_APP_ID.next());
    favAppDTO.setUserId(userId);
    favAppDTO.setCreatorId(userId);
    favAppDTO.setUpdatorId(userId);
    favAppDTO.setStatusCd(BaseConsts.STATUS_CD_VALID);
    // 获取已有的常用应用最大排序数
    Integer maxSort = userFavAppMapper.maxUserFavAppSort(userId, favAppDTO.getSpaceId());
    favAppDTO.setSortOrder((maxSort != null ? maxSort : 0) + 1);
    userFavAppMapper.insertUserFavApp(favAppDTO);
    return ResultVO.success();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResultVO<Void> batchSaveUserFavApp(BatchSaveUserFavAppDTO appDTO) {
    Assert.notNull(appDTO.getSpaceId(), "企业ID不能为空");

    Long userId = SessionUtil.getLoginInfo().getUserId();
    Long spaceId = appDTO.getSpaceId();

    // 应用列表为空表示清空全部常用应用
    if (CollectionUtils.isEmpty(appDTO.getAppList())) {
      userFavAppMapper.clearAllUserFavApp(userId, spaceId);
      return ResultVO.success();
    }

    // 查询旧的常用应用列表
    List<UserFavAppDTO> oldList = queryUserFavAppList(spaceId);
    if (CollectionUtils.isEmpty(oldList)) {
      batchAddUserFavApp(appDTO.getAppList(), spaceId);
      return ResultVO.success();
    }

    // 处理需要新增的列表
    List<UserFavAppDTO> addList = appDTO.getAppList().stream().filter(dto -> dto.getFavId() == null).toList();
    if (CollectionUtils.isNotEmpty(addList)) {
      batchAddUserFavApp(addList, spaceId);
    }

    // 处理需要删除的列表
    List<Long> compareIdList = appDTO.getAppList().stream().map(UserFavAppDTO::getFavId).filter(Objects::nonNull).toList();
    List<Long> delList = oldList.stream().map(UserFavAppDTO::getFavId).filter(favId -> !compareIdList.contains(favId)).toList();
    if (CollectionUtils.isNotEmpty(delList)) {
      userFavAppMapper.batchDeleteUserFavApp(delList, userId, spaceId);
    }

    // 处理需要更新顺序的列表
    Map<Long, Integer> sortOrderMap = new HashMap<>();
    oldList.forEach(dto -> sortOrderMap.put(dto.getFavId(), dto.getSortOrder()));
    for (UserFavAppDTO favAppDTO : appDTO.getAppList()) {
      if (favAppDTO.getFavId() != null && !Objects.equals(favAppDTO.getSortOrder(), sortOrderMap.get(favAppDTO.getFavId()))) {
        favAppDTO.setSpaceId(spaceId);
        favAppDTO.setUserId(userId);
        userFavAppMapper.updateSortOrder(favAppDTO);
      }
    }

    return ResultVO.success();
  }

  /**
   * 执行批量新增常用应用
   */
  private void batchAddUserFavApp(List<UserFavAppDTO> favAppList, Long spaceId) {
    if (CollectionUtils.isEmpty(favAppList)) {
      return;
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    // 补充必要数据
    for (UserFavAppDTO dto : favAppList) {
      dto.setFavId(Sequences.USER_FAV_APP_ID.next());
      dto.setUserId(userId);
      dto.setSpaceId(spaceId);
      dto.setCreatorId(userId);
      dto.setUpdatorId(userId);
      dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
    }
    // 批量插入常用应用
    userFavAppMapper.insertUserFavAppBatch(favAppList);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResultVO<Void> removeUserFavApp(Long appId, Long spaceId) {
    userFavAppMapper.deleteUserFavApp(appId, SessionUtil.getLoginInfo().getUserId(), spaceId);
    return ResultVO.success();
  }

}
