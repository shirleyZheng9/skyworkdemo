package com.iwhalecloud.bote.service.bot;

import com.iwhalecloud.bote.dto.bot.BotAuthDTO;
import com.iwhalecloud.bote.dto.bot.BotDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.bot.BotSettingInfoDTO;
import com.iwhalecloud.bote.dto.bot.SimpleAgentStrategyDTO;
import com.iwhalecloud.bote.dto.bot.query.BotApplyParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

import java.util.List;
import java.util.Map;

/**
 * 应用管理服务
 *
 * @author chen.linfa
 * @since 2024-08-01
 */
public interface IBotManageService {

  /**
   * 保存应用
   *
   * @param bot 应用
   * @return 结果
   */
  ResultVO<BotDTO> saveBot(BotDTO bot);

  /**
   * 保存应用欢迎页设置
   *
   * @param bot 应用
   * @return 结果
   */
  ResultVO<Void> savePageInfo(BotDTO bot);

  /**
   * 保存Bot应用设置信息
   *
   * @param settingInfoDTO Bot应用设置信息
   * @return 结果
   */
  ResultVO<Void> saveBotSettingInfo(BotSettingInfoDTO settingInfoDTO);

  /**
   * 删除应用
   *
   * @param tenantId 租户 ID
   * @param botId 应用 ID
   * @return 结果
   */
  ResultVO<Void> deleteBot(Long tenantId, Long botId);

  /**
   * 查询应用详情
   *
   * @param tenantId 租户 ID
   * @param botId 应用 ID
   * @return 应用
   */
  BotDTO findBot(Long tenantId, Long botId);

  /**
   * 应用上下架
   *
   * @param tenantId 租户 ID
   * @param botId 应用 ID
   * @param botsStatus 应用状态
   * @return 结果
   */
  ResultVO<Void> publishBot(Long tenantId, Long botId, String botsStatus);

  /**
   * 应用授权
   *
   * @param botAuth 应用授权信息
   * @return 结果
   */
  ResultVO<Void> authBot(BotAuthDTO botAuth);

  /**
   * 查询已授权的应用
   *
   * @param botId 应用 ID
   * @return 已授权的应用
   */
  ResultVO<Map<String, List<BotAuthDTO>>> queryAuthedBotList(Long botId);

  /**
   * 发起申请，生成新的智能体应用，或加入已有应用
   *
   * @param apply 申请信息
   * @param scene 场景信息
   * @return 结果
   */
  ResultVO<Long> apply(BotApplyParams apply, BotSceneDTO scene);

  /**
   * 保存智能体规划策略
   *
   * @param bot 应用
   * @return 结果
   */
  ResultVO<Void> saveAgentStrategy(BotDTO bot);

  /**
   * 查询智能体规划策略
   *
   * @param tenantId 租户 ID
   * @param botId 应用 ID
   * @return 保存智能体规划策略
   */
  SimpleAgentStrategyDTO getAgentStrategy(Long tenantId, Long botId);

  /**
   * 修改应用授权状态
   *
   * @param botId 应用ID
   * @param authStatus 授权状态
   */
  void modifyBotAuthStatus(Long botId, String authStatus);

  /**
   * 删除应用授权
   *
   * @param botId 应用ID
   */
  void deleteBotAuth(Long botId);

  /**
   * 修改应用授权基本信息
   *
   * @param authDTO 授权信息
   */
  void modifyBotAuthInfo(BotAuthDTO authDTO);

  /**
   * 查询 BoteClaw用详情
   *
   * @param spaceId 空间 ID
   * @param botId 应用 ID
   * @return BoteClaw用
   */
  BotDTO findBoteClaw(Long spaceId, Long botId);

  /**
   * 保存 BoteClaw 应用
   *
   * @param bot BoteClaw 应用
   * @return 结果
   */
  ResultVO<BotDTO> saveBoteClaw(BotDTO bot);
}
