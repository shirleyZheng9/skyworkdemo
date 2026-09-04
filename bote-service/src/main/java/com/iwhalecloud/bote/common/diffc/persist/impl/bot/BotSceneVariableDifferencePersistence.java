package com.iwhalecloud.bote.common.diffc.persist.impl.bot;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.bot.BotSceneParamDTO;
import com.iwhalecloud.bote.mapper.bot.BotSceneRelaManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：场景变量
 *
 * @author chen.linfa
 * @since 2024-08-05
 */
@Component
public final class BotSceneVariableDifferencePersistence extends BaseRootPersistence<BotSceneParamDTO> {
  public BotSceneVariableDifferencePersistence(BotSceneRelaManageMapper botSceneRelaManageMapper) {
    // 新增情况
    this.setAddConsumer(botSceneRelaManageMapper::insertSceneParam);
    // 修改情况
    this.setModifyConsumer(botSceneRelaManageMapper::updateSceneParam);
  }
}
