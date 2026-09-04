package com.iwhalecloud.bote.service.bot.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.bot.BotUserExperienceDTO;
import com.iwhalecloud.bote.dto.bot.query.BotUserExperienceQryParams;
import com.iwhalecloud.bote.dto.skill.SimplePageTemplateDTO;
import com.iwhalecloud.bote.mapper.bot.BotManageMapper;
import com.iwhalecloud.bote.mapper.bot.BotUserExperienceManageMapper;
import com.iwhalecloud.bote.service.bot.IBotUserExperienceManageService;
import com.iwhalecloud.bote.service.element.impl.UserExperienceResourceElementCustomizer;
import com.iwhalecloud.bote.service.skill.impl.helper.ParsePageTemplateHelper;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户会话辅助信息服务
 *
 * @author qian.sisheng
 * @since 2024/7/30
 */
@Service
@RequiredArgsConstructor
public class BotUserExperienceManageImpl implements IBotUserExperienceManageService {

  private final BotUserExperienceManageMapper userExperienceManageMapper;
  private final ParsePageTemplateHelper parseHelper;
  private final BotManageMapper botManageMapper;
  private final UserExperienceResourceElementCustomizer elementCustomizer;

  @Override
  @Transactional
  public ResultVO<BotUserExperienceDTO> saveBotUserExperience(BotUserExperienceDTO dto) {
    dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
    BotUserExperienceDTO old = dto.getExperienceId() == null ? null : getBotUserExperience(dto.getTenantId(), dto.getExperienceId());
    DataDifference<BotUserExperienceDTO> difference = DataDifferenceStarter.computeSave(old, dto, false, dto.getTenantId());
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    // 记录血缘关系
    elementCustomizer.add(dto, old);
    // 同步刷新应用的更新时间：用于刷新请求缓存的ETag值
    botManageMapper.modifyBotUpdateTime(dto.getTenantId(), dto.getBotId(), SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  public List<BotUserExperienceDTO> queryBotUserExperienceList(BotUserExperienceQryParams params) {
    List<BotUserExperienceDTO> list = userExperienceManageMapper.selectBotUserExperience(params);
    list = CollectionUtils.emptyIfNull(list).stream()
      .filter(p -> StringUtils.isEmpty(p.getSceneStatus()) || SceneConsts.SCENE_STATUS_PUBLISH.equals(p.getSceneStatus()))
      .collect(Collectors.toList());
    for (BotUserExperienceDTO dto : CollectionUtils.emptyIfNull(list)) {
      if (BaseConsts.USER_EXPERIENCE_TYPE_POINT.equals(dto.getType()) && StringUtils.isNotEmpty(dto.getPageTemplateJson())) {
        // 实时加载页面中的静态属性值
        dto.setPageContentInfo(JsonUtil.readTree(dto.getPageTemplateJson()));
        SimplePageTemplateDTO pageTemplate = JsonUtil.parseJsonRequired(dto.getPageTemplateJson(), SimplePageTemplateDTO.class);
        dto.setStaticCodeList(parseHelper.getStaticCodeList(params.getTenantId(), pageTemplate));
      }
    }
    return list;
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteBotUserExperience(Long tenantId, Long experienceId) {
    BotUserExperienceDTO botUserExperience = getBotUserExperience(tenantId, experienceId);
    if (botUserExperience == null) {
      return ResultVO.fail("信息不存在");
    }
    userExperienceManageMapper.deleteBotUserExperience(tenantId, SessionUtil.getLoginInfo().getUserId(), experienceId);
    elementCustomizer.delete(tenantId, botUserExperience.getBotId(), experienceId);
    // 同步刷新应用的更新时间：用于刷新请求缓存的ETag值
    botManageMapper.modifyBotUpdateTime(tenantId, botUserExperience.getBotId(), SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  public BotUserExperienceDTO getBotUserExperience(Long tenantId, Long experienceId) {
    BotUserExperienceDTO botUserExperience = userExperienceManageMapper.getBotUserExperience(tenantId, experienceId);
    if (botUserExperience == null) {
      return null;
    }
    if (BaseConsts.USER_EXPERIENCE_TYPE_POINT.equals(botUserExperience.getType()) && StringUtils.isNotEmpty(botUserExperience.getPageTemplateJson())) {
      // 实时加载页面中的静态属性值
      botUserExperience.setPageContentInfo(JsonUtil.readTree(botUserExperience.getPageTemplateJson()));
      SimplePageTemplateDTO pageTemplate = JsonUtil.parseJsonRequired(botUserExperience.getPageTemplateJson(), SimplePageTemplateDTO.class);
      botUserExperience.setStaticCodeList(parseHelper.getStaticCodeList(tenantId, pageTemplate));
    }
    return botUserExperience;
  }

  @Override
  public PageInfo<BotUserExperienceDTO> queryBotUserExperiencePage(BotUserExperienceQryParams params) {
    RowBounds rowBounds = params.buildRowBounds();
    // noinspection resource
    return userExperienceManageMapper.selectBotUserExperiencePage(params, rowBounds).toPageInfo();
  }

  @Override
  public List<BotUserExperienceDTO> queryBotPointList(BotUserExperienceQryParams params) {
    params.setType(BaseConsts.USER_EXPERIENCE_TYPE_POINT);
    return userExperienceManageMapper.selectBotUserExperience(params);
  }
}
