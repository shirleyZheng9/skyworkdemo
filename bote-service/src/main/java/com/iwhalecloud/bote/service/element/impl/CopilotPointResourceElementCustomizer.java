package com.iwhalecloud.bote.service.element.impl;

import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.bot.CopilotPointDTO;
import com.iwhalecloud.bote.mapper.bot.CopilotPointManageMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 配置数据实体关系记录 - 副驾指令
 *
 * @author chen.linfa
 * @since 2025-04-09
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.COPILOT_POINT)
public class CopilotPointResourceElementCustomizer extends AbstractResourceElementCustomizer {

  private final CopilotPointManageMapper copilotPointManageMapper;

  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.COPILOT_POINT.getCode();
  }

  /**
   * <p>1.目录 </p>
   * <p>2.应用 </p>
   * <p>3.智能体 </p>
   */
  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long pointId) {
    CopilotPointDTO point = copilotPointManageMapper.getPoint(tenantId, pointId);
    List<ResourceElementDTO> elements = new ArrayList<>(createCatalogElement(tenantId, pointId, point.getCatalogItemId()));
    if (point.getBotId() != null) {
      elements.add(createElement(tenantId, pointId, point.getBotId(), DataSyncCodeEnum.BOT.getCode()));
    }
    if (point.getSceneId() != null) {
      elements.add(createElement(tenantId, pointId, point.getSceneId(), DataSyncCodeEnum.SCENE.getCode()));
    }
    return elements;
  }
}
