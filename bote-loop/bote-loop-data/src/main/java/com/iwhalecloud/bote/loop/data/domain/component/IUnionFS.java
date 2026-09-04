package com.iwhalecloud.bote.loop.data.domain.component;

import com.iwhalecloud.bote.loop.data.domain.entity.Provider;
import java.io.File;

/**
 * 联合文件系统接口
 * 迁移对应关系: Go语言vfs.IUnionFS
 * - 功能: 提供统一的文件系统访问接口
 * - 方法定义: 各种文件系统操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的vfs.IUnionFS接口
 * - 使用Java接口定义，包含文件系统操作方法
 * - 提供跨存储提供商的统一文件访问
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface IUnionFS {

  /**
   * 获取文件信息
   * 迁移对应关系: Go语言IUnionFS.StatFile
   * - 功能: 获取文件信息
   * - 参数: provider - 存储提供商, path - 文件路径
   * - 返回: 文件信息
   * - 用途: 获取文件状态
   */
  File getFileInfo(Provider provider, String path);

  /**
   * 获取只读文件系统
   * 迁移对应关系: Go语言IUnionFS.GetROFileSystem
   * - 功能: 获取只读文件系统
   * - 参数: provider - 存储提供商
   * - 返回: 只读文件系统
   * - 用途: 获取只读文件系统实例
   */
  IROFileSystem getROFileSystem();
}
