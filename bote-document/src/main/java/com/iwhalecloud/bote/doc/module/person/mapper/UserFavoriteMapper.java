package com.iwhalecloud.bote.doc.module.person.mapper;

import com.iwhalecloud.bote.doc.module.person.entity.UserFavoriteEntity;
import org.apache.ibatis.annotations.Param;

/**
 * 用户收藏相关数据库操作
 *
 * @author yangran
 * @since 2025-08-13
 */
public interface UserFavoriteMapper {

  /**
   * 根据主键查询
   */
  UserFavoriteEntity selectByFavoriteId(@Param("favoriteId") Long favoriteId);

  /**
   * 插入记录
   */
  int insert(@Param("favorite") UserFavoriteEntity favorite);

  /**
   * 更新记录
   */
  int update(@Param("favorite") UserFavoriteEntity favorite);

  /**
   * 逻辑删除
   */
  int deleteByFavoriteId(@Param("favoriteId") Long favoriteId);
}
