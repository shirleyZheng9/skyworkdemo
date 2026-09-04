package com.iwhalecloud.bote.controller.bot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.cache.SceneIconCache;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.dto.base.SimpleFlowStepDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneBatchOperDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneSkillDTO;
import com.iwhalecloud.bote.dto.bot.RecommendedSceneDTO;
import com.iwhalecloud.bote.dto.bot.query.BotApplyParams;
import com.iwhalecloud.bote.dto.bot.query.BotScenePromptParams;
import com.iwhalecloud.bote.dto.bot.query.BotSceneQueryParams;
import com.iwhalecloud.bote.dto.scene.KnowledgeSceneSettingDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphDTO;
import com.iwhalecloud.bote.dto.skill.StandardServiceDiffViewDTO;
import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import com.iwhalecloud.bote.service.base.IAiGeneratorService;
import com.iwhalecloud.bote.service.bot.IBotSceneManageService;
import com.iwhalecloud.bote.service.bot.IPlatSceneInfoManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 智能体管理 controller
 *
 * @author chen.linfa
 * @since 2024-08-02
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/scene", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "应用：智能体管理")
public class BotSceneManageController {

  private final IBotSceneManageService sceneManageService;
  private final IRefreshCacheService refreshCacheService;
  private final IAiGeneratorService aiGeneratorService;
  private final SceneIconCache sceneIconCache;
  private final IPlatSceneInfoManageService platSceneInfoManageService;

  @Operation(summary = "保存智能体基础信息")
  @PostMapping("saveSceneInfo")
  public ResultVO<BotSceneDTO> saveSceneInfo(@RequestBody @Valid BotSceneDTO scene) {
    boolean isUpdate = scene.getSceneId() != null;
    ResultVO<BotSceneDTO> result = sceneManageService.saveSceneInfo(scene);
    if (isUpdate && result.isSuccess()) {
      refreshCache(scene);
    }
    return result;
  }

  @Operation(summary = "一句话生成提示词智能体智能体")
  @PostMapping("aiGenPromptScene")
  public ResultVO<BotSceneDTO> aiGenPromptScene(@RequestBody BotSceneDTO scene) {
    Assert.hasLength(scene.getSceneDesc(), "智能体描述不能为空");
    return sceneManageService.aiGenPromptScene(scene);
  }

  @Operation(summary = "查询智能生成智能体进度")
  @GetMapping("getAiGenProcess")
  public ResultVO<Map<String, Object>> getAiGenProcess(@RequestParam("processId") String processId) {
    return sceneManageService.getAiGenProcess(processId);
  }

  @Operation(summary = "保存智能体，修改智能体使用")
  @PostMapping("saveScene")
  public ResultVO<BotSceneDTO> saveScene(@RequestBody BotSceneDTO scene) {
    boolean isUpdate = scene.getSceneId() != null;
    // 保存技能配置
    for (List<BotSceneSkillDTO> skills : MapUtils.emptyIfNull(scene.getSkills()).values()) {
      for (BotSceneSkillDTO botSceneSkillDTO : ListUtils.emptyIfNull(skills)) {
        botSceneSkillDTO.saveSkillConfig();
      }
    }
    ResultVO<BotSceneDTO> result = sceneManageService.saveScene(scene);
    if (isUpdate && result.isSuccess()) {
      refreshCache(scene);
    }
    return result;
  }

  @Operation(summary = "删除智能体")
  @GetMapping("removeScene")
  public ResultVO<Void> removeScene(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @Parameter(description = "智能体 ID", required = true) @RequestParam("sceneId") Long sceneId) {
    Assert.notNull(sceneId, "智能体 ID 不能为空");
    BotSceneDTO sceneInfo = sceneManageService.getSimpleScene(tenantId, sceneId);
    if (sceneInfo == null) {
      return ResultVO.success();
    }
    ResultVO<Void> result = sceneManageService.removeScene(tenantId, sceneId);
    if (result.isSuccess()) {
      refreshCache(sceneInfo);
    }
    return result;
  }

  @Operation(summary = "批量删除智能体")
  @PostMapping("removeSceneBatch")
  public ResultVO<Void> removeSceneBatch(@RequestBody BotSceneBatchOperDTO botSceneOperDTO) {
    List<BotSceneDTO> sceneDTOList = sceneManageService.removeSceneBatch(botSceneOperDTO);
    if (CollectionUtils.isNotEmpty(sceneDTOList)) {
      refreshCacheBatch(sceneDTOList, botSceneOperDTO.getTenantId());
    }
    return ResultVO.success();
  }

  @Operation(summary = "批量移动智能体")
  @PostMapping("moveSceneBatch")
  public ResultVO<Void> moveSceneBatch(@RequestBody BotSceneBatchOperDTO botSceneOperDTO) {
    List<BotSceneDTO> sceneDTOList = sceneManageService.moveSceneBatch(botSceneOperDTO);
    if (CollectionUtils.isNotEmpty(sceneDTOList)) {
      refreshCacheBatch(sceneDTOList, botSceneOperDTO.getTenantId());
    }
    return ResultVO.success();
  }

  /**
   * 刷新缓存
   * <p>只有修改、删除、上下架时需要刷新，新增时不需要</p>
   */
  private void refreshCache(BotSceneDTO scene) {
    String key = scene.getTenantId() + CacheConsts.COLON + scene.getSceneId();
    // 刷新复杂智能体的 DSL 缓存
    if (SceneConsts.SCENE_TYPE_CHATFLOW.equals(scene.getSceneType())) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_SCENE_DSL, key);
    }
    // 刷新智能体缓存
    refreshCacheService.refresh(CacheConsts.CACHE_NAME_SCENE, key);
    // 刷新智能体意图缓存
    refreshCacheService.refresh(CacheConsts.CACHE_NAME_SCENE_INTENT, scene.getTenantId().toString());
    // 刷新智能体图标缓存
    refreshCacheService.refresh(CacheConsts.CACHE_NAME_SCENE_ICON, key);
  }

  /**
   * 批量刷新缓存
   * <p>只有修改、删除、上下架时需要刷新，新增时不需要</p>
   */
  private void refreshCacheBatch(List<BotSceneDTO> sceneList, Long tenantId) {
    if (CollectionUtils.isEmpty(sceneList)) {
      return;
    }
    // 组装待刷新的全部key和复杂智能体的DSL缓存Key
    List<String> allKeys = new ArrayList<>();
    List<String> chatFlowKeys = new ArrayList<>();
    for (BotSceneDTO sceneDTO : sceneList) {
      String key = tenantId + CacheConsts.COLON + sceneDTO.getSceneId();
      allKeys.add(key);
      if (SceneConsts.SCENE_TYPE_CHATFLOW.equals(sceneDTO.getSceneType())) {
        chatFlowKeys.add(key);
      }
    }
    // 刷新复杂智能体的 DSL 缓存
    if (!chatFlowKeys.isEmpty()) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_SCENE_DSL, chatFlowKeys);
    }
    // 刷新智能体缓存
    refreshCacheService.refresh(CacheConsts.CACHE_NAME_SCENE, allKeys);
    // 刷新智能体意图缓存
    refreshCacheService.refresh(CacheConsts.CACHE_NAME_SCENE_INTENT, String.valueOf(tenantId));
    // 刷新智能体图标缓存
    refreshCacheService.refresh(CacheConsts.CACHE_NAME_SCENE_ICON, allKeys);
  }

  @Operation(summary = "获取智能体基本信息")
  @GetMapping("getSceneInfo")
  public ResultVO<BotSceneDTO> getSceneInfo(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam("sceneId") Long sceneId) {
    return ResultVO.success(sceneManageService.getSceneInfo(tenantId, sceneId));
  }

  @Operation(summary = "获取智能体")
  @GetMapping("getScene")
  public ResultVO<BotSceneDTO> getScene(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @Parameter(description = "智能体 ID", required = true) @RequestParam("sceneId") Long sceneId) {
    Assert.notNull(sceneId, "智能体 ID 不能为空");
    BotSceneDTO scene = sceneManageService.getScene(tenantId, sceneId, true);
    if (StringUtils.isNotEmpty(scene.getSceneGraphJson())) {
      if (SceneConsts.SCENE_TYPE_SCENE.equals(scene.getSceneType())) {
        scene.setLogicViews(JsonUtil.parseJson(scene.getSceneGraphJson(), new TypeReference<>() {
        }));
      }
      else if (SceneConsts.SCENE_TYPE_KNOWLEDGE.equals(scene.getSceneType())) {
        scene.setKnowledgeSetting(JsonUtil.parseJsonRequired(scene.getSceneGraphJson(), KnowledgeSceneSettingDTO.class));
      }
      else if (SceneConsts.SCENE_TYPE_CHATFLOW.equals(scene.getSceneType())) {
        scene.setGraph(JsonUtil.parseJsonRequired(scene.getSceneGraphJson(), SceneGraphDTO.class));
      }
    }
    if (StringUtils.isNotEmpty(scene.getFlowStepJson())) {
      scene.setFlowSteps(JsonUtil.parseJson(scene.getFlowStepJson(), new TypeReference<>() {
      }));
    }
    // 自定义模型信息
    if (StringUtils.isNotEmpty(scene.getModelConfigJson())) {
      scene.setCustomModelConfig(JsonUtil.parseJsonRequired(scene.getModelConfigJson(), CustomModelConfig.class));
    }
    // 解析技能配置
    ListUtils.emptyIfNull(scene.getFlatSkills()).forEach(BotSceneSkillDTO::parseSkillConfig);
    scene.parseParams();
    scene.setSceneGraphJson(null);
    scene.setSceneDsl(null);
    return ResultVO.success(scene);
  }

  @Operation(summary = "获取智能体图标")
  @GetMapping(value = "sceneIcon", produces = MediaType.ALL_VALUE)
  @IgnoreSign
  @IgnoreSession
  public void getSceneIcon(@RequestParam("tenantId") Long tenantId, @RequestParam("sceneId") Long sceneId, HttpServletRequest request,
    HttpServletResponse response) throws IOException {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(sceneId, "智能体 ID 不能为空");
    sceneIconCache.sendIcon(tenantId, sceneId, request, response);
  }

  @Operation(summary = "查询智能体列表（分页），用于应用智能体设置")
  @PostMapping("queryScenePage")
  public ResultVO<PageInfo<BotSceneDTO>> queryScenePage(@RequestBody BotSceneQueryParams queryParams) {
    return ResultVO.success(sceneManageService.queryScenePage(queryParams));
  }

  @Operation(summary = "查询智能体列表")
  @PostMapping("querySceneList")
  public ResultVO<List<BotSceneDTO>> querySceneList(@RequestBody BotSceneQueryParams queryParams) {
    return ResultVO.success(sceneManageService.querySceneList(queryParams));
  }

  @Operation(summary = "查询智能体列表", description = "用于智能体加入应用")
  @PostMapping("queryScenePageForBot")
  public ResultVO<PageInfo<BotSceneDTO>> queryScenePageForBot(@RequestBody BotSceneQueryParams queryParams) {
    Assert.notNull(queryParams.getBotId(), "应用 ID 不能为空");
    return ResultVO.success(sceneManageService.queryScenePageForBot(queryParams));
  }

  @Operation(summary = "查询智能体列表（分页），用于百应平台")
  @PostMapping("beyondQueryScenePage")
  public ResultVO<PageInfo<BotSceneDTO>> beyondQueryScenePage(@RequestBody BotSceneQueryParams queryParams) {
    return ResultVO.success(sceneManageService.beyondQueryScenePage(queryParams));
  }

  @Operation(summary = "根据智能体配置的技能、大纲树，自动生成提示词")
  @PostMapping("generatePrompt")
  public ResultVO<String> generatePrompt(@RequestBody BotSceneDTO scene) {
    return ResultVO.success(sceneManageService.generatePrompt(scene));
  }

  @Operation(summary = "根据智能体配置的技能、大纲树，自动生成提示词，后续作废")
  @PostMapping("parseSkillAndPrompt")
  public ResultVO<String> parseSkillAndPrompt(@RequestBody BotSceneDTO scene) {
    return ResultVO.success(sceneManageService.generatePrompt(scene));
  }

  @Operation(summary = "调用大模型，优化智能体提示词")
  @PostMapping("perfectPrompt")
  public ResultVO<String> perfectPrompt(@RequestBody BotScenePromptParams params) {
    Assert.hasText(params.getPrompt(), "提示词不能为空");
    return ResultVO.success(aiGeneratorService.perfectScenePrompt(params.getTenantId(), params.getPrompt()));
  }

  @Operation(summary = "智能体上下架")
  @GetMapping("publishScene")
  public ResultVO<Void> publishScene(@RequestParam("sceneStatus") String sceneStatus, @RequestParam("scentId") Long sceneId,
    @RequestParam(value = "tenantId", required = false) Long tenantId) {
    Assert.notNull(sceneId, "智能体 ID 不能为空");
    Assert.hasText(sceneStatus, "智能体状态不能为空");
    BotSceneDTO sceneInfo = sceneManageService.getSimpleScene(tenantId, sceneId);
    if (sceneInfo == null) {
      return ResultVO.fail("智能体不存在");
    }
    ResultVO<Void> result = sceneManageService.publishScene(sceneStatus, sceneId, tenantId);
    if (result.isSuccess()) {
      refreshCache(sceneInfo);
    }
    return result;
  }

  @Operation(summary = "批量上下架智能体")
  @PostMapping("publishSceneBatch")
  public ResultVO<Void> publishSceneBatch(@RequestBody BotSceneBatchOperDTO botSceneOperDTO) {
    List<BotSceneDTO> sceneDTOList = sceneManageService.publishSceneBatch(botSceneOperDTO);
    if (CollectionUtils.isNotEmpty(sceneDTOList)) {
      refreshCacheBatch(sceneDTOList, botSceneOperDTO.getTenantId());
    }
    return ResultVO.success();
  }

  @Operation(summary = "查询智能体列表，用于聊天窗口的智能体切换功能")
  @PostMapping("queryRecommendedScenes")
  public ResultVO<List<RecommendedSceneDTO>> queryRecommendedScenes(@RequestBody BotSceneQueryParams params) {
    Assert.notNull(params.getBotId(), "应用 ID 不能为空");
    params.setSceneStatus(SceneConsts.SCENE_STATUS_PUBLISH);
    params.setExcludeLabels(SceneConsts.EXCLUDE_LABELS);
    if (params.getTenantId() == null) {
      params.setTenantId(TenantIdUtil.getTenantId());
    }
    return ResultVO.success(sceneManageService.queryRecommendedScenes(params));
  }

  @Operation(summary = "查找智能体历史版本详情")
  @GetMapping("findSceneVersion")
  public ResultVO<StandardServiceDiffViewDTO> findSceneVersion(@RequestParam("logId") Long logId, @RequestParam("tenantId") Long tenantId) {
    return sceneManageService.findSceneVersion(logId, tenantId);
  }

  @Operation(summary = "比较两次修改记录的差异")
  @GetMapping("diffSceneOperLog")
  public ResultVO<StandardServiceDiffViewDTO> diffSceneOperLog(@RequestParam("tenantId") Long tenantId, @RequestParam("oldLogId") Long oldLogId,
    @RequestParam("logId") Long logId) {
    return sceneManageService.diffSceneOperLog(tenantId, oldLogId, logId);
  }

  @Operation(summary = "发布", description = "发起申请，生成新的智能体应用，或加入已有应用")
  @PostMapping("publish")
  public ResultVO<Long> publish(@RequestBody BotApplyParams apply) {
    Assert.notNull(apply.getSceneId(), "智能体 ID 不能为空");
    if (CollectionUtils.isEmpty(apply.getBotIds()) && !BaseConsts.TRUE.equals(apply.getIsCreate())) {
      return ResultVO.fail("发布信息不全，请重新确认");
    }
    return sceneManageService.publish(apply);
  }

  @Operation(summary = "根据智能体配置，自动生成流程步骤")
  @PostMapping("generateFlowStep")
  public ResultVO<List<SimpleFlowStepDTO>> generateFlowStep(@RequestBody BotSceneDTO scene) {
    return sceneManageService.generateFlowStep(scene);
  }

  @Operation(summary = "通过模板智能体创建")
  @GetMapping("saveFromPlatScene")
  public ResultVO<BotSceneDTO> saveFromPlatScene(@RequestParam("tenantId") Long tenantId, @RequestParam("platSceneId") Long platSceneId) {
    Assert.notNull(platSceneId, "模板ID不能为空");
    if (BaseConsts.ASSET_TENANT_ID.equals(tenantId)) {
      return ResultVO.fail("不能在博特资产运营租户中通过模板智能体创建");
    }
    return platSceneInfoManageService.copyPlatScene(tenantId, platSceneId);
  }

  @Operation(summary = "查找智能体历史版本详情")
  @GetMapping("getBotSceneveisonDetail")
  public ResultVO<BotSceneDTO> getBotSceneveisonDetail(@RequestParam("logId") Long logId, @RequestParam("tenantId") Long tenantId) {
    Assert.notNull(logId, "日志 ID 不能为空");
    Assert.notNull(tenantId, "租户 ID 不能为空");
    return ResultVO.success(sceneManageService.getBotSceneveisonDetail(logId, tenantId));
  }
}
