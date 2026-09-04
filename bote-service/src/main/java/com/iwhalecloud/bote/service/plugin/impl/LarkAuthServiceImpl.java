package com.iwhalecloud.bote.service.plugin.impl;

import com.iwhalecloud.bote.cache.LarkUserAccessTokenCache;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.AesUtil;
import com.iwhalecloud.bote.common.util.LarkAuthUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.plugin.lark.LarkSateDTO;
import com.iwhalecloud.bote.dto.plugin.lark.LarkUserAccessTokenDTO;
import com.iwhalecloud.bote.dto.plugin.lark.LarkUserAuthInfoDTO;
import com.iwhalecloud.bote.service.plugin.ILarkAuthService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 飞书授权服务实现
 *
 * @author qian.sisheng
 * @since 2025-08-21
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class LarkAuthServiceImpl implements ILarkAuthService {

  private final Logger logger = LoggerFactory.getLogger(LarkAuthServiceImpl.class);
  private final LarkUserAccessTokenCache larkUserAccessTokenCache;

  @Override
  public ResultVO<Void> callback(String code, String state) {
    // 验证并解析state参数
    LarkSateDTO larkSate = validateAndParseState(state);
    // 保存用户授权信息
    LarkAuthUtil.saveUserAccessToken(code, larkSate);
    return ResultVO.success();
  }

  /**
   * 验证并解析state参数
   *
   * @param state state参数
   * @return 解析后的state参数
   */
  private LarkSateDTO validateAndParseState(String state) {
    if (StringUtils.isEmpty(state)) {
      throw new BssException("state参数不能为空");
    }
    String larkSate = AesUtil.aesDecrypt(state, SystemParameter.ENCRYPTION_AES.getValueFromDb());
    if (StringUtils.isEmpty(larkSate)) {
      throw new BssException("无效的state参数");
    }
    return JsonUtil.parseJsonRequired(larkSate, LarkSateDTO.class);
  }

  @Override
  public LarkUserAuthInfoDTO getUserAuthInfo() {
    LarkUserAuthInfoDTO userInfo = new LarkUserAuthInfoDTO();
    try {
      String userAccessToken = larkUserAccessTokenCache.get(SessionUtil.getLoginInfo().getUserId());
      if (StringUtils.isEmpty(userAccessToken)) {
        throw new BssException("用户授权已过期，请重新授权");
      }
      return JsonUtil.parseJsonRequired(userAccessToken, LarkUserAccessTokenDTO.class).getUserInfo();
    }
    catch (Exception e) {
      logger.error("获取用户授权信息异常, msg={}", e.getMessage(), e);
      userInfo.setAuthSuccess(false);
      return userInfo;
    }
  }

  @Override
  public void cancelLarkAuth() {
    larkUserAccessTokenCache.delete(SessionUtil.getLoginInfo().getUserId());
  }

}
