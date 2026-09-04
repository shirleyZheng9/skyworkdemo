package com.iwhalecloud.bote.controller.a2a;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.cache.A2aNotificationContextCache;
import com.iwhalecloud.bote.cache.FlowDslCache;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.a2a.A2aNotificationContext;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.orchestration.SceneDslDTO;
import com.iwhalecloud.bote.service.orchestration.IOrchestrationEngine;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.a2a.common.A2AHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * A2A 通知接口
 *
 * <p>用于接收 A2A 服务器发送的通知，需要忽略鉴权和签名，使用 A2A 的通知鉴权机制。</p>
 *
 * @author bianjp
 * @since 2025-11-27
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "a2a/notification", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@IgnoreSession
@IgnoreSign
@Tag(name = "A2A 通知接口")
public class A2aNotificationController {
  private static final Logger logger = LoggerFactory.getLogger(A2aNotificationController.class);

  private final IOrchestrationEngine orchestrationEngine;
  private final FlowDslCache flowDslCache;
  private final A2aNotificationContextCache notificationContextCache;

  @PostMapping("triggerFlow")
  @Operation(summary = "触发工作流")
  @SuppressWarnings("PMD.GuardLogStatement")
  public void triggerFlow(@RequestParam("tenantId") Long tenantId,
                          @RequestParam("flowId") Long flowId,
                          @RequestHeader(A2AHeaders.X_A2A_NOTIFICATION_TOKEN) String token,
                          // 使用 JsonNode 以方便记录日志时自动转为 JSON 字符串
                          @RequestBody JsonNode body) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(flowId, "流程 ID 不能为空");
    Assert.hasLength(token, "通知令牌不能为空");
    logger.debug("Received a2a notification: tenantId={}, flowId={}, token={}, body={}", tenantId, flowId, token, body);

    A2aNotificationContext context = notificationContextCache.getContext(token);
    Assert.notNull(context, "通知令牌无效");
    // 参数中的 tenantId, flowId 有助于统计、排查问题，但要防止滥用，校验一下和创建 token 时的信息是否一致
    Assert.isTrue(tenantId.equals(context.getTenantId()), "非法的通知令牌");
    Assert.isTrue(flowId.equals(context.getFlowId()), "非法的通知令牌");
    // 设置登录信息。SessionInterceptor 会清理，这里不用清理
    SessionUtil.setLoginInfo(context.getLoginInfo());

    SceneDslDTO dsl = flowDslCache.getDsl(tenantId, flowId, true);
    Assert.isTrue(Boolean.FALSE.equals(dsl.getChatflow()), () -> "流程不是任务型: " + flowId);

    // 异步执行，避免阻塞 A2A 服务器、避免 HTTP 超时
    ThreadPools.getOrchestration().submit(() -> {
      OrchestrationEngineRequest request = new OrchestrationEngineRequest();
      request.setTenantId(tenantId);
      request.setFlowId(flowId);
      request.setParameters(Map.of("task", JsonUtil.convert(body, Map.class)));

      OrchestrationEngineResponse response = orchestrationEngine.run(request);

      if (!Boolean.TRUE.equals(response.getSuccess())) {
        logger.warn("Failed to execute A2A notification flow: tenantId={}, flowId={}, error={}, request={}",
          tenantId, flowId, response.getFailMsg(), body);
      }
    });
  }

}
