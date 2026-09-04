package com.iwhalecloud.bote.service.publish.impl;

import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
import com.iwhalecloud.bote.service.publish.IPublishResourceService;
import com.iwhalecloud.bote.service.publish.IPublishWebhookService;
import com.iwhalecloud.bote.service.publish.SdkConnectionManager;
import com.iwhalecloud.bote.service.publish.platform.PlatformAdapter;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 发布平台回调服务实现
 *
 * @author system
 * @since 2025-01-09
 */
@Service
@RequiredArgsConstructor
public class PublishWebhookServiceImpl implements IPublishWebhookService {

  private final IPublishResourceService publishResourceService;
  private final SdkConnectionManager sdkConnectionManager;

  @Override
  public String handleWebhook(String callbackCode, HttpServletRequest request, String encryptedMsg) {
    // 1. 验证callbackCode有效性
    ResourcePublishRecordDTO record = publishResourceService.getRecordByCallbackCode(callbackCode);
    // 2. 获取平台适配器
    PlatformAdapter adapter = sdkConnectionManager.getAdapter(callbackCode);
    // 3. 委托给平台适配器处理消息
    return adapter.handleWebhookMessage(request, encryptedMsg, record);
  }

  @Override
  public String verifyWebhook(String callbackCode, HttpServletRequest request) {
    // 1. 获取adapter
    PlatformAdapter adapter = sdkConnectionManager.getAdapter(callbackCode);
    // 2. 验证签名
    boolean isValid = adapter.verifySignature(request, null);
    if (!isValid) {
      throw new BssException("签名验证失败");
    }
    // 4. 返回echostr
    return adapter.replyEchoStr(request);
  }

}
