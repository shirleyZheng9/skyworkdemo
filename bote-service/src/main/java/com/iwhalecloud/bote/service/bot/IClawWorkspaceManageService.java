package com.iwhalecloud.bote.service.bot;

import com.iwhalecloud.bote.dto.bot.BotSceneDTO;

/**
 * claw 工作空间管理服务
 *
 * @author chen.linfa
 * @since 2026-04-23
 */
public interface IClawWorkspaceManageService {

  /**
   * 补充 claw 智能体定义
   */
  void fillWorkspace(BotSceneDTO scene);

  /**
   * 查询并平铺 claw 智能体定义
   */
  void flatWorkspace(BotSceneDTO scene);
}
