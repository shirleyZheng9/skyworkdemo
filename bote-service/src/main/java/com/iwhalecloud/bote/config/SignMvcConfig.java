package com.iwhalecloud.bote.config;

import com.iwhalecloud.bote.cache.AttrSpecCache;
import com.iwhalecloud.bote.cache.SignReplayCache;
import com.iwhalecloud.bote.common.interceptor.SignReqParamInterceptor;
import com.iwhalecloud.bote.config.properties.SignProperties;
import com.iwhalecloud.bote.filter.SignFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.web.servlet.FilterRegistration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 安全相关拦截器配置
 *
 * @author zhangJun
 * @since 2022/3/23
 **/
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@ConditionalOnBooleanProperty("bote.security.sign.enable")
public class SignMvcConfig implements WebMvcConfigurer {

  private final SignProperties signProperties;
  private final AttrSpecCache attrSpecCache;
  private final SignReplayCache signReplayCache;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new SignReqParamInterceptor(signProperties, attrSpecCache, signReplayCache)).addPathPatterns("/**");
  }

  /**
   * 签名拦截器
   */
  @Bean
  @FilterRegistration(urlPatterns = "/*", order = 1)
  public SignFilter signFilter() {
    return new SignFilter();
  }

}
