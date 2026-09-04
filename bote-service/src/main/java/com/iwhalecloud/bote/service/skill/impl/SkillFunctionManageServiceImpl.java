package com.iwhalecloud.bote.service.skill.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.util.GroovyUtil;
import com.iwhalecloud.bote.common.util.ParamConverterUtil;
import com.iwhalecloud.bote.common.util.PythonUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.skill.FunctionTestParams;
import com.iwhalecloud.bote.dto.skill.SimpleSkillFunctionDTO;
import com.iwhalecloud.bote.dto.skill.SkillFunctionDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.mapper.skill.QuerySkillMapper;
import com.iwhalecloud.bote.mapper.skill.SkillFunctionManageMapper;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bote.service.skill.ISkillFunctionManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 技能：服务函数 服务实现
 *
 * @author auto
 * @since 2024-09-15
 */
@Service
@RequiredArgsConstructor
public class SkillFunctionManageServiceImpl implements ISkillFunctionManageService {

  private final SkillFunctionManageMapper functionMapper;
  private final QuerySkillMapper querySkillMapper;
  private final ICatalogManageService catalogManageService;
  private final IResourceElementService resourceElementService;

  @Override
  @Transactional
  public ResultVO<SkillFunctionDTO> saveSkillFunction(SkillFunctionDTO function) {
    if (function.getCopyFuncId() != null) {
      return copySkillFunction(function);
    }
    if (functionMapper.existsSkillFunctionCode(function)) {
      return BaseErrorConstant.CHECK_CODE.toResult(function.getFuncCode());
    }
    function.setStatusCd(BaseConsts.STATUS_CD_VALID);
    SkillFunctionDTO old = function.getFuncId() == null ? null : findSkillFunction(function.getTenantId(), function.getFuncId());
    DataDifference<SkillFunctionDTO> difference = DataDifferenceStarter.computeSaveAndLog(old, function, false, function.getTenantId(),
      OperClassEnum.SKILL_FUNCTION);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  private ResultVO<SkillFunctionDTO> copySkillFunction(SkillFunctionDTO function) {
    SkillFunctionDTO oldFunction = functionMapper.getSkillFunction(function.getTenantId(), function.getCopyFuncId());
    if (oldFunction == null) {
      return BaseErrorConstant.BOT_SKILL_FUNCTION_NOT_EXISTS.toResult(function.getCopyFuncId());
    }
    if (functionMapper.existsSkillFunctionCode(function)) {
      return BaseErrorConstant.CHECK_CODE.toResult(function.getFuncCode());
    }
    DataDifferenceStarter.computeSaveAndLog(null, function, false, function.getTenantId(), OperClassEnum.SKILL_FUNCTION);
    return ResultVO.success(function);
  }

  @Override
  public SkillFunctionDTO findSkillFunction(Long tenantId, Long funcId) {
    return functionMapper.getSkillFunction(tenantId, funcId);
  }

  @Override
  public List<SimpleSkillFunctionDTO> querySkillFunctionList(SkillQueryParams params) {
    return querySkillMapper.selectSkillFunctionList(params);
  }

  @Override
  public PageInfo<SkillFunctionDTO> querySkillFunctionPage(SkillQueryParams params) {
    if (!BaseConsts.FALSE.equals(params.getConfigFlag())) {
      params.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(params.getTenantId(), params.getCatalogItemId(), CatalogConsts.TYPE_SKILL));
    }
    // noinspection resource
    return functionMapper.selectSkillFunctionPage(params, params.buildRowBounds()).toPageInfo();
  }

  @Override
  public PageInfo<SimpleSkillFunctionDTO> querySimpleSkillFunctionPage(SkillQueryParams params) {
    params.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(params.getTenantId(), params.getCatalogItemId(), CatalogConsts.TYPE_SKILL));
    // noinspection resource
    return querySkillMapper.selectSkillFunctionPage(params, params.buildRowBounds()).toPageInfo();
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteSkillFunction(Long tenantId, Long funcId) {
    if (resourceElementService.existsRelatedResource(tenantId, funcId, DataSyncCodeEnum.SKILL_FUNCTION.getCode())) {
      return ResultVO.fail("服务函数已存在关联配置数据，不允许删除");
    }
    functionMapper.deleteSkillFunction(tenantId, funcId, SessionUtil.getLoginInfo().getUserId());
    ResourceElementFactory.get(OperClassEnum.SKILL_FUNCTION.name()).clear(tenantId, funcId);
    return ResultVO.success();
  }

  @Override
  public ResultVO<Object> test(FunctionTestParams params) {
    if (MapUtils.isNotEmpty(params.getParams()) && StringUtils.isEmpty(params.getReqJson())) {
      return ResultVO.fail("服务配置参数列表不能为空");
    }
    ParameterSpec inputSpec = StringUtils.isEmpty(params.getReqJson()) ? null : JsonUtil.parseJsonRequired(params.getReqJson(), ParameterSpec.class);
    ParameterSpec outputSpec = StringUtils.isEmpty(params.getRepJson()) ? null : JsonUtil.parseJsonRequired(params.getRepJson(), ParameterSpec.class);
    Map<String, Object> parsedParams = ParamConverterUtil.convertRoot(inputSpec, params.getParams());
    Object[] scriptParams = ParamConverterUtil.convertMapToArray(inputSpec, parsedParams);
    Object result;
    if (BaseConsts.SCRIPT_TYPE_GROOVY.equals(params.getFuncType())) {
      result = GroovyUtil.invoke(params.getScriptJson(), scriptParams);
    }
    else if (BaseConsts.SCRIPT_TYPE_PYTHON3.equals(params.getFuncType())) {
      result = PythonUtil.invoke(params.getScriptJson(), params.getPyPackageList(), scriptParams);
    }
    else {
      throw new BssException("未知的脚本类型: " + params.getFuncType());
    }
    if (result != null && outputSpec != null) {
      result = ParamConverterUtil.convert("", outputSpec, result);
    }
    return ResultVO.success(result);
  }

}
