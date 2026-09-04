package com.iwhalecloud.bote.service.agent.impl;

import com.iwhalecloud.bote.cache.GeneraAgentIdCache;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.agent.AddAiEnvVariableDTO;
import com.iwhalecloud.bote.dto.agent.AiEnvVariableDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import com.iwhalecloud.bote.mapper.agent.AiEnvVariableManageMapper;
import com.iwhalecloud.bote.service.agent.IAiEnvVariableManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.common.license.core.util.CollectionUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户级的环境变量管理服务实现
 *
 * @author linmengfan
 * @since 2026-03-05
 */
@Service
@RequiredArgsConstructor
public class AiEnvVariableManageServiceImpl implements IAiEnvVariableManageService {

  private final AiEnvVariableManageMapper aiEnvVariableManageMapper;
  private final GeneraAgentIdCache generaAgentIdCache;

  @Override
  @Transactional
  public ResultVO<List<AiEnvVariableDTO>> saveAiEnvVariable(AddAiEnvVariableDTO dto) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    AiQueryParams params = new AiQueryParams();
    params.setSpaceId(dto.getSpaceId());
    params.setBotId(dto.getBotId());
    if (CollectionUtils.isNotEmpty(dto.getBtAiEnvVariableDTOList())) {
      // 校验环境编码的唯一性
      ResultVO<Void> result = check(dto.getBtAiEnvVariableDTOList());
      if (!result.isSuccess()) {
        return ResultVO.fail(result.getResultMsg());
      }

      List<AiEnvVariableDTO> adds = dto.getBtAiEnvVariableDTOList().stream().filter(p -> p.getId() == null).toList();
      if (CollectionUtils.isNotEmpty(adds)) {
        for (AiEnvVariableDTO variable : adds) {
          variable.setId(Sequences.AI_ENV_VARIABLE_ID.next());
          variable.setSpaceId(dto.getSpaceId());
          variable.setBotId(dto.getBotId());
          variable.setStatusCd(CommonConsts.STATUS_CD_VALID);
          variable.setCreatorId(userId);
        }
        aiEnvVariableManageMapper.batchInsertAiEnvVariable(adds);
      }

      List<AiEnvVariableDTO> mods = dto.getBtAiEnvVariableDTOList().stream().filter(p -> p.getId() != null).toList();
      for (AiEnvVariableDTO variable : CollectionUtils.emptyIfNull(mods)) {
        variable.setUpdatorId(userId);
        aiEnvVariableManageMapper.updateAiEnvVariable(variable);
      }
    }
    if (!CollectionUtil.isEmpty(dto.getDelIds())) {
      aiEnvVariableManageMapper.deleteAiEnvVariableByIds(dto.getDelIds(), userId);
    }
    return ResultVO.success(queryAiEnvVariableList(params));
  }

  @Override
  public List<AiEnvVariableDTO> queryAiEnvVariableList(AiQueryParams queryParams) {
    Long userId = generaAgentIdCache.getBotOnwerUserId(queryParams.getSpaceId(), queryParams.getBotId(), SessionUtil.getLoginInfo().getUserId());
    queryParams.setUserId(userId);
    return aiEnvVariableManageMapper.selectAiEnvVariableList(queryParams);
  }

  private ResultVO<Void> check(List<AiEnvVariableDTO> variables) {
    Map<String, List<AiEnvVariableDTO>> map = variables.stream().collect(Collectors.groupingBy(AiEnvVariableDTO::getVariableCode));
    List<String> illegalCodes = new ArrayList<>();
    for (Entry<String, List<AiEnvVariableDTO>> entry : map.entrySet()) {
      if (entry.getValue().size() > 1) {
        illegalCodes.add(entry.getKey());
      }
    }
    if (CollectionUtils.isNotEmpty(illegalCodes)) {
      return ResultVO.fail("存在重复的 Key." + String.join(",", illegalCodes));
    }
    return ResultVO.success();
  }

}
