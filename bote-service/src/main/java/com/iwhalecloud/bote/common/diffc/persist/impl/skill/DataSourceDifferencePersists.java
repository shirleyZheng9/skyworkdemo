package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.skill.DataSourceDTO;
import com.iwhalecloud.bote.mapper.skill.DataSourceManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务: 数据源
 *
 * @author qian.sisheng
 * @since 2024/8/6
 */

@Component
public final class DataSourceDifferencePersists extends BaseRootPersistence<DataSourceDTO> {
  public DataSourceDifferencePersists(DataSourceManageMapper dataSourceManageMapper) {
    // 新增情况
    setAddConsumer(dataSourceManageMapper::insertDataSource);
    // 修改情况
    setModifyConsumer(dataSourceManageMapper::updateDataSource);
  }
}
