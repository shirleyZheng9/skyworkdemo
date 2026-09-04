package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.database.SimpleDataTableColumnDTO;
import com.iwhalecloud.bote.dto.database.SimpleDataTableDTO;
import com.iwhalecloud.bote.mapper.database.DataTableQueryMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 表定义缓存
 * <p>缓存 key 为 tenantId:tableId</p>
 *
 * @author chen.linfa
 * @since 2025-11-25
 */
@Component
public final class DataTableCache extends AbstractSkillCache<SimpleDataTableDTO> {
  private final DataTableQueryMapper tableQueryMapper;

  public DataTableCache(DataTableQueryMapper tableQueryMapper) {
    super(CacheConsts.KEY_PREFIX_DATA_TABLE);
    this.tableQueryMapper = tableQueryMapper;
    disableDistributionCache();
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_DATA_TABLE;
  }

  @Nullable
  @Override
  protected SimpleDataTableDTO loadById(Long tenantId, Long id) {
    SimpleDataTableDTO table = tableQueryMapper.selectSimpleTable(tenantId, id);
    if (table != null) {
      table.setColumns(tableQueryMapper.selectSimpleTableColumnList(tenantId, Collections.singletonList(id)));
    }
    return table;
  }

  @Override
  protected Map<Long, SimpleDataTableDTO> loadByIds(Long tenantId, List<Long> ids) {
    List<SimpleDataTableDTO> tables = tableQueryMapper.selectSimpleTableList(tenantId, ids);
    if (CollectionUtils.isEmpty(tables)) {
      return Map.of();
    }
    Map<Long, List<SimpleDataTableColumnDTO>> columns = CollectionUtils.emptyIfNull(tableQueryMapper.selectSimpleTableColumnList(tenantId, ids))
      .stream().collect(Collectors.groupingBy(SimpleDataTableColumnDTO::getTableId));
    for (SimpleDataTableDTO table : tables) {
      table.setColumns(columns.get(table.getTableId()));
    }
    return tables.stream().collect(Collectors.toMap(SimpleDataTableDTO::getTableId, table -> table));
  }

  /**
   * 刷新表定义缓存
   *
   * @param tenantId 租户ID
   * @param tableId 表ID
   */
  public void refreshTable(Long tenantId, Long tableId) {
    String key = tenantId + CacheConsts.COLON + tableId;
    refresh(Collections.singletonList(key));
  }

}
