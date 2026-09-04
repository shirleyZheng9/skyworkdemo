package com.iwhalecloud.bote.controller.portal;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.cache.ApiAuthCache;
import com.iwhalecloud.bote.common.annotation.RequireOAuth2;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.base.SimpleApiAuthDTO;
import com.iwhalecloud.bote.dto.portal.UserDTO;
import com.iwhalecloud.bote.dto.portal.query.UserBatchQueryParam;
import com.iwhalecloud.bote.service.portal.IUserManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用于服务端调用的用户开放接口， 需要传递Oauth认证的token
 *
 * @author Aiqing
 * @since 2025/12/26
 */
@RequiredArgsConstructor
@RestController
@RequireOAuth2
@RequestMapping(BaseConsts.API_PREFIX + "open/user")
@Tag(name = "开放接口：用户")
@IgnoreSession
public class UserOpenController {

  private final IUserManageService userManageService;
  private final ApiAuthCache apiAuthCache;


  @GetMapping("queryUserById")
  @Operation(summary = "查询单个用户")
  public ResultVO<UserDTO> queryUserById(@RequestParam("userId") Long userId) {
    Assert.notNull(userId, "用户 ID 不能为空");
    UserDTO user = userManageService.findUser(userId, false);
    // 避免将密码返回给前端
    user.setPassword(null);
    return ResultVO.success(user);
  }

  @PostMapping("queryUserBatchById")
  @Operation(summary = "查询批量用户")
  public ResultVO<List<UserDTO>> queryUserBatchById(@RequestBody @Validated UserBatchQueryParam queryParam) {
    List<UserDTO> dtoList = userManageService.findBatchUser(queryParam.getUserIdList());
    // 避免将密码返回给前端
    dtoList.forEach(user -> {
      user.setPassword(null);
    });
    return ResultVO.success(dtoList);
  }

  @Operation(summary = "根据apiKey查询用户ID")
  @GetMapping("queryUserIdByApiKey")
  public ResultVO<Long> queryUserIdByApiKey(@RequestParam String apiKey) {
    SimpleApiAuthDTO apiAuth = apiAuthCache.getApiAuth(apiKey);
    if (apiAuth == null) {
      return ResultVO.success(null);
    }
    return ResultVO.success(apiAuth.getUserId());
  }
}
