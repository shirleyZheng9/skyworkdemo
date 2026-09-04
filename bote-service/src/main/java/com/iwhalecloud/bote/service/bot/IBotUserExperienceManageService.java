package com.iwhalecloud.bote.service.bot;


import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bote.dto.bot.BotUserExperienceDTO;
import com.iwhalecloud.bote.dto.bot.query.BotUserExperienceQryParams;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 *  用户会话辅助信息服务
 *
 * @author qian.sisheng
 * @since 2024/7/30
 */

public interface IBotUserExperienceManageService {

  /**
   * 保存用户会话辅助信息
   *
   * @param botUserExperience 用户会话辅助信息
   * @return 用户会话辅助信息
   */
  ResultVO<BotUserExperienceDTO> saveBotUserExperience(BotUserExperienceDTO botUserExperience);

  /**
   * 查询用户会话辅助信息列表
   *
   * @param params 参数
   * @return 用户会话辅助信息列表
   */
  List<BotUserExperienceDTO> queryBotUserExperienceList(BotUserExperienceQryParams params);

  /**
   * 删除用户会话辅助信息
   *
   * @param tenantId 租户 ID
   * @param experienceId 会话辅助信息ID
   */
  ResultVO<Void> deleteBotUserExperience(Long tenantId, Long experienceId);

  /**
   * 查找用户会话辅助信息详情
   *
   * @param tenantId 租户 ID
   * @param experienceId 会话辅助信息ID
   * @return 会话辅助信息
   */
  @Nullable
  BotUserExperienceDTO getBotUserExperience(Long tenantId, Long experienceId);

  /**
   * 查询用户会话辅助信息分页
   *
   * @param params 查询参数
   * @return 结果
   */
  PageInfo<BotUserExperienceDTO> queryBotUserExperiencePage(BotUserExperienceQryParams params);

  /**
   * 查询场景关联的指令列表
   *
   * @param params 查询参数
   * @return 结果
   */
  List<BotUserExperienceDTO> queryBotPointList(BotUserExperienceQryParams params);
}
