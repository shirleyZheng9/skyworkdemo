package com.iwhalecloud.bote.service.agent.impl;

import com.iwhalecloud.bote.cache.GeneraAgentIdCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.dto.agent.AiModelDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import com.iwhalecloud.bote.dto.model.SimpleLargeModelDTO;
import com.iwhalecloud.bote.mapper.agent.AiModelManageMapper;
import com.iwhalecloud.bote.mapper.model.LargeModelManageMapper;
import com.iwhalecloud.bote.service.agent.IAiModelManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 新增表记录启用模型管理服务实现
 *
 * @author linmengfan
 * @since 2026-03-05
 */
@Service
@RequiredArgsConstructor
public class AiModelManageServiceImpl implements IAiModelManageService {

  private final AiModelManageMapper aiModelManageMapper;
  private final GeneraAgentIdCache generaAgentIdCache;
  private final LargeModelManageMapper largeModelManageMapper;

  @Override
  @Transactional
  public ResultVO<AiModelDTO> saveAiModel(AiModelDTO model) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    AiModelDTO old = aiModelManageMapper.getAiModel(model.getSpaceId(), model.getBotId(), userId, model.getModelType());
    if (old != null) {
      old.setModelId(model.getModelId());
      old.setUpdatorId(userId);
      aiModelManageMapper.updateAiModel(old);
      return ResultVO.success(old);
    }
    model.setId(Sequences.AI_MODEL_ID.next());
    model.setCreatorId(userId);
    model.setStatusCd(CommonConsts.STATUS_CD_VALID);
    aiModelManageMapper.insertAiModel(model);
    return ResultVO.success(model);
  }

  @Override
  public List<AiModelDTO> queryAiModelList(AiQueryParams queryParams) {
    Long userId = generaAgentIdCache.getBotOnwerUserId(queryParams.getSpaceId(), queryParams.getBotId(), SessionUtil.getLoginInfo().getUserId());
    queryParams.setUserId(userId);
    List<AiModelDTO> aiModelList = aiModelManageMapper.selectAiModelList(queryParams);
    // 如果没有启用的个人模型，则返回已启用的空间/平台模型（仅返回给前端展示用）
    if (aiModelList.isEmpty()) {
      SimpleLargeModelDTO enabledModel = null;
      // 先查询空间已启用的模型: 存量代码，空间级模型，会虚拟出一个 tenantId，而不是 spaceId 当做 tenantId
      Long modelTenantId = TenantIdUtil.getSpaceTenantId(queryParams.getSpaceId());
      if (modelTenantId != null) {
        enabledModel = largeModelManageMapper.selectEnabledLargeModel(modelTenantId);
      }
      if (enabledModel == null) {
        // 没有空间已启用模型，则返回平台已启用的模型
        enabledModel = largeModelManageMapper.selectEnabledLargeModel(BaseConsts.PLATFORM_TENANT_ID);
      }
      if (enabledModel != null) {
        AiModelDTO aiModel = new AiModelDTO();
        BeanUtils.copyProperties(enabledModel, aiModel);
        aiModelList.add(aiModel);
      }
    }
    return aiModelList;
  }

}
