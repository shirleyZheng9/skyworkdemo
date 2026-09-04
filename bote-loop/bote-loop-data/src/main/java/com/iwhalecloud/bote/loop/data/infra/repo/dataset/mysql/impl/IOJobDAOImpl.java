package com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.impl;

import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetIOJobEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.DeltaDatasetIOJob;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListIOJobsParams;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.IOJobDAO;
import com.iwhalecloud.bote.mapper.loop.data.dataset.IOJobMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * IO任务DAO实现类
 * 迁移对应关系: Go语言IOJobDAOImpl
 * - 功能: 实现IO任务数据访问
 * - 方法实现: 各种IO任务数据操作方法实现
 * <p>
 * Java实现说明:
 * - 对应Go的IOJobDAOImpl结构体
 * - 使用MyBatis实现数据库操作
 * - 提供IO任务数据CRUD操作实现
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
@Repository
@RequiredArgsConstructor
public class IOJobDAOImpl implements IOJobDAO {
  private final IOJobMapper ioJobMapper;

  /**
   * 创建IO任务
   * 迁移对应关系: Go语言IOJobDAOImpl.CreateIOJob
   * - 功能: 创建IO任务
   * - 参数: job - IO任务对象
   * - 用途: 创建新IO任务
   */
  @Override
  public void createIOJob(DatasetIOJobEntity job) {
    if (job == null) {
      throw new BssException("job is required");
    }

    try {
      ioJobMapper.createIOJob(job);
    }
    catch (Exception e) {
      throw new BssException("create dataset io job failed", e);
    }
  }

  /**
   * 获取IO任务
   * 迁移对应关系: Go语言IOJobDAOImpl.GetIOJob
   * - 功能: 获取IO任务
   * - 参数: id - 任务ID
   * - 返回: IO任务对象
   * - 用途: 获取单个IO任务
   */
  @Override
  public DatasetIOJobEntity getIOJob(Long id) {
    if (id == null || id <= 0) {
      throw new BssException("id is required");
    }

    try {
      return ioJobMapper.getIOJob(id);
    }
    catch (Exception e) {
      throw new BssException("get dataset io job failed", e);
    }
  }

  /**
   * 更新IO任务
   * 迁移对应关系: Go语言IOJobDAOImpl.UpdateIOJob
   * - 功能: 更新IO任务
   * - 参数: id - 任务ID, delta - 增量更新参数
   * - 用途: 增量更新IO任务
   */
  @Override
  public void updateIOJob(Long id, DeltaDatasetIOJob delta) {
    if (id == null || id <= 0) {
      throw new BssException("id is required");
    }
    if (delta == null) {
      throw new BssException("delta is required");
    }

    try {
      Map<String, Object> updates = delta.toUpdates();

      if (delta.getPreProcessed() != null) {
        // 带乐观锁的更新
        ioJobMapper.updateIOJobWithLock(id, delta.getPreProcessed(), updates);
      }
      else {
        // 普通更新
        ioJobMapper.updateIOJob(id, updates);
      }
    }
    catch (Exception e) {
      throw new BssException("update dataset io job failed", e);
    }
  }

  /**
   * 列表查询IO任务
   * 迁移对应关系: Go语言IOJobDAOImpl.ListIOJobs
   * - 功能: 列表查询IO任务
   * - 参数: params - 查询参数
   * - 返回: IO任务列表
   * - 用途: 查询IO任务列表
   */
  @Override
  public List<DatasetIOJobEntity> listIOJobs(ListIOJobsParams params) {
    if (params == null) {
      throw new BssException("params is required");
    }

    try {
      return ioJobMapper.listIOJobs(params);
    }
    catch (Exception e) {
      throw new BssException("list dataset io jobs failed", e);
    }
  }
}
