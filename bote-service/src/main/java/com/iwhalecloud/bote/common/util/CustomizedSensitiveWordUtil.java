package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.service.orchestration.IOrchestrationEngine;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * 自定义敏感词工作流执行工具类
 *
 * @author linemgnfan
 * @since 2026-03-02
 */
public final class CustomizedSensitiveWordUtil {
  private static final IOrchestrationEngine engine = SpringUtil.getBean(IOrchestrationEngine.class);
  private static final TenantSettingInfoCache settingInfoCache = SpringUtil.getBean(TenantSettingInfoCache.class);

  private CustomizedSensitiveWordUtil() {
  }

  /**
   * 检查文本中是否包含敏感词，通过调用之定义的工作流进行设置
   *
   * @param content 文本
   */
  public static void checkSensitive(String content, Long tenantId, String type) {
    // 检查是否配置了敏感词工作流
    Long securityFlowId = settingInfoCache.getSecurityFlowId(tenantId, type);
    if (securityFlowId == null) {
      return;
    }
    OrchestrationEngineRequest request = new OrchestrationEngineRequest();
    request.setTenantId(tenantId);
    request.setFlowId(securityFlowId);
    request.setParameters(Map.of("message", content));
    request.setLogEnabled(true);
    String message = "";
    try {
      OrchestrationEngineResponse response = engine.run(request);
      String resultCode = (String) response.getOutput().get("resultCode");
      if (!"0".equals(resultCode)) {
        message = (String) response.getOutput().get("resultMsg");
      }
    }
    catch (Exception e) {
      message = "安全围栏校验出现异常，" + ExpUtil.getMsg(e);
    }
    Assert.isTrue(StringUtils.isEmpty(message),  message);
  }

}
