package com.iwhalecloud.bote.common.diffc.persist.impl.bot;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.bot.BotSceneSkillDTO;
import com.iwhalecloud.bote.mapper.bot.BotSceneRelaManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：场景技能
 *
 * @author chen.linfa
 * @since 2024-09-11
 */
@Component
public final class BotSceneSkillIDifferencePersistence extends BaseRootPersistence<BotSceneSkillDTO> {
  public BotSceneSkillIDifferencePersistence(BotSceneRelaManageMapper botSceneRelaManageMapper) {
    // 新增情况
    this.setBatchAddConsumer(botSceneRelaManageMapper::batchInsertSceneSkill);
    // 修改情况
    this.setModifyConsumer(botSceneRelaManageMapper::updateSceneSkill);
  }
}
