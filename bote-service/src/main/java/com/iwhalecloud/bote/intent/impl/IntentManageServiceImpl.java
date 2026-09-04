package com.iwhalecloud.bote.intent.impl;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.intent.IntentStrategyDTO;
import com.iwhalecloud.bote.service.intent.IIntentManageService;
import com.iwhalecloud.bote.mapper.intent.IntentManageMapper;
import com.iwhalecloud.bote.mapper.model.ModelFinetuneManageMapper;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 意图服务实现
 *
 * @author auto
 * @since 2025-02-17
 */
@Service
@RequiredArgsConstructor
public class IntentManageServiceImpl implements IIntentManageService {

  private final IntentManageMapper intentManageMapper;
  private final ModelFinetuneManageMapper finetuneManageMapper;
  private final Environment environment;

  @Override
  public IntentStrategyDTO findStrategy(Long tenantId) {
    IntentStrategyDTO strategy = intentManageMapper.getStrategy(tenantId);
    if (strategy == null) {
      strategy = new IntentStrategyDTO();
      strategy.setMultiBotEnabled(BaseConsts.FALSE);
      strategy.setEmbeddingEnabled(BaseConsts.FALSE);
      strategy.setSlmEnabled(BaseConsts.FALSE);
    }
    // 补充小模型 API
    strategy.setSlmChatApi(environment.getProperty("slm.chat.apiUrl"));

    // 补充启用的小模型
    strategy.setFinetune(finetuneManageMapper.getOnlineFinetune(tenantId));
    return strategy;
  }

  @Override
  @Transactional
  public ResultVO<IntentStrategyDTO> saveStrategy(IntentStrategyDTO strategy) {
    if (strategy.isEmbedding()) {
      Assert.notNull(strategy.getEmbeddingModelId(), "Embedding 模型不能为空");
    }
    if (strategy.isSlm()) {
      String slmChatApi = environment.getProperty("slm.chat.apiUrl");
      Assert.hasText(slmChatApi, "平台未预置小模型推理 API，暂时不支持开启小模型推理策略");
    }

    Long userId = SessionUtil.getLoginInfo().getUserId();
    strategy.setStatusCd(BaseConsts.STATUS_CD_VALID);
    strategy.setCreatorId(userId);
    strategy.setUpdatorId(userId);
    if (strategy.getId() == null) {
      strategy.setId(Sequences.INTENTION_STRATEGY_ID.next());
      intentManageMapper.insertStrategy(strategy);
    }
    else {
      intentManageMapper.updateStrategy(strategy);
    }
    return ResultVO.success(strategy);
  }
}
