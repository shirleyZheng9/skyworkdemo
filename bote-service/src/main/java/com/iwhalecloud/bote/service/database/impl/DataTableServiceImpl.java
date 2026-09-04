package com.iwhalecloud.bote.service.database.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.sql.consts.DatabaseConsts;
import com.iwhalecloud.bote.common.sql.convert.SqlPageConvert;
import com.iwhalecloud.bote.common.sql.parse.QuerySqlParseUtil;
import com.iwhalecloud.bote.common.sql.script.TableModelScriptUtil;
import com.iwhalecloud.bote.common.util.DatabaseUtil;
import com.iwhalecloud.bote.common.util.DcPublicUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.database.DataSaveDTO;
import com.iwhalecloud.bote.dto.database.DataTableColumnDTO;
import com.iwhalecloud.bote.dto.database.DataTableDTO;
import com.iwhalecloud.bote.dto.database.SimpleDataTableColumnDTO;
import com.iwhalecloud.bote.dto.database.SimpleDataTableDTO;
import com.iwhalecloud.bote.dto.database.query.DataTableQueryParams;
import com.iwhalecloud.bote.dto.skill.DataSourceDTO;
import com.iwhalecloud.bote.entity.base.DcPublicEntity;
import com.iwhalecloud.bote.config.properties.PlatformDatabaseProperties;
import com.iwhalecloud.bote.entity.database.TableModelItemEntity;
import com.iwhalecloud.bote.mapper.database.DataTableColumnMapper;
import com.iwhalecloud.bote.mapper.database.DataTableMapper;
import com.iwhalecloud.bote.mapper.database.DataTableQueryMapper;
import com.iwhalecloud.bote.mapper.database.TableModelItemMapper;
import com.iwhalecloud.bote.service.base.IDataSourceProviderService;
import com.iwhalecloud.bote.service.database.IDataTableService;
import com.iwhalecloud.bote.service.skill.IDataSourceManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.database.consts.DatabaseType;
import com.iwhalecloud.bss.litchi.diffc.result.DataDiffState;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 业务数据表服务实现
 *
 * @author tingyun.wang
 * @since 2025-11-18
 */
@Service
@RequiredArgsConstructor
public class DataTableServiceImpl implements IDataTableService {

  private final DataTableMapper tableMapper;
  private final DataTableColumnMapper columnMapper;
  private final DataTableQueryMapper dataTableQueryMapper;
  private final PlatformDatabaseProperties platformDatabaseProperties;
  private final IDataSourceProviderService dataSourceProvider;
  private final IDataSourceManageService dataSourceManageService;
  private final TableModelItemMapper modelItemMapper;

  /** 数据表和字段编码校验规则：只允许小写字母、数字和下划线，并以小写字母开头 */
  private static final Pattern TABLE_COLUMN_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*$");

  @Override
  @Transactional
  public ResultVO<DataTableDTO> saveDataTable(DataTableDTO tableDTO) {
    DataTableDTO oldTableDTO = null;
    if (tableDTO.getTableId() != null) {
      oldTableDTO = getDataTable(tableDTO.getTableId(), tableDTO.getTenantId());
      Assert.notNull(oldTableDTO, "数据表不存在");
    }
    // 校验并补充相关参数
    checkAndFillParams(tableDTO);
    // 根据差异计算保存数据
    DataDifference<DataTableDTO> difference = DataDifferenceStarter.computeSaveAndLog(oldTableDTO, tableDTO, true,
      tableDTO.getTenantId(), OperClassEnum.DATA_TABLE);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    // 执行并保存建模脚本
    executeAndSaveScript(difference);

    return ResultVO.success(difference.getToSaveData());
  }

  /**
   * 执行并保存建模脚本
   */
  private void executeAndSaveScript(DataDifference<DataTableDTO> difference) {
    DataTableDTO tableDTO = difference.getToSaveData();
    // 构造模型变更脚本
    String sql = TableModelScriptUtil.analyseForTable(difference);
    if (StringUtils.isBlank(sql)) {
      return;
    }
    // 执行脚本
    TableModelScriptUtil.executeScript(tableDTO.getTenantId(), tableDTO.getDataSourceId(), sql);
    // 保存表建模记录
    saveTableModelItem(tableDTO.getTableId(), tableDTO.getTenantId(), sql);
  }

  /**
   * 保存表建模记录
   */
  private void saveTableModelItem(Long tableId, Long tenantId, String changeSql) {
    TableModelItemEntity itemEntity = new TableModelItemEntity();
    itemEntity.setItemId(Sequences.TABLE_MODEL_ITEM_ID.next());
    itemEntity.setTableId(tableId);
    itemEntity.setChangeSql(changeSql);
    itemEntity.setTenantId(tenantId);
    itemEntity.setStatusCd(BaseConsts.STATUS_CD_VALID);
    itemEntity.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    itemEntity.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    modelItemMapper.insertTableModelItem(itemEntity);
  }

  @Override
  @Transactional
  public void batchImportDataTable(List<DataTableDTO> tableDTOList) {
    for (DataTableDTO tableDTO : tableDTOList) {
      // 校验并补充相关参数
      checkAndFillParams(tableDTO);
      // 根据差异计算保存数据
      DataDifferenceStarter.computeSaveAndLog(null, tableDTO, true, tableDTO.getTenantId(), OperClassEnum.DATA_TABLE);
    }
  }

  /**
   * 校验参数并补充相关参数
   */
  private void checkAndFillParams(DataTableDTO tableDTO) {
    Assert.notNull(tableDTO.getTenantId(), "租户ID不能为空");
    Assert.notEmpty(tableDTO.getTableColumns(), "表字段列表不能为空");
    Assert.isTrue(Arrays.asList(BaseConsts.DATABASE_TUNNEL_CUSTOM, BaseConsts.DATABASE_TUNNEL_PLATFORM).contains(tableDTO.getDataSourceChannel()), "未知的数据库类型");

    Long userId = SessionUtil.getLoginInfo().getUserId();
    if (tableDTO.getTableId() == null) {
      // 校验表名
      checkTableCode(tableDTO.getTenantId(), tableDTO.getTableCode());
      // 校验数据源
      if (BaseConsts.DATABASE_TUNNEL_CUSTOM.equals(tableDTO.getDataSourceChannel())) {
        Assert.notNull(tableDTO.getDataSourceId(), "自定义数据源ID不能为空");
      }
      else {
        // 获取并设置平台数据源
        DataSourceDTO platformDataSource = dataSourceManageService.getPlatformDataSource(tableDTO.getTenantId());
        tableDTO.setDataSourceId(platformDataSource.getDataSourceId());
      }
      // 补充默认目录ID
      if (tableDTO.getCatalogItemId() == null) {
        tableDTO.setCatalogItemId(-1L);
      }
      tableDTO.setCreatorId(userId);
    }
    tableDTO.setUpdatorId(userId);
    // 校验字段编码
    checkColumns(tableDTO.getTableColumns());
    // 补充表字段相关参数
    for (DataTableColumnDTO column : tableDTO.getTableColumns()) {
      if (column.getCreatorId() == null) {
        column.setCreatorId(userId);
      }
      if (StringUtils.isBlank(column.getPrimaryKey())) {
        column.setPrimaryKey(BaseConsts.FALSE);
      }
      column.setUpdatorId(userId);
      column.setTableCode(tableDTO.getTableCode());
      // 前端页面没有配置浮点型精度的地方，默认为2
      if (DatabaseConsts.DATA_TYPE_NUMBER.equalsIgnoreCase(column.getDataType())) {
        column.setDataScale(2L);
      }
    }
  }

  @Override
  public void checkTableCode(Long tenantId, String tableCode) {
    Assert.notNull(tenantId, "租户ID不能为空");
    Assert.hasText(tableCode, "数据表名不能为空");
    // 校验表名是否重复
    if (tableMapper.checkDataTableExists(tenantId, tableCode)) {
      throw BaseErrorConstant.DATA_TABLE_CODE_EXIST.toException(tableCode);
    }
    // 校验表名是否符合规范
    Matcher matcher = TABLE_COLUMN_PATTERN.matcher(tableCode);
    if (!matcher.find()) {
      throw new BssException("表名只允许小写字母、数字和下划线，并以小写字母开头");
    }
    // 校验是否为关键字
    if (DatabaseUtil.checkSqlKeyword(tableCode)) {
      throw new BssException("表名不能为关键字");
    }
  }

  /**
   * 校验字段列表
   */
  private void checkColumns(List<DataTableColumnDTO> columnList) {
    List<String> columnCodeList = new ArrayList<>();
    for (DataTableColumnDTO columnDTO : columnList) {
      String columnCode = columnDTO.getColumnCode();
      Assert.hasText(columnCode, "字段编码不能为空");
      // 校验字段编码唯一
      if (columnCodeList.contains(columnCode)) {
        throw new BssException("字段编码重复或已存在：" + columnCode);
      }
      // 校验字段编码是否符合规范
      Matcher matcher = TABLE_COLUMN_PATTERN.matcher(columnCode);
      if (!matcher.find()) {
        throw new BssException("表名只允许小写字母、数字和下划线，并以小写字母开头");
      }
      // 校验字段编码是否为关键字
      if (DatabaseUtil.checkSqlKeyword(columnCode)) {
        throw new BssException("字段编码不能为关键字");
      }
      columnCodeList.add(columnCode);
    }
  }

  @Override
  public DataTableDTO getDataTable(Long tableId, Long tenantId) {
    // 查询表信息
    DataTableDTO tableDTO = tableMapper.selectDataTable(tableId, tenantId);
    if (tableDTO == null) {
      return null;
    }
    // 查询表字段列表
    List<DataTableColumnDTO> columnList = columnMapper.selectDataTableColumnList(tableId, tenantId);
    tableDTO.setTableColumns(columnList);
    return tableDTO;
  }

  @Override
  public PageInfo<DataTableDTO> getDataTablePage(DataTableQueryParams query) {
    return tableMapper.selectDataTablePage(query, query.buildRowBounds()).toPageInfo();
  }

  @Override
  @Transactional
  public ResultVO<DataTableDTO> editDataTableInfo(DataTableDTO tableDTO) {
    Assert.notNull(tableDTO.getTenantId(), "租户ID不能为空");
    Assert.notNull(tableDTO.getTableId(), "数据表ID不能为空");

    // 查找旧表信息
    DataTableDTO oldTableDTO = tableMapper.selectDataTable(tableDTO.getTableId(), tableDTO.getTenantId());
    Assert.notNull(oldTableDTO, "数据表不存在");

    // 补充必要信息
    tableDTO.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    tableDTO.setStatusCd(BaseConsts.STATUS_CD_VALID);

    // 根据差异计算保存数据
    DataDifference<DataTableDTO> difference = DataDifferenceStarter.computeSaveAndLog(oldTableDTO, tableDTO, false,
      tableDTO.getTenantId(), OperClassEnum.DATA_TABLE);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }

    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<Integer> deleteDataTable(Long tableId, Long tenantId) {
    Assert.notNull(tenantId, "租户ID不能为空");
    Assert.notNull(tableId, "数据表ID不能为空");
    // 查询表信息
    DataTableDTO tableDTO = tableMapper.selectDataTable(tableId, tenantId);
    if (tableDTO == null) {
      throw BaseErrorConstant.DATA_TABLE_NOT_EXIST.toException(tableId);
    }
    // 删除表：将实际数据库表名更改成固定长度的随机值，业务表的remark字段记录了删除时设置的临时表编码
    String backupTableCode = "deleted_" + RandomStringUtils.secure().next(8, true, true);
    int delResult = tableMapper.deleteDataTable(tableId, tenantId, SessionUtil.getLoginInfo().getUserId(), backupTableCode);
    // 构造模型变更脚本: 重命名实际的数据库表名
    tableDTO.setTableCode(tableDTO.getTableCode() + "," + backupTableCode);
    DataDifference<DataTableDTO> difference = new DataDifference<>();
    difference.setToSaveData(tableDTO);
    difference.setState(DataDiffState.REMOVE);
    executeAndSaveScript(difference);
    return ResultVO.success(delResult);
  }

  @Override
  public String getDataTableIcon(Long tableId, Long tenantId) {
    return tableMapper.selectDataTableIcon(tableId, tenantId);
  }

  @Override
  public boolean checkPlatformDatabaseEnabled() {
    if (platformDatabaseProperties == null) {
      return false;
    }
    String url = platformDatabaseProperties.getUrl();
    String username = platformDatabaseProperties.getUsername();
    String password = platformDatabaseProperties.getPassword();
    return StringUtils.isNotBlank(url) && StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password);
  }

  @Override
  public List<DataTableColumnDTO> getPlatformColumns() {
    List<DcPublicEntity> list = DcPublicUtil.getList("8400");
    if (CollectionUtils.isNotEmpty(list)) {
      List<DataTableColumnDTO> resultList = new ArrayList<>();
      for (DcPublicEntity dcPublic : list) {
        resultList.add(JsonUtil.parseJson(dcPublic.getCodea(), DataTableColumnDTO.class));
      }
      return resultList;
    }
    return List.of();
  }

  @Override
  public PageInfo<Map<String, Object>> getTestDataPage(DataTableQueryParams query) {
    Assert.notNull(query.getTableId(), "数据表ID不能为空");
    Assert.notNull(query.getTenantId(), "租户ID不能为空");

    // 查询表和字段相关信息
    DataTableDTO tableDTO = tableMapper.selectDataTableBasicInfo(query.getTableId(), query.getTenantId());
    Assert.notNull(tableDTO, "数据表不存在");
    List<DataTableColumnDTO> columnList = columnMapper.selectDataTableColumnList(query.getTableId(), query.getTenantId());
    // 组装字段-数据类型map
    Map<String, String> dataTypeMap = columnList.stream().collect(Collectors.toMap(DataTableColumnDTO::getColumnCode, DataTableColumnDTO::getDataType));
    // 补充测试渠道数据条件
    boolean match = columnList.stream().anyMatch(column -> BaseConsts.PLATFORM_COLUMN_BOTE_DATA_TUNNEL.equalsIgnoreCase(column.getColumnCode()));
    if (match) {
      query.getQueryColumns().put(BaseConsts.PLATFORM_COLUMN_BOTE_DATA_TUNNEL, BaseConsts.DATA_TUNNEL_TEST);
    }
    // 查询数据库类型
    DatabaseType dbType = dataSourceProvider.getDataSourceType(tableDTO.getTenantId(), tableDTO.getDataSourceId());
    String databaseType = dbType.getFamily().name().toLowerCase(Locale.ENGLISH);
    // 获取数据源
    DataSource dataSource = dataSourceProvider.getDataSource(tableDTO.getTenantId(), tableDTO.getDataSourceId());
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    // 组装查询SQL
    StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ").append(tableDTO.getTableCode());
    List<Object> params = new ArrayList<>();
    String sql = sqlBuilder.append(buildWhereCondition(query.getQueryColumns(), params, dataTypeMap, databaseType)).toString();
    // 查询总数
    String queryTotalSql = "SELECT COUNT(*) FROM (" + sql + ") TMP_" + System.currentTimeMillis();
    int total = ObjectUtils.getIfNull(jdbcTemplate.queryForObject(queryTotalSql, Integer.class, params.toArray()), 0);
    // noinspection resource
    Page<Map<String, Object>> page = new Page<>(query.getPageNum(), query.getPageSize()); //NOPMD - suppressed CloseResource - 不需要关闭
    page.setTotal(total);
    // 总数为零或页数超出总数时直接返回
    if (total <= 0 || (query.getPageNum() - 1) * query.getPageSize() >= total) {
      return page.toPageInfo();
    }
    // 转换分页参数
    sql = SqlPageConvert.convert(databaseType, sql, query.getPageNum(), query.getPageSize());
    // 执行数据查询
    List<Map<String, Object>> queryResult = jdbcTemplate.queryForList(sql, params.toArray());
    page.addAll(queryResult);
    return page.toPageInfo();
  }

  @Override
  @Transactional
  public void saveTestData(DataSaveDTO dataSaveDTO) {
    Assert.notNull(dataSaveDTO.getTableId(), "数据表ID不能为空");
    Assert.notNull(dataSaveDTO.getTenantId(), "租户ID不能为空");
    // 查询表信息
    DataTableDTO tableDTO = tableMapper.selectDataTableBasicInfo(dataSaveDTO.getTableId(), dataSaveDTO.getTenantId());
    Assert.notNull(tableDTO, "数据表不存在");
    // 查询字段列表
    List<DataTableColumnDTO> columnList = columnMapper.selectDataTableColumnList(dataSaveDTO.getTableId(), dataSaveDTO.getTenantId());
    List<String> primaryList = columnList.stream().filter(o -> BaseConsts.TRUE.equalsIgnoreCase(o.getPrimaryKey()))
      .map(DataTableColumnDTO::getColumnCode).toList();
    Assert.notEmpty(primaryList, "数据表缺失主键字段");
    // 组装字段-数据类型map
    Map<String, String> dataTypeMap = columnList.stream().collect(Collectors.toMap(DataTableColumnDTO::getColumnCode, DataTableColumnDTO::getDataType));
    // 对比新旧数据并获取执行SQL
    List<Object> params = new ArrayList<>();
    List<Map.Entry<String, int[]>> execStatements = compareAndGetExecSql(dataSaveDTO.getOldDataList(), dataSaveDTO.getDataList(),
      primaryList, tableDTO.getTableCode(), params, dataTypeMap);
    if (CollectionUtils.isNotEmpty(execStatements)) {
      // 获取数据源
      DataSource dataSource = dataSourceProvider.getDataSource(tableDTO.getTenantId(), tableDTO.getDataSourceId());
      JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
      // 逐条执行，避免 MySQL 等在单条 PreparedStatement 中禁止多语句
      for (Map.Entry<String, int[]> e : execStatements) {
        int[] range = e.getValue();
        jdbcTemplate.update(e.getKey(), params.subList(range[0], range[1]).toArray());
      }
    }
  }

  /**
   * 对比新旧两数据中的新增、修改、删除数据并转换成执行SQL
   */
  private List<Map.Entry<String, int[]>> compareAndGetExecSql(List<Map<String, Object>> oldList, List<Map<String, Object>> newList,
                                                              List<String> primaryKeys, String tableName, List<Object> params,
                                                              Map<String, String> dataTypeMap) {
    Map<String, Map<String, Object>> oldMap = new HashMap<>(16);
    List<Map.Entry<String, int[]>> execStatements = new ArrayList<>();
    // 将旧列表映射到主键的 Map
    for (Map<String, Object> item : oldList) {
      oldMap.put(buildIdentifierKey(item, primaryKeys), item);
    }
    // 检查新列表中的每个项
    for (Map<String, Object> item : newList) {
      String identifierKey = buildIdentifierKey(item, primaryKeys);
      if (oldMap.containsKey(identifierKey)) {
        // 检查是否修改
        if (!isMapsEqual(oldMap.get(identifierKey), item)) {
          int paramStart = params.size();
          String sql = buildUpdateSql(tableName, identifierKey, primaryKeys, oldMap.get(identifierKey), item, params, dataTypeMap);
          addTestDataExecIfNonEmpty(execStatements, params, paramStart, sql);
        }
        // 该项在两个列表中都存在，从映射中移除
        oldMap.remove(identifierKey);
      }
      else {
        // 新增项
        int paramStart = params.size();
        String sql = buildInsertSql(tableName, item, primaryKeys, params, dataTypeMap);
        addTestDataExecIfNonEmpty(execStatements, params, paramStart, sql);
      }
    }
    // 剩下的则为删除项
    for (Map.Entry<String, Map<String, Object>> entry : oldMap.entrySet()) {
      int paramStart = params.size();
      String sql = buildDeleteSql(tableName, entry.getKey(), primaryKeys, params);
      addTestDataExecIfNonEmpty(execStatements, params, paramStart, sql);
    }
    return execStatements;
  }

  private static void addTestDataExecIfNonEmpty(List<Map.Entry<String, int[]>> execStatements, List<Object> params, int paramStart, String sql) {
    if (StringUtils.isNotEmpty(sql)) {
      execStatements.add(new AbstractMap.SimpleEntry<>(sql, new int[]{paramStart, params.size()}));
    }
    else {
      params.subList(paramStart, params.size()).clear();
    }
  }

  /**
   * 组装唯一标识
   */
  private String buildIdentifierKey(Map<String, Object> item, List<String> primaryKeys) {
    List<String> valueList = new ArrayList<>();
    for (String key : primaryKeys) {
      valueList.add(String.valueOf(item.get(key)));
    }
    return String.join(",", valueList);
  }

  /**
   * 构建插入 SQL
   */
  private String buildInsertSql(String tableName, Map<String, Object> item, List<String> primaryKeys, List<Object> params,
                                Map<String, String> dataTypeMap) {
    List<String> keyList = new ArrayList<>();
    List<String> valueList = new ArrayList<>();
    for (Map.Entry<String, Object> entry : item.entrySet()) {
      keyList.add(entry.getKey());
      Object value = entry.getValue();
      // 空主键值默认分配ID
      if (value == null && primaryKeys.contains(entry.getKey())) {
        value = IDUtils.nextId();
      }
      else if (BaseConsts.PLATFORM_COLUMN_BOTE_DATA_TUNNEL.equalsIgnoreCase(entry.getKey()) && value == null) {
        // 设置数据渠道为测试数据
        value = BaseConsts.DATA_TUNNEL_TEST;
      }
      else {
        AttrDataType dataType = AttrDataType.ofCode(dataTypeMap.get(entry.getKey()));
        value = dataType == null ? value : dataType.convert(null, value);
      }
      params.add(value);
      valueList.add(value == null ? null : String.format("'%s'", value));
    }
    String keys = String.join(", ", keyList);
    String values = StringUtils.repeat("?", ",", valueList.size());
    return String.format("INSERT INTO %s (%s) VALUES (%s);", tableName, keys, values);
  }

  /**
   * 构建更新 SQL
   */
  private String buildUpdateSql(String tableName, String identifierKey, List<String>  primaryKeys, Map<String, Object> oldItem,
                                Map<String, Object> newItem, List<Object> params, Map<String, String> dataTypeMap) {
    // set 列表
    List<String> setList = new ArrayList<>();
    for (Map.Entry<String, Object> entry : oldItem.entrySet()) {
      if (!Objects.equals(entry.getValue(), newItem.get(entry.getKey()))) {
        Object value = newItem.get(entry.getKey());
        AttrDataType dataType = AttrDataType.ofCode(dataTypeMap.get(entry.getKey()));
        value = dataType == null ? value : dataType.convert(null, value);
        params.add(value);
        setList.add(String.format("%s = ?", entry.getKey()));
      }
    }
    // where 条件
    String whereSql = buildUpdateWhereSql(identifierKey, primaryKeys, params);
    return String.format("UPDATE %s SET %s WHERE %s;", tableName, String.join(", ", setList), whereSql);
  }

  /**
   * 构建删除 sql
   */
  private String buildDeleteSql(String tableName, String identifierKey, List<String> primaryKeys, List<Object> params) {
    String whereSql = buildUpdateWhereSql(identifierKey, primaryKeys, params);
    return String.format("DELETE FROM %s WHERE %s;", tableName, whereSql);
  }

  /**
   * 构建 update、delete 的 where 条件
   */
  private String buildUpdateWhereSql(String identifierKey, List<String> primaryKeys, List<Object> params) {
    List<String> whereList = new ArrayList<>();
    String[] valueArr = identifierKey.split(",");
    for (int i = 0; i < primaryKeys.size(); i++) {
      params.add(Long.valueOf(valueArr[i]));
      whereList.add(String.format("%s = ?", primaryKeys.get(i)));
    }
    return String.join(" AND ", whereList);
  }

  /**
   * 比较两个 Map 是否相等
   */
  private boolean isMapsEqual(Map<String, Object> map1, Map<String, Object> map2) {
    if (map1.size() != map2.size()) {
      return false;
    }
    for (Map.Entry<String, Object> entry : map1.entrySet()) {
      if (!map2.containsKey(entry.getKey())) {
        return false;
      }
      if (!Objects.equals(entry.getValue(), map2.get(entry.getKey()))) {
        return false;
      }
    }
    return true;
  }

  /**
   * 构建查询条件
   */
  private String buildWhereCondition(Map<String, Object> queryColumns, List<Object> params, Map<String, String> dataTypeMap,
                                     String databaseType) {
    if (MapUtils.isEmpty(queryColumns)) {
      return "";
    }
    boolean hasCondition = false;
    StringBuilder sb = new StringBuilder(" WHERE 1=1");
    for (Map.Entry<String, Object> entry : queryColumns.entrySet()) {
      String columnCode = entry.getKey();
      Object columnValue = entry.getValue();
      if (columnValue == null) {
        continue;
      }
      String dataType = dataTypeMap.get(columnCode);
      boolean isBoteDataTunnel = BaseConsts.PLATFORM_COLUMN_BOTE_DATA_TUNNEL.equalsIgnoreCase(columnCode);
      // 字符串类型支持模糊搜索（平台数据渠道字段不走模糊搜索）
      if (DatabaseConsts.DATA_TYPES_STRING.contains(dataType) && !isBoteDataTunnel) {
        if (StringUtils.isBlank(columnValue.toString())) {
          continue;
        }
        sb.append(" AND ").append(columnCode);
        if (DatabaseUtil.isOracle(databaseType)) {
          sb.append(" LIKE CONCAT(CONCAT('%', ?), '%')");
        }
        else {
          sb.append(" LIKE CONCAT('%', ?, '%')");
        }
        // 转义特殊符号
        columnValue = QuerySqlParseUtil.escapeSqlForLike(columnValue.toString());
      }
      else {
        sb.append(" AND ").append(columnCode).append(" = ?");
      }
      // 数据值类型转换
      AttrDataType attrType = AttrDataType.ofCode(dataTypeMap.get(columnCode));
      columnValue = attrType == null ? columnValue : attrType.convert(null, columnValue);
      params.add(columnValue);
      hasCondition = true;
    }
    if (hasCondition) {
      // 删除 WHERE 后面的 " 1=1 AND"
      sb.delete(6, 14);
    }
    return sb.toString();
  }

  @Override
  public PageInfo<SimpleDataTableDTO> querySimpleDataTablePage(DataTableQueryParams query) {
    RowBounds rowBounds = query.buildRowBounds();
    // noinspection resource
    PageInfo<SimpleDataTableDTO> pageInfo = dataTableQueryMapper.selectSimpleTablePage(query, rowBounds).toPageInfo();
    if (CollectionUtils.isNotEmpty(pageInfo.getList())) {
      List<Long> tableIds = pageInfo.getList().stream().map(SimpleDataTableDTO::getTableId).toList();
      Map<Long, List<SimpleDataTableColumnDTO>> columns = CollectionUtils.emptyIfNull(
          dataTableQueryMapper.selectSimpleTableColumnList(query.getTenantId(), tableIds)).stream()
        .collect(Collectors.groupingBy(SimpleDataTableColumnDTO::getTableId));
      for (SimpleDataTableDTO table : pageInfo.getList()) {
        table.setColumns(columns.get(table.getTableId()));
      }
    }
    return pageInfo;
  }

  @Override
  public List<DataTableColumnDTO> getTableColumns(Long tenantId, Long tableId) {
    Assert.notNull(tableId, "数据表ID不能为空");
    Assert.notNull(tenantId, "租户ID不能为空");
    // 查询表的字段列表
    List<DataTableColumnDTO> columnList = columnMapper.selectColumnBasicInfoList(tableId, tenantId);
    // 过滤系统预置的数据渠道字段
    return columnList.stream().filter(o -> !BaseConsts.PLATFORM_COLUMN_BOTE_DATA_TUNNEL.equals(o.getColumnCode())).toList();
  }

  @Override
  public DataTableDTO getTableBasicInfo(Long tenantId, Long tableId) {
    return tableMapper.selectDataTableBasicInfo(tableId, tenantId);
  }

}
