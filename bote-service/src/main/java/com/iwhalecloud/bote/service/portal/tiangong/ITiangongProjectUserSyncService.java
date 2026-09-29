package com.iwhalecloud.bote.service.portal.tiangong;

import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserRemoveRequest;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserSyncResponse;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserUpsertRequest;

/**
 * 天工项目成员同步服务
 *
 * @author zhengxueli
 * @since 2024-09-23
 * <p>
 * 为天工 MQ 提供独立的项目成员写入通道，与页面租户成员接口隔离。
 */
public interface ITiangongProjectUserSyncService {
  /** 校验技术账号并在分布式锁保护下新增成员、绑定创建人。 */
  TiangongProjectUserSyncResponse upsert(TiangongProjectUserUpsertRequest request);

  /** 校验技术账号并在分布式锁保护下移除成员。 */
  TiangongProjectUserSyncResponse remove(TiangongProjectUserRemoveRequest request);
}
