package com.iwhalecloud.bote.service.base.impl;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.iwhalecloud.bote.common.enums.DataTypeMapEnum;
import com.iwhalecloud.bote.common.util.CodeGenerateUtil;
import com.iwhalecloud.bote.dto.base.CodeGenerateDefinition;
import com.iwhalecloud.bote.dto.base.TableColumnDefinition;
import com.iwhalecloud.bote.dto.base.TableDefinition;
import com.iwhalecloud.bote.dto.base.query.TableParams;
import com.iwhalecloud.bote.service.base.ICodeGeneratorService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.database.inspect.DatabaseInspector;
import com.iwhalecloud.bss.litchi.database.inspect.definition.Column;
import com.iwhalecloud.bss.litchi.database.inspect.definition.Table;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 代码生成服务实现类
 *
 * @author chen.linfa
 * @since 2024-09-12
 */
@Service
public class CodeGeneratorServiceImpl implements ICodeGeneratorService {

  // @formatter:off
  /** 日期类型值 */
  private static final List<Integer> DATE_TYPES = ImmutableList.of(Types.DATE, Types.TIME, Types.TIMESTAMP, Types.TIME_WITH_TIMEZONE, Types.TIMESTAMP_WITH_TIMEZONE);
  /** 基础字段 */
  private static final List<String> BASE_COLUMNS = ImmutableList.of(
    "status_cd",
    "creator_id",
    "updator_id",
    "created_time",
    "updated_time",
    "remark"
  );
  /** 创建人字段 */
  private static final String CREATOR_ID_COLUMN = "creator_id";
  /** 修改人字段 */
  private static final String UPDATOR_ID_COLUMN = "updator_id";
  /** 创建时间字段 */
  private static final String CREATED_TIME_COLUMN = "created_time";
  /** 修改时间字段 */
  private static final String UPDATED_TIME_COLUMN = "updated_time";
  private static final Pattern PATTERN = Pattern.compile("\\s*|\t|\r|\n");

  // @formatter:on

  @Override
  public ResultVO<byte[]> execute(List<TableParams> params) {
    // 表合法性校验
    List<TableDefinition> tables = new ArrayList<>();
    ResultVO<Void> result = getTableDefinition(params, tables);
    if (!result.isSuccess()) {
      return ResultVO.fail(result.getResultMsg());
    }
    // 根据入参，构造标准的代码生成定义
    String packageDir = StringUtils.isNotEmpty(params.get(0).getPackageDir())
      ? params.get(0).getPackageDir()
      : Strings.CS.replace(CodeGeneratorServiceImpl.class.getPackage().getName(), ".service.impl", "");

    // @formatter:off
    CodeGenerateDefinition definition = CodeGenerateDefinition.builder()
      .packageDir(packageDir)
      .subDir(params.get(0).getSubDir())
      .author("linmengfan")
      .tables(tables)
      .build();
    // @formatter:on
    try {
      CodeGenerateUtil.generate(definition);
      return ResultVO.success(CodeGenerateUtil.readZipToByteArray(definition));
    }
    catch (Exception e) {
      return ResultVO.fail(e);
    }
    finally {
      CodeGenerateUtil.clearWorkspace(definition);
    }
  }

  /**
   * 探测表结构，构造代码生成器表定义数据
   *
   * @param params 条件
   * @param tables 表定义
   * @return 结果
   */
  private ResultVO<Void> getTableDefinition(List<TableParams> params, List<TableDefinition> tables) {
    StringBuilder failMsg = new StringBuilder();
    for (TableParams param : params) {
      Table table = null;
      try {
        table = DatabaseInspector.inspectTable(param.getTableCode());
        Assert.notNull(table, "表定义不能为空，请检查数据库表");
        if (CollectionUtils.isEmpty(table.getColumns())) {
          failMsg.append("表").append(param.getTableCode()).append("缺少字段定义，请检查数据库表字段 ");
        }
        if (CollectionUtils.isEmpty(table.getPrimaryKey())) {
          failMsg.append("表").append(param.getTableCode()).append("缺少主键字段 ");
        }
      }
      catch (Exception e) {
        failMsg.append("表").append(param.getTableCode()).append("不存在，请先在数据库创建表 ");
      }
      if (table == null || StringUtils.isNotEmpty(failMsg.toString())) {
        return ResultVO.fail(failMsg.toString());
      }
      String tableCode = param.getTableCode().toLowerCase();
      Assert.notEmpty(table.getPrimaryKey(), "缺少主键字段定义，请检查数据库表");
      String primaryKey = table.getPrimaryKey().get(0).toLowerCase();
      List<TableColumnDefinition> columnDefinitions = new ArrayList<>();

      // 构造表定义
      // @formatter:off
      TableDefinition tableDefinition = TableDefinition.builder()
        .tableCode(tableCode)
        .entityDesc(param.getEntityDesc())
        .entityCode(param.getEntityCode())
        .sequenceCode(("seq_" + tableCode + "_" + primaryKey).toUpperCase())
        .columns(columnDefinitions)
        .build();
      // 构造表字段定义
      for (Column column : table.getColumns()) {
        TableColumnDefinition columnDefinition = TableColumnDefinition.builder()
          .columnName(column.getName().toLowerCase())
          .columnType(column.getTypeName())
          .isPrimaryKey(primaryKey.equalsIgnoreCase(column.getName()))
          .isDateType(DATE_TYPES.contains(column.getDataType()))
          .isBaseKey(BASE_COLUMNS.contains(column.getName().toLowerCase()))
          .isCreatorIdKey(CREATOR_ID_COLUMN.equalsIgnoreCase(column.getName()))
          .isUpdatorIdKey(UPDATOR_ID_COLUMN.equalsIgnoreCase(column.getName()))
          .isCreatedTimeKey(CREATED_TIME_COLUMN.equalsIgnoreCase(column.getName()))
          .isUpdatedTimeKey(UPDATED_TIME_COLUMN.equalsIgnoreCase(column.getName()))
          .paramName(StringUtils.uncapitalize(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, column.getName())))
          .paramType(DataTypeMapEnum.valueOf(column.getTypeName().toUpperCase()).getJavaDataType())
          // 移除换行符
          .comment(StringUtils.isNotEmpty(column.getRemarks()) ? PATTERN.matcher(column.getRemarks()).replaceAll("") : "")
          .build();
        columnDefinitions.add(columnDefinition);
      }
      tableDefinition.setContainDateColumn(IterableUtils.matchesAny(columnDefinitions, p -> BooleanUtils.isTrue(p.getIsDateType()) && BooleanUtils.isNotTrue(p.getIsBaseKey())));
      tables.add(tableDefinition);
      // @formatter:on
    }
    return ResultVO.success();
  }
}
