package com.iwhalecloud.bote.controller.plugin;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.dto.plugin.lark.LarkUserAuthInfoDTO;
import com.iwhalecloud.bote.service.plugin.ILarkAuthService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 飞书授权回调接口
 *
 * @author qian.sisheng
 * @since 2025-08-19
 */
@RequestMapping(value = BaseConsts.API_PREFIX + "lark", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class LarkAuthController {

  private final Logger logger = LoggerFactory.getLogger(LarkAuthController.class);
  private final ILarkAuthService larkAuthService;
  /** 博特前端网关地址 */
  private static final String BOTE_FRONT_URL = SystemParameter.BOTE_WEB_URL.getValueFromEnv();
  /** 飞书授权跳转的页面地址 */
  private static final String LARK_AUTH_PAGE_URL = StringUtils.stripEnd(BOTE_FRONT_URL, "/") + "/manager/#/boteAuth";
  /** 飞书授权被拒绝的页面地址 */
  private static final String LARK_AUTH_REJECT_PAGE_URL = LARK_AUTH_PAGE_URL + "/reject";
  /** 飞书授权失败的页面地址 */
  private static final String LARK_AUTH_FAILED_PAGE_URL = LARK_AUTH_PAGE_URL + "/error";

  @IgnoreSession
  @IgnoreSign
  @GetMapping("/callback")
  @Operation(summary = "飞书授权回调接口")
  public ResponseEntity<Void> callback(@RequestParam(value = "code", required = false) String code, @RequestParam("state") String state,
    @RequestParam(value = "error", required = false) String error) {
    try {
      // 授权被拒绝, 当前 error 参数的固定值为 access_denied
      if ("access_denied".equals(error)) {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(LARK_AUTH_REJECT_PAGE_URL)).build();
      }
      larkAuthService.callback(code, state);
      return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(LARK_AUTH_PAGE_URL)).build();
    }
    catch (Exception e) {
      logger.error("Failed to auth lark, msg={}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(LARK_AUTH_FAILED_PAGE_URL)).build();
    }
  }

  @GetMapping("/getUserAuthInfo")
  @Operation(summary = "获取用户授权信息")
  public ResultVO<LarkUserAuthInfoDTO> getUserAuthInfo() {
    return ResultVO.success(larkAuthService.getUserAuthInfo());
  }

  @GetMapping("/cancelLarkAuth")
  @Operation(summary = "取消用户授权")
  public ResultVO<Void> cancelLarkAuth() {
    larkAuthService.cancelLarkAuth();
    return ResultVO.success();
  }
}
