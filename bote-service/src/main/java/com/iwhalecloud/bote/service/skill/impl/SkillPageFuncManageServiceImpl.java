package com.iwhalecloud.bote.service.skill.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.dto.skill.SimpleSkillPageFuncDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageFuncDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.mapper.skill.QuerySkillMapper;
import com.iwhalecloud.bote.mapper.skill.SkillPageFuncManageMapper;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bote.service.skill.ISkillPageFuncManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 技能：页面函数 服务实现
 *
 * @author auto
 * @since 2024-09-15
 */
@Service
@RequiredArgsConstructor
public class SkillPageFuncManageServiceImpl implements ISkillPageFuncManageService {

  private final SkillPageFuncManageMapper pageFuncMapper;
  private final QuerySkillMapper querySkillMapper;
  private final ICatalogManageService catalogManageService;
  private final IResourceElementService resourceElementService;

  @Override
  @Transactional
  public ResultVO<SkillPageFuncDTO> saveSkillPageFunc(SkillPageFuncDTO pageFunc) {
    if (pageFunc.getCopyPageFuncId() != null) {
      return copySkillPageFunc(pageFunc);
    }
    if (pageFuncMapper.existsSkillPageFuncCode(pageFunc)) {
      return BaseErrorConstant.CHECK_CODE.toResult(pageFunc.getFuncCode());
    }
    pageFunc.setStatusCd(BaseConsts.STATUS_CD_VALID);
    SkillPageFuncDTO old = pageFunc.getPageFuncId() == null ? null : findSkillPageFunc(pageFunc.getTenantId(), pageFunc.getPageFuncId());
    DataDifference<SkillPageFuncDTO> difference = DataDifferenceStarter.computeSaveAndLog(old, pageFunc, false, pageFunc.getTenantId(),
      OperClassEnum.SKILL_PAGE_FUNC);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  private ResultVO<SkillPageFuncDTO> copySkillPageFunc(SkillPageFuncDTO pageFunc) {
    SkillPageFuncDTO oldPageFunc = pageFuncMapper.getSkillPageFunc(pageFunc.getTenantId(), pageFunc.getCopyPageFuncId());
    if (oldPageFunc == null) {
      return BaseErrorConstant.BOT_SKILL_PAGE_FUNC_NOT_EXISTS.toResult(pageFunc.getCopyPageFuncId());
    }
    if (pageFuncMapper.existsSkillPageFuncCode(pageFunc)) {
      return BaseErrorConstant.CHECK_CODE.toResult(pageFunc.getFuncCode());
    }
    DataDifferenceStarter.computeSaveAndLog(null, pageFunc, false, pageFunc.getTenantId(), OperClassEnum.SKILL_PAGE_FUNC);
    return ResultVO.success(pageFunc);
  }

  @Override
  public SkillPageFuncDTO findSkillPageFunc(Long tenantId, Long funcId) {
    return pageFuncMapper.getSkillPageFunc(tenantId, funcId);
  }

  @Override
  public List<SimpleSkillPageFuncDTO> querySkillPageFuncList(SkillQueryParams params) {
    return querySkillMapper.selectSkillPageFuncList(params);
  }

  @Override
  public PageInfo<SkillPageFuncDTO> querySkillPageFuncPage(SkillQueryParams params) {
    if (!BaseConsts.FALSE.equals(params.getConfigFlag())) {
      params.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(params.getTenantId(), params.getCatalogItemId(), CatalogConsts.TYPE_SKILL));
    }
    // noinspection resource
    return pageFuncMapper.selectSkillPageFuncPage(params, params.buildRowBounds()).toPageInfo();
  }

  @Override
  public PageInfo<SimpleSkillPageFuncDTO> querySimpleSkillPageFuncPage(SkillQueryParams params) {
    params.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(params.getTenantId(), params.getCatalogItemId(), CatalogConsts.TYPE_SKILL));
    // noinspection resource
    return querySkillMapper.selectSkillPageFuncPage(params, params.buildRowBounds()).toPageInfo();
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteSkillPageFunc(Long tenantId, Long funcId) {
    SkillPageFuncDTO botSkillPageFunction = pageFuncMapper.getSkillPageFunc(tenantId, funcId);
    if (botSkillPageFunction == null) {
      return BaseErrorConstant.BOT_SKILL_PAGE_FUNC_NOT_EXISTS.toResult();
    }
    if (resourceElementService.existsRelatedResource(tenantId, funcId, DataSyncCodeEnum.SKILL_PAGE_FUNC.getCode())) {
      return ResultVO.fail("页面函数已存在关联配置数据，不允许删除");
    }
    LoginInfo loginInfo = SessionUtil.getLoginInfo();
    pageFuncMapper.deleteSkillPageFunc(tenantId, funcId, loginInfo.getUserId());
    ResourceElementFactory.get(OperClassEnum.SKILL_PAGE_FUNC.name()).clear(tenantId, funcId);
    return ResultVO.success();
  }
}
