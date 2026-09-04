package com.iwhalecloud.bote.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.iwhalecloud.bote.common.diffc.extend.DiffcDataMapper;
import com.iwhalecloud.bote.common.requestcache.RequestCacheAspect;
import com.iwhalecloud.bote.common.requestcache.RequestCacheableParamResolver;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.service.chat.helper.FlowRunLogDisruptorListener;
import com.iwhalecloud.bote.service.chat.helper.ModelUsageLogDisruptorListener;
import com.iwhalecloud.bss.litchi.diffc.config.LitchiDiffcAutoConfiguration;
import com.iwhalecloud.bss.litchi.diffc.mapper.DataMapper;
import com.iwhalecloud.bss.litchi.disruptor.DisruptorUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.Assert;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;

/**
 * 自动配置
 *
 * <p>添加 ComponentScan, MapperScan 注解以确保在定制包中也能正常注册 bean</p>
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
@AutoConfiguration(before = {
  LitchiDiffcAutoConfiguration.class,
  WebMvcAutoConfiguration.class // 避免 LocaleResolver bean 注册失败
})
@ComponentScan(basePackages = "com.iwhalecloud.bote", excludeFilters = @Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class))
@MapperScan(basePackages = {"com.iwhalecloud.bote.mapper", "com.iwhalecloud.bote.doc.mapper"})
@EnableScheduling
public class BoteAutoConfiguration {

  /**
   * 覆盖 bean: {@link LitchiDiffcAutoConfiguration#litchiDataMapper}
   */
  @Bean
  public DataMapper customDataMapper(JdbcTemplate jdbcTemplate) {
    return new DiffcDataMapper(jdbcTemplate);
  }

  /**
   * 注册日志监听器
   */
  @EventListener(ApplicationReadyEvent.class)
  public void initLogListener(ApplicationReadyEvent event) {
    DisruptorUtil.getInstance().registerListener(event.getApplicationContext().getBean(FlowRunLogDisruptorListener.class));
    DisruptorUtil.getInstance().registerListener(event.getApplicationContext().getBean(ModelUsageLogDisruptorListener.class));
  }

  /**
   * 自定义 Resource 序列化器
   *
   * <p>工作流的节点入参、出参中可能会有文件类型，Jackson 默认不支持，需要自定义序列化</p>
   */
  @Bean
  public SimpleModule customResourceSerializerModule() {
    SimpleModule module = new SimpleModule();
    module.addSerializer(Resource.class, ToStringSerializer.instance);
    return module;
  }

  /**
   * 语言解析器，固定使用简体中文（暂不支持多语言）
   *
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

  /**
   * 初始化 Python 和 Node.js 软件源配置，供 stdio 模式的 MCP server 使用
   *
   * <p>只适用于使用博特官方镜像的容器部署环境，本地开发、非容器部署可以直接修改系统配置文件，非官方镜像可以在 Dockerfile 中处理。</p>
   *
   * <p>以下代码只考虑 Linux 容器环境。</p>
   */
  @EventListener(ApplicationStartedEvent.class)
  public void initPythonAndNpmRegistry(ApplicationStartedEvent event) {
    ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();
    String npmRegistry = environment.getProperty("bote.npm.registry");
    String pipRegistry = environment.getProperty("bote.pip.registry");

    // 修改 npm 源
    if (StringUtils.isNotEmpty(npmRegistry)) {
      Assert.isTrue(HttpUtil.isValid(npmRegistry), () -> "Invalid npm registry url: " + npmRegistry);
      String content = "registry=" + npmRegistry + "\n";
      File configFile = Paths.get(FileUtils.getUserDirectoryPath(), ".npmrc").toFile();
      try {
        FileUtils.writeStringToFile(configFile, content, StandardCharsets.UTF_8);
      }
      catch (Exception e) {
        throw new IllegalStateException("Failed to create npm config", e);
      }
    }

    // 修改 pip 源
    if (StringUtils.isNotEmpty(pipRegistry)) {
      Assert.isTrue(HttpUtil.isValid(pipRegistry), () -> "Invalid pip registry url: " + pipRegistry);
      String content = "[[index]]\nurl = \"" + pipRegistry + "\"\ndefault = true\n";
      File configFile = Paths.get(FileUtils.getUserDirectoryPath(), ".config/uv/uv.toml").toFile();
      try {
        FileUtils.forceMkdirParent(configFile);
        FileUtils.writeStringToFile(configFile, content, StandardCharsets.UTF_8);
      }
      catch (Exception e) {
        throw new IllegalStateException("Failed to create uv config", e);
      }
    }
  }

  /**
   * 请求缓存切面
   *
   * @return 请求缓存切面
   */
  @Bean
  public RequestCacheAspect requestCacheAspect(JdbcTemplate jdbcTemplate, ObjectProvider<List<RequestCacheableParamResolver>> paramResolvers) {
    return new RequestCacheAspect(jdbcTemplate, paramResolvers);
  }

}
