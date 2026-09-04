package com.iwhalecloud.bote.common.interceptor;

import com.iwhalecloud.bote.cache.AttrSpecCache;
import com.iwhalecloud.bote.cache.SignReplayCache;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.BoteAuthUtil;
import com.iwhalecloud.bote.common.util.ServletUtil;
import com.iwhalecloud.bote.common.util.SignCreateUtil;
import com.iwhalecloud.bote.common.util.SignUtil;
import com.iwhalecloud.bote.config.properties.SignProperties;
import com.iwhalecloud.bote.dto.base.SimpleAttrDTO;
import com.iwhalecloud.bss.litchi.util.IPUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;

/**
 * 请求参数签名检验拦截器
 *
 * <p>将 url、参数、秘钥拼接成字符串，计算 hash 值，跟请求头中的 X-SIGN 对比。</p>
 *
 * @author zhangJun
 * @since 2022/3/23
 **/
@SuppressFBWarnings("CRLF_INJECTION_LOGS")
public class SignReqParamInterceptor implements HandlerInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(SignReqParamInterceptor.class);
  /** 静态属性编码：免签名接口 */
  private static final String ATTR_CODE_IGNORE_LOGIN_API = "IGNORE_SIGN_API";
  /** 签名校验失败的响应内容 */
  private static final String FAILED_RESPONSE_TEXT = "{\"resultCode\": \"406\", \"resultMsg\": \"%s\"}";
  /** 路劲匹配器 */
  private static final PathMatcher ANT_PATH_MATCHER = new AntPathMatcher();
  /** 异常页面 */
  private static final String ERROR_URI = "/error";

  private final SignProperties signProperties;
  private final AttrSpecCache attrSpecCache;
  private final SignReplayCache signReplayCache;

  /** 忽略签名的注解 */
  private final List<Class<? extends Annotation>> ignoreAnnotations;

  public SignReqParamInterceptor(SignProperties signProperties, AttrSpecCache attrSpecCache, SignReplayCache signReplayCache) {
    this.signProperties = signProperties;
    this.attrSpecCache = attrSpecCache;
    this.signReplayCache = signReplayCache;
    this.ignoreAnnotations = new ArrayList<>();
    ignoreAnnotations.add(IgnoreSign.class);
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
    // 检查免签名请求
    if (skipReq(request, handler)) {
      return true;
    }

    // 前端通过资源标签(img script link) 访问文件，请求头中带不了签名，会在get请求的查询参数中传递 X-SIGN 和 XA-TYPE
    // 是否移除 get 请求查询参数中的 X-SIGN 和 XA-TYPE 参数标志，移除值，比如：X-SIGN=xxxx&XA-TYPE=1.1
    boolean isRemove = false;
    String removeContent = "";

    //  get 请求查询参数中的 X-SIGN 参数，比如：X-SIGN=7c1fc26b6eeaa34f5f552b8be9b3749307b5aecd11f9c29a6c49a134e9473258
    String signParam;
    // 前端最终传递的 X-SIGN 参数值，比如：7c1fc26b6eeaa34f5f552b8be9b3749307b5aecd11f9c29a6c49a134e9473258
    String signValue = request.getHeader(BaseConsts.HEADER_KEY_SIGN);
    //  get 请求查询参数中的 XA-TYPE 参数，比如：XA-TYPE=1.1
    String securityMode;
    //  前端最终传递的 XA-TYPE 参数值，比如：1.1
    String modeValue = request.getHeader(BaseConsts.HEADER_KEY_SIGN_SECURITY_MODE);

    // 如果 get请求的 header 中不存在 x-sign，则从查询参数中获取 x-sign
    if (StringUtils.isEmpty(signValue) && HttpMethod.GET.matches(request.getMethod()) && StringUtils.isNotEmpty(request.getQueryString())) {
      String queryString = URLDecoder.decode(request.getQueryString(), StandardCharsets.UTF_8);
      // 获取 get 请求查询参数中的 X-SIGN 参数
      signParam = Arrays.stream(queryString.split("&"))
        .filter(p -> Strings.CI.contains(p, BaseConsts.HEADER_KEY_SIGN))
        .findFirst().orElse("");
      // 获取 get 请求查询参数中的 XA-TYPE 参数
      securityMode = Arrays.stream(queryString.split("&"))
        .filter(p -> Strings.CI.contains(p, BaseConsts.HEADER_KEY_SIGN_SECURITY_MODE))
        .findFirst().orElse("");
      // 取出 XA-TYPE 的值
      modeValue = getModeValue(securityMode, modeValue);
      if (StringUtils.isNotEmpty(signParam)) {
        isRemove = true;
        removeContent = queryString.substring(queryString.indexOf(BaseConsts.HEADER_KEY_SIGN));
        // 取出 X-SIGN 的值
        signValue = getSignValue(signParam);
      }
    }
    String signSecurityMode = StringUtils.defaultIfEmpty(modeValue, SystemParameter.SIGN_SECURITY_MODE.getValueFromDb());
    // 安全模式 XA-TYPE 为加解密时，跳过签名校验
    if (Strings.CS.equalsAny(signSecurityMode, BaseConsts.AES, BaseConsts.DES)) {
      return true;
    }

    // 生成待签名字符串
    String sourceStr = structureContent(request, isRemove, removeContent);
    return handleSign(request, response, signValue, signSecurityMode, sourceStr);
  }

  private String getSignValue(String signParam) {
    if (StringUtils.isEmpty(signParam)) {
      return "";
    }
    return signParam.substring(signParam.indexOf("=") + 1);
  }

  private String getModeValue(String securityMode, String modeValue) {
    if (StringUtils.isNotEmpty(securityMode) && StringUtils.isEmpty(modeValue)) {
      modeValue = securityMode.substring(securityMode.indexOf("=") + 1);
    }
    return modeValue;
  }

  @SuppressFBWarnings("XSS_SERVLET")
  @SuppressWarnings("PMD.GuardLogStatement")
  private boolean handleSign(HttpServletRequest request, HttpServletResponse response, String signValue, String signSecurityMode, String sourceStr) throws IOException {
    // 后端加密后生成的签名
    String sign;
    // #10859802 签名安全模式：取前端传递的 XA-TYPE，前端不传取后端配置的签名模式（SIGN_SECURITY_MODE）
    // 安全模式为 1.1 时，X-SIGN 参数为：X-SIGN=7c1fc26b6eeaa34f5f552b8be9b3749307b5aecd11f9c29a6c49a134e9473258.1704475736375.122（[签名串].[请求发起时间].[客户端时间偏差值]）
    // 安全模式为 1.0 时，X-SIGN 参数为：X-SIGN=7c1fc26b6eeaa34f5f552b8be9b3749307b5aecd11f9c29a6c49a134e9473258
    // 签名安全模式为 1.1（时间限制性签名）时
    if (BaseConsts.DEFAULT_SIGN_SECURITY_MODE.equals(signSecurityMode)) {
      String requestParamSign;
      String requestTimestamp;
      try {
        // 从前端传递的 X-SIGN 参数值中取出签名串（X-SIGN的第一段信息）
        requestParamSign = signValue.substring(0, signValue.indexOf("."));
        // 从前端传递的 X-SIGN 参数值中取出请求发起时间（X-SIGN的第二段信息），根据请求发起时间校验签名是否有效，有效则将请求发起时间加入待签名字符串中
        requestTimestamp = signValue.substring(signValue.indexOf(".") + 1, signValue.lastIndexOf("."));
      }
      catch (Exception e) {
        logger.warn("请求签名失败, url={},签名安全模式为1.1,签名参数异常,前端传来的sign={}", request.getRequestURI(), signValue);
        response.setStatus(HttpStatus.SC_BAD_REQUEST);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().print(String.format(FAILED_RESPONSE_TEXT, BaseErrorConstant.SECURITY_4.toException()));
        return false;
      }
      // 校验签名是否有效
      Integer signEffectiveTime = SystemParameter.REQUEST_SIGN_EFFECTIVE_TIME.getIntegerValueFromDb();
      if (signEffectiveTime == null || signEffectiveTime <= 0) {
        return true;
      }
      int secondsDifference = Math.abs((int) ((System.currentTimeMillis() - Long.parseLong(requestTimestamp)) / 1000));
      if (secondsDifference > signEffectiveTime) {
        logger.warn("请求签名已失效, url={},前端传来的sign={},后端计算的签名时间相隔时间={}秒", request.getRequestURI(), signValue, secondsDifference);
        response.setStatus(HttpStatus.SC_BAD_REQUEST);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().print(String.format(FAILED_RESPONSE_TEXT, BaseErrorConstant.SECURITY_3.toException()));
        return false;
      }
      else {
        sourceStr = sourceStr + BaseConsts.HASH + requestTimestamp;
        sign = DigestUtils.sha256Hex(sourceStr);
        if (sign.equals(requestParamSign) && replayVerify(sign, signEffectiveTime)) {
          return true;
        }
      }
    }
    // 签名安全模式为 1.0（普通签名）
    else {
      sign = DigestUtils.sha256Hex(sourceStr);
      if (sign.equals(signValue)) {
        return true;
      }
    }

    logger.warn("签名校验不通过, url={},签名安全模式={},后端生成的sign={},前端传来的sign={},待签名字符串={}", request.getRequestURI(), signSecurityMode, sign, signValue, sourceStr);
    response.setStatus(HttpStatus.SC_BAD_REQUEST);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().print(String.format(FAILED_RESPONSE_TEXT, BaseErrorConstant.SECURITY_2.toException()));
    return false;
  }

  /**
   * 校验签名是否被使用过,仅在1.1签名模式下校验
   */
  private boolean replayVerify(String sign, Integer signEffectiveTime) {
    // 是否对签名重放进行拦截开关， 默认为关闭（SECURITY_SIGN_REUSE_CHECK）
    if (BooleanUtils.isTrue(SystemParameter.SECURITY_SIGN_REUSE_CHECK.getBooleanValueFromDb())) {
      if (signReplayCache.hasKey(sign)) {
        logger.warn("签名已经被使用: {}", sign);
        return false;
      }
      signReplayCache.save(sign, signEffectiveTime + 5);
    }
    return true;
  }


  /**
   * 免签名校验检查
   */
  private boolean skipReq(HttpServletRequest request, Object handler) {
    // 忽略静态资源请求
    if (!(handler instanceof HandlerMethod handlerMethod) || ERROR_URI.equals(request.getRequestURI())) {
      return true;
    }

    // token 校验方式的时候，免签名校验
    if (BoteAuthUtil.shouldSkipAuth(request)) {
      return true;
    }

    Class<?> beanType = handlerMethod.getBeanType();
    for (Class<? extends Annotation> annotation : ignoreAnnotations) {
      // Controller 方法上有注解时跳过
      if (AnnotatedElementUtils.hasAnnotation(handlerMethod.getMethod(), annotation)) {
        return true;
      }
      // Controller 类上有注解时跳过
      if (AnnotatedElementUtils.hasAnnotation(beanType, annotation)) {
        return true;
      }
    }

    return skipReqType(request);
  }

  private boolean skipReqType(HttpServletRequest request) {
    // 只处理post与get
    if (!HttpMethod.POST.matches(request.getMethod()) && !HttpMethod.GET.matches(request.getMethod())) {
      return true;
    }

    // post请求只处理contentType 为空或者为 "application/json 开头的
    if (HttpMethod.POST.matches(request.getMethod()) &&
      StringUtils.isNotEmpty(request.getContentType()) &&
      !Strings.CI.startsWith(request.getContentType(), MediaType.APPLICATION_JSON_VALUE)) {
      return true;
    }

    // 忽略配置的签名请求
    if (skipConfigApi(request.getRequestURI())) {
      return true;
    }
    // 忽略配置的免签名IP的请求
    if (skipConfigIp(request)) {
      return true;
    }
    return request instanceof MultipartHttpServletRequest;
  }

  /**
   * 检测配置为免签名的接口
   */
  private boolean skipConfigApi(String requestUrl) {
    // 配置接口需要排除的
    for (String pt : CollectionUtils.emptyIfNull(signProperties.getWhitelist())) {
      if (ANT_PATH_MATCHER.match(pt, requestUrl)) {
        return true;
      }
    }
    // 检测数据库里配置为免签名的接口
    List<SimpleAttrDTO> attrList = attrSpecCache.get(BaseConsts.PLATFORM_TENANT_ID, ATTR_CODE_IGNORE_LOGIN_API);
    for (SimpleAttrDTO attrDTO : CollectionUtils.emptyIfNull(attrList)) {
      if (attrDTO.getAttrValue() != null && ANT_PATH_MATCHER.match(attrDTO.getAttrValue(), requestUrl)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 检测配置为免签名的 IP
   */
  private boolean skipConfigIp(HttpServletRequest request) {
    // 检测数据库里配置为免签名的 IP
    String ignoreSignIpParam = SystemParameter.IGNORE_SIGN_IP.getValueFromDb();
    if (StringUtils.isNotEmpty(ignoreSignIpParam)) {
      String[] ignoreIps = Arrays.stream(ignoreSignIpParam.split(",")).map(ip -> {
        if (ip.contains("*")) {
          return ip.substring(0, ip.indexOf("*"));
        }
        return ip;
      }).toArray(String[]::new);
      String clientIP = IPUtil.getClientIP(request);
      return Strings.CS.startsWithAny(clientIP, ignoreIps);
    }
    return false;
  }

  /**
   * 构建待加密的字符串，构建规则：
   * <p>1.get 请求：url+"#"+header+"#"+userId+"#"+参数+"#"+秘钥<p/>
   * <p>2.post 请求：url+"#"+header+"#"+userId+"#"+body+秘钥<p/>
   */
  private String structureContent(HttpServletRequest request, boolean isRemove, String removeContent) {
    String body = null;
    String queryString = null;
    if (HttpMethod.POST.matches(request.getMethod())) {
      StringBuilder stringBuilder = new StringBuilder();
      try (BufferedReader reader = request.getReader()) {
        String line;
        while ((line = reader.readLine()) != null) {
          stringBuilder.append(line);
        }
        body = stringBuilder.toString();
      }
      catch (IOException e) {
        logger.error("Failed to read request body input stream", e);
      }
    }
    else if (HttpMethod.GET.matches(request.getMethod()) && StringUtils.isNotEmpty(request.getQueryString())) {
      queryString = URLDecoder.decode(request.getQueryString(), StandardCharsets.UTF_8);
      // get请求 header 中不存在 x-sign 且参数中存在 x-sign 时，生成签名前去除查询参数中的 x-sign
      queryString = removeSignParam(queryString, isRemove, removeContent);
    }
    Map<String, String> headerMap = ServletUtil.getHeadersAsMap(request);
    Cookie userIdCookie = WebUtils.getCookie(request, SignUtil.getUserIdCookieName());
    String userId = Objects.nonNull(userIdCookie) ? userIdCookie.getValue() : "";
    String secretKey = signProperties.getSecretKey();
    return SignCreateUtil.structureContent(request.getMethod(), request.getRequestURI(), userId, queryString, body, headerMap, secretKey);
  }

  /**
   * get请求去除查询参数中的 x-sign
   *
   * @param queryString 查询参数
   * @param isRemove 是否移除
   * @param removeContent 待移除参数
   * @return 移除后的查询参数
   */
  @Nullable
  private String removeSignParam(String queryString, boolean isRemove, String removeContent) {
    if (isRemove) {
      // queryString形如：X-SIGN=c17748c002b8&XA-TYPE=1.1，removeContent：X-SIGN=c17748c002b8&XA-TYPE=1.1
      if (queryString.indexOf(removeContent) == 0) {
        queryString = null;
      }
      // queryString形如：param=1&X-SIGN=c17748c002b8&XA-TYPE=1.1，removeContent：X-SIGN=c17748c002b8&XA-TYPE=1.1
      else if (queryString.indexOf(removeContent) > 0) {
        queryString = queryString.replace("&" + removeContent, "");
      }
    }
    return queryString;
  }

}
