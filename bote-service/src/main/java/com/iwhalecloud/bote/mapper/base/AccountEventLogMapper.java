package com.iwhalecloud.bote.mapper.base;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.base.AccountEventLog;
import com.iwhalecloud.bote.dto.base.query.AccountEventLogQueryParams;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 账号事件日志 Mapper
 *
 * @author tingyun.wang
 * @since 2025-07-15
 */
@Mapper
public interface AccountEventLogMapper {

  /**
   * 新增账号事件日志
   *
   * @param log 账号事件日志
   * @return 结果
   */
  int insertLog(@Param("log") AccountEventLog log);

  /**
   * 获取账号事件日志列表（分页）
   *
   * @param param 查询条件
   * @param rowBounds 分页参数
   * @return 账号事件日志列表
   */
  Page<AccountEventLog> selectLogPage(@Param("param") AccountEventLogQueryParams param, RowBounds rowBounds);

}
