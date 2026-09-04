package com.iwhalecloud.bote.service.chat;

import com.iwhalecloud.bote.dto.chat.ChatGroupCfgDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * 对话分组服务
 *
 * @author chen.linfa
 * @since 2025-09-08
 */
public interface IChatGroupCfgService {

  /**
   * 查询当前用户空间下的对话分组
   *
   * @param spaceId 空间 ID
   * @param userId 用户 ID
   * @return 对话分组
   */
  ChatGroupCfgDTO getGroupCfg(Long spaceId, Long userId);

  /**
   * 保存当前用户租户下的对话分组
   *
   * @param group 对话分组
   * @return 结果
   */
  ResultVO<ChatGroupCfgDTO> saveGroupCfg(ChatGroupCfgDTO group);
}
