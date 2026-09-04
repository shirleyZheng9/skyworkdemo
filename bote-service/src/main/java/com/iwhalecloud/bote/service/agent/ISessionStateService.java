package com.iwhalecloud.bote.service.agent;

import java.util.Map;
import org.springframework.lang.Nullable;

/**
 * 会话状态服务
 *
 * @author bianjp
 * @since 2026-04-13
 */
public interface ISessionStateService {

  /**
   * 获取所有作用域的会话状态
   */
  Map<String, Map<String, Object>> listAllStates(Long sessionId);

  /**
   * 获取指定作用域的会话状态
   */
  @Nullable
  Map<String, Object> getState(Long sessionId, String scope);

  /**
   * 更新会话状态
   */
  void updateState(Long sessionId, String scope, boolean merge, Map<String, Object> data);

  /**
   * 清空会话状态
   */
  void clearState(Long sessionId, String scope);
}
