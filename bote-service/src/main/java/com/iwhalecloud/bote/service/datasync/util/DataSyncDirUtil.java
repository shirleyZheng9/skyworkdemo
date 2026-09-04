package com.iwhalecloud.bote.service.datasync.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.util.PathUtil;
import com.iwhalecloud.bote.common.util.ZipUtil;
import com.iwhalecloud.bote.dto.datasync.DataSyncTableDefinition;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据同步辅助工具类
 *
 * @author chen.linfa
 * @since 2022-09-05
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class DataSyncDirUtil {
  private DataSyncDirUtil() {
  }

  private static final Logger logger = LoggerFactory.getLogger(DataSyncDirUtil.class);

  public static final String FILE_NAME = "data-sync-%s.zip";

  public static final String FILE_PATH = "data-sync-%s";

  /** 脚本存储父目录 */
  private static final String SCRIPT_PATH = "script";

  /**
   * 新建工作空间
   * <p>采用系统临时目录，优点：默认拥有读写权限；临时目录的 IO 操作性能好</p>
   */
  public static void createWorkspec(DataSyncParams params) {
    try {
      String temp = Files.createTempDirectory("data-sync-").toString();
      Path baseDir = PathUtil.resolvePath(temp, params.getTenantId().toString());

      // 压缩数据包，使用到的工作目录
      Path compressDir = PathUtil.resolvePath(baseDir.toString(), "compressDir");
      // 解压数据包，使用到的工作目录，收集、保存数据，会在此目录存储、采集数据
      Path decompressDir = PathUtil.resolvePath(baseDir.toString(), "decompressDir");

      FileUtils.forceMkdir(compressDir.toFile());
      FileUtils.forceMkdir(decompressDir.toFile());

      params.setCompressDir(compressDir.toString());
      params.setDecompressDir(decompressDir.toString());
    }
    catch (Exception e) {
      logger.error("Failed to create workspec", e);
      throw new BssException("创建数据同步工作空间异常", e);
    }
  }

  /**
   * 清空工作空间
   */
  public static void clearWorkspace(DataSyncParams params) {
    try {
      // 清空之前，先记录操作日志
      Path compressDir = PathUtil.resolvePath(params.getCompressDir());
      File parentDir = compressDir.toFile().getParentFile();
      FileUtils.deleteQuietly(parentDir);
    }
    catch (Exception e) {
      logger.error("Failed to clear workspec", e);
      throw new BssException("清空数据同步工作空间异常", e);
    }
  }

  /**
   * 将 decompressDir 目录下是数据包，进行压缩，压缩后的 zip 存储到 compressDir 目录下
   */
  public static void compress(DataSyncParams params) {
    String fileName = getExportFileName(params);
    Path path = PathUtil.resolvePath(params.getCompressDir(), fileName);
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(path))) {
      ZipUtil.zipDirectory(PathUtil.resolvePath(params.getDecompressDir()), zos);
    }
    catch (IOException e) {
      logger.error("Failed to compress data. error={}", e.getMessage(), e);
    }
  }

  /**
   * 将 compressDir 目录下是数据包，进行解压，解压后的内容存储到 decompressDir 目录下
   */
  public static void decompress(DataSyncParams params) {
    String fileName = String.format(FILE_NAME, params.getTenantId());
    Path sourcePath = PathUtil.resolvePath(params.getCompressDir(), fileName);
    Path targetPath = PathUtil.resolvePath(params.getDecompressDir());
    // 出于调整应用压缩包内容需要，会在 windows 创建压缩包进行应用导入，但 JDK 原生 zip 流会因为文件或文件夹命名所用字符集编码不匹配，抛出异常 MALFORMED
    try (ZipArchiveInputStream zis = new ZipArchiveInputStream(Files.newInputStream(sourcePath))) {
      byte[] buffer = new byte[8 * 1024];
      File destFile;
      for (ArchiveEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
        destFile = new File(targetPath.toFile(), PathUtil.normalizePath(entry.getName()));
        if (!entry.isDirectory()) {
          FileUtils.forceMkdir(destFile.getParentFile());
          try (OutputStream out = Files.newOutputStream(destFile.toPath())) {
            int len;
            while ((len = zis.read(buffer)) > 0) {
              out.write(buffer, 0, len);
            }
          }
        }
        else {
          FileUtils.forceMkdir(destFile);
        }
      }
    }
    catch (IOException e) {
      logger.error("Failed to decompress data. error={}", e.getMessage(), e);
    }
  }

  /**
   * 上传数据包
   * <p>1. 数据包上传到 compressDir 工作目录下</p>
   * <p>2. 解压 compressDir 目录下 zip，内容存储到 decompressDir 目录下</p>
   * <p>3. 遍历 decompressDir 子目录，找到数据包中涉及的 Id 列表/>
   * <p>4. 给每个 Id 创建新的工作空间，同时将 decompressDir 属于该应用的数据文件，迁移到指定的工作空间</p>
   */
  public static synchronized DataSyncParams uploadZip(File tempFile) {
    DataSyncParams params = new DataSyncParams();
    params.setTenantId(-1L);
    createWorkspec(params);
    String fileName = String.format(FILE_NAME, params.getTenantId());
    Path path = PathUtil.resolvePath(params.getCompressDir(), fileName);
    try {
      // 上传原始数据包
      FileUtils.write(path.toFile(), null, StandardCharsets.UTF_8);
      try (OutputStream out = Files.newOutputStream(path)) {
        IOUtils.copy(Files.newInputStream(tempFile.toPath()), out);
      }
      // 解压数据包
      decompress(params);
      List<String> dirs = getDirFromPath(params.getDecompressDir());
      if (CollectionUtils.isEmpty(dirs)) {
        return null;
      }
      // 解析 decompressDir 子目录
      String dir = dirs.get(0);
      // 从目录中提取出 id
      String id = dir.replace("data-sync-", "");
      if (NumberUtils.isCreatable(id)) {
        DataSyncParams target = new DataSyncParams();
        target.setTenantId(Long.valueOf(id));
        dirs = getDirFromPath(params.getDecompressDir() + File.separatorChar + dir);
        // 初始化新的应用工作空间
        createWorkspec(target);
        Path sourceDir = PathUtil.resolvePath(params.getDecompressDir() + File.separatorChar + dir);
        Path targetDir = PathUtil.resolvePath(target.getDecompressDir());
        // 将属于该应用的数据文件，迁移到新的工作空间 decompressDir 目录
        FileUtils.moveDirectoryToDirectory(sourceDir.toFile(), targetDir.toFile(), true);
        setTableDefinition(target, dirs);
        return target;
      }
    }
    catch (Exception e) {
      logger.error("Failed to upload zip. error={}", e.getMessage(), e);
    }
    clearWorkspace(params);
    return null;
  }

  /**
   * 获取导出的数据包
   * <p>1. 不采用 FileUtils.readFileToByteArray 方式输出文件流，避免出现 direct buffer memory 类型的 OOM </p>
   */
  public static File getZipFile(DataSyncParams params) {
    String fileName = getExportFileName(params);
    Path path = PathUtil.resolvePath(params.getCompressDir(), fileName);
    return path.toFile();
  }

  /**
   * 获取导出文件名称
   *
   * @param params 数据同步参数
   * @return 文件名称
   */
  private static String getExportFileName(DataSyncParams params) {
    if (StringUtils.isNotBlank(params.getExportFileName())) {
      return params.getExportFileName() + ".zip";
    }
    return String.format(FILE_NAME, params.getTenantId());
  }

  /**
   * 新建脚本文件
   */
  public static void createScriptFile(DataSyncParams params, String fileName, String content) {
    try {
      String scriptDir = PathUtil.resolvePath(params.getDecompressDir(), String.format(FILE_PATH, params.getTenantId()), SCRIPT_PATH).toString();
      Path path = PathUtil.resolvePath(scriptDir, fileName);
      FileUtils.write(path.toFile(), content, StandardCharsets.UTF_8);
    }
    catch (Exception e) {
      logger.error("Failed to create script file={}, error={}", content, e.getMessage(), e);
    }
  }

  public static void createRootJsonFile(DataSyncParams params) {
    Map<String, Object> args = new HashMap<>();
    args.put("tenantId", params.getTenantId());
    args.put("syncAll", params.getSyncAll());
    args.put("codeAndIds", params.getCodeAndIds());
    Path path = PathUtil.resolvePath(params.getDecompressDir(), String.format(FILE_PATH, params.getTenantId()), "ROOT.json");
    File file = path.toFile();
    try {
      FileUtils.forceMkdirParent(file);
      // 先创建文件再写数据
      FileUtils.touch(file);
      JsonUtil.write(file, args);
    }
    catch (IOException e) {
      logger.error("Failed to create root json file. error={}", e.getMessage(), e);
    }
  }

  /**
   * 按照同步节点粒度，在 decompressDir 工作目录下，新建节点 json 文件
   * <p>数据按照表粒度存储，当前的 json 文件只记录节点定义，dataRecords 参数需要清空</p>
   *
   * @param params 收集条件
   * @param definition 数据节点
   */
  public static void createNodeJsonFile(DataSyncParams params, DataSyncTableDefinition definition) {
    definition.setDataRecords(Collections.emptyList());
    String code = (definition.getDataConfigCode() + "-" + definition.getTableCode()).toUpperCase();
    Path path = PathUtil.resolvePath(params.getDecompressDir(), String.format(FILE_PATH, params.getTenantId()), code + ".json");
    File file = path.toFile();
    try {
      FileUtils.forceMkdirParent(file);
      // 先创建文件再写数据
      FileUtils.touch(file);
      JsonUtil.write(file, definition);
    }
    catch (IOException e) {
      logger.error("Failed to create node json file. error={}", e.getMessage(), e);
    }
  }

  /**
   * 按照表粒度，在 decompressDir 工作目录下新建表数据 json 文件
   * <p>1. 目的：旧版本配置数据集中在节点 json 文件，对于配置数据量过多的应用，这种模式，读写大文件性能差</p>
   * <p>2. 考虑到多个节点可能存在相同表定义，表文件命名规则：${path}-${tableCode}</p>
   *
   * @param params 收集条件
   * @param definition 表定义
   */
  @SuppressFBWarnings("REC_CATCH_EXCEPTION")
  public static void createTableJsonFile(DataSyncParams params, DataSyncTableDefinition definition) {
    try {
      // 将表里所有内容一次写入同一个文件。这种方式如果表里数据量太大时会发生 OOM
      String dir = getTableDir(params).toString();
      String code = (definition.getDataConfigCode() + "-" + definition.getTableCode()).toUpperCase();
      Path path = PathUtil.resolvePath(dir, code + ".json");
      File file = path.toFile();
      // 先创建文件再写数据
      FileUtils.forceMkdirParent(file);
      FileUtils.touch(file);
      JsonUtil.write(file, definition.getDataRecords());
    }
    catch (Exception e) {
      logger.error("Failed to create table json file. error={}", e.getMessage(), e);
    }
  }

  public static Path getTableDir(DataSyncParams params) {
    return PathUtil.resolvePath(params.getDecompressDir(), String.format(FILE_PATH, params.getTenantId()), "table");
  }

  private static List<String> getDirFromPath(String path) {
    File f = new File(path);
    if (!f.exists()) {
      return Collections.emptyList();
    }
    String[] s = f.list();
    if (s == null) {
      return Collections.emptyList();
    }
    return Arrays.asList(s);
  }

  private static void setTableDefinition(DataSyncParams params, List<String> dirs) {
    if (CollectionUtils.isEmpty(dirs)) {
      return;
    }
    List<DataSyncTableDefinition> definitions = new ArrayList<>();
    params.setDefinitions(definitions);
    for (String dir : dirs) {
      if ("ROOT.json".equals(dir)) {
        DataSyncParams dataSyncParams = getJsonFileContent(params, dir, new TypeReference<DataSyncParams>() {
        });
        if (dataSyncParams != null) {
          params.setTenantId(dataSyncParams.getTenantId());
          params.setSyncAll(dataSyncParams.getSyncAll());
          params.setCodeAndIds(dataSyncParams.getCodeAndIds());
        }
      }
      else if (dir.endsWith(".json")) {
        DataSyncTableDefinition definition = getJsonFileContent(params, dir, new TypeReference<DataSyncTableDefinition>() {
        });
        if (definition != null) {
          definitions.add(definition);
        }
      }
    }
  }

  private static <V> V getJsonFileContent(DataSyncParams params, String fileName, TypeReference<V> typeReference) {
    try {
      String path = PathUtil.resolvePath(params.getDecompressDir(), String.format(FILE_PATH, params.getTenantId()), fileName).toString();
      File file = new File(path);
      if (file.exists()) {
        String data = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        return JsonUtil.parseJson(data, typeReference);
      }
    }
    catch (Exception e) {
      logger.error("Failed to get json file content. fileName={}", fileName, e);
    }
    return null;
  }

  public static List<Map<String, Object>> getDataRecords(DataSyncParams params, String fileName) {
    try {
      String code = fileName.toUpperCase();
      String path = PathUtil.resolvePath(params.getDecompressDir(), String.format(FILE_PATH, params.getTenantId()), "table", code + ".json")
        .toString();
      File file = new File(path);
      if (file.exists()) {
        String data = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        return JsonUtil.parseJson(data, new TypeReference<List<Map<String, Object>>>() {
        });
      }
    }
    catch (Exception e) {
      logger.error("Failed to get data records. fileName={}", fileName, e);
    }
    return Collections.emptyList();
  }

  /**
   * 往应用数据包中指定文件写入内容
   *
   * @param params 数据同步参数
   * @param fileName 文件名称
   * @param datas 文件内容
   */
  public static void writeJsonFile(DataSyncParams params, String fileName, List<Map<String, Object>> datas) {
    try {
      String code = fileName.toUpperCase();
      Path path = PathUtil.resolvePath(params.getDecompressDir(), String.format(FILE_PATH, params.getTenantId()), "table", code + ".json");
      JsonUtil.write(path.toFile(), datas);
    }
    catch (RuntimeException e) {
      logger.error("Failed to write json file. error={}", e.getMessage(), e);
    }
  }

  public static boolean existsFile(DataSyncParams params, String fileName) {
    String code = fileName.toUpperCase();
    String path = PathUtil.resolvePath(params.getDecompressDir(), String.format(FILE_PATH, params.getTenantId()), "table", code + ".json").toString();
    File file = new File(path);
    return file.exists();
  }
}
