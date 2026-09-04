package com.iwhalecloud.bote.service.intent;

import com.iwhalecloud.bote.dto.intent.IntentStrategyDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * 意图管理服务
 *
 * @author auto
 * @since 2025-02-17
 */
public interface IIntentManageService {

  /**
   * 查询策略
   *
   * @param tenantId 租户 ID
   * @return 意图识别策略
   */
  IntentStrategyDTO findStrategy(Long tenantId);

  /**
   * 保存策略
   *
   * @param strategy 策略
   * @return 结果
   */
  ResultVO<IntentStrategyDTO> saveStrategy(IntentStrategyDTO strategy);
}
