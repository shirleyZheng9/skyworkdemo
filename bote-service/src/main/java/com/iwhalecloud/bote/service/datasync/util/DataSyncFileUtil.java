package com.iwhalecloud.bote.service.datasync.util;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.jdbc.LowerCaseColumnMapRowMapper;
import com.iwhalecloud.bote.common.util.FileTypeUtil;
import com.iwhalecloud.bote.common.util.PathUtil;
import com.iwhalecloud.bote.dto.datasync.DataSyncTableDefinition;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 数据同步辅助工具类 - 文件
 *
 * @author chen.linfa
 * @since 2024-10-25
 */
public final class DataSyncFileUtil {
  private DataSyncFileUtil() {
  }

  private static final Logger logger = LoggerFactory.getLogger(DataSyncFileUtil.class);

  /** 文件文件存储父目录 */
  private static final String FILE_RESOURCE_PATH = "resource";

  /**
   * 收集文件资源
   * <p>1. 解析同步节点中获取 fileId 集合</p>
   * <p>2. 调用文件系统，下载文件资源，存储到 decompressDir 目录下 resource 子目录</p>
   * <p>3. 文件命名规则：${fileId}-${fileName}</p>
   */
  public static void collectFileResource(DataSyncParams params) {
    List<DataSyncTableDefinition> tables = params.getDefinitions().stream()
      .filter(p -> "bt_file_info".equals(p.getTableCode()))
      .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(tables)) {
      return;
    }
    List<Long> fileIds = new ArrayList<>();
    for (DataSyncTableDefinition table : tables) {
      fileIds.addAll(
        CollectionUtils.emptyIfNull(DataSyncDirUtil.getDataRecords(params, table.getDataConfigCode() + "-" + table.getTableCode())).stream()
          .map(p -> MapUtils.getLong(p, "file_id")).collect(Collectors.toList()));
    }
    if (CollectionUtils.isEmpty(fileIds)) {
      return;
    }
    try {
      IFileStoreService fileStoreService = SpringUtil.getBean(IFileStoreService.class);
      List<FileInfoVO> fileInfos = fileStoreService.getFileInfoByIds(fileIds);
      String dir = PathUtil.resolvePath(params.getDecompressDir(), String.format(DataSyncDirUtil.FILE_PATH, params.getTenantId()), FILE_RESOURCE_PATH)
        .toString();
      for (FileInfoVO fileInfo : fileInfos) {
        Path path = PathUtil.resolvePath(dir, fileInfo.getFileId() + "-" + fileInfo.getFileName());
        FileUtils.forceMkdirParent(path.toFile());
        // 下载文件到本地
        fileStoreService.downloadFile(fileInfo.getFileId(), path.toString());
      }
    }
    catch (Exception e) {
      logger.error("Failed to collect file resource={}", fileIds, e);
    }
  }

  /**
   * 读取文件资源
   * <p>1. 读取 decompressDir/resource 目录下的文件列表</p>
   * <p>2. 上传文件到文件系统</p>
   * <p>3. 文件命名规则：${fileId}-${fileName}，记录新旧 fileId 的映射关系，方便后面的保存数据前重置 fileId</p>
   * <p>4. 应用复制场景，需要强制更新文件资源（忽略文件大小比对）</p>
   */
  @SuppressFBWarnings({"SECSQLISPRJDBC", "REC_CATCH_EXCEPTION"})
  @SuppressWarnings("PMD.GuardLogStatement")
  public static void readFileResource(DataSyncParams params) {
    try {
      Collection<File> files = getFileList(params);
      if (CollectionUtils.isEmpty(files)) {
        return;
      }

      logger.debug("开始处理文件资源，共 {} 个文件，复制模式: {}", files.size(), params.isCopy() ? "同环境跨租户" : "跨环境同租户");
      Map<Long, Long> fileIdMapper = new HashMap<>(16);

      if (params.isCopy()) {
        processCopyMode(files, params, fileIdMapper);
      } else {
        processNormalMode(files, fileIdMapper);
      }

      params.setFileIdMap(fileIdMapper);
    }
    catch (Exception e) {
      logger.error("Failed to read file resource", e);
    }
  }

  /**
   * 获取文件列表
   */
  private static Collection<File> getFileList(DataSyncParams params) {
    String path = PathUtil.resolvePath(params.getDecompressDir(), String.format(DataSyncDirUtil.FILE_PATH, params.getTenantId()),
      FILE_RESOURCE_PATH).toString();
    File dir = new File(path);
    if (!dir.exists()) {
      return Collections.emptyList();
    }
    return FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
  }

  /**
   * 处理同环境跨租户复制模式
   */
  private static void processCopyMode(Collection<File> files, DataSyncParams params, Map<Long, Long> fileIdMapper) throws Exception {
    logger.debug("同环境跨租户复制模式，所有文件都需要为目标租户创建新记录");
    IFileStoreService fileStoreService = SpringUtil.getBean(IFileStoreService.class);
    for (File file : files) {
      Long oldFileId = extractFileId(file);
      String fileName = extractFileName(file);
      FileInfoVO fileInfo = uploadFileForCopyMode(file, fileName, params, fileStoreService);
      fileIdMapper.put(oldFileId, fileInfo.getFileId());
    }
  }

  /**
   * 处理跨环境同租户复制模式
   */
  private static void processNormalMode(Collection<File> files, Map<Long, Long> fileIdMapper) throws Exception {
    logger.debug("跨环境同租户模式，根据文件大小判断是否需要重新上传");

    List<Map<String, Object>> records = queryExistingFileRecords();
    IFileStoreService fileStoreService = SpringUtil.getBean(IFileStoreService.class);

    for (File file : files) {
      Long oldFileId = extractFileId(file);
      String fileName = extractFileName(file);

      if (shouldSkipFileUpload(file, oldFileId, records)) {
        continue;
      }

      FileInfoVO fileInfo = uploadFileForNormalMode(file, fileName, oldFileId, records, fileStoreService);
      fileIdMapper.put(oldFileId, fileInfo.getFileId());
    }
  }

  /**
   * 为跨租户模式上传文件
   */
  private static FileInfoVO uploadFileForCopyMode(File file, String fileName, DataSyncParams params, IFileStoreService fileStoreService) throws Exception {
    UploadConfigVO uploadConfig = createUploadConfig(fileName, file.length(), params.getResetTenantId());
    setFileType(uploadConfig, file);
    return fileStoreService.uploadFile(file, uploadConfig);
  }

  /**
   * 为普通模式上传文件
   */
  private static FileInfoVO uploadFileForNormalMode(File file, String fileName, Long oldFileId,
      List<Map<String, Object>> records, IFileStoreService fileStoreService) throws Exception {
    Map<String, Object> record = IterableUtils.find(CollectionUtils.emptyIfNull(records),
      p -> Objects.equals(MapUtils.getLong(p, "file_id"), oldFileId));

    UploadConfigVO uploadConfig = createUploadConfig(fileName, file.length(), MapUtils.getLong(record, "tenant_id"));
    setFileType(uploadConfig, file);
    return fileStoreService.uploadFile(file, uploadConfig);
  }

  /**
   * 创建上传配置
   */
  private static UploadConfigVO createUploadConfig(String fileName, long fileSize, Long tenantId) {
    UploadConfigVO uploadConfig = new UploadConfigVO();
    uploadConfig.setAppId(tenantId);
    uploadConfig.setOriginalFileName(fileName);
    uploadConfig.setFileSize(fileSize);
    return uploadConfig;
  }

  /**
   * 设置文件类型
   */
  private static void setFileType(UploadConfigVO uploadConfig, File file) throws Exception {
    try (InputStream inputStream = Files.newInputStream(file.toPath())) {
      String fileType = FileTypeUtil.getType(inputStream, StringUtils.substringAfterLast(file.getName(), "."));
      uploadConfig.setFileType(fileType);
    }
  }

  /**
   * 查询现有文件记录
   */
  private static List<Map<String, Object>> queryExistingFileRecords() {
    String sql = "select a.file_id, b.file_size, a.tenant_id FROM bt_file_info a, bt_file b where a.file_id = b.file_id and a.status_cd = ?";
    return SpringUtil.getBean(JdbcTemplate.class)
      .query(sql, new LowerCaseColumnMapRowMapper(), BaseConsts.STATUS_CD_VALID);
  }

  /**
   * 判断是否应该跳过文件上传
   */
  private static boolean shouldSkipFileUpload(File file, Long oldFileId, List<Map<String, Object>> records) {
    Long fileSize = file.length();
    for (Map<String, Object> p : records) {
      if (oldFileId.equals(MapUtils.getLong(p, "file_id")) && fileSize.equals(MapUtils.getLong(p, "file_size"))) {
        return true;
      }
    }
    return false;
  }

  /**
   * 从文件名中提取文件ID
   */
  private static Long extractFileId(File file) {
    return Long.valueOf(file.getName().substring(0, file.getName().indexOf("-")));
  }

  /**
   * 从文件名中提取原始文件名
   */
  private static String extractFileName(File file) {
    return file.getName().substring(file.getName().indexOf("-") + 1);
  }

}
