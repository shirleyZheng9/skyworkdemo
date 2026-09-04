package com.iwhalecloud.bote.service.bot.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.bot.PlatBotInfoDTO;
import com.iwhalecloud.bote.dto.bot.TemplateBotDTO;
import com.iwhalecloud.bote.dto.bot.query.PlatBotInfoQueryParams;
import com.iwhalecloud.bote.dto.organization.OrganizationUserDTO;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import com.iwhalecloud.bote.mapper.bot.BotAuthManageMapper;
import com.iwhalecloud.bote.mapper.bot.PlatBotInfoManageMapper;
import com.iwhalecloud.bote.mapper.organization.OrganizationMemberMapper;
import com.iwhalecloud.bote.mapper.portal.TenantManageMapper;
import com.iwhalecloud.bote.service.bot.IPlatBotInfoManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 应用模板管理服务实现
 *
 * @author auto
 * @since 2025-05-26
 */
@Service
@RequiredArgsConstructor
public class PlatBotInfoManageServiceImpl implements IPlatBotInfoManageService {

  private final PlatBotInfoManageMapper platBotInfoManageMapper;
  private final BotAuthManageMapper botAuthManageMapper;
  private final OrganizationMemberMapper organizationMemberMapper;
  private final TenantManageMapper tenantManageMapper;

  @Override
  public TemplateBotDTO findPlatBotInfo(Long platBotId) {
    return platBotInfoManageMapper.getPlatBot(platBotId);
  }

  @Override
  public TemplateBotDTO findUserBotInfo(Long platBotId) {
    return platBotInfoManageMapper.getUserBot(platBotId);
  }

  @Override
  @Transactional
  public ResultVO<PlatBotInfoDTO> savePlatBotInfo(PlatBotInfoDTO platBotInfo) {
    // 校验编码唯一性
    if (platBotInfoManageMapper.existsPlatBotInfoCode(platBotInfo)) {
      return BaseErrorConstant.CHECK_ATTR_NBR.toResult();
    }
    platBotInfo.setStatusCd(BaseConsts.STATUS_CD_VALID);
    platBotInfo.setTenantId(-1L);
    PlatBotInfoDTO old = platBotInfo.getPlatBotId() == null ? null : platBotInfoManageMapper.getPlatBotInfo(platBotInfo.getPlatBotId());
    if (old == null && "platform".equals(platBotInfo.getBotType()) && platBotInfoManageMapper.existsPlatBotInfo(platBotInfo)) {
      return ResultVO.fail("智能应用已存在模板应用，请检查模板应用");
    }
    platBotInfo.setStatus(old != null ? old.getStatus() : "1");
    DataDifference<PlatBotInfoDTO> difference = DataDifferenceStarter.computeSave(old, platBotInfo, false, platBotInfo.getTenantId());
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<Void> deletePlatBotInfo(Long platBotId) {
    platBotInfoManageMapper.deletePlatBotInfo(platBotId, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  public List<PlatBotInfoDTO> queryPlatBotInfoList(PlatBotInfoQueryParams queryParams) {
    return platBotInfoManageMapper.selectPlatBotInfoList(queryParams);
  }

  @Override
  public PageInfo<PlatBotInfoDTO> queryPlatBotInfoPage(PlatBotInfoQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    // noinspection resource
    return platBotInfoManageMapper.selectPlatBotInfoPage(queryParams, rowBounds).toPageInfo();
  }

  @Override
  public PageInfo<TemplateBotDTO> queryPlatAndUserBotPage(PlatBotInfoQueryParams queryParams) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    queryParams.setUserId(userId);
    queryParams.setOrgIdList(getUserOrgIds(userId, queryParams.getTenantId()).stream().toList());
    queryParams.setIsAdmin(SessionUtil.isSuperAdmin(userId));
    RowBounds rowBounds = queryParams.buildRowBounds();
    // noinspection resource
    return platBotInfoManageMapper.queryPlatAndUserBotPage(queryParams, rowBounds).toPageInfo();
  }

  /**
   * 获取用户归属的组织列表
   *
   * @param userId 用户ID
   * @return 组织ID列表
   */
  private Set<Long> getUserOrgIds(Long userId, Long tenantId) {
    Set<Long> orgIds = new HashSet<>();
    orgIds.add(-1L);
    TenantDTO tenant = tenantManageMapper.getTenant(tenantId);
    if (tenant == null) {
      return orgIds;
    }
    // 查询用户归属的组织列表
    List<OrganizationUserDTO> organizationUserList = organizationMemberMapper.selectUserOrgByUserId(tenant.getSpaceId(), userId);
    if (CollectionUtils.isNotEmpty(organizationUserList)) {
      orgIds.addAll(organizationUserList.stream().map(OrganizationUserDTO::getOrgId).toList());
    }
    return orgIds;
  }

  @Override
  public ResultVO<Void> publishPlatBot(Long platBotId, String status) {
    PlatBotInfoDTO platBotInfo = platBotInfoManageMapper.getPlatBotInfo(platBotId);
    if (platBotInfo == null) {
      return BaseErrorConstant.NOT_EXIST.toResult(platBotId);
    }
    platBotInfo.setStatus(status);
    platBotInfoManageMapper.updatePlatBotInfoStatus(platBotId, SessionUtil.getLoginInfo().getUserId(), status);
    return ResultVO.success();
  }

  @Override
  public ResultVO<Void> removeBot(Long botId) {
    int effectedRows = botAuthManageMapper.deleteAuthByBotId(botId, SessionUtil.getLoginInfo().getUserId());
    if (effectedRows == 0) {
      return BaseErrorConstant.NOT_EXIST.toResult(botId);
    }
    return ResultVO.success();
  }

  @Override
  public PageInfo<TemplateBotDTO> queryUserAuthBotPage(PlatBotInfoQueryParams queryParams) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    queryParams.setUserId(userId);
    queryParams.setOrgIdList(getUserOrgIds(userId, queryParams.getTenantId()).stream().toList());
    queryParams.setIsAdmin(SessionUtil.isSuperAdmin(userId));
    RowBounds rowBounds = queryParams.buildRowBounds();
    // noinspection resource
    return platBotInfoManageMapper.selectUserAuthBotPage(queryParams, rowBounds).toPageInfo();
  }
}
