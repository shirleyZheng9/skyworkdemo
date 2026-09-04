package com.iwhalecloud.bote.mapper.app;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.app.UserFavAppDTO;
import com.iwhalecloud.bote.dto.app.query.UserFavAppQueryParams;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * 用户常用应用 Mapper
 *
 * @author wang.tingyun
 * @since 2025-09-12
 */
public interface UserFavAppMapper {

  /**
   * 批量新增用户常用应用
   *
   * @param dto 应用对象
   * @return 新增结果
   */
  int insertUserFavApp(@Param("dto") UserFavAppDTO dto);

  /**
   * 批量新增用户常用应用
   * @param dtoList 收藏列表
   * @return 新增结果
   */
  int insertUserFavAppBatch(@Param("list") List<UserFavAppDTO> dtoList);

  /**
   * 删除单个常用应用
   *
   * @param appId 应用ID
   * @param userId 用户ID
   * @param spaceId 企业空间ID
   * @return 删除结果
   */
  int deleteUserFavApp(@Param("appId") Long appId, @Param("userId") Long userId, @Param("spaceId") Long spaceId);

  /**
   * 批量删除用户常用应用
   *
   * @param list 常用应用ID列表
   * @return 删除结果
   */
  int batchDeleteUserFavApp(@Param("list") List<Long> list, @Param("userId") Long userId, @Param("spaceId") Long spaceId);

  /**
   * 根据用户ID查询常用列表
   *
   * @param params 查询参数对象
   * @return 常用列表
   */
  List<UserFavAppDTO> selectUserFavAppList(@Param("params") UserFavAppQueryParams params);

  /**
   * 分页查询用户查询常用列表
   *
   * @param params 查询参数对象
   * @return 常用列表
   */
  Page<UserFavAppDTO> selectUserFavAppPage(@Param("params") UserFavAppQueryParams params, RowBounds rowBounds);

  /**
   * 统计用户的常用应用数量
   *
   * @param userId 用户ID
   * @param spaceId 企业空间ID
   * @return 用户的常用应用数量
   */
  int countUserFavApp(@Param("userId") Long userId, @Param("spaceId") Long spaceId);

  /**
   * 修改常用应用的排序
   *
   * @param dto 常用应用对象
   * @return 修改结构
   */
  int updateSortOrder(@Param("dto") UserFavAppDTO dto);

  /**
   * 查询用户的常用应用的最大排序数
   *
   * @param userId 用户ID
   * @param spaceId 企业空间ID
   * @return 常用应用的最大排序数
   */
  Integer maxUserFavAppSort(@Param("userId") Long userId, @Param("spaceId") Long spaceId);

  /**
   * 检查应用是否已被设置为常用
   *
   * @param appId 应用ID
   * @param spaceId 企业空间ID
   * @param userId 用户ID
   * @return 检查结果
   */
  boolean checkExistsByAppId(@Param("appId") Long appId, @Param("spaceId") Long spaceId, @Param("userId") Long userId);

  /**
   * 清空用户常用应用
   *
   * @param userId 用户ID
   * @param spaceId 企业空间ID
   * @return 删除结果
   */
  int clearAllUserFavApp(@Param("userId") Long userId, @Param("spaceId") Long spaceId);

}