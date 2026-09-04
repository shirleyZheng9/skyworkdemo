package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 简单 SQL 技能信息
 *
 * @author bianjp
 * @since 2024-12-16
 */
@Getter
@Setter
@ToString
public class SimpleSqlSkillDTO {
  /** 租户 ID */
  private Long tenantId;
  /** SQL 服务 ID */
  private Long serviceId;
  /** 服务名称 */
  private String serviceName;
  /** 服务编码 */
  private String serviceCode;
  /** 数据源 ID */
  private Long dataSourceId;
  /** SQL 脚本 */
  private String sql;
  /** 返回结果类型 */
  private String resultType;
  /** 入参结构 */
  private ParameterSpec request;
  /** 出参结构 */
  private ParameterSpec response;

  /**
   * 转换 SQL 服务对象
   */
  public static SimpleSqlSkillDTO from(SkillSqlDTO skill) {
    SimpleSqlSkillDTO dto = new SimpleSqlSkillDTO();
    dto.setTenantId(skill.getTenantId());
    dto.serviceId = skill.getServiceId();
    dto.serviceName = skill.getServiceName();
    dto.serviceCode = skill.getServiceCode();
    dto.sql = skill.getScriptSql();
    dto.dataSourceId = skill.getDataSourceId();
    dto.resultType = skill.getResultType();
    if (StringUtils.isNotEmpty(skill.getReqJson())) {
      dto.request = JsonUtil.parseJsonRequired(skill.getReqJson(), ParameterSpec.class);
    }
    if (StringUtils.isNotEmpty(skill.getRespJson())) {
      dto.response = JsonUtil.parseJsonRequired(skill.getRespJson(), ParameterSpec.class);
    }
    return dto;
  }

  /**
   * 是否是分页查询
   */
  @JsonIgnore
  public boolean isQueryPage() {
    return BaseConsts.SQL_RESULT_TYPE_PAGE.equals(resultType);
  }

  /**
   * 是否是查询列表
   */
  @JsonIgnore
  public boolean isQueryList() {
    return BaseConsts.SQL_RESULT_TYPE_LIST.equals(resultType);
  }

  /**
   * 是否是查询单对象
   */
  @JsonIgnore
  public boolean isQuerySingleRecord() {
    return BaseConsts.SQL_RESULT_TYPE_MAP.equals(resultType);
  }

  /**
   * 是否是查询单值
   */
  @JsonIgnore
  public boolean isQuerySingleColumn() {
    return BaseConsts.SQL_RESULT_TYPE_VALUE.equals(resultType);
  }
}
