package com.iwhalecloud.bote.loop.data.domain.dataset.repo;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetVersion;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListDatasetVersionsParams;
import java.util.List;

/**
 * 版本仓库接口
 * 迁移对应关系: Go语言repo.IVersionRepo
 * - 功能: 提供数据集版本数据访问接口
 * - 方法定义: 各种数据集版本数据操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的repo.IVersionRepo接口
 * - 使用Java接口定义，包含数据集版本数据访问方法
 * - 提供数据集版本数据CRUD操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface IVersionRepo {

  /**
   * 获取版本项目数量
   * 迁移对应关系: Go语言repo.IVersionRepo.GetItemCountOfVersion
   * - 功能: 获取版本项目数量
   * - 参数: versionID - 版本ID
   * - 返回: 项目数量
   * - 用途: 获取版本项目数量
   */
  Long getItemCountOfVersion(Long versionID);

  /**
   * 设置版本项目数量
   * 迁移对应关系: Go语言repo.IVersionRepo.SetItemCountOfVersion
   * - 功能: 设置版本项目数量
   * - 参数: datasetID - 数据集ID, n - 数量
   * - 用途: 设置版本项目数量
   */
  void setItemCountOfVersion(Long datasetID, Long n);

  /**
   * 创建版本
   * 迁移对应关系: Go语言repo.IVersionRepo.CreateVersion
   * - 功能: 创建版本
   * - 参数: version - 数据集版本
   * - 用途: 创建版本
   */
  void createVersion(DatasetVersion version);

  /**
   * 获取版本
   * 迁移对应关系: Go语言repo.IVersionRepo.GetVersion
   * - 功能: 获取版本
   * - 参数: spaceID - 空间ID, versionID - 版本ID
   * - 返回: 数据集版本
   * - 用途: 获取单个版本
   */
  DatasetVersion getVersion(Long spaceID, Long versionID);

  /**
   * 批量获取版本
   * 迁移对应关系: Go语言repo.IVersionRepo.MGetVersions
   * - 功能: 批量获取版本
   * - 参数: spaceID - 空间ID, ids - 版本ID列表
   * - 返回: 版本列表
   * - 用途: 批量获取版本
   */
  List<DatasetVersion> mGetVersions(Long spaceID, List<Long> ids);

  /**
   * 列表版本
   * 迁移对应关系: Go语言repo.IVersionRepo.ListVersions
   * - 功能: 列表版本
   * - 参数: params - 列表参数
   * - 返回: 版本列表
   * - 用途: 列表版本
   */
  PageInfo<DatasetVersion> listVersions(ListDatasetVersionsParams params);

  /**
   * 统计版本数量
   * 迁移对应关系: Go语言repo.IVersionRepo.CountVersions
   * - 功能: 统计版本数量
   * - 参数: params - 列表参数
   * - 返回: 数量
   * - 用途: 统计版本数量
   */
  Long countVersions(ListDatasetVersionsParams params);

  /**
   * 更新版本
   * 迁移对应关系: Go语言repo.IVersionRepo.PatchVersion
   * - 功能: 更新版本
   * - 参数: patch - 更新数据, where - 条件
   * - 用途: 更新版本
   */
  void patchVersion(DatasetVersion patch, DatasetVersion where);
}
