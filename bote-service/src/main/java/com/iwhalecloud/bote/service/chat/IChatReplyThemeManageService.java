package com.iwhalecloud.bote.service.chat;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.chat.ChatReplyThemeDTO;
import com.iwhalecloud.bote.dto.chat.query.ChatReplyThemeQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * 回复消息主题管理服务
 *
 * @author qian.sisheng
 * @since 2025-12-02
 */
public interface IChatReplyThemeManageService {

  /**
   * 查询单个回复消息主题
   *
   * @param replyThemeId 回复消息主题主键
   * @param tenantId 租户 ID
   * @return 回复消息主题
   */
  ChatReplyThemeDTO findChatReplyTheme(Long replyThemeId, Long tenantId);

  /**
   * 保存回复消息主题
   *
   * @param chatReplyTheme 回复消息主题
   * @return 结果
   */
  ResultVO<ChatReplyThemeDTO> saveChatReplyTheme(ChatReplyThemeDTO chatReplyTheme);

  /**
   * 删除回复消息主题
   *
   * @param replyThemeId 回复消息主题主键
   * @param tenantId 租户 ID
   * @return 结果
   */
  ResultVO<Void> deleteChatReplyTheme(Long replyThemeId, Long tenantId);

  /**
   * 查询回复消息主题列表
   *
   * @param queryParams 查询条件
   * @return 回复消息主题列表
   */
  List<ChatReplyThemeDTO> queryChatReplyThemeList(ChatReplyThemeQueryParams queryParams);

  /**
   * 查询回复消息主题列表（分页）
   *
   * @param queryParams 查询条件
   * @return 回复消息主题分页列表
   */
  PageInfo<ChatReplyThemeDTO> queryChatReplyThemePage(ChatReplyThemeQueryParams queryParams);

  /**
   * 导入回复消息主题
   *
   * @param file 文件
   * @param tenantId 租户 ID
   * @param replyThemeName 回复主题名称
   * @return 结果
   */
  ResultVO<Void> importReplyTheme(MultipartFile file, Long tenantId, String replyThemeName);

}
