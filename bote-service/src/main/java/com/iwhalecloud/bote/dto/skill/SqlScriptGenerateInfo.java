package com.iwhalecloud.bote.dto.skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * SQL脚本生成信息
 *
 * @author qian.sisheng
 * @since 2024/8/8
 */
@Getter
@Setter
@ToString
public class SqlScriptGenerateInfo {
  /** 数据库类型 */
  private String databaseType;
  /** 服务编码 */
  private String serviceCode;
  /** SQL查询结果集类型 - 1单字段，2Map对象，3List对象集 */
  private String scriptResultType;
  /** value = 静态SQL */
  private String scriptSql;
  /** 出参对象 */
  private String responseObj;
  /** 全量查询语句（不带Where条件） */
  private String fullQuerySql;
  /** 查询参数Map列表 */
  private List<Map<String, String>> selectItemList;
  /** 表语句 */
  private String tableExpr;
  /** 涉及到的表名(小写) */
  private Set<String> lowerTableNameSet;
  /** Where条件列表 */
  private List<Map<String, String>> conditionList;
  /** 入参Map列表 */
  private List<Map<String, String>> inputItemList;
  /** 排序 */
  private String orderBy;
  /** 分组 */
  private String groupBy;
  /** 临时变量，用于标记某个查询字段是否已添加 */
  private Map<String, Map<String, String>> columnMapGroups;

  public SqlScriptGenerateInfo() {
    this.selectItemList = new ArrayList<>(10);
    this.lowerTableNameSet = new HashSet<>(8);
    this.conditionList = new ArrayList<>(4);
    this.inputItemList = new ArrayList<>(4);
    this.columnMapGroups = new HashMap<>(4);
  }
}
