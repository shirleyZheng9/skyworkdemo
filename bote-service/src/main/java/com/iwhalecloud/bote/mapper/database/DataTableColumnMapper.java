package com.iwhalecloud.bote.mapper.database;

import com.iwhalecloud.bote.dto.database.DataTableColumnDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 数据表字段 Mapper
 *
 * @author wangtingyun
 * @since 2025-11-19
 */
public interface DataTableColumnMapper {

  /**
   * 根据字段ID、租户ID获取字段信息
   *
   * @param tableColumnId 字段ID
   * @param tenantId 租户ID
   * @return 字段信息
   */
  DataTableColumnDTO selectDataTableColumn(@Param("tableColumnId") Long tableColumnId, @Param("tenantId") Long tenantId);

  /**
   * 批量新增数据表字段
   *
   * @param columnList 字段信息列表
   * @return 结果
   */
  int insertDataTableColumnBatch(@Param("columnList") List<DataTableColumnDTO> columnList);

  /**
   * 修改数据表字段
   *
   * @param column 字段信息
   * @return 结果
   */
  int updateDataTableColumn(@Param("dto") DataTableColumnDTO column);

  /**
   * 根据表ID获取字段列表
   *
   * @param tableId 表ID
   * @param tenantId 租户ID
   * @return 字段列表
   */
  List<DataTableColumnDTO> selectDataTableColumnList(@Param("tableId") Long tableId, @Param("tenantId") Long tenantId);

  /**
   * 查询表字段基本信息列表
   *
   * @param tableId 表ID
   * @param tenantId 租户ID
   * @return 字段列表
   */
  List<DataTableColumnDTO> selectColumnBasicInfoList(@Param("tableId") Long tableId, @Param("tenantId") Long tenantId);

  /**
   * 检查字段编码是否存在
   *
   * @param tableId 表ID
   * @param tenantId 租户ID
   * @param columnCode 字段编码
   * @return 校验结果
   */
  boolean checkDataTableColumnExists(@Param("tableId") Long tableId, @Param("tenantId") Long tenantId, @Param("columnCode") String columnCode);

  /**
   * 批量删除数据表字段
   *
   * @param tableColumnIds 字段ID列表
   * @param tenantId 租户ID
   * @param updatorId 更新人ID
   * @return 结果
   */
  int batchDeleteByColumnIds(@Param("tableColumnIds") List<Long> tableColumnIds, @Param("tenantId") Long tenantId, @Param("updatorId") Long updatorId);

}

