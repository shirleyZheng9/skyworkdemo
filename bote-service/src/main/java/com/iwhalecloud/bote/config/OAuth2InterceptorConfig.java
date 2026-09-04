package com.iwhalecloud.bote.config;

import com.iwhalecloud.bote.common.interceptor.Oauth2Interceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * OAuth2拦截器配置
 * <p>
 * 配置OAuth2认证拦截器，拦截需要OAuth2认证的接口。
 * </p>
 * <p>
 * 配置说明：
 * <ul>
 *   <li>通过{@code bote.oauth2.enabled=true}启用OAuth2功能</li>
 *   <li>只有标记了{@link com.iwhalecloud.bote.common.annotation.RequireOAuth2}注解的接口才会进行OAuth2认证</li>
 * </ul>
 * </p>
 *
 * @author Aiqing
 * @since 2025/12/26
 */
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "bote.oauth2.enabled", havingValue = "true")
public class OAuth2InterceptorConfig implements WebMvcConfigurer {

  private final Oauth2Interceptor oauth2Interceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 注册OAuth2拦截器，拦截所有请求（拦截器内部会根据注解判断是否需要认证）
    // 拦截顺序：在SessionInterceptor之后执行
    registry.addInterceptor(oauth2Interceptor)
      .addPathPatterns("/**")
      .order(1);
  }
}

