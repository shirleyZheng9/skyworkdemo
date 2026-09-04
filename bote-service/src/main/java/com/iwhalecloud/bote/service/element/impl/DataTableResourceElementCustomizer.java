package com.iwhalecloud.bote.service.element.impl;

import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.database.DataTableDTO;
import com.iwhalecloud.bote.mapper.database.DataTableMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置数据实体关系记录 - 业务数据表
 *
 * @author wangtingyun
 * @since 2025-12-02
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.DATA_TABLE)
public class DataTableResourceElementCustomizer extends AbstractResourceElementCustomizer {

  private final DataTableMapper dataTableMapper;

  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.DATA_TABLE.getCode();
  }

  /**
   * <p>1.目录 </p>
   * <p>2.数据源 </p>
   */
  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long tableId) {
    DataTableDTO tableDTO = dataTableMapper.selectDataTable(tableId, tenantId);
    // 目录
    List<ResourceElementDTO> elements = new ArrayList<>(createCatalogElement(tenantId, tableId, tableDTO.getCatalogItemId()));
    // 数据源
    elements.add(createElement(tenantId, tableId, tableDTO.getDataSourceId(), DataSyncCodeEnum.DATA_SOURCE.getCode()));
    return elements;
  }
}
