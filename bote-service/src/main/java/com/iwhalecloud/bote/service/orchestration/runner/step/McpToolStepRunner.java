package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.cache.McpClientCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.McpToolStep;
import com.iwhalecloud.bote.mcp.client.McpClient;
import com.iwhalecloud.bote.mcp.dto.request.CallToolRequest;
import com.iwhalecloud.bote.mcp.dto.response.CallToolResult;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * MCP 工具步骤执行器
 *
 * @author bianjp
 * @since 2025-07-14
 */
public class McpToolStepRunner extends AbstractStepRunner<McpToolStep> {
  private static final McpClientCache mcpClientCache = SpringUtil.getBean(McpClientCache.class);

  @Override
  protected void doRun(SceneOrchestrationContext context, McpToolStep step) {
    Long tenantId = Boolean.TRUE.equals(step.getPlatform()) ? BaseConsts.PLATFORM_TENANT_ID : context.getTenantId();
    McpClient client = mcpClientCache.getMcpClient(tenantId, step.getServerId(), true);
    Map<String, Object> parameters = buildRequestParametersToMap(step.getParameters());
    Optional<OrchestrationStepRunLog> log = context.getLastStepRunLogOptional();
    log.ifPresent(l -> {
      Map<String, Object> input = new LinkedHashMap<>();
      input.put("serverId", step.getServerId());
      input.put("serverName", client.getName());
      input.put("tool", step.getToolName());
      input.put("parameters", parameters);
      l.setInput(input);
    });
    CallToolResult result = client.callTool(new CallToolRequest(step.getToolName(), parameters));
    context.setStepOutput(step, JsonUtil.convert(result, new TypeReference<Map<String, Object>>() {
    }));
  }

}
