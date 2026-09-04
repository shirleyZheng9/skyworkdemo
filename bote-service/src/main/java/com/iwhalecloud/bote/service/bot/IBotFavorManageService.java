package com.iwhalecloud.bote.service.bot;

import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bote.dto.bot.BotFavorDTO;
import java.util.List;

/**
 * 机器人收藏服务
 *
 * @author auto
 * @since 2024-09-14
 */
public interface IBotFavorManageService {

  /**
   * 添加收藏
   *
   * @param tenantId 租户 ID
   * @param botId 机器人 ID
   * @return 结果
   */
  ResultVO<Void> addBotFavor(Long tenantId, Long botId);

  /**
   * 取消收藏
   *
   * @param tenantId 租户 ID
   * @param botId 机器人 ID
   * @return 结果
   */
  ResultVO<Void> cancelBotFavor(Long tenantId, Long botId);

  /**
   * 查询收藏列表
   *
   * @param tenantId 租户 ID
   * @param userId 用户 ID
   * @return 结果
   */
  List<BotFavorDTO> queryBotFavorList(Long tenantId, Long userId);
}
