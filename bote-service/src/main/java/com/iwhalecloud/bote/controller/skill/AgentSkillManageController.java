package com.iwhalecloud.bote.controller.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.ontology.OntoSceneDTO;
import com.iwhalecloud.bote.dto.ontology.OntologyActionDTO;
import com.iwhalecloud.bote.dto.ontology.OntologyAppDTO;
import com.iwhalecloud.bote.dto.ontology.OntologyObjectDTO;
import com.iwhalecloud.bote.dto.ontology.OntologyRuleDTO;
import com.iwhalecloud.bote.dto.ontology.query.OntologyQueryParams;
import com.iwhalecloud.bote.dto.skill.AgentSkillDirDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillFileDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillPublishResultDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillTreeNodeDTO;
import com.iwhalecloud.bote.dto.skill.query.AgentSkillQueryParams;
import com.iwhalecloud.bote.service.skill.IAgentSkillManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Agent Skill 管理功能
 *
 * @author bianjp
 * @since 2026-02-03
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/agentSkill", name = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@RestController
@Tag(name = "技能：Agent Skill 管理")
public class AgentSkillManageController {
  private final IAgentSkillManageService agentSkillManageService;
  private final IRefreshCacheService refreshCacheService;

  @Operation(summary = "保存技能")
  @PostMapping("saveAgentSkill")
  public ResultVO<AgentSkillDTO> saveAgentSkill(@RequestBody AgentSkillDTO skill) {
    if (StringUtils.isNotEmpty(skill.getDataFrom()) && skill.getBotId() == null) {
      skill.setBotId(BaseConsts.BOTE_AI_ID);
    }
    ResultVO<AgentSkillDTO> result = agentSkillManageService.saveAgentSkill(skill);
    if (result.isSuccess()) {
      String key = skill.getTenantId() + CacheConsts.COLON + result.getResultObject().getSkillId();
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_AGENT_SKILL, key);
    }
    return result;
  }

  @Operation(summary = "查询单个技能")
  @GetMapping("findAgentSkill")
  public ResultVO<AgentSkillDTO> findAgentSkill(@RequestParam("tenantId") Long tenantId, @RequestParam("skillId") Long skillId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(skillId, "技能 ID 不能为空");
    return ResultVO.success(agentSkillManageService.findAgentSkill(tenantId, skillId));
  }

  @Operation(summary = "分页查询技能")
  @PostMapping("queryAgentSkillPage")
  public ResultVO<PageInfo<AgentSkillDTO>> queryAgentSkillPage(@RequestBody AgentSkillQueryParams params) {
    return ResultVO.success(agentSkillManageService.queryAgentSkillPage(params));
  }

  @Operation(summary = "查询 Bot 已安装的技能广场技能 ID 列表")
  @GetMapping("listInstalledSkillSquareIds")
  public ResultVO<List<Long>> listInstalledSkillSquareIds(@RequestParam("tenantId") Long tenantId, @RequestParam("botId") Long botId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(botId, "Bot ID 不能为空");
    return ResultVO.success(agentSkillManageService.listInstalledSkillSquareIds(tenantId, botId));
  }

  @Operation(summary = "删除技能")
  @PostMapping("deleteAgentSkill")
  public ResultVO<Void> deleteAgentSkill(@RequestParam("tenantId") Long tenantId, @RequestParam("skillId") Long skillId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(skillId, "技能 ID 不能为空");
    ResultVO<Void> result = agentSkillManageService.deleteAgentSkill(tenantId, skillId);
    if (result.isSuccess()) {
      String key = tenantId + CacheConsts.COLON + skillId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_AGENT_SKILL, key);
    }
    return result;
  }

  @Operation(summary = "上传 Agent Skill 文件")
  @PostMapping("uploadAgentSkillFile")
  public ResultVO<FileInfoVO> uploadAgentSkillFile(@RequestParam("file") MultipartFile file) {
    return agentSkillManageService.uploadAgentSkillFile(file);
  }

  @Operation(summary = "查询当前空间下用户的技能")
  @PostMapping("queryUserSpaceSkillPage")
  public ResultVO<PageInfo<AgentSkillDTO>> queryUserSpaceSkillPage(@RequestBody AgentSkillQueryParams params) {
    Assert.notNull(params.getSpaceId(), "空间 ID 不能为空");
    return ResultVO.success(agentSkillManageService.getUserSpaceSkillPage(params));
  }

  @Operation(summary = "保存 Agent Skill 目录")
  @PostMapping("saveAgentSkillDir")
  public ResultVO<AgentSkillDirDTO> saveAgentSkillDir(@RequestBody AgentSkillDirDTO dir) {
    return agentSkillManageService.saveAgentSkillDir(dir);
  }

  @Operation(summary = "删除 Agent Skill 目录")
  @GetMapping("deleteAgentSkillDir")
  public ResultVO<Void> deleteAgentSkillDir(@RequestParam("tenantId") Long tenantId, @RequestParam("skillId") Long skillId,
    @RequestParam("dirId") Long dirId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(skillId, "技能 ID 不能为空");
    Assert.notNull(dirId, "目录 ID 不能为空");
    return agentSkillManageService.deleteAgentSkillDir(tenantId, skillId, dirId);
  }

  @Operation(summary = "保存 Agent Skill 文本文件")
  @PostMapping("saveAgentSkillFile")
  public ResultVO<AgentSkillFileDTO> saveAgentSkillFile(@RequestBody AgentSkillFileDTO file) {
    Assert.notNull(file.getTenantId(), "租户 ID 不能为空");
    Assert.notNull(file.getSkillId(), "技能 ID 不能为空");
    Assert.notNull(file.getDirId(), "目录 ID 不能为空");
    Assert.hasLength(file.getFileName(), "文件名不能为空");
    return agentSkillManageService.saveAgentSkillFile(file);
  }

  @Operation(summary = "上传 Agent Skill 文件")
  @PostMapping("uploadAgentSkillSingleFile")
  public ResultVO<AgentSkillFileDTO> uploadAgentSkillSingleFile(@RequestPart("file") MultipartFile file, @RequestParam("tenantId") Long tenantId,
    @RequestParam("skillId") Long skillId, @RequestParam("dirId") Long dirId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(skillId, "技能 ID 不能为空");
    Assert.notNull(dirId, "目录 ID 不能为空");
    return agentSkillManageService.uploadAgentSkillFile(file, tenantId, skillId, dirId);
  }

  @Operation(summary = "删除 Agent Skill 文件")
  @GetMapping("deleteAgentSkillFile")
  public ResultVO<Void> deleteAgentSkillFile(@RequestParam("tenantId") Long tenantId, @RequestParam("skillFileId") Long skillFileId,
    @RequestParam("skillId") Long skillId) {
    return agentSkillManageService.deleteAgentSkillFile(tenantId, skillId, skillFileId);
  }

  @Operation(summary = "查询 Agent Skill 单个文件内容")
  @GetMapping("getAgentSkillFileContent")
  public ResultVO<AgentSkillFileDTO> getAgentSkillFileContent(@RequestParam("tenantId") Long tenantId,
    @RequestParam("skillFileId") Long skillFileId) {
    return agentSkillManageService.getAgentSkillFileContent(tenantId, skillFileId);
  }

  @Operation(summary = "查询 Agent Skill 目录与文件树")
  @GetMapping("queryAgentSkillTree")
  public ResultVO<List<AgentSkillTreeNodeDTO>> queryAgentSkillTree(@RequestParam("tenantId") Long tenantId, @RequestParam("skillId") Long skillId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(skillId, "技能 ID 不能为空");
    return agentSkillManageService.queryAgentSkillTree(tenantId, skillId);
  }

  @Operation(summary = "检测并初始化 Agent Skill 存量数据")
  @GetMapping("checkAndInitAgentSkill")
  public ResultVO<Void> checkAndInitAgentSkill(@RequestParam("tenantId") Long tenantId, @RequestParam("skillId") Long skillId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(skillId, "技能 ID 不能为空");
    return agentSkillManageService.checkAndInitAgentSkill(tenantId, skillId);
  }

  @Operation(summary = "一键发布 Agent Skill")
  @GetMapping("publishAgentSkill")
  public ResultVO<AgentSkillPublishResultDTO> publishAgentSkill(@RequestParam("tenantId") Long tenantId, @RequestParam("skillId") Long skillId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(skillId, "技能 ID 不能为空");
    ResultVO<AgentSkillPublishResultDTO> result = agentSkillManageService.publishAgentSkill(tenantId, skillId);
    if (result.isSuccess()) {
      String key = tenantId + CacheConsts.COLON + skillId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_AGENT_SKILL, key);
    }
    return result;
  }

  @Operation(summary = "查询本体应用列表(分页)")
  @PostMapping("queryOntologyAppPage")
  public ResultVO<PageInfo<OntologyAppDTO>> queryOntologyAppPage(@RequestBody OntologyQueryParams params) {
    return ResultVO.success(agentSkillManageService.queryOntologyAppPage(params));
  }

  @Operation(summary = "查询本体场景列表(分页)")
  @PostMapping("queryOntologyScenePage")
  public ResultVO<PageInfo<OntoSceneDTO>> queryOntologyScenePage(@RequestBody OntologyQueryParams params) {
    return ResultVO.success(agentSkillManageService.queryOntologyScenePage(params));
  }

  @Operation(summary = "查询本体场景详情")
  @PostMapping("getOntologySceneDetail")
  public ResultVO<OntoSceneDTO> queryOntologySceneDetail(@RequestBody OntologyQueryParams params) {
    return ResultVO.success(agentSkillManageService.getOntologySceneDetail(params));
  }

  @Operation(summary = "查询本体应用规则列表（分页）")
  @PostMapping("queryOntologyRulePage")
  public ResultVO<PageInfo<OntologyRuleDTO>> queryOntologyRulePage(@RequestBody OntologyQueryParams params) {
    return ResultVO.success(agentSkillManageService.queryOntologyRulePage(params));
  }

  @Operation(summary = "查询本应用对象列表（分页）")
  @PostMapping("queryOntologyObjectPage")
  public ResultVO<PageInfo<OntologyObjectDTO>> queryOntologyObjectPage(@RequestBody OntologyQueryParams params) {
    return ResultVO.success(agentSkillManageService.queryOntologyObjectPage(params));
  }

  @Operation(summary = "查询本体应用动作列表（分页）")
  @PostMapping("queryOntologyActionPage")
  public ResultVO<PageInfo<OntologyActionDTO>> queryOntologyActionPage(@RequestBody OntologyQueryParams params) {
    return ResultVO.success(agentSkillManageService.queryOntologyActionPage(params));
  }
}

