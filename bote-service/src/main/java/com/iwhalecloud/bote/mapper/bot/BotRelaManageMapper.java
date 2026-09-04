package com.iwhalecloud.bote.mapper.bot;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.bot.BotSceneRelDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneRelDTO;
import com.iwhalecloud.bote.dto.bot.query.BotQueryParams;
import java.util.List;

import com.iwhalecloud.bote.dto.chat.ChatThemeDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;

/**
 * 机器人关联管理
 *
 * @author chen.linfa
 * @since 2025-04-23
 */
public interface BotRelaManageMapper {
  /**
   * 批量新增关联的场景
   *
   * @param list 关联的场景
   * @return 结果
   */
  int batchInsertBotSceneRel(@Param("list") List<BotSceneRelDTO> list);

  /**
   * 删除关联场景
   *
   * @param tenantId 租户 ID
   * @param botId 机器人 ID
   * @param sceneId 场景 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteBotSceneRel(@Param("tenantId") Long tenantId, @Param("botId") Long botId, @Param("sceneId") Long sceneId, @Param("updatorId") Long updatorId);

  /**
   * 更新关联场景
   *
   * @param tenantId 租户 ID
   * @param botId 机器人 ID
   * @param sceneId 场景 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int updateDefaultBotSceneRel(@Param("tenantId") Long tenantId, @Param("botId") Long botId, @Param("sceneId") Long sceneId, @Param("updatorId") Long updatorId);

  /**
   * 清理机器人下的默认场景
   *
   * @param tenantId 租户 ID
   * @param botId 机器人 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int clearDefaultBotSceneRel(@Param("tenantId") Long tenantId, @Param("botId") Long botId, @Param("updatorId") Long updatorId);

  /**
   * 查询机器人关联的场景列表
   *
   * @param tenantId 租户 ID
   * @param botIds 机器人 ID 列表
   * @return 关联场景列表
   */
  List<BotSceneRelDTO> selectBotSceneRelList(@Param("tenantId") Long tenantId, @Param("botIds") List<Long> botIds);

  /**
   * 查询机器人关联的场景 ID 列表
   */
  List<Long> selectRelSceneIds(@Param("tenantId") Long tenantId, @Param("botId") Long botId);

  /**
   * 查询机器人关联的场景列表
   *
   * @param tenantId 租户 ID
   * @param botIds 机器人 ID 列表
   * @return 简单关联场景列表
   */
  List<SimpleBotSceneRelDTO> selectSimpleBotSceneRelList(@Param("tenantId") Long tenantId, @Param("botIds") List<Long> botIds);

  /**
   * 查询机器人关联的场景列表（分页）
   *
   * @param queryParams 查询条件
   * @param rowBounds 分页信息
   * @return 关联场景（分页）
   */
  Page<BotSceneRelDTO> selectBotSceneRelPage(@Param("query") BotQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询机器人关联的场景列表
   *
   * @param queryParams 查询条件
   * @return 关联场景
   */
  List<BotSceneRelDTO> selectBotSceneRels(@Param("query") BotQueryParams queryParams);

  /**
   * 检查场景是否存在关联的机器人
   *
   * @param tenantId 租户 ID
   * @param botId 应用 ID，非必填
   * @param sceneId 场景 ID
   * @return 结果
   */
  boolean existsBotSceneRelByScene(@Param("tenantId") Long tenantId, @Nullable @Param("botId") Long botId, @Param("sceneId") Long sceneId);

  /**
   * 通过应用 ID 检查场景是否存在关联的机器人
   *
   * @param tenantId 租户 ID
   * @param botId 应用 ID
   * @return 结果
   */
  boolean existsBotSceneRelByBot(@Param("tenantId") Long tenantId, @Param("botId") Long botId);

  /**
   * 查询有效的关联场景
   *
   * @param tenantId 租户 ID
   * @param relId 关联 ID
   * @return 关联场景
   */
  BotSceneRelDTO getBotSceneRel(@Param("tenantId") Long tenantId, @Param("relId") Long relId);

  /**
   * 查询应用下的规划智能体
   *
   * @param tenantId 租户 ID
   * @param labelName 标签名称
   * @return 规划智能体
   */
  List<BotSceneRelDTO> selectPlanAgentList(@Param("tenantId") Long tenantId, @Param("labelName") String labelName);

  /**
   * 根据sceneId查询全部botId
   */
  List<Long> querytBotIdBySceneId(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId);

  /**
   * 查询机器人关联的主题列表
   *
   * @param tenantId 租户 ID
   * @param botId 机器人 ID
   * @return 关联主题列表
   */
  List<ChatThemeDTO> selectSimpleBotThemeRelList(@Param("tenantId") Long tenantId, @Param("botId") Long botId);

}
