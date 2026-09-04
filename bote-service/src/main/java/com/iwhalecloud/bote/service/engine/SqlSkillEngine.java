package com.iwhalecloud.bote.service.engine;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.cache.AttrSpecCache;
import com.iwhalecloud.bote.cache.SqlSkillCache;
import com.iwhalecloud.bote.common.jdbc.LimitRowsResultSetExtractor;
import com.iwhalecloud.bote.common.sql.parse.SqlScriptParseUtil;
import com.iwhalecloud.bote.common.util.ParamConverterUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.base.SimpleAttrDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSqlSkillDTO;
import com.iwhalecloud.bote.service.base.IDataSourceProviderService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.database.consts.DatabaseFamily;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * SQL 服务执行引擎
 *
 * @author bianjp
 * @since 2024-12-16
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class SqlSkillEngine {
  private static final Logger logger = LoggerFactory.getLogger(SqlSkillEngine.class);

  private final SqlSkillCache sqlSkillCache;
  private final AttrSpecCache attrSpecCache;
  private final IDataSourceProviderService dataSourceProvider;

  /**
   * 执行 SQL 服务
   *
   * @param tenantId 租户 ID
   * @param serviceId SQL 服务 ID
   * @param params 参数
   * @return SQL 查询结果
   */
  @Nullable
  public Object execute(Long tenantId, Long serviceId, @Nullable Map<String, Object> params) {
    Assert.notNull(serviceId, "SQL 服务 ID 不能为空");
    SimpleSqlSkillDTO skill = sqlSkillCache.get(tenantId, serviceId);
    Assert.notNull(skill, () -> "SQL 技能不存在: serviceId=" + serviceId);
    return execute(skill, params);
  }

  /**
   * 执行 SQL 服务
   *
   * @param skill SQL 服务
   * @param params 参数
   * @return SQL 查询结果
   */
  @Nullable
  public Object execute(SimpleSqlSkillDTO skill, @Nullable Map<String, Object> params) {
    Assert.notNull(skill.getDataSourceId(), () -> "SQL 技能未关联数据源: serviceId=" + skill.getServiceId());
    Assert.notNull(skill.getSql(), () -> "SQL 技能的 SQL 不能为空: serviceId=" + skill.getServiceId());

    // 转换入参结构
    Map<String, Object> convertedParams = ParamConverterUtil.convertRoot(skill.getRequest(), params);
    // 执行 SQL
    Object result = executeSql(skill, convertedParams);

    // 转换出参结构
    if (result != null && skill.getResponse() != null) {
      result = ParamConverterUtil.convert("", skill.getResponse(), result);
    }
    return result;
  }

  @Nullable
  @SuppressFBWarnings("SQL_INJECTION_SPRING_JDBC")
  @SuppressWarnings("SqlSourceToSinkFlow")
  private Object executeSql(SimpleSqlSkillDTO skill, Map<String, Object> params) {
    // 字段名称 -> 静态数据映射
    Map<String, List<SimpleAttrDTO>> fieldAttrValuesMap = getFieldAttrValuesMap(skill);

    DatabaseFamily databaseType = dataSourceProvider.getDataSourceType(skill.getTenantId(), skill.getDataSourceId()).getFamily();
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourceProvider.getDataSource(skill.getTenantId(), skill.getDataSourceId()));

    // 解析 SQL 和参数
    Pair<String, Object[]> pair = SqlScriptParseUtil.parse(skill.getSql(), params, databaseType.name().toLowerCase());
    String sql = pair.getLeft();
    Object[] arguments = pair.getRight();

    // 最终结果
    Object result;

    // 分页查询
    if (skill.isQueryPage()) {
      int pageNum = MapUtils.getIntValue(params, "pageNum", 1);
      int pageSize = MapUtils.getIntValue(params, "pageSize", 10);
      Assert.isTrue(pageNum > 0, "pageNum 必须是正整数");
      Assert.isTrue(pageSize > 0, "pageSize 必须是正整数");
      PageInfo<Map<String, Object>> pageInfo = queryPage(jdbcTemplate, databaseType, sql, arguments, pageNum, pageSize);
      // 翻译静态数据
      translateAttrValue(pageInfo.getList(), fieldAttrValuesMap);
      // 将 PageInfo 转为 Map 类型，否则后面转换出参结构会报错
      result = JsonUtil.convert(pageInfo, Map.class);
    }
    else {
      LimitRowsResultSetExtractor resultSetExtractor = new LimitRowsResultSetExtractor();
      logger.trace("Executing sql: sql={}, params={}", sql, arguments);
      List<Map<String, Object>> records = ListUtils.emptyIfNull(jdbcTemplate.query(sql, resultSetExtractor, arguments));
      logger.trace("Executed sql: count={}", records.size());
      // 翻译静态数据
      translateAttrValue(records, fieldAttrValuesMap);
      // 查询单值
      if (skill.isQuerySingleColumn()) {
        result = !records.isEmpty() ? IterableUtils.first(records.get(0).values()) : null;
      }
      // 查询单对象
      else if (skill.isQuerySingleRecord()) {
        result = !records.isEmpty() ? records.get(0) : null;
      }
      // 查询列表
      else {
        result = records;
      }
    }
    return result;
  }

  /**
   * 分页查询
   */
  @SuppressFBWarnings("SQL_INJECTION_SPRING_JDBC")
  @SuppressWarnings("SqlSourceToSinkFlow")
  public PageInfo<Map<String, Object>> queryPage(JdbcTemplate jdbcTemplate, DatabaseFamily databaseType, String sql, Object[] arguments,
                                                 int pageNum, int pageSize) {
    // 查询总数
    String queryTotalSql = "SELECT COUNT(*) FROM (" + sql + ") TMP_" + System.currentTimeMillis();
    logger.trace("Executing query total sql: sql={}, params={}", queryTotalSql, arguments);
    int total = ObjectUtils.getIfNull(jdbcTemplate.queryForObject(queryTotalSql, Integer.class, arguments), 0);
    logger.trace("Executed query total sql: total={}", total);
    //noinspection resource
    Page<Map<String, Object>> page = new Page<>(pageNum, pageSize); //NOPMD - suppressed CloseResource - 不需要手动关闭
    page.setTotal(total);

    // 总数为零或页数超出总数时直接返回
    if (total <= 0 || (pageNum - 1) * pageSize >= total) {
      return page.toPageInfo();
    }
    // 查询一页数据
    // 使用临时表，以避免 sql 中已经包含 where/limit 时构造的 SQL 有语法错误
    StringBuilder queryPageSqlSb = new StringBuilder(sql.length() + 100);
    queryPageSqlSb.append("SELECT * FROM (").append(sql).append(") TMP_").append(System.currentTimeMillis());
    if (databaseType == DatabaseFamily.ORACLE) {
      queryPageSqlSb.append(" WHERE ROWNUM > ").append((pageNum - 1) * pageSize).append(" AND ROWNUM <= ").append(pageNum * pageSize);
    }
    else {
      queryPageSqlSb.append(" LIMIT ").append(pageSize).append(" OFFSET ").append((pageNum - 1) * pageSize);
    }
    String queryPageSql = queryPageSqlSb.toString();
    logger.trace("Executing query page sql: sql={}, params={}", queryPageSql, arguments);
    List<Map<String, Object>> records = jdbcTemplate.queryForList(queryPageSql, arguments);
    logger.trace("Executed query page sql: count={}", records.size());
    if (CollectionUtils.isNotEmpty(records)) {
      page.addAll(records);
    }
    return page.toPageInfo();
  }

  /**
   * 获取字段名称 -> 静态数据 映射
   */
  private Map<String, List<SimpleAttrDTO>> getFieldAttrValuesMap(SimpleSqlSkillDTO skill) {
    ParameterSpec response = skill.getResponse();
    if (response == null) {
      return Collections.emptyMap();
    }

    List<ParameterSpec> parameters;
    if (skill.isQueryPage()) {
      Assert.isTrue(response.isObject(), "分页查询的出参结构应是对象");
      ParameterSpec list = IterableUtils.find(response.getChildren(), p -> "list".equals(p.getName()));
      Assert.notNull(list, "分页查询的出参结构缺少 list");
      Assert.isTrue(list.isList(), "分页查询的出参结构中 list 必须是数组");
      ParameterSpec element = list.getArrayElement();
      Assert.isTrue(element != null, "分页查询的出参结构中 list 缺少元素");
      Assert.isTrue(element.isObject(), "分页查询的出参结构中 list 元素必须是对象");
      parameters = ListUtils.emptyIfNull(element.getChildren());
    }
    else if (skill.isQueryList()) {
      Assert.isTrue(response.isList(), "列表查询的出参结构应是数组");
      ParameterSpec element = response.getArrayElement();
      Assert.isTrue(element != null, "列表查询的出参结构缺少元素");
      Assert.isTrue(element.isObject(), "列表查询的出参结构中元素必须是对象");
      parameters = ListUtils.emptyIfNull(element.getChildren());
    }
    else if (skill.isQuerySingleRecord()) {
      Assert.isTrue(response.isObject(), "单对象查询的出参结构必须是对象");
      parameters = ListUtils.emptyIfNull(response.getChildren());
    }
    else if (skill.isQuerySingleColumn()) {
      Assert.isTrue(response.isProperty(), "单值查询的出参结构必须是属性");
      parameters = Collections.singletonList(response);
    }
    else {
      throw new BssException("返回结果类型: " + skill.getResultType());
    }

    Map<String, List<SimpleAttrDTO>> fieldAttrValuesMap = new HashMap<>();
    for (ParameterSpec spec : parameters) {
      if (!StringUtils.isNotEmpty(spec.getAttrCode())) {
        continue;
      }
      List<SimpleAttrDTO> attrValues = attrSpecCache.get(skill.getTenantId(), spec.getAttrCode());
      if (CollectionUtils.isNotEmpty(attrValues)) {
        fieldAttrValuesMap.put(spec.getName(), attrValues);
      }
    }

    return fieldAttrValuesMap;
  }

  /**
   * 翻译静态数据
   */
  private void translateAttrValue(List<Map<String, Object>> records, Map<String, List<SimpleAttrDTO>> fieldAttrValuesMap) {
    if (CollectionUtils.isEmpty(records) || fieldAttrValuesMap.isEmpty()) {
      return;
    }

    for (Map<String, Object> record : records) {
      for (Entry<String, List<SimpleAttrDTO>> entry : fieldAttrValuesMap.entrySet()) {
        String fieldName = entry.getKey();
        List<SimpleAttrDTO> attrValues = entry.getValue();
        String originalValue = Objects.toString(record.get(fieldName), null);
        if (StringUtils.isEmpty(originalValue)) {
          continue;
        }
        SimpleAttrDTO attr = IterableUtils.find(attrValues, a -> originalValue.equals(a.getAttrValue()));
        if (attr != null) {
          record.put(fieldName, attr.getAttrValueName());
        }
      }
    }
  }

}
