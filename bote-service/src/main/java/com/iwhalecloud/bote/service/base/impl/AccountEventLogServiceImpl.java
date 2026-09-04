package com.iwhalecloud.bote.service.base.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bassc.basiccenter.util.ext.HttpUtils;
import com.iwhalecloud.bote.common.consts.AccountEventTypeEnum;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.dto.base.AccountEventLog;
import com.iwhalecloud.bote.dto.base.query.AccountEventLogQueryParams;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.mapper.base.AccountEventLogMapper;
import com.iwhalecloud.bote.service.base.IAccountEventLogService;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import com.iwhalecloud.bss.litchi.util.IPUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 账号事件日志服务实现
 *
 * @author tingyun.wang
 * @since 2025-07-15
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class AccountEventLogServiceImpl implements IAccountEventLogService {

  private static final Logger logger = LoggerFactory.getLogger(AccountEventLogServiceImpl.class);

  private final AccountEventLogMapper accountEventLogMapper;

  @Override
  public void addAccountEventLog(AccountEventTypeEnum eventType, String eventContent) {
    try {
      AccountEventLog eventLog = new AccountEventLog();
      // 事件基本信息
      eventLog.setLogId(IDUtils.nextId());
      eventLog.setEventType(eventType.getEventType());
      eventLog.setEventCode(eventType.getEventCode());
      eventLog.setEventDesc(eventType.getEventDesc());
      eventLog.setEventContent(eventContent);
      eventLog.setOperIp(IPUtil.getClientIP(HttpUtils.getRequest()));
      eventLog.setOperDate(new Date());
      // 操作人信息
      LoginInfo loginInfo = SessionUtil.getLoginInfo();
      eventLog.setOperatorId(loginInfo.getUserId());
      eventLog.setOperatorName(loginInfo.getUserName());
      eventLog.setTenantId(TenantIdUtil.getTenantIdOptional());
      TransactionUtil.executeAsync(() -> accountEventLogMapper.insertLog(eventLog));
    }
    catch (Exception e) {
      logger.error("Failed to record account event log. err = {}", e.getMessage(), e);
    }
  }

  @Override
  public PageInfo<AccountEventLog> queryLogPage(AccountEventLogQueryParams params) {
    RowBounds rowBounds = params.buildRowBounds();
    //noinspection resource
    return accountEventLogMapper.selectLogPage(params, rowBounds).toPageInfo();
  }

}
