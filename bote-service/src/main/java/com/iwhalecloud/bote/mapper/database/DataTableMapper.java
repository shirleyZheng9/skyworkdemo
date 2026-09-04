package com.iwhalecloud.bote.mapper.database;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.database.DataTableDTO;
import com.iwhalecloud.bote.dto.database.query.DataTableQueryParams;
import com.iwhalecloud.bote.entity.database.DataTableEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;


/**
 * 数据表 Mapper
 *
 * @author auto
 * @since 2024-12-18
 */
public interface DataTableMapper {

  /**
   * 根据tableId、tenantId获取数据表
   *
   * @param tableId 表ID
   * @param tenantId 租户ID
   * @return 数据表
   */
  DataTableDTO selectDataTable(@Param("tableId") Long tableId, @Param("tenantId") Long tenantId);

  /**
   * 新增数据表
   *
   * @param dataTable 数据表
   * @return 结果
   */
  int insertDataTable(@Param("dto") DataTableEntity dataTable);

  /**
   * 修改数据表
   *
   * @param dataTable 数据表
   * @return 结果
   */
  int updateDataTable(@Param("dto") DataTableDTO dataTable);

  /**
   * 获取数据表列表（分页）
   *
   * @param queryParams 查询条件
   * @param rowBounds 分页参数
   * @return 数据表分页列表
   */
  Page<DataTableDTO> selectDataTablePage(@Param("query") DataTableQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询数据表列表
   *
   * @param queryParams 查询条件
   * @return 数据表列表
   */
  List<DataTableDTO> selectDataTableList(@Param("query") DataTableQueryParams queryParams);

  /**
   * 检查表编码是否已存在
   *
   * @param tenantId 租户ID
   * @param tableCode 表编码
   * @return 检查结果
   */
  boolean checkDataTableExists(@Param("tenantId") Long tenantId, @Param("tableCode") String tableCode);

  /**
   * 删除数据表
   *
   * @param tableId 表ID
   * @param tenantId 租户ID
   * @param updatorId 更新人ID
   * @param backupTableCode 删除后设置的临时表编码
   * @return 结果
   */
  int deleteDataTable(@Param("tableId") Long tableId, @Param("tenantId") Long tenantId,
                      @Param("updatorId") Long updatorId, @Param("backupTableCode") String backupTableCode);

  /**
   * 查询数据表图标
   *
   * @param tableId 表ID
   * @param tenantId 租户ID
   * @return 数据表图标
   */
  String selectDataTableIcon(@Param("tableId") Long tableId, @Param("tenantId") Long tenantId);

  /**
   * 查询数据表基本信息
   *
   * @param tableId 表ID
   * @param tenantId 租户ID
   * @return 数据表
   */
  DataTableDTO selectDataTableBasicInfo(@Param("tableId") Long tableId, @Param("tenantId") Long tenantId);

}