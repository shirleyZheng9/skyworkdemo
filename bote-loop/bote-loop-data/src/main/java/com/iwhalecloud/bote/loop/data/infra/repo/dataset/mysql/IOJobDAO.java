package com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql;

import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetIOJobEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.DeltaDatasetIOJob;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListIOJobsParams;
import java.util.List;

/**
 * IO任务DAO接口
 * 迁移对应关系: Go语言IIOJobDAO
 * - 功能: 提供IO任务数据访问接口
 * - 方法定义: 各种IO任务数据操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的IIOJobDAO接口
 * - 使用Java接口定义，包含IO任务数据访问方法
 * - 提供IO任务数据CRUD操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface IOJobDAO {

  /**
   * 创建IO任务
   * 迁移对应关系: Go语言IIOJobDAO.CreateIOJob
   * - 功能: 创建IO任务
   * - 参数: job - IO任务对象
   * - 用途: 创建新IO任务
   */
  void createIOJob(DatasetIOJobEntity job);

  /**
   * 获取IO任务
   * 迁移对应关系: Go语言IIOJobDAO.GetIOJob
   * - 功能: 获取IO任务
   * - 参数: id - 任务ID
   * - 返回: IO任务对象
   * - 用途: 获取单个IO任务
   */
  DatasetIOJobEntity getIOJob(Long id);

  /**
   * 更新IO任务
   * 迁移对应关系: Go语言IIOJobDAO.UpdateIOJob
   * - 功能: 更新IO任务
   * - 参数: id - 任务ID, delta - 增量更新参数
   * - 用途: 增量更新IO任务
   */
  void updateIOJob(Long id, DeltaDatasetIOJob delta);

  /**
   * 列表查询IO任务
   * 迁移对应关系: Go语言IIOJobDAO.ListIOJobs
   * - 功能: 列表查询IO任务
   * - 参数: params - 查询参数
   * - 返回: IO任务列表
   * - 用途: 查询IO任务列表
   */
  List<DatasetIOJobEntity> listIOJobs(ListIOJobsParams params);
}
