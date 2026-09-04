package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bote.dto.skill.SkillPublishApplyDTO;
import com.iwhalecloud.bote.mapper.skill.SkillPublishApplyMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：技能发布申请
 *
 * @author wangtingyun
 * @since 2026-04-03
 */
@Component
public final class SkillPublishApplyDifferencePersistence extends BaseRootPersistence<SkillPublishApplyDTO> {

  public SkillPublishApplyDifferencePersistence(SkillPublishApplyMapper skillPublishApplyMapper) {
    setAddConsumer(skillPublishApplyMapper::insertSkillPublishApply);
    setModifyConsumer(skillPublishApplyMapper::updateSkillPublishApply);
  }

}
