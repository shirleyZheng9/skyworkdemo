package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.skill.SkillSqlDTO;
import com.iwhalecloud.bote.mapper.skill.SkillSqlManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：技能：SQL
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Component
public final class SkillSqlDifferencePersistence extends BaseRootPersistence<SkillSqlDTO> {
  public SkillSqlDifferencePersistence(SkillSqlManageMapper sqlManageMapper) {
    // 新增情况
    setAddConsumer(sqlManageMapper::insertSkillSql);
    // 修改情况
    setModifyConsumer(sqlManageMapper::updateSkillSql);
  }
}
