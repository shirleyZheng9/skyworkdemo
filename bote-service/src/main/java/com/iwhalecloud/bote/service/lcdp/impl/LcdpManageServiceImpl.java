package com.iwhalecloud.bote.service.lcdp.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.LcdpApiUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.lcdp.LcdpAppAggregateInfoDTO;
import com.iwhalecloud.bote.dto.lcdp.LcdpAppVersionDTO;
import com.iwhalecloud.bote.dto.lcdp.LcdpAttrSpecDTO;
import com.iwhalecloud.bote.dto.lcdp.LcdpAttrValueDTO;
import com.iwhalecloud.bote.dto.lcdp.LcdpPageInstDTO;
import com.iwhalecloud.bote.dto.lcdp.LcdpParams;
import com.iwhalecloud.bote.dto.lcdp.LcdpStandardServiceDTO;
import com.iwhalecloud.bote.dto.lcdp.query.LcdpQueryParams;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import com.iwhalecloud.bote.dto.skill.SkillAttrSpecDTO;
import com.iwhalecloud.bote.dto.skill.SkillAttrValueDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageDTO;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bote.mapper.portal.TenantManageMapper;
import com.iwhalecloud.bote.mapper.skill.SkillAttrManageMapper;
import com.iwhalecloud.bote.mapper.skill.SkillPageManageMapper;
import com.iwhalecloud.bote.service.lcdp.ILcdpManageService;
import com.iwhalecloud.bote.service.lcdp.helper.LcdpParamsConvertHelper;
import com.iwhalecloud.bote.service.skill.ISkillServiceManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 灵犀平台查询服务实现类
 *
 * @author qian.sisheng
 * @since 2025-06-05
 */
@Service
@RequiredArgsConstructor
public class LcdpManageServiceImpl implements ILcdpManageService {

  private static final String SERVICE_TYPE_ORCHESTRATION = "orchestration";
  private static final String PAGE_TYPE_PAGE = "Page";
  private final TenantManageMapper tenantManageMapper;
  private final ISkillServiceManageService serviceManageService;
  private final SkillPageManageMapper pageManageMapper;
  private final SkillAttrManageMapper attrManageMapper;

  @Override
  public PageInfo<LcdpStandardServiceDTO> queryStandardServicePage(LcdpQueryParams queryParams) {
    queryParams.setServiceType(SERVICE_TYPE_ORCHESTRATION);
    return LcdpApiUtil.queryStandardServicePage(queryParams);
  }

  @Override
  public List<LcdpAppVersionDTO> queryAppVersionList(Long tenantId) {
    TenantDTO tenant = tenantManageMapper.getTenant(tenantId);
    return LcdpApiUtil.queryAppVersionList(tenant.getAppId());
  }

  @Override
  public LcdpAppAggregateInfoDTO queryAppAggregateInfo(LcdpQueryParams queryParams) {
    return LcdpApiUtil.queryAppAggregateInfo(queryParams);
  }

  @Override
  public PageInfo<LcdpPageInstDTO> queryPageInstPage(LcdpQueryParams queryParams) {
    queryParams.setPageContainerType(PAGE_TYPE_PAGE);
    return LcdpApiUtil.queryPageInstPage(queryParams);
  }

  @Override
  public PageInfo<LcdpAttrSpecDTO> queryAttrSpecPage(LcdpQueryParams queryParams) {
    return LcdpApiUtil.queryAttrSpecPage(queryParams);
  }

  @Override
  @Transactional
  public ResultVO<Void> batchSavePageInst(List<SkillPageDTO> skillPageList) {
    for (SkillPageDTO skillPage : skillPageList) {
      if (pageManageMapper.existsSkillPageCode(skillPage)) {
        return ResultVO.fail("页面已存在, name=" + skillPage.getPageName() + ", code=" + skillPage.getPageCode());
      }
      skillPage.setPageId(Sequences.SKILL_PAGE_ID.next());
      skillPage.setTenantId(skillPage.getTenantId());
      skillPage.setCreatorId(SessionUtil.getLoginInfo().getUserId());
      skillPage.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
      skillPage.setStatusCd(BaseConsts.STATUS_CD_VALID);
    }
    pageManageMapper.batchInsertSkillPage(skillPageList);
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> batchSaveAttrSpec(LcdpAttrSpecDTO attrSpec) {
    LcdpQueryParams queryParams = new LcdpQueryParams();
    queryParams.setAppId(attrSpec.getAppId());
    queryParams.setAttrSpecAttrIds(attrSpec.getAttrIds());
    LcdpAppAggregateInfoDTO appAggregateInfo = LcdpApiUtil.queryAppAggregateInfo(queryParams);
    if (CollectionUtils.isEmpty(appAggregateInfo.getAppAttrSpecList())) {
      return ResultVO.success();
    }
    List<SkillAttrSpecDTO> skillAttrSpecList = new ArrayList<>();
    List<SkillAttrValueDTO> skillAttrValueList = new ArrayList<>();
    for (LcdpAttrSpecDTO lcdpAttrSpec : appAggregateInfo.getAppAttrSpecList()) {
      SkillAttrSpecDTO skillAttrSpec = new SkillAttrSpecDTO();
      Long attrId = Sequences.SKILL_ATTR_SPEC_ID.next();
      skillAttrSpec.setAttrId(attrId);
      skillAttrSpec.setAttrName(lcdpAttrSpec.getAttrName());
      skillAttrSpec.setAttrNbr(lcdpAttrSpec.getAttrNbr());
      skillAttrSpec.setAttrDesc(lcdpAttrSpec.getAttrDesc());
      skillAttrSpec.setDataType("1200");
      skillAttrSpec.setCatalogItemId(attrSpec.getCatalogItemId());
      skillAttrSpec.setTenantId(attrSpec.getTenantId());
      skillAttrSpec.setStatusCd(BaseConsts.STATUS_CD_VALID);
      skillAttrSpec.setCreatorId(SessionUtil.getLoginInfo().getUserId());
      skillAttrSpec.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
      skillAttrSpecList.add(skillAttrSpec);
      if (attrManageMapper.checkAttrNbr(skillAttrSpec)) {
        throw new BssException("属性已存在, attName= " + skillAttrSpec.getAttrName() + "attrNbr=" + skillAttrSpec.getAttrNbr());
      }
      for (LcdpAttrValueDTO lcdpAttrValue : lcdpAttrSpec.getAttrValueDTOList()) {
        SkillAttrValueDTO attrValue = new SkillAttrValueDTO();
        attrValue.setAttrValueId(Sequences.SKILL_ATTR_VALUE_ID.next());
        attrValue.setAttrId(attrId);
        attrValue.setAttrValue(lcdpAttrValue.getAttrValue());
        attrValue.setAttrValueName(lcdpAttrValue.getAttrValueName());
        attrValue.setTenantId(attrSpec.getTenantId());
        attrValue.setStatusCd(BaseConsts.STATUS_CD_VALID);
        attrValue.setCreatorId(SessionUtil.getLoginInfo().getUserId());
        attrValue.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
        skillAttrValueList.add(attrValue);
      }
    }
    if (CollectionUtils.isNotEmpty(skillAttrSpecList)) {
      attrManageMapper.batchInsertAttrSpec(skillAttrSpecList);
    }
    if (CollectionUtils.isNotEmpty(skillAttrValueList)) {
      attrManageMapper.batchInsertAttrValue(skillAttrValueList);
    }
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> batchSaveService(SkillServiceDTO service) {
    LcdpQueryParams queryParams = new LcdpQueryParams();
    queryParams.setAppId(service.getAppId());
    queryParams.setStandardServiceIds(service.getServiceIds());
    LcdpAppAggregateInfoDTO appAggregateInfo = LcdpApiUtil.queryAppAggregateInfo(queryParams);
    List<LcdpStandardServiceDTO> standardServiceList = appAggregateInfo.getStandardServiceList();
    if (CollectionUtils.isEmpty(standardServiceList)) {
      return ResultVO.fail("未查询到应用的编排服务信息");
    }
    batchSave(service, standardServiceList);
    return ResultVO.success();
  }

  /**
   * 保存服务
   *
   * @param service 服务
   * @param standardServiceList 编排服务列表
   */
  private void batchSave(SkillServiceDTO service, List<LcdpStandardServiceDTO> standardServiceList) {
    for (LcdpStandardServiceDTO standardService : standardServiceList) {
      SkillServiceDTO newService = new SkillServiceDTO();
      newService.setServiceName(standardService.getServiceName());
      newService.setServiceCode(standardService.getServiceCode());
      newService.setReqMethod("POST");
      newService.setTenantId(service.getTenantId());
      String relativePath = "app/orchestration/runWithOutputOnly/" + service.getAppId() + "/" + standardService.getServiceCode();
      newService.setRelativePath(relativePath);
      newService.setCatalogItemId(service.getCatalogItemId());
      newService.setPlatform(service.getPlatform());
      convertParams(standardService, newService);
      serviceManageService.saveSkillService(newService);
    }
  }

  private void convertParams(LcdpStandardServiceDTO standardService, SkillServiceDTO service) {
    if (StringUtils.isNotEmpty(standardService.getRequestJson())) {
      List<LcdpParams> request = JsonUtil.parseJsonRequired(standardService.getRequestJson(), new TypeReference<List<LcdpParams>>() {
      });
      ParameterSpec requestParameter = LcdpParamsConvertHelper.convert(request.get(0));
      service.setBodyJson(JsonUtil.toJsonString(requestParameter));
    }
    if (StringUtils.isNotEmpty(standardService.getResponseJson())) {
      List<LcdpParams> response = JsonUtil.parseJsonRequired(standardService.getResponseJson(), new TypeReference<List<LcdpParams>>() {
      });
      ParameterSpec responseParameter = LcdpParamsConvertHelper.convert(response.get(0));
      service.setResponseJson(JsonUtil.toJsonString(responseParameter));
    }
  }

}
