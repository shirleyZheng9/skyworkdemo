package com.iwhalecloud.bote.common.diffc.persist.impl.publish;

import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
import com.iwhalecloud.bote.mapper.publish.ResourcePublishRecordMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 资源发布记录的差异化持久化
 *
 * <p>
 * 负责在 DataDifferenceStarter.computeSave 调用时，将新增映射到 insert，修改映射到 update。
 * </p>
 *
 * @author lizuyin
 * @since 2025-08-14
 */
@Component
public final class ResourcePublishRecordDifferencePersistence extends BaseRootPersistence<ResourcePublishRecordDTO> {

  public ResourcePublishRecordDifferencePersistence(ResourcePublishRecordMapper mapper) {
    setAddConsumer(mapper::insertResourcePublishRecord);
    setModifyConsumer(mapper::updateResourcePublishRecord);
  }
}


