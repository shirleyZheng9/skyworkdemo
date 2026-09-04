package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bote.entity.skill.DataSourceInstEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：数据源实例 DTO
 *
 * @author auto
 * @since 2024-09-16
 */
@Getter
@Setter
@ToString(callSuper = true)
public class DataSourceInstDTO extends DataSourceInstEntity {
  /** 数据源编码 */
  private String dataSourceCode;
  /** 数据源名称 */
  private String dataSourceName;
}
