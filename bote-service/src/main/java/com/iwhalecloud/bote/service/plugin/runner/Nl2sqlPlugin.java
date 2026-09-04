package com.iwhalecloud.bote.service.plugin.runner;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.jdbc.LimitRowsResultSetExtractor;
import com.iwhalecloud.bote.common.util.EnvUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.Nl2sqlPluginParams;
import com.iwhalecloud.bote.service.base.IDataSourceProviderService;
import com.iwhalecloud.bote.service.plugin.IPluginRemoteService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.database.util.DbUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.collections4.ListUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * sql生成插件
 *
 * @author zhangJun
 * @since 2025-07-18
 */
@Component
public class Nl2sqlPlugin extends AbstractPlugin<Nl2sqlPluginParams> {

  private final IDataSourceProviderService dataSourceProvider;
  private final IPluginRemoteService pluginRemoteService;

  public Nl2sqlPlugin(IDataSourceProviderService dataSourceProvider, IPluginRemoteService pluginRemoteService) {
    super(Nl2sqlPluginParams.class);
    this.dataSourceProvider = dataSourceProvider;
    this.pluginRemoteService = pluginRemoteService;
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_IMAGE_NL2SQL;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("dataSourceId", "数据源ID", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("query", "用户问题", AttrDataType.STRING));

    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(
      Arrays.asList(ParameterSpec.newProperty("sql", "sql语句", AttrDataType.STRING),
        ParameterSpec.newProperty("records", "查询结果", AttrDataType.ARRAY)));
  }

  @Override
  public void validateParams(Nl2sqlPluginParams params) {
    Assert.notNull(params.getDataSourceId(), "数据源不能为空");
    Assert.hasText(params.getQuery(), "输入内容不能为空");
    // 插件运行无需控制，调整为通过脚本隐藏插件
//    String nl2sqlPluginEnabled = paramCache.getDcParamValByCode("NL2SQL_PLUGIN_ENABLED", BaseConsts.FALSE);
//    if (BaseConsts.FALSE.equals(nl2sqlPluginEnabled)) {
//      throw new BssException("NL2SQL插件未启用，请联系管理员开启");
//    }
  }

  @Override
  @SuppressFBWarnings("SECSQLISPRJDBC")
  @SuppressWarnings("PMD.GuardLogStatement")
  public Object doRun(Nl2sqlPluginParams pluginParams) {
    Map<String, Object> params = new HashMap<>();
    // 1.根据问题远程调用NL2SQL服务获取需要执行的SQL
    Long tenantId = getCurrentContext().getTenantId();
    Long dataSourceId = pluginParams.getDataSourceId();
    String question = pluginParams.getQuery();
    String evnCode = EnvUtil.getEnvCode();
    ResultVO<String> resultVO = pluginRemoteService.createSqlByQuestion(tenantId, dataSourceId, question, evnCode);
    if (!resultVO.isSuccess()) {
      throw new BssException("调用远程服务生成SQL失败: " + resultVO.getResultMsg());
    }

    String sql = resultVO.getResultObject();
    logger.info("nl2sql 生成的SQL: {}", sql);
    // 2.校验SQL是否合法
    DataSource dataSource = dataSourceProvider.getDataSource(tenantId, pluginParams.getDataSourceId());
    String dbType = DbUtil.detectDatabaseType(dataSource).getFamily().name().toLowerCase(Locale.ENGLISH);
    checkSql(sql, dbType);
    // 3.执行查询SQL并返回结果
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    LimitRowsResultSetExtractor resultSetExtractor = new LimitRowsResultSetExtractor();
    //logger.trace("Executing sql: sql={}", sql);
    List<Map<String, Object>> records = ListUtils.emptyIfNull(jdbcTemplate.query(sql, resultSetExtractor));
    logger.trace("Executed sql: count={}", records.size());
    params.put("sql", sql);
    params.put("records", records);
    return params;
  }

  private void checkSql(String sql, String dbType) {
    SQLStatement statement;
    try {
      statement = SQLUtils.parseSingleStatement(sql, dbType);
    } catch (RuntimeException e) {
      throw new BssException("解析生成的SQL失败: " + e.getMessage(), e);
    }
    if (!(statement instanceof SQLSelectStatement)) {
      throw new BssException("生成的sql是非查询语句，不允许执行！");
    }
  }

}
