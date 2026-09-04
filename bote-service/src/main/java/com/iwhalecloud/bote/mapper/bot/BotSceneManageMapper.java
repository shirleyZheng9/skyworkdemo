package com.iwhalecloud.bote.mapper.bot;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.bot.BotSceneBatchOperDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.bot.query.BeyondResourceQueryParams;
import com.iwhalecloud.bote.dto.bot.query.BotSceneQueryParams;
import com.iwhalecloud.bote.entity.bot.BotSceneEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 场景配置 Mapper
 *
 * @author chen.linfa
 * @since 2024-08-02
 */
public interface BotSceneManageMapper {
  /**
   * 检查场景名称是否存在
   */
  boolean existsSceneName(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId, @Param("sceneName") String sceneName);

  /**
   * 新增场景
   *
   * @param scene 场景
   * @return 结果
   */
  int insertScene(@Param("dto") BotSceneEntity scene);

  /**
   * 修改场景
   *
   * @param scene 场景
   * @return 结果
   */
  int updateScene(@Param("dto") BotSceneDTO scene);

  /**
   * 修改场景基本信息
   */
  int updateSceneBasicInfo(@Param("dto") BotSceneEntity scene);

  /**
   * 删除场景
   */
  int deleteScene(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId, @Param("updatorId") Long updatorId);

  /**
   * 更新场景状态
   */
  int updateSceneStatus(@Param("tenantId") Long tenantId, @Param("sceneStatus") String sceneStatus, @Param("sceneId") Long sceneId, @Param("updatorId") Long updatorId);

  /**
   * 根据主键查询场景
   */
  BotSceneDTO getScene(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId);

  /**
   * 查询智能体的基本信息
   */
  BotSceneDTO selectSimpleScene(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId);

  /**
   * 根据主键查询场景的基本信息
   */
  BotSceneEntity selectSceneBasicInfo(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId);

  /**
   * 查询场景列表（分页）- 用于机器人场景设置
   *
   * @param queryParams 查询条件
   * @param rowBounds 分页信息
   * @return 场景列表（分页）
   */
  Page<BotSceneDTO> selectScenePage(@Param("query") BotSceneQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询场景列表
   *
   * @param queryParams 查询条件
   * @return 场景列表
   */
  List<BotSceneDTO> selectSceneList(@Param("query") BotSceneQueryParams queryParams);

  /**
   * 查询场景列表，用于智能体加入应用
   *
   * @param queryParams 查询条件
   * @param rowBounds 分页信息
   * @return 场景列表
   */
  Page<BotSceneDTO> queryScenePageForBot(@Param("query") BotSceneQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询场景列表（分页），用于百应平台
   *
   * @param queryParams 查询条件
   * @param rowBounds 分页信息
   * @return 场景列表
   */
  Page<BotSceneDTO> beyondQueryScenePage(@Param("query") BotSceneQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询智能体列表（分页），用于百应平台分隔查询
   *
   * @param queryParams 查询条件
   * @param rowBounds 分页信息
   * @return 智能体列表（分页）
   */
  Page<BotSceneDTO> beyondQueryScenePageForSeparated(@Param("query") BeyondResourceQueryParams queryParams, RowBounds rowBounds);

  /**
   * 修改场景更新时间
   *
   * @param dto 场景对象
   * @return 结果
   */
  int updateSceneUpdatedTime(@Param("dto") BotSceneDTO dto);

  /**
   * 批量查询场景信息
   *
   * @param tenantId 租户ID
   * @param sceneIds 场景ID列表
   * @return 场景列表
   */
  List<BotSceneDTO> getScenesByIds(@Param("tenantId") Long tenantId, @Param("sceneIds") List<Long> sceneIds);

  /**
   * 根据场景ID列表查询智能体信息列表
   *
   * @param sceneIds 场景ID列表
   * @param tenantId 租户ID
   * @return 智能体信息列表
   */
  List<BotSceneDTO> selectListBySceneIds(@Param("sceneIds") List<Long> sceneIds, @Param("tenantId") Long tenantId);

  /**
   * 批量更新智能体发布状态
   *
   * @param dto 智能体操作DTO
   * @return 更新结果
   */
  int batchUpdateSceneStatus(@Param("dto") BotSceneBatchOperDTO dto);

  /**
   * 批量更新智能体目录ID
   *
   * @param dto 智能体操作DTO
   * @return 更新结果
   */
  int batchUpdateSceneCatalogItemId(@Param("dto") BotSceneBatchOperDTO dto);

  /**
   * 批量删除场景
   *
   * @param dto 智能体操作DTO
   * @return 删除结果
   */
  int batchDeleteScene(@Param("dto") BotSceneBatchOperDTO dto);

}
