package com.iwhalecloud.bote.service.security;

import com.iwhalecloud.bassc.basiccenter.service.ILitchiAuthSkipService;
import com.iwhalecloud.bote.common.util.BoteAuthUtil;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 是否跳过Litchi-Security模块的鉴权：
 * Litchi-Security鉴权模块会对Cookie的Session信息做登录校验，但有些情况，比如通过 token 实现自定义鉴权，
 * 则需要由使用Litchi-Security模块的平台做鉴权处理，因此通过实现ILitchiAuthSkipService服务来告诉鉴权模块是否需要跳过鉴权
 *
 * @author tingyun.wang
 * @since 2025-07-25
 */
@Component
public class LitchiAuthSkipServiceImpl implements ILitchiAuthSkipService {

  @Override
  public boolean shouldSkipAuth(HttpServletRequest request) {
    return BoteAuthUtil.shouldSkipAuth(request);
  }

}
