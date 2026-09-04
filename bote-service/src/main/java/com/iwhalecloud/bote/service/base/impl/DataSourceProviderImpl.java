package com.iwhalecloud.bote.service.base.impl;

import com.alibaba.druid.pool.DataSourceDisableException;
import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.jdbc.ConnectionWrapper;
import com.iwhalecloud.bote.common.jdbc.DataSourceWrapper;
import com.iwhalecloud.bote.dto.base.DataSourceProperties;
import com.iwhalecloud.bote.service.base.IDataSourceProviderService;
import com.iwhalecloud.bote.service.skill.IDataSourceManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.database.config.JdbcProperties;
import com.iwhalecloud.bss.litchi.database.consts.DatabaseType;
import com.iwhalecloud.bss.litchi.database.util.DbUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author qian.sisheng
 * @since 2024/8/8
 */
@Service
@SuppressWarnings("PMD.GuardLogStatement")
public final class DataSourceProviderImpl implements IDataSourceProviderService, DisposableBean {
  private static final Logger logger = LoggerFactory.getLogger(DataSourceProviderImpl.class);
  /** 关闭数据源延迟(ms) */
  private static final long CLOSE_DATASOURCE_DELAY = 60000;

  private final IDataSourceManageService dataSourceManageService;
  private final JdbcProperties defaultJdbcProperties;
  private final TaskScheduler taskScheduler;

  /** 数据源实例缓存. key 为 (botId, dataSourceId) */
  private final LoadingCache<@NonNull Pair<Long, Long>, @NonNull DruidDataSource> dataSourceCache = CacheBuilder.newBuilder()
    .maximumSize(500)
    .expireAfterAccess(Duration.ofHours(2))
    .removalListener(this::closeDataSourceOnReleaseCache)
    .build(CacheLoader.from(this::createDataSource));

  public DataSourceProviderImpl(IDataSourceManageService dataSourceManageService,
                                JdbcProperties defaultJdbcProperties,
                                ObjectProvider<TaskScheduler> taskSchedulerProvider) {
    this.dataSourceManageService = dataSourceManageService;
    this.defaultJdbcProperties = defaultJdbcProperties;
    this.taskScheduler = taskSchedulerProvider.getIfAvailable(() -> {
      SimpleAsyncTaskScheduler scheduler = new SimpleAsyncTaskScheduler();
      scheduler.setVirtualThreads(true);
      scheduler.setThreadNamePrefix("AppDataSourceCacheReleaser-");
      return scheduler;
    });
  }

  /**
   * 释放数据源实例缓存时关闭数据源
   */
  private void closeDataSourceOnReleaseCache(@SuppressWarnings("NullableProblems") RemovalNotification<Pair<Long, Long>, @NonNull DruidDataSource> notification) {
    Pair<Long, Long> key = notification.getKey();
    Long tenantId = key != null ? key.getLeft() : null;
    Long dataSourceId = key != null ? key.getRight() : null;
    DruidDataSource dataSource = notification.getValue(); //NOPMD - suppressed CloseResource - 下面做了检查状态和关闭操作
    if (dataSource == null) {
      return;
    }
    // 已经关闭时无需操作
    if (dataSource.isClosed()) {
      logger.debug("Releasing dataSource instance, already closed: tenantId={}, dataSourceId={}, cause={}", tenantId, dataSourceId,
        notification.getCause());
      return;
    }

    // 延迟关闭数据源，以避免正在使用该数据源的地方报错
    long time = System.currentTimeMillis();
    logger.debug("Releasing dataSource instance asynchronously: tenantId={}, dataSourceId={}, cause={}, time={}", tenantId, dataSourceId,
      notification.getCause(), time);
    taskScheduler.schedule(() -> {
      try {
        dataSource.close();
        logger.debug("Released dataSource instance successfully: tenantId={}, dataSourceId={}, cause={}, time={}", tenantId, dataSourceId,
          notification.getCause(), time);
      }
      catch (RuntimeException e) {
        logger.error("Failed to close dataSource instance: tenantId={}, dataSourceId={}, cause={}, time={}", tenantId, dataSourceId,
          notification.getCause(), time, e);
      }
    }, Instant.now().plusMillis(CLOSE_DATASOURCE_DELAY));
  }

  /**
   * 创建数据源实例
   */
  private DruidDataSource createDataSource(Pair<Long, Long> key) {
    Long tenantId = key.getLeft();
    Long dataSourceId = key.getRight();
    JdbcProperties jdbcProperties = buildJdbcProperties(tenantId, dataSourceId);
    DruidDataSource dataSource = jdbcProperties.configure(new DruidDataSource());
    dataSource.setName("DataSource-" + dataSourceId);

    try {
      dataSource.init();
    }
    catch (Exception e) {
      logger.error("Failed to create dataSource: dataSourceId={}", dataSourceId, e);
      // Druid 初始化时会创建新线程，若初始化失败必须调用 close 方法否则线程仍会一直运行
      dataSource.close();
      throw BaseErrorConstant.CONNECT_DATASOURCE_FAILED.toException(e);
    }
    logger.debug("Created dataSource instance successfully: dataSourceId={}", dataSourceId);
    return dataSource;
  }

  /**
   * 构造数据源配置
   */
  private JdbcProperties buildJdbcProperties(Long tenantId, Long dataSourceId) {
    DataSourceProperties properties = dataSourceManageService.findDataSourceProperties(tenantId, dataSourceId);
    Assert.notNull(properties, () -> "未找到数据源实例");
    Assert.hasLength(properties.getUrl(), () -> "数据源实例的连接地址不能为空");
    // 继承平台自身的数据源配置，以便于通过配置文件调整连接池参数
    JdbcProperties jdbcProperties = new JdbcProperties(defaultJdbcProperties);
    jdbcProperties.setUrl(properties.getUrl());
    jdbcProperties.setUsername(properties.getUsername());
    jdbcProperties.setPassword(properties.getPassword());
    jdbcProperties.setValidationQuery(null);
    jdbcProperties.generateValidationQuery();
    // 语句超时时间（秒），非正数表示不限制
    Integer statementTimeout = SystemParameter.APP_JDBC_STATEMENT_TIMEOUT.getIntegerValueFromDb();
    // 超长事务阈值（秒），超过时只记录错误日志，不会中止事务。非正数表示不记录
    Integer transactionThreshold = SystemParameter.APP_JDBC_TRANSACTION_THRESHOLD.getIntegerValueFromDb();
    // 未配置时使用平台数据源的默认配置
    if (statementTimeout != null) {
      jdbcProperties.setStatementTimeout(statementTimeout);
      jdbcProperties.setSocketTimeout(statementTimeout);
    }
    if (transactionThreshold != null) {
      jdbcProperties.setTransactionThreshold(transactionThreshold);
    }
    return jdbcProperties;
  }

  @Override
  @SuppressWarnings("PMD.PreserveStackTrace")
  public DataSource getDataSource(Long tenantId, Long dataSourceId) {
    Assert.notNull(dataSourceId, "dataSourceId 不能为空");
    try {
      return dataSourceCache.get(Pair.of(tenantId, dataSourceId));
    }
    catch (ExecutionException | UncheckedExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof BssException) {
        throw (BssException) cause;
      }
      logger.error("Failed to create dataSource: tenantId={}, dataSourceId={}", tenantId, dataSourceId, cause);
      throw BaseErrorConstant.GET_DATASOURCE_FAIL.toException(e);
    }
  }

  @Override
  public void destroy() {
    Map<Pair<Long, Long>, DruidDataSource> dataSourceMap = dataSourceCache.asMap();
    if (dataSourceMap.isEmpty()) {
      return;
    }
    logger.debug("Closing all dataSource instances");
    for (Entry<Pair<Long, Long>, DruidDataSource> entry : dataSourceMap.entrySet()) {
      Long tenantId = entry.getKey().getLeft();
      Long dataSourceId = entry.getKey().getRight();
      DruidDataSource dataSource = entry.getValue(); //NOPMD - suppressed CloseResource - 下面做了关闭；不能使用 try-with-resources, 会报 dataSource 变量未被使用
      logger.debug("Closing dataSource instance: tenantId={}, dataSourceId={}", tenantId, dataSourceId);
      try {
        dataSource.close();
      }
      catch (RuntimeException e) {
        logger.error("Failed to close dataSource instance: tenantId={}, dataSourceId={}", tenantId, dataSourceId, e);
      }
    }
  }

  @Override
  public String getDataSourceDialect(Long tenantId, Long dataSourceId) {
    return getDataSourceType(tenantId, dataSourceId).getFamily().name().toLowerCase(Locale.ENGLISH);
  }

  @Override
  public ConnectionWrapper getConnectionWrapper(DataSource dataSource) {
    // 获取数据库连接
    Connection connection;
    try {
      connection = dataSource.getConnection();
      boolean autoCommit = connection.getAutoCommit();
      if (autoCommit) {
        // 手动管理事务
        // 释放连接时数据源通常会重置连接配置(如 DruidConnectionHolder#reset, SingleConnectionDataSource#prepareConnection), 因此不需要手动还原
        connection.setAutoCommit(false);
      }
    }
    catch (DataSourceDisableException e) {
      throw new BssException("获取数据库连接失败: 数据源实例已关闭", e);
    }
    catch (SQLException e) {
      throw new BssException("获取数据库连接失败", e);
    }
    return new ConnectionWrapper(connection);
  }

  @Override
  public NamedParameterJdbcTemplate createNamedJdbcTemplate(ConnectionWrapper connectionWrapper) {
    return new NamedParameterJdbcTemplate(createJdbcTemplate(connectionWrapper));
  }

  @Override
  public void closeConnection(@Nullable ConnectionWrapper connectionWrapper) {
    if (connectionWrapper == null) {
      return;
    }
    try {
      connectionWrapper.closeTargetConnection();
    }
    catch (Exception e) {
      logger.error("Failed to close database connection", e);
    }
  }

  private JdbcTemplate createJdbcTemplate(ConnectionWrapper connectionWrapper) {
    // 将连接封装为一个新的数据源。如果直接使用原始数据源，无法保证每次返回的都是同一个连接，无法手动控制事务
    DataSourceWrapper dataSourceWrapper = new DataSourceWrapper(connectionWrapper);
    return new JdbcTemplate(dataSourceWrapper);
  }

  @Override
  public DatabaseType getDataSourceType(Long tenantId, Long dataSourceId) {
    // 使用数据源实例探测类型以保证正确性，应用数据源配置信息中指定的类型可能不准确（配置的连接地址与指定的类型可能不一致；一些国产数据库支持多种模式，需要使用连接实例判断应使用哪种模式）
    DataSource dataSource = getDataSource(tenantId, dataSourceId);
    return DbUtil.detectDatabaseType(dataSource);
  }


  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_BOT_DATA_SOURCE;
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public boolean isDistributedCacheEnabled() {
    return false;
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void refreshLocalCache() {
    logger.debug("Refresh all: cacheName={}", getCacheName());
    // 直接清除缓存，不必重新加载
    dataSourceCache.invalidateAll();
  }

  @Override
  public void refreshLocalCache(List<String> keys) {
    logger.debug("Refresh by keys: cacheName={}, keys={}", getCacheName(), keys);
    for (String key : keys) {
      String[] pieces = StringUtils.split(key, ':');
      if (pieces == null || pieces.length == 0) {
        continue;
      }
      // 刷新租户下的所有数据源
      if (pieces.length == 1 && StringUtils.isNumeric(pieces[0])) {
        Long tenantId = Long.parseLong(pieces[0]);
        List<Pair<Long, Long>> matchingKeys = dataSourceCache.asMap().keySet().stream().filter(k -> tenantId.equals(k.getLeft())).collect(Collectors.toList());
        if (!matchingKeys.isEmpty()) {
          dataSourceCache.invalidateAll(matchingKeys);
        }
      }
      // 刷新单个数据源
      else if (pieces.length == 2 && StringUtils.isNumeric(pieces[0]) && StringUtils.isNumeric(pieces[1])) {
        dataSourceCache.invalidate(Pair.of(Long.parseLong(pieces[0]), Long.parseLong(pieces[1])));
      }
      else {
        logger.warn("Invalid appDataSource cache key: {}", key);
      }
    }
  }

  @Override
  public void refresh() {
    refreshLocalCache();
  }

  @Override
  public void refresh(List<String> keys) {
    refreshLocalCache(keys);
  }

}
