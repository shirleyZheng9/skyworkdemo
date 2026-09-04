package com.iwhalecloud.bote.loop.data.infra.repo.dataset.redis;

/**
 * 数据集版本Redis DAO接口
 * 迁移对应关系: Go语言VersionDAO
 * - 功能: 提供数据集版本Redis缓存操作接口
 * - 方法定义: 各种数据集版本Redis缓存操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的VersionDAO接口
 * - 使用Java接口定义，包含数据集版本Redis缓存操作方法
 * - 提供数据集版本项目数量缓存操作
 * <p>
 * 技术栈迁移:
 * - Go Redis客户端 -> Java StringRedisTemplate
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface RedisVersionDAO {

  /**
   * 获取版本项目数量
   * 迁移对应关系: Go语言VersionDAO.GetItemCountOfVersion
   * - 功能: 获取数据集版本项目数量
   * - 参数: versionID - 版本ID
   * - 返回: 项目数量（可能为null）
   * - 用途: 从Redis获取数据集版本项目数量
   */
  Long getItemCountOfVersion(Long versionID);

  /**
   * 设置版本项目数量
   * 迁移对应关系: Go语言VersionDAO.SetItemCountOfVersion
   * - 功能: 设置数据集版本项目数量
   * - 参数: versionID - 版本ID, n - 数量
   * - 用途: 设置数据集版本项目数量到Redis
   */
  void setItemCountOfVersion(Long versionID, Long n);
}
