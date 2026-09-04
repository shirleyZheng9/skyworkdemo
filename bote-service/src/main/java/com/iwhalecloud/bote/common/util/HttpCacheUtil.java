package com.iwhalecloud.bote.common.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * HTTP 缓存工具类
 *
 * @author bianjp
 * @since 2025-06-30
 */
public final class HttpCacheUtil {
  private HttpCacheUtil() {
  }

  /**
   * 发送缓存响应头
   *
   * @param request 请求
   * @param response 响应
   * @param digest 内容摘要
   * @return 缓存是否有效
   */
  public static boolean sendCacheHeader(HttpServletRequest request, HttpServletResponse response, String digest) {
    String etag = "W/\"" + digest + "\"";
    // 无论缓存是否有效都需要返回缓存响应头
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.setHeader(HttpHeaders.ETAG, etag);

    // 检查请求头中的 etag 与最新 etag 是否相同，如果相同则表示客户端缓存有效，直接返回 304 状态码
    String oldEtag = request.getHeader(HttpHeaders.IF_NONE_MATCH);
    if (etag.equals(oldEtag)) {
      response.setStatus(HttpStatus.NOT_MODIFIED.value());
      return true;
    }
    return false;
  }

  /**
   * 发送错误响应
   */
  public static void sendError(HttpServletResponse response, Exception e) throws IOException {
    sendError(response, ExpUtil.getMsg(e));
  }

  /**
   * 发送错误响应
   */
  @SuppressFBWarnings("XSS_SERVLET")
  public static void sendError(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    response.setContentType("text/plain;charset=UTF-8");
    response.getWriter().write(message);
  }
}
