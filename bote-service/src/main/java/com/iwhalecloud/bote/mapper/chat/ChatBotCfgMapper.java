package com.iwhalecloud.bote.mapper.chat;

import com.iwhalecloud.bote.dto.chat.ChatBotCfgDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 对话应用配置 Mapper
 *
 * @author chen.linfa
 * @since 2025-09-09
 */
public interface ChatBotCfgMapper {

  /**
   * 检查是否存在对话应用配置
   *
   * @param cfg 配置
   * @return 结果
   */
  boolean existsChatBotCfg(@Param("dto") ChatBotCfgDTO cfg);

  /**
   * 新增对话应用配置
   *
   * @param dto 配置
   * @return 结果
   */
  int insertChatBotCfg(@Param("dto") ChatBotCfgDTO dto);

  /**
   * 删除对话应用配置
   *
   * @param cfg 配置
   * @return 结果
   */
  int deteleChatBotCfg(@Param("dto") ChatBotCfgDTO cfg);

  /**
   * 根据用户 ID 租户 ID 查询应用配置
   *
   * @param spaceId 企业空间 ID
   * @param userId 用户 ID
   * @return 应用配置列表
   */
  List<ChatBotCfgDTO> selectBotCfgList(@Param("spaceId") Long spaceId, @Param("userId") Long userId);
}
