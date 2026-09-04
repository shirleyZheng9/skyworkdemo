package com.iwhalecloud.bote.controller.wechat;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.wechat.WechatMsgDTO;
import com.iwhalecloud.bote.service.wechat.IWechatProxyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统一代理入口控制器（平台无关，当前落地：微信公众号）
 *
 * <p>职责：
 * - 提供 GET 验证（微信服务器校验 URL 所在回调）；
 * - 提供 POST 事件/消息入口（签名校验后转服务层处理）。
 *
 * @author lizuyin
 * @since 2025-08-08
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "platform/proxy")
@Tag(name = "微信统一代理入口")
@RequiredArgsConstructor
public class WechatProxyController {

  private final IWechatProxyService wechatProxyService;

  /**
   * 微信服务器 GET 验证（原样返回 echostr）
   *
   * @param code 智能体编码
   * @param signature 签名
   * @param timestamp 时间戳
   * @param nonce 随机串
   * @return 验证通过返回 echostr，否则返回 "forbidden"
   */
  @IgnoreSession
  @IgnoreSign
  @GetMapping(path = "/wechat/{code}")
  @Operation(summary = "微信GET验证：回显echoStr")
  public ResponseEntity<String> wechatEchoVerify(
    @PathVariable String code,
    @RequestParam String signature,
    @RequestParam String timestamp,
    @RequestParam String nonce,
    @RequestParam(name = "echostr") String echoStr) {
    WechatMsgDTO msgDTO = new WechatMsgDTO();
    msgDTO.setCode(code);
    msgDTO.setMsgSignature(signature);
    msgDTO.setTimestamp(timestamp);
    msgDTO.setNonce(nonce);
    msgDTO.setEchoStr(echoStr);
    String result = wechatProxyService.wechatEchoVerify(msgDTO);
    return ResponseEntity.ok(result);
  }

  /**
   * 微信消息/事件投递入口（签名校验通过后，快速回包 success）
   *
   * @param code 智能体代码
   * @param msgSignature 签名
   * @param timestamp 时间戳
   * @param nonce 随机串
   * @param openid 发送方 openid（可选）
   * @param xml 请求体原始XML
   * @return 默认返回 "success"，验签失败返回 "forbidden"
   */
  @IgnoreSession
  @IgnoreSign
  @PostMapping(path = "/wechat/{code}")
  @Operation(summary = "微信POST入口：签名校验与快速回包")
  public String wechatProxy(
    @PathVariable String code,
    @RequestParam(name = "msg_signature") String msgSignature,
    @RequestParam String timestamp,
    @RequestParam String nonce,
    @RequestParam(required = false) String openid,
    @RequestBody String xml) {
    WechatMsgDTO msgDTO = new WechatMsgDTO();
    msgDTO.setCode(code);
    msgDTO.setMsgSignature(msgSignature);
    msgDTO.setTimestamp(timestamp);
    msgDTO.setNonce(nonce);
    msgDTO.setOpenid(openid);
    msgDTO.setXml(xml);
    return wechatProxyService.wechatProxy(msgDTO);
  }
}
