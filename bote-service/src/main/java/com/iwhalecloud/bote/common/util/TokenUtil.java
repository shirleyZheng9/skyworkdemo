package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Token 工具类
 *
 * @author tingyun.wang
 * @since 2026-02-10
 */
public final class TokenUtil {

  private TokenUtil() {
  }

  /**
   * 构造 accessToken
   *
   * <p>通过 AES 加密 content 字符串，再 Base64URL 编码，生成 accessToken</p>
   */
  @Nullable
  public static String buildAccessToken(String content) {
    Map<String, Object> map = new HashMap<>();
    map.put("key", content);
    String encrypt = AesUtil.aesEncrypt(JsonUtil.toJsonString(map), SystemParameter.ENCRYPTION_AES.getValueFromDb());
    if (StringUtils.isEmpty(encrypt)) {
      return null;
    }
    // 替换特殊字符：+ -> -, / -> _
    String urlSafe = encrypt.replace('+', '-').replace('/', '_');
    // 移除末尾的填充符 =
    return urlSafe.replaceAll("=+$", "");
  }

}
