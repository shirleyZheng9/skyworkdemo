package com.iwhalecloud.bote.mapper.app;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.app.SimpleWebAppDTO;
import com.iwhalecloud.bote.dto.app.WebAppDTO;
import com.iwhalecloud.bote.dto.app.query.WebAppQueryParams;
import com.iwhalecloud.bote.entity.app.WebAppEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 网页应用 Mapper
 *
 * @author tingyun.wang
 * @since 2025-09-05
 */
public interface WebAppMapper {

  /**
   * 根据webAppId、spaceId获取网页应用
   *
   * @param webAppId 应用ID
   * @param spaceId 企业空间 ID
   * @return 网页应用
   */
  WebAppDTO selectWebApp(@Param("webAppId") Long webAppId, @Param("spaceId") Long spaceId);

  /**
   * 获取网页应用基本信息
   *
   * @param webAppId 应用ID
   * @param spaceId 企业空间 ID
   * @return 网页应用
   */
  WebAppDTO selectWebAppBasicInfo(@Param("webAppId") Long webAppId, @Param("spaceId") Long spaceId);

  /**
   * 新增网页应用
   *
   * @param webApp 网页应用
   * @return 结果
   */
  int insertWebApp(@Param("dto") WebAppEntity webApp);

  /**
   * 修改网页应用
   *
   * @param webApp 网页应用
   * @return 结果
   */
  int updateWebApp(@Param("dto") WebAppDTO webApp);

  /**
   * 删除网页应用
   *
   * @param webAppId 应用ID
   * @return 结果
   */
  int deleteWebApp(@Param("webAppId") Long webAppId, @Param("spaceId") Long spaceId, @Param("updatorId") Long updatorId);

  /**
   * 获取网页应用列表（分页）
   *
   * @param queryParams 查询条件
   * @param rowBounds 分页参数
   * @return 网页应用分页列表
   */
  Page<WebAppDTO> selectWebAppPage(@Param("query") WebAppQueryParams queryParams, RowBounds rowBounds);

  /**
   * 获取全部网页应用列表
   *
   * @param spaceId 企业空间 ID
   * @return 网页应用列表
   */
  List<WebAppDTO> selectWebAppList(@Param("spaceId") Long spaceId);

  /**
   * 检查是否被已启用的工作台应用关联
   *
   * @param webAppId 网页应用ID
   * @return 检查结果
   */
  boolean checkUsedForBenchApp(@Param("webAppId") Long webAppId);

  /**
   * 查询获取网页应用图标
   *
   * @param webAppId 应用ID
   * @param spaceId 企业空间 ID
   * @return 网页应用图标
   */
  String selectWebAppIcon(@Param("spaceId") Long spaceId, @Param("webAppId") Long webAppId);

  /**
   * 通过 botId 查询应用包下的网页应用
   *
   * @param tenantId 租户ID
   * @param botId 助手 ID
   * @return 网页应用
   */
  SimpleWebAppDTO selectWebAppByBotId(@Param("tenantId") Long tenantId, @Param("botId") Long botId);
}
