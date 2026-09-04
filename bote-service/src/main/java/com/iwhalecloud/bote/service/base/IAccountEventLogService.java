package com.iwhalecloud.bote.service.base;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.AccountEventTypeEnum;
import com.iwhalecloud.bote.dto.base.AccountEventLog;
import com.iwhalecloud.bote.dto.base.query.AccountEventLogQueryParams;

/**
 * 账号事件日志服务
 *
 * @author tingyun.wang
 * @since 2025-07-15
 */
public interface IAccountEventLogService {

  /**
   * 添加用户账号事件日志
   * @param eventType 事件类型
   * @param eventContent 事件内容信息
   */
  void addAccountEventLog(AccountEventTypeEnum eventType, String eventContent);

  /**
   * 查询账号事件日志列表（分页）
   *
   * @param params 查询条件
   * @return 账号事件日志（分页）
   */
  PageInfo<AccountEventLog> queryLogPage(AccountEventLogQueryParams params);

}
