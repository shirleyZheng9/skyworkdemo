package com.iwhalecloud.bote.service.chat;

import com.iwhalecloud.bote.dto.chat.ChatBotCfgDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 对话应用配置服务
 *
 * @author chen.linfa
 * @since 2025-09-09
 */
public interface IChatBotCfgService {

  /**
   * 根据应用 ID、动作类型，添加对话应用配置
   *
   * @param cfg 配置
   */
  ResultVO<Void> add(ChatBotCfgDTO cfg);

  /**
   * 根据应用 ID、动作类型，删除对话应用配置
   *
   * @param cfg 配置
   */
  ResultVO<Void> delete(ChatBotCfgDTO cfg);

  /**
   * 查询空间下的应用配置列表
   *
   * @param spaceId 空间 ID
   * @param userId 用户 ID
   * @return 应用配置列表
   */
  List<ChatBotCfgDTO> qeuryBotCfgList(Long spaceId, Long userId);
}
