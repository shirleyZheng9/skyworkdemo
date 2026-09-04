package com.iwhalecloud.bote.mapper.app;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.app.SimpleWebAppDTO;
import com.iwhalecloud.bote.dto.app.WorkbenchAppDTO;
import com.iwhalecloud.bote.dto.app.query.WorkbenchAppQueryParams;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.dto.chat.query.SearchQueryParams;
import com.iwhalecloud.bote.entity.app.WorkbenchAppEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 工作台应用 Mapper
 *
 * @author tingyun.wang
 * @since 2025-09-05
 */
public interface WorkbenchAppMapper {

  /**
   * 根据workbenchAppId、spaceId获取工作台应用
   *
   * @param workbenchAppId 应用ID
   * @param spaceId 企业空间 ID
   * @return 工作台应用
   */
  WorkbenchAppDTO selectWorkbenchApp(@Param("workbenchAppId") Long workbenchAppId, @Param("spaceId") Long spaceId);

  /**
   * 新增工作台应用
   *
   * @param workbenchApp 工作台应用
   * @return 结果
   */
  int insertWorkbenchApp(@Param("dto") WorkbenchAppEntity workbenchApp);

  /**
   * 修改工作台应用
   *
   * @param workbenchApp 工作台应用
   * @return 结果
   */
  int updateWorkbenchApp(@Param("dto") WorkbenchAppDTO workbenchApp);

  /**
   * 删除工作台应用
   *
   * @param workbenchAppId 应用ID
   * @param spaceId 企业空间 ID
   * @param updatorId 更新人ID
   * @return 结果
   */
  int deleteWorkbenchApp(@Param("workbenchAppId") Long workbenchAppId, @Param("spaceId") Long spaceId, @Param("updatorId") Long updatorId);

  /**
   * 获取工作台应用列表（分页）
   *
   * @param queryParams 查询条件
   * @param rowBounds 分页参数
   * @return 工作台应用分页列表
   */
  Page<WorkbenchAppDTO> selectWorkbenchAppPage(@Param("query") WorkbenchAppQueryParams queryParams, RowBounds rowBounds);

  /**
   * 更新工作台应用状态
   *
   * @param dto 应用数据
   * @return 结果
   */
  int updateWorkbenchAppStatus(@Param("dto") WorkbenchAppDTO dto);

  /**
   * 查询应用配置状态
   *
   * @param workbenchAppId 工作台应用ID
   * @param spaceId 企业空间 ID
   * @return 应用配置状态
   */
  String getAppSettingStatus(@Param("workbenchAppId") Long workbenchAppId, @Param("spaceId") Long spaceId);

  /**
   * 查询应用启用状态
   *
   * @param workbenchAppId 工作台应用ID
   * @param spaceId 企业空间 ID
   * @return 应用配置状态
   */
  String getAppStatus(@Param("workbenchAppId") Long workbenchAppId, @Param("spaceId") Long spaceId);

  /**
   * 查询应用基本信息（一般不包含图标、描述等大字段信息）
   *
   * @param workbenchAppId 工作台应用ID
   * @param spaceId 企业空间 ID
   * @return 应用基本发信息
   */
  WorkbenchAppDTO selectAppBasicInfo(@Param("workbenchAppId") Long workbenchAppId, @Param("spaceId") Long spaceId);

  /**
   * 查询应用基本信息列表（一般不包含图标、描述等大字段信息）
   *
   * @param appIdList 工作台应用ID列表
   * @param spaceId 企业空间 ID
   * @return 应用基本发信息
   */
  List<WorkbenchAppDTO> selectAppBasicInfoList(@Param("appIdList") List<Long> appIdList, @Param("spaceId") Long spaceId);

  /**
   * 查询应用能力配置数据
   *
   * @param workbenchAppId 工作台应用ID
   * @param spaceId 企业空间 ID
   * @return 应用能力配置数据
   */
  WorkbenchAppDTO selectAppSetting(@Param("workbenchAppId") Long workbenchAppId, @Param("spaceId") Long spaceId);

  /**
   * 更新应用能力配置信息
   *
   * @param dto 应用数据
   * @return 结果
   */
  int updateAppSettingInfo(@Param("dto") WorkbenchAppDTO dto);

  /**
   * 查询工作台应用图标
   *
   * @param workbenchAppId 应用ID
   * @param spaceId 企业空间 ID
   * @return 工作台应用图标
   */
  String selectWorkbenchAppIcon(@Param("spaceId") Long spaceId, @Param("workbenchAppId") Long workbenchAppId);

  /**
   * 查询用户授权的工作台应用列表（分页）
   *
   * @param queryParams 查询条件
   * @param rowBounds 分页参数
   * @return 工作台应用分页列表
   */
  Page<WorkbenchAppDTO> selectAuthWorkbenchAppPage(@Param("query") WorkbenchAppQueryParams queryParams, RowBounds rowBounds);

  /**
   *
   * @param workbenchAppId 应用ID
   * @param spaceId 企业空间 ID
   * @return 工作台应用
   */
  WorkbenchAppDTO selectAuthWorkbenchAppDetail(@Param("workbenchAppId") Long workbenchAppId, @Param("spaceId") Long spaceId);

  /**
   * 查询授权的智能应用
   *
   * @param params 查询条件
   * @return 智能应用
   */
  List<SimpleBotDTO> selectAuthBot(@Param("query") SearchQueryParams params);

  /**
   * 查询授权的网页应用
   *
   * @param params 查询条件
   * @return 网页应用
   */
  List<SimpleWebAppDTO> selectAuthWeb(@Param("query") SearchQueryParams params);
}
