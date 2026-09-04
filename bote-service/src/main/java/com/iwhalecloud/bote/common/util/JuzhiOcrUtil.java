package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.service.external.JuzhiDictClient;
import com.iwhalecloud.bote.service.external.JuzhiOcrClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.lang.Nullable;

/**
 * 聚智 ocr 识别辅助工具
 *
 * @author tingyun.wang
 * @since 2025-06-25
 */
public final class JuzhiOcrUtil {

  private JuzhiOcrUtil() {
  }

  private static final IFileStoreService service = SpringUtil.getBean(IFileStoreService.class);
  private static final JuzhiOcrClient juzhiOcrClient = SpringUtil.getBeanOptional(JuzhiOcrClient.class);
  private static final JuzhiDictClient juzhiDictClient = SpringUtil.getBeanOptional(JuzhiDictClient.class);

  /**
   * 解析文档内容
   */
  public static Object parseFile(@Nullable Long fileId, @Nullable File file) {
    Assert.notNull(juzhiOcrClient, "未找到聚智ocr客户端实例：JuzhiOcrClient，请检查配置是否正确。");
    Assert.isTrue(fileId != null || file != null, "文件 ID 和文件不能同时同时为空");
    try {
      if (file != null) {
        try (FileInputStream fileStream = new FileInputStream(file)) {
          return juzhiOcrClient.parseFile(fileStream, file.getName());
        }
      }
      // 下载文件
      FileInfoVO fileInfo = service.getFileInfoById(fileId);
      Assert.notNull(fileInfo, "查询不到有效的文件信息");
      try (InputStream fileStream = service.downloadFileStream(fileId)) {
        return juzhiOcrClient.parseFile(fileStream, fileInfo.getFileName());
      }
    }
    catch (IOException e) {
      throw new BssException("解析文件内容异常: " + e.getMessage(), e);
    }
  }

  /**
   * 解析图片内容
   */
  public static Object parseImage(@Nullable Long fileId, @Nullable File file) {
    Assert.notNull(juzhiOcrClient, "未找到聚智ocr客户端实例：JuzhiOcrClient，请检查配置是否正确。");
    Assert.isTrue(fileId != null || file != null, "文件 ID 和文件不能同时同时为空");
    try {
      if (file != null) {
        try (FileInputStream fileStream = new FileInputStream(file)) {
          return juzhiOcrClient.parseImage(fileStream, file.getName());
        }
      }
      FileInfoVO fileInfo = service.getFileInfoById(fileId);
      Assert.notNull(fileInfo, "查询不到有效的文件信息");
      try (InputStream fileStream = service.downloadFileStream(fileId)) {
        return juzhiOcrClient.parseImage(fileStream, fileInfo.getFileName());
      }
    }
    catch (IOException e) {
      throw new BssException("解析图片内容异常: " + e.getMessage(), e);
    }
  }

  /**
   * 解析语音内容
   */
  public static String parseVoice(File file) {
    Assert.notNull(juzhiOcrClient, "未找到聚智ocr客户端实例：JuzhiOcrClient，请检查配置是否正确。");
    // 临时文件的删除由调用者处理
    try (FileInputStream fileStream = new FileInputStream(file)) {
      return juzhiOcrClient.parseVoice(fileStream, file.getName());
    }
    catch (IOException e) {
      throw new BssException("解析语音内容异常: " + e.getMessage(), e);
    }
  }

  /**
   * 解析文件内容：给 Dict 项目团队用的
   *
   * @param fileId 文件 ID
   * @return 提取的文字
   */
  public static Object parseFileForDict(Long fileId) {
    Assert.notNull(juzhiDictClient, "未找到聚智Dict客户端实例：JuzhiDictClient，请检查配置是否正确。");
    // 查询文件信息
    FileInfoVO fileInfo = service.getFileInfoById(fileId);
    Assert.notNull(fileInfo, "查询不到有效的文件信息");
    try (InputStream fileStream = service.downloadFileStream(fileId)) {
      // 解析文件内容
      return juzhiDictClient.parseFileContent(fileStream, fileInfo.getFileName());
    }
    catch (IOException e) {
      throw new BssException("解析文件内容异常: " + e.getMessage(), e);
    }
  }

}
