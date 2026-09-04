package com.iwhalecloud.bote.doc.config;

import com.iwhalecloud.bote.doc.common.web.filter.AutoClearFilter;
import com.iwhalecloud.bote.doc.config.properties.DimTableServerProperties;
import com.iwhalecloud.bote.doc.config.properties.NodeJsServerProperties;
import com.iwhalecloud.bote.doc.listener.DocKnowledgeQaRecordListener;
import com.iwhalecloud.bote.doc.listener.DocumentChangeListener;
import com.iwhalecloud.bote.doc.listener.DocumentChangeRebuildListener;
import com.iwhalecloud.bote.doc.listener.DocumentPathChangeListener;
import com.iwhalecloud.bote.doc.module.knowledge.semantic.IKnowledgeQuestionEmbeddingService;
import com.iwhalecloud.bote.doc.module.knowledge.semantic.impl.FallbackKnowledgeQuestionEmbeddingService;
import com.iwhalecloud.bss.litchi.disruptor.DisruptorUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * 文档中心配置
 *
 * @author Aiqing
 * @since 2025/8/19
 */
@Configuration
@MapperScan(basePackages = "com.iwhalecloud.bote.doc.module.*.mapper")
@EnableConfigurationProperties({NodeJsServerProperties.class, DimTableServerProperties.class})
public class DocAutoConfiguration {

  /**
   * 注册Disruptor消息监听器
   */
  @EventListener(ApplicationReadyEvent.class)
  public void initDisruptorListener(ApplicationReadyEvent event) {
    // 此处枚举手动注册
    DisruptorUtil disruptorUtil = DisruptorUtil.getInstance();
    ConfigurableApplicationContext applicationContext = event.getApplicationContext(); //NOPMD - suppressed CloseResource - 不能关闭
    disruptorUtil.registerListener(applicationContext.getBean(DocumentChangeListener.class));
    disruptorUtil.registerListener(applicationContext.getBean(DocumentPathChangeListener.class));
    disruptorUtil.registerListener(applicationContext.getBean(DocumentChangeRebuildListener.class));
    disruptorUtil.registerListener(applicationContext.getBean(DocKnowledgeQaRecordListener.class));
  }

  /**
   * 自动清理过滤器
   *
   * @return AutoClearFilter
   */
  @Bean
  public AutoClearFilter autoClearFilter() {
    return new AutoClearFilter();
  }

  /**
   * 文档中心独立部署时的 embedding 占位实现。
   * <p>集成部署主博特服务时，{@code bote-service} 中的实现会替换本 Bean。</p>
   */
  @Bean
  @ConditionalOnMissingBean(IKnowledgeQuestionEmbeddingService.class)
  public IKnowledgeQuestionEmbeddingService knowledgeQuestionEmbeddingService() {
    return new FallbackKnowledgeQuestionEmbeddingService();
  }
}
