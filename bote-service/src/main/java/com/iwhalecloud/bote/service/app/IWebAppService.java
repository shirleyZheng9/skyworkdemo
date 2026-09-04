package com.iwhalecloud.bote.service.app;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.app.WebAppDTO;
import com.iwhalecloud.bote.dto.app.query.WebAppQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

import java.util.List;

/**
 * 网页应用服务
 *
 * @author tingyun.wang
 * @since 2025-09-05
 */
public interface IWebAppService {

  /**
   * 保存网页应用
   *
   * @param webAppDTO 网页应用DTO
   * @return 结果
   */
  ResultVO<WebAppDTO> saveWebApp(WebAppDTO webAppDTO);

  /**
   * 查询网页应用列表（分页）
   *
   * @param queryParams 查询条件
   * @return 网页应用分页列表
   */
  PageInfo<WebAppDTO> queryWebAppPage(WebAppQueryParams queryParams);

  /**
   * 查询网页应用的基本信息列表
   *
   * @param spaceId 企业空间ID
   * @return 网页应用的基本信息列表
   */
  List<WebAppDTO> queryWebAppList(Long spaceId);

  /**
   * 获取网页应用
   *
   * @param webAppId 应用ID
   * @param spaceId 企业空间ID
   * @return 网页应用详情
   */
  WebAppDTO getWebApp(Long webAppId, Long spaceId);

  /**
   * 删除网页应用
   *
   * @param webAppId 应用ID
   * @param spaceId 企业空间ID
   * @return 结果
   */
  ResultVO<Void> deleteWebApp(Long webAppId, Long spaceId);

  /**
   * 获取网页应用图标
   *
   * @param webAppId 应用ID
   * @param spaceId 企业空间ID
   * @return 网页应用图标
   */
  String getWebAppIcon(Long webAppId, Long spaceId);

}
