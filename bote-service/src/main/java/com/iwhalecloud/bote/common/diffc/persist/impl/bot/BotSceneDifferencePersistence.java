package com.iwhalecloud.bote.common.diffc.persist.impl.bot;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.mapper.bot.BotSceneManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：场景
 *
 * @author chen.linfa
 * @since 2024-08-02
 */
@Component
public final class BotSceneDifferencePersistence extends BaseRootPersistence<BotSceneDTO> {
  public BotSceneDifferencePersistence(BotSceneManageMapper botSceneManageMapper) {
    // 新增情况
    this.setAddConsumer(botSceneManageMapper::insertScene);
    // 修改情况
    this.setModifyConsumer(botSceneManageMapper::updateScene);
  }
}
