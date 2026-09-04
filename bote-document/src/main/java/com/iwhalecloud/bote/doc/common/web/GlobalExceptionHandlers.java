package com.iwhalecloud.bote.doc.common.web;

import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ClassUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 异常处理器
 *
 * @author bianjp
 * @since 2024-08-07
 */
@RestControllerAdvice(basePackages = "com.iwhalecloud.bote.doc")
@SuppressFBWarnings("CRLF_INJECTION_LOGS")
@SuppressWarnings("PMD.GuardLogStatement")
public class GlobalExceptionHandlers {
  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandlers.class);

  private static final String EXCEPTION_STACK = "exception.stack";

  /**
   * 业务异常
   */
  @ExceptionHandler(BssException.class)
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
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> exp(IllegalArgumentException e, HandlerMethod handlerMethod) {
    ResultVO<?> result = ResultVO.fail("400", ExpUtil.getMsg(e), null, e);
    return buildResponseEntity(result, HttpStatus.INTERNAL_SERVER_ERROR, handlerMethod);
  }

  /**
   * 缺少请求参数
   *
   * <p>避免记录异常堆栈以降低性能损耗</p>
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<?> exp(MissingServletRequestParameterException e, HandlerMethod handlerMethod) {
    ResultVO<?> result = ResultVO.fail("400", "缺少必填请求参数: " + e.getParameterName(), null, e);
    return buildResponseEntity(result, HttpStatus.BAD_REQUEST, handlerMethod);
  }

  /**
   * 请求参数类型不匹配异常
   *
   * <p>避免记录异常堆栈以降低性能损耗</p>
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<?> exp(MethodArgumentTypeMismatchException e, HandlerMethod handlerMethod) {
    Class<?> requiredType = e.getRequiredType();
    Object value = e.getValue();
    // @formatter:off
    String msg = "请求参数 " + e.getName() + " 类型错误";
    String guidance = "期望类型=" + (requiredType == null ? "null" : requiredType.getTypeName()) +
      ", 实际类型=" + (value == null ? "null" : ClassUtils.getDescriptiveType(value)) +
      ", 实际值=" + (value == null ? "" : value);
    // @formatter:on
    ResultVO<?> result = ResultVO.fail("400", msg, guidance, e);
    return buildResponseEntity(result, HttpStatus.BAD_REQUEST, handlerMethod);
  }

  /**
   * 请求媒体类型错误
   *
   * <p>避免记录异常堆栈以降低性能损耗</p>
   */
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<?> exp(HttpMediaTypeNotSupportedException e, HandlerMethod handlerMethod) {
    String msg = "不支持的请求媒体类型: " + e.getContentType();
    ResultVO<?> result = ResultVO.fail("415", msg, null, e);
    return buildResponseEntity(result, HttpStatus.UNSUPPORTED_MEDIA_TYPE, handlerMethod);
  }

  /**
   * 数据库执行SQL异常
   */
  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<?> exp(HttpServletRequest request, DataAccessException e, HandlerMethod handlerMethod) {
    logger.warn("Caught exception while processing request: {} {}", request.getMethod(), request.getRequestURI(), e);
    // SQL执行异常时，根据是否返回异常栈信息屏蔽具体的SQL信息
    Boolean stackEnabled = SpringUtil.getProperty(EXCEPTION_STACK, Boolean.class, false);
    String errorMsg = stackEnabled ? e.getMessage() : "系统执行SQL出现异常，请联系管理员处理";
    ResultVO<?> result = ResultVO.fail("500", errorMsg, null, e);
    return buildResponseEntity(result, HttpStatus.INTERNAL_SERVER_ERROR, handlerMethod);
  }

  /**
   * Spring validation 校验失败 - 方法参数校验
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<?> exp(ConstraintViolationException e, HandlerMethod handlerMethod) {
    String message;
    if (CollectionUtils.isNotEmpty(e.getConstraintViolations())) {
      ConstraintViolation<?> violation = e.getConstraintViolations().iterator().next();
      message = violation.getMessage();
    }
    else {
      message = e.getMessage();
    }
    ResultVO<?> result = ResultVO.fail("400", message, "", e);
    return buildResponseEntity(result, HttpStatus.BAD_REQUEST, handlerMethod);
  }

  /**
   * Spring validation 校验失败 - 请求体参数校验
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> exp(MethodArgumentNotValidException e, HandlerMethod handlerMethod) {
    logger.debug("处理MethodArgumentNotValidException异常: {}", e.getMessage());

    String message;
    ObjectError error = e.getBindingResult().getAllErrors().get(0);
    if (error instanceof FieldError) {
      message = ((FieldError) error).getField() + " " + error.getDefaultMessage();
    }
    else {
      message = error.getObjectName() + " " + error.getDefaultMessage();
    }

    logger.info("参数校验失败: {}", message);
    ResultVO<?> result = ResultVO.fail("400", message, "", e);
    return buildResponseEntity(result, HttpStatus.BAD_REQUEST, handlerMethod);
  }

  /**
   * 未知异常 - 兜底处理
   */
  @ExceptionHandler(Throwable.class)
  public ResponseEntity<?> exp(HttpServletRequest request, Throwable e, HandlerMethod handlerMethod) {
    logger.warn("Caught exception while processing request: {} {}", request.getMethod(), request.getRequestURI(), e);
    if (e.getCause() instanceof BssException) {
      BssException bssException = (BssException) e.getCause();
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
