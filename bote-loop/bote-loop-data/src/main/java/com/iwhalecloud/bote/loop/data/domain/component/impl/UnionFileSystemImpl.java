package com.iwhalecloud.bote.loop.data.domain.component.impl;

import com.iwhalecloud.bote.loop.data.domain.component.IROFileSystem;
import com.iwhalecloud.bote.loop.data.domain.component.IUnionFS;
import com.iwhalecloud.bote.loop.data.domain.entity.Provider;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.io.File;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 联合文件系统实现类
 * 迁移对应关系: Go语言vfs.IUnionFS
 * - 功能: 实现统一的文件系统访问接口
 * - 方法实现: 各种文件系统操作方法实现
 * <p>
 * Java实现说明:
 * - 对应Go的vfs.IUnionFS接口
 * - 使用现有的FileStoreService实现文件操作
 * - 提供跨存储提供商的统一文件访问
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
@Service
@RequiredArgsConstructor
public class UnionFileSystemImpl implements IUnionFS {
  private static final Logger logger = LoggerFactory.getLogger(UnionFileSystemImpl.class);

  private final IFileStoreService fileStoreService;

  @Override
  public File getFileInfo(Provider provider, String path) {
    try {
      // 从路径中提取文件ID（假设路径格式为 fileId 或包含fileId）
      Long fileId = extractFileIdFromPath(path);
      if (fileId == null) {
        throw new BssException("无法从路径中提取文件ID: " + path);
      }

      FileInfoVO fileInfo = fileStoreService.getFileInfoById(fileId);
      if (fileInfo == null) {
        return null;
      }

      // 创建File对象
      return new File(fileInfo.getFilePathInServer());
    }
    catch (Exception e) {
      logger.error("获取文件信息失败: provider={}, path={}", provider, path, e);
      throw new BssException("获取文件信息失败", e);
    }
  }

  @Override
  public IROFileSystem getROFileSystem() {
    return new ROFileSystemImpl(fileStoreService);
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
      // 假设路径格式为 "fileId" 或 "prefix/fileId" 或 "fileId.suffix"
      var fileNamePath = Paths.get(path).getFileName();
      if (fileNamePath == null) {
        return null;
      }
      String fileName = fileNamePath.toString();
      String fileIdStr = fileName.split("\\.")[0]; // 去掉扩展名
      return Long.parseLong(fileIdStr);
    }
    catch (Exception e) {
      logger.warn("无法从路径中提取文件ID: {}", path, e);
      return null;
    }
  }
}
