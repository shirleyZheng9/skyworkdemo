package com.iwhalecloud.bote.doc.module.collaboration.socket.session;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Channel会话管理器
 * 支持多维度的channel管理，包括用户、业务、会话等多种维度
 *
 * @author Aiqing
 * @since 2025/08/29
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class ChannelSessionManager {

  /**
   * SessionInfo在Channel中的属性键
   */
  public static final AttributeKey<SessionInfo> SESSION_INFO_ATTR = AttributeKey.valueOf("SESSION_INFO");
  private static final Logger logger = LoggerFactory.getLogger(ChannelSessionManager.class);
  /**
   * 会话ID到Channel的映射
   */
  private final Map<String, ChannelHandlerContext> sessionChannelMap = new ConcurrentHashMap<>();
  /**
   * 会话ID到会话信息的映射
   */
  private final Map<String, SessionInfo> sessionInfoMap = new ConcurrentHashMap<>();
  /**
   * 用户ID到会话ID列表的映射
   */
  private final Map<Long, Set<String>> userSessionsMap = new ConcurrentHashMap<>();
  /**
   * 业务订阅表：businessType -> businessId -> 会话ID集合
   * 使用Guava Table支持多维度索引：业务类型 + 业务ID -> 订阅会话
   * 例如：("document", "doc_123") -> Set{"session1", "session2"}
   */
  private final Table<String, String, Set<String>> businessSubscriptionTable =
    HashBasedTable.create();
  /**
   * 会话业务订阅映射：会话ID -> (业务类型 -> 业务ID集合)
   * 用于快速查找某个会话订阅了哪些业务
   * 例如：session1 -> {document: [doc_123, doc_456], chatroom: [room_789]}
   */
  private final Map<String, Map<String, Set<String>>> sessionBusinessMap = new ConcurrentHashMap<>();

  /**
   * 添加会话
   */
  public void addSession(ChannelHandlerContext ctx, SessionInfo sessionInfo) {
    String sessionId = sessionInfo.getSessionId();

    // 添加到基本映射中
    sessionChannelMap.put(sessionId, ctx);
    sessionInfoMap.put(sessionId, sessionInfo);

    // 添加到用户会话映射
    if (sessionInfo.getUserId() != null) {
      userSessionsMap.computeIfAbsent(sessionInfo.getUserId(), k -> ConcurrentHashMap.newKeySet())
        .add(sessionId);
    }

    // 初始化会话的业务订阅映射
    sessionBusinessMap.put(sessionId, new ConcurrentHashMap<>());

    // 在Channel中设置会话信息
    ctx.channel().attr(SESSION_INFO_ATTR).set(sessionInfo);

    logger.trace("会话已添加: sessionId={}, userId={}", sessionId, sessionInfo.getUserId());
  }

  /**
   * 移除会话
   */
  public void removeSession(String sessionId) {
    SessionInfo sessionInfo = sessionInfoMap.remove(sessionId);
    if (sessionInfo == null) {
      return;
    }

    // 从基本映射中移除
    sessionChannelMap.remove(sessionId);

    // 从用户会话映射中移除
    if (sessionInfo.getUserId() != null) {
      Set<String> userSessions = userSessionsMap.get(sessionInfo.getUserId());
      if (userSessions != null) {
        userSessions.remove(sessionId);
        if (userSessions.isEmpty()) {
          userSessionsMap.remove(sessionInfo.getUserId());
        }
      }
    }

    // 清理该会话的所有业务订阅
    Map<String, Set<String>> sessionBusinesses = sessionBusinessMap.remove(sessionId);
    int unsubscribeCount = 0;
    if (sessionBusinesses != null) {
      for (Map.Entry<String, Set<String>> typeEntry : sessionBusinesses.entrySet()) {
        String businessType = typeEntry.getKey();
        Set<String> businessIds = typeEntry.getValue();
        for (String businessId : businessIds) {
          unsubscribeBusinessInternal(sessionId, businessType, businessId);
          unsubscribeCount++;
        }
      }
    }

    logger.trace("会话已移除: sessionId={}, userId={}, 取消订阅业务数量={}",
      sessionId, sessionInfo.getUserId(), unsubscribeCount);
  }

  /**
   * 通过Channel移除会话
   */
  public void removeSessionByChannel(ChannelHandlerContext ctx) {
    SessionInfo sessionInfo = ctx.channel().attr(SESSION_INFO_ATTR).get();
    if (sessionInfo != null) {
      removeSession(sessionInfo.getSessionId());
    }
  }

  /**
   * 获取会话对应的Channel
   */
  public ChannelHandlerContext getChannel(String sessionId) {
    return sessionChannelMap.get(sessionId);
  }

  /**
   * 获取会话信息
   */
  public SessionInfo getSessionInfo(String sessionId) {
    return sessionInfoMap.get(sessionId);
  }

  /**
   * 订阅业务
   */
  public boolean subscribeBusiness(String sessionId, String businessType, String businessId) {
    if (sessionId == null || businessType == null || businessId == null) {
      logger.warn("订阅业务参数不能为空: sessionId={}, businessType={}, businessId={}",
        sessionId, businessType, businessId);
      return false;
    }

    // 检查会话是否存在
    if (!sessionInfoMap.containsKey(sessionId)) {
      logger.warn("尝试订阅业务但会话不存在: sessionId={}, businessType={}, businessId={}",
        sessionId, businessType, businessId);
      return false;
    }

    synchronized (businessSubscriptionTable) {
      // 添加到业务订阅表
      Set<String> sessions = businessSubscriptionTable.get(businessType, businessId);
      if (sessions == null) {
        sessions = ConcurrentHashMap.newKeySet();
        businessSubscriptionTable.put(businessType, businessId, sessions);
      }
      sessions.add(sessionId);

      // 添加到会话业务映射
      Map<String, Set<String>> sessionBusinesses = sessionBusinessMap.get(sessionId);
      if (sessionBusinesses != null) {
        sessionBusinesses.computeIfAbsent(businessType, k -> ConcurrentHashMap.newKeySet())
          .add(businessId);
      }
    }

    logger.trace("会话订阅业务: sessionId={}, businessType={}, businessId={}",
      sessionId, businessType, businessId);
    return true;
  }

  /**
   * 取消订阅业务
   */
  public boolean unsubscribeBusiness(String sessionId, String businessType, String businessId) {
    if (sessionId == null || businessType == null || businessId == null) {
      logger.warn("取消订阅业务参数不能为空: sessionId={}, businessType={}, businessId={}",
        sessionId, businessType, businessId);
      return false;
    }

    synchronized (businessSubscriptionTable) {
      boolean result = unsubscribeBusinessInternal(sessionId, businessType, businessId);

      // 从会话业务映射中移除
      Map<String, Set<String>> sessionBusinesses = sessionBusinessMap.get(sessionId);
      if (sessionBusinesses != null) {
        Set<String> businessIds = sessionBusinesses.get(businessType);
        if (businessIds != null) {
          businessIds.remove(businessId);
          if (businessIds.isEmpty()) {
            sessionBusinesses.remove(businessType);
          }
        }
      }

      if (result) {
        logger.trace("会话取消订阅业务: sessionId={}, businessType={}, businessId={}",
          sessionId, businessType, businessId);
      }
      return result;
    }
  }

  /**
   * 内部取消订阅方法（不更新会话业务映射）
   */
  private boolean unsubscribeBusinessInternal(String sessionId, String businessType, String businessId) {
    Set<String> businessSessions = businessSubscriptionTable.get(businessType, businessId);
    if (businessSessions != null) {
      businessSessions.remove(sessionId);
      if (businessSessions.isEmpty()) {
        businessSubscriptionTable.remove(businessType, businessId);
      }
      return true;
    }
    return false;
  }

  /**
   * 通过Channel订阅业务
   */
  public boolean subscribeBusiness(ChannelHandlerContext ctx, String businessType, String businessId) {
    SessionInfo sessionInfo = ctx.channel().attr(SESSION_INFO_ATTR).get();
    if (sessionInfo != null) {
      return subscribeBusiness(sessionInfo.getSessionId(), businessType, businessId);
    }
    return false;
  }

  /**
   * 通过Channel取消订阅业务
   */
  public boolean unsubscribeBusiness(ChannelHandlerContext ctx, String businessType, String businessId) {
    SessionInfo sessionInfo = ctx.channel().attr(SESSION_INFO_ATTR).get();
    if (sessionInfo != null) {
      return unsubscribeBusiness(sessionInfo.getSessionId(), businessType, businessId);
    }
    return false;
  }

  /**
   * 获取会话订阅的所有业务（按业务类型分组）
   */
  public Map<String, Set<String>> getSessionBusinesses(String sessionId) {
    Map<String, Set<String>> businesses = sessionBusinessMap.get(sessionId);
    if (businesses == null) {
      return Collections.emptyMap();
    }

    // 返回不可变的副本
    Map<String, Set<String>> result = new HashMap<>();
    for (Map.Entry<String, Set<String>> entry : businesses.entrySet()) {
      result.put(entry.getKey(), Collections.unmodifiableSet(entry.getValue()));
    }
    return Collections.unmodifiableMap(result);
  }

  /**
   * 获取会话订阅的指定类型业务
   */
  public Set<String> getSessionBusinesses(String sessionId, String businessType) {
    Map<String, Set<String>> businesses = sessionBusinessMap.get(sessionId);
    if (businesses == null) {
      return Collections.emptySet();
    }

    Set<String> businessIds = businesses.get(businessType);
    return businessIds != null ? Collections.unmodifiableSet(businessIds) : Collections.emptySet();
  }

  /**
   * 获取用户的所有会话
   */
  public List<SessionInfo> getUserSessions(Long userId) {
    Set<String> sessionIds = userSessionsMap.get(userId);
    if (sessionIds == null || sessionIds.isEmpty()) {
      return Collections.emptyList();
    }

    return sessionIds.stream()
      .map(sessionInfoMap::get)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  /**
   * 获取用户指定终端类型的会话
   */
  public List<SessionInfo> getUserSessions(Long userId, Integer terminalType) {
    return getUserSessions(userId).stream()
      .filter(session -> Objects.equals(session.getTerminalType(), terminalType))
      .collect(Collectors.toList());
  }

  /**
   * 获取业务相关的所有会话
   */
  public List<SessionInfo> getBusinessSessions(String businessType, String businessId) {
    Set<String> sessionIds = businessSubscriptionTable.get(businessType, businessId);
    if (sessionIds == null || sessionIds.isEmpty()) {
      return Collections.emptyList();
    }

    return sessionIds.stream()
      .map(sessionInfoMap::get)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  /**
   * 获取业务相关的所有Channel
   */
  public List<ChannelHandlerContext> getBusinessChannels(String businessType, String businessId) {
    return getBusinessSessions(businessType, businessId).stream()
      .map(session -> sessionChannelMap.get(session.getSessionId()))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  /**
   * 获取指定业务类型的所有业务及其会话数量
   */
  public Map<String, Integer> getBusinessTypeStatistics(String businessType) {
    Map<String, Set<String>> businessesOfType = businessSubscriptionTable.row(businessType);
    Map<String, Integer> statistics = new HashMap<>();

    for (Map.Entry<String, Set<String>> entry : businessesOfType.entrySet()) {
      statistics.put(entry.getKey(), entry.getValue().size());
    }

    return statistics;
  }

  /**
   * 获取用户的所有Channel
   */
  public List<ChannelHandlerContext> getUserChannels(Long userId) {
    return getUserSessions(userId).stream()
      .map(session -> sessionChannelMap.get(session.getSessionId()))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  /**
   * 根据条件查找会话
   */
  public List<SessionInfo> findSessions(Predicate<SessionInfo> predicate) {
    return sessionInfoMap.values().stream()
      .filter(predicate)
      .collect(Collectors.toList());
  }

  /**
   * 更新会话活跃时间
   */
  public void updateSessionActiveTime(String sessionId) {
    SessionInfo sessionInfo = sessionInfoMap.get(sessionId);
    if (sessionInfo != null) {
      sessionInfo.updateLastActiveTime();
    }
  }

  /**
   * 通过Channel更新会话活跃时间
   */
  public void updateSessionActiveTime(ChannelHandlerContext ctx) {
    SessionInfo sessionInfo = ctx.channel().attr(SESSION_INFO_ATTR).get();
    if (sessionInfo != null) {
      sessionInfo.updateLastActiveTime();
    }
  }

  /**
   * 获取所有活跃会话数量
   */
  public int getActiveSessionCount() {
    return sessionInfoMap.size();
  }

  /**
   * 获取指定用户的会话数量
   */
  public int getUserSessionCount(Long userId) {
    Set<String> sessions = userSessionsMap.get(userId);
    return sessions != null ? sessions.size() : 0;
  }

  /**
   * 获取指定业务的会话数量
   */
  public int getBusinessSessionCount(String businessType, String businessId) {
    Set<String> sessions = businessSubscriptionTable.get(businessType, businessId);
    return sessions != null ? sessions.size() : 0;
  }

  /**
   * 获取所有业务类型的统计信息
   */
  public Map<String, Map<String, Integer>> getAllBusinessStatistics() {
    Map<String, Map<String, Integer>> allStats = new HashMap<>();

    for (String businessType : businessSubscriptionTable.rowKeySet()) {
      allStats.put(businessType, getBusinessTypeStatistics(businessType));
    }

    return allStats;
  }
}
