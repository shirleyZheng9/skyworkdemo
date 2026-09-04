package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.skill.FlowExecutionStateDTO;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 工作流执行状态缓存
 *
 * @author bianjp
 * @since 2025-06-17
 */
@Component
public class WorkflowExecutionStausCache {
  /** 缓存时间(s) */
  private static final int EXPIRE_TIME = 3600 * 24;

  private final ICacheClient cacheClient;

  public WorkflowExecutionStausCache(CacheFactory cacheFactory) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_FLOW_EXECUTION_STATUS);
  }

  /**
   * 保存工作流执行状态
   */
  public void put(String requestId, FlowExecutionStateDTO state) {
    if (state.getCreateTime() == null) {
      state.setCreateTime(DateUtil.format());
    }
    else {
      state.setUpdateTime(DateUtil.format());
    }
    cacheClient.opsForValue().set(requestId, JsonUtil.toJsonString(state), EXPIRE_TIME, TimeUnit.SECONDS);
  }

  /**
   * 获取工作流执行状态
   */
  @Nullable
  public FlowExecutionStateDTO get(String requestId) {
    String json = cacheClient.opsForValue().get(requestId);
    if (StringUtils.isEmpty(json)) {
      return null;
    }
    return JsonUtil.parseJson(json, FlowExecutionStateDTO.class);
  }
}
