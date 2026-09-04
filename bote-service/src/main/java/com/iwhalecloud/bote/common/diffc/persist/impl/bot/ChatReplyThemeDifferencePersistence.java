package com.iwhalecloud.bote.common.diffc.persist.impl.bot;

import com.iwhalecloud.bote.dto.chat.ChatReplyThemeDTO;
import com.iwhalecloud.bote.mapper.chat.ChatReplyThemeManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：回复消息主题
 *
 * @author qian.sisheng
 * @since 2025-12-02
 */
@Component
public final class ChatReplyThemeDifferencePersistence extends BaseRootPersistence<ChatReplyThemeDTO> {

  public ChatReplyThemeDifferencePersistence(ChatReplyThemeManageMapper chatReplyThemeManageMapper) {
    setAddConsumer(chatReplyThemeManageMapper::insertChatReplyTheme);
    setModifyConsumer(chatReplyThemeManageMapper::updateChatReplyTheme);
  }

}
