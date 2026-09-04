package com.iwhalecloud.bote.common.requestcache;

import com.iwhalecloud.bote.common.annotation.RequestCacheable;
import org.springframework.lang.Nullable;

/**
 * 请求缓存参数解析器
 *
 * <p>用于解析 {@link RequestCacheable#sql()} 中的参数，以支持解析不在请求参数中的全局参数，比如 session 中的用户信息。</p>
 *
 * <p>有多个 bean 实例时可以使用 Spring 的 {@link org.springframework.core.annotation.Order} 注解或者 {@link org.springframework.core.Ordered} 接口指定优先级。</p>
 *
 * <p>参数名称可以是嵌套属性（使用 "." 分隔），比如 <code>session.userId</code></p>
 *
 * @author bianjp
 * @since 2023-06-25
 */
public interface RequestCacheableParamResolver {

  /**
   * 是否支持解析指定参数
   *
   * @param paramName 参数名称，可以是嵌套属性（以 "." 分隔）
   * @return 是否支持解析指定参数
   */
  boolean supports(String paramName);

  /**
   * 解析参数值
   *
   * @param paramName 参数名称，可以是嵌套属性（以 "." 分隔）
   * @return 参数值
   */
  @Nullable
  Object resolve(String paramName);

}
