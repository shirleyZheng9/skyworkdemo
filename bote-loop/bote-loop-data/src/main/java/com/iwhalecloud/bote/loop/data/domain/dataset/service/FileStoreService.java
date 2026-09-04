package com.iwhalecloud.bote.loop.data.domain.dataset.service;

import com.iwhalecloud.bote.loop.data.domain.entity.Provider;

/**
 * 文件存储服务接口
 * 迁移对应关系: Go语言service.IFileStoreService
 * - 功能: 提供文件存储相关的业务逻辑服务
 * - 方法定义: 各种文件存储操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的service.IFileStoreService接口
 * - 使用Java接口定义，包含文件存储服务方法
 * - 提供文件存储业务逻辑操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface FileStoreService {

  /**
   * 获取文件信息
   * 迁移对应关系: Go语言service.IFileStoreService.StatFile
   * - 功能: 获取文件信息
   * - 参数: provider - 提供商, path - 路径
   * - 返回: 文件信息
   * - 用途: 获取文件信息
   */
  Object statFile(Provider provider, String path);
}
