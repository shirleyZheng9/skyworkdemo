package com.iwhalecloud.bote.controller.mcp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.cache.McpClientCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.util.JsonSchemaUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.mcp.McpServerDTO;
import com.iwhalecloud.bote.dto.mcp.McpServerQueryParams;
import com.iwhalecloud.bote.dto.mcp.SimpleMcpServiceDTO;
import com.iwhalecloud.bote.dto.mcp.query.McpQueryParams;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import com.iwhalecloud.bote.mcp.client.McpClient;
import com.iwhalecloud.bote.mcp.dto.McpTool;
import com.iwhalecloud.bote.mcp.dto.request.CallToolRequest;
import com.iwhalecloud.bote.mcp.dto.response.CallToolResult;
import com.iwhalecloud.bote.mcp.dto.response.InitializeResult;
import com.iwhalecloud.bote.service.mcp.IMcpServerManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
 * MCP 服务管理
 *
 * @author auto
 * @since 2025-05-13
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/mcp", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "MCP 服务管理")
public class McpServerManageController {

  private final IMcpServerManageService mcpServerManageService;
  private final McpClientCache mcpClientCache;
  private final IRefreshCacheService refreshCacheService;

  @Operation(summary = "查询单个 MCP 服务")
  @GetMapping("findMcpServer")
  public ResultVO<McpServerDTO> findMcpServer(@RequestParam(value = "tenantId", required = false) Long tenantId,
                                              @RequestParam(name = "serverId") Long serverId) {
    Assert.notNull(serverId, "主键 ID 不能为空");
    McpServerDTO mcpServer = mcpServerManageService.findMcpServer(tenantId, serverId);
    mcpServer.parseJsonConfig();
    return ResultVO.success(mcpServer);
  }

  @Operation(summary = "保存 MCP 服务")
  @PostMapping("saveMcpServer")
  public ResultVO<McpServerDTO> saveMcpServer(@RequestBody McpServerDTO mcpServer) {
    boolean isUpdate = mcpServer.getServerId() != null;
    mcpServer.saveJsonConfig();
    ResultVO<McpServerDTO> result = mcpServerManageService.saveMcpServer(mcpServer);
    if (isUpdate && result.isSuccess()) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_MCP_CLIENT, mcpServer.getTenantId() + ":" + mcpServer.getServerId());
    }
    return result;
  }

  @Operation(summary = "删除 MCP 服务")
  @GetMapping("deleteMcpServer")
  public ResultVO<Void> deleteMcpServer(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam(name = "serverId") Long serverId) {
    Assert.notNull(serverId, "主键 ID 不能为空");
    ResultVO<Void> result = mcpServerManageService.deleteMcpServer(tenantId, serverId);
    if (result.isSuccess()) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_MCP_CLIENT, tenantId + ":" + serverId);
    }
    return result;
  }

  @Operation(summary = "查询 MCP 服务列表")
  @PostMapping("queryMcpServerList")
  public ResultVO<List<McpServerDTO>> queryMcpServerList(@RequestBody McpServerQueryParams queryParams) {
    return ResultVO.success(mcpServerManageService.queryMcpServerList(queryParams));
  }

  @Operation(summary = "分页查询 MCP 服务")
  @PostMapping("queryMcpServerPage")
  public ResultVO<PageInfo<McpServerDTO>> queryMcpServerPage(@RequestBody McpServerQueryParams queryParams) {
    return ResultVO.success(mcpServerManageService.queryMcpServerPage(queryParams));
  }

  @Operation(summary = "分页查询 MCP 服务基本信息", description = "用于智能体中的技能选择弹窗")
  @PostMapping("querySimpleMcpServerPage")
  public ResultVO<PageInfo<SimpleMcpServiceDTO>> querySimpleMcpServerPage(@RequestBody McpServerQueryParams queryParams) {
    return ResultVO.success(mcpServerManageService.querySimpleMcpServerPage(queryParams));
  }

  @Operation(summary = "发布 MCP 服务")
  @GetMapping("publishMcpServer")
  public ResultVO<Void> publishMcpServer(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam("serverId") Long serverId,
                                         @RequestParam("serverEffect") String serverEffect) {
    ResultVO<Void> result = mcpServerManageService.publishMcpServer(tenantId, serverId, serverEffect);
    if (result.isSuccess()) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_MCP_CLIENT, tenantId + ":" + serverId);
    }
    return result;
  }

  @Operation(summary = "获取 MCP 服务的工具列表")
  @PostMapping("getMcpTools")
  public ResultVO<List<McpToolWithParameters>> getMcpTools(@RequestBody McpQueryParams queryParams) {
    if (StringUtils.isEmpty(queryParams.getParams())) {
      queryParams.setParams("schema");
    }
    McpClient client = mcpClientCache.getMcpClient(queryParams.getTenantId(), queryParams.getServerId(), false);
    List<McpTool> tools = client.listAllToolsWithCache();
    List<McpToolWithParameters> finalTools = new ArrayList<>(tools.size());
    if ("none".equals(queryParams.getParams())) {
      for (McpTool tool : tools) {
        finalTools.add(new McpToolWithParameters(tool.getName(), tool.getDescription()));
      }
    }
    else if ("spec".equals(queryParams.getParams())) {
      for (McpTool tool : tools) {
        ParameterSpec parameters = JsonSchemaUtil.parseRootSchema(tool.getInputSchema());
        finalTools.add(new McpToolWithParameters(tool.getName(), tool.getDescription(), parameters));
      }
    }
    else {
      for (McpTool tool : tools) {
        finalTools.add(new McpToolWithParameters(tool.getName(), tool.getDescription(), tool.getInputSchema()));
      }
    }
    return ResultVO.success(finalTools);
  }

  @Operation(summary = "测试 MCP 服务")
  @PostMapping("testMcpService")
  public ResultVO<InitializeResult> testMcpService(@RequestBody SimpleMcpServiceDTO server) {
    try (McpClient client = McpClientCache.buildMcpClient(server)) {
      return ResultVO.success(client.getInitializeResult());
    }
  }

  @Operation(summary = "测试调用 MCP 工具")
  @PostMapping("callMcpTool")
  public ResultVO<CallToolResult> callMcpTool(@RequestBody TestMcpToolParams params) {
    Assert.notNull(params.getTenantId(), "租户 ID 不能为空");
    Assert.notNull(params.getServerId(), "服务 ID 不能为空");
    Assert.hasLength(params.getToolName(), "工具名称不能为空");
    McpClient client = mcpClientCache.getMcpClient(params.getTenantId(), params.getServerId(), false);
    CallToolResult callToolResult = client.callTool(new CallToolRequest(params.getToolName(), params.getToolParams()));
    return ResultVO.success(callToolResult);
  }

  @Getter
  @Setter
  @ToString
  @JsonInclude(Include.NON_NULL)
  @Schema(description = "MCP 工具(含参数)")
  public static class McpToolWithParameters {
    @Schema(description = "工具名称")
    private String name;
    @Schema(description = "工具描述")
    private String description;
    @Schema(description = "工具入参")
    private JsonSchemaNode inputSchema;
    @Schema(description = "入参（虚拟根节点）")
    private ParameterSpec parameters;

    public McpToolWithParameters(String name, String description) {
      this.name = name;
      this.description = description;
    }

    public McpToolWithParameters(String name, String description, JsonSchemaNode inputSchema) {
      this.name = name;
      this.description = description;
      this.inputSchema = inputSchema;
    }

    public McpToolWithParameters(String name, String description, ParameterSpec parameters) {
      this.name = name;
      this.description = description;
      this.parameters = parameters;
    }
  }

  /**
   * 测试调用 MCP 工具请求
   */
  @Getter
  @Setter
  @ToString
  public static class TestMcpToolParams {
    /** 租户 ID */
    private Long tenantId;
    /** 服务 ID */
    private Long serverId;
    /** 工具名称 */
    private String toolName;
    /** 工具参数，可选 */
    private Map<String, Object> toolParams;
  }
}


