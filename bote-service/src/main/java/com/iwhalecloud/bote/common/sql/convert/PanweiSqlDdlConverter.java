package com.iwhalecloud.bote.common.sql.convert;

import com.iwhalecloud.bote.common.jdbc.LowerCaseColumnMapRowMapper;
import com.iwhalecloud.bote.common.sql.script.TableModelScriptUtil;
import com.iwhalecloud.bote.dto.base.DataSourceProperties;
import com.iwhalecloud.bote.service.base.IDataSourceProviderService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 磐维 DDL 脚本转换器，基于 PostgreSQL 脚本转换器实现。由于磐维不支持修改序列名称，调整成先删后增
 *
 * @author shizhuxiong
 * @since 2025-08-28
 */
@Component
@RequiredArgsConstructor
public class PanweiSqlDdlConverter extends PostgreSqlDdlConverter {

  private static final Logger logger = LoggerFactory.getLogger(PanweiSqlDdlConverter.class);

  private final IDataSourceProviderService dataSourceProvider;

  /** SQL: 磐维序列配置 */
  private static final String SQL_SEQUENCE = "SELECT a.sequence_name ,a.last_value,a.start_value,a.increment_by,a.max_value,a.min_value,a.cache_value FROM %s a";

  /**
   * 根据序列信息，构造改序列名脚本
   *
   * @param oldSequenceCode 序列编码旧值
   * @param newSequenceCode 序列编码新值
   * @return 修改序列名脚本
   */
  @SuppressFBWarnings("SECSQLISPRJDBC")
  @Override
  protected String getRenameSequenceSql(String oldSequenceCode, String newSequenceCode, DataSourceProperties dataSourceProperties) {
    if (Strings.CI.equals(oldSequenceCode, newSequenceCode)) {
      return "";
    }

    // 查询旧序列的信息
    try {
      DataSource dataSource = dataSourceProvider.getDataSource(dataSourceProperties.getTenantId(), dataSourceProperties.getDataSourceId());

      JdbcTemplate appJdbcTemplate = new JdbcTemplate(dataSource);

      // 序列编码是从数据库中查询出来，而且存储时也是后端生成的编码，不会从前端传入的参数。所以不会引起sql注入
      Map<String, Object> dataMap = appJdbcTemplate.queryForObject(String.format(SQL_SEQUENCE, oldSequenceCode), new LowerCaseColumnMapRowMapper());

      // 根据旧序列的信息，新建新序列
      String maxValue = MapUtils.getString(dataMap, "max_value");
      String incrementBy = MapUtils.getString(dataMap, "increment_by");
      String startValue = MapUtils.getString(dataMap, "start_value");
      String startBy = MapUtils.getString(dataMap, "last_value", startValue);

      return TableModelScriptUtil.generateAddNewSeqAndDropOldSeqSql(newSequenceCode, incrementBy, maxValue, startBy, oldSequenceCode);
    }
    catch (Exception e) {
      logger.error("Failed to rename sequence for panwei database", e);
      return "";
    }
  }
}
