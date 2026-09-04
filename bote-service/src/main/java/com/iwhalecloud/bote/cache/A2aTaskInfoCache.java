package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.a2a.A2aTaskInfo;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.a2a.spec.Task;
import io.a2a.spec.TaskState;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * A2A 任务信息缓存
 *
 * <p>用于缓存调用外部 A2A 服务的任务信息</p>
 *
 * @author bianjp
 * @since 2025-10-25
 */
@Component
public class A2aTaskInfoCache {
  /** A2A 任务信息缓存有效时间: 30d */
  private static final int TASK_CACHE_EXPIRE_SECONDS = 3600 * 24 * 30;
  /** A2A contextId 缓存 key 后缀。contextId 需要跨任务使用，因此和任务信息使用不同的缓存 key 存储 */
  private static final String KEY_SUFFIX_CONTEXT_ID = ":contextId";

  private final ICacheClient client;

  public A2aTaskInfoCache(CacheFactory cacheFactory) {
    this.client = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_A2A_TASK_INFO);
  }

  /**
   * 获取任务信息
   */
  public A2aTaskInfo get(String key) {
    String json = client.opsForValue().get(key);
    A2aTaskInfo taskInfo;
    if (json == null || json.isEmpty()) {
      taskInfo = new A2aTaskInfo();
    }
    else {
      taskInfo = JsonUtil.parseJsonRequired(json, A2aTaskInfo.class);
    }
    taskInfo.setCacheKey(key);
    // 无论是否是新任务，都要获取 contextId
    taskInfo.setContextId(client.opsForValue().get(key + KEY_SUFFIX_CONTEXT_ID));
    return taskInfo;
  }

  /**
   * 保存任务信息
   */
  public void save(A2aTaskInfo taskInfo, Task task) {
    taskInfo.setTaskId(task.getId());
    taskInfo.setContextId(task.getContextId());
    taskInfo.setTaskState(task.getStatus().state());

    // 无论任务是否结束，都要更新、续期 contextId
    String contextId = taskInfo.getContextId();
    if (StringUtils.isNotEmpty(contextId)) {
      client.opsForValue().set(taskInfo.getCacheKey() + KEY_SUFFIX_CONTEXT_ID, contextId, TASK_CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }
    // 如果任务已终结，下次会话就不能再使用，清除缓存
    TaskState taskState = taskInfo.getTaskState();
    if (taskState != null && taskState.isFinal()) {
      client.delete(taskInfo.getCacheKey());
      return;
    }
    String value = JsonUtil.toJsonString(taskInfo);
    client.opsForValue().set(taskInfo.getCacheKey(), value, TASK_CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
  }

  /**
   * 删除任务信息
   */
  public void delete(A2aTaskInfo taskInfo) {
    client.delete(taskInfo.getCacheKey());
  }
}
