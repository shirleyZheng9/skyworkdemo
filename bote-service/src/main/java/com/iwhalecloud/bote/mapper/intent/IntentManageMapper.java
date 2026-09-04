package com.iwhalecloud.bote.mapper.intent;

import com.iwhalecloud.bote.dto.intent.IntentStrategyDTO;
import org.apache.ibatis.annotations.Param;

/**
 * 意图识别管理
 *
 * @author auto
 * @since 2024-12-18
 */
public interface IntentManageMapper {

  /**
   * 根据租户 ID 获取策略
   *
   * @param tenantId 租户 ID
   * @return 策略
   */
  IntentStrategyDTO getStrategy(@Param("tenantId") Long tenantId);

  /**
   * 新增策略
   *
   * @param strategy 策略
   * @return 结果
   */
  int insertStrategy(@Param("dto") IntentStrategyDTO strategy);

  /**
   * 更新策略
   *
   * @param strategy 策略
   * @return 结果
   */
  int updateStrategy(@Param("dto") IntentStrategyDTO strategy);

}
