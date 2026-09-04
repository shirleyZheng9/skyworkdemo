package com.iwhalecloud.bote.service.element.impl;

import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.skill.SkillSqlDTO;
import com.iwhalecloud.bote.mapper.skill.SkillSqlManageMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 配置数据实体关系记录 - 技能 - SQL
 *
 * @author chen.linfa
 * @since 2025-04-09
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.SKILL_SQL)
public class SkillSqlResourceElementCustomizer extends AbstractResourceElementCustomizer {

  private final SkillSqlManageMapper sqlManageMapper;

  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.SKILL_SQL.getCode();
  }

  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long serviceId) {
    SkillSqlDTO sql = sqlManageMapper.getSkillSql(tenantId, serviceId);
    List<ResourceElementDTO> elements = new ArrayList<>(createCatalogElement(tenantId, serviceId, sql.getCatalogItemId()));
    if (sql.getDataSourceId() != null) {
      elements.add(createElement(tenantId, serviceId, sql.getDataSourceId(), DataSyncCodeEnum.DATA_SOURCE.getCode()));
    }
    return elements;
  }
}
