package com.iwhalecloud.bote.filter;

import com.iwhalecloud.bote.cache.DcParamCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.AesUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ServletRequestPathUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
public class EncryptionFilter implements Filter {
  private static final Logger logger = LoggerFactory.getLogger(EncryptionFilter.class);

  /** 应用安全缓存 */
  private final DcParamCache dcParamCache;
  private final Map<String, String> encryptionMap = new HashMap<>();

  /** 请求映射处理器 */
  private final RequestMappingHandlerMapping reqMappingHandler;

  @Override
  public void init(FilterConfig filterConfig) {
    // XA-TYPE=1.0(签名);3.0(AES);4.0(DES)
    encryptionMap.put("3.0", "ENCRYPTION_AES");
    encryptionMap.put("4.0", "ENCRYPTION_DES");
  }

  @SuppressFBWarnings({"HRS_REQUEST_PARAMETER_TO_HTTP_HEADER", "HTTP_RESPONSE_SPLITTING"})
  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    // 入参解密header
    String encryptionType = request.getHeader(BaseConsts.HEADER_KEY_SIGN_SECURITY_MODE);

    // 如果 get请求的 header 中不存在 XA-TYPE，则从查询参数中获取 XA-TYPE
    if (StringUtils.isEmpty(encryptionType) && HttpMethod.GET.matches(request.getMethod()) && StringUtils.isNotEmpty(request.getQueryString())) {
      String queryString = URLDecoder.decode(request.getQueryString(), StandardCharsets.UTF_8);
      // 获取 get 请求查询参数中的 XA-TYPE 参数
      String encryptionParam = Arrays.stream(queryString.split("&"))
        .filter(p -> Strings.CI.contains(p, BaseConsts.HEADER_KEY_SIGN_SECURITY_MODE))
        .findFirst().orElse("");
      if (StringUtils.isNotEmpty(encryptionParam)) {
        encryptionType = encryptionParam.substring(encryptionParam.indexOf("=") + 1);
      }
    }

    if (Strings.CS.equalsAny(encryptionType, BaseConsts.AES, BaseConsts.DES)) {
      // 处理请求头中携带加解密模式的接口
      handleWithSecurityMode(filterChain, request, response, servletResponse, encryptionType);
    }
    // 不加解密的接口进行放行
    else {
      filterChain.doFilter(request, response);
    }
  }

  /**
   * 处理请求头中携带加解密模式的接口
   */
  private void handleWithSecurityMode(FilterChain filterChain, HttpServletRequest request, HttpServletResponse response,
    ServletResponse servletResponse, String encryptionType) throws IOException, ServletException {
    // 1.表单请求
    if (Strings.CI.startsWithAny(request.getContentType(), MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
      handleFormRequest(filterChain, request, response, servletResponse, encryptionType);
    }
    // 2.非表单请求
    else {
      handleNormalRequest(filterChain, request, response, servletResponse, encryptionType);
    }
  }

  /**
   * 处理非表单请求
   */
  @SuppressFBWarnings("HTTP_RESPONSE_SPLITTING")
  private void handleNormalRequest(FilterChain filterChain, HttpServletRequest request, HttpServletResponse response,
    ServletResponse servletResponse, String encryptionType) throws IOException, ServletException {
    EncryptionReqestWrapper requestWrapper = new EncryptionReqestWrapper(request);
    // 请求解密
    processDecryption(requestWrapper, request, encryptionType);

    // SseEmitter 类型的响应不加密
    if (isTextEventStream(request)) {
      filterChain.doFilter(requestWrapper, response);
    }
    else {
      // 其类型正常加密响应
      EncryptionResponseWrapper responseWrapper = new EncryptionResponseWrapper(response);
      filterChain.doFilter(requestWrapper, responseWrapper);
      // 处理响应
      if (Strings.CI.startsWith(response.getContentType(), MediaType.APPLICATION_JSON_VALUE) && response.getStatus() != HttpStatus.INTERNAL_SERVER_ERROR.value()) {
        response.setHeader(BaseConsts.HEADER_KEY_SIGN_SECURITY_MODE, encryptionType);
        String responseData = responseWrapper.getResponseData();
        if (!responseData.isEmpty()) {
          writeEncryptContent(responseData, servletResponse, encryptionType);
        }
      }
      else {
        // 响应是文件流
        filterChain.doFilter(requestWrapper, response);
      }
    }
  }

  /**
   * 检查 Controller 方法的 produces 是否包含 MediaType.TEXT_EVENT_STREAM_VALUE
   */
  private boolean isTextEventStream(HttpServletRequest request) {
    Boolean isSSE = false;
    try {
      // 解析请求路径并缓存
      ServletRequestPathUtils.parseAndCache(request);
      // 获取请求的处理器执行链
      HandlerExecutionChain handlerExecutionChain = reqMappingHandler.getHandler(request);
      if (handlerExecutionChain != null) {
        Object handler = handlerExecutionChain.getHandler();
        if (handler instanceof HandlerMethod handlerMethod) {
          Method method = handlerMethod.getMethod();
          // 获取方法上的 @PostMapping 注解
          PostMapping requestMapping = AnnotationUtils.findAnnotation(method, PostMapping.class);
          if (requestMapping != null) {
            isSSE = Arrays.asList(requestMapping.produces()).contains(MediaType.TEXT_EVENT_STREAM_VALUE);
          }
        }
      }
    }
    catch (Exception e) {
      logger.error("解析 RequestMapping 的 produces 类型数据失败！", e);
      throw new SecurityException(e);
    }
    // 添加到请求属性中
    request.setAttribute(BaseConsts.ATTRIBUTE_KEY_IS_SSE, isSSE);
    return isSSE;
  }

  /**
   * 请求解密处理
   * @param requestWrapper 继承请求类
   * @param request        请求类
   */
  private void processDecryption(EncryptionReqestWrapper requestWrapper, HttpServletRequest request, String encryptionType) {
    String requestData = requestWrapper.getRequestData();
    try {
      if (!Strings.CI.endsWith(request.getMethod(), RequestMethod.GET.name()) && !StringUtils.isEmpty(requestData)
        && Strings.CI.startsWith(request.getContentType(), MediaType.APPLICATION_JSON_VALUE)) {
        String decryptRequestData = decrypt(requestData, encryptionType);
        requestWrapper.setRequestData(decryptRequestData);
      }

      // 参数值加密v
      Map<String, String[]> paramMap = new HashMap<>();
      Enumeration<String> parameterNames = request.getParameterNames();
      while (parameterNames.hasMoreElements()) {
        String paramName = parameterNames.nextElement();
        if (BaseConsts.HEADER_KEY_SIGN_SECURITY_MODE.equalsIgnoreCase(paramName)) {
          continue;
        }
        String[] paramValue = request.getParameterValues(paramName);
        List<String> params = new ArrayList<>();
        for (String parameterValue : paramValue) {
          String decryptParamValue = decrypt(parameterValue, encryptionType);
          String decodeValue = URLDecoder.decode(decryptParamValue, StandardCharsets.UTF_8);
          params.add(decodeValue);
        }
        paramMap.put(paramName, params.toArray(new String[0]));
      }
      requestWrapper.setParamMap(paramMap);
    }
    catch (Exception e) {
      logger.error("请求数据解密失败", e);
      throw new SecurityException(e);
    }
  }

  private String decrypt(String data, String encryptionType) {
    String key = dcParamCache.getDcParamValByCode(encryptionMap.get(encryptionType));
    Assert.hasLength(key, () -> "缺少密钥配置: encryptionType=" + encryptionType);
    if (BaseConsts.AES.equals(encryptionType)) {
      return AesUtil.aesDecrypt(data, key);
    }
    else {
      return data;
    }
  }

  /**
   * 输出加密内容
   */
  private void writeEncryptContent(String responseData, ServletResponse response, String encryptionType) throws IOException {
    ServletOutputStream out = null;
    try {
      responseData = encrypt(responseData, encryptionType);
      response.setContentLength(responseData.length());
      response.setCharacterEncoding(StandardCharsets.UTF_8.name());
      out = response.getOutputStream();
      out.write(responseData.getBytes(StandardCharsets.UTF_8));
    }
    catch (Exception e) {
      logger.error("响应数据加密失败", e);
      throw new SecurityException(e);
    }
    finally {
      if (out != null) {
        out.flush();
        out.close();
      }
    }
  }

  private String encrypt(String data, String encryptionType) {
    String key = dcParamCache.getDcParamValByCode(encryptionMap.get(encryptionType));
    Assert.hasLength(key, () -> "缺少密钥配置: encryptionType=" + encryptionType);
    if (BaseConsts.AES.equals(encryptionType)) {
      return AesUtil.aesEncrypt(data, key);
    }
    else {
      return data;
    }
  }

  /**
   * 处理表单请求
   */
  @SuppressFBWarnings("HTTP_RESPONSE_SPLITTING")
  private void handleFormRequest(FilterChain filterChain, HttpServletRequest request, HttpServletResponse response,
    ServletResponse servletResponse, String encryptionType) throws IOException, ServletException {
    EncryptionFormRequestWrapper requestWrapper = new EncryptionFormRequestWrapper(request);
    // 请求解密
    processFormRequestDecryption(requestWrapper, request, encryptionType);

    // SseEmitter 类型的响应不加密
    if (isTextEventStream(request)) {
      filterChain.doFilter(requestWrapper, response);
    }
    else {
      // 其类型正常加密响应
      EncryptionResponseWrapper responseWrapper = new EncryptionResponseWrapper(response);
      filterChain.doFilter(requestWrapper, responseWrapper);
      // 处理响应
      if (Strings.CI.startsWith(response.getContentType(), MediaType.APPLICATION_JSON_VALUE) && response.getStatus() != HttpStatus.INTERNAL_SERVER_ERROR.value()) {
        response.setHeader(BaseConsts.HEADER_KEY_SIGN_SECURITY_MODE, encryptionType);
        String responseData = responseWrapper.getResponseData();
        if (!responseData.isEmpty()) {
          writeEncryptContent(responseData, servletResponse, encryptionType);
        }
      }
      else {
        // 响应是文件流
        filterChain.doFilter(requestWrapper, response);
      }
    }
  }

  /**
   * 表单请求解密处理
   */
  private void processFormRequestDecryption(EncryptionFormRequestWrapper requestWrapper, HttpServletRequest request, String encryptionType) {
    try {
      // 参数值解密
      Map<String, String[]> paramMap = new HashMap<>();
      Enumeration<String> parameterNames = requestWrapper.getParameterNames();
      while (parameterNames.hasMoreElements()) {
        String paramName = parameterNames.nextElement();
        String[] paramValue = request.getParameterValues(paramName);
        List<String> params = new ArrayList<>();
        for (String parameterValue : paramValue) {
          if (StringUtils.isNotEmpty(parameterValue)) {
            String decryptParamValue = decrypt(parameterValue, encryptionType);
            String decodeValue = URLDecoder.decode(decryptParamValue, StandardCharsets.UTF_8);
            params.add(decodeValue);
          }
        }
        paramMap.put(paramName, params.toArray(new String[0]));
      }
      requestWrapper.setParamMap(paramMap);
    }
    catch (Exception e) {
      logger.error("表单请求数据解密失败", e);
      throw new SecurityException(e);
    }
  }

}
