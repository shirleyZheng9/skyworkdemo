package com.iwhalecloud.bote.mapper.bot;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.app.query.AiBotQueryParams;
import com.iwhalecloud.bote.dto.bot.BaseBotDTO;
import com.iwhalecloud.bote.dto.bot.BeyondResourceDTO;
import com.iwhalecloud.bote.dto.bot.BeyondResourceStatusCountDTO;
import com.iwhalecloud.bote.dto.bot.BotDTO;
import com.iwhalecloud.bote.dto.bot.PublishedAppDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotUserExperienceDTO;
import com.iwhalecloud.bote.dto.bot.SquareBotDTO;
import com.iwhalecloud.bote.dto.bot.query.BeyondResourceQueryParams;
import com.iwhalecloud.bote.dto.bot.query.BotQueryParams;
import com.iwhalecloud.bote.dto.bot.query.PublishedAppQueryParams;
import com.iwhalecloud.bote.dto.chat.query.SearchQueryParams;
import com.iwhalecloud.bote.entity.bot.BotEntity;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;

/**
 * 机器人查询 Mapper
 *
 * @author chen.linfa
 * @since 2024-08-01
 */
public interface BotQueryMapper {

  /**
   * 根据主键查询机器人
   *
   * @param botId 机器人
   * @return 机器人
   */
  SimpleBotDTO getBot(@Param("botId") Long botId, @Param("tenantId") Long tenantId);

  /**
   * 查询机器人的基本信息
   */
  SimpleBotDTO selectSimpleBot(@Param("tenantId") Long tenantId, @Param("botId") Long botId);

  /**
   * 根据主键查询机器人名称
   */
  String selectBotNameByBotId(@Param("tenantId") Long tenantId, @Param("botId") Long botId);

  /**
   * 批量查询机器人的基本信息
   */
  List<SimpleBotDTO> selectSimpleBots(@Param("tenantId") Long tenantId, @Param("botIds") List<Long> botIds, @Nullable @Param("botStatus") String botStatus);

  /**
   * 查询机器人的智能体规划策略
   */
  SimpleBotDTO selectAgentStrategy(@Param("tenantId") Long tenantId, @Param("botId") Long botId);

  /**
   * 查询机器人的欢迎页配置
   */
  String selectPageSettingInfo(@Param("tenantId") Long tenantId, @Param("botId") Long botId);

  /**
   * 校验智能体是否存在且有效
   *
   * @param botId 智能体ID
   * @param tenantId 租户ID
   * @return 是否存在
   */
  boolean existsBotById(@Param("botId") Long botId, @Param("tenantId") Long tenantId);

  /**
   * 查询机器人图标
   */
  BotEntity selectBotIcon(@Param("tenantId") Long tenantId, @Param("botId") Long botId);

  /**
   * 获取当前租户下默认的机器人
   *
   * @param tenantId 租户 ID
   * @return 机器人
   */
  SimpleBotDTO getDefaultBot(@Param("tenantId") Long tenantId);

  /**
   * 获取当前租户下随机一个机器人
   *
   * @param tenantId 租户 ID
   * @return 机器人
   */
  SimpleBotDTO getFirstBot(@Param("tenantId") Long tenantId);

  /**
   * 检查是否存在机器人收藏
   */
  boolean existsBotFavor(@Param("tenantId") Long tenantId, @Param("botId") Long botId, @Param("userId") Long userId);

  /**
   * 会话辅助信息
   */
  List<SimpleBotUserExperienceDTO> selectBotUserExperience(@Param("tenantId") Long tenantId, @Param("botId") Long botId);

  /**
   * 获取机器人的默认大模型 ID
   */
  @Nullable
  Long selectModelIdByBotId(@Param("tenantId") Long tenantId, @Param("botId") Long botId);

  /**
   * 查询机器人列表（分页）
   *
   * @param queryParams 查询条件
   * @param rowBounds 分页信息
   * @return 机器人列表（分页）
   */
  Page<BotDTO> selectBotPage(@Param("query") BotQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询机器人列表（分页），用于百应平台
   *
   * @param queryParams 查询条件
   * @param rowBounds 分页信息
   * @return 机器人列表（分页）
   */
  Page<BotDTO> beyondQueryBotPage(@Param("query") BotQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询智能应用与智能体混合列表（分页），用于百应平台
   *
   * @param queryParams 查询条件
   * @param userId 当前登录用户ID
   * @param rowBounds 分页信息
   * @return 资源列表（分页）
   */
  Page<BeyondResourceDTO> beyondQueryResourcePage(@Param("query") BeyondResourceQueryParams queryParams, @Param("userId") Long userId, RowBounds rowBounds);

  /**
   * 获取指定过滤条件下的上下架状态数量，用于百应平台
   *
   * @param queryParams 查询条件
   * @param userId 当前登录用户ID
   * @return 状态统计结果
   */
  BeyondResourceStatusCountDTO beyondGetTotalCountOnAllStatus(@Param("query") BeyondResourceQueryParams queryParams, @Param("userId") Long userId);

  /**
   * 查询智能应用列表（分页），用于百应平台分隔查询
   *
   * @param queryParams 查询条件
   * @param rowBounds 分页信息
   * @return 智能应用列表（分页）
   */
  Page<BotDTO> beyondQueryBotPageForSeparated(@Param("query") BeyondResourceQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询机器人列表
   *
   * @param queryParams 查询条件
   * @return 机器人列表
   */
  List<BotDTO> selectBotList(@Param("query") BotQueryParams queryParams);

  /**
   * 查询机器人列表，用于应用广场
   *
   * @return 机器人列表
   */
  List<BotDTO> selectCommonBotList();

  /**
   * 查询当前用户最近访问的前4个机器人
   *
   * @param queryParams 查询条件
   * @return 机器人列表
   */
  List<BotDTO> selectUserBotList(@Param("query") BotQueryParams queryParams);

  /**
   * 查询推荐的机器人列表
   *
   * @param queryParams 查询条件
   * @return 机器人列表
   */
  List<BotDTO> selectPopularBotList(@Param("query") BotQueryParams queryParams);

  /**
   * 查询租户下应用列表，用于意图识别
   *
   * @param tenantId 租户 ID
   * @return 应用列表
   */
  List<SimpleBotDTO> selectBotListForIntent(@Param("tenantId") Long tenantId);

  /**
   * 根据智能体 ID 查询关联的应用
   *
   * @param tenantId 租户 ID
   * @param sceneId 智能体 ID
   * @return 应用列表
   */
  List<SimpleBotDTO> selectBotListBySceneId(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId);

  /**
   * 查询租户下应用列表
   *
   * @param tenantId 租户 ID，非必填
   * @param botIds 应用 ID 集合
   * @return 应用列表
   */
  List<SimpleBotDTO> selectBotListByIds(@Param("tenantId") @Nullable Long tenantId, @Param("botIds") Collection<Long> botIds);

  /**
   * 查询租户下应用列表
   *
   * @param tenantId 租户 ID
   * @return 场景列表
   */
  List<BaseBotDTO> selectBaseBotList(@Param("tenantId") Long tenantId);

  /**
   * 查询应用的欢迎页图标
   *
   * @param tenantId 租户 ID
   * @param botId 机器人
   * @return 机器人
   */
  SimpleBotDTO getBotPageSettingIcon(@Param("tenantId") Long tenantId, @Param("botId") Long botId);

  /**
   * 分页查询AI助理信息列表
   *
   * @param params 查询参数
   * @return 应用信息列表
   */
  Page<BaseBotDTO> selectAiBotPage(@Param("params") AiBotQueryParams params, RowBounds rowBounds);

  /**
   * 根据应用ID列表和企业空间ID查询应用信息列表
   *
   * @param spaceId 企业空间 ID
   * @param botIds 应用 ID 集合
   * @return 应用列表
   */
  List<SimpleBotDTO> selectBotListByIdsAndSpaceId(@Param("spaceId") @Nullable Long spaceId, @Param("botIds") Collection<Long> botIds);

  /**
   * 分页查询已上架智能应用列表
   *
   * @param params 查询参数
   * @param tenantId 灵犀租户ID（由 extTenantId 转换）
   * @param rowBounds 分页参数
   * @return 已上架智能应用列表（分页）
   */
  Page<PublishedAppDTO> selectPublishedAppPage(@Param("params") PublishedAppQueryParams params, @Param("tenantId") Long tenantId,
                                               RowBounds rowBounds);

  /**
   * 搜索BoteClaw机器人(运行态通用搜索使用)
   *
   * @param params 查询参数
   * @return 机器人列表
   */
  List<SimpleBotDTO> selectBoteClawList(@Param("params") SearchQueryParams params);


  /**
   * 查询通用机器人列表（不含广场技能安装态，由 Service 二次查询填充）
   *
   * @param tenantId 项目id或者空间id
   * @param userId 登录人
   */
  List<SquareBotDTO> selectSquareBotDTOList(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

  /**
   * 查询所有 BoteClaw botId
   */
  List<Long> selectAllBoteClawId();

  /**
   * 查询用户创建的BoteCraw应用列表
   *
   * @param spaceId 企业空间ID
   * @param userId 创建人
   */
  List<SimpleBotDTO> selectUserBoteCrawList(@Param("spaceId") Long spaceId, @Param("userId") Long userId);

  /**
   * 分页查询 BoteClaw 应用
   *
   * @param query 查询参数
   * @param rowBounds 分页参数
   * @return BoteClaw 应用列表
   */
  Page<BotDTO> selectBoteClawPage(@Param("query") BotQueryParams query, RowBounds rowBounds);

  /**
   * AI 门户搜索授权的或创建的 BoteClaw 应用
   *
   * @param queryParams 查询参数
   * @return 应用列表
   */
  List<SimpleBotDTO> searchBoteClaw(@Param("query") BotQueryParams queryParams);

  /**
   * 查询 BoteClaw 应用归属的空间 ID
   */
  Long getBoteClawSpacId(@Param("botId") Long botId);
}
