package com.iwhalecloud.bote.service.app;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.app.BatchSaveUserFavAppDTO;
import com.iwhalecloud.bote.dto.app.UserFavAppDTO;
import com.iwhalecloud.bote.dto.app.query.UserFavAppQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

import java.util.List;

/**
 * 用户常用应用服务
 *
 * @author wang.tingyun
 * @since 2025-09-12
 */
public interface IUserFavAppService {

  /**
   * 分页查询用户常用应用列表
   *
   * @param params 查询参数
   * @return 常用应用列表
   */
  PageInfo<UserFavAppDTO> queryUserFavAppPage(UserFavAppQueryParams params);

  /**
   * 查询用户常用应用列表
   *
   * @param spaceId 企业空间ID
   * @return 常用应用列表
   */
  List<UserFavAppDTO> queryUserFavAppList(Long spaceId);

  /**
   * 新增常用应用
   *
   * @param favAppDTO 常用应用对象
   * @return 结果
   */
  ResultVO<Void> addUserFavApp(UserFavAppDTO favAppDTO);

  /**
   * 批量保存常用应用
   *
   * @param appDTO 批量常用应用DTO
   * @return 结果
   */
  ResultVO<Void> batchSaveUserFavApp(BatchSaveUserFavAppDTO appDTO);

  /**
   * 移除某个常用的应用
   *
   * @param appId 应用ID
   * @param spaceId 企业空间ID
   * @return 结果
   */
  ResultVO<Void> removeUserFavApp(Long appId, Long spaceId);

}