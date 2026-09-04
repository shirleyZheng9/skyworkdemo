package com.iwhalecloud.bote.service.database;

import com.iwhalecloud.bote.dto.database.DataTableDTO;

import java.util.List;

/**
 * 表模型管理服务
 *
 * @author wangtingyun
 * @since 2025-11-28
 */
public interface ITableModelService {

  /**
   * 根据数据源获取表定义列表(不含表字段)
   *
   * @param tenantId 租户ID
   * @param dataSourceId 数据源ID
   * @return 表定义列表
   */
  List<DataTableDTO> getTableListByDataSource(Long tenantId, Long dataSourceId);

  /**
   * 根据表名获取单个表的定义信息
   *
   * @param tenantId 租户ID
   * @param dataSourceId 数据源ID
   * @param tableCode 表编码
   * @return 表的定义信息
   */
  DataTableDTO getTableByDataSource(Long tenantId, Long dataSourceId, String tableCode);

  /**
   * 刷新数据源对应的表列表缓存
   *
   * @param tenantId 租户ID
   * @param dataSourceId 数据源ID
   */
  void refreshDataSourceTableList(Long tenantId, Long dataSourceId);

}
