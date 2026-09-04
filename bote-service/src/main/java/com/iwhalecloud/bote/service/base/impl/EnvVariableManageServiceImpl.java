package com.iwhalecloud.bote.service.base.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.util.EnvUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.EnvVariableDTO;
import com.iwhalecloud.bote.dto.base.EnvVariableValDTO;
import com.iwhalecloud.bote.dto.base.query.EnvVariableQueryParams;
import com.iwhalecloud.bote.mapper.base.EnvVariableManageMapper;
import com.iwhalecloud.bote.service.base.IEnvVariableManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 环境变量服务实现类
 *
 * @author qian.sisheng
 * @since 2025-11-03
 */
@Service
@RequiredArgsConstructor
public class EnvVariableManageServiceImpl implements IEnvVariableManageService {

  private final EnvVariableManageMapper envVariableManageMapper;

  @Override
  @Transactional
  public ResultVO<EnvVariableDTO> saveEnvVariable(EnvVariableDTO variable) {
    if (envVariableManageMapper.existsEnvVariableCode(variable)) {
      return BaseErrorConstant.CHECK_CODE.toResult(variable.getVariableCode());
    }
    variable.setStatusCd(BaseConsts.STATUS_CD_VALID);
    EnvVariableDTO old = variable.getVariableId() == null ? null : findEnvVariable(variable.getVariableId(), variable.getTenantId());
    DataDifference<EnvVariableDTO> difference = DataDifferenceStarter.computeSave(old, variable, true, variable.getTenantId());
    // 检测本次变更的数据
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  public List<EnvVariableDTO> queryEnvVariableList(EnvVariableQueryParams param) {
    return envVariableManageMapper.selectEnvVariableList(param);
  }

  @Override
  public PageInfo<EnvVariableDTO> queryEnvVariablePage(EnvVariableQueryParams param) {
    PageInfo<EnvVariableDTO> pageInfo = envVariableManageMapper.selectEnvVariablePage(param, param.buildRowBounds()).toPageInfo();
    buildEnvVariable(param.getTenantId(), pageInfo.getList());
    return pageInfo;
  }

  @Override
  public List<EnvVariableDTO> getEnvVariableParamList(Long tenantId) {
    List<EnvVariableDTO> envVariableList = envVariableManageMapper.selectListByTenantId(tenantId);
    if (CollectionUtils.isNotEmpty(envVariableList)) {
      // 设置变量参数格式：$.envVar.{variableCode}
      envVariableList.forEach(envVar -> envVar.setVariableCode("$.envVar." + envVar.getVariableCode()));
    }
    return envVariableList;
  }

  /**
   * 填充环境变量值
   *
   * @param tenantId 租户ID
   * @param envVariableList 环境变量列表
   */
  private void buildEnvVariable(Long tenantId, List<EnvVariableDTO> envVariableList) {
    if (CollectionUtils.isEmpty(envVariableList)) {
      return;
    }
    List<Long> variableIds = envVariableList.stream().map(EnvVariableDTO::getVariableId).toList();
    Map<Long, List<EnvVariableValDTO>> variableVals = envVariableManageMapper.selectVariableValByVariableIds(variableIds, tenantId).stream()
      .collect(Collectors.groupingBy(EnvVariableValDTO::getVariableId));
    variableVals.forEach((k, v) -> {
      EnvVariableDTO envVariable = IterableUtils.find(envVariableList, p -> Objects.equals(k, p.getVariableId()));
      envVariable.setEnvVariableVals(v);
    });
  }

  @Override
  @Nullable
  public EnvVariableDTO findEnvVariable(Long variableId, Long tenantId) {
    EnvVariableDTO envVariable = envVariableManageMapper.findEnvVariableById(variableId, tenantId);
    if (envVariable != null) {
      envVariable.setEnvVariableVals(envVariableManageMapper.selectVariableValByVariableId(variableId, tenantId));
    }
    return envVariable;
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteEnvVariableById(Long variableId, Long tenantId) {
    EnvVariableDTO variable = envVariableManageMapper.findEnvVariableById(variableId, tenantId);
    if (variable == null) {
      throw new BssException("环境变量不存在，variableId=: " + variableId);
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    envVariableManageMapper.deleteEnvVariableById(userId, variableId, tenantId);
    envVariableManageMapper.deleteEnvVariableValById(userId, variableId, tenantId);
    return ResultVO.success();
  }

  @Override
  @Nullable
  public EnvVariableValDTO getEnvVariableValueByCode(String variableCode, Long tenantId) {
    String envCode = EnvUtil.getEnvCode();
    return envVariableManageMapper.selectEnvVariableValByCode(tenantId, variableCode, envCode);
  }

}
