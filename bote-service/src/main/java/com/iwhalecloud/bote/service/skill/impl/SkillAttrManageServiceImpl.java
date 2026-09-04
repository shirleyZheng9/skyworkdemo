package com.iwhalecloud.bote.service.skill.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.skill.AttrSpecValueImport;
import com.iwhalecloud.bote.dto.skill.SkillAttrSpecDTO;
import com.iwhalecloud.bote.dto.skill.SkillAttrValueDTO;
import com.iwhalecloud.bote.dto.skill.SkillAttrValueRelDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.mapper.skill.SkillAttrManageMapper;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bote.service.skill.ISkillAttrManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 技能：属性管理服务实现
 *
 * @author auto
 * @since 2024-09-15
 */
@Service
@RequiredArgsConstructor
public class SkillAttrManageServiceImpl implements ISkillAttrManageService {
  private final SkillAttrManageMapper attrManageMapper;
  private final ICatalogManageService catalogManageService;
  private final IResourceElementService resourceElementService;

  @Override
  @Nullable
  public SkillAttrSpecDTO findAttrSpec(Long tenantId, Long attrId) {
    SkillAttrSpecDTO attrSpec = attrManageMapper.getAttrSpec(tenantId, attrId);
    if (attrSpec == null) {
      return null;
    }
    List<SkillAttrValueDTO> attrValues = attrManageMapper.selectAttrValueList(attrSpec.getTenantId(), attrSpec.getAttrId());
    attrSpec.setAttrValues(attrValues);
    // 属性值关联
    if (CollectionUtils.isNotEmpty(attrValues)) {
      attrValues.forEach(value -> {
        // 设置属性值关联
        value.setAttrValueRelList(attrManageMapper.selectAttrValueRelList(attrSpec.getTenantId(), Collections.singletonList(value.getAttrValueId())));
      });
    }
    return attrSpec;
  }

  @Override
  @Nullable
  public SkillAttrSpecDTO findAttrSpecByCode(String attrCode, Long tenantId) {
    SkillAttrSpecDTO attrSpec = attrManageMapper.getAttrSpecByCode(attrCode, tenantId);
    if (attrSpec == null) {
      return null;
    }
    attrSpec.setAttrValues(attrManageMapper.selectAttrValueList(attrSpec.getTenantId(), attrSpec.getAttrId()));
    return attrSpec;
  }

  @Override
  @Transactional
  public ResultVO<SkillAttrSpecDTO> saveAttrSpec(SkillAttrSpecDTO attrSpec) {
    ResultVO<Void> result = validateAttrSpec(attrSpec);
    if (!result.isSuccess()) {
      return new ResultVO<>(result);
    }
    if (attrSpec.getCopyAttrId() != null) {
      return copyAttrSpec(attrSpec);
    }
    // 校验属性编码唯一性
    if (attrManageMapper.checkAttrNbr(attrSpec)) {
      return BaseErrorConstant.CHECK_ATTR_NBR.toResult(attrSpec.getAttrNbr());
    }
    fillAttrSpec(attrSpec);
    SkillAttrSpecDTO old = attrSpec.getAttrId() == null ? null : findAttrSpec(attrSpec.getTenantId(), attrSpec.getAttrId());
    DataDifference<SkillAttrSpecDTO> difference = DataDifferenceStarter.computeSaveAndLog(old, attrSpec, true, attrSpec.getTenantId(),
      OperClassEnum.SKILL_ATTR);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  private ResultVO<Void> validateAttrSpec(SkillAttrSpecDTO attrSpec) {
    if (CollectionUtils.isEmpty(attrSpec.getAttrValues())) {
      return ResultVO.success();
    }
    for (SkillAttrValueDTO attrValue : attrSpec.getAttrValues()) {
      if (CollectionUtils.isEmpty(attrValue.getAttrValueRelList())) {
        continue;
      }
      for (SkillAttrValueRelDTO rel : attrValue.getAttrValueRelList()) {
        if (Objects.equals(rel.getZAttrId(), attrValue.getAttrId())) {
          return ResultVO.fail("属性值不能与自身关联");
        }
      }
    }
    return ResultVO.success();
  }

  private ResultVO<SkillAttrSpecDTO> copyAttrSpec(SkillAttrSpecDTO attrSpec) {
    SkillAttrSpecDTO oldAttrSpec = findAttrSpec(attrSpec.getTenantId(), attrSpec.getCopyAttrId());
    if (oldAttrSpec == null) {
      return BaseErrorConstant.BOT_SKILL_ATTR_SPEC_NOT_EXISTS.toResult(attrSpec.getAttrId());
    }
    if (attrManageMapper.checkAttrNbr(attrSpec)) {
      return BaseErrorConstant.CHECK_ATTR_NBR.toResult(attrSpec.getAttrNbr());
    }
    for (SkillAttrValueDTO attr : CollectionUtils.emptyIfNull(attrSpec.getAttrValues())) {
      attr.setAttrId(null);
      attr.setAttrValueId(null);
      for (SkillAttrValueRelDTO rel : CollectionUtils.emptyIfNull(attr.getAttrValueRelList())) {
        rel.setRelId(null);
        rel.setAAttrValueId(null);
      }
    }
    DataDifferenceStarter.computeSaveAndLog(null, attrSpec, true, attrSpec.getTenantId(), OperClassEnum.SKILL_ATTR);
    return ResultVO.success(attrSpec);
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteAttrSpec(Long tenantId, Long attrId) {
    if (resourceElementService.existsRelatedResource(tenantId, attrId, DataSyncCodeEnum.SKILL_ATTR.getCode())) {
      return ResultVO.fail("数据已存在关联配置数据，不允许删除");
    }
    attrManageMapper.deleteAttrSpec(tenantId, attrId, SessionUtil.getLoginInfo().getUserId());
    // 删除属性值关联
    attrManageMapper.deleteAttrValueRel(tenantId, attrId, SessionUtil.getLoginInfo().getUserId());
    ResourceElementFactory.get(OperClassEnum.SKILL_ATTR.name()).clear(tenantId, attrId);
    return ResultVO.success();
  }

  @Override
  public List<SkillAttrSpecDTO> queryAttrSpecList(SkillQueryParams queryParams) {
    return attrManageMapper.selectAttrSpecList(queryParams);
  }

  @Override
  public PageInfo<SkillAttrSpecDTO> queryAttrSpecPage(SkillQueryParams queryParams) {
    if (!BaseConsts.FALSE.equals(queryParams.getConfigFlag())) {
      queryParams.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(queryParams.getTenantId(), queryParams.getCatalogItemId(), CatalogConsts.TYPE_SKILL));
    }
    RowBounds rowBounds = queryParams.buildRowBounds();
    // noinspection resource
    return attrManageMapper.selectAttrSpecPage(queryParams, rowBounds).toPageInfo();
  }

  private void fillAttrSpec(SkillAttrSpecDTO attrSpec) {
    if (StringUtils.isEmpty(attrSpec.getDataType())) {
      attrSpec.setDataType("1200");
    }
    attrSpec.setStatusCd(BaseConsts.STATUS_CD_VALID);
    for (SkillAttrValueDTO value : CollectionUtils.emptyIfNull(attrSpec.getAttrValues())) {
      value.setStatusCd(BaseConsts.STATUS_CD_VALID);
      value.setTenantId(attrSpec.getTenantId());
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void parseSpec(List<AttrSpecValueImport> attrSpecValueImportList, Long tenantId, Long catalogItemId) {
    Map<String, List<AttrSpecValueImport>> attrSpecValueImportGroup = attrSpecValueImportList.stream()
      .collect(Collectors.groupingBy(AttrSpecValueImport::getAttrNbr));
    List<SkillAttrSpecDTO> attrSpecSaveList = new ArrayList<>();
    setAttrSpecSaveList(attrSpecValueImportGroup, attrSpecSaveList, tenantId, catalogItemId);
    for (SkillAttrSpecDTO attrSpec : attrSpecSaveList) {
      DataDifferenceStarter.computeSaveAndLog(findAttrSpecByCode(attrSpec.getAttrNbr(), tenantId), attrSpec, true, tenantId,
        OperClassEnum.SKILL_ATTR);
    }
  }

  private void setAttrSpecSaveList(Map<String, List<AttrSpecValueImport>> attrSpecValueImportGroup, List<SkillAttrSpecDTO> attrSpecSaveList,
    Long tenantId, Long catalogItemId) {
    attrSpecValueImportGroup.forEach((attrNbr, importList) -> {
      SkillAttrSpecDTO attrSpec = new SkillAttrSpecDTO();
      attrSpec.setAttrNbr(attrNbr);
      attrSpec.setAttrDesc(importList.get(0).getAttrDesc());
      attrSpec.setAttrName(importList.get(0).getAttrName());
      attrSpec.setAttrValues(new ArrayList<>());
      attrSpec.setDataType("1200");
      attrSpec.setCatalogItemId(catalogItemId);
      attrSpec.setTenantId(tenantId);
      for (AttrSpecValueImport attrSpecValueImport : importList) {
        SkillAttrValueDTO attrValue = new SkillAttrValueDTO();
        attrValue.setAttrValue(attrSpecValueImport.getAttrValue());
        attrValue.setAttrValueName(attrSpecValueImport.getAttrValueName());
        attrValue.setTenantId(tenantId);
        attrSpec.getAttrValues().add(attrValue);
      }
      attrSpecSaveList.add(attrSpec);
    });
  }

}
