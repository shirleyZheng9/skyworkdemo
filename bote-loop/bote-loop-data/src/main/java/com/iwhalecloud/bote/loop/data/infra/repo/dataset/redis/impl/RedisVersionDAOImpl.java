package com.iwhalecloud.bote.loop.data.infra.repo.dataset.redis.impl;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.loop.data.infra.rediskey.RedisKeyFormatter;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.redis.RedisVersionDAO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import org.springframework.stereotype.Repository;

/**
 * 数据集版本Redis DAO实现类
 * 迁移对应关系: Go语言VersionDAOImpl
 * - 功能: 实现数据集版本Redis缓存操作
 * - 方法实现: 各种数据集版本Redis缓存操作方法实现
 * <p>
 * Java实现说明:
 * - 对应Go的VersionDAOImpl结构体
 * - 使用ICacheClient实现Redis操作
 * - 提供数据集版本项目数量缓存操作实现
 * <p>
 * 技术栈迁移:
 * - Go Redis客户端 -> Java ICacheClient
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
@Repository
public class RedisVersionDAOImpl implements RedisVersionDAO {
  private final RedisKeyFormatter redisKeyFormatter;
  private final ICacheClient cacheClient;

  public RedisVersionDAOImpl(CacheFactory cacheFactory, RedisKeyFormatter redisKeyFormatter) {
    this.redisKeyFormatter = redisKeyFormatter;
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_WECHAT_AUTH);
  }

  /**
   * 获取版本项目数量
   * 迁移对应关系: Go语言VersionDAOImpl.GetItemCountOfVersion
   * - 功能: 获取数据集版本项目数量
   * - 参数: versionID - 版本ID
   * - 返回: 项目数量（可能为null）
   * - 用途: 从Redis获取数据集版本项目数量
   */
  @Override
  public Long getItemCountOfVersion(Long versionID) {
    if (versionID == null || versionID <= 0) {
      throw new BssException("versionID is required");
    }

    try {
      String key = redisKeyFormatter.formatDatasetVersionItemCountKey(versionID);
      String value = cacheClient.opsForValue().get(key);
      if (value == null || value.isEmpty()) {
        // Redis中没有值，返回0
        return 0L;
      }
      return Long.parseLong(value);
    }
    catch (NumberFormatException e) {
      throw new BssException("Invalid number format for versionID=" + versionID, e);
    }
    catch (Exception e) {
      throw new BssException("GetItemCountOfVersion failed, versionID=" + versionID, e);
    }
  }

  /**
   * 设置版本项目数量
   * 迁移对应关系: Go语言VersionDAOImpl.SetItemCountOfVersion
   * - 功能: 设置数据集版本项目数量
   * - 参数: versionID - 版本ID, n - 数量
   * - 用途: 设置数据集版本项目数量到Redis
   */
  @Override
  public void setItemCountOfVersion(Long versionID, Long n) {
    if (versionID == null || versionID <= 0 || n == null) {
      throw new BssException("versionID and n are required");
    }

    try {
      String key = redisKeyFormatter.formatDatasetVersionItemCountKey(versionID);
      cacheClient.opsForValue().set(key, String.valueOf(n));
    }
    catch (Exception e) {
      throw new BssException("SetItemCountOfVersion failed, versionID=" + versionID + ", n=" + n, e);
    }
  }
}
