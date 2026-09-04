package com.iwhalecloud.bote.service.database.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.sql.consts.DatabaseDataTypeEnum;
import com.iwhalecloud.bote.dto.database.DataTableColumnDTO;
import com.iwhalecloud.bote.dto.database.DataTableDTO;
import com.iwhalecloud.bote.dto.database.query.DataTableQueryParams;
import com.iwhalecloud.bote.mapper.database.DataTableMapper;
import com.iwhalecloud.bote.service.base.IDataSourceProviderService;
import com.iwhalecloud.bote.service.database.ITableModelService;
import com.iwhalecloud.bss.litchi.database.inspect.DatabaseInspector;
import com.iwhalecloud.bss.litchi.database.inspect.definition.Table;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 表模型管理服务
 *
 * @author wangtingyun
 * @since 2025-11-27
 */
@Service
@RequiredArgsConstructor
public class TableModelServiceImpl implements ITableModelService {

  private final IDataSourceProviderService dataSourceProvider;
  private final DataTableMapper dataTableMapper;

  /** 缓存指定数据源对应的表集合，key 为 租户ID-数据源编码 */
  private final LoadingCache<Pair<Long, Long>, List<Table>> tableListCache = CacheBuilder.newBuilder()
    .expireAfterWrite(Duration.ofHours(6))
    .build(new CacheLoader<>() {
      @Override
      public List<Table> load(@NonNull Pair<Long, Long> key) {
        DataSource dataSource = dataSourceProvider.getDataSource(key.getLeft(), key.getRight());
        return DatabaseInspector.listTables(dataSource);
      }
    });

  @Override
  public List<DataTableDTO> getTableListByDataSource(Long tenantId, Long dataSourceId) {
    Assert.notNull(tenantId, "租户ID不能为空");
    Assert.notNull(dataSourceId, "数据源ID不能为空");
    // 查找数据源对应的数据表
    List<Table> tableList = tableListCache.getUnchecked(ImmutablePair.of(tenantId, dataSourceId));
    List<DataTableDTO> dataTableList = ListUtils.emptyIfNull(tableList).stream()
      .map(table -> convertToModelTable(table, tenantId, dataSourceId)).toList();
    // 过滤租户下已有的数据表
    DataTableQueryParams queryParams = new DataTableQueryParams();
    queryParams.setTenantId(tenantId);
    List<DataTableDTO> existTableList = dataTableMapper.selectDataTableList(queryParams);
    if (CollectionUtils.isNotEmpty(existTableList)) {
      List<String> existTableCodes = existTableList.stream()
        .map(existTable -> existTable.getTableCode().toLowerCase()).toList();
      dataTableList = dataTableList.stream()
        .filter(table -> !existTableCodes.contains(table.getTableCode().toLowerCase())).toList();
    }
    return dataTableList;
  }

  @Override
  public DataTableDTO getTableByDataSource(Long tenantId, Long dataSourceId, String tableName) {
    Assert.notNull(tenantId, "租户ID不能为空");
    Assert.notNull(dataSourceId, "数据源ID不能为空");
    Assert.hasText(tableName, "数据表编码不能为空");

    DataSource dataSource = dataSourceProvider.getDataSource(tenantId, dataSourceId);
    Table table = DatabaseInspector.inspectTable(dataSource, tableName);
    if (table == null) {
      throw BaseErrorConstant.INSPECT_TABLE_SCHEMA_FAIL.toException(tableName);
    }
    return convertToModelTable(table, tenantId, dataSourceId);
  }

  /**
   * 表模型转换程数据表结构
   */
  private DataTableDTO convertToModelTable(Table table, Long tenantId, Long dataSourceId) {
    // 构建表信息
    DataTableDTO dataTableDTO = new DataTableDTO();
    dataTableDTO.setTenantId(tenantId);
    dataTableDTO.setTableCode(table.getName());
    dataTableDTO.setTableName(table.getRemarks());
    dataTableDTO.setTableDesc(table.getRemarks());
    dataTableDTO.setDataSourceId(dataSourceId);
    // 当前只支持探测自定义数据源
    dataTableDTO.setDataSourceChannel(BaseConsts.DATABASE_TUNNEL_CUSTOM);

    // 填充表字段信息
    List<DataTableColumnDTO> appModelTableColumns = ListUtils.emptyIfNull(table.getColumns()).stream().map(column -> {
      DataTableColumnDTO tableColumn = new DataTableColumnDTO();
      tableColumn.setTenantId(tenantId);
      tableColumn.setColumnCode(column.getName());
      String remarks = column.getRemarks();
      if (StringUtils.isEmpty(remarks)) {
        remarks = column.getName();
      }
      else if (remarks.length() > 50) {
        remarks = remarks.substring(0, 50);
      }
      tableColumn.setColumnName(remarks);
      String dataType = DatabaseDataTypeEnum.findColumnDataType(column.getTypeName().replace(" ", "").toUpperCase());
      tableColumn.setDataType(dataType);
      tableColumn.setDataLength(column.getSize() <= 0 ? 32L : column.getSize());
      tableColumn.setPrimaryKey(ListUtils.emptyIfNull(table.getPrimaryKey()).contains(column.getName()) ? BaseConsts.TRUE : BaseConsts.FALSE);
      tableColumn.setNullable(BooleanUtils.isTrue(column.getNullable()) ? BaseConsts.TRUE : BaseConsts.FALSE);
      tableColumn.setPlatformColumn(BaseConsts.FALSE);
      return tableColumn;
    }).toList();
    dataTableDTO.setTableColumns(appModelTableColumns);
    return dataTableDTO;
  }

  @Override
  public void refreshDataSourceTableList(Long tenantId, Long dataSourceId) {
    Assert.notNull(tenantId, "租户ID不能为空");
    Assert.notNull(dataSourceId, "数据源ID不能为空");

    // 刷新表缓存
    Map<Pair<Long, Long>, List<Table>> tableListMap = tableListCache.asMap();
    Pair<Long, Long> key = IterableUtils.find(tableListMap.keySet(),
      p -> Objects.equals(p.getLeft(), tenantId) && Objects.equals(dataSourceId, p.getRight()));
    if (key != null) {
      tableListCache.invalidate(key);
    }
    // 重新探测
    DataSource dataSource = dataSourceProvider.getDataSource(tenantId, dataSourceId);
    tableListCache.put(ImmutablePair.of(tenantId, dataSourceId), DatabaseInspector.listTables(dataSource));
  }

}
