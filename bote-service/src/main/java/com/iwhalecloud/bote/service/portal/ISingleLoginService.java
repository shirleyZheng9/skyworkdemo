package com.iwhalecloud.bote.service.portal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * 单点登录访问博特指定页面
 *
 * @author wangtingyun
 * @since 2026-02-07
 */
public interface ISingleLoginService {

  /**
   * 单点登录并跳转
   * <p>访问示例：http://10.45.136.98:8080/api/bote/single?token={encrypt}&redirect=/pluginConfig </p>
   *
   * @param accessToken 加密的用户鉴权信息，格式：AES加密的JSON字符串，包含用户的 API KEY
   * @param redirect 重定向的前端页面路径
   * @param queryParams 其它查询参数，用于构造重定向 URL
   * @param request 请求
   * @param response 响应
   */
  void single(String accessToken, String redirect, Map<String, String> queryParams,
              HttpServletRequest request, HttpServletResponse response) throws IOException;

}
