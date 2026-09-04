package com.iwhalecloud.bote.controller.publish;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.service.publish.IPublishWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发布平台回调控制器
 *
 * @author system
 * @since 2025-01-09
 */
@RestController
@RequestMapping(BaseConsts.API_PREFIX + "publish/webhook")
@RequiredArgsConstructor
@Tag(name = "发布平台回调", description = "处理第三方平台的消息回调")
@IgnoreSign
@IgnoreSession
public class PublishWebhookController {

  private final IPublishWebhookService publishWebhookService;

  /**
   * 统一回调接口
   *
   * @param callbackCode 回调编码
   * @param request HTTP请求
   * @return 响应结果
   */
  @PostMapping(path = "/{callbackCode}", produces = MediaType.TEXT_XML_VALUE)
  @Operation(summary = "处理第三方平台回调消息")
  public String handleWebhook(
    @Parameter(description = "回调编码") @PathVariable("callbackCode") String callbackCode,
    HttpServletRequest request, @RequestBody String encryptedMsg) {
    return publishWebhookService.handleWebhook(callbackCode, request, encryptedMsg);
  }


  /**
   * 验证回调地址
   *
   * @param callbackCode 回调编码
   * @param request HTTP请求
   * @return 验证结果
   */
  @GetMapping("/{callbackCode}")
  @Operation(summary = "验证回调地址")
  public String verifyWebhook(
    @PathVariable("callbackCode") String callbackCode,
    HttpServletRequest request) {
    return publishWebhookService.verifyWebhook(callbackCode, request);
  }
}
