package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * ZIP 压缩/解压缩工具类
 *
 * @author chen.linfa
 * @since 2024-09-12
 */
public abstract class ZipUtil {

  /**
   * 压缩类路径资源文件到 HTTP 响应
   *
   * @param pattern 资源路径模式
   * @param filename 下载文件名
   * @param response HTTP 响应
   */
  public static void zipClassPathResourcesToResponse(String pattern, String filename, HttpServletResponse response) throws IOException {
    PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = resourcePatternResolver.getResources(pattern);
    if (resources.length == 0) {
      throw BaseErrorConstant.GET_RESOURCES_ERROR.toException(pattern);
    }
    String contentDisposition = ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString();
    response.setContentType("application/zip");
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
    try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
      // 使用固定的缓冲区，避免读取每个文件时重复申请
      byte[] buffer = new byte[8192];
      for (Resource resource : resources) {
        String resourceFilename = resource.getFilename();
        if (resourceFilename == null) {
          continue;
        }
        zos.putNextEntry(new ZipEntry(resourceFilename));
        try (InputStream inputStream = resource.getInputStream()) {
          IOUtils.copyLarge(inputStream, zos, buffer);
        }
        zos.closeEntry();
      }
      zos.flush();
    }
  }

  /**
   * 将目录打包压缩成 zip 文件
   *
   * @param source 源目录
   * @param zos 压缩文件输出流
   */
  public static void zipDirectory(Path source, ZipOutputStream zos) {
    zipDirectory(source, null, false, zos);
  }

  /**
   * 将目录打包压缩成 zip 文件
   *
   * @param source 源目录
   * @param filter 文件过滤器，用于排除不需要打包的文件
   * @param zos 压缩文件输出流
   */
  public static void zipDirectory(Path source, @Nullable BiPredicate<File, String> filter, ZipOutputStream zos) {
    zipDirectory(source, filter, false, zos);
  }

  /**
   * 将目录打包压缩成 zip 文件
   *
   * @param source 源目录
   * @param filter 文件过滤器，用于排除不需要打包的文件
   * @param includeSource 是否在压缩包中包含源目录（将源目录作为压缩包中的第一级目录）
   * @param zos 压缩文件输出流
   */
  public static void zipDirectory(Path source, @Nullable BiPredicate<File, String> filter, boolean includeSource, ZipOutputStream zos) {
    Assert.notNull(source, "源目录不能为空");
    Assert.notNull(zos, "压缩文件输出流不能为空");
    Assert.isTrue(source.toFile().isDirectory(), "源目录必须是目录");

    // 使用固定的缓冲区，避免读取每个文件时重复申请
    byte[] buffer = new byte[8192];
    try {
      // 是否包含源目录（将源目录作为压缩包中的第一级目录）
      if (includeSource) {
        // 递归处理子文件和子目录
        zipFiles("", new File[]{source.toFile()}, filter, zos, buffer);
      }
      else {
        // 递归处理子文件和子目录
        zipFiles("", source.toFile().listFiles(), filter, zos, buffer);
      }
      zos.flush();
    }
    catch (IOException e) {
      throw BaseErrorConstant.ZIP_FILES_ERROR.toException(e);
    }
  }

  /**
   * 将指定的文件列表写入到压缩文件中
   */
  private static void zipFiles(String parentPath, @Nullable File[] files, @Nullable BiPredicate<File, String> filter, ZipOutputStream zos,
                               byte[] buffer) throws IOException {
    if (files == null) {
      return;
    }
    for (File file : files) {
      Path path = file.toPath();
      String relativePath = parentPath.isEmpty() ? file.getName() : (parentPath + "/" + file.getName());
      if (filter != null && !filter.test(file, relativePath)) {
        continue;
      }
      if (file.isDirectory()) {
        zos.putNextEntry(new ZipEntry(relativePath + "/"));
        zipFiles(relativePath, file.listFiles(), filter, zos, buffer);
      }
      else {
        zos.putNextEntry(new ZipEntry(relativePath));
        try (InputStream inputStream = Files.newInputStream(path)) {
          IOUtils.copyLarge(inputStream, zos, buffer);
        }
      }
    }
  }

  /**
   * 解压缩 zip 文件到指定目录
   *
   * @param zipFile ZIP 文件
   * @param targetDir 目标目录
   * @return 压缩包中的顶级文件列表。用于方便调用方获取解压得到的文件
   * @see <a href="https://www.baeldung.com/java-compress-and-uncompress">Zipping and Unzipping in Java</a>
   */
  public static List<File> unzip(File zipFile, Path targetDir) {
    Assert.notNull(zipFile, "ZIP 文件不能为空");
    Assert.isTrue(zipFile.exists(), () -> "ZIP 文件不存在: " + zipFile.getAbsolutePath());
    try (InputStream inputStream = Files.newInputStream(zipFile.toPath())) {
      return unzip(inputStream, targetDir);
    }
    catch (IOException e) {
      throw BaseErrorConstant.UNZIP_FILES_ERROR.toException(e);
    }
  }

  /**
   * 解压缩 zip 输入流到指定目录
   *
   * @param inputStream 输入流
   * @param targetDir 目标目录
   * @return 压缩包中的顶级文件列表。用于方便调用方获取解压得到的文件
   */
  public static List<File> unzip(InputStream inputStream, Path targetDir) {
    Assert.notNull(inputStream, "ZIP 文件不能为空");
    File targetDirFile = targetDir.toFile();
    try (ZipArchiveInputStream zis = new ZipArchiveInputStream(inputStream, "GB18030", true, true)) {
      return doUnzip(targetDirFile, zis);
    }
    catch (Exception e) {
      throw BaseErrorConstant.UNZIP_FILES_ERROR.toException(e);
    }
  }

  /**
   * 执行解压缩
   */
  private static List<File> doUnzip(File targetDirFile, ZipArchiveInputStream zis) throws IOException {
    // 压缩包中的顶级文件列表
    List<File> topLevelFiles = new ArrayList<>();
    // 使用固定的缓冲区，避免读取每个文件时重复申请
    byte[] buffer = new byte[8192];
    // 规范的目标路径
    String canonicalTargetPath = targetDirFile.getCanonicalPath() + File.separator;
    Path targetDirFilePath = targetDirFile.toPath();
    int basePathSegmentCount = targetDirFilePath.getNameCount();
    File destFile;
    Path destPath;
    for (ZipArchiveEntry zipEntry = zis.getNextZipEntry(); zipEntry != null; zipEntry = zis.getNextZipEntry()) {
      String entryName = zipEntry.getName();
      // 忽略 __MACOSX 目录及其内容
      if (entryName.contains("__MACOSX")) {
        continue;
      }
      destFile = new File(targetDirFile, entryName);
      // 安全检查，不允许解压到目标目录以外的目录
      if (!destFile.getCanonicalPath().startsWith(canonicalTargetPath)) {
        throw BaseErrorConstant.DO_UNZIP_FILES_ERROR.toException(entryName);
      }

      destPath = destFile.toPath();
      if (zipEntry.isDirectory()) {
        FileUtils.forceMkdir(destFile);
      }
      else {
        FileUtils.forceMkdir(destFile.getParentFile());
        try (OutputStream fos = Files.newOutputStream(destPath)) {
          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }
        }
      }
      // 有些压缩包中有单独的目录 entry, 有些没有，保险起见需要对每个文件计算顶级文件
      File topFile = targetDirFilePath.resolve(destPath.subpath(basePathSegmentCount, basePathSegmentCount + 1)).toFile();
      if (!topLevelFiles.contains(topFile)) {
        topLevelFiles.add(topFile);
      }
    }
    return topLevelFiles;
  }

}
