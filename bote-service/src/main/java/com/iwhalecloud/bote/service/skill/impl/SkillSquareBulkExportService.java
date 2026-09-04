package com.iwhalecloud.bote.service.skill.impl;

import com.iwhalecloud.bote.cache.SkillSquareBulkExportCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.SkillSquareConsts;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.base.SyncFileInfoRequest;
import com.iwhalecloud.bote.dto.skill.SkillMetaDTO;
import com.iwhalecloud.bote.dto.skill.SkillSquareBulkExportPartVO;
import com.iwhalecloud.bote.entity.skill.AgentSkillSquareEntity;
import com.iwhalecloud.bote.mapper.skill.AgentSkillSquareMapper;
import com.iwhalecloud.bote.service.base.IFileInfoManageService;
import com.iwhalecloud.bote.service.skill.SkillSquareBulkExportJobHandler;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 技能广场大批量异步导出：构建 zip 分包、上传并更新任务进度缓存。
 */
@Service
@RequiredArgsConstructor
public class SkillSquareBulkExportService implements SkillSquareBulkExportJobHandler {

  private static final Logger logger = LoggerFactory.getLogger(SkillSquareBulkExportService.class);

  /** 全量导出分页大小（串行预取路径），避免一次性加载全部行与大字段 */
  private static final int EXPORT_PAGE_SIZE = 200;
  /**
   * 启用并行预取时单页条数略小，避免一页内攒齐过多安装包字节导致内存尖峰。
   */
  private static final int BULK_EXPORT_PARALLEL_PAGE_SIZE = 50;
  /** 每累计该条数向缓存刷新一次「已处理技能数」 */
  private static final int BULK_EXPORT_PROGRESS_FLUSH_EVERY = 10;
  private final AgentSkillSquareMapper agentSkillSquareMapper;
  private final IFileStoreService fileStoreService;
  private final IFileInfoManageService fileInfoManageService;
  private final SkillSquareBulkExportCache skillSquareBulkExportCache;
  @Value("${bote.skill-square.bulk-export.fetch-parallelism:8}")
  private int bulkExportFetchParallelism;
  private volatile ExecutorService bulkExportFetchExecutor;

  private static void waitForTask(Future<PreparedSkillZipSlice> f,
                                  List<PreparedSkillZipSlice> out,
                                  List<Future<PreparedSkillZipSlice>> futures) throws IOException {
    try {
      out.add(f.get());
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      cancelFutures(futures);
      throw new IOException("技能导出预取被中断", e);
    }
    catch (ExecutionException e) {
      cancelFutures(futures);
      Throwable c = e.getCause();
      if (c instanceof Error err) {
        err.addSuppressed(e);
        throw err;
      }
      if (c instanceof IOException io) {
        io.addSuppressed(e);
        throw io;
      }
      if (c instanceof RuntimeException re) {
        re.addSuppressed(e);
        throw re;
      }
      IOException wrapped = new IOException("技能导出预取失败", c);
      wrapped.addSuppressed(e);
      throw wrapped;
    }
  }

  private static void cancelFutures(List<Future<PreparedSkillZipSlice>> futures) {
    for (Future<PreparedSkillZipSlice> f : futures) {
      f.cancel(true);
    }
  }

  @PostConstruct
  void initBulkExportFetchExecutor() {
    int n = bulkExportFetchParallelism;
    if (n <= 1) {
      bulkExportFetchExecutor = null;
      return;
    }
    int poolSize = Math.min(64, Math.max(2, n));
    bulkExportFetchExecutor = Executors.newFixedThreadPool(poolSize, r -> {
      Thread t = new Thread(r, "skill-square-export-fetch");
      t.setDaemon(true);
      return t;
    });
  }

  @PreDestroy
  void shutdownBulkExportFetchExecutor() {
    ExecutorService ex = bulkExportFetchExecutor;
    if (ex == null) {
      return;
    }
    ex.shutdown();
    try {
      if (!ex.awaitTermination(60, TimeUnit.SECONDS)) {
        ex.shutdownNow();
      }
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      ex.shutdownNow();
    }
  }

  @Override
  public void executeLargeExport(long jobId, Integer top) {
    assertExportTopValid(top);
    int total = (int) agentSkillSquareMapper.countForExport(top);
    int chunk = SkillSquareConsts.EXPORT_PACKAGE_MAX_RECORDS;
    int totalParts = (total + chunk - 1) / chunk;
    int baseOffset = 0;
    for (int p = 1; p <= totalParts; p++) {
      int n = Math.min(chunk, total - baseOffset);
      Path tmp = null;
      try {
        tmp = Files.createTempFile("skill-square-export-" + jobId + "-" + p + "-", ".zip");
        writeExportZipBodySegment(top, tmp, baseOffset, n, jobId);
        SkillSquareBulkExportPartVO partVo = uploadBulkExportZipPart(tmp, jobId, p, totalParts, n);
        skillSquareBulkExportCache.appendPart(jobId, partVo);
      }
      catch (IOException e) {
        throw new BssException("构建导出 zip 分包失败: 第 " + p + "/" + totalParts + " 包", e);
      }
      finally {
        if (tmp != null) {
          try {
            Files.deleteIfExists(tmp);
          }
          catch (IOException ignored) {
            // ignore
          }
        }
      }
      baseOffset += n;
    }
  }

  private void writeExportZipBodySegment(Integer topLimit, Path zipPath, int baseOffset, int maxSkills, long jobId)
    throws IOException {
    int segmentAdded = 0;
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath), StandardCharsets.UTF_8)) {
      List<SkillMetaDTO> manifest = new ArrayList<>();
      int offset = baseOffset;
      int added = 0;
      while (added < maxSkills) {
        int pageCap = bulkExportFetchExecutor != null ? BULK_EXPORT_PARALLEL_PAGE_SIZE : EXPORT_PAGE_SIZE;
        int pageLimit = Math.min(pageCap, maxSkills - added);
        List<AgentSkillSquareEntity> page = agentSkillSquareMapper.selectForExportPage(topLimit, offset, pageLimit);
        if (page.isEmpty()) {
          break;
        }
        List<PreparedSkillZipSlice> slices = prepareSkillZipSlicesParallel(page);
        for (PreparedSkillZipSlice slice : slices) {
          appendPreparedSkillToZip(zos, manifest, slice);
          added++;
          segmentAdded++;
          if (segmentAdded % BULK_EXPORT_PROGRESS_FLUSH_EVERY == 0) {
            skillSquareBulkExportCache.addProcessedRecords(jobId, BULK_EXPORT_PROGRESS_FLUSH_EVERY);
          }
          if (added >= maxSkills) {
            break;
          }
        }
        offset += page.size();
        if (page.size() < pageLimit) {
          break;
        }
      }
      putZipEntry(zos, SkillSquareConsts.ZIP_FILE_MANIFEST, JsonUtil.toJsonString(manifest));
      zos.finish();
    }
    int remainder = segmentAdded % BULK_EXPORT_PROGRESS_FLUSH_EVERY;
    if (remainder != 0) {
      skillSquareBulkExportCache.addProcessedRecords(jobId, remainder);
    }
  }

  private SkillSquareBulkExportPartVO uploadBulkExportZipPart(Path tmp, long jobId, int part1Based, int totalParts, int recordCount) {
    try (InputStream in = Files.newInputStream(tmp)) {
      UploadConfigVO config = new UploadConfigVO();
      config.setOriginalFileName(String.format("skill-square-export-part%d-of-%d.zip", part1Based, totalParts));
      config.setSubFolder(SkillSquareConsts.UPLOAD_SUBFOLDER_SQUARE);
      config.setSaveName("bulk-export-" + jobId + "-p" + part1Based + ".zip");
      config.setFileSize(Files.size(tmp));
      config.setFileType("zip");
      config.setIsPicture(false);
      FileInfoVO vo = fileStoreService.uploadFile(in, config);
      createFileInfoForSquare(vo);
      String downloadPath = BaseConsts.API_PREFIX + "file/download?fileId=" + vo.getFileId();
      return SkillSquareBulkExportPartVO.builder()
        .partIndex(part1Based)
        .recordCount(recordCount)
        .fileId(vo.getFileId())
        .downloadPath(downloadPath)
        .build();
    }
    catch (IOException e) {
      throw new BssException("读取导出临时 zip 失败", e);
    }
    catch (Exception e) {
      throw new BssException("上传导出分包失败: " + e.getMessage(), e);
    }
  }

  private void createFileInfoForSquare(FileInfoVO fileInfo) {
    Assert.notNull(fileInfo.getFileSize(), "文件大小不能为空");
    SyncFileInfoRequest req = new SyncFileInfoRequest();
    req.setFileId(fileInfo.getFileId());
    req.setStoreType(fileInfo.getStoreType());
    req.setFilePathInServer(fileInfo.getFilePathInServer());
    req.setFileName(fileInfo.getFileName());
    req.setFileSize(String.valueOf(fileInfo.getFileSize()));
    req.setTenantId(BaseConsts.PLATFORM_TENANT_ID);
    req.setBusiType(BaseConsts.FILE_BUSI_TYPE_AGENT_SKILL);
    ResultVO<FileInfoDTO> syncResult = fileInfoManageService.syncFileInfo(req);
    Assert.isTrue(syncResult.isSuccess(), syncResult.getResultMsg());
    FileInfoDTO dto = syncResult.getResultObject();
    Assert.notNull(dto, "同步文件信息返回为空");
  }

  private void assertExportTopValid(@Nullable Integer top) {
    if (top == null) {
      return;
    }
    if (top < 1) {
      throw new BssException("top 须为大于等于 1 的整数，或不传表示按全部上架技能导出");
    }
    if (top > SkillSquareConsts.EXPORT_TOP_MAX) {
      throw new BssException("top 不能超过 " + SkillSquareConsts.EXPORT_TOP_MAX);
    }
  }

  private List<PreparedSkillZipSlice> prepareSkillZipSlicesParallel(List<AgentSkillSquareEntity> page) throws IOException {
    if (page.isEmpty()) {
      return Collections.emptyList();
    }
    ExecutorService ex = bulkExportFetchExecutor;
    if (ex == null || page.size() == 1) {
      List<PreparedSkillZipSlice> out = new ArrayList<>(page.size());
      for (AgentSkillSquareEntity skill : page) {
        out.add(prepareSkillZipSlice(skill));
      }
      return out;
    }
    List<Future<PreparedSkillZipSlice>> futures = new ArrayList<>(page.size());
    for (AgentSkillSquareEntity skill : page) {
      futures.add(ex.submit(() -> prepareSkillZipSlice(skill)));
    }
    List<PreparedSkillZipSlice> out = new ArrayList<>(page.size());
    for (Future<PreparedSkillZipSlice> f : futures) {
      waitForTask(f, out, futures);
    }
    return out;
  }

  private PreparedSkillZipSlice prepareSkillZipSlice(AgentSkillSquareEntity skill) {
    SkillMetaDTO manifestEntry = toMetaDTO(skill);
    String metaJson = JsonUtil.toJsonString(buildMeta(skill));
    String skillMd = skill.getSkillContent() != null ? skill.getSkillContent() : "";
    byte[] packageBytes = null;
    if (skill.getFileInfoId() != null) {
      try {
        FileInfoDTO fi = fileInfoManageService.findFileInfo(BaseConsts.PLATFORM_TENANT_ID, skill.getFileInfoId());
        if (fi != null && fi.getFileId() != null) {
          byte[] pkg = fileStoreService.downloadFile(fi.getFileId());
          if (pkg != null && pkg.length > 0) {
            packageBytes = pkg;
          }
        }
      }
      catch (Exception e) {
        if (logger.isWarnEnabled()) {
          logger.warn("bulk export: skip skill package due to error, skillCode={}, fileInfoId={}",
            skill.getSkillCode(), skill.getFileInfoId(), e);
        }
      }
    }
    return new PreparedSkillZipSlice(manifestEntry, skill.getSkillCode(), metaJson, skillMd, packageBytes);
  }

  private void appendPreparedSkillToZip(ZipOutputStream zos, List<SkillMetaDTO> manifest, PreparedSkillZipSlice slice)
    throws IOException {
    manifest.add(slice.manifestEntry);
    String prefix = SkillSquareConsts.ZIP_DIR_SKILLS + slice.skillCode + "/";
    putZipEntry(zos, prefix + SkillSquareConsts.ZIP_FILE_META, slice.metaJson);
    putZipEntry(zos, prefix + SkillSquareConsts.ZIP_FILE_SKILL_MD, slice.skillMd);
    if (slice.packageBytes != null && slice.packageBytes.length > 0) {
      putZipEntry(zos, prefix + SkillSquareConsts.ZIP_FILE_PACKAGE, slice.packageBytes);
    }
  }

  private SkillMetaDTO buildMeta(AgentSkillSquareEntity s) {
    SkillMetaDTO m = new SkillMetaDTO();
    m.setSkillCode(s.getSkillCode());
    m.setSkillName(s.getSkillName());
    m.setSkillDesc(s.getSkillDesc());
    m.setSkillType(s.getSkillType());
    m.setTags(s.getTags());
    m.setVersion(s.getVersion() != null ? s.getVersion() : SkillMetaDTO.DEFAULT_VERSION);
    m.setSource(s.getSource() != null ? s.getSource() : SkillMetaDTO.DEFAULT_SOURCE);
    return m;
  }

  private SkillMetaDTO toMetaDTO(AgentSkillSquareEntity s) {
    SkillMetaDTO dto = new SkillMetaDTO();
    dto.setSkillCode(s.getSkillCode());
    dto.setSkillName(s.getSkillName());
    dto.setSkillType(s.getSkillType());
    dto.setVersion(s.getVersion() != null ? s.getVersion() : SkillMetaDTO.DEFAULT_VERSION);
    dto.setSource(s.getSource() != null ? s.getSource() : SkillMetaDTO.DEFAULT_SOURCE);
    return dto;
  }

  private void putZipEntry(ZipOutputStream zos, String name, String content) throws IOException {
    putZipEntry(zos, name, content.getBytes(StandardCharsets.UTF_8));
  }

  private void putZipEntry(ZipOutputStream zos, String name, byte[] content) throws IOException {
    zos.putNextEntry(new ZipEntry(name));
    zos.write(content);
    zos.closeEntry();
  }

  private record PreparedSkillZipSlice(SkillMetaDTO manifestEntry, String skillCode, String metaJson, String skillMd, byte[] packageBytes) {

  }
}
