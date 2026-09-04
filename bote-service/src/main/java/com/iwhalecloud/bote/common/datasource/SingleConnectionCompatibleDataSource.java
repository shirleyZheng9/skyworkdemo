package com.iwhalecloud.bote.common.datasource;

import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import java.sql.DriverManager;

/**
 *
 * 兼容磐维数据库，转换 jdbc 的 url
 *
 * @author qian.sisheng
 * @since 2024/11/14
 */
public class SingleConnectionCompatibleDataSource extends SingleConnectionDataSource {

  public SingleConnectionCompatibleDataSource(String url, String username, String password, boolean suppressClose) {

    super(ifPanweiDriverUrl(url), username, password, suppressClose);
    // 设置数据库连接超时时间为 3 秒
    DriverManager.setLoginTimeout(3);
  }

  public SingleConnectionCompatibleDataSource(String url, boolean suppressClose) {
    super(ifPanweiDriverUrl(url), suppressClose);
  }


  /**
   * 如果是磐维数据库，将 url 右 opengauss 转换为 panweidb
   * @param url jdbc 的 url
   * @return 将 opengauss 替换为 panweidb 后的 url
   */
  private static String ifPanweiDriverUrl(String url) {

    // 如果是
    if (url != null && url.contains("opengauss")) {
      url = url.replace("opengauss", "panweidb");
    }

    return url;
  }
}

