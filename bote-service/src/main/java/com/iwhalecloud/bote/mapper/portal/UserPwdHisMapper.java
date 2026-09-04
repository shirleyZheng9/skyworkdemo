package com.iwhalecloud.bote.mapper.portal;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.portal.UserPwdHisDTO;
import com.iwhalecloud.bote.entity.portal.UserPwdHisEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 用户密码历史 mapper
 *
 * @author wangtinyun
 * @since 2025-10-17
 */
public interface UserPwdHisMapper {

  /**
   * 新增用户密码历史
   *
   * @param entity 密码历史实体
   * @return 新增结果
   */
  int insertUserPwdHis(@Param("dto") UserPwdHisEntity entity);

  /**
   * 根据用户ID分页查询用户密码历史列表
   *
   * @param userId 用户ID
   * @return 用户密码历史列表
   */
  Page<UserPwdHisDTO> selectPageByUserId(@Param("userId") Long userId, RowBounds rowBounds);

}
