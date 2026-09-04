package com.iwhalecloud.bote.controller.skill;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.dto.generator.flow.ExplainFlowParamsDTO;
import com.iwhalecloud.bote.dto.generator.flow.GenerateFlowParamsDTO;
import com.iwhalecloud.bote.dto.generator.flow.QuerySkillInfoParamsDTO;
import com.iwhalecloud.bote.dto.generator.flow.SaveFlowRequestDTO;
import com.iwhalecloud.bote.dto.generator.flow.SkillBasicInfoDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowDTO;
import com.iwhalecloud.bote.generator.flow.FlowAiHelper;
import com.iwhalecloud.bote.generator.flow.SkillQueryHelper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 工作流 AI 辅助功能
 *
 * @author bianjp
 * @since 2025-03-28
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/flowAi", name = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor
@Tag(name = "技能：流程 AI 功能")
@SuppressWarnings("PMD.GuardLogStatement")
public class FlowAiController {
  private static final Logger logger = LoggerFactory.getLogger(FlowAiController.class);

  private final FlowAiHelper flowAiHelper;
  private final SkillQueryHelper skillQueryHelper;
  private final IRefreshCacheService refreshCacheService;

  @PostMapping("qrySkills")
  @Operation(summary = "查询技能基本信息")
  public ResultVO<Map<String, List<SkillBasicInfoDTO>>> qrySkills(@RequestBody QuerySkillInfoParamsDTO params) {
    Assert.notNull(params.getTenantId(), "租户 ID 不能为空");
    Assert.notEmpty(params.getSkillIdsMap(), "技能 ID 列表映射不能为空");
    return ResultVO.success(skillQueryHelper.querySkills(params.getTenantId(), params.getSkillIdsMap()));
  }

  @PostMapping(path = "explain", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Operation(summary = "解释流程")
  public SseEmitter explain(@RequestBody ExplainFlowParamsDTO params) {
    Assert.notNull(params.getTenantId(), "租户 ID 不能为空");
    Assert.notNull(params.getFlowId(), "流程 ID 不能为空");
    return SseUtil.createSseEmitter(params.getClientId(), emitter -> {
      try {
        flowAiHelper.explainStream(params, m -> {
          if (StringUtils.isNotEmpty(m.getContent())) {
            SseUtil.sendJson(emitter, ChatMessageType.TEXT, m.getContent());
          }
        });
      }
      catch (BssException e) {
        logger.error("Failed to explain flow: tenantId={}, flowId={}, error={}", params.getTenantId(), params.getFlowId(), e.getMessage());
        SseUtil.sendJson(emitter, ChatMessageType.ERROR, e.getMessage());
      }
      catch (RuntimeException e) {
        logger.error("Failed to explain flow: tenantId={}, flowId={}", params.getTenantId(), params.getFlowId(), e);
        SseUtil.sendJson(emitter, ChatMessageType.ERROR, ExpUtil.getMsg(e));
      }
      finally {
        SseUtil.completeQuietly(emitter);
      }
    });
  }

  @PostMapping(path = "generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Operation(summary = "生成流程")
  public SseEmitter generate(@RequestBody GenerateFlowParamsDTO params) {
    Assert.notNull(params.getTenantId(), "租户 ID 不能为空");
    Assert.hasLength(params.getPrompt(), "需求描述不能为空");
    Assert.hasLength(params.getFlowType(), "流程类型不能为空");
    return SseUtil.createSseEmitter(params.getClientId(), emitter -> {
      try {
        SkillFlowDTO flow = flowAiHelper.generate(params, m -> {
          if (StringUtils.isNotEmpty(m.getReasoningContent())) {
            SseUtil.sendJson(emitter, ChatMessageType.REASONING, m.getReasoningContent());
          }
          else if (StringUtils.isNotEmpty(m.getContent())) {
            SseUtil.sendJson(emitter, ChatMessageType.TEXT, m.getContent());
          }
        });
        SseUtil.sendJson(emitter, "flow", null, flow);
      }
      catch (BssException e) {
        logger.error("Failed to generate flow: tenantId={}, error={}", params.getTenantId(), e.getMessage());
        SseUtil.sendJson(emitter, ChatMessageType.ERROR, e.getMessage());
      }
      catch (RuntimeException e) {
        logger.error("Failed to generate flow: tenantId={}", params.getTenantId(), e);
        SseUtil.sendJson(emitter, ChatMessageType.ERROR, ExpUtil.getMsg(e));
      }
      finally {
        SseUtil.completeQuietly(emitter);
      }
    });
  }

  @PostMapping("saveFlow")
  @Operation(summary = "智能生成流程并保存", description = "提供给外部调用")
  public ResultVO<SkillFlowDTO> saveFlow(@RequestBody SaveFlowRequestDTO request) {
    Assert.notNull(request.getTenantId(), "租户 ID 不能为空");
    Assert.hasLength(request.getPrompt(), "功能描述不能为空");
    ResultVO<SkillFlowDTO> result = flowAiHelper.saveFlow(request);
    if (result.isSuccess()) {
      // 修改流程时刷新缓存
      if (request.getFlowId() != null) {
        refreshCacheService.refresh(CacheConsts.CACHE_NAME_FLOW_DSL, request.getTenantId() + ":" + request.getFlowId());
      }
      // 只返回部分基本属性
      SkillFlowDTO flow = result.getResultObject();
      SkillFlowDTO newFlow = new SkillFlowDTO();
      newFlow.setFlowId(flow.getFlowId());
      newFlow.setFlowName(flow.getFlowName());
      newFlow.setFlowCode(flow.getFlowCode());
      result.setResultObject(newFlow);
    }
    return result;
  }
}
