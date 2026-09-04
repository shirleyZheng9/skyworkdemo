package com.iwhalecloud.bote.dto.datasync;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 数据同步表定义
 *
 * @author chen.linfa
 * @since 2024-10-22
 */
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@ToString
public class DataSyncTableDefinition {
  /** 表名 */
  private String tableCode;
  /** 主键字段 */
  private String primaryKey;
  /** 外键字段 */
  private String foreignKey;
  /** 租户 ID 字段别名 */
  private String tenantIdAlias;
  /** 全量查询条件 */
  private String syncAllQueryCondition;
  /** 表排序 */
  private Integer sortby;
  /** 模块编码 */
  private String dataConfigCode;
  /** 模块分类 */
  private String dataConfigPath;
  /** 表字段内容替换规则 */
  private String contentReplaceRule;

  /**
   * 日期字段
   * <p>网络传输过程中，日期格式数据会发生变化，需要传输前转化为字段串格式
   */
  private List<String> dateColumns;

  /** 数据库记录 */
  private List<Map<String, Object>> dataRecords;
  /** 删除状态的数据主键值 */
  private String invalidValues;

  /** 表字段 */
  private List<String> tableColumns;

  /** 是否节点模块主表 */
  private Boolean mainTable;
  /** 租户 ID */
  private Long tenantId;
  /** 重置租户 ID */
  private Long resetTenantId;
  /** 是否重置主键 ID */
  private Boolean resetPrimaryKey;
  /** 自定义方式，模块主表的主键值串 */
  private String values;
  /** 空间ID 文档中心表需要替换 */
  private Long spaceId;
}
