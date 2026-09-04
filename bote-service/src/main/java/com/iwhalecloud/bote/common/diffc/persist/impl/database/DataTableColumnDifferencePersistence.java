package com.iwhalecloud.bote.common.diffc.persist.impl.database;

import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.database.DataTableColumnDTO;
import com.iwhalecloud.bote.mapper.database.DataTableColumnMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据差异服务：业务数据表字段
 *
 * @author tingyun.wang
 * @since 2025-11-19
 */
@Component
public final class DataTableColumnDifferencePersistence extends BaseRootPersistence<DataTableColumnDTO> {

  public DataTableColumnDifferencePersistence(DataTableColumnMapper dataTableColumnMapper) {
    // 批量新增情况
    this.setBatchAddConsumer(dataTableColumnMapper::insertDataTableColumnBatch);
    // 修改情况
    this.setModifyConsumer(dataTableColumnMapper::updateDataTableColumn);
    // 批量删除情况
    this.setBatchRemoveConsumer(columns -> {
      List<Long> columnIds = CollectionUtils.emptyIfNull(columns).stream().map(DataTableColumnDTO::getTableColumnId).toList();
      if (CollectionUtils.isEmpty(columnIds)) {
        return;
      }
      dataTableColumnMapper.batchDeleteByColumnIds(columnIds, columns.getFirst().getTenantId(), SessionUtil.getLoginInfo().getUserId());
    });
  }

}
