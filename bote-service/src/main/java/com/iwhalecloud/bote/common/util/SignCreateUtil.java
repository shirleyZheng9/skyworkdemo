package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.config.properties.SignProperties;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;

/**
 * 签名相关工具
 *
 * @author zhangJun
 * @since 2022/8/12
 **/
public final class SignCreateUtil {

  private SignCreateUtil() {

  }

  private static final SignProperties signProperties = SpringUtil.getBean(SignProperties.class);

  /** 请求头前缀 */
  private static final String HEADER_NAME_PREFIX = "X-BT-";
  /** 需要获取加入签名的header */
  private static final List<String> HEADER_NAMES = new ArrayList<>();

  static {
    HEADER_NAMES.add("X-B-AUTH");
    HEADER_NAMES.add("X-B-TARGET-ID");
  }

  /**
   * 创建get请求的签名
   *
   * @param urlStr 请求url
   * @return 签名
   */
  public static String createGetSign(String urlStr, @Nullable String queryString) {
    String content = structureContent(HttpMethod.GET.name(), urlStr, String.valueOf(SessionUtil.getLoginInfo().getUserId()), queryString, null, new HashMap<>(), signProperties.getSecretKey());
    return DigestUtils.sha256Hex(content);
  }

  /**
   * 构建待加密的字符串，构建规则：
   * <p>1.get 请求：url+"#"+header+"#"+参数+"#"+userId+"#"+秘钥<p/>
   * <p>2.post 请求：url+"#"+header+"#"+userId+"#"+body+秘钥<p/>
   */
  public static String structureContent(String httpMethod, String url, @Nullable String userId, @Nullable String queryString, @Nullable String body, Map<String, String> headerMap, String secretKey) {
    StringBuilder sb = new StringBuilder();
    sb.append(getUrlLastPath(url)).append(BaseConsts.HASH).append(getHeaderStr(headerMap)).append(BaseConsts.HASH);
    if (HttpMethod.POST.name().equalsIgnoreCase(httpMethod)) {
      sb.append(StringUtils.defaultString(body));
    }
    else if (HttpMethod.GET.name().equalsIgnoreCase(httpMethod)) {
      sb.append(StringUtils.defaultString(queryString));
    }

    sb.append(BaseConsts.HASH);
    if (StringUtils.isNotEmpty(userId)) {
      sb.append(userId);
    }
    sb.append(BaseConsts.HASH).append(secretKey);
    return sb.toString();
  }

  /**
   * 获取需要加入签名的头部信息字符串，规则：
   * <p>X-开头的请求头</p>
   * <p>多个header，先正向排序，分号分隔拼接，header name 转为小写</p>
   */
  private static String getHeaderStr(Map<String, String> headerMap) {
    if (MapUtils.isEmpty(headerMap)) {
      return StringUtils.EMPTY;
    }
    List<String> headers = new ArrayList<>();
    for (Map.Entry<String, String> entry : headerMap.entrySet()) {
      String headerName = entry.getKey();
      if (HEADER_NAMES.contains(headerName.toUpperCase()) || headerName.toUpperCase().startsWith(HEADER_NAME_PREFIX)) {
        headers.add(headerName.toLowerCase() + "=" + entry.getValue());
      }
    }
    if (headers.isEmpty()) {
      return StringUtils.EMPTY;
    }
    Collections.sort(headers);
    return String.join(";", headers);
  }

  /**
   * 从url最后一个/开始截取
   *
   * @param url 请求url
   * @return url
   */
  private static String getUrlLastPath(String url) {
    if (StringUtils.isBlank(url)) {
      return url;
    }
    int indexNum = -1;
      if (url.lastIndexOf("/") != -1) {
        indexNum = Math.max(indexNum, url.lastIndexOf("/")) + 1;
    }
    if (indexNum != -1) {
      return StringUtils.substring(url, indexNum);
    }
    return url;
  }
}
