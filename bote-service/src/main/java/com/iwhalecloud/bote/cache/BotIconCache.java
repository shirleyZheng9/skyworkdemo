package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.entity.bot.BotEntity;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 智能应用图标缓存
 *
 * @author bianjp
 * @since 2025-06-16
 */
@Component
public class BotIconCache extends AbstractIconCache {
  private final BotQueryMapper botQueryMapper;

  public BotIconCache(BotQueryMapper botQueryMapper) {
    super("智能应用图标", null);
    this.botQueryMapper = botQueryMapper;
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_BOT_ICON;
  }

  @Override
  protected String loadIconContent(Long tenantId, Long id) {
    BotEntity bot = botQueryMapper.selectBotIcon(tenantId, id);
    if (bot == null) {
      throw new BssException("智能应用不存在");
    }
    if (StringUtils.isEmpty(bot.getBotIcon())) {
      throw new BssException("智能应用未配置图标");
    }
    return StringUtils.defaultString(bot.getBotIcon());
  }
}
