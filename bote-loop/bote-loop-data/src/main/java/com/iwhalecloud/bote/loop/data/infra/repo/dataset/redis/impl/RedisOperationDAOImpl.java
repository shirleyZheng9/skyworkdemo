package com.iwhalecloud.bote.loop.data.infra.repo.dataset.redis.impl;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetOpType;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetOperation;
import com.iwhalecloud.bote.loop.data.infra.rediskey.RedisKeyFormatter;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.redis.RedisOperationDAO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

/**
 * 数据集操作Redis DAO实现类
 * 迁移对应关系: Go语言OperationDAOImpl
 * - 功能: 实现数据集操作Redis缓存操作
 * - 方法实现: 各种数据集操作Redis缓存操作方法实现
 * <p>
 * Java实现说明:
 * - 对应Go的OperationDAOImpl结构体
 * - 使用ICacheClient实现Redis操作
 * - 提供数据集操作缓存操作实现
 * <p>
 * 技术栈迁移:
 * - Go Redis客户端 -> Java ICacheClient
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
@Repository
public class RedisOperationDAOImpl implements RedisOperationDAO {
  private final RedisKeyFormatter redisKeyFormatter;
  private final ICacheClient cacheClient;

  public RedisOperationDAOImpl(CacheFactory cacheFactory, RedisKeyFormatter redisKeyFormatter) {
    this.redisKeyFormatter = redisKeyFormatter;
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_WECHAT_AUTH);
  }

  /**
   * 添加数据集操作
   * 迁移对应关系: Go语言OperationDAOImpl.AddDatasetOperation
   * - 功能: 添加数据集操作
   * - 参数: datasetID - 数据集ID, op - 操作对象
   * - 用途: 添加数据集操作到Redis
   */
  @Override
  public void addDatasetOperation(Long datasetID, DatasetOperation op) {
    if (datasetID == null || datasetID <= 0 || op == null) {
      throw new BssException("datasetID and op are required");
    }

    try {
      String data = JsonUtil.toJsonString(op);
      String key = redisKeyFormatter.formatDatasetOperationKey(datasetID, op.getType().toString());
      cacheClient.opsForHash().put(key, op.getId(), data);
    }
    catch (Exception e) {
      throw new BssException("AddDatasetOperation failed, datasetID=" + datasetID + ", op=" + op, e);
    }
  }

  /**
   * 删除数据集操作
   * 迁移对应关系: Go语言OperationDAOImpl.DelDatasetOperation
   * - 功能: 删除数据集操作
   * - 参数: datasetID - 数据集ID, opType - 操作类型, id - 操作ID
   * - 用途: 从Redis删除数据集操作
   */
  @Override
  public void delDatasetOperation(Long datasetID, DatasetOpType opType, String id) {
    if (datasetID == null || datasetID <= 0 || opType == null || id == null) {
      throw new BssException("datasetID, opType and id are required");
    }

    try {
      String key = redisKeyFormatter.formatDatasetOperationKey(datasetID, opType.toString());
      cacheClient.opsForHash().delete(key, id);
    }
    catch (Exception e) {
      throw new BssException("DelDatasetOperation failed, datasetID=" + datasetID + ", opType=" + opType + ", id=" + id, e);
    }
  }

  /**
   * 批量获取数据集操作
   * 迁移对应关系: Go语言OperationDAOImpl.MGetDatasetOperations
   * - 功能: 批量获取数据集操作
   * - 参数: datasetID - 数据集ID, opTypes - 操作类型列表
   * - 返回: 操作类型到操作列表的映射
   * - 用途: 批量从Redis获取数据集操作
   */
  @Override
  public Map<DatasetOpType, List<DatasetOperation>> mGetDatasetOperations(Long datasetID, List<DatasetOpType> opTypes) {
    if (datasetID == null || datasetID <= 0 || CollectionUtils.isEmpty(opTypes)) {
      return new HashMap<>();
    }

    try {
      Map<DatasetOpType, List<DatasetOperation>> result = new HashMap<>();
      LocalDateTime now = LocalDateTime.now();

      for (DatasetOpType opType : opTypes) {
        List<DatasetOperation> operations = getOperationsForType(datasetID, opType, now);
        result.put(opType, operations);
      }

      return result;
    } catch (Exception e) {
      throw new BssException("MGetDatasetOperations failed, datasetID=" + datasetID + ", opTypes=" + opTypes, e);
    }
  }

  private List<DatasetOperation> getOperationsForType(Long datasetID, DatasetOpType opType, LocalDateTime now) {
    String key = redisKeyFormatter.formatDatasetOperationKey(datasetID, opType.toString());
    Map<Object, Object> hashValue = cacheClient.opsForHash().entries(key);

    if (hashValue.isEmpty()) {
      return new ArrayList<>();
    }

    List<DatasetOperation> operations = new ArrayList<>();
    List<String> expiredKeys = new ArrayList<>();

    processHashEntries(hashValue, opType, now, operations, expiredKeys);
    cleanupExpiredOperations(key, expiredKeys);

    return operations;
  }

  private void processHashEntries(Map<Object, Object> hashValue, DatasetOpType opType,
      LocalDateTime now, List<DatasetOperation> operations, List<String> expiredKeys) {
    for (Map.Entry<Object, Object> entry : hashValue.entrySet()) {
      String id = entry.getKey().toString();
      String val = entry.getValue().toString();

      try {
        DatasetOperation op = parseOperation(val, id, opType);
        if (isOperationExpired(op, now)) {
          expiredKeys.add(id);
        } else {
          operations.add(op);
        }
      } catch (Exception e) {
        throw new BssException("Unmarshal dataset operation failed, key=" + id, e);
      }
    }
  }

  private DatasetOperation parseOperation(String val, String id, DatasetOpType opType) {
    DatasetOperation op = JsonUtil.parseJsonRequired(val, DatasetOperation.class);
    op.setId(id);
    op.setType(opType);
    return op;
  }

  private boolean isOperationExpired(DatasetOperation op, LocalDateTime now) {
    return op.getTs().plus(op.getTtl()).isBefore(now);
  }

  private void cleanupExpiredOperations(String key, List<String> expiredKeys) {
    if (!expiredKeys.isEmpty()) {
      for (String expiredKey : expiredKeys) {
        cacheClient.opsForHash().delete(key, expiredKey);
      }
    }
  }
}
