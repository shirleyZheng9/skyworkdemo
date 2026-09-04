package com.iwhalecloud.bote.service.skill.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.skill.SimpleSkillPluginDTO;
import com.iwhalecloud.bote.dto.skill.SkillPluginDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.mapper.skill.QuerySkillMapper;
import com.iwhalecloud.bote.mapper.skill.SkillPluginManageMapper;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bote.service.skill.ISkillPluginManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 技能：插件 服务实现
 *
 * @author auto
 * @since 2024-09-21
 */
@Service
@RequiredArgsConstructor
public class SkillPluginManageServiceImpl implements ISkillPluginManageService {

  private final SkillPluginManageMapper pluginManageMapper;
  private final QuerySkillMapper querySkillMapper;
  private final ICatalogManageService catalogManageService;
  private final IResourceElementService resourceElementService;

  @Override
  public SkillPluginDTO getSkillPlugin(Long tenantId, Long apiId) {
    return pluginManageMapper.getSkillPlugin(tenantId, apiId);
  }

  @Override
  @Transactional
  public ResultVO<SkillPluginDTO> saveSkillPlugin(SkillPluginDTO plugin) {
    if (plugin.getCopyApiId() != null) {
      return copySkillPlugin(plugin);
    }
    // 校验编码唯一性
    if (pluginManageMapper.existsSkillPluginCode(plugin)) {
      return BaseErrorConstant.CHECK_CODE.toResult(plugin.getApiCode());
    }
    plugin.setStatusCd(BaseConsts.STATUS_CD_VALID);
    SkillPluginDTO old = plugin.getApiId() == null ? null : getSkillPlugin(plugin.getTenantId(), plugin.getApiId());
    DataDifference<SkillPluginDTO> difference = DataDifferenceStarter.computeSaveAndLog(old, plugin, false, plugin.getTenantId(),
      OperClassEnum.SKILL_PLUGIN);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  private ResultVO<SkillPluginDTO> copySkillPlugin(SkillPluginDTO plugin) {
    SkillPluginDTO oldPlugin = pluginManageMapper.getSkillPlugin(plugin.getTenantId(), plugin.getCopyApiId());
    if (oldPlugin == null) {
      return BaseErrorConstant.SKILL_PLUGIN_NOT_EXISTS.toResult();
    }
    if (pluginManageMapper.existsSkillPluginCode(plugin)) {
      return BaseErrorConstant.CHECK_CODE.toResult(plugin.getApiCode());
    }
    DataDifferenceStarter.computeSaveAndLog(null, plugin, false, plugin.getTenantId(), OperClassEnum.SKILL_PLUGIN);
    return ResultVO.success(plugin);
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteSkillPlugin(Long tenantId, Long apiId) {
    if (resourceElementService.existsRelatedResource(tenantId, apiId, DataSyncCodeEnum.SKILL_PLUGIN.getCode())) {
      return ResultVO.fail("插件已存在关联配置数据，不允许删除");
    }
    pluginManageMapper.deleteSkillPlugin(tenantId, apiId, SessionUtil.getLoginInfo().getUserId());
    ResourceElementFactory.get(OperClassEnum.SKILL_PLUGIN.name()).clear(tenantId, apiId);
    return ResultVO.success();
  }

  @Override
  public List<SkillPluginDTO> querySkillPluginList(SkillQueryParams queryParams) {
    return pluginManageMapper.selectSkillPluginList(queryParams);
  }

  @Override
  public PageInfo<SkillPluginDTO> querySkillPluginPage(SkillQueryParams params) {
    if (!BaseConsts.FALSE.equals(params.getConfigFlag())) {
      params.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(params.getTenantId(), params.getCatalogItemId(), CatalogConsts.TYPE_SKILL));
    }
    // noinspection resource
    return pluginManageMapper.selectSkillPluginPage(params, params.buildRowBounds()).toPageInfo();
  }

  @Override
  public PageInfo<SimpleSkillPluginDTO> querySimpleSkillPluginPage(SkillQueryParams params) {
    params.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(params.getTenantId(), params.getCatalogItemId(), CatalogConsts.TYPE_SKILL));
    // noinspection resource
    return querySkillMapper.selectSkillPluginPage(params, params.buildRowBounds()).toPageInfo();
  }
}
