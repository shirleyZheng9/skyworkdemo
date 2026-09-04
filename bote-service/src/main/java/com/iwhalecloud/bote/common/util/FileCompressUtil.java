package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * 文件批量下载辅助工作类
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class FileCompressUtil {
  private FileCompressUtil() {
  }

  private static final Logger logger = LoggerFactory.getLogger(FileCompressUtil.class);

  /**
   * 导出文件
   */
  public static ResponseEntity<?> doExport(String compressDir, String fileName, ResultVO<InputStream> result) {
    try {
      if (!result.isSuccess()) {
        return ResponseEntity.ok().contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
          .body("压缩文件出错:" + result.getResultMsg());
      }
      String contentDisposition = ContentDisposition.attachment().filename(System.currentTimeMillis() + ".zip").build().toString();
      MediaType contentType = MediaType.parseMediaType("application/zip");
      InputStreamResource resource = new InputStreamResource(result.getResultObject());
      return ResponseEntity.ok().contentType(contentType).header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition).body(resource);
    }
    catch (Exception e) {
      logger.error("Failed to export sync data.", e);
      return ResponseEntity.badRequest().contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
        .body("导出文件异常, " + e.getMessage());
    }
    finally {
      if (StringUtils.isNotEmpty(compressDir)) {
        Path path = PathUtil.resolvePath(compressDir, fileName);
        FileUtils.deleteQuietly(path.toFile());
      }
    }
  }

  /**
   * 压缩文件
   */
  public static void compress(String compressDir, String fileName, List<String> fileIdList) {
    IFileStoreService fileStoreService = SpringUtil.getBean(IFileStoreService.class);
    Path path = PathUtil.resolvePath(compressDir, fileName);
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(path))) {
      for (String fileId : fileIdList) {
        if (StringUtils.isNumeric(fileId)) {
          FileInfoVO fileInfo = fileStoreService.getFileInfoById(Long.parseLong(fileId));
          if (fileInfo == null) {
            throw new BssException("文件不存在, fileId=" + fileId);
          }
          try (InputStream inputStream = fileStoreService.downloadFileStream(fileInfo)) {
            zos.putNextEntry(new ZipEntry(fileInfo.getFileName()));
            IOUtils.copy(inputStream, zos);
          }
          zos.closeEntry();
        }
      }
      zos.finish();
    }
    catch (IOException e) {
      logger.error("Error in compressing files, msg={}", e.getMessage(), e);
    }
  }

  /**
   * 读取zip文件流
   */
  public static ResultVO<InputStream> readFileAsStream(String compressDir, String fileName) {
    try {
      Path path = PathUtil.resolvePath(compressDir, fileName);
      InputStream inputStream = Files.newInputStream(path);
      return ResultVO.success(inputStream);
    }
    catch (IOException e) {
      logger.error("Failed to read zip to byte array. error={}", e.getMessage(), e);
      return ResultVO.fail("读取zip文件流异常");
    }
  }

  /**
   * 创建临时文件工作空间
   */
  public static String createWorkSpace() {
    try {
      Path base = Files.createTempDirectory("zip_temp_");
      String compressDir = StringUtils.stripEnd(base.toString(), "/");
      FileUtils.forceMkdir(new File(compressDir));
      return compressDir;
    }
    catch (IOException e) {
      logger.error("Failed to create workSpace. error={}", e.getMessage(), e);
      throw new BssException("创建临时工作空间异常", e);
    }
  }

}
