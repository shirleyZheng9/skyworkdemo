package com.iwhalecloud.bote.service.plugin;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.dto.plugin.PluginDTO;
import com.iwhalecloud.bote.dto.plugin.request.QueryCatalogRequest;
import com.iwhalecloud.bote.dto.plugin.request.QueryPluginRequest;
import com.iwhalecloud.bote.dto.plugin.request.SubscribePluginRequest;
import com.iwhalecloud.bote.dto.plugin.request.TestPluginToolRequest;
import com.iwhalecloud.bote.dto.plugin.response.PluginDefinition;
import com.iwhalecloud.bote.dto.plugin.response.PluginToolSpec;
import com.iwhalecloud.bote.dto.skill.query.PluginQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

/**
 * 插件管理服务
 *
 * @author auto
 * @since 2025-04-01
 */
public interface IPluginManageService {

  /**
   * 查询单个插件
   *
   * @param tenantId 租户 ID
   * @param pluginId 插件主键
   * @return 插件
   */
  PluginDTO findPlugin(Long tenantId, Long pluginId);

  /**
   * 保存插件
   *
   * @param plugin 插件
   * @return 结果
   */
  ResultVO<PluginDTO> savePlugin(PluginDTO plugin);

  /**
   * 删除插件
   *
   * @param pluginId 插件主键
   * @return 结果
   */
  ResultVO<Void> deletePlugin(Long pluginId);

  /**
   * 查询插件列表
   *
   * @param queryParams 查询条件
   * @return 插件列表
   */
  List<PluginDTO> queryPluginList(PluginQueryParams queryParams);

  /**
   * 查询插件列表（分页）
   *
   * @param queryParams 查询条件
   * @return 插件分页列表
   */
  PageInfo<PluginDTO> queryPluginPage(PluginQueryParams queryParams);

  /**
   * 上架/下架插件
   *
   * @param tenantId 租户 ID
   * @param pluginId 插件主键
   * @param status 状态
   * @return 结果
   */
  ResultVO<Void> publishPlugin(Long tenantId, Long pluginId, String status);

  /**
   * 同步插件市场门户开发者信息
   *
   * @param tenantId 租户 ID
   */
  void syncPortalUserInfo(Long tenantId);

  /**
   * 查询插件目录
   *
   * @param request 查询条件
   * @return 插件目录
   */
  List<CatalogDTO> queryPluginCatalog(QueryCatalogRequest request);

  /**
   * 查询授权的插件列表（分页）
   *
   * @param request 查询条件
   * @return 插件列表（分页）
   */
  PageInfo<PluginDefinition> queryAuthPluginPage(QueryPluginRequest request);

  /**
   * 根据插件分类统计插件数量
   *
   * @param request 查询条件
   * @return 插件统计列表
   */
  Map<String, Long> countAuthPluginByCatalog(QueryPluginRequest request);

  /**
   * 订阅插件
   *
   * @param request 入参
   * @return 结果
   */
  ResultVO<Void> subscribePlugin(SubscribePluginRequest request);

  /**
   * 取消订阅插件
   *
   * @param tenantId 租户 ID
   * @param pluginId 插件 ID
   * @return 结果
   */
  ResultVO<Void> unsubscribePlugin(Long tenantId, Long pluginId);

  /**
   * 查询插件定义
   *
   * @param tenantId 租户 ID
   * @param pluginId 插件 ID
   * @param includeTools 是否包含工具
   * @return 插件定义
   */
  ResultVO<PluginDefinition> getPluginDefinition(Long tenantId, Long pluginId, @Nullable Boolean includeTools);

  /**
   * 查询插件的工具列表
   *
   * @param tenantId 租户 ID
   * @param pluginId 插件 ID
   * @return 插件定义
   */
  ResultVO<List<PluginToolSpec>> getPluginTools(Long tenantId, Long pluginId);

  /**
   * 调用插件工具
   *
   * @param request 入参
   * @return 结果
   */
  ResultVO<Object> callPluginTool(TestPluginToolRequest request);
}
