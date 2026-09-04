package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bss.litchi.cache.refresh.Refreshable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * BoteClaw 缓存
 *
 * @author chen.linfa
 * @since 2026-03-19
 */
@Component
@RequiredArgsConstructor
public class GeneraAgentIdCache implements Refreshable {
  private final BotQueryMapper botQueryMapper;

  /** BoteClaw botId 集合 */
  private volatile List<Long> botIds;

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_GENERAL_AGENT_ID;
  }

  /**
   * 判断是否 BoteClaw
   */
  public boolean isBoteClaw(Long botId) {
    if (BaseConsts.BOTE_AI_ID.equals(botId)) {
      return true;
    }
    else {
      if (botIds == null) {
        loadBoteClaw();
      }
      return CollectionUtils.isNotEmpty(botIds) && botIds.contains(botId);
    }
  }

  /**
   * 获取应用归属的用户 ID
   */
  public Long getBotOnwerUserId(Long tenantId, Long botId, Long userId) {
    if (!isBoteClaw(botId) || BaseConsts.BOTE_AI_ID.equals(botId)) {
      return userId;
    }
    SimpleBotDTO bot = botQueryMapper.selectSimpleBot(tenantId, botId);
    return bot == null ? userId : bot.getCreatorId();
  }

  @Override
  public boolean isDistributedCacheEnabled() {
    return false;
  }

  @Override
  public void refreshLocalCache() {
    loadBoteClaw();
  }

  @Override
  public void refreshLocalCache(List<String> keys) {
    loadBoteClaw();
  }

  @Override
  public void refresh() {
    loadBoteClaw();
  }

  @Override
  public void refresh(List<String> keys) {
    loadBoteClaw();
  }

  @Override
  public Object getLocalCache(String key) {
    return botIds;
  }

  /**
   * 更新 botId 集合
   */
  private void loadBoteClaw() {
    this.botIds = botQueryMapper.selectAllBoteClawId();
  }
}
