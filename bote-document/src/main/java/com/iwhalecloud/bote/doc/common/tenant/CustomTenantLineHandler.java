package com.iwhalecloud.bote.doc.common.tenant;

import com.iwhalecloud.bote.doc.common.mybatis.plugins.handler.TenantLineHandler;
import java.util.HashSet;
import java.util.Set;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

/**
 * 基于 MyBatis Plus 多租户的功能，实现 DB 层面的多租户的功能
 */
public class CustomTenantLineHandler implements TenantLineHandler {

  private final Set<String> ignoreTables = new HashSet<>();

  public CustomTenantLineHandler(TenantProperties properties) {
    // 不同 DB 下，大小写的习惯不同，所以需要都添加进去
    properties.getIgnoreTables().forEach(table -> {
      ignoreTables.add(table.toLowerCase());
      ignoreTables.add(table.toUpperCase());
    });
    // 在 OracleKeyGenerator 中，生成主键时，会查询这个表，查询这个表后，会自动拼接 TENANT_ID 导致报错
    ignoreTables.add("DUAL");
  }

  @Override
  public Expression getTenantId() {
    Long tenantId = TenantContextHolder.getTenantId();
    return new LongValue(tenantId);
  }

  @Override
  public boolean ignoreTable(String tableName) {
    // 情况一，忽略多租户
    // 情况二，忽略多租户的表
    return TenantContextHolder.isIgnore() || TenantContextHolder.getTenantId() == null
      || ignoreTables.contains(tableName.toLowerCase());
  }

}
