package com.iwhalecloud.bote.controller.portal;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bassc.basiccenter.util.VerificationCodeUtils;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.portal.adapter.DefaultPortalAuthProvider;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 博特门户接口
 *
 * @author bianjp
 * @since 2024-08-22
 */
@IgnoreSign
@IgnoreSession
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@ConditionalOnProperty(name = "bote.portal.type", havingValue = "default")
@RequiredArgsConstructor
@Tag(name = "门户：博特门户")
public class DefaultPortalController {

  private final DefaultPortalAuthProvider defaultPortalAuthProvider;

  @GetMapping("verificationCode")
  @Operation(summary = "生成图形验证码")
  public void generateCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
    VerificationCodeUtils.generateCode(request, response);
  }

  @GetMapping("smsCode")
  @Operation(summary = "发送短信验证码")
  public ResultVO<String> sendSmsCode(@RequestParam("username") String username, @RequestParam("password") String password) {
    return ResultVO.success(defaultPortalAuthProvider.sendSmsCode(username, password));
  }

}
