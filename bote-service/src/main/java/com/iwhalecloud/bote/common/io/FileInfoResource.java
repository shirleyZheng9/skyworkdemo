package com.iwhalecloud.bote.common.io;

import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import org.springframework.core.io.AbstractResource;

import java.io.InputStream;
import org.springframework.util.Assert;

/**
 * 文件信息输入流资源
 *
 * @author qian.sisheng
 * @since 2025-07-21
 */
public class FileInfoResource extends AbstractResource {

  /** 文件信息 */
  private final FileInfoVO fileInfo;
  /** 文件存储服务 */
  private static final IFileStoreService fileStoreService = SpringUtil.getBean(IFileStoreService.class);

  public FileInfoResource(FileInfoVO fileInfo) {
    Assert.notNull(fileInfo, "文件信息不能为空");
    this.fileInfo = fileInfo;
  }

  /**
   * 获取文件名
   */
  @Override
  public String getFilename() {
    return fileInfo.getFileName();
  }

  /**
   * 获取描述信息
   */
  @Override
  public String getDescription() {
    return "FileInfoInputStreamResource [resource loaded through fileInfo]";
  }

  /**
   * 获取输入流
   */
  @Override
  public InputStream getInputStream() {
    return fileStoreService.downloadFileStream(fileInfo.getFileId());
  }
}
