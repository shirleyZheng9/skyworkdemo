package com.iwhalecloud.bote.service.datasync.helper;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.jdbc.LowerCaseColumnMapRowMapper;
import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.dto.base.CatalogTree;
import com.iwhalecloud.bote.dto.datasync.DataSyncNodeDTO;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 查询配置数据辅助类
 *
 * @author chen.linfa
 * @since 2025-01-14
 */
@Component
@RequiredArgsConstructor
public class QueryDataRecordHelper {

  private final JdbcTemplate jdbcTemplate;

  @SuppressFBWarnings("SECSQLISPRJDBC")
  public void setDataRecord(Long tenantId, @Nullable Date startDate, DataSyncNodeDTO node, List<CatalogDTO> catalogs) {
    Date effectiveStartDate = startDate == null ? new Date(0L) : startDate;
    StringBuilder querySql = new StringBuilder();
    buildSelectAndFrom(querySql, node);
    appendWhereAndOrder(querySql, node, tenantId);
    List<Object> args = new ArrayList<>();
    args.add(effectiveStartDate);
    List<Map<String, Object>> records = jdbcTemplate.query(querySql.toString(), new LowerCaseColumnMapRowMapper(), args.toArray(new Object[0]));
    node.setTotal(records.size());
    assignRecordsToNode(node, catalogs, records);
  }

  private void buildSelectAndFrom(StringBuilder sql, DataSyncNodeDTO node) {
    String tableCode = node.getTableCode();
    if ("bt_prompt_basic".equalsIgnoreCase(tableCode)) {
      appendPromptBasicSelect(sql, node, tableCode);
    }
    else if ("bt_prompt_commit".equalsIgnoreCase(tableCode)) {
      appendPromptCommitSelect(sql, tableCode);
    }
    else {
      appendDefaultSelect(sql, node);
    }
  }

  private void appendPromptBasicSelect(StringBuilder sql, DataSyncNodeDTO node, String tableCode) {
    sql.append("SELECT id AS id, prompt_key AS code, name AS name, updated_by AS updator_name, ")
      .append("updated_at AS updated_time, created_by AS creator_name, created_at AS created_time");
    appendCatalogItemIdIfNeeded(sql, node);
    sql.append(" FROM ").append(tableCode).append(" WHERE 1=1 ");
  }

  private void appendPromptCommitSelect(StringBuilder sql, String tableCode) {
    sql.append("SELECT id AS id, prompt_key AS code, version AS name, committed_by AS updator_name, ")
      .append("updated_at AS updated_time, committed_by AS creator_name, created_at AS created_time");
    sql.append(" FROM ").append(tableCode).append(" WHERE 1=1 ");
  }

  private void appendDefaultSelect(StringBuilder sql, DataSyncNodeDTO node) {
    String nameColumn = DataSyncCodeEnum.CATALOG.getTableCode().equalsIgnoreCase(node.getTableCode())
      ? "CONCAT(CONCAT(catalog_name, '-'), catalog_type)" : node.getNameColumn();
    sql.append("SELECT ").append(node.getPrimaryColumn()).append(" AS id, ")
      .append(node.getCodeColumn()).append(" AS code, ").append(nameColumn).append(" AS name, ")
      .append("(SELECT u.real_name FROM bt_user u where u.user_id = ").append(node.getTableCode()).append(".updator_id) AS updator_name, ")
      .append(" updated_time,")
      .append("(SELECT u.real_name FROM bt_user u where u.user_id = ").append(node.getTableCode()).append(".creator_id) AS creator_name, ")
      .append(" created_time");
    appendCatalogItemIdIfNeeded(sql, node);
    sql.append(" FROM ").append(node.getTableCode()).append(" WHERE 1=1 ");
  }

  private void appendCatalogItemIdIfNeeded(StringBuilder sql, DataSyncNodeDTO node) {
    if (BaseConsts.TRUE.equals(node.getCatalogFlag())) {
      sql.append(" ,COALESCE(catalog_item_id, -1) AS catalog_item_id");
    }
  }

  private void appendWhereAndOrder(StringBuilder sql, DataSyncNodeDTO node, Long tenantId) {
    if (StringUtils.isNotEmpty(node.getQueryCondition())) {
      String condition = node.getQueryCondition().replace(":tenantId", String.valueOf(tenantId));
      sql.append(" AND ").append(condition);
    }
    String tableCode = node.getTableCode();
    boolean useUpdatedAt = "bt_prompt_basic".equalsIgnoreCase(tableCode) || "bt_prompt_commit".equalsIgnoreCase(tableCode);
    sql.append(useUpdatedAt ? " AND updated_at > ? " : " AND updated_time > ? ");
    sql.append(" ORDER BY updated_time DESC ");
  }

  private void assignRecordsToNode(DataSyncNodeDTO node, List<CatalogDTO> catalogs, List<Map<String, Object>> records) {
    if (CollectionUtils.isEmpty(records)) {
      return;
    }
    if (BaseConsts.TRUE.equals(node.getCatalogFlag())) {
      setTreeDataRecord(node, catalogs, records);
    }
    else {
      node.setRecords(records);
    }
  }

  private void setTreeDataRecord(DataSyncNodeDTO node, List<CatalogDTO> catalogs, List<Map<String, Object>> records) {
    Map<Long, List<Map<String, Object>>> group = records.stream().collect(Collectors.groupingBy(p -> MapUtils.getLong(p, "catalog_item_id")));
    List<CatalogTree<Map<String, Object>>> list = new ArrayList<>(catalogs.size());

    List<Long> rootCatalogIds = new ArrayList<>();
    for (CatalogDTO catalog : catalogs) {
      // 提取所有根节点目录
      if (StringUtils.isNotEmpty(catalog.getCatalogPath())) {
        List<Long> ids = Arrays.stream(catalog.getCatalogPath().split(",")).map(Long::valueOf).collect(Collectors.toList());
        rootCatalogIds.add(ids.get(0));
      }
      else {
        rootCatalogIds.add(catalog.getCatalogId());
      }

      // 目录数据格式调整
      List<Map<String, Object>> values = group.get(catalog.getCatalogId());
      CatalogTree<Map<String, Object>> dto = new CatalogTree<>();
      dto.setId(catalog.getCatalogId().toString());
      dto.setName(catalog.getCatalogName());
      dto.setParentId(catalog.getParCatalogId().toString());
      dto.setValues(values);
      dto.setTotal(CollectionUtils.isEmpty(values) ? 0 : values.size());
      list.add(dto);
    }

    // 构造目录树，剔除空数据节点
    List<CatalogTree<Map<String, Object>>> tree = new ArrayList<>();
    for (CatalogTree<Map<String, Object>> catalog : list) {
      if (rootCatalogIds.contains(Long.valueOf(catalog.getId()))) {
        treeCatalog(catalog, list);
        if (catalog.getTotal() > 0) {
          tree.add(catalog);
        }
      }
    }
    node.setTreeRecords(tree);
  }

  private int treeCatalog(CatalogTree<Map<String, Object>> catalog, List<CatalogTree<Map<String, Object>>> catalogs) {
    List<CatalogTree<Map<String, Object>>> children = catalogs.stream().filter(p -> Objects.equals(catalog.getId(), p.getParentId()))
      .collect(Collectors.toList());
    int total = catalog.getTotal();
    catalog.setChildren(children);
    for (CatalogTree<Map<String, Object>> child : CollectionUtils.emptyIfNull(children)) {
      total = total + treeCatalog(child, catalogs);
    }
    catalog.setTotal(total);
    if (total == 0) {
      // 清理空数据节点
      catalog.setChildren(Collections.emptyList());
    }
    return total;
  }
}
