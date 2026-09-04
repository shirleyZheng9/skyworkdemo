package com.iwhalecloud.bote.mapper.bot;

import com.iwhalecloud.bote.dto.bot.BotDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 机器人配置 Mapper
 *
 * @author auto
 * @since 2024-09-14
 */
public interface BotManageMapper {

  /**
   * 校验名称唯一性
   *
   * @param bot 机器人
   * @return 结果
   */
  boolean existsBotName(@Param("dto") BotDTO bot);

  /**
   * 新增机器人
   *
   * @param bot 机器人
   * @return 结果
   */
  int insertBot(@Param("dto") BotDTO bot);

  /**
   * 修改机器人
   *
   * @param bot 机器人
   * @return 结果
   */
  int updateBot(@Param("dto") BotDTO bot);

  /**
   * 保存智能体规划策略
   *
   * @param bot 应用
   * @return 结果
   */
  int updateAgentStrategy(@Param("dto") BotDTO bot);

  /**
   * 保存智能应用的页面配置信息
   * @param bot 智能应用
   * @return 结果
   */
  int updatePageSettingInfo(@Param("dto") BotDTO bot);

  /**
   * 删除机器人
   */
  int deleteBot(@Param("tenantId") Long tenantId, @Param("botId") Long botId, @Param("updatorId") Long updatorId);

  /**
   * 根据主键查询机器人
   */
  BotDTO getBot(@Param("tenantId") Long tenantId, @Param("botId") Long botId);

  /**
   * 更新机器人状态
   */
  int updateBotStatus(@Param("tenantId") Long tenantId, @Param("botStatus") String botStatus, @Param("botId") Long botId, @Param("updatorId") Long updatorId, @Param("isDefault") String isDefault);

  /**
   * 清理租户下非当前机器人的默认标识
   *
   * @param tenantId 租户 ID
   * @param botId 机器人 ID
   * @param updatorId 修改人
   * @return 结果
   */
  int clearDefaultBot(@Param("tenantId") Long tenantId, @Param("botId") Long botId, @Param("updatorId") Long updatorId);

  /**
   * 刷新Bot应用的更新时间
   *
   * @param tenantId 租户 ID
   * @param botId 机器人 ID
   * @param updatorId 修改人
   * @return 修改结果
   */
  int modifyBotUpdateTime(@Param("tenantId") Long tenantId, @Param("botId") Long botId, @Param("updatorId") Long updatorId);

  /**
   * 批量刷新Bot应用的更新时间
   *
   * @param tenantId 租户 ID
   * @param botIds 机器人 ID 列表
   * @param updatorId 修改人
   * @return 修改结果
   */
  int modifyBotUpdateTimeBatch(@Param("botIds") List<Long> botIds, @Param("tenantId") Long tenantId, @Param("updatorId") Long updatorId);

}
