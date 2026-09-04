package com.iwhalecloud.bote.config;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 异常信息统一返回包装器
 *
 * @author chen.linfa
 * @since 2022-11-10
 */
@RestControllerAdvice("com.iwhalecloud.bote")
public class ExceptionResponseAdvice implements ResponseBodyAdvice<Object> {

  /** 是否返回异常堆栈 */
  private final boolean exceptionStackEnabled;

  public ExceptionResponseAdvice(Environment environment) {
    this.exceptionStackEnabled = environment.getProperty(BaseConsts.EXCEPTION_STACK, Boolean.class, false);
  }

  @Override
  public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return true;
  }

  @Override
  @SuppressWarnings("unchecked")
  @Nullable
  public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType,
                                Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                ServerHttpRequest request, ServerHttpResponse response) {
    // 空值不处理
    if (body == null) {
      return null;
    }

    // 非 ResultVO 不处理
    if (!(body instanceof ResultVO)) {
      return body;
    }
    ResultVO<Object> resultVO = (ResultVO<Object>) body;

    // 如果未启用打印日志，则将堆栈信息清空
    if (!this.exceptionStackEnabled) {
      resultVO.setStack("");
    }

    // 过滤处理msg信息
    filterMsg(resultVO);

    // 特殊处理开启安全模式3.0时设置响应头
    setSecurityModeForResponse(request, response);

    return resultVO;
  }

  /**
   * 过滤 msg 信息
   */
  private void filterMsg(ResultVO<Object> resultVO) {
    String resultMsg = resultVO.getResultMsg();
    if (StringUtils.isEmpty(resultMsg)) {
      return;
    }
    // 过滤数据库敏感信息
    if (resultMsg.contains("Error updating database") && !this.exceptionStackEnabled) {
      resultVO.setResultMsg("系统执行SQL出现异常，请联系管理员处理");
    }
  }

  private void setSecurityModeForResponse(ServerHttpRequest request, ServerHttpResponse respons) {
    // 开启安全模式3.0时，设置响应头（抛出异常会提交响应导致 EncryptionFilter 无法设置响应头）
    List<String> values = request.getHeaders().get(BaseConsts.HEADER_KEY_SIGN_SECURITY_MODE);
    if (values != null && !values.isEmpty()) {
      String securityModeValue = values.get(0);
      // 3.0 安全模式下，SSE类型的请求不设置安全模式响应头，避免前端对数据进行解密
      boolean isSSE = Boolean.TRUE.equals(request.getAttributes().get(BaseConsts.ATTRIBUTE_KEY_IS_SSE));
      if (Strings.CS.equalsAny(securityModeValue, "3.0", "4.0") && !isSSE) {
        respons.getHeaders().set(BaseConsts.HEADER_KEY_SIGN_SECURITY_MODE, securityModeValue);
      }
    }
  }

}
