package com.iwhalecloud.bote.intent;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.intent.IntentLogDTO;
import com.iwhalecloud.bote.dto.intent.query.IntentQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * 意图识别日志管理服务
 *
 * @author auto
 * @since 2024-12-18
 */
public interface IIntentLogManageService {
  /**
   * 查询意图识别日志列表（分页）
   *
   * @param queryParams 查询条件
   * @return 意图识别日志分页列表
   */
  PageInfo<IntentLogDTO> queryIntentLogPage(IntentQueryParams queryParams);

  /**
   * 标记意图日志
   *
   * @param log 意图日志
   * @return 结果
   */
  ResultVO<Void> markIntentLog(IntentLogDTO log);

  /**
   * 取消标记
   *
   * @param tenantId 租户 ID
   * @param logId 意图日志ID
   * @return 结果
   */
  ResultVO<Void> cancelIntentLog(Long tenantId, Long logId);
}
