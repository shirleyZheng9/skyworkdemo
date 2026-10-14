package com.iwhalecloud.bote.service.portal.tiangong;

import com.iwhalecloud.bote.common.lock.DistributedLock;
import com.iwhalecloud.bote.common.lock.DistributedLockFactory;
import com.iwhalecloud.bote.common.lock.LockHelper;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserItem;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserRemoveRequest;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserSyncResponse;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserUpsertRequest;
import com.iwhalecloud.bote.portal.PortalLockConsts;
import com.iwhalecloud.bote.portal.config.properties.TiangongProjectSyncProperties;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 天工项目成员同步门面服务
 *
 * @author zhengxueli
 * @since 2024-09-23
 * <p>
 * 门面持有分布式锁，再委派给事务工作器；这样锁会覆盖事务提交，避免 finally 在提交前提前解锁。
 */
@Service
@RequiredArgsConstructor
public class TiangongProjectUserSyncServiceImpl implements ITiangongProjectUserSyncService {
  private final DistributedLockFactory distributedLockFactory;
  private final TiangongProjectUserSyncWorker worker;
  private final TiangongProjectSyncProperties properties;

  /** 先校验技术账号与请求，再在项目锁和用户锁保护下执行整个事务。 */
  @Override
  public TiangongProjectUserSyncResponse upsert(TiangongProjectUserUpsertRequest request) {
    //校验当前请求是不是一个合法的“技术会话”或者受信任调用。
    verifyTechnicalSession();
    validateUpsert(request);
    return withLocks(request.getExternalProjectId(), request.getUsers().stream()
      .map(TiangongProjectUserItem::getExtUserId).toList(), () -> worker.upsert(request));
  }

  /** 移除成员采用与新增相同的项目锁，避免两个方向的消息同时修改成员关系。 */
  @Override
  public TiangongProjectUserSyncResponse remove(TiangongProjectUserRemoveRequest request) {
    verifyTechnicalSession();
    validateRemove(request);
    return withLocks(request.getExternalProjectId(), request.getExtUserIds(), () -> worker.remove(request));
  }

  /**
   * Session 只用于确认调用者是配置的同步账号；真正的成员身份由外部用户 ID 单独解析。
   */
  private void verifyTechnicalSession() {
    LoginInfo loginInfo = SessionUtil.getLoginInfo();
    if (loginInfo == null || !properties.getSyncUserId().equals(loginInfo.getUserId())
      || !StringUtils.equals(properties.getSyncUserSystemCode(), loginInfo.getSystemCode())) {
      throw new TiangongProjectSyncException("TG_SYNC_FORBIDDEN", "仅允许配置的天工技术同步账号调用");
    }
  }

  /**
   * 提前拒绝空成员、重复成员和不在本批成员中的创建人，防止工作器写入部分错误请求。
   */
  private void validateUpsert(TiangongProjectUserUpsertRequest request) {
    validateProject(request == null ? null : request.getRequestId(), request == null ? null : request.getExternalSpaceId(),
      request == null ? null : request.getExternalProjectId());
    //检查 users 成员列表是否合法
    if (request.getUsers() == null || request.getUsers().isEmpty() || request.getUsers().size() > properties.getMaxBatchSize()) {
      throw new TiangongProjectSyncException("TG_REQUEST_INVALID", "users 数量必须在 1 到 " + properties.getMaxBatchSize() + " 之间");
    }
    Set<String> extUserIds = new HashSet<>();
    for (TiangongProjectUserItem user : request.getUsers()) {
      if (user == null || StringUtils.isBlank(user.getExtUserId())) {
        throw new TiangongProjectSyncException("TG_REQUEST_INVALID", "成员 extUserId 不能为空");
      }
      user.setExtUserId(user.getExtUserId().trim());
      user.setDisplayName(StringUtils.trimToEmpty(user.getDisplayName()));
      user.setPhoneNo(StringUtils.trimToEmpty(user.getPhoneNo()));
      if (!extUserIds.add(user.getExtUserId())) {
        throw new TiangongProjectSyncException("TG_REQUEST_INVALID", "请求中存在重复 extUserId=" + user.getExtUserId());
      }
    }
    request.setExternalSpaceId(request.getExternalSpaceId().trim());
    request.setRequestId(request.getRequestId().trim());
    if (StringUtils.isNotBlank(request.getCreatorExtUserId())) {
      request.setCreatorExtUserId(request.getCreatorExtUserId().trim());
      if (!extUserIds.contains(request.getCreatorExtUserId())) {
        throw new TiangongProjectSyncException("TG_REQUEST_INVALID", "creatorExtUserId 必须包含在 users 中");
      }
    }
  }

  /** 移除消息同样限制批量大小并拒绝重复用户，保证一条消息只产生一次成员变更。 */
  private void validateRemove(TiangongProjectUserRemoveRequest request) {
    validateProject(request == null ? null : request.getRequestId(), request == null ? null : request.getExternalSpaceId(),
      request == null ? null : request.getExternalProjectId());
    if (request.getExtUserIds() == null || request.getExtUserIds().isEmpty()
      || request.getExtUserIds().size() > properties.getMaxBatchSize()) {
      throw new TiangongProjectSyncException("TG_REQUEST_INVALID", "extUserIds 数量必须在 1 到 " + properties.getMaxBatchSize() + " 之间");
    }
    Set<String> extUserIds = new HashSet<>();
    for (int i = 0; i < request.getExtUserIds().size(); i++) {
      String extUserId = StringUtils.trimToEmpty(request.getExtUserIds().get(i));
      if (extUserId.isEmpty() || !extUserIds.add(extUserId)) {
        throw new TiangongProjectSyncException("TG_REQUEST_INVALID", "extUserIds 含空值或重复值");
      }
      request.getExtUserIds().set(i, extUserId);
    }
    request.setExternalSpaceId(request.getExternalSpaceId().trim());
    request.setRequestId(request.getRequestId().trim());
  }

  /** 项目 ID 用于定位，空间 ID 用于归属校验；两类请求均必须携带这些字段。 */
  private void validateProject(String requestId, String externalSpaceId, Long externalProjectId) {
    if (StringUtils.isBlank(requestId) || StringUtils.isBlank(externalSpaceId) || externalProjectId == null) {
      throw new TiangongProjectSyncException("TG_REQUEST_INVALID", "requestId、externalSpaceId 和 externalProjectId 均不能为空");
    }
  }

  /**
   * 固定顺序获取项目锁和用户锁，工作器事务提交后再反向释放。
   * 取锁失败返回可重试错误，并释放此前已取得的锁。
   */
  private TiangongProjectUserSyncResponse withLocks(Long externalProjectId, List<String> extUserIds,
                                                    SyncOperation operation) {
    //项目锁 + 每个用户的用户锁：同一个用户可能同时被多个不同项目同步
    List<DistributedLock> locks = new ArrayList<>();
    try {
      // 先锁项目，再按外部用户 ID 的固定顺序加锁，避免多成员消息产生死锁。
      //创建一个“项目成员同步锁”，并放到 locks 集合里
      locks.add(distributedLockFactory.getBizLock(PortalLockConsts.TIANGONG_PROJECT_USER_SYNC_LOCK,
        LockHelper.formatBizLockKey("project", String.valueOf(externalProjectId))));
      extUserIds.stream().sorted(Comparator.naturalOrder()).forEach(extUserId -> locks.add(distributedLockFactory.getBizLock(
        PortalLockConsts.PORTAL_USER_SYNC_LOCK, LockHelper.formatBizLockKey(properties.getBusinessUserSystemCode(), extUserId))));
      for (DistributedLock lock : locks) {
        if (!lock.tryLock()) {
          throw new TiangongProjectSyncException("TG_SYNC_BUSY", "天工项目成员同步繁忙，请稍后重试");
        }
      }
      return operation.execute();
    }
    finally {
      // worker 的事务方法返回时事务已完成，随后才释放锁，确保提交前的状态不会被并发请求观察到。
      for (int i = locks.size() - 1; i >= 0; i--) {
        if (locks.get(i).isHeldByCurrentThread()) {
          locks.get(i).unlock();
        }
      }
    }
  }

  @FunctionalInterface
  private interface SyncOperation {
    /** 在所有同步锁持有期间执行事务工作器。 */
    TiangongProjectUserSyncResponse execute();
  }
}
