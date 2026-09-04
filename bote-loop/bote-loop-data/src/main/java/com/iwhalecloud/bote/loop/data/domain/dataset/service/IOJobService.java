package com.iwhalecloud.bote.loop.data.domain.dataset.service;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.IOJob;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JobRunMessage;

/**
 * IO任务服务接口
 * 迁移对应关系: Go语言service.IIOJobService
 * - 功能: 提供IO任务相关的业务逻辑服务
 * - 方法定义: 各种IO任务操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的service.IIOJobService接口
 * - 使用Java接口定义，包含IO任务服务方法
 * - 提供IO任务业务逻辑操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface IOJobService {

  /**
   * 获取IO任务
   * 迁移对应关系: Go语言service.IIOJobService.GetIOJob
   * - 功能: 获取IO任务
   * - 参数: jobId - 任务ID
   * - 返回: IO任务
   * - 用途: 获取IO任务信息
   */
  IOJob getIOJob(Long jobId);

  /**
   * 创建IO任务
   * 迁移对应关系: Go语言service.IIOJobService.CreateIOJob
   * - 功能: 创建 job, 并发送执行 job 的消息
   * - 参数: job - IO任务
   * - 用途: 创建IO任务
   */
  void createIOJob(IOJob job);

  /**
   * 运行IO任务
   * 迁移对应关系: Go语言service.IIOJobService.RunIOJob
   * - 功能: 运行IO任务
   * - 参数: msg - 任务运行消息
   * - 用途: 运行IO任务
   */
  void runIOJob(JobRunMessage msg);
}
