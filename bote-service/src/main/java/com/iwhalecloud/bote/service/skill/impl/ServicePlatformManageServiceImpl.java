package com.iwhalecloud.bote.service.skill.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.util.EnvUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.skill.ServiceGatewayDTO;
import com.iwhalecloud.bote.dto.skill.ServicePlatformDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.mapper.skill.ServiceGatewayManageMapper;
import com.iwhalecloud.bote.mapper.skill.ServicePlatformManageMapper;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bote.service.skill.IServicePlatformManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDiffState;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 技能：API 平台 服务实现
 *
 * @author auto
 * @since 2024-09-17
 */
@Service
@RequiredArgsConstructor
public class ServicePlatformManageServiceImpl implements IServicePlatformManageService {

  private final ServicePlatformManageMapper platformManageMapper;
  private final ServiceGatewayManageMapper gatewayManageMapper;
  private final IResourceElementService resourceElementService;

  @Override
  @Transactional
  public ResultVO<ServicePlatformDTO> saveServicePlatform(ServicePlatformDTO platform) {
    if (platformManageMapper.existsServicePlatformCode(platform)) {
      return BaseErrorConstant.CHECK_CODE.toResult(platform.getPlatformCode());
    }
    platform.setStatusCd(BaseConsts.STATUS_CD_VALID);
    for (ServiceGatewayDTO gateway : CollectionUtils.emptyIfNull(platform.getGateways())) {
      gateway.setStatusCd(BaseConsts.STATUS_CD_VALID);
      gateway.setPlatformId(platform.getPlatformId());
    }

    ServicePlatformDTO old = platform.getPlatformId() == null ? null : findServicePlatform(platform.getTenantId(), platform.getPlatformId());
    DataDifference<ServicePlatformDTO> difference = DataDifferenceStarter.computeSave(old, platform, true, platform.getTenantId());
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    if (!EnvUtil.isDevEnv()) {
      boolean existsRemove = IterableUtils.matchesAny(CollectionUtils.emptyIfNull(difference.getChildren()),
        p -> DataDiffState.REMOVE.equals(p.getState()));
      Assert.isTrue(!existsRemove, "非配置态环境，不允许修改网关信息，避免跨环境迁移出现数据混乱");
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  public List<ServicePlatformDTO> queryServicePlatformList(SkillQueryParams param) {
    List<ServicePlatformDTO> list = platformManageMapper.selectServicePlatformList(param);
    buildServicePlatform(param.getTenantId(), list);
    return list;
  }

  @Override
  @Nullable
  public ServicePlatformDTO findServicePlatform(Long tenantId, Long platformId) {
    ServicePlatformDTO platform = platformManageMapper.getServicePlatform(tenantId, platformId);
    if (platform == null) {
      return null;
    }
    platform.setGateways(gatewayManageMapper.selectServiceGatewayById(tenantId, platformId));
    return platform;
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteServicePlatform(Long tenantId, Long platformId) {
    if (resourceElementService.existsRelatedResource(tenantId, platformId, DataSyncCodeEnum.SERVICE_PLATFORM.getCode())) {
      return ResultVO.fail("网关已存在关联配置数据，不允许删除");
    }
    platformManageMapper.deleteServicePlatform(tenantId, platformId, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  public PageInfo<ServicePlatformDTO> queryServicePlatformPage(SkillQueryParams param) {
    // noinspection resource
    PageInfo<ServicePlatformDTO> result = platformManageMapper.selectServicePlatformPage(param, param.buildRowBounds()).toPageInfo();
    buildServicePlatform(param.getTenantId(), result.getList());
    return result;
  }

  private void buildServicePlatform(Long tenantId, List<ServicePlatformDTO> platforms) {
    if (CollectionUtils.isEmpty(platforms)) {
      return;
    }
    List<Long> platformIds = platforms.stream().map(ServicePlatformDTO::getPlatformId).collect(Collectors.toList());
    Map<Long, List<ServiceGatewayDTO>> gatewats = gatewayManageMapper.selectServiceGatewayByIds(tenantId, platformIds).stream()
      .collect(Collectors.groupingBy(ServiceGatewayDTO::getPlatformId));
    gatewats.forEach((k, v) -> {
      ServicePlatformDTO platform = IterableUtils.find(platforms, p -> Objects.equals(k, p.getPlatformId()));
      platform.setGateways(v);
    });
  }

  @Override
  public List<ServicePlatformDTO> queryServicePlatformByIds(Long tenantId, List<Long> platformIds) {
    if (CollectionUtils.isEmpty(platformIds)) {
      return Collections.emptyList();
    }
    List<ServicePlatformDTO> platforms = platformManageMapper.selectServicePlatformByIds(tenantId, platformIds);
    buildServicePlatform(tenantId, platforms);
    return platforms;
  }
}
