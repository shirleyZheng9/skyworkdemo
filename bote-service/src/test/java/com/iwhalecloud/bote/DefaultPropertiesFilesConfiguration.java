package com.iwhalecloud.bote;

import org.springframework.context.annotation.PropertySource;

/**
 * 默认配置文件配置
 *
 * <p>只用于让 IDE 识别到默认配置文件是 Spring 配置以支持语法高亮、智能提示。</p>
 *
 * @author bianjp
 * @since 2024-07-14
 */
@PropertySource({
  "classpath:config/default/bote.properties",
  "classpath:config/default/cache.properties",
  "classpath:config/default/dfs.properties",
  "classpath:config/default/diffc.properties",
  "classpath:config/default/ftf.properties",
  "classpath:config/default/jdbc.properties",
  "classpath:config/default/job.properties",
  "classpath:config/default/lock.properties",
  "classpath:config/default/portal.properties",
  "classpath:config/default/spring.properties",
  "classpath:config/default/thread-pool.properties"
})
@SuppressWarnings("unused")
public class DefaultPropertiesFilesConfiguration {
}
