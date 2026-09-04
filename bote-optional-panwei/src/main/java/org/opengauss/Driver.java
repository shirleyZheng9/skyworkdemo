package org.opengauss;

import com.github.pagehelper.dialect.helper.PostgreSqlDialect;
import com.github.pagehelper.page.PageAutoDialect;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 自定义数据库驱动
 * <p>1. druid.jar 数据库驱动解析逻辑，暂时不支持较新的国产数据库</p>
 * <p>2. 磐维数据库基于 openGauss 改造， openGauss 是基于 PostgreSQL 9.2 版本开发的</p>
 * <p>3. jdbc.url 采用 jdbc:opengauss 驱动方式对接</p>
 *
 * @author qian.sisheng
 * @since 2025-05-16
 */
@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class Driver extends org.panweidb.Driver {
  @Override
  public Connection connect(String url, Properties info) throws SQLException {
    // 兼容分页查询插件
    PageAutoDialect.registerDialectAlias("panweidb", PostgreSqlDialect.class);

    // 调整驱动方式
    url = url.replace("opengauss", "panweidb");
    return super.connect(url, info);
  }
}
