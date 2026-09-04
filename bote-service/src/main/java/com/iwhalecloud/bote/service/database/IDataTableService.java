package com.iwhalecloud.bote.service.database;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.database.DataSaveDTO;
import com.iwhalecloud.bote.dto.database.DataTableColumnDTO;
import com.iwhalecloud.bote.dto.database.DataTableDTO;
import com.iwhalecloud.bote.dto.database.SimpleDataTableDTO;
import com.iwhalecloud.bote.dto.database.query.DataTableQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

import java.util.List;
import java.util.Map;

/**
 * 业务数据表服务
 *
 * @author tingyun.wang
 * @since 2025-11-18
 */
public interface IDataTableService {

  /**
   * 保存数据表
   *
   * @param tableDTO 数据表
   * @return 保存结果
   */
  ResultVO<DataTableDTO> saveDataTable(DataTableDTO tableDTO);

  /**
   * 批量导入保存数据表（外部数据源导入）
   *
   * @param tableDTOList 数据表列表
   */
  void batchImportDataTable(List<DataTableDTO> tableDTOList);

  /**
   * 校验表编码是否可用
   *
   * @param tenantId 租户ID
   * @param tableCode 表编码
   */
  void checkTableCode(Long tenantId, String tableCode);

  /**
   * 查询数据表详情
   *
   * @param tableId 表ID
   * @param tenantId 租户ID
   * @return 数据表详情
   */
  DataTableDTO getDataTable(Long tableId, Long tenantId);

  /**
   * 分页查询业务数据表
   *
   * @param query 查询参数
   * @return 业务数据表列表
   */
  PageInfo<DataTableDTO> getDataTablePage(DataTableQueryParams query);

  /**
   * 编辑数据表基本信息
   *
   * @param tableDTO 数据表对象
   */
  ResultVO<DataTableDTO> editDataTableInfo(DataTableDTO tableDTO);

  /**
   * 删除数据表
   *
   * @param tableId 表ID
   * @param tenantId 租户ID
   */
  ResultVO<Integer> deleteDataTable(Long tableId, Long tenantId);

  /**
   * 查询数据表图标
   *
   * @param tableId 表ID
   * @param tenantId 租户ID
   * @return 数据表图标
   */
  String getDataTableIcon(Long tableId, Long tenantId);

  /**
   * 检查是否开启平台数据库
   *
   * @return 平台数据库开启结果
   */
  boolean checkPlatformDatabaseEnabled();

  /**
   * 获取系统预置的数据表字段
   *
   * @return 系统预置的数据表字段
   */
  List<DataTableColumnDTO> getPlatformColumns();

  /**
   * 分页查询数据表测试数据
   *
   * @param query 查询参数
   * @return 表测试数据
   */
  PageInfo<Map<String, Object>> getTestDataPage(DataTableQueryParams query);

  /**
   * 保存数据表测试数据
   *
   * @param dataSaveDTO 保存数据对象
   */
  void saveTestData(DataSaveDTO dataSaveDTO);

  /**
   * 分页查询表定义，用于其他模块引用
   *
   * @param query 查询参数
   * @return 表定义数据（分页）
   */
  PageInfo<SimpleDataTableDTO> querySimpleDataTablePage(DataTableQueryParams query);

  /**
   * 查询表字段列表
   *
   * @param tenantId 租户ID
   * @param tableId 表ID
   * @return 表字段列表
   */
  List<DataTableColumnDTO> getTableColumns(Long tenantId, Long tableId);

  /**
   * 查询数据表基本信息
   *
   * @param tenantId 租户ID
   * @param tableId 表ID
   * @return 表基本信息
   */
  DataTableDTO getTableBasicInfo(Long tenantId, Long tableId);

}
