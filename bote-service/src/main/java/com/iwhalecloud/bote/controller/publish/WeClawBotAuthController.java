package com.iwhalecloud.bote.controller.publish;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.publish.WeClawBotConfirmLoginRequest;
import com.iwhalecloud.bote.service.publish.IWeClawBotAuthService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人微信（ClawBot）扫码登录管理接口。
 * <p>委托 {@link IWeClawBotAuthService} 处理发布记录、渠道与长连接。</p>
 *
 * @author chen.linfa
 * @since 2026-04-02
 */
@RestController
@RequestMapping(BaseConsts.API_PREFIX + "publish/wechat")
@RequiredArgsConstructor
@Tag(name = "个人微信接入", description = "个人微信（ClawBot）扫码登录与连接管理")
public class WeClawBotAuthController {

  private final IWeClawBotAuthService weClawBotAuthService;

  @GetMapping("/qrcode/{callbackCode}")
  @Operation(summary = "获取个人微信登录二维码")
  public ResultVO<Map<String, Object>> getQrcode(
    @Parameter(description = "回调编码") @PathVariable("callbackCode") String callbackCode) {
    return ResultVO.success(weClawBotAuthService.getQrcode(callbackCode));
  }

  @PostMapping("/confirm/{callbackCode}")
  @Operation(summary = "确认扫码登录并保存 token（成功后自动重启连接）")
  public ResultVO<Map<String, Object>> confirmLogin(
    @Parameter(description = "回调编码") @PathVariable("callbackCode") String callbackCode,
    @RequestBody WeClawBotConfirmLoginRequest request) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    return ResultVO.success(weClawBotAuthService.confirmLogin(callbackCode, request.getQrcode(),
      request.getMaxWaitSeconds(), request.getPollIntervalMs(), userId));
  }

  @PostMapping("/disconnect/{callbackCode}")
  @Operation(summary = "主动断开个人微信长连接（优雅退出，最多等3秒）")
  public ResultVO<Map<String, Object>> disconnect(
    @Parameter(description = "回调编码") @PathVariable("callbackCode") String callbackCode) {
    weClawBotAuthService.disconnect(callbackCode, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

}
