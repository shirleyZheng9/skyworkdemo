package com.iwhalecloud.bote.service.base;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.base.OperLogDTO;
import com.iwhalecloud.bote.dto.base.OperLogDetailDTO;
import com.iwhalecloud.bote.dto.base.query.OperLogQueryParams;
import java.util.List;

/**
 * 操作日志服务
 *
 * @author auto
 * @since 2024-10-26
 */
public interface IOperLogService {

  /**
   * 查询操作日志列表（分页）
   *
   * @param params 查询条件
   * @return 操作日志（分页）
   */
  PageInfo<OperLogDTO> queryOperLogPage(OperLogQueryParams params);

  /**
   * 根据操作日志 ID 查询日志详情列表
   *
   * @param logId 日志 ID
   * @return 日志详情列表
   */
  List<OperLogDetailDTO> queryOperLogDetail(Long logId);
}
