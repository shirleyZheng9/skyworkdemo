package com.iwhalecloud.bote.mapper.database;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.database.TableModelItemDTO;
import com.iwhalecloud.bote.entity.database.TableModelItemEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;


/**
 * 表建模记录 Mapper
 *
 * @author wangtingyun
 * @since 2025-11-30
 */
public interface TableModelItemMapper {

  /**
   * 根据itemId、tenantId获取表建模记录
   *
   * @param itemId 主键ID
   * @param tenantId 租户ID
   * @return 表建模记录
   */
  TableModelItemDTO selectTableModelItem(@Param("itemId") Long itemId, @Param("tenantId") Long tenantId);

  /**
   * 新增表建模记录
   *
   * @param tableModelItem 表建模记录
   * @return 结果
   */
  int insertTableModelItem(@Param("dto") TableModelItemEntity tableModelItem);

  /**
   * 获取表建模记录列表（分页）
   *
   * @param tableId 表ID
   * @param tenantId 租户ID
   * @param rowBounds 分页参数
   * @return 表建模记录分页列表
   */
  Page<TableModelItemDTO> selectTableModelItemPage(@Param("tableId") Long tableId, @Param("tenantId") Long tenantId, RowBounds rowBounds);

  /**
   * 查询表建模记录列表（用于脚本导出）
   *
   * @param tableIds 表ID列表
   * @param tenantId 租户ID
   * @return 表建模记录列表
   */
  List<TableModelItemDTO> selectItemListForExport(@Param("tableIds") List<Long> tableIds, @Param("tenantId") Long tenantId);

  /**
   * 删除表建模记录
   *
   * @param itemId 主键ID
   * @param tenantId 租户ID
   * @param updatorId 更新人ID
   * @return 结果
   */
  int deleteTableModelItem(@Param("itemId") Long itemId, @Param("tenantId") Long tenantId, @Param("updatorId") Long updatorId);

}

