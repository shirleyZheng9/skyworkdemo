package com.iwhalecloud.bote.controller.portal;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.portal.DingTalkParameterDTO;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.portal.config.properties.DingTalkLoginProperties;
import com.iwhalecloud.bote.portal.support.DingtalkAuthSupport;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 集成钉钉登录能力，可选
 *
 * @author Aiqing
 * @since 2025/6/4
 */
@IgnoreSign
@IgnoreSession
@Tag(name = "门户：博特门户")
@RequestMapping(path = BaseConsts.API_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class DingtalkAuthController {

  private static final String SYSTEM_CODE_DINGTALK = "dingTalk";

  private final DingtalkAuthSupport dingtalkAuthSupport;
  private final DingTalkLoginProperties dingTalkLoginProperties;

  @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
  public DingtalkAuthController(ObjectProvider<DingtalkAuthSupport> dingtalkAuthSupport, ObjectProvider<DingTalkLoginProperties> dingTalkLoginProperties) {
    this.dingtalkAuthSupport = dingtalkAuthSupport.getIfAvailable();
    this.dingTalkLoginProperties = dingTalkLoginProperties.getIfAvailable();
  }

  /**
   * 集成钉钉登录
   * <p>
   * 钉钉发起登录有两种情况：
   * 1. 在浏览器打开，需要跳转钉钉授权页，用户授权后调用本接口进行登录
   * 2. 在钉钉内部打开，此时不需要用户授权
   * </p>
   * 两种情况通过临时授权码获取用户信息的逻辑有所区别
   * 官方文档： <a href="https://open.dingtalk.com/document/orgapp/tutorial-obtaining-user-personal-information">钉钉文档</a>
   *
   * @param request   request对象
   * @param response  response对象
   * @param code      钉钉临时授权码
   * @param tenantId  当前访问租户ID
   * @param innerAuth 是否是钉钉内部免登
   */
  @Operation(summary = "集成钉钉登录")
  @PostMapping("dingTalkLogin")
  public ResultVO<LoginInfo> login(HttpServletRequest request,
                                   HttpServletResponse response,
                                   @RequestParam("code") String code,
                                   @RequestParam("tenantId") Long tenantId,
                                   @RequestParam("innerAuth") Boolean innerAuth) {
    if (Objects.isNull(dingtalkAuthSupport)) {
      throw new BssException("当前环境不支持钉钉登录");
    }
    return dingtalkAuthSupport.login(request, response, tenantId, code, innerAuth, SYSTEM_CODE_DINGTALK);
  }

  @Operation(summary = "钉钉配置参数")
  @GetMapping("/dingTalk/configs")
  public ResultVO<DingTalkParameterDTO> queryConfigs() {
    DingTalkParameterDTO parameterDTO = new DingTalkParameterDTO();
    if (Objects.nonNull(dingTalkLoginProperties)) {
      parameterDTO.setCorpId(dingTalkLoginProperties.getCorpId());
    }
    return ResultVO.success(parameterDTO);
  }

  @Operation(summary = "钉钉认证登录页URL")
  @GetMapping("/dingTalk/oauthUrl")
  public ResultVO<DingTalkParameterDTO> dingTalkOauthUrl() {
    DingTalkParameterDTO parameterDTO = new DingTalkParameterDTO();
    if (Objects.nonNull(dingtalkAuthSupport)) {
      parameterDTO.setOauthUrl(dingtalkAuthSupport.getLoginUrl());
    }
    return ResultVO.success(parameterDTO);
  }


}
