package com.iwhalecloud.bote.doc.module.collaboration.cache;

import com.iwhalecloud.bote.doc.consts.DocCacheConsts;
import com.iwhalecloud.bote.doc.module.collaboration.constant.SocketConst;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.service.WorkbookFacade.ChangesetMessage;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 在线表格协作缓存
 * @author Aiqing
 * @since 2025/9/3
 */
@Component
public class WorkbookCollaborationCache {
  private static final Logger logger = LoggerFactory.getLogger(WorkbookCollaborationCache.class);

  private static final String ROOM_USER_CACHE_KEY = "workbook:room:user:";

  private final ICacheClient cacheClient;

  public WorkbookCollaborationCache(CacheFactory cacheFactory) {
    this.cacheClient = cacheFactory.getCacheClient(DocCacheConsts.GROUP_DOC, DocCacheConsts.CACHE_PREFIX_WORKBOOK);
  }


  /**
   * 加入房间
   *
   * @param roomIdList 房间ID
   */
  public void joinRoom(List<String> roomIdList, Long userId) {
    roomIdList.forEach(roomId -> {
      this.cacheClient.opsForSet().add(ROOM_USER_CACHE_KEY + roomId, String.valueOf(userId));
    });
  }

  /**
   * 查询房间中的用户
   *
   * @param roomId 房间ID
   * @return 用户ID
   */
  public List<String> roomMembers(String roomId) {
    Set<String> members = this.cacheClient.opsForSet().members(ROOM_USER_CACHE_KEY + roomId);
    if (members == null) {
      return new ArrayList<>();
    }
    return new ArrayList<>(members);
  }

  public void pushToChangesetQueue(String documentId, ChangesetMessage changesetMessage) {
    String queueKey = SocketConst.CHANGESET_QUEUE_KEY + documentId;
    this.cacheClient.opsForList().rightPush(queueKey, JsonUtil.toJsonString(changesetMessage));
    // 4. 设置队列过期时间（24小时）
    this.cacheClient.expire(queueKey, 24, TimeUnit.HOURS);
    logger.debug("在线表格变更消息已加入队列: documentId={}, queueKey={}", documentId, queueKey);
  }

  public List<String> popChangesetQueue(String queueKey, int count) {
    return this.cacheClient.opsForList().leftPop(queueKey, count);
  }
}
