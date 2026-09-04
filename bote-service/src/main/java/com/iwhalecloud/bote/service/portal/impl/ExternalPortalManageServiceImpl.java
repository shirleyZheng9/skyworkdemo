package com.iwhalecloud.bote.service.portal.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.portal.ExternalPortalDTO;
import com.iwhalecloud.bote.dto.portal.query.ExternalPortalQueryParams;
import com.iwhalecloud.bote.mapper.portal.ExternalPortalMapper;
import com.iwhalecloud.bote.service.portal.IExternalPortalManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.UUIDUtils;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 外部门户管理服务
 *
 * @author bianjp
 * @since 2025-02-24
 */
@Service
@RequiredArgsConstructor
public class ExternalPortalManageServiceImpl implements IExternalPortalManageService {
  private final ExternalPortalMapper externalPortalMapper;

  @Override
  public ExternalPortalDTO getPortal(Long id) {
    ExternalPortalDTO portal = externalPortalMapper.selectPortalById(id);
    Assert.notNull(portal, () -> "门户不存在: " + id);
    portal.setEnabled(BaseConsts.STATUS_CD_VALID.equals(portal.getStatusCd()));
    portal.buildHeaders(portal.getHeaderJson());
    return portal;
  }

  @Override
  public PageInfo<ExternalPortalDTO> queryPortalPage(ExternalPortalQueryParams queryParams) {
    //noinspection resource
    PageInfo<ExternalPortalDTO> pageInfo = externalPortalMapper.selectPortalPage(queryParams, queryParams.buildRowBounds()).toPageInfo();
    for (ExternalPortalDTO portal : ListUtils.emptyIfNull(pageInfo.getList())) {
      portal.setEnabled(BaseConsts.STATUS_CD_VALID.equals(portal.getStatusCd()));
      portal.setStatusCd(null);
    }
    return pageInfo;
  }

  @Override
  @Transactional
  public ResultVO<Long> savePortal(ExternalPortalDTO portal) {
    ExternalPortalDTO oldPortal = portal.getId() != null ? getPortal(portal.getId()) : null;
    // 校验
    ResultVO<Long> validationResult = validatePortal(portal, oldPortal);
    if (validationResult != null) {
      return validationResult;
    }

    // 处理请求头信息
    if (CollectionUtils.isNotEmpty(portal.getHeaders())) {
      portal.setHeaderJson(JsonUtil.toJsonStringCompact(portal.getHeaders()));
    }
    portal.setStatusCd(Boolean.FALSE.equals(portal.getEnabled()) ? BaseConsts.STATUS_CD_DISABLED : BaseConsts.STATUS_CD_VALID);
    // 部分字段不允许修改
    if (oldPortal != null) {
      portal.setCreatorId(oldPortal.getCreatorId());
      portal.setCreatedTime(oldPortal.getCreatedTime());
      portal.setSecretKey(oldPortal.getSecretKey());
    }
    else if (BaseConsts.PORTAL_TYPE_SSO.equals(portal.getPortalType())) {
      // 自动生成签名密钥
      portal.setSecretKey(UUIDUtils.randomFormatUuid());
    }
    DataDifference<ExternalPortalDTO> difference = DataDifferenceStarter.computeSave(oldPortal, portal, false, null);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }

    return ResultVO.success(difference.getToSaveData().getId());
  }


  /**
   * 校验门户配置
   */
  @Nullable
  private ResultVO<Long> validatePortal(ExternalPortalDTO portal, @Nullable ExternalPortalDTO oldPortal) {
    Assert.hasLength(portal.getPortalName(), "门户名称不能为空");
    Assert.hasLength(portal.getPortalCode(), "门户编码不能为空");
    Assert.hasLength(portal.getPortalType(), "门户类型不能为空");
    Assert.isTrue(BaseConsts.EXTERNAL_PORTAL_TYPES.contains(portal.getPortalType()), () -> "未知的门户类型: " + portal.getPortalType());
    // 移除链接中的空格
    if (StringUtils.isNotEmpty(portal.getLoginUrl())) {
      portal.setLoginUrl(portal.getLoginUrl().trim());
    }
    if (StringUtils.isNotEmpty(portal.getLoggedUrl())) {
      portal.setLoggedUrl(portal.getLoggedUrl().trim());
    }
    // 校验门户编码唯一性
    if ((oldPortal == null || !Objects.equals(portal.getPortalCode(), oldPortal.getPortalCode())) && externalPortalMapper.existsPortalCode(portal.getPortalCode())) {
      return ResultVO.fail("门户编码已存在");
    }
    return null;
  }

  @Override
  @Transactional
  public ResultVO<Void> updatePortalStatus(Long id, boolean enabled) {
    String status = enabled ? BaseConsts.STATUS_CD_VALID : BaseConsts.STATUS_CD_DISABLED;
    if (externalPortalMapper.updatePortalStatus(id, status, SessionUtil.getOptionalUserId()) > 0) {
      return ResultVO.success();
    }
    return ResultVO.fail("门户不存在");
  }

  @Override
  @Transactional
  public ResultVO<Void> deletePortal(Long id) {
    if (externalPortalMapper.deletePortal(id, SessionUtil.getOptionalUserId()) > 0) {
      return ResultVO.success();
    }
    return ResultVO.fail("门户不存在");
  }
}
