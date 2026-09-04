package com.iwhalecloud.bote.license.autoconfigure;

import com.iwhalecloud.bote.license.cache.LicenseCache;
import com.iwhalecloud.bote.license.check.LicenseBootChecker;
import com.iwhalecloud.bote.license.check.LicenseCheckInterceptor;
import com.iwhalecloud.bote.license.check.LicenseChecker;
import com.iwhalecloud.bote.license.condition.ConditionalOnLicense;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * license自动配置类
 *
 * @author cheng.xu
 */
@Configuration
@ConditionalOnLicense
@Import({
  LicenseCache.class,
  LicenseChecker.class,
  LicenseBootChecker.class,
  LicenseCheckInterceptor.class,
  LicenseAutoConfiguration.LicenseWebMvcConfig.class
})
@SuppressWarnings("java:S1118")
public class LicenseAutoConfiguration {

  /**
   * license MVC 配置
   */
  @RequiredArgsConstructor
  public static class LicenseWebMvcConfig implements WebMvcConfigurer {
    private final LicenseCheckInterceptor licenseCheckInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(licenseCheckInterceptor).addPathPatterns("/**");
    }
  }
}
