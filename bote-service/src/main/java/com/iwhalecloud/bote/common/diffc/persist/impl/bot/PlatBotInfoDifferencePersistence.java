package com.iwhalecloud.bote.common.diffc.persist.impl.bot;

import com.iwhalecloud.bote.dto.bot.PlatBotInfoDTO;
import com.iwhalecloud.bote.mapper.bot.PlatBotInfoManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：应用模板
 *
 * @author auto
 * @since 2025-05-26
 */
@Component
public final class PlatBotInfoDifferencePersistence extends BaseRootPersistence<PlatBotInfoDTO> {

  public PlatBotInfoDifferencePersistence(PlatBotInfoManageMapper platBotInfoManageMapper) {
    setAddConsumer(platBotInfoManageMapper::insertPlatBotInfo);
    setBatchAddConsumer(platBotInfoManageMapper::batchInsertPlatBotInfo);
    setModifyConsumer(platBotInfoManageMapper::updatePlatBotInfo);
  }

}
