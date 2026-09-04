package com.iwhalecloud.bote.service.base;

import com.iwhalecloud.bote.dto.base.EditLockInfoDTO;
import com.iwhalecloud.bote.dto.base.LockResultDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import org.springframework.lang.Nullable;

/**
 * 编辑锁服务
 *
 * @author qian.sisheng
 * @since 2025-05-09
 */
public interface IEditLockService {

  /**
   * 获取编辑锁
   *
   * @param id 实体id
   * @param type 实体类型
   * @param entityName 实体名称
   * @param tenantId 租户 ID
   * @return 编辑锁信息
   */
  ResultVO<EditLockInfoDTO> acquireLock(Long id, String type, String entityName, Long tenantId);

  /**
   * 续锁
   *
   * @param id 实体id
   * @param type 实体类型
   * @param entityName 实体名称
   * @param tenantId 租户 ID
   * @return 续锁结果
   */
  ResultVO<LockResultDTO> renewLock(Long id, String type, String entityName, Long tenantId);

  /**
   * 释放锁
   *
   * @param id 实体id
   * @param type 实体类型
   */
  void release(Long id, String type, Long tenantId);

  /**
   * 解锁
   *
   * @param id 实体id
   * @param type 实体类型
   * @param tenantId 租户 ID
   */
  void unLock(Long id, String type, Long tenantId);

  /**
   * 释放指定用户下的所有资源锁，用户退出登录时需要释放资源锁
   * @param userId 用户ID
   */
  void releaseAllKey(@Nullable Long userId);
}
