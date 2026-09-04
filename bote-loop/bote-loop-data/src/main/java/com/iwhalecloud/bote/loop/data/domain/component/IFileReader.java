package com.iwhalecloud.bote.loop.data.domain.component;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

/**
 * 文件读取器接口
 * 迁移对应关系: Go语言vfs.Reader
 * - 功能: 提供文件读取接口
 * - 方法定义: 各种文件读取操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的vfs.Reader接口
 * - 使用Java接口定义，包含文件读取操作方法
 * - 提供文件内容读取功能
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface IFileReader extends Closeable {

  /**
   * 获取文件名
   * 迁移对应关系: Go语言FileReader.GetName
   * - 功能: 获取文件名
   * - 返回: 文件名
   * - 用途: 获取当前文件名
   */
  String getName();

  /**
   * 获取游标位置
   * 迁移对应关系: Go语言FileReader.GetCursor
   * - 功能: 获取当前游标位置
   * - 返回: 游标位置
   * - 用途: 获取当前读取位置
   */
  long getCursor();

  /**
   * 设置游标位置
   * 迁移对应关系: Go语言FileReader.SetCursor
   * - 功能: 设置游标位置
   * - 参数: cursor - 游标位置
   * - 用途: 设置读取位置
   */
  void setCursor(long cursor);

  /**
   * 跳转到指定偏移
   * 迁移对应关系: Go语言FileReader.SeekToOffset
   * - 功能: 跳转到指定偏移位置
   * - 参数: offset - 偏移量
   * - 用途: 定位到指定位置
   */
  void seekToOffset(long offset) throws IOException;

  /**
   * 读取下一行数据
   * 迁移对应关系: Go语言FileReader.Next
   * - 功能: 读取下一行数据
   * - 返回: 键值对数据
   * - 用途: 读取下一行内容
   */
  Map<String, Object> next() throws IOException;
}
