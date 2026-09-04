package com.iwhalecloud.bote.dto.orchestration.step.database;

import com.iwhalecloud.bote.dto.base.SqlParameterSpec;
import com.iwhalecloud.bote.dto.database.SimpleDataTableDTO;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 数据库相关的步骤抽象类
 *
 * <p>自定义 SQL 的参数使用 named parameter, 如 where user_id = :userId, 基于表的数据库操作使用 ?</p>
 *
 * @author bianjp
 * @since 2025-11-25
 */
@Getter
@Setter
public abstract class AbstractDatabaseStep extends AbstractStep {
  /** 表 ID */
  protected Long tableId;
  /** where 条件的参数列表(用于 sql 语句的 where 条件) */
  protected List<SqlParameterSpec> where;
  /** 参数列表(用于自定义 sql 的参数, 或者非自定义 sql 中非 where 子句的参数) */
  protected List<SqlParameterSpec> parameters;

  /** 表定义 */
  protected SimpleDataTableDTO table;

  public AbstractDatabaseStep(String type) {
    super(type);
  }
}
