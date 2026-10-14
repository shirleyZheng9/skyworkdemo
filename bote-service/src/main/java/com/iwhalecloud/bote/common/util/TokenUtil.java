package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.cmsr.tiangong.util.JsonUtil;
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
    return buildAccessToken(content, null);
  }

  /**
   * 构造 accessToken，并将发布记录 ID 一并加密在其中
   *
   * <p>publishId 加密在 accessToken 中，服务端校验发布链接是否有效时以此为准，
   * 链接上的参数被删除或篡改都不会影响校验结果</p>
   *
   * @param content 加密内容（用户密钥）
   * @param publishId 发布记录 ID，为空时不携带
   */
  @Nullable
  public static String buildAccessToken(String content, @Nullable Long publishId) {
    Map<String, Object> map = new HashMap<>();
    map.put("key", content);
    if (publishId != null) {
      map.put("publishId", publishId);
    }
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
