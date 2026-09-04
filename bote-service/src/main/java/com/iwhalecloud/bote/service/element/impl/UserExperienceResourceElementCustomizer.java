package com.iwhalecloud.bote.service.element.impl;

import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.base.SimpleAttrDTO;
import com.iwhalecloud.bote.dto.bot.BotUserExperienceDTO;
import com.iwhalecloud.bote.dto.skill.SimplePageTemplateDTO;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bote.service.skill.impl.helper.ParsePageTemplateHelper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 配置数据实体关系记录 - 问题指令
 *
 * @author chen.linfa
 * @since 2025-10-10
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.USER_EXPERIENCE)
public class UserExperienceResourceElementCustomizer extends AbstractResourceElementCustomizer {

  private final ParsePageTemplateHelper parseHelper;
  private final IResourceElementService resourceElementService;

  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.USER_EXPERIENCE.getCode();
  }

  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long resourceId) {
    return List.of();
  }

  @Transactional
  public void add(BotUserExperienceDTO dto, BotUserExperienceDTO old) {
    Long tenantId = dto.getTenantId();
    Long experienceId = dto.getExperienceId();
    if (old == null) {
      resourceElementService.batchAdd(tenantId, dto.getBotId(), DataSyncCodeEnum.BOT.getCode(),
        Collections.singletonList(experienceId), DataSyncCodeEnum.USER_EXPERIENCE.getCode());
    }
    // 第一步，清理旧实体关系
    mapper.deleteElementByResourceId(tenantId, experienceId, null, null);
    // 第二步，计算关联的静态数据
    if (StringUtils.isNotEmpty(dto.getPageTemplateJson())) {
      SimplePageTemplateDTO pageTemplate = JsonUtil.parseJsonRequired(dto.getPageTemplateJson(), SimplePageTemplateDTO.class);
      Map<String, List<SimpleAttrDTO>> map = parseHelper.getStaticCodeList(tenantId, pageTemplate);
      if (MapUtils.isNotEmpty(map)) {
        List<Long> attrIds = new ArrayList<>(map.size());
        for (Entry<String, List<SimpleAttrDTO>> entry : map.entrySet()) {
          attrIds.add(entry.getValue().get(0).getAttrId());
        }
        resourceElementService.batchAdd(tenantId, experienceId, DataSyncCodeEnum.USER_EXPERIENCE.getCode(), attrIds,
          DataSyncCodeEnum.SKILL_ATTR.getCode());
      }
    }
  }

  @Transactional
  public void delete(Long tenantId, Long botId, Long experienceId) {
    resourceElementService.remove(tenantId, botId, experienceId, DataSyncCodeEnum.USER_EXPERIENCE.getCode());
    clear(tenantId, experienceId);
  }
}
