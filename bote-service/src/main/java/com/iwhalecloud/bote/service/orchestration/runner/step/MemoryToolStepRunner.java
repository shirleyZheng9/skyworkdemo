package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.MemoryToolStep;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.mapper.portal.TenantManageMapper;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import java.util.Map;
import java.util.Optional;

import com.iwhalecloud.bss.litchi.util.SpringUtil;
import org.springframework.lang.Nullable;

/**
 * 长期记忆工具步骤执行器
 *
 * <p>在简单场景中执行 {@link com.iwhalecloud.bote.agent.tools.MemoryTools} 工具调用。
 * 执行前根据 {@link SceneChatParamsDTO} 构建 {@link ToolContext}，再委托给
 * {@link com.iwhalecloud.bote.agent.tool.callback.ToolCallback} 完成实际调用。</p>
 *
 * @author wangtingyun
 * @since 2026-04-22
 */
public class MemoryToolStepRunner extends AbstractStepRunner<MemoryToolStep> {

  private final TenantManageMapper tenantManageMapper = SpringUtil.getBean(TenantManageMapper.class);

  @Override
  @Nullable
  public Object runAsTool(SceneChatParamsDTO sceneChatParams, MemoryToolStep step, String toolCallId,
      @Nullable Map<String, Object> toolArguments, Optional<OrchestrationStepRunLog> log) {
    ToolContext toolContext = ToolContext.builder()
        .userId(SessionUtil.getLoginInfo().getUserId())
        .botId(sceneChatParams.getBotId())
        .tenantId(sceneChatParams.getTenantId())
        .sceneId(sceneChatParams.getSceneId())
        .spaceId(tenantManageMapper.getSpaceIdByTenantId(sceneChatParams.getTenantId()))
        .build();
    Object result = step.getToolCallback().call(toolArguments, toolContext);
    log.ifPresent(l -> l.succeed(result));
    return result;
  }
}
