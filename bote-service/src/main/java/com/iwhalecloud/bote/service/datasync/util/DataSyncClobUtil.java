package com.iwhalecloud.bote.service.datasync.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.dto.datasync.ContentReplaceRule;
import com.iwhalecloud.bote.dto.datasync.DataSyncTableDefinition;
import com.iwhalecloud.bote.dto.datasync.ScriptDefinition;
import com.iwhalecloud.bss.litchi.database.util.DbUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 数据同步辅助工具类 - clob 字段
 *
 * @author chen.linfa
 * @since 2025-05-26
 */
public final class DataSyncClobUtil {
  private DataSyncClobUtil() {
  }

  /**
   * 获取 update clob sql
   *
   * @param definition 表定义
   * @param sqls update clob sql
   */
  public static void getClobUpdateSql(DataSyncTableDefinition definition, List<ScriptDefinition> sqls) {
    if (CollectionUtils.isEmpty(definition.getDataRecords())) {
      return;
    }
    List<String> clobColumns = getClobColumns(definition.getContentReplaceRule());
    if (CollectionUtils.isEmpty(clobColumns)) {
      return;
    }

    String primaryKey = definition.getPrimaryKey().toLowerCase();
    String tenantIdKey = StringUtils.isEmpty(definition.getTenantIdAlias()) ? "tenant_id" : definition.getTenantIdAlias();
    Long tenantId = definition.getResetTenantId() != null ? definition.getResetTenantId() : definition.getTenantId();
    for (Map<String, Object> record : definition.getDataRecords()) {
      Object primaryValue = record.get(primaryKey);
      String sql = " UPDATE " + definition.getTableCode()
        + " SET " + String.join("=?,", clobColumns)
        + "=? WHERE " + primaryKey + "=" + primaryValue
        + " AND " + tenantIdKey + "=" + tenantId;
      List<String> args = new ArrayList<>();
      clobColumns.forEach(f -> args.add(MapUtils.getString(record, f)));
      sqls.add(ScriptDefinition.builder().sql(sql).args(args).build());
    }
  }

  /**
   * 获取 clob 字段集合
   *
   * @param contentReplaceRule 赋值规则
   * @return clob 字段集合
   */
  public static List<String> getClobColumns(String contentReplaceRule) {
    if (StringUtils.isEmpty(contentReplaceRule) || !DbUtil.isOracle()) {
      return Collections.emptyList();
    }
    List<ContentReplaceRule> rules = JsonUtil.parseJson(contentReplaceRule, new TypeReference<List<ContentReplaceRule>>() {
    });
    return CollectionUtils.emptyIfNull(rules).stream().filter(p -> "toClob".equals(p.getRule())).map(ContentReplaceRule::getColumnCode)
      .collect(Collectors.toList());
  }
}
