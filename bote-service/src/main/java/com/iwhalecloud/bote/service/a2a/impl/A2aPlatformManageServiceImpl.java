package com.iwhalecloud.bote.service.a2a.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.cache.ToolboxCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.a2a.A2aPlatformDTO;
import com.iwhalecloud.bote.dto.a2a.query.A2aPlatformQueryParams;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.skill.SimpleFunctionSkillDTO;
import com.iwhalecloud.bote.mapper.a2a.A2aPlatformMapper;
import com.iwhalecloud.bote.service.a2a.IA2aPlatformManageService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.IterableUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * A2A 平台管理服务实现
 *
 * @author bianjp
 * @since 2025-09-08
 */
@Service
@RequiredArgsConstructor
public class A2aPlatformManageServiceImpl implements IA2aPlatformManageService {

  private final A2aPlatformMapper platformMapper;
  private final ToolboxCache toolboxCache;

  @Override
  public A2aPlatformDTO findA2aPlatform(Long tenantId, Long platformId) {
    A2aPlatformDTO platform = platformMapper.selectPlatform(tenantId, platformId);
    Assert.notNull(platform, () -> "A2A 平台不存在: platformId=" + platformId);
    return platform;
  }

  @Override
  @Transactional
  public ResultVO<A2aPlatformDTO> saveA2aPlatform(A2aPlatformDTO platform) {
    Assert.notNull(platform.getPlatformName(), "平台名称不能为空");
    Assert.notNull(platform.getPlatformCode(), "平台编码不能为空");

    A2aPlatformDTO old = platform.getPlatformId() == null ? null : findA2aPlatform(platform.getTenantId(), platform.getPlatformId());
    // 校验编码唯一性
    if ((old == null || !Objects.equals(old.getPlatformCode(), platform.getPlatformCode()))
      && platformMapper.existsPlatformCode(platform.getTenantId(), platform.getPlatformCode())) {
      return BaseErrorConstant.CHECK_CODE.toResult(platform.getPlatformCode());
    }
    // 校验动态请求头扩展脚本
    if (platform.getAuthExtFuncId() != null) {
      validateAuthExtFunc(platform.getTenantId(), platform.getAuthExtFuncId());
    }

    if (old == null) {
      // 新增时随机生成发布密钥
      platform.setPublishKey(UUID.randomUUID().toString());
    }
    else {
      // 不允许修改的字段
      platform.setPublishKey(old.getPublishKey());
    }
    platform.setStatusCd(BaseConsts.STATUS_CD_VALID);
    DataDifference<A2aPlatformDTO> difference = DataDifferenceStarter.computeSaveAndLog(old, platform, false, platform.getTenantId(), OperClassEnum.A2A_PLATFORM);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  /**
   * 校验动态请求头扩展脚本
   */
  private void validateAuthExtFunc(Long tenantId, Long funcId) {
    SimpleFunctionSkillDTO function = toolboxCache.getFunction(tenantId, funcId);
    Assert.notNull(function, () -> "服务函数不存在: " + funcId);
    ParameterSpec request = function.getRequest();
    ParameterSpec response = function.getResponse();
    Assert.isTrue(request == null || (request.isObject() && !request.hasChildren()), "动态请求头扩展脚本不能有入参");
    Assert.isTrue(response != null && response.isObject() && response.hasChildren(), "动态请求头扩展脚本必须返回 headers 对象");
    ParameterSpec headersSpec = IterableUtils.find(response.getChildren(), s -> "headers".equals(s.getName()));
    Assert.isTrue(headersSpec != null && headersSpec.isObject(), "动态请求头扩展脚本必须返回 headers 对象");
  }

  @Override
  @Transactional
  public String resetPublishKey(Long tenantId, Long platformId) {
    String publishKey = UUID.randomUUID().toString();
    int affectedRows = platformMapper.updatePublishKey(tenantId, platformId, publishKey, SessionUtil.getLoginInfo().getUserId());
    Assert.isTrue(affectedRows > 0, "A2A 平台不存在: platformId=" + platformId);
    return publishKey;
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteA2aPlatform(Long tenantId, Long platformId) {
    if (platformMapper.existsAgentByPlatformId(tenantId, platformId)) {
      return ResultVO.fail("该平台下存在 A2A 服务，不允许删除");
    }
    platformMapper.deletePlatform(tenantId, platformId, SessionUtil.getLoginInfo().getUserId());
    ResourceElementFactory.get(OperClassEnum.A2A_PLATFORM.name()).clear(tenantId, platformId);
    return ResultVO.success();
  }

  @Override
  public List<A2aPlatformDTO> queryA2aPlatformList(A2aPlatformQueryParams queryParams) {
    return platformMapper.selectPlatformList(queryParams);
  }

  @Override
  public PageInfo<A2aPlatformDTO> queryA2aPlatformPage(A2aPlatformQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    //noinspection resource
    return platformMapper.selectPlatformPage(queryParams, rowBounds).toPageInfo();
  }
}
