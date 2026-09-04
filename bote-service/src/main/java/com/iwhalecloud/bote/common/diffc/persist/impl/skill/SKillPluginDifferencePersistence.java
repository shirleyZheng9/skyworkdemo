package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.skill.SkillPluginDTO;
import com.iwhalecloud.bote.mapper.skill.SkillPluginManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：技能：插件
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Component
public final class SKillPluginDifferencePersistence extends BaseRootPersistence<SkillPluginDTO> {

  public SKillPluginDifferencePersistence(SkillPluginManageMapper pluginManageMapper) {
    // 新增情况
    setAddConsumer(pluginManageMapper::insertSkillPlugin);
    // 修改情况
    setModifyConsumer(pluginManageMapper::updateSkillPlugin);
  }
}
