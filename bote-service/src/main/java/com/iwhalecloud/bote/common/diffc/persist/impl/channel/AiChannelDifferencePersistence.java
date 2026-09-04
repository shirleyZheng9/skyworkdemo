package com.iwhalecloud.bote.common.diffc.persist.impl.channel;

import com.iwhalecloud.bote.dto.channel.AiChannelDTO;
import com.iwhalecloud.bote.mapper.channel.AiChannelManagerMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：渠道
 *
 * @author wangtingyun
 * @since 2026-03-09
 */
@Component
public final class AiChannelDifferencePersistence extends BaseRootPersistence<AiChannelDTO> {

  public AiChannelDifferencePersistence(AiChannelManagerMapper mapper) {
    setAddConsumer(mapper::insertAiChannel);
    setModifyConsumer(mapper::updateAiChannel);
  }
}
