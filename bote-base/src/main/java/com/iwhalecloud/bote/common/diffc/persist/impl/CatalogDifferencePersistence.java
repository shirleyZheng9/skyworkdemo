package com.iwhalecloud.bote.common.diffc.persist.impl;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.mapper.base.CatalogManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：目录
 *
 * @author chen.linfa
 * @since 2024-08-05
 */
@Component
public final class CatalogDifferencePersistence extends BaseRootPersistence<CatalogDTO> {
  public CatalogDifferencePersistence(CatalogManageMapper catalogManageMapper) {
    // 新增情况
    this.setAddConsumer(catalogManageMapper::insertCatalog);
    // 修改情况
    this.setModifyConsumer(catalogManageMapper::updateCatalog);
  }

}
