package com.iwhalecloud.bote.service.chat.helper;

import com.iwhalecloud.bote.cache.AttrSpecCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.dto.base.SimpleAttrDTO;
import com.iwhalecloud.bote.dto.chat.ChatBotCfgDTO;
import com.iwhalecloud.bote.dto.chat.SimpleBotCfgDTO;
import com.iwhalecloud.bote.dto.chat.SimpleSessionGroupDTO;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.stereotype.Component;

/**
 * 运行态回话查询辅助类
 *
 * @author chen.linfa
 * @since 2024-12-18
 */
@RequiredArgsConstructor
@Component
public class RuntimeSessionHelper {

  private final AttrSpecCache attrSpecCache;

  /**
   * 初始化预置的平台应用，例如博特 AI、问数
   */
  public void setDefualtPlatBot(List<SimpleSessionGroupDTO> list, List<SimpleSessionGroupDTO> sessions, boolean isMark) {
    if (isMark) {
      return;
    }
    List<SimpleAttrDTO> attrs = attrSpecCache.get(BaseConsts.PLATFORM_TENANT_ID, ChatConsts.CHAT_DEFAULT_PLAT_BOT);
    for (SimpleAttrDTO attr : CollectionUtils.emptyIfNull(attrs)) {
      Long botId = Long.valueOf(attr.getAttrValue());
      if (!IterableUtils.matchesAny(sessions, p -> Objects.equals(p.getPlatBotId(), botId))) {
        list.add(mockPlatBotSession(botId, attr.getAttrValueName(), attr.getAttrValueDesc()));
      }
    }
  }

  /**
   * 组装应用配置
   */
  public void setBotCfg(SimpleSessionGroupDTO group, List<ChatBotCfgDTO> cfgs) {
    SimpleBotCfgDTO cfg = new SimpleBotCfgDTO(false, false, false);
    group.setBotCfg(cfg);
    Long relBotId = group.getBotId() != null ? group.getBotId() : group.getPlatBotId();
    List<ChatBotCfgDTO> groupCfgs = cfgs.stream().filter(p -> Objects.equals(relBotId, p.getBotId())).toList();
    if (CollectionUtils.isNotEmpty(groupCfgs)) {
      cfg.setTop(IterableUtils.matchesAny(groupCfgs, p -> Objects.equals(p.getActionType(), ChatConsts.CHAT_BOT_ATTR_TOP)));
      cfg.setMark(IterableUtils.matchesAny(groupCfgs, p -> Objects.equals(p.getActionType(), ChatConsts.CHAT_BOT_ATTR_MARK)));
      cfg.setClose(IterableUtils.matchesAny(groupCfgs, p -> Objects.equals(p.getActionType(), ChatConsts.CHAT_BOT_ATTR_CLOSE)));
    }
  }

  /**
   * 收集置顶的会话数据
   */
  public List<SimpleSessionGroupDTO> queryTop(List<SimpleSessionGroupDTO> sessions, List<ChatBotCfgDTO> cfgs) {
    List<ChatBotCfgDTO> tops = CollectionUtils.emptyIfNull(cfgs).stream().filter(p -> Objects.equals(p.getActionType(), ChatConsts.CHAT_BOT_ATTR_TOP))
      .sorted(Comparator.comparing(ChatBotCfgDTO::getUpdatedTime).reversed()).toList();
    List<SimpleSessionGroupDTO> list = new ArrayList<>();
    for (ChatBotCfgDTO cfg : CollectionUtils.emptyIfNull(tops)) {
      SimpleSessionGroupDTO session = IterableUtils.find(sessions,
        p -> Objects.equals(p.getTenantId(), cfg.getBotTenantId()) && Objects.equals(p.getBotId(), cfg.getBotId()));
      if (session == null) {
        session = IterableUtils.find(sessions, p -> Objects.equals(p.getPlatBotId(), cfg.getBotId()));
      }
      if (session != null) {
        list.add(session);
      }
    }
    return list;
  }

  private SimpleSessionGroupDTO mockPlatBotSession(Long platBotId, String botName, String url) {
    SimpleSessionGroupDTO group = new SimpleSessionGroupDTO();
    group.setIsBot(false);
    group.setTenantId(BaseConsts.PLATFORM_TENANT_ID);
    group.setBotName(botName);
    group.setPlatBotId(platBotId);
    group.setPlatReqUrl(url);
    group.setPlatBotType(BaseConsts.PLAT_BOT_TYPE_OTHER);
    if (Objects.equals(platBotId, BaseConsts.BOTE_AI_ID)) {
      group.setBotId(BaseConsts.BOTE_AI_ID);
      group.setIsBot(true);
    }
    return group;
  }
}
