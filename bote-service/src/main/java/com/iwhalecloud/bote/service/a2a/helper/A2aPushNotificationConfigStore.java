package com.iwhalecloud.bote.service.a2a.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.a2a.server.tasks.PushNotificationConfigStore;
import io.a2a.spec.PushNotificationConfig;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * A2A 推送通知配置存储器
 *
 * <p>参考 {@link io.a2a.server.tasks.InMemoryPushNotificationConfigStore}, 使用分布式缓存存储以兼容多节点部署、节点下线。</p>
 *
 * @author bianjp
 * @since 2025-09-15
 */
@Component
public class A2aPushNotificationConfigStore implements PushNotificationConfigStore {
  /** 失效时间: 7 天 */
  private static final long EXPIRE_SECONDS = 3600 * 24 * 7;

  private final ICacheClient cacheClient;

  public A2aPushNotificationConfigStore(CacheFactory cacheFactory) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_A2A_PUSH_CONFIG);
  }

  @Override
  public PushNotificationConfig setInfo(String taskId, PushNotificationConfig notificationConfig) {
    // 通知配置的 id 为空时，使用任务 id
    if (StringUtils.isEmpty(notificationConfig.id())) {
      notificationConfig = new PushNotificationConfig.Builder(notificationConfig).id(taskId).build();
    }
    List<PushNotificationConfig> oldConfigList = getInfo(taskId);
    List<PushNotificationConfig> newConfigList;
    if (CollectionUtils.isEmpty(oldConfigList)) {
      newConfigList = List.of(notificationConfig);
    }
    else {
      newConfigList = new ArrayList<>(oldConfigList);
      String configId = notificationConfig.id();
      // 存在相同 id 的配置时更新，否则添加
      int pos = ListUtils.indexOf(newConfigList, config -> configId.equals(config.id()));
      if (pos >= 0) {
        newConfigList.set(pos, notificationConfig);
      }
      else {
        newConfigList.add(notificationConfig);
      }
    }

    cacheClient.opsForValue().set(taskId, JsonUtil.toJsonString(newConfigList), EXPIRE_SECONDS);
    return notificationConfig;
  }

  @Override
  @Nullable
  public List<PushNotificationConfig> getInfo(String taskId) {
    String json = cacheClient.opsForValue().get(taskId);
    if (StringUtils.isEmpty(json)) {
      return null;
    }
    return JsonUtil.parseJsonRequired(json, new TypeReference<>() {
    });
  }

  @Override
  public void deleteInfo(String taskId, String configId) {
    // 通知配置的 id 为空时，使用任务 id
    String finalConfigId = StringUtils.isNotEmpty(configId) ? configId : taskId;
    List<PushNotificationConfig> notificationConfigList = getInfo(taskId);
    if (notificationConfigList == null || notificationConfigList.isEmpty()) {
      return;
    }

    List<PushNotificationConfig> newConfigList = notificationConfigList.stream().filter(config -> !finalConfigId.equals(config.id())).toList();
    // 删除后为空，直接删除缓存
    if (newConfigList.isEmpty()) {
      cacheClient.delete(taskId);
    }
    // 删除后有变化，更新缓存
    else if (newConfigList.size() < notificationConfigList.size()) {
      cacheClient.opsForValue().set(taskId, JsonUtil.toJsonString(newConfigList), EXPIRE_SECONDS);
    }
  }
}
