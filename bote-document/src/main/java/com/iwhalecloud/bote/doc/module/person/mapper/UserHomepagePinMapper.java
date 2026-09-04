package com.iwhalecloud.bote.doc.module.person.mapper;

import com.iwhalecloud.bote.doc.module.person.entity.UserHomepagePinEntity;
import org.apache.ibatis.annotations.Param;

/**
 * 用户首页置顶相关数据库操作
 *
 * @author yangran
 * @since 2025-08-13
 */
public interface UserHomepagePinMapper {

  /**
   * 根据主键查询
   */
  UserHomepagePinEntity selectByPinId(@Param("pinId") Long pinId);

  /**
   * 插入记录
   */
  int insert(@Param("pin") UserHomepagePinEntity pin);

  /**
   * 更新记录
   */
  int update(@Param("pin") UserHomepagePinEntity pin);

  /**
   * 逻辑删除
   */
  int deleteByPinId(@Param("pinId") Long pinId);
}
