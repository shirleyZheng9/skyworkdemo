package com.iwhalecloud.bote.service.a2a.helper;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.a2a.server.tasks.TaskStateProvider;
import io.a2a.server.tasks.TaskStore;
import io.a2a.spec.Task;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * A2A 任务存储器
 *
 * <p>参考 {@link io.a2a.server.tasks.InMemoryTaskStore}, 使用分布式缓存存储以兼容多节点部署、节点下线。</p>
 *
 * @author bianjp
 * @since 2025-09-13
 */
@Component
public class A2aTaskStore implements TaskStore, TaskStateProvider {
  /** 失效时间: 7 天 */
  private static final long EXPIRE_SECONDS = 3600 * 24 * 7;

  private final ICacheClient cacheClient;

  public A2aTaskStore(CacheFactory cacheFactory) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_A2A_TASK);
  }

  @Override
  public void save(Task task) {
    cacheClient.opsForValue().set(task.getId(), JsonUtil.toJsonString(task), EXPIRE_SECONDS, TimeUnit.SECONDS);
  }

  @Override
  @Nullable
  public Task get(String taskId) {
    String json = cacheClient.opsForValue().get(taskId);
    if (StringUtils.isEmpty(json)) {
      return null;
    }
    return JsonUtil.parseJsonRequired(json, Task.class);
  }

  @Override
  public void delete(String taskId) {
    cacheClient.delete(taskId);
  }

  @Override
  public boolean isTaskActive(String taskId) {
    Task task = get(taskId);
    if (task == null) {
      return false;
    }
    // Task is active if not in final state
    return task.getStatus() == null || task.getStatus().state() == null || !task.getStatus().state().isFinal();
  }

  @Override
  public boolean isTaskFinalized(String taskId) {
    Task task = get(taskId);
    if (task == null) {
      return false;
    }
    // Task is finalized if in final state (ignores grace period)
    return task.getStatus() != null && task.getStatus().state() != null && task.getStatus().state().isFinal();
  }
}
