package com.iwhalecloud.bote.common.diffc.persist.impl.bot;

import com.iwhalecloud.bote.dto.chat.ChatThemeDTO;
import com.iwhalecloud.bote.mapper.chat.ChatThemeMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：对话主题
 *
 * @author tingyun.wang
 * @since 2025-07-23
 */
@Component
public final class ChatThemeDifferencePersistence extends BaseRootPersistence<ChatThemeDTO> {
  public ChatThemeDifferencePersistence(ChatThemeMapper themeMapper) {
    // 新增情况
    this.setAddConsumer(themeMapper::insertChatTheme);
    // 修改情况
    this.setModifyConsumer(themeMapper::updateChatTheme);
  }
}
