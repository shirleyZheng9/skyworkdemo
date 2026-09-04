package com.iwhalecloud.bote.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * DocChain API 工具类
 *
 * @author bianjp
 * @since 2025-07-21
 */
public final class DocChainApiUtil {
  private DocChainApiUtil() {
  }

  /**
   * 提取响应体中的错误信息
   *
   * @param responseBody 响应体
   * @return 错误信息，提取不到时返回 HTTP 状态码和描述
   */
  public static String extractErrorMsg(HttpStatusCodeException httpStatusCodeException, String responseBody) {
    if (!responseBody.startsWith("{") || !responseBody.endsWith("}")) {
      return httpStatusCodeException.toString();
    }
    try {
      JsonNode res = JsonUtil.getObjectMapper().readTree(responseBody);
      // 接口内报错时一般会返回 err, 比如召回接口指定的 topic 不存在
      JsonNode err = res.path("err");
      if (err.isTextual()) {
        return err.asText();
      }
      // JSON 不合法, 或参数校验失败时会返回 detail
      JsonNode detailMsg = res.path("detail").path(0).path("msg");
      if (detailMsg.isTextual()) {
        return detailMsg.asText();
      }
    }
    catch (Exception e) {
      // 忽略解析失败
    }
    return httpStatusCodeException.toString();
  }

}
