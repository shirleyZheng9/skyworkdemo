package com.iwhalecloud.bote.service.chat;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.chat.ChatThemeDTO;
import com.iwhalecloud.bote.dto.chat.query.ChatThemeQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 聊天主题服务
 *
 * @author tingyun.wang
 * @since 2025-07-23
 */
public interface IChatThemeService {

  /**
   * 新增主题
   *
   * @param chatThemeDTO 主题DTO
   * @return 结果
   */
  ResultVO<ChatThemeDTO> addTheme(ChatThemeDTO chatThemeDTO);

  /**
   * 修改主题
   *
   * @param chatThemeDTO 主题DTO
   * @return 结果
   */
  ResultVO<ChatThemeDTO> updateTheme(ChatThemeDTO chatThemeDTO);

  /**
   * 重命名主题
   *
   * @param chatThemeDTO 主题DTO
   */
  void renameTheme(ChatThemeDTO chatThemeDTO);

  /**
   * 删除聊天主题
   *
   * @param chatThemeDTO 主题DTO
   * @return 结果
   */
  ResultVO<Void> deleteChatTheme(ChatThemeDTO chatThemeDTO);

  /**
   * 查询聊天主题列表（分页）
   *
   * @param queryParams 查询条件
   * @return 聊天主题分页列表
   */
  PageInfo<ChatThemeDTO> queryChatThemePage(ChatThemeQueryParams queryParams);

  /**
   * 主题列表查询
   *
   * @param botId 智能应用ID
   * @param tenantId 租户ID
   * @return 平台级主题数据+应用级主题数据
   */
  List<ChatThemeDTO> getThemeList(Long botId, Long tenantId);

  /**
   * 主题详情
   *
   * @param botId 智能应用ID
   * @param themeId 主题ID
   * @param tenantId 租户ID
   * @return 主题详情
   */
  @Nullable
  ChatThemeDTO getThemeDetail(Long botId, Long themeId, Long tenantId);

  /**
   * 获取正在使用的主题详情
   *
   * @param botId 智能应用ID
   * @param tenantId 租户ID
   * @return 主题详情
   */
  @Nullable
  ChatThemeDTO getUsedTheme(Long botId, Long tenantId);

  /**
   * 切换主题
   *
   * @param chatThemeDTO 主题DTO
   * @return 结果
   */
  ResultVO<Void> switchTheme(ChatThemeDTO chatThemeDTO);

  /**
   * 创建默认应用主题
   *
   * @param botId 智能应用ID
   * @param tenantId 租户ID
   */
  void createDefaultTheme(Long botId, Long tenantId);

}
