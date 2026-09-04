package com.iwhalecloud.bote.doc.config;

import com.iwhalecloud.bote.common.diffc.extend.DiffcDataMapper;
import com.iwhalecloud.bote.common.requestcache.RequestCacheAspect;
import com.iwhalecloud.bote.common.requestcache.RequestCacheableParamResolver;
import com.iwhalecloud.bss.litchi.diffc.config.LitchiDiffcAutoConfiguration;
import com.iwhalecloud.bss.litchi.diffc.mapper.DataMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;

/**
 * 自动配置
 * <p>添加 ComponentScan, MapperScan 注解以确保在定制包中也能正常注册 bean</p>
 *
 * @author chen.linfa
 * @since 2025-11-19
 */
@AutoConfiguration(before = {LitchiDiffcAutoConfiguration.class, WebMvcAutoConfiguration.class})
@ComponentScan(basePackages = "com.iwhalecloud.bote", excludeFilters = @Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class))
@MapperScan(basePackages = {"com.iwhalecloud.bote.mapper", "com.iwhalecloud.bote.doc.module.*.mapper"})
@EnableScheduling
public class DocumentAutoConfiguration {

  /**
   * 请求缓存切面
   *
   * @return 请求缓存切面
   */
  @Bean
  public RequestCacheAspect requestCacheAspect(JdbcTemplate jdbcTemplate, ObjectProvider<List<RequestCacheableParamResolver>> paramResolvers) {
    return new RequestCacheAspect(jdbcTemplate, paramResolvers);
  }

  /**
   * 覆盖 bean: {@link LitchiDiffcAutoConfiguration#litchiDataMapper}
   */
  @Bean
  public DataMapper customDataMapper(JdbcTemplate jdbcTemplate) {
    return new DiffcDataMapper(jdbcTemplate);
  }

  /**
   * 语言解析器，固定使用简体中文（暂不支持多语言）
   * <p>用于覆盖 Spring 根据 Accept-Language 请求头设置的语言: {@link org.springframework.web.filter.RequestContextFilter#initContextHolders}</p>
   */
  @Bean(DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME)
  public LocaleResolver localeResolver() {
    return new LocaleResolver() {
      @Override
      public Locale resolveLocale(HttpServletRequest request) {
        return Locale.SIMPLIFIED_CHINESE;
      }

      @Override
      public void setLocale(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Locale locale) {
        throw new UnsupportedOperationException();
      }
    };
  }
}
