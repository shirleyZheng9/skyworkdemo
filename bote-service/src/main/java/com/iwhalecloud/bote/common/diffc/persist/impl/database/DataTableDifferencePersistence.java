package com.iwhalecloud.bote.common.diffc.persist.impl.database;

import com.iwhalecloud.bote.dto.database.DataTableDTO;
import com.iwhalecloud.bote.mapper.database.DataTableMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异服务：业务数据表
 *
 * @author tingyun.wang
 * @since 2025-11-19
 */
@Component
public final class DataTableDifferencePersistence extends BaseRootPersistence<DataTableDTO> {

  public DataTableDifferencePersistence(DataTableMapper dataTableMapper) {
    // 新增情况
    this.setAddConsumer(dataTableMapper::insertDataTable);
    // 修改情况
    this.setModifyConsumer(dataTableMapper::updateDataTable);
  }

}
