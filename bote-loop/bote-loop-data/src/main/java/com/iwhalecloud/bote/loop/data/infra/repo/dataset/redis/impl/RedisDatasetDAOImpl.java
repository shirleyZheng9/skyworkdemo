package com.iwhalecloud.bote.loop.data.infra.repo.dataset.redis.impl;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.loop.data.infra.rediskey.RedisKeyFormatter;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.redis.RedisDatasetDAO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

/**
 * 数据集Redis DAO实现类
 * 迁移对应关系: Go语言DatasetDAOImpl
 * - 功能: 实现数据集Redis缓存操作
 * - 方法实现: 各种数据集Redis缓存操作方法实现
 * <p>
 * Java实现说明:
 * - 对应Go的DatasetDAOImpl结构体
 * - 使用ICacheClient实现Redis操作
 * - 提供数据集项目数量缓存操作实现
 * <p>
 * 技术栈迁移:
 * - Go Redis客户端 -> Java ICacheClient
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
@Repository
public class RedisDatasetDAOImpl implements RedisDatasetDAO {
  private final RedisKeyFormatter redisKeyFormatter;
  private final ICacheClient cacheClient;

  public RedisDatasetDAOImpl(CacheFactory cacheFactory, RedisKeyFormatter redisKeyFormatter) {
    this.redisKeyFormatter = redisKeyFormatter;
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_WECHAT_AUTH);
  }

  /**
   * 设置项目数量
   * 迁移对应关系: Go语言DatasetDAOImpl.SetItemCount
   * - 功能: 设置数据集项目数量
   * - 参数: datasetID - 数据集ID, n - 数量
   * - 用途: 设置数据集项目数量到Redis
   */
  @Override
  public void setItemCount(Long datasetID, Long n) {
    if (datasetID == null || datasetID <= 0 || n == null) {
      throw new BssException("datasetID and n are required");
    }

    try {
      String key = redisKeyFormatter.formatDatasetItemCountKey(datasetID);
      cacheClient.opsForValue().set(key, String.valueOf(n));
    }
    catch (Exception e) {
      throw new BssException("SetItemCount failed, datasetID=" + datasetID + ", n=" + n, e);
    }
  }

  /**
   * 增加项目数量
   * 迁移对应关系: Go语言DatasetDAOImpl.IncrItemCount
   * - 功能: 增加数据集项目数量
   * - 参数: datasetID - 数据集ID, n - 增加数量
   * - 返回: 新数量
   * - 用途: 增加数据集项目数量
   */
  @Override
  public Long incrItemCount(Long datasetID, Long n) {
    if (datasetID == null || datasetID <= 0 || n == null) {
      throw new BssException("datasetID and n are required");
    }

    try {
      String key = redisKeyFormatter.formatDatasetItemCountKey(datasetID);
      return cacheClient.opsForValue().increment(key, n);
    }
    catch (Exception e) {
      throw new BssException("IncrItemCount failed, datasetID=" + datasetID + ", n=" + n, e);
    }
  }

  /**
   * 获取项目数量
   * 迁移对应关系: Go语言DatasetDAOImpl.GetItemCount
   * - 功能: 获取数据集项目数量
   * - 参数: datasetID - 数据集ID
   * - 返回: 项目数量
   * - 用途: 从Redis获取数据集项目数量
   */
  @Override
  public Long getItemCount(Long datasetID) {
    if (datasetID == null || datasetID <= 0) {
      throw new BssException("datasetID is required");
    }

    try {
      String key = redisKeyFormatter.formatDatasetItemCountKey(datasetID);
      String value = cacheClient.opsForValue().get(key);
      if (value == null || value.isEmpty()) {
        return 0L;
      }
      return Long.parseLong(value);
    }
    catch (NumberFormatException e) {
      throw new BssException("Invalid number format for datasetID=" + datasetID, e);
    }
    catch (Exception e) {
      throw new BssException("GetItemCount failed, datasetID=" + datasetID, e);
    }
  }

  /**
   * 批量获取项目数量
   * 迁移对应关系: Go语言DatasetDAOImpl.MGetItemCount
   * - 功能: 批量获取项目数量
   * - 参数: datasetIDs - 数据集ID列表
   * - 返回: 数据集ID到项目数量的映射
   * - 用途: 批量从Redis获取数据集项目数量
   */
  @Override
  public Map<Long, Long> mGetItemCount(List<Long> datasetIDs) {
    if (datasetIDs == null || datasetIDs.isEmpty()) {
      return new HashMap<>();
    }

    try {
      // 生成Redis键
      String[] keys = datasetIDs.stream()
        .map(redisKeyFormatter::formatDatasetItemCountKey)
        .toArray(String[]::new);

      // 批量获取值
      List<String> values = cacheClient.opsForValue().multiGet(Arrays.asList(keys));

      // 构建结果映射
      Map<Long, Long> result = new HashMap<>();
      for (int i = 0; i < datasetIDs.size(); i++) {
        Long datasetID = datasetIDs.get(i);
        String value = values.get(i);
        if (value == null) {
          result.put(datasetID, 0L);
        }
        else {
          try {
            result.put(datasetID, Long.parseLong(value));
          }
          catch (NumberFormatException e) {
            throw new BssException("Invalid number format for datasetID=" + datasetID + ", value=" + value, e);
          }
        }
      }

      return result;
    }
    catch (Exception e) {
      throw new BssException("MGetItemCount failed, datasetIDs=" + datasetIDs, e);
    }
  }
}
