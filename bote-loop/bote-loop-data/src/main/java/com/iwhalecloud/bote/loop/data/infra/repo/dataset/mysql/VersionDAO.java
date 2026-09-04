package com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetVersionEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListDatasetVersionsParams;
import java.util.List;

/**
 * 数据集版本DAO接口
 * 迁移对应关系: Go语言IVersionDAO
 * - 功能: 提供数据集版本数据访问接口
 * - 方法定义: 各种数据集版本数据操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的IVersionDAO接口
 * - 使用Java接口定义，包含数据集版本数据访问方法
 * - 提供数据集版本数据CRUD操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface VersionDAO {

  /**
   * 创建版本
   * 迁移对应关系: Go语言IVersionDAO.CreateVersion
   * - 功能: 创建数据集版本
   * - 参数: version - 版本对象
   * - 用途: 创建新版本
   */
  void createVersion(DatasetVersionEntity version);

  /**
   * 获取版本
   * 迁移对应关系: Go语言IVersionDAO.GetVersion
   * - 功能: 获取数据集版本
   * - 参数: spaceId - 空间ID, versionId - 版本ID
   * - 返回: 版本对象
   * - 用途: 获取单个版本
   */
  DatasetVersionEntity getVersion(Long spaceId, Long versionId);

  /**
   * 批量获取版本
   * 迁移对应关系: Go语言IVersionDAO.MGetVersions
   * - 功能: 批量获取数据集版本
   * - 参数: spaceId - 空间ID, ids - 版本ID列表
   * - 返回: 版本列表
   * - 用途: 批量获取版本
   */
  List<DatasetVersionEntity> mGetVersions(Long spaceId, List<Long> ids);

  /**
   * 列表查询版本
   * 迁移对应关系: Go语言IVersionDAO.ListVersions
   * - 功能: 列表查询数据集版本
   * - 参数: params - 查询参数
   * - 返回: 版本列表
   * - 用途: 分页查询版本
   */
  PageInfo<DatasetVersionEntity> listVersions(ListDatasetVersionsParams params);

  /**
   * 统计版本数量
   * 迁移对应关系: Go语言IVersionDAO.CountVersions
   * - 功能: 统计数据集版本数量
   * - 参数: params - 查询参数
   * - 返回: 版本数量
   * - 用途: 统计版本数量
   */
  Long countVersions(ListDatasetVersionsParams params);

  /**
   * 更新版本
   * 迁移对应关系: Go语言IVersionDAO.PatchVersion
   * - 功能: 更新数据集版本
   * - 参数: patch - 更新数据, where - 条件
   * - 用途: 更新版本
   */
  void patchVersion(DatasetVersionEntity patch, DatasetVersionEntity where);
}
