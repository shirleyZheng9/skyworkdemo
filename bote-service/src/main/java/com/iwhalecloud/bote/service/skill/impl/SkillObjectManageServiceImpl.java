package com.iwhalecloud.bote.service.skill.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.skill.SkillObjectDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.mapper.skill.SkillObjectManageMapper;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bote.service.skill.ISkillObjectManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 技能：对象 服务实现
 *
 * @author auto
 * @since 2024-09-16
 */
@Service
@RequiredArgsConstructor
public class SkillObjectManageServiceImpl implements ISkillObjectManageService {

  private final SkillObjectManageMapper objectManageMapper;
  private final ICatalogManageService catalogManageService;
  private final IResourceElementService resourceElementService;

  @Override
  @Transactional
  public ResultVO<SkillObjectDTO> saveSkillObject(SkillObjectDTO object) {
    if (object.getCopyBusiObjectId() != null) {
      return copySkillObject(object);
    }
    if (objectManageMapper.existsSkillObjectCode(object)) {
      return BaseErrorConstant.CHECK_CODE.toResult(object.getBusiObjectCode());
    }
    object.setStatusCd(BaseConsts.STATUS_CD_VALID);
    SkillObjectDTO old = object.getBusiObjectId() == null ? null : findSkillObject(object.getTenantId(), object.getBusiObjectId());
    DataDifference<SkillObjectDTO> difference = DataDifferenceStarter.computeSaveAndLog(old, object, false, object.getTenantId(),
      OperClassEnum.SKILL_OBJECT);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  private ResultVO<SkillObjectDTO> copySkillObject(SkillObjectDTO object) {
    SkillObjectDTO oldObject = objectManageMapper.findSkillObject(object.getTenantId(), object.getCopyBusiObjectId());
    if (oldObject == null) {
      return BaseErrorConstant.BOT_SKILL_OBJECT_NOT_EXISTS.toResult(object.getCopyBusiObjectId());
    }
    if (objectManageMapper.existsSkillObjectCode(object)) {
      return BaseErrorConstant.CHECK_CODE.toResult(object.getBusiObjectCode());
    }
    DataDifferenceStarter.computeSaveAndLog(null, object, false, object.getTenantId(), OperClassEnum.SKILL_OBJECT);
    return ResultVO.success(object);
  }

  @Override
  public SkillObjectDTO findSkillObject(Long tenantId, Long busiObjectId) {
    return objectManageMapper.findSkillObject(tenantId, busiObjectId);
  }

  @Override
  public List<SkillObjectDTO> querySkillObjectList(SkillQueryParams params) {
    return objectManageMapper.selectSkillObjectList(params);
  }

  @Override
  public PageInfo<SkillObjectDTO> querySkillObjectPage(SkillQueryParams params) {
    if (!BaseConsts.FALSE.equals(params.getConfigFlag())) {
      params.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(params.getTenantId(), params.getCatalogItemId(), CatalogConsts.TYPE_SKILL));
    }
    // noinspection resource
    return objectManageMapper.selectSkillObjectPage(params, params.buildRowBounds()).toPageInfo();
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteSkillObject(Long tenantId, Long busiObjectId) {
    if (resourceElementService.existsRelatedResource(tenantId, busiObjectId, "skill_object")) {
      return ResultVO.fail("服务已存在关联配置数据，不允许删除");
    }
    objectManageMapper.deleteSkillObject(tenantId, busiObjectId, SessionUtil.getLoginInfo().getUserId());
    ResourceElementFactory.get(OperClassEnum.SKILL_OBJECT.name()).clear(tenantId, busiObjectId);
    return ResultVO.success();
  }
}
