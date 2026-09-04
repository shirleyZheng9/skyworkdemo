package com.iwhalecloud.bote.service.publish.step.datasync;

import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.service.datasync.IDataSyncService;
import com.iwhalecloud.bote.service.datasync.impl.DataSyncConsumer;
import com.iwhalecloud.bote.service.datasync.impl.DataSyncProducer;
import com.iwhalecloud.bote.service.publish.step.AbstractPublishStep;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

/**
 * 步骤执行器：数据同步抽象类
 *
 * @author chen.linfa
 * @since 2025-02-18
 */
public abstract class AbstractDataSyncStep<T> extends AbstractPublishStep<T> {

  protected static final IDataSyncService dataSyncService = SpringUtil.getBean(IDataSyncService.class);
  protected static final DataSyncProducer producer = SpringUtil.getBean(DataSyncProducer.class);
  protected static final DataSyncConsumer consumer = SpringUtil.getBean(DataSyncConsumer.class);

  public AbstractDataSyncStep(PublishRecordDTO record, PublishStepDTO step) {
    super(record, step);
  }
}
