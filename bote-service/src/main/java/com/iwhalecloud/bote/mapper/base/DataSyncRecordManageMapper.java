package com.iwhalecloud.bote.mapper.base;

import com.iwhalecloud.bote.dto.datasync.DataSyncNodeDTO;
import com.iwhalecloud.bote.dto.datasync.DataSyncTableDefinition;
import java.util.List;

/**
 * 数据同步管理
 *
 * @author auto
 * @since 2024-10-21
 */
public interface DataSyncRecordManageMapper {

  /**
   * 查询数据同步表定义列表
   *
   * @return 表定义列表
   */
  List<DataSyncTableDefinition> selectTableDefinition();

  /**
   * 查询数据同步节点定义列表
   *
   * @return 节点列表
   */
  List<DataSyncNodeDTO> selectDataSyncNode();
}
