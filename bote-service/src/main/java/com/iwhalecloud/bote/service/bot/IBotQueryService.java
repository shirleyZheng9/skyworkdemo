package com.iwhalecloud.bote.service.bot;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.app.query.AiBotQueryParams;
import com.iwhalecloud.bote.dto.bot.BaseBotDTO;
import com.iwhalecloud.bote.dto.bot.BeyondResourceDTO;
import com.iwhalecloud.bote.dto.bot.BeyondResourceStatusCountDTO;
import com.iwhalecloud.bote.dto.bot.BotAuthDTO;
import com.iwhalecloud.bote.dto.bot.BotDTO;
import com.iwhalecloud.bote.dto.bot.PublishedAppDTO;
import com.iwhalecloud.bote.dto.bot.SceneIntentDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.dto.bot.SquareBotDTO;
import com.iwhalecloud.bote.dto.bot.query.BeyondResourceQueryParams;
import com.iwhalecloud.bote.dto.bot.query.BotAuthQueryParams;
import com.iwhalecloud.bote.dto.bot.query.BotQueryParams;
import com.iwhalecloud.bote.dto.bot.query.PublishedAppQueryParams;
import java.util.List;
import java.util.Map;

import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import org.springframework.lang.Nullable;

/**
 * 应用查询服务
 *
 * @author chen.linfa
 * @since 2024-08-01
 */
public interface IBotQueryService {
  /**
   * 根据应用 ID 查询配置
   *
   * @param botId 应用 ID
   * @param tenantId 租户 ID
   * @return 应用
   */
  @Nullable
  SimpleBotDTO getBotById(Long botId, Long tenantId);

  /**
   * 根据应用 ID 查询基本配置
   *
   * @param tenantId 租户 ID
   * @param botId 应用 ID
   * @return 应用
   */
  SimpleBotDTO getSimpleBot(Long tenantId, Long botId);

  /**
   * 根据租户 ID 查询默认的应用，如果没有，随机返回一个
   *
   * @param tenantId 租户 ID
   * @return 应用
   */
  SimpleBotDTO getDefaultBot(Long tenantId);

  /**
   * 查询应用列表（分页）
   *
   * @param queryParams 查询条件
   * @return 应用列表
   */
  PageInfo<BotDTO> queryBotPage(BotQueryParams queryParams);

  /**
   * 查询应用列表（分页），用于百应平台
   *
   * @param queryParams 查询条件
   * @return 应用列表
   */
  PageInfo<BotDTO> beyondQueryBotPage(BotQueryParams queryParams);

  /**
   * 查询智能应用与智能体混合列表（分页），用于百应平台
   *
   * @param queryParams 查询条件
   * @return 资源列表
   */
  PageInfo<BeyondResourceDTO> beyondQueryResourcePage(BeyondResourceQueryParams queryParams);

  /**
   * 获取指定过滤条件下的上下架状态数量，用于百应平台
   *
   * @param queryParams 查询条件
   * @return 状态统计结果
   */
  BeyondResourceStatusCountDTO beyondGetTotalCountOnAllStatus(BeyondResourceQueryParams queryParams);

  /**
   * 查询应用列表（包含场景）
   *
   * @param tenantId 租户 ID
   * @return 应用列表
   */
  List<BaseBotDTO> queryBotAndSceneList(Long tenantId);

  /**
   * 查询应用列表
   *
   * @param queryParams 查询条件
   * @return 应用列表
   */
  List<BotDTO> queryBotList(BotQueryParams queryParams);

  /**
   * 查询应用列表，用于应用广场
   *
   * @return 应用列表
   */
  List<BotDTO> queryCommonBotList();

  /**
   * 查询当前用户参与开发的应用列表（分页）
   *
   * @param queryParams 查询条件
   * @return 应用列表（分页）
   */
  PageInfo<BotDTO> queryUserBotPage(BotQueryParams queryParams);

  /**
   * 查询当前用户最近访问的前4个应用
   *
   * @param queryParams 查询条件
   * @return 应用列表
   */
  List<BotDTO> queryUserBotList(BotQueryParams queryParams);

  /**
   * 查询推荐的应用列表
   *
   * @param queryParams 查询条件
   * @return 应用列表
   */
  List<BotDTO> queryPopularBotList(BotQueryParams queryParams);

  /**
   * 应用授权列表（分页）
   *
   * @param queryParams 查询参数
   * @return 结果
   */
  PageInfo<BotAuthDTO> queryAuthBotPage(BotAuthQueryParams queryParams);

  /**
   * 查询租户下应用列表，用于意图识别
   *
   * @param tenantId 租户 ID
   * @return 应用列表
   */
  List<SimpleBotDTO> queryBotListForIntent(Long tenantId);

  /**
   * 查询租户下智能体列表，用于意图识别
   *
   * @param tenantId 租户 ID
   * @param botId 应用 ID
   * @return 应用列表
   */
  List<SceneIntentDTO> querySceneListForIntent(Long tenantId, @Nullable Long botId);

  /**
   * 查询应用的欢迎页图标
   *
   * @param tenantId 租户 ID
   * @param botId 应用 ID
   * @return 欢迎页图标
   */
  Map<String, Object> getPageSettingIcon(Long tenantId, Long botId);

  /**
   * 分页查询AI助理信息列表
   *
   * @param params 查询参数
   * @return 应用信息列表
   */
  PageInfo<BaseBotDTO> queryAiBotPage(AiBotQueryParams params);

  /**
   * 分页查询授权应用列表（授权管理使用）
   *
   * @param queryParams 查询参数
   * @return 应用授权发布列表
   */
  PageInfo<BotAuthDTO> queryBotAuthPageForManage(BotAuthQueryParams queryParams);

  /**
   * 分页查询已上架智能应用列表（开放接口）
   *
   * @param params 查询参数（含 extTenantId、keyword）
   * @return 已上架智能应用列表（含应用ID、租户ID、应用名称、应用描述、副驾链接）
   */
  PageInfo<PublishedAppDTO> queryPublishedAppPage(PublishedAppQueryParams params);

  /**
   * 获取当前用户空间智能体列表（占位：返回空列表）
   */
  List<SquareBotDTO> listClawBotsInSpace(Long skillId, Long tenantId);

  /**
   * 根据BoteClaw列表（用于应用选择场景）
   *
   * @param spaceId 空间ID
   * @param containAuthBot 是否包含授权应用
   * @return BoteClaw应用列表
   */
  List<SimpleBotDTO> queryBoteCrawList(Long spaceId, Boolean containAuthBot);

  /**
   * 分页查询BoteClaw应用列表
   *
   * @param queryParams 查询参数
   * @return BoteClaw应用列表
   */
  PageInfo<BotDTO> queryBoteClawPage(BotQueryParams queryParams);

  /**
   * AI 门户搜索授权的或创建的 BoteClaw 应用
   *
   * @param params 查询参数
   * @return BoteClaw应用列表
   */
  ResultVO<List<SimpleBotDTO>> searchBoteClaw(BotQueryParams params);
}
