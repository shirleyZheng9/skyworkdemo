package com.iwhalecloud.bote.doc.module.document.service;

import com.iwhalecloud.bote.doc.module.document.entity.SheetBlockEntity;

/**
 * 在线表格sheet 数据处理service
 *
 * @author Aiqing
 * @since 2025/9/2
 */
public interface ISheetBlockService {

  /**
   * 根据blockId查询sheet的black数据
   *
   * @param blockId 主键ID
   * @return sheetBlock
   */
  SheetBlockEntity queryByBlockId(Long blockId);
}
