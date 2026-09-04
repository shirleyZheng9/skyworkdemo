package com.iwhalecloud.bote.service.skill.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.skill.SkillPageCompDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageCompQueryParams;
import com.iwhalecloud.bote.mapper.skill.SkillPageCompManageMapper;
import com.iwhalecloud.bote.service.skill.ISkillPageCompManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 技能：页面组件服务实现
 *
 * @author lizuyin
 * @since 2026-01-14
 */
@Service
@RequiredArgsConstructor
public class SkillPageCompManageServiceImpl implements ISkillPageCompManageService {

  private final SkillPageCompManageMapper pageCompManageMapper;

  @Override
  public SkillPageCompDTO findSkillPageComp(Long tenantId, Long pageCompId) {
    return pageCompManageMapper.getSkillPageComp(tenantId, pageCompId);
  }

  @Override
  @Transactional
  public ResultVO<SkillPageCompDTO> saveSkillPageComp(SkillPageCompDTO dto) {
    // 检查同名页面组件
    if (pageCompManageMapper.existsSkillPageCompName(dto)) {
      return BaseErrorConstant.CHECK_NAME.toResult(dto.getPageCompName());
    }
    SkillPageCompDTO old = dto.getPageCompId() == null ? null : findSkillPageComp(dto.getTenantId(), dto.getPageCompId());
    if (old == null) {
      dto.setPageCompId(Sequences.SKILL_PAGE_COMP_ID.next());
      dto.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    }
    dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
    dto.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    DataDifferenceStarter.computeSave(old, dto, false, dto.getTenantId());
    return ResultVO.success(dto);
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteSkillPageComp(Long tenantId, Long pageCompId) {
    // 检查记录是否存在
    SkillPageCompDTO pageComp = pageCompManageMapper.getSkillPageComp(tenantId, pageCompId);
    Assert.notNull(pageComp, "页面组件不存在，无法删除");

    // 逻辑删除：更新status_cd为'00X'
    pageCompManageMapper.deleteSkillPageComp(tenantId, pageCompId, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  public PageInfo<SkillPageCompDTO> querySkillPageCompPage(SkillPageCompQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    // noinspection resource
    return pageCompManageMapper.selectSkillPageCompPage(queryParams, rowBounds).toPageInfo();
  }
}

