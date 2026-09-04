package com.iwhalecloud.bote.service.agent.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.entity.agent.SessionStateEntity;
import com.iwhalecloud.bote.mapper.agent.SessionStateMapper;
import com.iwhalecloud.bote.service.agent.ISessionStateService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会话状态服务
 *
 * @author bianjp
 * @since 2026-04-13
 */
@Service
@RequiredArgsConstructor
public class SessionStateServiceImpl implements ISessionStateService {
  private final SessionStateMapper sessionStateMapper;

  @Override
  public Map<String, Map<String, Object>> listAllStates(Long sessionId) {
    List<SessionStateEntity> entities = sessionStateMapper.selectBySessionId(sessionId);
    if (!entities.isEmpty()) {
      Map<String, Map<String, Object>> allDataMap = new LinkedHashMap<>();
      for (SessionStateEntity entity : entities) {
        Map<String, Object> data = parseDataValue(entity.getStateData());
        if (!data.isEmpty()) {
          allDataMap.put(entity.getStateScope(), data);
        }
      }
      return allDataMap;
    }
    return Map.of();
  }

  @Override
  @Nullable
  public Map<String, Object> getState(Long sessionId, String scope) {
    SessionStateEntity entity = sessionStateMapper.selectBySessionIdAndScope(sessionId, scope);
    if (entity == null || StringUtils.isEmpty(entity.getStateData())) {
      return null;
    }
    return parseDataValue(entity.getStateData());
  }

  @Override
  public void updateState(Long sessionId, String scope, boolean merge, Map<String, Object> data) {
    SessionStateEntity existing = sessionStateMapper.selectBySessionIdAndScope(sessionId, scope);
    Map<String, Object> newData;
    if (merge) {
      newData = existing == null ? new LinkedHashMap<>() : new LinkedHashMap<>(parseDataValue(existing.getStateData()));
      for (Entry<String, Object> entry : data.entrySet()) {
        // value=null 时删除 key
        if (entry.getValue() == null) {
          newData.remove(entry.getKey());
        }
        else {
          newData.put(entry.getKey(), entry.getValue());
        }
      }
    }
    else {
      newData = data;
    }

    SessionStateEntity entity = new SessionStateEntity();
    entity.setStateData(JsonUtil.toJsonString(newData));
    if (existing == null) {
      entity.setId(IDUtils.nextId());
      entity.setSessionId(sessionId);
      entity.setStateScope(scope);
      sessionStateMapper.insert(entity);
    }
    else {
      entity.setId(existing.getId());
      sessionStateMapper.update(entity);
    }
  }

  @Override
  @Transactional
  public void clearState(Long sessionId, String scope) {
    sessionStateMapper.deleteBySessionIdAndScope(sessionId, scope);
  }

  /**
   * 解析数据
   */
  private static Map<String, Object> parseDataValue(@Nullable String json) {
    if (StringUtils.isEmpty(json)) {
      return Map.of();
    }
    Map<String, Object> data = JsonUtil.parseJson(json, new TypeReference<>() {
    });
    return data != null ? data : Map.of();
  }
}
