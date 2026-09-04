package com.iwhalecloud.bote.common.datasource.helper;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.sql.consts.DatabaseConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.sql.script.SqlScriptUtil;
import com.iwhalecloud.bote.common.util.DatabaseUtil;
import com.iwhalecloud.bote.common.util.DcPublicUtil;
import com.iwhalecloud.bote.config.properties.PlatformDatabaseProperties;
import com.iwhalecloud.bote.dto.base.DataSourceProperties;
import com.iwhalecloud.bote.entity.base.DcPublicEntity;
import com.iwhalecloud.bss.litchi.base.annotation.ExtendPoint;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 创建数据源辅助工具
 *
 * @author bianjp
 * @since 2021-06-12
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.NonStaticInitializer")
public class CreateDatabaseHelper {

  private static final Logger logger = LoggerFactory.getLogger(CreateDatabaseHelper.class);

  private final PlatformDatabaseProperties platformDatabaseProperties;

  /** 数据库配置处理策略，用于调整自动分配数据库的参数 */
  private final Map<String, Consumer<Map<String, String>>> dbConfigStrategy = new HashMap<>(8);

  private static final String COLON = ":";

  {
    // postgresql
    dbConfigStrategy.put(DatabaseConsts.DATABASE_TYPE_PG, dbProperties -> {
      String dbName = dbProperties.get(DatabaseConsts.DB_NAME);
      String url = dbProperties.get(DatabaseConsts.URL);
      dbProperties.put(DatabaseConsts.URL, url + (url.contains("?") ? "&" : "?") + "currentSchema=" + dbName);
    });

    // mysql
    dbConfigStrategy.put(DatabaseConsts.DATABASE_TYPE_MYSQL, dbProperties -> {
      String dbName = dbProperties.get(DatabaseConsts.DB_NAME);
      String url = dbProperties.get(DatabaseConsts.URL);
      if (url.contains("?")) {
        url = StringUtils.substringBeforeLast(url, "/") + "/" + dbName + "?" + StringUtils.substringAfterLast(url, "?");
      }
      else {
        url = StringUtils.substringBeforeLast(url, "/") + "/" + dbName;
      }
      dbProperties.put(DatabaseConsts.URL, url);
    });

    // oracle
    dbConfigStrategy.put(DatabaseConsts.DATABASE_TYPE_ORACLE, dbProperties -> {
    });

    // oceanbase
    dbConfigStrategy.put(DatabaseConsts.DATABASE_TYPE_OB, dbProperties -> {
      String dbName = dbProperties.get(DatabaseConsts.DB_NAME);
      String url = dbProperties.get(DatabaseConsts.URL);
      String username = dbProperties.get(DatabaseConsts.USERNAME);
      String parentUsername = dbProperties.get(DatabaseConsts.PARENT_USERNAME);

      String database;
      int indexOfQ = url.indexOf("?");
      if (indexOfQ < 0) {
        database = url.substring(url.lastIndexOf("/") + 1);
      }
      else {
        String baseUrl = url.substring(0, indexOfQ);
        database = baseUrl.substring(baseUrl.lastIndexOf("/") + 1);
      }
      // 获取租户信息
      String tenantSuffix = parentUsername.substring(parentUsername.indexOf("@"));

      dbProperties.put(DatabaseConsts.URL, url.replace(database, dbName));
      dbProperties.put(DatabaseConsts.USERNAME, username + tenantSuffix);
    });

    // dm
    dbConfigStrategy.put(DatabaseConsts.DATABASE_TYPE_DM, dbProperties -> {
    });

    // goldendb
    dbConfigStrategy.put(DatabaseConsts.DATABASE_TYPE_GOLDEN_DB, dbProperties -> {
      String dbName = dbProperties.get(DatabaseConsts.DB_NAME);
      String url = dbProperties.get(DatabaseConsts.URL);
      if (url.contains("?")) {
        url = StringUtils.substringBeforeLast(url, "/") + "/" + dbName + "?" + StringUtils.substringAfterLast(url, "?");
      }
      else {
        url = StringUtils.substringBeforeLast(url, "/") + "/" + dbName;
      }
      dbProperties.put(DatabaseConsts.URL, url);
    });

  }

  /**
   * 创建租户所属的平台数据库
   *
   * @param tenantId 租户 ID
   * @param envCode 环境编码
   * @param databaseName database/schema 名称
   * @return 数据源信息
   */
  public DataSourceProperties createPlatformDatabase(Long tenantId, String envCode, @Nullable String databaseName) {

    String dataSourceType = platformDatabaseProperties.getType();
    Assert.hasText(dataSourceType, "创建数据库的平台数据库类型不能为空");
    String databaseType = dataSourceType.toLowerCase();

    // 1. 根据数据库类型获取对应的创建数据库配置脚本
    List<DcPublicEntity> dcPublicList = DcPublicUtil.getList(DcPublicUtil.DATABASE_CREATE_SQL);
    // 1.1. 没有找到对应配置，说明不支持该数据库
    if (CollectionUtils.isEmpty(dcPublicList)) {
      throw BaseErrorConstant.CREATE_DATABASE_ERROR.toException(dataSourceType, "缺少数据库预置脚本");
    }
    DcPublicEntity dcPublic = dcPublicList.stream().filter(p -> databaseType.equals(p.getPcode())).findFirst().orElse(null);
    if (dcPublic == null) {
      throw BaseErrorConstant.CREATE_DATABASE_ERROR.toException(dataSourceType, "缺少创建该数据库的语句");
    }

    // 2. 获取数据库自动分配配置
    DataSourceProperties properties = platformDatabaseProperties.toDataSourceProperties();
    String tablespace = StringUtils.defaultIfBlank(properties.getTablespace(), DatabaseConsts.DEFAULT_TABLESPACE);
    // database/schema 名称
    // 判断是否传入了 自定义的 database/schema 名称
    // 未传入时按照 buildDatabaseName() 自动生成名称，传入时直接取指定的名称
    String dbName = StringUtils.isEmpty(databaseName) ? buildDatabaseName(tenantId, envCode) : databaseName;
    String url = properties.getUrl();

    Map<String, String> beforeDbProperties = new HashMap<>(8);
    beforeDbProperties.put(DatabaseConsts.USERNAME, dbName);
    beforeDbProperties.put(DatabaseConsts.PASSWORD, RandomStringUtils.secure().next(10, true, true));

    List<String> rawStatements;
    // 判断是否开启应用数据库安全模式（安全模式会创建新用户，隔离访问。默认开启）
    if (BaseConsts.FALSE.equals(SystemParameter.DATABASE_SAFE_MODE_ENABLED.getValueFromDb())) {
      // 非安全模式使用 codea 字段的创建定义
      rawStatements = Arrays.asList(dcPublic.getCodea().split(";"));
      adjustBeforeDbProperties(databaseType, properties, beforeDbProperties);
    }
    else {
      // 安全模式使用 codeb 字段的创建定义
      rawStatements = Arrays.asList(dcPublic.getCodeb().split(";"));
    }

    List<String> statements = new ArrayList<>(4);
    rawStatements.forEach(sql -> {
      sql = sql.replace(COLON + DatabaseConsts.DB_NAME, dbName);
      sql = sql.replace(COLON + DatabaseConsts.USERNAME, beforeDbProperties.get(DatabaseConsts.USERNAME));
      sql = sql.replace(COLON + DatabaseConsts.PASSWORD, beforeDbProperties.get(DatabaseConsts.PASSWORD));
      sql = sql.replace(COLON + DatabaseConsts.TABLESPACE, tablespace);
      statements.addAll(SqlScriptUtil.readScript(sql));
    });

    // 3. 创建数据库
    SqlScriptUtil.ScriptExecuteResult result = SqlScriptUtil.executeScript(url, properties.getUsername(), properties.getPassword(), statements);
    // 创建失败
    if (!result.isSuccess()) {
      logger.error("Failed to create {} database: statements={}, result={}", dataSourceType, statements, result);
      throw BaseErrorConstant.CREATE_DATABASE_ERROR.toException(dataSourceType, result.getFirstError());
    }

    // 4. 调整自动分配数据库的参数
    Map<String, String> dbProperties = new HashMap<>(8);
    dbProperties.put(DatabaseConsts.TABLESPACE, tablespace);
    dbProperties.put(DatabaseConsts.DB_NAME, dbName);
    dbProperties.put(DatabaseConsts.URL, url);
    dbProperties.put(DatabaseConsts.USERNAME, beforeDbProperties.get(DatabaseConsts.USERNAME));
    dbProperties.put(DatabaseConsts.PASSWORD, beforeDbProperties.get(DatabaseConsts.PASSWORD));
    dbProperties.put(DatabaseConsts.PARENT_USERNAME, properties.getUsername());
    dbProperties.put(DatabaseConsts.PARENT_PASSWORD, properties.getPassword());
    adjustDbConfig(dbProperties, databaseType);

    return new DataSourceProperties(dbProperties.get(DatabaseConsts.URL), dbProperties.get(DatabaseConsts.USERNAME), dbProperties.get(DatabaseConsts.PASSWORD));
  }

  /**
   * 如果没有开启安全模式，以下类型的业务库使用同一份用户名密码
   *
   * @param databaseType 数据源类型
   * @param properties 创建业务库的账号配置
   * @param beforeDbProperties 新业务库的账号密码配置
   */
  private void adjustBeforeDbProperties(String databaseType, DataSourceProperties properties, Map<String, String> beforeDbProperties) {
    if (DatabaseConsts.DATABASE_TYPE_PG.equals(databaseType) || DatabaseConsts.DATABASE_TYPE_MYSQL.equals(databaseType)
      || DatabaseConsts.DATABASE_TYPE_GOLDEN_DB.equals(databaseType)) {
      beforeDbProperties.put(DatabaseConsts.USERNAME, properties.getUsername());
      beforeDbProperties.put(DatabaseConsts.PASSWORD, properties.getPassword());
    }
  }

  /**
   * 调整数据库参数
   * <p>提取出方法，支持通过埋点增加调整策略</p>
   *
   * @param dbProperties 自动分配后得到的数据库原始配置
   * @param dataSourceType 数据源类型
   */
  @ExtendPoint(name = "调整自动分配数据源的配置", module = "base")
  private void adjustDbConfig(Map<String, String> dbProperties, String dataSourceType) {
    dbConfigStrategy.get(dataSourceType).accept(dbProperties);
  }

  /**
   * 构造数据库名称
   *
   * @param tenantId 租户 ID
   * @param envCode 环境编码
   * @return 数据库名称
   */
  private String buildDatabaseName(Long tenantId, String envCode) {
    return "bote_" + tenantId + "_" + envCode;
  }

  /**
   * 检测数据库/schema名称是否存在
   *
   * @param dataSourceType 数据库类型
   * @param databaseName database/schema 名称
   * @return 是否存在
   */
  public boolean isExistDatabaseName(String dataSourceType, String databaseName) {
    String databaseType = dataSourceType.toLowerCase();
    // 1. 根据数据库类型获取对应的检测配置脚本
    List<DcPublicEntity> dcPublicList = DcPublicUtil.getList(DcPublicUtil.DATABASE_CREATE_SQL);
    if (CollectionUtils.isEmpty(dcPublicList)) {
      throw BaseErrorConstant.CHECK_DATABASE_NAME_ERROR.toException(dataSourceType, "缺少数据库预置脚本");
    }
    DcPublicEntity dcPublic = dcPublicList.stream().filter(p -> databaseType.equals(p.getPcode())).findFirst().orElse(null);
    if (dcPublic == null || StringUtils.isEmpty(dcPublic.getCodec())) {
      throw BaseErrorConstant.CHECK_DATABASE_NAME_ERROR.toException(dataSourceType, "缺少检测该数据库的语句");
    }
    // 2. 获取数据库自动分配配置
    DataSourceProperties properties = platformDatabaseProperties.toDataSourceProperties();
    // 3. 执行检测
    return DatabaseUtil.databaseNameExist(properties.getUrl(), properties.getUsername(), properties.getPassword(), dcPublic.getCodec(), databaseName);
  }

}
