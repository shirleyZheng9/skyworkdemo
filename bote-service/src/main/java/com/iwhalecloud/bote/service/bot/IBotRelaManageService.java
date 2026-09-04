package com.iwhalecloud.bote.service.bot;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.bot.BotSceneRelDTO;
import com.iwhalecloud.bote.dto.bot.query.BotQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 应用关联管理服务
 *
 * @author chen.linfa
 * @since 2025-04-24
 */
public interface IBotRelaManageService {
  /**
   * 查询应用关联的场景列表（分页）
   *
   * @param query 查询参数
   * @return 关联的场景列表（分页）
   */
  PageInfo<BotSceneRelDTO> queryBotSceneRelPage(BotQueryParams query);

  /**
   * 查询应用关联的场景列表
   *
   * @param query 查询参数
   * @return 关联的场景列表
   */
  List<BotSceneRelDTO> queryBotSceneRelList(BotQueryParams query);

  /**
   * 添加智能体
   *
   * @param tenantId 租户 ID
   * @param botId 应用 ID
   * @param sceneIds 场景 ID 列表
   * @return 结果
   */
  ResultVO<Void> addBotSceneRel(Long tenantId, Long botId, List<Long> sceneIds);

  /**
   * 移除智能体
   *
   * @param tenantId 租户 ID
   * @param botId 应用 ID
   * @param sceneId 场景 ID
   * @return 结果
   */
  ResultVO<Void> removeBotSceneRel(Long tenantId, Long botId, Long sceneId);

  /**
   * 设置默认智能体
   *
   * @param tenantId 租户 ID
   * @param botId 应用 ID
   * @param sceneId 场景 ID
   * @return 结果
   */
  ResultVO<Void> setDefaultBotSceneRel(Long tenantId, Long botId, Long sceneId);

  /**
   * 取消默认智能体
   *
   * @param tenantId 租户 ID
   * @param botId 应用 ID
   * @return 结果
   */
  ResultVO<Void> cancelDefaultBotSceneRel(Long tenantId, Long botId);

  /**
   * 检查应用是否存在关联智能体
   *
   * @param tenantId 租户 ID
   * @param botId 应用 ID
   * @return 是否存在关联智能体
   */
  boolean existsBotSceneRel(Long tenantId, Long botId);

  /**
   * 查询租户下的规划智能体
   *
   * @param tenantId 租户 ID
   * @return 规划智能体
   */
  @Nullable
  List<BotSceneRelDTO> queryPlanAgentList(Long tenantId);
}
