package com.iwhalecloud.bote.controller.base;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Hidden;
import java.io.IOException;
import java.util.Map;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 自定义错误处理
 *
 * <p>Spring 默认的错误页面不会显示 response.sendError 指定的错误信息（比如下载文件接口提示文件不存在），对用户不太友好，容易误判错误原因</p>
 *
 * @author bianjp
 * @since 2025-06-30
 */
@RestController
@RequiredArgsConstructor
@Hidden
public class BoteErrorController implements ErrorController {
  private final ErrorAttributes errorAttributes;

  /**
   * 处理错误
   *
   * <p>正常的接口报错一般会被 {@link com.iwhalecloud.bote.config.ExceptionHandlers} 捕获，这里用来处理一些特殊接口的报错，比如文件下载接口</p>
   */
  @RequestMapping("error")
  @SuppressFBWarnings("XSS_SERVLET")
  public void error(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // 如果响应已经提交，则不处理
    if (response.isCommitted()) {
      return;
    }
    HttpStatus status = getStatus(request);
    String message = getErrorMessage(request, status);
    // 直接写入响应，不使用 ResponseEntity, 避免报 HttpMediaTypeNotAcceptableException
    response.setStatus(status.value());
    if (Strings.CS.containsAny(request.getHeader(HttpHeaders.ACCEPT), MediaType.TEXT_HTML_VALUE, MediaType.TEXT_PLAIN_VALUE)) {
      // 使用 text/plain 而非 text/html 这样就不需要转义 message 了
      // 指定字符集，避免中文显示为乱码
      response.setContentType("text/plain;charset=UTF-8");
      response.getWriter().write(message);
    }
    else {
      Map<String, String> data = ImmutableMap.of("resultCode", Integer.toString(status.value()), "resultMsg", message);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write(JsonUtil.toJsonString(data));
    }
  }

  /**
   * 获取状态码
   */
  private HttpStatus getStatus(HttpServletRequest request) {
    Throwable error = errorAttributes.getError(new ServletWebRequest(request));
    // 上传文件大小超出限制时返回 413 状态码（默认是 500）
    if (error instanceof MaxUploadSizeExceededException) {
      return HttpStatus.PAYLOAD_TOO_LARGE;
    }
    Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    if (statusCode == null) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    try {
      return HttpStatus.valueOf(statusCode);
    }
    catch (Exception ex) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }

  /**
   * 获取错误信息
   */
  private String getErrorMessage(HttpServletRequest request, HttpStatus status) {
    Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
    if (!ObjectUtils.isEmpty(message)) {
      return message.toString();
    }
    Throwable error = errorAttributes.getError(new ServletWebRequest(request));
    if (error != null) {
      return ExpUtil.getMsg(error);
    }
    return status.toString();
  }

}
