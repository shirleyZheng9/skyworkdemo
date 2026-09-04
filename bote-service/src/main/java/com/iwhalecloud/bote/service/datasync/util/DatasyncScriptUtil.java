package com.iwhalecloud.bote.service.datasync.util;

import com.iwhalecloud.bote.dto.database.TableModelItemDTO;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bote.entity.database.TableModelItemEntity;
import com.iwhalecloud.bote.mapper.database.TableModelItemMapper;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * 数据同步辅助工具类 - 模型脚本
 *
 * @author wangtingyun
 * @since 2025-12-04
 */
@SuppressFBWarnings("SQL_INJECTION_SPRING_JDBC")
public final class DatasyncScriptUtil {

  private DatasyncScriptUtil() {
  }

  /** 数据表节点编码 */
  private static final String DATA_TABLE = "data_table";

  /**
   * 收集 bt_table_model_item 相关模型脚本，导出脚本 sql
   *
   * @param params 收集条件
   */
  public static void export(DataSyncParams params) {
    // 解析需要导出建模的表
    Map<String, String> codeAndIds = params.getCodeAndIds();
    String tableIds = MapUtils.getString(codeAndIds, DATA_TABLE);
    if (!params.getSyncAll() && StringUtils.isBlank(tableIds)) {
      return;
    }
    List<Long> tableIdList = params.getSyncAll() ? null : Arrays.stream(tableIds.split("/")).map(Long::valueOf).toList();

    // 查询建模脚本
    TableModelItemMapper modelItemMapper = SpringUtil.getBean(TableModelItemMapper.class);
    List<TableModelItemDTO> modelItemList = modelItemMapper.selectItemListForExport(tableIdList, params.getTenantId());

    // 按照数据源编码进行分组
    Map<String, List<TableModelItemDTO>> dataSourceGroup = modelItemList.stream().filter(p -> StringUtils.isNotEmpty(p.getChangeSql()))
      .collect(Collectors.groupingBy(TableModelItemDTO::getDataSourceCode));
    if (MapUtils.isEmpty(dataSourceGroup)) {
      return;
    }

    for (Entry<String, List<TableModelItemDTO>> entry : dataSourceGroup.entrySet()) {
      StringBuilder sqlBuilder = new StringBuilder();
      // 按照表的粒度进行分组，方便阅读脚本内容
      Map<Long, List<TableModelItemDTO>> tableGroup = entry.getValue().stream().collect(Collectors.groupingBy(TableModelItemDTO::getTableId));
      for (Entry<Long, List<TableModelItemDTO>> tableEntry : tableGroup.entrySet()) {
        List<TableModelItemDTO> tableItemList = tableEntry.getValue();
        // 按 itemId 进行排序
        tableItemList.sort(Comparator.comparing(TableModelItemEntity::getItemId));
        List<String> changeSqlList = tableItemList.stream().map(TableModelItemDTO::getChangeSql).toList();
        for (String changeSql : changeSqlList) {
          Arrays.asList(changeSql.split(";")).forEach(sql -> sqlBuilder.append(sql).append(";").append(System.lineSeparator()));
        }
        sqlBuilder.append(System.lineSeparator());
      }
      // 写入脚本
      DataSyncDirUtil.createScriptFile(params, entry.getKey() + ".sql", sqlBuilder.toString());
    }
  }

}
