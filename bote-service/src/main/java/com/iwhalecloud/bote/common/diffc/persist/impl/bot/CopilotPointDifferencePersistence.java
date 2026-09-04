package com.iwhalecloud.bote.common.diffc.persist.impl.bot;

import com.iwhalecloud.bote.dto.bot.CopilotPointDTO;
import com.iwhalecloud.bote.mapper.bot.CopilotPointManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：鉴权指令
 *
 * @author auto
 * @since 2024-09-19
 */
@Component
public final class CopilotPointDifferencePersistence extends BaseRootPersistence<CopilotPointDTO> {

  public CopilotPointDifferencePersistence(CopilotPointManageMapper mapper) {
    setAddConsumer(mapper::insertPoint);
    setModifyConsumer(mapper::updatePoint);
  }

}
