package com.iwhalecloud.bote.service.plugin.impl;

import com.iwhalecloud.bote.dto.plugin.PluginAuthDTO;
import com.iwhalecloud.bote.dto.plugin.params.PluginAuthQueryParams;
import com.iwhalecloud.bote.service.plugin.IPluginAuthManageService;
import com.iwhalecloud.bote.service.plugin.impl.helper.PluginHubHelper;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 插件鉴权管理服务实现
 *
 * @author chen.linfa
 * @since 2025-12-09
 */
@Service
@RequiredArgsConstructor
public class PluginAuthManageServiceImpl implements IPluginAuthManageService {

  private final PluginHubHelper pluginHubHelper;

  @Override
  @Nullable
  public PluginAuthDTO getPluginAuth(Long tenantId, Long authId) {
    return pluginHubHelper.getPluginAuthJson(tenantId, authId);
  }

  @Override
  @Transactional
  public ResultVO<PluginAuthDTO> savePluginAuth(PluginAuthDTO auth) {
    return ResultVO.success(pluginHubHelper.savePluginAuthJson(auth));
  }

  @Override
  public List<PluginAuthDTO> queryPluginAuthList(PluginAuthQueryParams queryParams) {
    return pluginHubHelper.queryPluginAuthJsonList(queryParams);
  }

}
