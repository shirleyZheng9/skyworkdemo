package com.iwhalecloud.bote.doc.module.collaboration.doc.controller;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.common.tenant.annotation.IgnoreTenant;
import com.iwhalecloud.bote.doc.module.collaboration.cache.SocketServerCache;
import com.iwhalecloud.bote.doc.module.collaboration.constant.ConnectTypeEnum;
import com.iwhalecloud.bote.doc.module.collaboration.doc.ro.SocketTokenVerifyRO;
import com.iwhalecloud.bote.doc.module.collaboration.doc.vo.SocketTokenVO;
import com.iwhalecloud.bote.doc.module.collaboration.socket.auth.TokenValidator;
import com.iwhalecloud.bote.doc.module.collaboration.socket.auth.UserAuthInfo;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.sequence.UUIDUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * socket 链接认证
 *
 * @author Aiqing
 * @since 2025/8/30
 */
@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "dc/socket/token", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "文档中心-在线协作")
public class SocketAuthController {

  private final IDcUserService dcUserService;
  private final SocketServerCache socketServerCache;
  private final TokenValidator tokenValidator;

  @Operation(summary = "签发socket连接token")
  @PostMapping("issue")
  @IgnoreTenant
  public ResultVO<SocketTokenVO> issueSocketToken() {
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
    PortalUserDTO portalUserDTO = dcUserService.findUserById(currentLoginUserId);
    if (portalUserDTO == null) {
      return ResultVO.fail("用户不存在");
    }
    SocketTokenVO socketAuthToken = socketServerCache.getUserExistSocketAuthToken(currentLoginUserId);
    if (socketAuthToken != null) {
      return ResultVO.success(socketAuthToken);
    }
    SocketTokenVO socketTokenVO = issueToken(currentLoginUserId, portalUserDTO);
    return ResultVO.success(socketTokenVO);
  }

  private SocketTokenVO issueToken(Long currentLoginUserId, PortalUserDTO portalUserDTO) {
    UserAuthInfo authInfo = new UserAuthInfo();
    authInfo.setUserId(currentLoginUserId);
    authInfo.setUserCode(portalUserDTO.getUserCode());
    authInfo.setUserName(portalUserDTO.getUserName());
    authInfo.setIssuedTime(LocalDateTime.now());
    // 默认一天的有效期
    authInfo.setExpireTime(LocalDateTime.now().plusHours(SocketServerCache.TOKEN_EXPIRE_HOURS));

    String token = UUIDUtils.randomFormatUuid();
    socketServerCache.setUserSocketAuthToken(token, authInfo, currentLoginUserId);
    long expireMills = authInfo.getExpireTime()
      .atZone(ZoneId.systemDefault())
      .toInstant()
      .toEpochMilli();
    return new SocketTokenVO(token, expireMills);
  }

  @Operation(summary = "验证socket token有效性")
  @PostMapping("/verify")
  @IgnoreTenant
  @IgnoreSession
  public ResultVO<UserAuthInfo> verifyToken(@RequestBody SocketTokenVerifyRO verifyRO) {
    UserAuthInfo authInfo = tokenValidator.validateToken(verifyRO.getToken(), ConnectTypeEnum.OUTER.name());
    if (authInfo == null) {
      return ResultVO.fail("401", "无效token", null, null);
    }
    return ResultVO.success(authInfo);
  }
}
