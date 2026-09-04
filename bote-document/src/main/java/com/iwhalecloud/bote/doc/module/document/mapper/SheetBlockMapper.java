package com.iwhalecloud.bote.doc.module.document.mapper;

import com.iwhalecloud.bote.doc.module.document.entity.SheetBlockEntity;
import org.apache.ibatis.annotations.Param;

/**
 * Sheet数据块相关数据库操作
 *
 * @author Aiqing
 * @since 2025-08-15
 */
public interface SheetBlockMapper {

  /**
   * 根据主键查询
   */
  SheetBlockEntity selectByPrimaryKey(@Param("blockId") Long blockId);

  /**
   * 根据文件ID查询
   */
  SheetBlockEntity selectByFileId(@Param("fileId") Long fileId);

  /**
   * 插入记录
   */
  int insert(@Param("entity") SheetBlockEntity entity);

  /**
   * 逻辑删除
   */
  int deleteByPrimaryKey(@Param("blockId") Long blockId);
}
