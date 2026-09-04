package com.iwhalecloud.bote.controller.agent;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.agent.AiSkillDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import com.iwhalecloud.bote.dto.skill.AgentSkillDTO;
import com.iwhalecloud.bote.service.agent.IAiSkillManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * BoteClaw 配置管理 controller
 *
 * @author linmengfan
 * @since 2026-03-05
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/generalAgent/skill", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "BoteClaw 配置管理-技能")
public class AiSkillGeneralAgentManageController {

  // @formatter:off
  private final IAiSkillManageService btAiSkillManageService;
  private final IRefreshCacheService refreshCacheService;
  // @formatter:on

  @Operation(summary = "保存新增表记录启用技能")
  @PostMapping("enableBtAiSkill")
  public ResultVO<AiSkillDTO> enableAiSkill(@RequestBody AiSkillDTO skill) {
    Assert.notNull(skill.getSpaceId(), "空间 ID 不能为空");
    Assert.notNull(skill.getSkillId(), "技能 ID 不能为空");
    if (skill.getBotId() == null) {
      skill.setBotId(BaseConsts.BOTE_AI_ID);
    }
    ResultVO<AiSkillDTO> result = btAiSkillManageService.saveAiSkill(skill);
    if (result.isSuccess()) {
      Long userId = SessionUtil.getLoginInfo().getUserId();
      String key = skill.getSpaceId() + CacheConsts.COLON + skill.getBotId() + CacheConsts.COLON + userId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_GENERAL_AGENT, key);
    }
    return result;
  }

  @Operation(summary = "禁用启用技能")
  @PostMapping("disabledBtAiSkill")
  public ResultVO<AiSkillDTO> disabledBtAiSkill(@RequestBody AiSkillDTO skill) {
    Assert.notNull(skill.getSpaceId(), "空间 ID 不能为空");
    Assert.notNull(skill.getSkillId(), "技能 ID 不能为空");
    if (skill.getBotId() == null) {
      skill.setBotId(BaseConsts.BOTE_AI_ID);
    }
    ResultVO<AiSkillDTO> result = btAiSkillManageService.disabledBtAiSkill(skill);
    if (result.isSuccess()) {
      AiSkillDTO resultObject = result.getResultObject();
      Long userId = SessionUtil.getLoginInfo().getUserId();
      String key = resultObject.getSpaceId() + CacheConsts.COLON + resultObject.getBotId() + CacheConsts.COLON + userId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_GENERAL_AGENT, key);
    }
    return result;
  }

  @Operation(summary = "分页查询ai通用智能体技能")
  @PostMapping("queryAiAgentSkillPage")
  public ResultVO<PageInfo<AgentSkillDTO>> queryAiAgentSkillPage(@RequestBody AiQueryParams queryParams) {
    // 纠正入参
    if (queryParams.getSpaceId() == null) {
      queryParams.setSpaceId(queryParams.getTenantId());
    }
    Assert.notNull(queryParams.getSpaceId(), "空间 ID 不能为空");
    if (queryParams.getBotId() == null) {
      queryParams.setBotId(BaseConsts.BOTE_AI_ID);
    }
    queryParams.setSearchContent(queryParams.getKeyword());
    return ResultVO.success(btAiSkillManageService.queryAiAgentSkillPage(queryParams));
  }

  @Operation(summary = "保存技能通用技能")
  @PostMapping("saveAiAgentSkill")
  public ResultVO<AgentSkillDTO> saveAiAgentSkill(@RequestBody AgentSkillDTO skill) {
    Long skillId = skill.getSkillId();
    ResultVO<AgentSkillDTO> result = btAiSkillManageService.saveAiAgentSkill(skill);
    if (skillId != null && result.isSuccess()) {
      String key = skill.getTenantId() + CacheConsts.COLON + skillId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_AGENT_SKILL, key);
    }
    return result;
  }

  @Operation(summary = "删除通用智能体技能")
  @PostMapping("deleteAiAgentSkill")
  public ResultVO<Boolean> deleteAgentSkill(@RequestParam("spaceId") Long spaceId,
                                         @RequestParam("skillId") Long skillId) {
    Assert.notNull(spaceId, "空间 ID 不能为空");
    Assert.notNull(skillId, "技能 ID 不能为空");
    ResultVO<Boolean> result = btAiSkillManageService.deleteAgentSkill(spaceId, skillId);
    if (result.isSuccess() && Boolean.TRUE.equals(result.getResultObject())) {
      String key = spaceId + CacheConsts.COLON + skillId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_AGENT_SKILL, key);
    }
    return result;
  }

  @Operation(summary = "批量安装技能应用列表")
  @PostMapping("batchSaveAiAgentSkill")
  public ResultVO<Void> batchSaveAiAgentSkill(@RequestBody AiSkillDTO skill) {
    Assert.notNull(skill.getSpaceId(), "空间 ID 不能为空");
    Assert.notNull(skill.getSkillId(), "技能 ID 不能为空");
    ResultVO<List<Long>> resultVO = btAiSkillManageService.batchSaveAiAgentSkill(skill);
    if (resultVO.isSuccess() && !resultVO.getResultObject().isEmpty()) {
      Long userId = SessionUtil.getLoginInfo().getUserId();
      List<String> keyList = resultVO.getResultObject().stream()
        .map(botId -> skill.getSpaceId() + CacheConsts.COLON + botId + CacheConsts.COLON + userId)
        .toList();
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_GENERAL_AGENT, keyList);
    }
    return ResultVO.success();
  }

  @Operation(summary = "查询技能所安装的应用列表")
  @GetMapping("queryBotListBySkillId")
  public ResultVO<List<AiSkillDTO>> queryBotListBySkillId(@RequestParam("spaceId") Long spaceId, @RequestParam("skillId") Long skillId) {
    Assert.notNull(spaceId, "空间 ID 不能为空");
    Assert.notNull(skillId, "技能 ID 不能为空");
    return ResultVO.success(btAiSkillManageService.queryBotListBySkillId(spaceId, skillId));
  }
}
