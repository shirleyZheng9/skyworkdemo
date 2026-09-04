package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bote.dto.plugin.PluginDTO;
import com.iwhalecloud.bote.mapper.plugin.PluginManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：插件
 *
 * @author auto
 * @since 2025-04-01
 */
@Component
public final class PluginDifferencePersistence extends BaseRootPersistence<PluginDTO> {

  public PluginDifferencePersistence(PluginManageMapper pluginManageMapper) {
    setAddConsumer(pluginManageMapper::insertPlugin);
    setBatchAddConsumer(pluginManageMapper::batchInsertPlugin);
    setModifyConsumer(pluginManageMapper::updatePlugin);
  }

}
