package com.iwhalecloud.bote.mapper.chat;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.chat.ChatThemeDTO;
import com.iwhalecloud.bote.dto.chat.query.ChatThemeQueryParams;
import com.iwhalecloud.bote.entity.chat.ChatThemeEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 聊天主题管理
 *
 * @author tingyun.wang
 * @since 2025-07-23
 */
public interface ChatThemeMapper {

  /**
   * 根据botId、themeId、tenantId获取聊天主题
   *
   * @param botId 智能体ID
   * @param themeId 主题主键
   * @param tenantId 租户ID
   * @return 聊天主题
   */
  ChatThemeDTO getChatTheme(@Param("botId") Long botId, @Param("themeId") Long themeId, @Param("tenantId") Long tenantId);

  /**
   * 新增聊天主题
   *
   * @param chatTheme 聊天主题
   * @return 结果
   */
  int insertChatTheme(@Param("dto") ChatThemeEntity chatTheme);

  /**
   * 修改聊天主题
   *
   * @param chatTheme 聊天主题
   * @return 结果
   */
  int updateChatTheme(@Param("dto") ChatThemeDTO chatTheme);

  /**
   * 删除聊天主题
   *
   * @param themeId 主键 ID
   * @param botId 智能应用 ID
   * @return 结果
   */
  int deleteChatTheme(@Param("botId") Long botId, @Param("themeId") Long themeId, @Param("tenantId") Long tenantId);

  /**
   * 获取聊天主题列表（分页）
   *
   * @param queryParams 查询条件
   * @param rowBounds 分页参数
   * @return 聊天主题分页列表
   */
  Page<ChatThemeDTO> selectChatThemePage(@Param("query") ChatThemeQueryParams queryParams, RowBounds rowBounds);

  /**
   * 获取聊天主题列表
   *
   * @param queryParams 查询条件
   * @return 聊天主题列表
   */
  List<ChatThemeDTO> selectChatThemeList(@Param("query") ChatThemeQueryParams queryParams);

  /**
   * 更新主题使用状态
   *
   * @param tenantId 租户ID
   * @param botId 智能应用ID
   * @param themeId 主题ID
   * @param isUsing 是否使用中
   * @return 结果
   */
  int updateThemeUsingStatus(@Param("tenantId") Long tenantId, @Param("botId") Long botId,
                            @Param("themeId") Long themeId, @Param("isUsing") String isUsing);

  /**
   * 批量更新主题使用状态为未使用
   *
   * @param tenantId 租户ID
   * @param botId 智能应用ID
   * @return 结果
   */
  int batchUpdateThemeNotUsing(@Param("tenantId") Long tenantId, @Param("botId") Long botId);

  /**
   * 获取当前租户下的bote使用的主题
   * @param botId 智能应用ID
   * @param tenantId 租户ID
   * @return 主题数据
   */
  ChatThemeDTO getBotUsedTheme(@Param("botId") Long botId, @Param("tenantId") Long tenantId);

  /**
   * 检查主题是否正在使用
   *
   * @param tenantId 租户ID
   * @param botId 智能应用ID
   * @param themeId 主题ID
   * @return 是否正在使用
   */
  boolean checkIsThemeUsed(@Param("tenantId") Long tenantId, @Param("botId") Long botId, @Param("themeId") Long themeId);

  /**
   * 根据主题ID获取平台主题JSON数据
   *
   * @param themeId 主题ID
   * @return 平台主题JSON数据
   */
  String getPlatformThemeJson(@Param("themeId") Long themeId);

  /**
   * 获取默认的平台主题
   *
   * @return 默认的平台主题
   */
  ChatThemeDTO getDefaultPlatformTheme();

  /**
   * 更新主题名称
   *
   * @param dto 主题对象
   * @return 执行结果
   */
  int updateThemeName(@Param("dto") ChatThemeDTO dto);

  /**
   * 获取会话主题基本信息
   *
   * @param botId 智能体ID
   * @param themeId 主题主键
   * @param tenantId 租户ID
   * @return 聊天主题
   */
  ChatThemeDTO getThemeBasicInfo(@Param("botId") Long botId, @Param("themeId") Long themeId, @Param("tenantId") Long tenantId);

}
