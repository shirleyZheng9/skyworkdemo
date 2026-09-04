package com.iwhalecloud.bote.service.skill.impl;

import static com.iwhalecloud.bss.litchi.base.vo.ResultVO.success;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.skill.AgentSkillDTO;
import com.iwhalecloud.bote.dto.skill.SkillPublishApplyDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillPublishApplyQueryParams;
import com.iwhalecloud.bote.entity.skill.AgentSkillSquareEntity;
import com.iwhalecloud.bote.mapper.skill.AgentSkillMapper;
import com.iwhalecloud.bote.mapper.skill.AgentSkillSquareMapper;
import com.iwhalecloud.bote.mapper.skill.SkillPublishApplyMapper;
import com.iwhalecloud.bote.service.skill.ISkillPublishApplyService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;

import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 技能发布申请服务实现
 *
 * @author wangtingyun
 * @since 2026-04-03
 */
@Service
@RequiredArgsConstructor
public class SkillPublishApplyServiceImpl implements ISkillPublishApplyService {

  private static final Logger logger = LoggerFactory.getLogger(SkillPublishApplyServiceImpl.class);

  private final SkillPublishApplyMapper skillPublishApplyMapper;
  private final AgentSkillMapper agentSkillMapper;
  private final AgentSkillSquareMapper agentSkillSquareMapper;

  @Override
  @Transactional
  public ResultVO<SkillPublishApplyDTO> saveSkillPublishApply(SkillPublishApplyDTO dto) {
    dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
    // 默认审核状态为待审核
    if (dto.getAuditStatus() == null) {
      dto.setAuditStatus(BaseConsts.SKILL_APPLY_STATUS_WAIT);
    }
    SkillPublishApplyDTO old = dto.getApplyId() == null ? null : skillPublishApplyMapper.selectSkillPublishApply(dto.getApplyId());
    DataDifference<SkillPublishApplyDTO> difference = DataDifferenceStarter.computeSave(old, dto, false, dto.getSpaceId());
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return success(difference.getToSaveData());
  }

  @Override
  public SkillPublishApplyDTO getSkillPublishApply(Long applyId) {
    return skillPublishApplyMapper.selectSkillPublishApply(applyId);
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteSkillPublishApply(Long applyId) {
    skillPublishApplyMapper.deleteSkillPublishApply(applyId, SessionUtil.getLoginInfo().getUserId());
    return success();
  }

  @Override
  public PageInfo<SkillPublishApplyDTO> getSkillPublishApplyAuditPage(SkillPublishApplyQueryParams params) {
    // noinspection resource
    return skillPublishApplyMapper.selectSkillPublishApplyAuditPage(params, params.buildRowBounds()).toPageInfo();
  }

  @Override
  public PageInfo<SkillPublishApplyDTO> getUserSkillPublishApplyPage(SkillPublishApplyQueryParams params) {
    params.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    // noinspection resource
    return skillPublishApplyMapper.selectUserSkillPublishApplyPage(params, params.buildRowBounds()).toPageInfo();
  }

  @Override
  @Transactional
  public ResultVO<Void> auditSkillPublishApply(SkillPublishApplyDTO applyDTO) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    // 1. 查询申请记录
    SkillPublishApplyDTO apply = skillPublishApplyMapper.selectSkillPublishApply(applyDTO.getApplyId());
    if (apply == null) {
      return ResultVO.fail("申请记录不存在");
    }
    // 2. 检查是否已审核
    if (!BaseConsts.SKILL_APPLY_STATUS_WAIT.equals(apply.getAuditStatus())) {
      return ResultVO.fail("该申请已审核，请勿重复操作");
    }
    // 3. 更新审核状态
    skillPublishApplyMapper.updateAuditStatus(applyDTO.getApplyId(), applyDTO.getAuditStatus(), userId, applyDTO.getAuditContent());
    // 4. 如果审核通过，发布为广场技能
    if (BaseConsts.SKILL_APPLY_STATUS_APPROVE.equals(applyDTO.getAuditStatus())) {
      publishToSkillSquare(apply);
      // 将技能此次申请记录以前的待审核状态记录设置为失效状态
      skillPublishApplyMapper.invalidSkillOldApply(apply.getApplyId(), apply.getSpaceId(), apply.getSkillId(), userId);
    }
    return success();
  }

  /**
   * 发布技能到广场（userHub 类型）
   */
  private void publishToSkillSquare(SkillPublishApplyDTO apply) {
    // 1. 查询 AgentSkill 获取技能信息
    AgentSkillDTO agentSkill = agentSkillMapper.selectAgentSkillById(apply.getSpaceId(), apply.getSkillId());
    if (agentSkill == null) {
      throw new BssException("技能不存在");
    }

    // 2. 检查是否已存在同编码的广场技能
    AgentSkillSquareEntity existing = agentSkillSquareMapper.selectByCode(agentSkill.getSkillCode());
    if (existing != null) {
      // 更新广场技能
      AgentSkillSquareEntity update = new AgentSkillSquareEntity();
      update.setSkillId(existing.getSkillId());
      update.setSkillName(agentSkill.getSkillName());
      update.setSkillDesc(agentSkill.getRemark());
      update.setFileInfoId(agentSkill.getFileInfoId());
      update.setVersion(incrementVersion(existing.getVersion()));
      agentSkillSquareMapper.updateById(update);
      return;
    }

    // 3. 创建广场技能记录: 用户贡献类型
    AgentSkillSquareEntity squareEntity = new AgentSkillSquareEntity();
    squareEntity.setSkillId(IDUtils.nextId());
    squareEntity.setSkillCode(agentSkill.getSkillCode());
    squareEntity.setSkillName(agentSkill.getSkillName());
    squareEntity.setSkillDesc(agentSkill.getRemark());
    squareEntity.setSkillType("userHub");
    squareEntity.setVersion(BaseConsts.SKILL_DEFAULT_VERSION);
    squareEntity.setSource("square");
    squareEntity.setFileInfoId(agentSkill.getFileInfoId());
    squareEntity.setOnlineStatus(BaseConsts.TRUE);
    squareEntity.setStatusCd(BaseConsts.STATUS_CD_VALID);
    squareEntity.setOwnerUserId(apply.getCreatorId());
    squareEntity.setOwnerTenantId(apply.getSpaceId());
    agentSkillSquareMapper.insert(squareEntity);
  }

  /**
   * 增加版本号
   * @param version 原版本号，格式如 "1.0.0"
   * @return 递增后的版本号，格式如 "1.0.1"
   */
  private String incrementVersion(String version) {
    if (StringUtils.isBlank(version)) {
      return BaseConsts.SKILL_DEFAULT_VERSION;
    }
    
    try {
      String[] parts = version.trim().split("\\.");
      if (parts.length == 0) {
        return BaseConsts.SKILL_DEFAULT_VERSION;
      }
      
      // 解析最新版本号部分（最后一位）
      int lastIndexOfDot = version.lastIndexOf('.');
      if (lastIndexOfDot == -1) {
        // 没有点号，直接递增整个字符串
        int versionNum = Integer.parseInt(version.trim());
        return String.valueOf(versionNum + 1);
      }
      
      String prefix = version.substring(0, lastIndexOfDot + 1);
      String lastPart = version.substring(lastIndexOfDot + 1);
      int versionNum = Integer.parseInt(lastPart);
      
      return prefix + (versionNum + 1);
    }
    catch (Exception e) {
      logger.error("解析版本号失败", e);
      return version;
    }
  }
}
