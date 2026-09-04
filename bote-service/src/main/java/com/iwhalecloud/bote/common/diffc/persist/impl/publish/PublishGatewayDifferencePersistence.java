package com.iwhalecloud.bote.common.diffc.persist.impl.publish;

import com.iwhalecloud.bote.dto.publish.PublishGatewayDTO;
import com.iwhalecloud.bote.mapper.publish.PublishGatewayManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：在线环境维护
 *
 * <p>
 * 负责在 DataDifferenceStarter.computeSave 调用时，将新增映射到 insert，修改映射到 update。
 * </p>
 *
 * @author lizuyin
 * @since 2026-01-21
 */
@Component
public final class PublishGatewayDifferencePersistence extends BaseRootPersistence<PublishGatewayDTO> {

  public PublishGatewayDifferencePersistence(PublishGatewayManageMapper mapper) {
    setAddConsumer(mapper::insertPublishGateway);
    setModifyConsumer(mapper::updatePublishGateway);
  }
}

