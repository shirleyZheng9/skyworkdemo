package com.iwhalecloud.bote.common.diffc.factory;

import com.iwhalecloud.bote.common.diffc.persist.impl.CatalogDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.JobDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.LabelDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.LabelObjectRelDifferencePersistence;
import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.dto.base.LabelDTO;
import com.iwhalecloud.bote.dto.base.LabelObjectRelDTO;
import com.iwhalecloud.bote.dto.job.JobDTO;
import com.iwhalecloud.bss.litchi.diffc.factory.BatchPersistenceFactory;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务工厂初始化
 *
 * @author chen.linfa
 * @since 2025-11-07
 */
@Component
public class BasePersistenceFactoryInitializer {
  public BasePersistenceFactoryInitializer() {
    // 目录
    BatchPersistenceFactory.register(CatalogDTO.class, CatalogDifferencePersistence.class);

    // 定时任务
    BatchPersistenceFactory.register(JobDTO.class, JobDifferencePersistence.class);

    // 标签
    BatchPersistenceFactory.register(LabelDTO.class, LabelDifferencePersistence.class);
    // 关联标签
    BatchPersistenceFactory.register(LabelObjectRelDTO.class, LabelObjectRelDifferencePersistence.class);
  }
}
