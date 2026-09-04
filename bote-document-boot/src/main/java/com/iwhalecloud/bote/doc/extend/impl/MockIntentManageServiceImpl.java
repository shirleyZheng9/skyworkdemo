package com.iwhalecloud.bote.doc.extend.impl;

import com.iwhalecloud.bote.dto.intent.IntentStrategyDTO;
import com.iwhalecloud.bote.service.intent.IIntentManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import org.springframework.stereotype.Component;

/**
 * @author chen.linfa
 * @since 2025-11-19
 */
@Component
public class MockIntentManageServiceImpl implements IIntentManageService {
  @Override
  public IntentStrategyDTO findStrategy(Long tenantId) {
    return new IntentStrategyDTO();
  }

  @Override
  public ResultVO<IntentStrategyDTO> saveStrategy(IntentStrategyDTO strategy) {
    return null;
  }
}
