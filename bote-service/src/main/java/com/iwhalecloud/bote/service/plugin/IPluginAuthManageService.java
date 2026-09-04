package com.iwhalecloud.bote.service.plugin;

import com.iwhalecloud.bote.dto.plugin.PluginAuthDTO;
import com.iwhalecloud.bote.dto.plugin.params.PluginAuthQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 插件鉴权管理服务
 *
 * @author chen.linfa
 * @since 2025-12-09
 */
public interface IPluginAuthManageService {

  /**
   * 查询单个插件鉴权
   *
   * @param tenantId 租户 ID
   * @param authId 插件鉴权主键
   * @return 插件鉴权
   */
  @Nullable
  PluginAuthDTO getPluginAuth(Long tenantId, Long authId);

  /**
   * 保存插件鉴权
   *
   * @param pluginAuth 插件鉴权
   * @return 结果
   */
  ResultVO<PluginAuthDTO> savePluginAuth(PluginAuthDTO pluginAuth);

  /**
   * 查询插件鉴权列表
   *
   * @param queryParams 查询条件
   * @return 插件鉴权列表
   */
  List<PluginAuthDTO> queryPluginAuthList(PluginAuthQueryParams queryParams);
}
