package com.iwhalecloud.bote.loop.data.domain.component.impl;

import com.iwhalecloud.bote.loop.data.domain.component.IFileReader;
import com.iwhalecloud.bote.loop.data.domain.component.IROFileSystem;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FileFormat;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 只读文件系统实现类
 * 迁移对应关系: Go语言vfs.ROFileSystem
 * - 功能: 实现只读文件系统访问接口
 * - 方法实现: 各种只读文件系统操作方法实现
 * <p>
 * Java实现说明:
 * - 对应Go的vfs.ROFileSystem接口
 * - 使用现有的FileStoreService实现文件操作
 * - 提供只读文件访问功能
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public class ROFileSystemImpl implements IROFileSystem {

  private static final Logger logger = LoggerFactory.getLogger(ROFileSystemImpl.class);

  private final IFileStoreService fileStoreService;

  public ROFileSystemImpl(IFileStoreService fileStoreService) {
    this.fileStoreService = fileStoreService;
  }

  @Override
  public File getFileInfo(String name) {
    try {
      Long fileId = extractFileIdFromPath(name);
      if (fileId == null) {
        throw new BssException("无法从路径中提取文件ID: " + name);
      }

      FileInfoVO fileInfo = fileStoreService.getFileInfoById(fileId);
      if (fileInfo == null) {
        return null;
      }

      return new File(fileInfo.getFilePathInServer());
    }
    catch (Exception e) {
      logger.error("获取文件信息失败: name={}", name, e);
      throw new BssException("获取文件信息失败", e);
    }
  }

  @Override
  public List<String> readDir(String name) {
    // 对于文件存储服务，通常不支持目录遍历
    // 这里可以根据实际需求实现
    logger.warn("目录遍历功能暂不支持: {}", name);
    return new ArrayList<>();
  }

  @Override
  public IFileReader readFile(String name) {
    try {
      Long fileId = extractFileIdFromPath(name);
      if (fileId == null) {
        throw new BssException("无法从路径中提取文件ID: " + name);
      }

      FileInfoVO fileInfo = fileStoreService.getFileInfoById(fileId);
      if (fileInfo == null) {
        throw new BssException("文件不存在: " + name);
      }

      // 获取文件流
      var inputStream = fileStoreService.downloadFileStream(fileId);

      // 根据文件扩展名确定格式
      String fileName = fileInfo.getFileName();
      String extension = getFileExtension(fileName);
      FileFormat format = determineFileFormat(extension);

      return new FileReaderImpl(name, inputStream, format);
    }
    catch (Exception e) {
      logger.error("读取文件失败: name={}", name, e);
      throw new BssException("读取文件失败", e);
    }
  }

  /**
   * 从路径中提取文件ID
   * 迁移对应关系: 自定义方法
   * - 功能: 从文件路径中提取文件ID
   * - 参数: path - 文件路径
   * - 返回: 文件ID
   * - 用途: 路径解析
   */
  private Long extractFileIdFromPath(String path) {
    try {
      if (path == null) {
        return null;
      }
      var fileNamePath = Paths.get(path).getFileName();
      if (fileNamePath == null) {
        return null;
      }
      String fileName = fileNamePath.toString();
      String fileIdStr = fileName.split("\\.")[0];
      return Long.parseLong(fileIdStr);
    }
    catch (Exception e) {
      logger.warn("无法从路径中提取文件ID: {}", path, e);
      return null;
    }
  }

  /**
   * 获取文件扩展名
   * 迁移对应关系: 自定义方法
   * - 功能: 获取文件扩展名
   * - 参数: fileName - 文件名
   * - 返回: 扩展名
   * - 用途: 文件格式识别
   */
  private String getFileExtension(String fileName) {
    if (fileName == null || !fileName.contains(".")) {
      return "";
    }
    return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
  }

  /**
   * 确定文件格式
   * 迁移对应关系: 自定义方法
   * - 功能: 根据扩展名确定文件格式
   * - 参数: extension - 文件扩展名
   * - 返回: 文件格式
   * - 用途: 文件格式识别
   */
  private FileFormat determineFileFormat(String extension) {
    return switch (extension.toLowerCase()) {
      case "csv" -> FileFormat.CSV;
      case "jsonl", "json" -> FileFormat.JSONL;
      case "parquet" -> FileFormat.PARQUET;
      default -> FileFormat.CSV; // 默认格式
    };
  }
}
