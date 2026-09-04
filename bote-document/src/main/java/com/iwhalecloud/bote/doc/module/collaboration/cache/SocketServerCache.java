package com.iwhalecloud.bote.doc.module.collaboration.cache;

import com.iwhalecloud.bote.doc.consts.DocCacheConsts;
import com.iwhalecloud.bote.doc.module.collaboration.constant.SocketConst;
import com.iwhalecloud.bote.doc.module.collaboration.doc.dto.NodePublishEvent;
import com.iwhalecloud.bote.doc.module.collaboration.doc.listener.DocsMessageSubscriber;
import com.iwhalecloud.bote.doc.module.collaboration.doc.vo.SocketTokenVO;
import com.iwhalecloud.bote.doc.module.collaboration.pubsub.MessagePubSubTemplate;
import com.iwhalecloud.bote.doc.module.collaboration.socket.auth.UserAuthInfo;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * Socket服务器缓存管理器
 * 负责管理WebSocket服务的缓存操作，包括：
 * 1. 用户认证信息缓存
 * 2. 服务器ID管理
 * 3. 文档协作消息的发布订阅管理
 *
 * @author Aiqing
 * @since 2025/8/29
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class SocketServerCache implements InitializingBean {
  public static final int TOKEN_EXPIRE_HOURS = 24;
  private static final Logger logger = LoggerFactory.getLogger(SocketServerCache.class);
  private static final String SOCKET_AUTH_KEY_PREFIX = "socket_token:";
  private static final String SOCKET_USER_AUTH_KEY_PREFIX = "user_socket_token:";

  private static final String DOCUMENT_COLLABORATE_USER = "document_collab_users:";

  /**
   * Token续期配置
   */
  private static final int TOKEN_RENEWAL_THRESHOLD_HOURS = 2; // 距离过期2小时时开始续期
  private static final int TOKEN_CACHE_EXPIRE_HOURS = 25; // 缓存过期时间（比token过期时间稍长）
  private final ICacheClient cacheClient;
  private final DocsMessageSubscriber docsMessageSubscriber;
  private final MessagePubSubTemplate messagePubSubTemplate;

  /**
   * 存储已订阅的文档ID及其对应的订阅ID
   * Key: documentId, Value: subscriptionId
   */
  private final Map<String, String> subscriptions = new ConcurrentHashMap<>();

  public SocketServerCache(CacheFactory cacheFactory, DocsMessageSubscriber messageSubscriber,
                           MessagePubSubTemplate messagePubSubTemplate) {
    this.cacheClient = cacheFactory.getCacheClient(DocCacheConsts.GROUP_DOC, DocCacheConsts.CACHE_PREFIX_SOCKET_SERVER);
    this.docsMessageSubscriber = messageSubscriber;
    this.messagePubSubTemplate = messagePubSubTemplate;
  }


  /**
   * serverId 自增
   *
   * @return 自增后的序号
   */
  public long incrementServerId() {
    Long increment = this.cacheClient.opsForValue().increment(SocketConst.CACHE_MAX_SERVER_ID, 1);
    return Optional.ofNullable(increment).orElse(0L);
  }

  /**
   * 缓存用户的临时认证信息
   *
   * @param token token
   * @param authInfo 用户信息
   * @param userId 用户ID
   */
  public void setUserSocketAuthToken(String token, UserAuthInfo authInfo, Long userId) {
    String authInfoJson = JsonUtil.toJsonString(authInfo);

    // 设置token对应的认证信息，带过期时间
    this.cacheClient.opsForValue().set(SOCKET_AUTH_KEY_PREFIX + token, authInfoJson, TOKEN_CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

    // 设置用户ID对应的token，带过期时间
    this.cacheClient.opsForValue().set(SOCKET_USER_AUTH_KEY_PREFIX + userId, token, TOKEN_CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

    logger.trace("缓存用户Socket认证信息: userId={}, token={}, expireTime={}",
      userId, token, authInfo.getExpireTime());
  }

  /**
   * 根据认证token获取用户
   *
   * @param token token
   * @return 连接用户信息
   */
  public UserAuthInfo getUserSocketAuth(String token) {
    String userStr = this.cacheClient.opsForValue().get(SOCKET_AUTH_KEY_PREFIX + token);
    if (StringUtils.isBlank(userStr)) {
      return null;
    }
    return JsonUtil.parseJson(userStr, UserAuthInfo.class);
  }

  /**
   * 获取用户已存在的token
   *
   * @param userId userId
   * @return 连接用户信息
   */
  public SocketTokenVO getUserExistSocketAuthToken(Long userId) {
    String token = this.cacheClient.opsForValue().get(SOCKET_USER_AUTH_KEY_PREFIX + userId);
    if (StringUtils.isBlank(token)) {
      return null;
    }
    UserAuthInfo userSocketAuth = this.getUserSocketAuth(token);
    if (userSocketAuth == null) {
      this.cacheClient.delete(SOCKET_USER_AUTH_KEY_PREFIX + userId);
      return null;
    }
    // 已过期
    if (userSocketAuth.isExpired()) {
      return null;
    }
    // 检查是否需要续期
    if (shouldRenewToken(userSocketAuth)) {
      renewToken(userSocketAuth, token, userId);
    }
    long expireMills = userSocketAuth.getExpireTime()
      .atZone(ZoneId.systemDefault())
      .toInstant()
      .toEpochMilli();
    return new SocketTokenVO(token, expireMills);
  }

  /**
   * 缓存文档的实时协作用户
   *
   * @param documentId 文档ID
   * @param userId 用户ID
   */
  public void addDocumentCollabUsers(String documentId, Long userId) {
    this.cacheClient.opsForSet().add(DOCUMENT_COLLABORATE_USER + documentId, String.valueOf(userId));
  }

  public void removeDocumentCollabUser(String documentId, Long userId) {
    this.cacheClient.opsForSet().remove(DOCUMENT_COLLABORATE_USER + documentId, String.valueOf(userId));
  }

  /**
   * 订阅文档协作消息
   *
   * @param documentId 文档ID
   */
  public void subscribeForCollab(String documentId) {
    if (StringUtils.isBlank(documentId)) {
      logger.warn("文档ID不能为空");
      return;
    }

    if (!messagePubSubTemplate.isSupported()) {
      logger.warn("当前缓存不支持发布订阅模式");
      return;
    }

    // 检查是否已经订阅
    if (subscriptions.containsKey(documentId)) {
      logger.debug("文档 {} 已经订阅，跳过重复订阅", documentId);
      return;
    }

    try {
      String channelName = docsMessageSubscriber.encodeSubscribeKey(documentId);
      String subscriptionId = messagePubSubTemplate.subscribe(channelName, docsMessageSubscriber);

      if (subscriptionId != null) {
        subscriptions.put(documentId, subscriptionId);
        logger.debug("成功订阅文档协作消息: documentId={}, channel={}, subscriptionId={}, 当前订阅数={}",
          documentId, channelName, subscriptionId, subscriptions.size());
      }
      else {
        logger.warn("订阅文档协作消息失败: documentId={}, channel={}", documentId, channelName);
      }
    }
    catch (Exception e) {
      logger.error("订阅文档协作消息失败: documentId={}, error={}", documentId, e.getMessage(), e);
    }
  }

  /**
   * 取消订阅文档协作消息
   *
   * @param documentId 文档ID
   */
  public void unsubscribeForCollab(String documentId) {
    if (StringUtils.isBlank(documentId)) {
      logger.warn("文档ID不能为空");
      return;
    }

    if (!messagePubSubTemplate.isSupported()) {
      logger.warn("当前缓存不支持发布订阅模式");
      return;
    }

    String subscriptionId = subscriptions.remove(documentId);
    if (subscriptionId == null) {
      logger.debug("文档 {} 未订阅，无需取消订阅", documentId);
      return;
    }

    try {
      String channelName = docsMessageSubscriber.encodeSubscribeKey(documentId);
      messagePubSubTemplate.unsubscribe(channelName, subscriptionId);
      logger.info("成功取消订阅文档协作消息: documentId={}, subscriptionId={}", documentId, subscriptionId);
    }
    catch (Exception e) {
      logger.error("取消订阅文档协作消息失败: documentId={}, error={}", documentId, e.getMessage(), e);
    }
  }

  /**
   * 清理所有订阅并停止发布订阅服务
   * 通常在应用关闭时调用
   *
   * 清理流程：
   * 1. 通过 unsubscribe 清理业务层和底层实现层的订阅
   * 2. 停止底层发布订阅服务（会进行防御性清理并停止容器）
   */
  public void clearAllSubscriptions() {
    if (messagePubSubTemplate == null || !messagePubSubTemplate.isSupported()) {
      return;
    }

    logger.info("开始清理所有文档协作订阅，当前订阅数量: {}", subscriptions.size());

    // 通过 unsubscribe 清理订阅，这会同时清理业务层和底层实现层的订阅
    for (Map.Entry<String, String> entry : subscriptions.entrySet()) {
      try {
        String documentId = entry.getKey();
        String subscriptionId = entry.getValue();
        String channelName = docsMessageSubscriber.encodeSubscribeKey(documentId);
        messagePubSubTemplate.unsubscribe(channelName, subscriptionId);
        logger.debug("清理订阅: documentId={}, subscriptionId={}", documentId, subscriptionId);
      }
      catch (Exception e) {
        logger.warn("清理订阅失败: documentId={}, error={}", entry.getKey(), e.getMessage());
      }
    }

    // 清空业务层的订阅映射
    subscriptions.clear();
    logger.info("清理所有文档协作订阅完成");

    // 停止底层发布订阅服务（会进行防御性清理并停止容器）
    try {
      messagePubSubTemplate.stop();
    }
    catch (Exception e) {
      logger.error("停止消息发布订阅服务失败", e);
    }
  }

  /**
   * 发布消息到指定文档
   *
   * @param documentId 文档ID
   * @param event 消息
   * @return 是否成功发布
   */
  public boolean publish(String documentId, NodePublishEvent event) {
    if (StringUtils.isBlank(documentId)) {
      logger.warn("文档ID不能为空");
      return false;
    }

    if (StringUtils.isBlank(event.getMessage())) {
      logger.warn("消息内容不能为空");
      return false;
    }

    if (!messagePubSubTemplate.isSupported()) {
      logger.warn("当前缓存不支持发布订阅模式");
      return false;
    }

    try {
      String channelKey = docsMessageSubscriber.encodeSubscribeKey(documentId);
      boolean success = messagePubSubTemplate.publish(channelKey, JsonUtil.toJsonString(event));

      if (success) {
        logger.debug("成功发布消息到文档: documentId={}, channel={}, messageLength={}",
          documentId, channelKey, event.getMessage().length());
      }
      return success;
    }
    catch (Exception e) {
      logger.error("发布消息失败: documentId={}, error={}", documentId, e.getMessage(), e);
      return false;
    }
  }

  /**
   * Spring容器销毁时自动清理所有订阅
   */
  @PreDestroy
  public void destroy() {
    logger.info("SocketServerCache销毁，清理所有订阅");
    clearAllSubscriptions();
  }

  /**
   * 检查是否需要续期token
   *
   * @param authInfo 用户认证信息
   * @return 是否需要续期
   */
  private boolean shouldRenewToken(UserAuthInfo authInfo) {
    if (authInfo == null || authInfo.getExpireTime() == null) {
      return true;
    }
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime expireTime = authInfo.getExpireTime();

    // 如果已经过期，不需要续期
    if (now.isAfter(expireTime)) {
      logger.debug("Token已过期，不需要续期: userId={}, expireTime={}",
        authInfo.getUserId(), expireTime);
      return false;
    }

    // 计算距离过期的时间
    long hoursUntilExpire = ChronoUnit.HOURS.between(now, expireTime);

    // 如果距离过期时间小于等于续期阈值，则需要续期
    boolean shouldRenew = hoursUntilExpire <= TOKEN_RENEWAL_THRESHOLD_HOURS;

    if (shouldRenew) {
      logger.trace("Token需要续期: userId={}, hoursUntilExpire={}, threshold={}",
        authInfo.getUserId(), hoursUntilExpire, TOKEN_RENEWAL_THRESHOLD_HOURS);
    }

    return shouldRenew;
  }

  /**
   * 续期token
   *
   * @param authInfo 原始认证信息
   * @param token token
   * @param userId 用户ID
   */
  private void renewToken(UserAuthInfo authInfo, String token, Long userId) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime newExpireTime = now.plusHours(TOKEN_EXPIRE_HOURS);
    // 创建续期后的认证信息
    authInfo.setExpireTime(newExpireTime);
    // 更新缓存
    setUserSocketAuthToken(token, authInfo, userId);
    logger.trace("Token续期成功: userId={}, token={}, newExpireTime={}, duration={}小时",
      userId, token, newExpireTime, TOKEN_EXPIRE_HOURS);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (messagePubSubTemplate != null) {
      messagePubSubTemplate.start();
    }
  }
}
