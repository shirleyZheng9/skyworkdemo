package com.iwhalecloud.bote.loop.data.domain.dataset.service;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetVersion;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.DatasetWithSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.VersionWithDatasetResult;
import java.util.List;

/**
 * 版本服务接口
 * 迁移对应关系: Go语言service.IVersionService
 * - 功能: 提供版本相关的业务逻辑服务
 * - 方法定义: 各种版本操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的service.IVersionService接口
 * - 使用Java接口定义，包含版本服务方法
 * - 提供版本CRUD和业务逻辑操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface VersionService {

  /**
   * 创建版本
   * 迁移对应关系: Go语言service.IVersionService.CreateVersion
   * - 功能: 创建版本
   * - 参数: dataset - 数据集和模式, version - 版本
   * - 用途: 创建数据集版本
   */
  void createVersion(DatasetWithSchema dataset, DatasetVersion version);

  /**
   * 获取版本（带选项）
   * 迁移对应关系: Go语言service.IVersionService.GetVersionWithOpt
   * - 功能: 获取版本（带选项）
   * - 参数: spaceId - 空间ID, versionId - 版本ID, opt - 获取选项
   * - 返回: 版本, 数据集和模式
   * - 用途: 获取版本信息（带选项）
   */
  VersionWithDatasetResult getVersionWithOpt(Long spaceId, Long versionId, Boolean withDeleted);

  /**
   * 获取或设置版本项目数量
   * 迁移对应关系: Go语言service.IVersionService.GetOrSetItemCountOfVersion
   * - 功能: 获取或设置版本项目数量
   * - 参数: version - 版本
   * - 返回: 项目数量
   * - 用途: 管理版本项目数量
   */
  Long getOrSetItemCountOfVersion(DatasetVersion version);

  /**
   * 批量获取版本化数据集（带选项）
   * 迁移对应关系: Go语言service.IVersionService.BatchGetVersionedDatasetsWithOpt
   * - 功能: 批量获取版本化数据集（带选项）
   * - 参数: spaceId - 空间ID, versionIds - 版本ID列表, opt - 获取选项
   * - 返回: 版本化数据集和模式列表
   * - 用途: 批量获取版本化数据集信息
   */
  List<VersionWithDatasetResult> batchGetVersionedDatasetsWithOpt(Long spaceId, List<Long> versionIds, Boolean withDeleted);
}
