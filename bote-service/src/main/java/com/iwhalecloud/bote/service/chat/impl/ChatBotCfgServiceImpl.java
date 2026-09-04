package com.iwhalecloud.bote.service.chat.impl;

import com.iwhalecloud.bote.cache.AttrSpecCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.chat.ChatBotCfgDTO;
import com.iwhalecloud.bote.dto.chat.SessionDTO;
import com.iwhalecloud.bote.mapper.chat.ChatBotCfgMapper;
import com.iwhalecloud.bote.mapper.chat.SessionMapper;
import com.iwhalecloud.bote.service.chat.IChatBotCfgService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 对话应用配置服务
 *
 * @author chen.linfa
 * @since 2025-09-09
 */
@Service
@RequiredArgsConstructor
public class ChatBotCfgServiceImpl implements IChatBotCfgService {

  private final ChatBotCfgMapper mapper;
  private final SessionMapper sessionMapper;
  private final AttrSpecCache attrSpecCache;

  @Override
  @Transactional
  public ResultVO<Void> add(ChatBotCfgDTO cfg) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    cfg.setUserId(userId);
    if (mapper.existsChatBotCfg(cfg)) {
      return ResultVO.success();
    }
    List<Long> botIds = CollectionUtils.emptyIfNull(attrSpecCache.get(BaseConsts.PLATFORM_TENANT_ID, ChatConsts.CHAT_DEFAULT_PLAT_BOT)).stream()
      .map(p -> Long.valueOf(p.getAttrValue())).toList();
    if (ChatConsts.CHAT_BOT_ATTR_CLOSE.equals(cfg.getActionType())) {
      // 平台预置的应用，不可标记为完成
      if (botIds.contains(cfg.getBotId())) {
        return ResultVO.fail("平台预置的应用不可标记为已完成");
      }
    }
    cfg.setId(IDUtils.nextId());
    cfg.setUserId(userId);
    cfg.setStatusCd(BaseConsts.STATUS_CD_VALID);
    cfg.setCreatorId(userId);
    cfg.setUpdatorId(userId);
    mapper.insertChatBotCfg(cfg);

    // 如果是预置的平台应用，如果检查没有会话，需要自动新建一个
    if (botIds.contains(cfg.getBotId())) {
      SessionDTO session = sessionMapper.getSessionByPlatBotId(cfg.getSpaceId(), cfg.getBotId(), userId);
      if (session == null) {
        session = new SessionDTO();
        session.setSessionId(Sequences.BOT_SESSION_ID.next());
        if (Objects.equals(cfg.getBotId(), BaseConsts.BOTE_AI_ID)) {
          session.setBotId(BaseConsts.BOTE_AI_ID);
        }
        session.setBotTenantId(BaseConsts.PLATFORM_TENANT_ID);
        session.setSpaceId(cfg.getSpaceId());
        session.setPlatBotId(cfg.getBotId());
        session.setSessionTitle(ChatConsts.DEFAULT_SESSION_TITLE);
        session.setBeginTime(new Date());
        session.setStatusCd(BaseConsts.STATUS_CD_VALID);
        session.setCreatorId(userId);
        session.setUpdatorId(userId);
        sessionMapper.insertSession(session);
      }
    }
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> delete(ChatBotCfgDTO cfg) {
    cfg.setUserId(SessionUtil.getLoginInfo().getUserId());
    mapper.deteleChatBotCfg(cfg);
    return ResultVO.success();
  }

  @Override
  public List<ChatBotCfgDTO> qeuryBotCfgList(Long spaceId, Long userId) {
    return mapper.selectBotCfgList(spaceId, userId);
  }
}
