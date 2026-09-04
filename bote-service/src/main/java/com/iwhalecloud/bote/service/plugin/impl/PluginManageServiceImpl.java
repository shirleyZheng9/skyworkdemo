package com.iwhalecloud.bote.service.plugin.impl;

import static com.iwhalecloud.bss.litchi.base.vo.ResultVO.success;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.PluginDTO;
import com.iwhalecloud.bote.dto.plugin.request.QueryCatalogRequest;
import com.iwhalecloud.bote.dto.plugin.request.QueryPluginRequest;
import com.iwhalecloud.bote.dto.plugin.request.SubscribePluginRequest;
import com.iwhalecloud.bote.dto.plugin.request.TestPluginToolRequest;
import com.iwhalecloud.bote.dto.plugin.response.PluginDefinition;
import com.iwhalecloud.bote.dto.plugin.response.PluginToolSpec;
import com.iwhalecloud.bote.dto.plugin.response.ToolInputSpec;
import com.iwhalecloud.bote.dto.portal.SimpleTenantDTO;
import com.iwhalecloud.bote.dto.skill.query.PluginQueryParams;
import com.iwhalecloud.bote.mapper.plugin.PluginManageMapper;
import com.iwhalecloud.bote.mapper.portal.TenantQueryMapper;
import com.iwhalecloud.bote.service.plugin.IPluginManageService;
import com.iwhalecloud.bote.service.plugin.impl.helper.PluginHubHelper;
import com.iwhalecloud.bote.service.portal.ITenantSettingInfoManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 插件管理服务实现
 *
 * @author auto
 * @since 2025-04-01
 */
@Service
@RequiredArgsConstructor
public class PluginManageServiceImpl implements IPluginManageService {

  // @formatter:off
  private final PluginManageMapper pluginManageMapper;
  private final TenantQueryMapper tenantQueryMapper;
  private final PluginHubHelper pluginHubHelper;
  private final TenantSettingInfoCache tenantSettingInfoCache;
  private final ITenantSettingInfoManageService tenantSettingInfoManageService;
  // @formatter:on

  @Override
  public PluginDTO findPlugin(Long tenantId, Long pluginId) {
    return pluginManageMapper.getPlugin(tenantId, pluginId);
  }

  @Override
  @Transactional
  public ResultVO<PluginDTO> savePlugin(PluginDTO plugin) {
    // 校验编码唯一性
    if (pluginManageMapper.existsPluginCode(plugin)) {
      return ResultVO.fail("方法名不唯一，pluginCode=" + plugin.getPluginCode());
    }
    plugin.setTenantId(BaseConsts.PLATFORM_TENANT_ID);
    PluginDTO old = plugin.getPluginId() == null ? null : findPlugin(plugin.getTenantId(), plugin.getPluginId());
    DataDifference<PluginDTO> difference = DataDifferenceStarter.computeSave(old, plugin, false, plugin.getTenantId());
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<Void> deletePlugin(Long pluginId) {
    pluginManageMapper.deletePlugin(pluginId, SessionUtil.getLoginInfo().getUserId());
    return success();
  }

  @Override
  public List<PluginDTO> queryPluginList(PluginQueryParams queryParams) {
    return pluginManageMapper.selectPluginList(queryParams);
  }

  @Override
  public PageInfo<PluginDTO> queryPluginPage(PluginQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    // noinspection resource
    return pluginManageMapper.selectPluginPage(queryParams, rowBounds).toPageInfo();
  }

  @Override
  public ResultVO<Void> publishPlugin(Long tenantId, Long pluginId, String status) {
    PluginDTO plugin = findPlugin(tenantId, pluginId);
    if (plugin == null) {
      return BaseErrorConstant.NOT_EXIST.toResult(pluginId);
    }
    plugin.setPluginStatus(status);
    plugin.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    plugin.setUpdatedTime(new Date());
    pluginManageMapper.updatePlugin(plugin);
    return success();
  }

  @Override
  public void syncPortalUserInfo(Long tenantId) {
    if (SystemParameter.PLUGIN_ENABLED.getBooleanValueFromDb()) {
      syncTenantApiKey(tenantId);
    }
  }

  @Override
  public List<CatalogDTO> queryPluginCatalog(QueryCatalogRequest request) {
    return pluginHubHelper.queryCatalogTree(request);
  }

  @Override
  public PageInfo<PluginDefinition> queryAuthPluginPage(QueryPluginRequest request) {
    return pluginHubHelper.queryAuthPluginPage(request);
  }

  @Override
  public Map<String, Long> countAuthPluginByCatalog(QueryPluginRequest request) {
    return pluginHubHelper.countAuthPluginByCatalog(request);
  }

  @Override
  public ResultVO<Void> subscribePlugin(SubscribePluginRequest request) {
    pluginHubHelper.subscribePlugin(request.getTenantId(), request.getPluginId(), request.getAuthParams());
    return success();
  }

  @Override
  public ResultVO<Void> unsubscribePlugin(Long tenantId, Long pluginId) {
    pluginHubHelper.unsubscribePlugin(tenantId, pluginId);
    return success();
  }

  @Override
  public ResultVO<PluginDefinition> getPluginDefinition(Long tenantId, Long pluginId, @Nullable Boolean includeTools) {
    return ResultVO.success(pluginHubHelper.getPluginDefinition(tenantId, pluginId, includeTools));
  }

  @Override
  public ResultVO<List<PluginToolSpec>> getPluginTools(Long tenantId, Long pluginId) {
    // 查询插件工具列表
    List<PluginToolSpec> pluginTools = pluginHubHelper.getPluginTools(tenantId, pluginId);
    // 填充参数的 key, 避免前端出现赋值错乱
    for (PluginToolSpec toolSpec : CollectionUtils.emptyIfNull(pluginTools)) {
      ToolInputSpec inputSpec = toolSpec.getInput();
      if (inputSpec != null) {
        fillParameterKey(inputSpec.getHeader());
        fillParameterKey(inputSpec.getPath());
        fillParameterKey(inputSpec.getQuery());
        fillParameterKey(inputSpec.getBody());
      }
    }
    return success(pluginTools);
  }

  /**
   * 填充参数的 key
   */
  private void fillParameterKey(List<ParameterSpec> parameterSpecList) {
    if (CollectionUtils.isEmpty(parameterSpecList)) {
      return;
    }
    for (ParameterSpec parameterSpec : parameterSpecList) {
      parameterSpec.setKey(UUID.randomUUID().toString());
      if (CollectionUtils.isNotEmpty(parameterSpec.getChildren())) {
        fillChildrenParameterKey(parameterSpec.getKey(), parameterSpec.getChildren());
      }
    }
  }

  /**
   * 递归填充参数 children 的 key
   */
  private void fillChildrenParameterKey(String parentKey, List<ParameterSpec> children) {
    for (ParameterSpec child : children) {
      String key = UUID.randomUUID().toString();
      child.setParentKey(parentKey);
      child.setKey(key);
      if (CollectionUtils.isNotEmpty(child.getChildren())) {
        fillChildrenParameterKey(key, child.getChildren());
      }
    }
  }

  @Override
  public ResultVO<Object> callPluginTool(TestPluginToolRequest request) {
    return success(pluginHubHelper.callPluginTool(request));
  }

  /**
   * 同步插件市场门户开发者信息
   */
  private void syncTenantApiKey(Long tenantId) {
    String apiKey = tenantSettingInfoCache.getPluginApiKey(tenantId);
    if (StringUtils.isNotEmpty(apiKey)) {
      return;
    }
    String portalCode = SystemParameter.PLUGIN_PORTAL_CODE.getValueFromDb();
    SimpleTenantDTO tenant = tenantQueryMapper.getSimpleTenant(tenantId);
    // 采用以下方式定义用户名称，确保唯一性
    String userName = portalCode + "_" + tenant.getTenantCode();
    apiKey = pluginHubHelper.createPortalUser(portalCode, userName, tenant.getTenantName());
    Assert.hasText(apiKey, "同步插件市场门户开发者信息异常");
    tenantSettingInfoManageService.savePluginHub(tenantId, apiKey);
  }
}
