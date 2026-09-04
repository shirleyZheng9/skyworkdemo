package com.iwhalecloud.bote.service.base.impl;

import com.iwhalecloud.bote.cache.EditLockCache;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.EditLockEnum;
import com.iwhalecloud.bote.common.consts.PrivConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.EditLockInfoDTO;
import com.iwhalecloud.bote.dto.base.LockResultDTO;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.mapper.portal.TenantManageMapper;
import com.iwhalecloud.bote.service.base.IEditLockService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.iterator.KeysIterator;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 编辑锁服务实现
 *
 * @author qian.sisheng
 * @since 2025-05-08
 */
@Service
@RequiredArgsConstructor
public class EditLockServiceImpl implements IEditLockService {

  private final Logger logger = LoggerFactory.getLogger(EditLockServiceImpl.class);
  private final EditLockCache editLockCache;
  private final TenantManageMapper tenantManageMapper;

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public ResultVO<EditLockInfoDTO> acquireLock(Long id, String type, String entityName, Long tenantId) {
    Assert.notNull(id, "实体id不能为空");
    Assert.hasText(type, "实体类型不能为空");
    LoginInfo loginInfo = SessionUtil.getLoginInfo();
    String key = tenantId + CacheConsts.KEY_PREFIX_HYPHEN + type + CacheConsts.KEY_PREFIX_HYPHEN + id;
    EditLockInfoDTO editLockInfo = getEditLockInfo(key);
    if (editLockInfo == null) {
      // @formatter:off
      editLockInfo = EditLockInfoDTO.builder()
        .id(id)
        .entityName(entityName)
        .userId(loginInfo.getUserId())
        .realName(loginInfo.getRealName())
        .lockTime(System.currentTimeMillis())
        .lastUpdateTime(System.currentTimeMillis())
        .build();
      // @formatter:on
      Boolean success = editLockCache.saveIfAbsent(key, JsonUtil.toJsonString(editLockInfo));
      if (!Boolean.TRUE.equals(success)) {
        logger.debug("Acquire edit lock failed: id={}, userId={}", id, loginInfo.getUserId());
        throw new BssException("抢占编辑锁失败，请重试");
      }
    }
    else if (loginInfo.getUserId().equals(editLockInfo.getUserId())) {
      doRenewLock(editLockInfo, key);
    }
    else {
      return ResultVO.success(editLockInfo);
    }
    return ResultVO.success();
  }

  @Nullable
  private EditLockInfoDTO getEditLockInfo(String key) {
    String lockInfo = editLockCache.get(key);
    if (StringUtils.isNotEmpty(lockInfo)) {
      return JsonUtil.parseJsonRequired(lockInfo, EditLockInfoDTO.class);
    }
    return null;
  }

  private void doRenewLock(EditLockInfoDTO lockInfo, String key) {
    lockInfo.setLastUpdateTime(System.currentTimeMillis());
    editLockCache.save(key, JsonUtil.toJsonString(lockInfo));
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public ResultVO<LockResultDTO> renewLock(Long id, String type, String entityName, Long tenantId) {
    Assert.notNull(id, "实体id不能为空");
    Assert.hasText(type, "实体类型不能为空");
    Assert.hasText(entityName, "实体名称不能为空");
    Long userId = SessionUtil.getLoginInfo().getUserId();
    String key = tenantId + CacheConsts.KEY_PREFIX_HYPHEN + type + CacheConsts.KEY_PREFIX_HYPHEN + id;
    EditLockInfoDTO lockInfo = getEditLockInfo(key);
    String typName = EditLockEnum.getTypeName(type);
    String message;
    if (lockInfo == null) {
      message = "您长时间未操作【" + typName + "-" + entityName + "(" + id + ")】，前端资源已被释放。";
      return ResultVO.success(new LockResultDTO("fail.nothold", message, ""));
    }
    else if (!userId.equals(lockInfo.getUserId())) {
      logger.debug("Renew edit lock failed as others is holding the lock: id={}, userId={}, holder={}", id, userId, lockInfo.getUserId());
      message = "【" + typName + "-" + entityName + "(" + id + ")】当前已被用户【" + lockInfo.getRealName() + "】锁定，无法进行编辑。";
      return ResultVO.success(new LockResultDTO("fail.occupied", message, lockInfo.getRealName()));
    }
    else {
      doRenewLock(lockInfo, key);
    }
    return ResultVO.success(new LockResultDTO("success", "", ""));
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void release(Long id, String type, Long tenantId) {
    Assert.notNull(id, "实体id不能为空");
    Assert.hasText(type, "实体类型不能为空");
    String key = tenantId + CacheConsts.KEY_PREFIX_HYPHEN + type + CacheConsts.KEY_PREFIX_HYPHEN + id;
    Long userId = SessionUtil.getLoginInfo().getUserId();
    EditLockInfoDTO lockInfo = getEditLockInfo(key);
    if (lockInfo == null) {
      return;
    }
    if (!userId.equals(lockInfo.getUserId())) {
      logger.debug("Release edit lock failed as others is holding the lock: id={}, userId={}, holder={}", id, userId, lockInfo.getUserId());
    }
    else {
      logger.info("Release edit lock: tenantId={}, id={}, userId={}", tenantId, id, userId);
      editLockCache.delete(key);
    }
  }

  @Override
  public void unLock(Long id, String type, Long tenantId) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    String userRole = tenantManageMapper.getUserRole(tenantId, userId);
    if (!PrivConsts.ROLE_MANAGE.equals(userRole) && !SessionUtil.isSuperAdmin(userId)) {
      throw new BssException("非管理员不允许释放锁");
    }
    Assert.notNull(id, "实体id不能为空");
    Assert.hasText(type, "实体类型不能为空");
    String key = tenantId + CacheConsts.KEY_PREFIX_HYPHEN + type + CacheConsts.KEY_PREFIX_HYPHEN + id;
    EditLockInfoDTO lockInfo = getEditLockInfo(key);
    if (lockInfo != null) {
      editLockCache.delete(key);
    }
  }

  @Override
  public void releaseAllKey(@Nullable Long userId) {
    if (userId == null) {
      return;
    }
    String pattern = "*" + CacheConsts.KEY_PREFIX_HYPHEN + "*";
    try (KeysIterator iterator = editLockCache.scan(pattern)) {
      if (iterator.hasNext()) {
        // 获取编辑器锁相关 key 列表
        List<String> keyList = new ArrayList<>(iterator.next());
        for (String key : keyList) {
          // 是否退出登录的用户ID所持有的编辑器锁
          EditLockInfoDTO lockInfo = getEditLockInfo(key);
          if (lockInfo != null && Objects.equals(userId, lockInfo.getUserId())) {
            editLockCache.delete(key);
          }
        }
      }
    }
  }

}

