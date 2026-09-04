package com.iwhalecloud.bote.mapper.chat;

import com.iwhalecloud.bote.dto.chat.ChatGroupCfgDTO;
import org.apache.ibatis.annotations.Param;

/**
 * 对话分组 Mapper
 *
 * @author chen.linfa
 * @since 2025-09-08
 */
public interface ChatGroupCfgMapper {

  /**
   * 根据userId、spaceId获取对话分组
   *
   * @param spaceId 空间 ID
   * @param userId 用户 ID
   * @return 对话分组
   */
  ChatGroupCfgDTO getChatGroupCfg(@Param("spaceId") Long spaceId, @Param("userId") Long userId);

  /**
   * 新增对话分组
   *
   * @param group 对话分组
   * @return 结果
   */
  int insertChatGroupCfg(@Param("dto") ChatGroupCfgDTO group);

  /**
   * 修改对话分组
   *
   * @param group 对话分组
   * @return 结果
   */
  int updateChatGroupCfg(@Param("dto") ChatGroupCfgDTO group);
}
