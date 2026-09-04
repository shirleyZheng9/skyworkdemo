package com.iwhalecloud.bote.loop.data.domain.component;

import java.io.File;
import java.util.List;

/**
 * 只读文件系统接口
 * 迁移对应关系: Go语言vfs.ROFileSystem
 * - 功能: 提供只读文件系统访问接口
 * - 方法定义: 各种只读文件系统操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的vfs.ROFileSystem接口
 * - 使用Java接口定义，包含只读文件系统操作方法
 * - 提供只读文件访问功能
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface IROFileSystem {

  /**
   * 获取文件信息
   * 迁移对应关系: Go语言ROFileSystem.Stat
   * - 功能: 获取文件信息
   * - 参数: name - 文件路径
   * - 返回: 文件信息
   * - 用途: 获取文件状态
   */
  File getFileInfo(String name);

  /**
   * 读取目录
   * 迁移对应关系: Go语言ROFileSystem.ReadDir
   * - 功能: 读取目录内容
   * - 参数: name - 目录路径
   * - 返回: 目录条目列表
   * - 用途: 列出目录内容
   */
  List<String> readDir(String name);

  /**
   * 读取文件
   * 迁移对应关系: Go语言ROFileSystem.ReadFile
   * - 功能: 读取文件内容
   * - 参数: name - 文件路径
   * - 返回: 文件读取器
   * - 用途: 读取文件内容
   */
  IFileReader readFile(String name);
}
