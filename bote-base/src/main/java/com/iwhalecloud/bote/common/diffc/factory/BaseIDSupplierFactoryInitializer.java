package com.iwhalecloud.bote.common.diffc.factory;

import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.dto.base.LabelDTO;
import com.iwhalecloud.bote.dto.base.LabelObjectRelDTO;
import com.iwhalecloud.bote.dto.job.JobDTO;
import com.iwhalecloud.bss.litchi.diffc.factory.IDSupplierFactory;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import org.springframework.stereotype.Component;

/**
 * 主键提供者工厂初始化
 *
 * @author chen.linfa
 * @since 2025-11-07
 */
@Component
public class BaseIDSupplierFactoryInitializer {
  public BaseIDSupplierFactoryInitializer() {
    // 目录
    IDSupplierFactory.register(CatalogDTO.class, IDUtils::nextId);

    // 定时任务
    IDSupplierFactory.register(JobDTO.class, IDUtils::nextId);

    // 标签
    IDSupplierFactory.register(LabelDTO.class, IDUtils::nextId);
    // 关联标签
    IDSupplierFactory.register(LabelObjectRelDTO.class, IDUtils::nextId);
  }
}
