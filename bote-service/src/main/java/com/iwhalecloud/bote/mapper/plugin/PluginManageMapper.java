package com.iwhalecloud.bote.mapper.plugin;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.plugin.PluginDTO;
import com.iwhalecloud.bote.dto.skill.query.PluginQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 插件管理
 *
 * @author auto
 * @since 2025-04-01
 */
public interface PluginManageMapper {
  /**
   * 校验插件的编码唯一性
   */
  boolean existsPluginCode(@Param("dto") PluginDTO plugin);

  /**
   * 根据主键获取插件
   */
  PluginDTO getPlugin(@Param("tenantId") Long tenantId, @Param("id") Long pluginId);

  /**
   * 新增插件
   *
   * @param plugin 插件
   * @return 结果
   */
  int insertPlugin(@Param("dto") PluginDTO plugin);

  /**
   * 批量新增插件
   *
   * @param plugins 插件列表
   * @return 结果
   */
  int batchInsertPlugin(@Param("list") List<PluginDTO> plugins);

  /**
   * 修改插件
   *
   * @param plugin 插件
   * @return 结果
   */
  int updatePlugin(@Param("dto") PluginDTO plugin);

  /**
   * 删除插件
   */
  int deletePlugin(@Param("pluginId") Long pluginId, @Param("updatorId") Long updatorId);

  /**
   * 根据插件 ID 列表批量查询插件
   */
  List<PluginDTO> selectPluginsByIds(@Param("tenantId") Long tenantId, @Param("pluginIds") List<Long> pluginIds);

  /**
   * 获取插件列表
   */
  List<PluginDTO> selectPluginList(@Param("query") PluginQueryParams queryParams);

  /**
   * 获取插件列表（分页）
   */
  Page<PluginDTO> selectPluginPage(@Param("query") PluginQueryParams queryParams, RowBounds rowBounds);
}
