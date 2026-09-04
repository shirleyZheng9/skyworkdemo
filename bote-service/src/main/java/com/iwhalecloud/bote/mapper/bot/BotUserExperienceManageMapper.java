package com.iwhalecloud.bote.mapper.bot;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.bot.BotUserExperienceDTO;
import com.iwhalecloud.bote.dto.bot.query.BotUserExperienceQryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 机器人辅助功能
 *
 * @author auto
 * @since 2024-09-14
 */
public interface BotUserExperienceManageMapper {

  /**
   * 新增会话辅助信息
   *
   * @param dto 会话辅助信息
   * @return 结果
   */
  int insertBotUserExperience(@Param("dto") BotUserExperienceDTO dto);

  /**
   * 更新会话辅助信息
   *
   * @param dto 会话辅助信息
   * @return 结果
   */
  int updateBotUserExperience(@Param("dto") BotUserExperienceDTO dto);

  /**
   * 查找单个会话辅助信息
   *
   * @param experienceId 会话辅助信息
   * @return 会话辅助信息
   */
  BotUserExperienceDTO getBotUserExperience(@Param("tenantId") Long tenantId, @Param("experienceId") Long experienceId);

  /**
   * 查找会话辅助信息列表
   *
   * @param params 参数
   * @return 会话辅助信息列表
   */
  List<BotUserExperienceDTO> selectBotUserExperience(@Param("query") BotUserExperienceQryParams params);

  /**
   * 删除会话辅助信息
   */
  int deleteBotUserExperience(@Param("tenantId") Long tenantId, @Param("updatorId") Long updatorId, @Param("experienceId") Long experienceId);

  /**
   * 查询会话辅助信息(分页)
   *
   * @param params 参数
   * @param rowBounds 分页参数
   * @return 结果
   */
  Page<BotUserExperienceDTO> selectBotUserExperiencePage(@Param("query") BotUserExperienceQryParams params, RowBounds rowBounds);

  /**
   * 查询会话辅助信息
   *
   * @param params 查询参数
   * @return 结果
   */
  BotUserExperienceDTO getBotUserExperienceByType(@Param("query") BotUserExperienceQryParams params);

  /**
   * 查询场景下关联指令
   *
   * @param sceneId 场景ID
   * @return 结果
   */
  List<String> selectBotUserExperienceBySceneId(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId, @Param("botId") Long botId);
}
