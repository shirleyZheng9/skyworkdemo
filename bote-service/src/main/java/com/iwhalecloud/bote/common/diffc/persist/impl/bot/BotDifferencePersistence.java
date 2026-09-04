package com.iwhalecloud.bote.common.diffc.persist.impl.bot;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.bot.BotDTO;
import com.iwhalecloud.bote.mapper.bot.BotManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：机器人
 *
 * @author chen.linfa
 * @since 2024-08-01
 */
@Component
public final class BotDifferencePersistence extends BaseRootPersistence<BotDTO> {
  public BotDifferencePersistence(BotManageMapper botManageMapper) {
    // 新增情况
    this.setAddConsumer(botManageMapper::insertBot);
    // 修改情况
    this.setModifyConsumer(botManageMapper::updateBot);
  }

}
