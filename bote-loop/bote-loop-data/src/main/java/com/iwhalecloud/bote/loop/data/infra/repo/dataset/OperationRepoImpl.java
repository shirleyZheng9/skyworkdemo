package com.iwhalecloud.bote.loop.data.infra.repo.dataset;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetOpType;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetOperation;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IOperationRepo;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.redis.RedisOperationDAO;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 数据集操作Repository实现类
 */
@Component("operationRepoImpl")
@RequiredArgsConstructor
public class OperationRepoImpl implements IOperationRepo {
  private static final Logger logger = LoggerFactory.getLogger(OperationRepoImpl.class);

  private final RedisOperationDAO redisOperationDAO;
  private final IIDGenerator idGenerator;

  /**
   * 添加数据集操作
   */
  @Override
  public void addDatasetOperation(Long datasetID, DatasetOperation op) {
    if (datasetID == null || datasetID <= 0) {
      throw new BssException("datasetID is required");
    }
    if (op == null) {
      throw new BssException("operation is null");
    }
    // 生成ID
    Long id = idGenerator.genId();
    if (id == null || id == 0) {
      logger.warn("gen id for item op failed, dataset_id={}", datasetID);
      // 使用UUID作为备用ID
      op.setId(java.util.UUID.randomUUID().toString());
    }
    else {
      op.setId(String.valueOf(id));
    }
    // 添加操作
    redisOperationDAO.addDatasetOperation(datasetID, op);
  }

  /**
   * 删除数据集操作
   */
  @Override
  public void delDatasetOperation(Long datasetID, DatasetOpType opType, String id) {
    if (datasetID == null || datasetID <= 0) {
      throw new BssException("datasetID is required");
    }
    if (opType == null) {
      throw new BssException("opType is required");
    }
    if (!StringUtils.hasText(id)) {
      throw new BssException("id is required");
    }
    redisOperationDAO.delDatasetOperation(datasetID, opType, id);
  }

  /**
   * 批量获取数据集操作
   */
  @Override
  public Map<DatasetOpType, List<DatasetOperation>> mGetDatasetOperations(Long datasetID, List<DatasetOpType> opTypes) {
    if (datasetID == null || datasetID <= 0) {
      throw new BssException("datasetID is required");
    }
    return redisOperationDAO.mGetDatasetOperations(datasetID, opTypes);
  }
}
