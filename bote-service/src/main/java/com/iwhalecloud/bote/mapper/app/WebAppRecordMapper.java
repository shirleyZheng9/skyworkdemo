package com.iwhalecloud.bote.mapper.app;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.app.WebAppRecordDTO;
import com.iwhalecloud.bote.dto.app.query.WebAppRecordQueryParams;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * 网页应用访问记录 Mapper
 *
 * @author wang.tingyun
 * @since 2025-09-22
 */
public interface WebAppRecordMapper {

  /**
   * 新增网页应用访问记录
   *
   * @param dto 访问记录实体
   * @return 新增结果
   */
  int insertRecord(@Param("dto") WebAppRecordDTO dto);

  /**
   * 更新访问记录时间
   *
   * @param dto 访问记录实体
   * @return 更新结果
   */
  int updateRecordTime(@Param("dto") WebAppRecordDTO dto);

  /**
   * 根据应用访问记录ID删除记录
   *
   * @param recordId 应用访问ID
   * @param userId 用户ID
   * @return 删除结果
   */
  int deleteByRecordId(@Param("recordId") Long recordId, @Param("userId") Long userId);

  /**
   * 查询用户应用访问记录列表
   *
   * @param params 查询参数
   * @return 授权列表
   */
  List<WebAppRecordDTO> selectList(@Param("params") WebAppRecordQueryParams params);

  /**
   * 分页查询用户应用访问记录
   *
   * @param params 查询参数
   * @return 授权列表
   */
  Page<WebAppRecordDTO> selectPage(@Param("params") WebAppRecordQueryParams params, RowBounds rowBounds);

  /**
   * 检查应用是否已在访问记录中
   *
   * @param webAppId 应用ID
   * @param userId 用户ID
   * @return 是否存在
   */
  boolean checkExists(@Param("webAppId") Long webAppId, @Param("userId") Long userId);


}