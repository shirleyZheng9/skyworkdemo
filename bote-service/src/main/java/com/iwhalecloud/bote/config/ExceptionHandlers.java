package com.iwhalecloud.bote.config;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 异常处理器
 *
 * @author bianjp
 * @since 2024-08-07
 */
@RestControllerAdvice(basePackages = "com.iwhalecloud.bote.controller")
@SuppressFBWarnings("CRLF_INJECTION_LOGS")
@SuppressWarnings("PMD.GuardLogStatement")
public class ExceptionHandlers {
  private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlers.class);

  /** 是否返回异常堆栈 */
  protected final boolean exceptionStackEnabled;

  public ExceptionHandlers(Environment environment) {
    this.exceptionStackEnabled = environment.getProperty(BaseConsts.EXCEPTION_STACK, Boolean.class, false);
  }

  /**
   * 业务异常
   */
  @ExceptionHandler
  public ResponseEntity<?> exp(HttpServletRequest request, BssException e, HandlerMethod handlerMethod) {
    logger.warn("Caught exception while processing request: {} {}", request.getMethod(), request.getRequestURI(), e);
    ResultVO<?> result = ResultVO.fail(e.getFailCode(), e.getFailMsg(), e.getGuidance(), e);
    return buildResponseEntity(result, HttpStatus.INTERNAL_SERVER_ERROR, handlerMethod);
  }

  /**
   * 校验异常
   *
   * <p>一般是使用 {@link org.springframework.util.Assert} 抛出的异常</p>
   */
  @ExceptionHandler
  public ResponseEntity<?> exp(IllegalArgumentException e, HandlerMethod handlerMethod) {
    ResultVO<?> result = new ResultVO<>("400", ExpUtil.getMsg(e));
    return buildResponseEntity(result, HttpStatus.INTERNAL_SERVER_ERROR, handlerMethod);
  }

  /**
   * 缺少请求参数
   *
   * <p>避免记录异常堆栈以降低性能损耗</p>
   */
  @ExceptionHandler
  @SuppressFBWarnings("BC_UNCONFIRMED_CAST")
  public ResponseEntity<?> exp(MissingRequestValueException e, HandlerMethod handlerMethod) {
    String message = switch (e) {
      case MissingRequestHeaderException e1 -> "缺少必填请求头: " + e1.getHeaderName();
      case MissingPathVariableException e1 -> "缺少必填路径参数: " + e1.getVariableName();
      case MissingRequestCookieException e1 -> "缺少必填 Cookie: " + e1.getCookieName();
      case MissingServletRequestParameterException e1 -> "缺少必填请求参数: " + e1.getParameterName();
      default -> e.getMessage();
    };
    ResultVO<?> result = new ResultVO<>("400", message);
    return buildResponseEntity(result, HttpStatus.BAD_REQUEST, handlerMethod);
  }

  /**
   * 请求参数类型不匹配异常
   *
   * <p>避免记录异常堆栈以降低性能损耗</p>
   */
  @ExceptionHandler
  public ResponseEntity<?> exp(MethodArgumentTypeMismatchException e, HandlerMethod handlerMethod) {
    Class<?> requiredType = e.getRequiredType();
    Object value = e.getValue();
    // @formatter:off
    String msg = "请求参数 " + e.getName() + " 类型错误";
    String guidance = "期望类型=" + (requiredType == null ? "null" : requiredType.getTypeName()) +
      ", 实际类型=" + (value == null ? "null" : ClassUtils.getDescriptiveType(value)) +
      ", 实际值=" + (value == null ? "" : value);
    // @formatter:on
    ResultVO<?> result = new ResultVO<>("400", msg, null, guidance);
    return buildResponseEntity(result, HttpStatus.BAD_REQUEST, handlerMethod);
  }

  /**
   * 请求媒体类型错误
   *
   * <p>避免记录异常堆栈以降低性能损耗</p>
   */
  @ExceptionHandler
  public ResponseEntity<?> exp(HttpMediaTypeNotSupportedException e, HandlerMethod handlerMethod) {
    String msg = "不支持的请求媒体类型: " + e.getContentType();
    ResultVO<?> result = new ResultVO<>("415", msg);
    return buildResponseEntity(result, HttpStatus.UNSUPPORTED_MEDIA_TYPE, handlerMethod);
  }

  /**
   * 数据库执行SQL异常
   */
  @ExceptionHandler
  public ResponseEntity<?> exp(HttpServletRequest request, DataAccessException e, HandlerMethod handlerMethod) {
    logger.warn("Caught exception while processing request: {} {}", request.getMethod(), request.getRequestURI(), e);
    // SQL执行异常时，根据是否返回异常栈信息屏蔽具体的SQL信息
    String errorMsg = exceptionStackEnabled ? e.getMessage() : "系统执行SQL出现异常，请联系管理员处理";
    ResultVO<?> result = ResultVO.fail("500", errorMsg, null, e);
    return buildResponseEntity(result, HttpStatus.INTERNAL_SERVER_ERROR, handlerMethod);
  }

  /**
   * HTTP请求体解析异常
   */
  @ExceptionHandler
  public ResponseEntity<?> exp(HttpServletRequest request, HttpMessageNotReadableException e, HandlerMethod handlerMethod) {
    logger.warn("Caught exception while processing request: {} {}", request.getMethod(), request.getRequestURI(), e);
    // HTTP请求体解析异常，根据是否返回异常栈信息开关屏蔽具体的异常敏感信息
    String errorMsg = exceptionStackEnabled ? e.getMessage() : "系统解析请求体参数出现异常，请联系管理员处理";
    ResultVO<?> result = new ResultVO<>("400", errorMsg);
    return buildResponseEntity(result, HttpStatus.BAD_REQUEST, handlerMethod);
  }

  /**
   * Spring validation 校验失败
   */
  @ExceptionHandler
  public ResponseEntity<?> exp(ConstraintViolationException e, HandlerMethod handlerMethod) {
    String message;
    if (CollectionUtils.isNotEmpty(e.getConstraintViolations())) {
      message = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("; "));
    }
    else {
      message = e.getMessage();
    }
    ResultVO<?> result = new ResultVO<>("400", message);
    return buildResponseEntity(result, HttpStatus.BAD_REQUEST, handlerMethod);
  }

  /**
   * Spring validation 校验失败
   */
  @ExceptionHandler
  public ResponseEntity<?> exp(MethodArgumentNotValidException e, HandlerMethod handlerMethod) {
    List<String> errorMessages = new ArrayList<>(e.getBindingResult().getErrorCount());
    for (ObjectError error : e.getBindingResult().getAllErrors()) {
      if (error instanceof FieldError) {
        errorMessages.add(((FieldError) error).getField() + " " + error.getDefaultMessage());
      }
      else {
        errorMessages.add(error.getObjectName() + " " + error.getDefaultMessage());
      }
    }
    ResultVO<?> result = new ResultVO<>("400", String.join("; ", errorMessages));
    return buildResponseEntity(result, HttpStatus.BAD_REQUEST, handlerMethod);
  }

  /**
   * 请求参数校验失败
   */
  @ExceptionHandler
  public ResponseEntity<?> exp(HandlerMethodValidationException e, HandlerMethod handlerMethod) {
    List<String> errors = new ArrayList<>();
    for (ParameterValidationResult result : e.getParameterValidationResults()) {
      String paramName = StringUtils.defaultIfEmpty(result.getMethodParameter().getParameterName(), "unknown");
      errors.add(result.getResolvableErrors().stream()
        .map(MessageSourceResolvable::getDefaultMessage)
        .collect(Collectors.joining("、", paramName + ": ", "")));
    }
    for (MessageSourceResolvable result : e.getCrossParameterValidationResults()) {
      String msg = result.getDefaultMessage();
      if (StringUtils.isNotEmpty(msg)) {
        errors.add(msg);
      }
    }
    ResultVO<?> result = new ResultVO<>("400", errors.stream().collect(Collectors.joining("; ", "请求参数不合法. ", "")));
    return buildResponseEntity(result, HttpStatus.BAD_REQUEST, handlerMethod);
  }

  /**
   * 未知异常
   */
  @ExceptionHandler
  @Nullable
  public ResponseEntity<?> exp(HttpServletRequest request, Throwable e, HandlerMethod handlerMethod) {
    // 客户端断开连接，不需要打印异常堆栈
    if (e instanceof AsyncRequestNotUsableException || (e instanceof IOException && "Broken pipe".equals(e.getMessage()))) {
      logger.warn("Client aborted while processing request: {} {}", request.getMethod(), request.getRequestURI());
      return null;
    }
    logger.warn("Caught exception while processing request: {} {}", request.getMethod(), request.getRequestURI(), e);
    if (e.getCause() instanceof BssException bssException) {
      return exp(request, bssException, handlerMethod);
    }
    ResultVO<?> result = ResultVO.fail("500", ExpUtil.getMsg(e), null, e);
    return buildResponseEntity(result, HttpStatus.INTERNAL_SERVER_ERROR, handlerMethod);
  }

  /**
   * 构造响应对象
   */
  private ResponseEntity<?> buildResponseEntity(ResultVO<?> result, HttpStatus defaultHttpStatus, HandlerMethod handlerMethod) {
    // 如果处理方法返回类型是 ResultVO, 那么返回 200 状态码，以减少开销并方便前端处理
    if (ResultVO.class.equals(handlerMethod.getMethod().getReturnType())) {
      return new ResponseEntity<>(result, HttpStatus.OK);
    }
    return new ResponseEntity<>(result, defaultHttpStatus);
  }

}
