package com.iwhalecloud.bote.common.sql.convert;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.DcPublicUtil;
import com.iwhalecloud.bote.entity.base.DcPublicEntity;
import com.iwhalecloud.bss.litchi.database.consts.DatabaseType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 分页查询 SQL 转换器
 *
 * @author chen.linfa
 * @since 2024-10-22
 */
public final class SqlPageConvert {

  private SqlPageConvert() {
  }

  private static final Map<String, BiFunction<Map<String, Object>, String, String>> TYPES = new HashMap<>(16);

  static {
    TYPES.put(DatabaseType.POSTGRESQL.name().toLowerCase(), (params, sql) -> {
      Integer pageNum = (Integer) params.get(BaseConsts.PAGE_NUM);
      Integer pageSize = (Integer) params.get(BaseConsts.PAGE_SIZE);
      if (pageNum <= 1) {
        sql += " offset 0 limit " + pageSize;
      }
      else {
        sql += " offset " + ((pageNum - 1) * pageSize) + " limit " + pageSize;
      }
      return sql;
    });

    TYPES.put(DatabaseType.MYSQL.name().toLowerCase(), (params, sql) -> {
      Integer pageNum = (Integer) params.get(BaseConsts.PAGE_NUM);
      Integer pageSize = (Integer) params.get(BaseConsts.PAGE_SIZE);
      if (pageNum <= 1) {
        sql += " limit 0," + pageSize;
      }
      else {
        sql += " limit " + ((pageNum - 1) * pageSize) + "," + pageSize;
      }
      return sql;
    });

    TYPES.put(DatabaseType.ORACLE.name().toLowerCase(), (params, sql) -> {
      Integer pageNum = (Integer) params.get(BaseConsts.PAGE_NUM);
      Integer pageSize = (Integer) params.get(BaseConsts.PAGE_SIZE);
      return "SELECT * FROM ("
        + "SELECT tmp_page.*, rownum as rowno FROM ("
        + sql + ") tmp_page WHERE ROWNUM <= "
        + pageNum * pageSize
        + ") tmp_page_ WHERE rowno >"
        + (pageNum - 1) * pageSize;
    });
  }

  public static String convert(String databaseType, String sql, Integer pageNum, Integer pageSize) {
    // 获取当前数据源类型对应的数据库方言类型
    List<DcPublicEntity> list = DcPublicUtil.getList(DcPublicUtil.DATABASE_ID_MATCH);
    Map<String, String> databaseMap = new HashMap<>(16);
    for (DcPublicEntity dcPublic : list) {
      String[] dataSourceTypes = dcPublic.getCodea().split(",");
      for (String dataSourceType : dataSourceTypes) {
        databaseMap.put(dataSourceType, dcPublic.getPcode());
      }
    }
    databaseType = databaseMap.get(databaseType);
    return TYPES.get(databaseType).apply(ImmutableMap.of(BaseConsts.PAGE_NUM, pageNum, BaseConsts.PAGE_SIZE, pageSize), sql);
  }
}
