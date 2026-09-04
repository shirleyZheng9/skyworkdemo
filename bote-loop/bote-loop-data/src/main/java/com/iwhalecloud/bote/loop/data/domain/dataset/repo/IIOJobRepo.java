package com.iwhalecloud.bote.loop.data.domain.dataset.repo;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.IOJob;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.DeltaDatasetIOJob;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListIOJobsParams;
import java.util.List;

/**
 * IO任务仓库接口
 * 迁移对应关系: Go语言repo.IIOJobRepo
 * - 功能: 提供IO任务数据访问接口
 * - 方法定义: 各种IO任务数据操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的repo.IIOJobRepo接口
 * - 使用Java接口定义，包含IO任务数据访问方法
 * - 提供IO任务数据CRUD操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface IIOJobRepo {

  /**
   * 创建IO任务
   * 迁移对应关系: Go语言repo.IIOJobRepo.CreateIOJob
   * - 功能: 创建IO任务
   * - 参数: job - IO任务
   * - 用途: 创建IO任务
   */
  void createIOJob(IOJob job);

  /**
   * 获取IO任务
   * 迁移对应关系: Go语言repo.IIOJobRepo.GetIOJob
   * - 功能: 获取IO任务
   * - 参数: id - IO任务ID
   * - 返回: IO任务
   * - 用途: 获取单个IO任务
   */
  IOJob getIOJob(Long id);

  /**
   * 更新IO任务
   * 迁移对应关系: Go语言repo.IIOJobRepo.UpdateIOJob
   * - 功能: 更新IO任务
   * - 参数: id - IO任务ID, delta - 增量更新数据
   * - 用途: 更新IO任务
   */
  void updateIOJob(Long id, DeltaDatasetIOJob delta);

  /**
   * 列表IO任务
   * 迁移对应关系: Go语言repo.IIOJobRepo.ListIOJobs
   * - 功能: 列表IO任务
   * - 参数: params - 列表参数
   * - 返回: IO任务列表
   * - 用途: 列表IO任务
   */
  List<IOJob> listIOJobs(ListIOJobsParams params);
}
