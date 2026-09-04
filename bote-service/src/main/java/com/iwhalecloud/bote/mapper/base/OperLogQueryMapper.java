package com.iwhalecloud.bote.mapper.base;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.base.OperLogDTO;
import com.iwhalecloud.bote.dto.base.OperLogDetailDTO;
import com.iwhalecloud.bote.dto.base.query.OperLogQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 操作日志查询 Mapper
 *
 * @author auto
 * @since 2024-10-26
 */
public interface OperLogQueryMapper {

  /**
   * 查询操作日志
   *
   * @param params 查询参数
   * @param rowBounds 分页参数
   * @return 操作日志分页数据
   */
  Page<OperLogDTO> selectOperLogPage(@Param("query") OperLogQueryParams params, RowBounds rowBounds);

  /**
   * 查询操作日志详情
   *
   * @param logId 日志 ID
   * @return 操作日志详情
   */
  List<OperLogDetailDTO> selectOperLogDetail(@Param("logId") Long logId);

  /**
   * 根据修改记录 ID 查询版本数据
   *
   * @param logId 日志 ID
   * @param tenantId 租户ID
   * @return 结果
   */
  String getObjDescByLogId(@Param("logId") Long logId, @Param("tenantId") Long tenantId);

  /**
   * 根据日志 ID 查询操作日志详情
   *
   * @param logId 日志 ID
   * @return 操作日志详情
   */
  OperLogDTO getOperLogDetail(@Param("logId") Long logId, @Param("tenantId") Long tenantId);
}
