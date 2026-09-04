package com.iwhalecloud.bote.common.sql.factory;

import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.sql.consts.DatabaseConsts;
import com.iwhalecloud.bote.common.sql.convert.AbstractDdlConverter;
import com.iwhalecloud.bote.common.sql.convert.DmDbDdlConverter;
import com.iwhalecloud.bote.common.sql.convert.GoldenDbDdlConverter;
import com.iwhalecloud.bote.common.sql.convert.MysqlDdlConverter;
import com.iwhalecloud.bote.common.sql.convert.OracleDdlConverter;
import com.iwhalecloud.bote.common.sql.convert.PanweiSqlDdlConverter;
import com.iwhalecloud.bote.common.sql.convert.PostgreSqlDdlConverter;
import com.iwhalecloud.bote.common.util.DatabaseUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * DDL 工厂类
 *
 * @author chen.linfa
 * @since 2022-01-21
 */
public final class DataDefinitionLanguageFactory {

  private DataDefinitionLanguageFactory() {
  }

  public static AbstractDdlConverter create(String database) {
    // 获取数据库类型对应的转换器
    String dialect = DatabaseUtil.getDatabaseDialect(database, database);

    if (StringUtils.isEmpty(dialect)) {
      // 没有找到对应的DDL转换器，说明不支持该数据库
      throw BaseErrorConstant.DDL_CONVERTER_NOT_FOUND.toException(database);
    }

    AbstractDdlConverter converter;
    switch (dialect) {
      case DatabaseConsts.DATABASE_TYPE_PG:
        converter = SpringUtil.getBean("postgreSqlDdlConverter", PostgreSqlDdlConverter.class);
        break;
      case DatabaseConsts.DATABASE_TYPE_ORACLE:
        if (DatabaseConsts.DATABASE_TYPE_DM.equalsIgnoreCase(database)) {
          converter = SpringUtil.getBean("dmDbDdlConverter", DmDbDdlConverter.class);
        }
        else {
          converter = SpringUtil.getBean("oracleDdlConverter", OracleDdlConverter.class);
        }
        break;
      case DatabaseConsts.DATABASE_TYPE_MYSQL:
        if (DatabaseConsts.DATABASE_TYPE_GOLDEN_DB.equalsIgnoreCase(database)) {
          converter = SpringUtil.getBean("goldenDbDdlConverter", GoldenDbDdlConverter.class);
        }
        else {
          converter = SpringUtil.getBean("mysqlDdlConverter", MysqlDdlConverter.class);
        }
        break;
      case DatabaseConsts.DATABASE_TYPE_PANWEI:
        converter = SpringUtil.getBean("panweiSqlDdlConverter", PanweiSqlDdlConverter.class);
        break;
      default:
        throw BaseErrorConstant.NO_SUPPORT_DATABASE.toException(database);
    }
    return converter;
  }
}
