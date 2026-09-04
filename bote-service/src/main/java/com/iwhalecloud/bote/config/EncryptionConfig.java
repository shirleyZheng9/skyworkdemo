package com.iwhalecloud.bote.config;

import com.iwhalecloud.bassc.basiccenter.config.CookiesProperties;
import com.iwhalecloud.bassc.basiccenter.config.SecurityProperties;
import com.iwhalecloud.bote.cache.DcParamCache;
import com.iwhalecloud.bote.common.interceptor.MybatisParamsInterceptor;
import com.iwhalecloud.bote.common.interceptor.MybatisResultSetInterceptor;
import com.iwhalecloud.bote.filter.EncryptionFilter;
import com.iwhalecloud.bote.service.security.BoteCookieSerializer;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * 加密配置类
 *
 * @author tingyun.wang
 */
@Configuration(proxyBeanMethods = false)
public class EncryptionConfig {
  private final RequestMappingHandlerMapping reqMappingHandler;
  private final List<SqlSessionFactory> sqlSessionFactoryList;
  private final MybatisParamsInterceptor mybatisParamsInterceptor;
  private final MybatisResultSetInterceptor mybatisResultSetInterceptor;

  // 有两个 RequestMappingHandlerMapping bean，需要指定 Qualifier
  // org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport.requestMappingHandlerMapping
  // org.springframework.boot.actuate.autoconfigure.endpoint.web.servlet.WebMvcEndpointManagementContextConfiguration.webEndpointServletHandlerMapping
  public EncryptionConfig(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping reqMappingHandler, List<SqlSessionFactory> sqlSessionFactoryList, MybatisParamsInterceptor mybatisParamsInterceptor, MybatisResultSetInterceptor mybatisResultSetInterceptor) {
    this.reqMappingHandler = reqMappingHandler;
    this.sqlSessionFactoryList = sqlSessionFactoryList;
    this.mybatisParamsInterceptor = mybatisParamsInterceptor;
    this.mybatisResultSetInterceptor = mybatisResultSetInterceptor;
  }

  @Bean
  @FilterRegistration(order = Ordered.HIGHEST_PRECEDENCE)
  public EncryptionFilter encryptionFilter(DcParamCache dcParamCache) {
    return new EncryptionFilter(dcParamCache, reqMappingHandler);
  }

  @Bean
  public CookieSerializer cookieSerializer(SecurityProperties securityProperties, CookiesProperties cookiesProperties,
      @Value("${bote.portal.cookieCleanupPaths:/bote}") String cleanupPathsCsv) {
    // 历史残留 BOTE_SESSION 的待清理 path 列表（逗号分隔），用于自愈清理同名多 path cookie
    List<String> cleanupPaths = Arrays.stream(cleanupPathsCsv.split(","))
      .map(String::trim)
      .filter(s -> !s.isEmpty())
      .distinct()
      .collect(Collectors.toList());
    return new BoteCookieSerializer(securityProperties, cookiesProperties, cleanupPaths);
  }

  /**
   * 注册MyBatis拦截器
   */
  @PostConstruct
  public void addMybatisParamsInterceptor() {
    for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
      sqlSessionFactory.getConfiguration().addInterceptor(mybatisParamsInterceptor);
      sqlSessionFactory.getConfiguration().addInterceptor(mybatisResultSetInterceptor);
    }
  }

}
