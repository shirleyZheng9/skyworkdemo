package com.iwhalecloud.bote.config;

import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.service.a2a.helper.A2aTaskStore;
import com.iwhalecloud.bote.service.a2a.helper.CustomA2AHttpClient;
import io.a2a.client.http.A2AHttpClient;
import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.events.InMemoryQueueManager;
import io.a2a.server.events.QueueManager;
import io.a2a.server.requesthandlers.DefaultRequestHandler;
import io.a2a.server.requesthandlers.RequestHandler;
import io.a2a.server.tasks.BasePushNotificationSender;
import io.a2a.server.tasks.PushNotificationConfigStore;
import io.a2a.server.tasks.PushNotificationSender;
import io.a2a.server.tasks.TaskStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

/**
 * A2A 自动配置
 *
 * @author bianjp
 * @since 2025-09-13
 */
@AutoConfiguration
@PropertySource("classpath:META-INF/a2a-defaults.properties")
public class BoteA2aAutoConfiguration {

  /**
   * A2A HTTP 客户端
   *
   * <p>有两个用途: 1. A2A 客户端调用 A2A 服务器接口; 2. A2A 服务器调用 A2A 客户端提供的通知接口</p>
   */
  @Bean
  public A2AHttpClient a2AHttpClient() {
    return new CustomA2AHttpClient();
  }

  @Bean
  public QueueManager a2aQueueManager(A2aTaskStore taskStore) {
    // TODO 改用分布式队列以兼容多节点部署的情况
    return new InMemoryQueueManager(taskStore);
  }

  @Bean
  public PushNotificationSender a2aPushNotificationSender(PushNotificationConfigStore pushNotificationConfigStore, A2AHttpClient httpClient) {
    return new BasePushNotificationSender(pushNotificationConfigStore, httpClient);
  }

  @Bean
  public RequestHandler a2aRequestHandler(AgentExecutor agentExecutor, TaskStore taskStore, QueueManager queueManager,
                                          PushNotificationConfigStore pushNotificationConfigStore, PushNotificationSender pushSender) {
    return new DefaultRequestHandler(agentExecutor, taskStore, queueManager, pushNotificationConfigStore, pushSender, ThreadPools.getA2a());
  }
}
