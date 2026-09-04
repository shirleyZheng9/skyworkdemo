package com.iwhalecloud.bote.doc.module.document.service.impl;

import com.iwhalecloud.bote.doc.module.document.entity.SheetBlockEntity;
import com.iwhalecloud.bote.doc.module.document.mapper.SheetBlockMapper;
import com.iwhalecloud.bote.doc.module.document.service.ISheetBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 在线表格sheet 数据处理service
 *
 * @author Aiqing
 * @since 2025/9/2
 */
@Service
@RequiredArgsConstructor
public class SheetBlockServiceImpl implements ISheetBlockService {

  private final SheetBlockMapper sheetBlockMapper;

  @Override
  public SheetBlockEntity queryByBlockId(Long blockId) {
    return sheetBlockMapper.selectByPrimaryKey(blockId);
  }
}
