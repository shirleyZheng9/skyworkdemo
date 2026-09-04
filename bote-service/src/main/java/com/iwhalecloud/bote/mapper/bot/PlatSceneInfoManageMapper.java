package com.iwhalecloud.bote.mapper.bot;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.bot.PlatSceneInfoDTO;
import com.iwhalecloud.bote.dto.bot.query.PlatSceneInfoQueryParams;
import com.iwhalecloud.bote.dto.scene.SimpleSceneDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 模板智能体管理 Mapper
 *
 * @author auto
 * @since 2025-06-21
 */
public interface PlatSceneInfoManageMapper {

  /**
   * 校验场景是否已存在模板（一对一关系校验）
   *
   * @param sceneId 场景ID
   * @param tenantId 租户ID
   * @param platSceneId 模板ID（修改时排除自己）
   * @return 是否存在
   */
  boolean existsPlatSceneInfo(@Param("sceneId") Long sceneId, @Param("tenantId") Long tenantId, @Param("platSceneId") Long platSceneId);

  /**
   * 查找已删除的模板智能体记录
   *
   * @param sceneId 智能体ID
   * @param tenantId 租户ID
   * @return 已删除的模板智能体记录
   */
  PlatSceneInfoDTO findDeletedPlatSceneInfo(@Param("sceneId") Long sceneId, @Param("tenantId") Long tenantId);

  /**
   * 根据主键获取模板智能体
   *
   * @param platSceneId 模板ID
   * @param tenantId 租户ID
   * @return 模板智能体
   */
  PlatSceneInfoDTO getPlatSceneInfo(@Param("platSceneId") Long platSceneId, @Param("tenantId") Long tenantId);

  /**
   * 新增模板智能体
   *
   * @param platSceneInfo 模板智能体
   * @return 结果
   */
  int insertPlatSceneInfo(@Param("dto") PlatSceneInfoDTO platSceneInfo);


  /**
   * 修改模板智能体
   *
   * @param platSceneInfo 模板智能体
   * @return 结果
   */
  int updatePlatSceneInfo(@Param("dto") PlatSceneInfoDTO platSceneInfo);

  /**
   * 删除模板智能体
   *
   * @param platSceneId 模板ID
   * @param tenantId 租户ID
   * @param updatorId 操作人ID
   * @return 结果
   */
  int deletePlatSceneInfo(@Param("platSceneId") Long platSceneId, @Param("tenantId") Long tenantId, @Param("updatorId") Long updatorId);

  /**
   * 查询模板智能体列表（分页）
   *
   * @param queryParams 查询条件
   * @param rowBounds 分页参数
   * @return 模板智能体分页列表
   */
  Page<PlatSceneInfoDTO> selectPlatSceneInfoPage(@Param("query") PlatSceneInfoQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询模板智能体列表
   *
   * @param queryParams 查询条件
   * @return 模板智能体列表
   */
  List<PlatSceneInfoDTO> selectPlatSceneInfoList(@Param("query") PlatSceneInfoQueryParams queryParams);

  /**
   * 获取可选择的场景列表（用于新增模板时的下拉选择）
   *
   * @param tenantId 租户ID
   * @param searchContent 搜索关键词
   * @return 场景列表
   */
  List<PlatSceneInfoDTO> selectAvailableScenes(@Param("tenantId") Long tenantId, @Param("searchContent") String searchContent);

  /**
   * 获取最大排序值
   *
   * @param tenantId 租户ID
   * @return 最大排序值
   */
  Integer getMaxSortOrder(@Param("tenantId") Long tenantId);

  /**
   * 查询智能体列表（分页）
   *
   * @param queryParams 查询条件
   * @param rowBounds 分页参数
   * @return 智能体列表分页列表
   */
  Page<SimpleSceneDTO> selectSimpleScenePage(@Param("query") PlatSceneInfoQueryParams queryParams, RowBounds rowBounds);
}
