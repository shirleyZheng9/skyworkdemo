package com.iwhalecloud.bote.mapper.database;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.database.SimpleDataTableColumnDTO;
import com.iwhalecloud.bote.dto.database.SimpleDataTableDTO;
import com.iwhalecloud.bote.dto.database.query.DataTableQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 表查询
 *
 * @author chen.linfa
 * @since 2025-11-26
 */
public interface DataTableQueryMapper {
  /**
   * 查询表定义的简单信息
   */
  SimpleDataTableDTO selectSimpleTable(@Param("tenantId") Long tenantId, @Param("tableId") Long tableId);

  /**
   * 查询表定义的简单信息列表
   */
  List<SimpleDataTableDTO> selectSimpleTableList(@Param("tenantId") Long tenantId, @Param("ids") List<Long> tableIds);

  /**
   * 查询表定义的简单信息列表（分页）
   */
  Page<SimpleDataTableDTO> selectSimpleTablePage(@Param("query") DataTableQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询表字段的简单信息列表
   */
  List<SimpleDataTableColumnDTO> selectSimpleTableColumnList(@Param("tenantId") Long tenantId, @Param("ids") List<Long> tableIds);
}
