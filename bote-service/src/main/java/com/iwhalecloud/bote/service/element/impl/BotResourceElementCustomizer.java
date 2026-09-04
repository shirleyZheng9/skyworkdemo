package com.iwhalecloud.bote.service.element.impl;

import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.bot.SimpleAgentStrategyDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneRelDTO;
import com.iwhalecloud.bote.dto.chat.ChatThemeDTO;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bote.mapper.bot.BotRelaManageMapper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 配置数据实体关系记录 - 机器人
 *
 * @author chen.linfa
 * @since 2025-04-09
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.BOT)
public class BotResourceElementCustomizer extends AbstractResourceElementCustomizer {

  private final BotQueryMapper botQueryMapper;

  private final BotRelaManageMapper botRelaManageMapper;

  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.BOT.getCode();
  }

  /**
   * <p>1.目录 </p>
   * <p>2.智能体 </p>
   * <p>3.规划智能体 </p>
   */
  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long botId) {
    SimpleBotDTO bot = botQueryMapper.getBot(botId, tenantId);
    List<ResourceElementDTO> elements = new ArrayList<>(createCatalogElement(tenantId, botId, bot.getCatalogItemId()));

    List<SimpleBotSceneRelDTO> scenes = botRelaManageMapper.selectSimpleBotSceneRelList(tenantId, Collections.singletonList(botId));
    for (SimpleBotSceneRelDTO scene : CollectionUtils.emptyIfNull(scenes)) {
      DataSyncCodeEnum elementType = SceneConsts.SCENE_TYPE_A2A.equals(scene.getSceneType()) ? DataSyncCodeEnum.A2A_AGENT : DataSyncCodeEnum.SCENE;
      elements.add(createElement(tenantId, botId, scene.getSceneId(), elementType.getCode()));
    }

    // 机器人关联的会话主题
    List<ChatThemeDTO> chatThemeList = botRelaManageMapper.selectSimpleBotThemeRelList(tenantId, botId);
    for (ChatThemeDTO theme : CollectionUtils.emptyIfNull(chatThemeList)) {
      elements.add(createElement(tenantId, botId, theme.getId(), DataSyncCodeEnum.CHAT_THEME.getCode()));
    }

    if (StringUtils.isNotEmpty(bot.getAgentStrategy())) {
      // 提取规划智能体
      SimpleAgentStrategyDTO strategy = JsonUtil.parseJson(bot.getAgentStrategy(), SimpleAgentStrategyDTO.class);
      if (strategy != null && strategy.getPlanAgent() != null) {
        boolean exists = IterableUtils.matchesAny(elements,
          p -> DataSyncCodeEnum.SCENE.getCode().equals(p.getElementType()) && Objects.equals(p.getElementId(), strategy.getPlanAgent().getSceneId()));
        if (!exists) {
          elements.add(createElement(tenantId, botId, strategy.getPlanAgent().getSceneId(), DataSyncCodeEnum.SCENE.getCode()));
        }
      }
    }
    return elements;
  }
}
