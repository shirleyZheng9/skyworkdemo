package com.iwhalecloud.bote.doc.common.tenant;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.iwhalecloud.bote.doc.common.mybatis.parser.JsqlParserGlobal;
import com.iwhalecloud.bote.doc.common.mybatis.parser.cache.JdkSerialCaffeineJsqlParseCache;
import com.iwhalecloud.bote.doc.common.mybatis.plugins.MybatisPlusInterceptor;
import com.iwhalecloud.bote.doc.common.mybatis.plugins.helper.InterceptorIgnoreHelper;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 租户相关配置
 *
 * @author Aiqing
 * @since 2025/9/6
 */
@Configuration
@EnableConfigurationProperties(TenantProperties.class)
public class TenantAutoConfiguration {

  @Bean
  public MybatisPlusInterceptor mybatisPlusInterceptor(TenantProperties tenantProperties) {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    // 是否开启多租户
    if (Objects.equals(Boolean.TRUE, tenantProperties.getEnable())) {
      interceptor.addInnerInterceptor(new CustomTenantLineInterceptor(new CustomTenantLineHandler(tenantProperties)));
    }
    List<String> ignoreMethods = tenantProperties.getIgnoreMethods();
    if (CollectionUtils.isNotEmpty(ignoreMethods)) {
      ignoreMethods.forEach(InterceptorIgnoreHelper::addIgnoreMethod);
    }
    List<String> enablePackages = tenantProperties.getEnablePackages();
    if (CollectionUtils.isNotEmpty(enablePackages)) {
      enablePackages.forEach(InterceptorIgnoreHelper::addEnabledPackage);
    }
    Cache<String, byte[]> cache = CacheBuilder.newBuilder()
      .initialCapacity(100)
      .maximumSize(1000)
      .build();
    JdkSerialCaffeineJsqlParseCache jsqlParseCache = new JdkSerialCaffeineJsqlParseCache(cache);
    JsqlParserGlobal.setJsqlParseCache(jsqlParseCache);
    return interceptor;
  }
}
