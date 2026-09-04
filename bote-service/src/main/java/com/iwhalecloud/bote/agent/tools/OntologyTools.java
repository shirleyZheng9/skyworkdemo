package com.iwhalecloud.bote.agent.tools;

import com.iwhalecloud.bote.agent.annotation.Tool;
import com.iwhalecloud.bote.agent.annotation.ToolParam;
import com.iwhalecloud.bote.agent.annotation.ToolRequest;
import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.agent.tool.exception.ToolExecutionException;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.ServletUtil;
import com.iwhalecloud.bote.dto.ontology.ActionExecuteRequest;
import com.iwhalecloud.bote.dto.ontology.RuleExecuteRequest;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 本体平台内置工具：封装 ontologyQuery/ruleExecute/actionExecute 三个接口
 *
 * @author chen.linfa
 * @since 2026-04-28
 */
public final class OntologyTools {

  private static final String API_ONTOLOGY_QUERY = "/bote/onto/proxy/reason/customQuery/quintupletByScene";

  private static final String API_RULE_EXECUTE = "/bote/ontology/rule/execute";

  private static final String API_ACTION_EXECUTE = "/bote/ontology/action/execute";

  private OntologyTools() {
  }

  @Tool(name = "ontology_scene_query", description = "Query ontology quintuplet by sceneId/appId. Calls /bote/onto/proxy/reason/customQuery/quintupletByScene with env ONTOLOGY_BASE_URL, ONTOLOGY_API_KEY, BOTE_COOKIE.")
  public static String ontologyQuery(@ToolParam(description = "Scene ID (required)") String sceneId,
    @ToolParam(description = "App ID (required)") String appId, @Nullable ToolContext toolContext) {
    Assert.notNull(toolContext, "toolContext is required");
    Assert.hasText(sceneId, "Error: sceneId is required");
    Assert.hasText(appId, "Error: appId is required");
    Map<String, Object> body = Map.of("sceneId", sceneId.trim(), "appId", appId.trim());
    Map<String, Object> response = doPost(API_ONTOLOGY_QUERY, body);
    return JsonUtil.toJsonString(response);
  }

  @Tool(name = "ontology_rule_execute", description = "Execute ontology rules in batch. Request includes appId and rules[]. Each rule has ruleId and inputParams.")
  public static String ontologyRuleExecute(@ToolRequest RuleExecuteRequest request, @Nullable ToolContext toolContext) {
    Assert.notNull(toolContext, "toolContext is required");
    Assert.notNull(request, "Error: request is required");
    Assert.hasText(request.getAppId(), "Error: appId is required");
    Assert.notEmpty(request.getRules(), "Error: rules is required");
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("appId", request.getAppId().trim());
    body.put("rules", request.getRules());
    Map<String, Object> response = doPost(API_RULE_EXECUTE, body);
    return JsonUtil.toJsonString(response);
  }

  @Tool(name = "ontology_action_execute", description = "Execute one ontology action. Request includes actionId, appId, optional requestId, and inputParams.")
  public static String ontologyActionExecute(@ToolRequest ActionExecuteRequest request, @Nullable ToolContext toolContext) {
    Assert.notNull(toolContext, "toolContext is required");
    Assert.notNull(request, "Error: request is required");
    Assert.hasText(request.getActionId(), "Error: actionId is required");
    Assert.hasText(request.getAppId(), "Error: appId is required");
    Assert.notEmpty(request.getInputParams(), "Error: inputParams is required");

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("actionId", request.getActionId().trim());
    body.put("appId", request.getAppId().trim());
    if (StringUtils.isNotEmpty(request.getRequestId())) {
      body.put("requestId", request.getRequestId().trim());
    }
    body.put("inputParams", request.getInputParams());
    Map<String, Object> response = doPost(API_ACTION_EXECUTE, body);
    return JsonUtil.toJsonString(response);
  }

  private static Map<String, Object> doPost(String apiPath, Map<String, Object> body) {
    String baseUrl = SystemParameter.ONTOLOGY_BASE_URL.getValueFromDb();
    String url = StringUtils.stripEnd(baseUrl, "/") + apiPath;
    HttpHeaders headers = buildHeaders();
    try {
      Map<String, Object> response = HttpUtil.post(url, body, new ParameterizedTypeReference<>() {
      }, headers);
      if (response == null) {
        throw new ToolExecutionException("Error: ontology API returned empty response");
      }
      return response;
    }
    catch (ToolExecutionException e) {
      throw e;
    }
    catch (Exception e) {
      throw new ToolExecutionException("Error: " + ExpUtil.getMsg(e), e);
    }
  }

  private static HttpHeaders buildHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpServletRequest request = ServletUtil.getRequest();
    String cookie = request != null ? request.getHeader(HttpHeaders.COOKIE) : null;
    if (StringUtils.isNotEmpty(cookie)) {
      headers.set(HttpHeaders.COOKIE, cookie);
    }
    String apiKey = SystemParameter.ONTOLOGY_API_KEY.getValueFromDb();
    if (StringUtils.isNotEmpty(apiKey)) {
      headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
    }
    return headers;
  }
}
