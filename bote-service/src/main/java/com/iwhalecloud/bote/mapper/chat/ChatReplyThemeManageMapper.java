package com.iwhalecloud.bote.mapper.chat;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.chat.ChatReplyThemeDTO;
import com.iwhalecloud.bote.dto.chat.query.ChatReplyThemeQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 回复消息主题管理
 *
 * @author qian.sisheng
 * @since 2025-12-02
 */
public interface ChatReplyThemeManageMapper {
  /**
   * 校验回复消息主题的编码唯一性
   *
   * @param chatReplyTheme 回复消息主题
   * @return 结果
   */
  boolean existsChatReplyThemeCode(@Param("dto") ChatReplyThemeDTO chatReplyTheme);

  /**
   * 根据主键获取回复消息主题
   *
   * @param replyThemeId 回复消息主题主键
   * @return 回复消息主题
   */
  ChatReplyThemeDTO getChatReplyTheme(@Param("id") Long replyThemeId, @Param("tenantId") Long tenantId);

  /**
   * 新增回复消息主题
   *
   * @param chatReplyTheme 回复消息主题
   * @return 结果
   */
  int insertChatReplyTheme(@Param("dto") ChatReplyThemeDTO chatReplyTheme);

  /**
   * 修改回复消息主题
   *
   * @param chatReplyTheme 回复消息主题
   * @return 结果
   */
  int updateChatReplyTheme(@Param("dto") ChatReplyThemeDTO chatReplyTheme);

  /**
   * 删除属性
   *
   * @param replyThemeId 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteChatReplyTheme(@Param("replyThemeId") Long replyThemeId, @Param("updatorId") Long updatorId, @Param("tenantId") Long tenantId);

  /**
   * 获取回复消息主题列表
   *
   * @param queryParams 查询条件
   * @return 回复消息主题列表
   */
  List<ChatReplyThemeDTO> selectChatReplyThemeList(@Param("query") ChatReplyThemeQueryParams queryParams);

  /**
   * 获取回复消息主题列表（分页）
   *
   * @param queryParams 查询条件
   * @return 回复消息主题分页列表
   */
  Page<ChatReplyThemeDTO> selectChatReplyThemePage(@Param("query") ChatReplyThemeQueryParams queryParams, RowBounds rowBounds);
}
