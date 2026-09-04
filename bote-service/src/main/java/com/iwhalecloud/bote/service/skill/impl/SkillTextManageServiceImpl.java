package com.iwhalecloud.bote.service.skill.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.skill.SkillTextDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.mapper.skill.SkillTextManageMapper;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.skill.ISkillTextManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 技能：文本 服务实现
 *
 * @author auto
 * @since 2024-09-16
 */
@Service
@RequiredArgsConstructor
public class SkillTextManageServiceImpl implements ISkillTextManageService {

  private final SkillTextManageMapper textManageMapper;

  private final ICatalogManageService catalogManageService;

  @Override
  @Transactional
  public ResultVO<SkillTextDTO> saveSkillText(SkillTextDTO text) {
    text.setStatusCd(BaseConsts.STATUS_CD_VALID);
    SkillTextDTO old = text.getTextId() == null ? null : findSkillText(text.getTenantId(), text.getTextId());
    DataDifference<SkillTextDTO> difference = DataDifferenceStarter.computeSave(old, text, false, text.getTenantId());
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  public SkillTextDTO findSkillText(Long tenantId, Long textId) {
    return textManageMapper.getSkillText(tenantId, textId);
  }

  @Override
  public List<SkillTextDTO> querySkillTextList(SkillQueryParams params) {
    return textManageMapper.selectSkillTextList(params);
  }

  @Override
  public PageInfo<SkillTextDTO> querySkillTextPage(SkillQueryParams params) {
    if (!BaseConsts.FALSE.equals(params.getConfigFlag())) {
      params.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(params.getTenantId(), params.getCatalogItemId(), CatalogConsts.TYPE_SKILL));
    }
    // noinspection resource
    return textManageMapper.selectSkillTextPage(params, params.buildRowBounds()).toPageInfo();
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteSkillText(Long tenantId, Long textId) {
    textManageMapper.deleteSkillText(tenantId, textId, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }
}
