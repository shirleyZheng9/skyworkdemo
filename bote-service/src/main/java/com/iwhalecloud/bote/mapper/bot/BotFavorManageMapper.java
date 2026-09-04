package com.iwhalecloud.bote.mapper.bot;

import com.iwhalecloud.bote.dto.bot.BotFavorDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 应用收藏 Mapper
 *
 * @author auto
 * @since 2024-09-14
 */
public interface BotFavorManageMapper {

  /**
   * 新增应用收藏
   *
   * @param botFavor 收藏
   * @return 结果
   */
  int insertBotFavor(@Param("dto") BotFavorDTO botFavor);

  /**
   * 取消收藏
   */
  int deleteBotFavor(@Param("tenantId") Long tenantId, @Param("botId") Long botId, @Param("userId") Long userId);

  /**
   * 查询用户的收藏列表
   *
   * @param userId 用户 ID
   * @param tenantId 租户 ID
   * @return 结果
   */
  List<BotFavorDTO> selectBotFavorByUserId(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

  /**
   * 查询用户收藏的智能应用 ID 列表
   */
  List<Long> selectFavoriteBotIdsByUserId(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

  /**
   * 查找单个收藏
   */
  BotFavorDTO getBotFavor(@Param("tenantId") Long tenantId, @Param("botId") Long botId, @Param("userId") Long userId);
}
