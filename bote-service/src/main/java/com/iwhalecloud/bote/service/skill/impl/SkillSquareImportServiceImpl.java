package com.iwhalecloud.bote.service.skill.impl;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.agent.skill.AgentSkillMarkdownParser;
import com.iwhalecloud.bote.agent.skill.AgentSkillSpec;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.SkillSquareConsts;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.common.util.SkillCodeUtils;
import com.iwhalecloud.bote.common.util.SkillZipReadUtil;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.skill.AdminImportRequest;
import com.iwhalecloud.bote.dto.skill.ImportAllResponse;
import com.iwhalecloud.bote.dto.skill.SkillMetaDTO;
import com.iwhalecloud.bote.dto.skill.SkillSquareExistingKey;
import com.iwhalecloud.bote.entity.skill.AgentSkillSquareEntity;
import com.iwhalecloud.bote.mapper.base.FileInfoManageMapper;
import com.iwhalecloud.bote.mapper.skill.AgentSkillSquareMapper;
import com.iwhalecloud.bote.service.skill.ISkillSquareImportService;
import com.iwhalecloud.bote.service.skill.support.AgentSkillZipSupport;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * {@link ISkillSquareImportService}
 */
@Service
@RequiredArgsConstructor
public class SkillSquareImportServiceImpl implements ISkillSquareImportService {

  private static final Logger logger = LoggerFactory.getLogger(SkillSquareImportServiceImpl.class);
  /** 批量导入并行写入时每批提交任务数，避免一次性 submit 超过公共线程池并发上限 */
  private static final int BULK_IMPORT_TASK_BATCH_SIZE = 100;
  /** 预加载已有 skill_code 映射时的分页大小，避免单次查询结果集过大 */
  private static final int EXISTING_KEYS_PAGE_SIZE = 2000;
  /** 预加载分页最大循环次数，防止分页异常导致死循环（约可覆盖 pageSize * 此次数 行） */
  private static final int EXISTING_KEYS_LOAD_MAX_ITERATIONS = 10000;

  private final AgentSkillSquareMapper agentSkillSquareMapper;
  private final IFileStoreService fileStoreService;
  private final FileInfoManageMapper fileInfoManageMapper;

  private static String buildPackageSaveName(String skillCode, String version) {
    String safeCode = SkillCodeUtils.toSafeId(skillCode);
    String safeVer = SkillCodeUtils.toSafeId(version);
    return safeCode + "_v" + safeVer + ".zip";
  }

  @Nullable
  private static String normalizeMetaTagsToString(@Nullable Object tagsObj) {
    if (tagsObj == null) {
      return null;
    }
    if (tagsObj instanceof String s) {
      String t = s.trim();
      if (t.isEmpty()) {
        return null;
      }
      if (t.startsWith("{") || t.startsWith("[")) {
        return t;
      }
      return JsonUtil.toJsonString(s);
    }
    return JsonUtil.toJsonString(tagsObj);
  }

  private static void cleanupBulkPackageTemps(Map<String, SkillImportData> byCode) {
    for (SkillImportData data : byCode.values()) {
      data.discardBulkPackageTemp();
    }
  }

  private static void resolveImportData(InputStream in,
                                        Map<String, SkillImportData> result,
                                        String code,
                                        String file) throws IOException {
    SkillImportData data = result.computeIfAbsent(code, k -> new SkillImportData());
    switch (file) {
      case SkillSquareConsts.ZIP_FILE_META -> {
        byte[] bytes = SkillZipReadUtil.readEntryBytesLimited(in, SkillSquareConsts.MAX_BULK_IMPORT_TEXT_ENTRY_BYTES);
        data.meta = JsonUtil.parseJson(new String(bytes, StandardCharsets.UTF_8), SkillMetaDTO.class);
      }
      case SkillSquareConsts.ZIP_FILE_SKILL_MD -> {
        byte[] bytes = SkillZipReadUtil.readEntryBytesLimited(in, SkillSquareConsts.MAX_BULK_IMPORT_TEXT_ENTRY_BYTES);
        data.skillSpec = AgentSkillMarkdownParser.parseSkill("", new String(bytes, StandardCharsets.UTF_8));
      }
      case SkillSquareConsts.ZIP_FILE_PACKAGE -> {
        if (data.packageTempPath != null) {
          Files.deleteIfExists(data.packageTempPath);
          data.packageTempPath = null;
        }
        Path tmp = Files.createTempFile("sq-import-pkg-", ".zip");
        try (OutputStream out = Files.newOutputStream(tmp, StandardOpenOption.TRUNCATE_EXISTING)) {
          SkillZipReadUtil.copyStreamLimited(in, out, SkillSquareConsts.MAX_BULK_IMPORT_ENTRY_BYTES);
        }
        catch (IOException ex) {
          Files.deleteIfExists(tmp);
          throw ex;
        }
        data.packageTempPath = tmp;
      }
      default -> {
        // 忽略其他文件
      }
    }
  }

  private static byte[] readPackagePayload(SkillImportData data) throws IOException {
    if (data.packageTempPath != null) {
      return Files.readAllBytes(data.packageTempPath);
    }
    if (data.packageBytes != null && data.packageBytes.length > 0) {
      return data.packageBytes;
    }
    return new byte[0];
  }

  private SkillImportData parseAgentSkillZipForSquareImport(byte[] zipBytes) {
    AgentSkillSpec skillSpec = AgentSkillZipSupport.parseStandardLayout(zipBytes);
    SkillImportData data = new SkillImportData();
    data.skillSpec = skillSpec;
    data.packageBytes = zipBytes;
    return data;
  }

  @Nullable
  private String resolveSkillCodeFromImportData(SkillImportData data, String overrideSkillCode) {
    if (StringUtils.isNotBlank(overrideSkillCode)) {
      return SkillCodeUtils.toSafeId(overrideSkillCode);
    }
    if (data.meta != null && StringUtils.isNotBlank(data.meta.getSkillCode())) {
      return data.meta.getSkillCode();
    }
    if (data.skillSpec != null) {
      String name = data.skillSpec.getName();
      if (StringUtils.isNotBlank(name)) {
        return SkillCodeUtils.nameToSlug(name);
      }
    }
    return null;
  }

  private void prepareMetaForImport(SkillImportData data, String skillCode, @Nullable String skillTypeInSquare) {
    if (data.meta == null) {
      data.meta = new SkillMetaDTO();
    }
    data.meta.setSkillCode(skillCode);
    data.meta.setSkillType(skillTypeInSquare != null ? skillTypeInSquare : SkillMetaDTO.DEFAULT_SKILL_TYPE);
    data.meta.setSource(SkillMetaDTO.DEFAULT_SOURCE);
    if (data.meta.getVersion() == null) {
      data.meta.setVersion(SkillMetaDTO.DEFAULT_VERSION);
    }
    if (data.skillSpec != null) {
      if (StringUtils.isBlank(data.meta.getSkillName())) {
        String name = data.skillSpec.getName();
        data.meta.setSkillName(StringUtils.isNotBlank(name) ? name : skillCode);
      }
      if (StringUtils.isBlank(data.meta.getSkillDesc())) {
        data.meta.setSkillDesc(data.skillSpec.getDescription());
      }
    }
  }

  @Override
  @Transactional
  public ResultVO<Long> importOne(AdminImportRequest request) {
    return importOneFromZip(request.getPackageFile(), request.getSkillCode(), request.getSkillTypeInSquare());
  }

  @Override
  public ResultVO<ImportAllResponse> importAll(MultipartFile zipFile) {
    if (zipFile.isEmpty()) {
      return ResultVO.fail("请上传 zip 文件");
    }
    String name = zipFile.getOriginalFilename();
    if (!SkillSquareConsts.hasAllowedArchiveExtension(name)) {
      return ResultVO.fail(SkillSquareConsts.getArchiveExtensionHint());
    }
    if (logger.isInfoEnabled()) {
      logger.info("技能广场批量导入开始, originalFilename={}, sizeBytes={}", name, zipFile.getSize());
    }
    long startMills = System.currentTimeMillis();
    Path tempZip = null;
    try {
      tempZip = Files.createTempFile("skill-square-bulk-", ".zip");
      try (InputStream in = zipFile.getInputStream()) {
        Files.copy(in, tempZip, StandardCopyOption.REPLACE_EXISTING);
      }
      ImportAllResponse result = doImportSync(tempZip);
      long costMs = System.currentTimeMillis() - startMills;
      if (logger.isInfoEnabled()) {
        logger.info(
          "技能广场批量导入结束, success=true, costMs={}, created={}, updated={}, failed={}",
          costMs, result.getCreated(), result.getUpdated(), result.getFailed());
      }
      return ResultVO.success(result);
    }
    catch (IOException e) {
      long costMs = System.currentTimeMillis() - startMills;
      if (logger.isInfoEnabled()) {
        logger.info("技能广场批量导入结束, success=false, costMs={}, error={}", costMs, e.getMessage());
      }
      return ResultVO.fail("解析 zip 失败: " + e.getMessage());
    }
    finally {
      if (tempZip != null) {
        try {
          Files.deleteIfExists(tempZip);
        }
        catch (IOException e) {
          logger.warn("删除临时 zip 失败: {}", tempZip, e);
        }
      }
    }
  }

  private ResultVO<Long> importOneFromZip(@Nullable MultipartFile packageFile, String skillCode, String skillTypeInSquare) {
    if (packageFile == null || packageFile.isEmpty()) {
      return ResultVO.fail("请上传 zip 文件");
    }
    String filename = packageFile.getOriginalFilename();
    if (!SkillSquareConsts.hasAllowedArchiveExtension(filename)) {
      return ResultVO.fail(SkillSquareConsts.getArchiveExtensionHint());
    }
    try {
      byte[] zipBytes = packageFile.getBytes();
      SkillImportData data = parseAgentSkillZipForSquareImport(zipBytes);
      String resolvedCode = resolveSkillCodeFromImportData(data, skillCode);
      if (StringUtils.isBlank(resolvedCode)) {
        return ResultVO.fail("无法解析 skillCode，请指定 skillCode 参数或使用 SKILL.md 元数据中的 name");
      }
      AgentSkillSquareEntity existing = agentSkillSquareMapper.selectByCode(resolvedCode);
      if (existing != null) {
        return ResultVO.fail("技能编码已存在: " + resolvedCode + "，请勿重复导入");
      }
      prepareMetaForImport(data, resolvedCode, skillTypeInSquare);
      importOneSkillData(resolvedCode, data, null);
      AgentSkillSquareEntity inserted = agentSkillSquareMapper.selectByCode(resolvedCode);
      return ResultVO.success(inserted != null ? inserted.getSkillId() : null);
    }
    catch (IllegalArgumentException e) {
      return ResultVO.fail(e.getMessage());
    }
    catch (IOException e) {
      return ResultVO.fail("解析 zip 失败: " + e.getMessage());
    }
  }

  private ImportAllResponse doImportSync(Path zipPath) throws IOException {
    long zipOnDiskBytes = Files.size(zipPath);
    if (logger.isInfoEnabled()) {
      logger.info("技能广场批量导入处理 zip 文件, path={}, sizeBytes={}", zipPath.toAbsolutePath(), zipOnDiskBytes);
    }
    Map<String, SkillImportData> byCode = collectSkillDataFromZipPath(zipPath);
    if (logger.isInfoEnabled()) {
      logger.info("技能广场批量导入解析完成, distinctSkillCodes={}, 即将并行写入(每批{}条)",
        byCode.size(), BULK_IMPORT_TASK_BATCH_SIZE);
    }
    Map<String, SkillSquareExistingKey> existingByCode = loadExistingKeysForBulkImport();
    if (logger.isInfoEnabled()) {
      logger.info("技能广场批量导入预加载已有技能编码数={}", existingByCode.size());
    }
    AtomicInteger created = new AtomicInteger(0);
    AtomicInteger updated = new AtomicInteger(0);
    AtomicInteger failed = new AtomicInteger(0);

    try {
      List<Runnable> tasks = new ArrayList<>(byCode.size());
      for (Map.Entry<String, SkillImportData> entry : byCode.entrySet()) {
        String skillCode = entry.getKey();
        SkillImportData data = entry.getValue();
        tasks.add(() -> {
          try {
            boolean isNew = importOneSkillData(skillCode, data, existingByCode);
            if (isNew) {
              created.incrementAndGet();
            }
            else {
              updated.incrementAndGet();
            }
          }
          catch (Exception e) {
            failed.incrementAndGet();
            if (logger.isWarnEnabled()) {
              logger.warn("技能广场批量导入单条失败, skillCode={}", skillCode, e);
            }
          }
          finally {
            data.discardBulkPackageTemp();
          }
        });
      }
      for (int from = 0; from < tasks.size(); from += BULK_IMPORT_TASK_BATCH_SIZE) {
        int to = Math.min(from + BULK_IMPORT_TASK_BATCH_SIZE, tasks.size());
        ThreadPools.invokeTasks(ThreadPools.getCommon(), tasks.subList(from, to));
      }
      if (logger.isInfoEnabled()) {
        logger.info("技能广场批量导入并行写入结束, created={}, updated={}, failed={}",
          created.get(), updated.get(), failed.get());
      }

      return ImportAllResponse.builder()
        .created(created.get())
        .updated(updated.get())
        .failed(failed.get())
        .build();
    }
    finally {
      cleanupBulkPackageTemps(byCode);
    }
  }

  /**
   * 分页加载 status_cd=00A 的已有技能，构建 skill_code -> 主键/包文件引用映射。
   */
  private Map<String, SkillSquareExistingKey> loadExistingKeysForBulkImport() {
    Map<String, SkillSquareExistingKey> map = new HashMap<>();
    int offset = 0;
    int iterations = 0;
    while (true) {
      iterations++;
      if (iterations > EXISTING_KEYS_LOAD_MAX_ITERATIONS) {
        if (logger.isErrorEnabled()) {
          logger.error(
            "技能广场批量导入预加载已有技能超过最大循环次数, maxIterations={}, pageSize={}, lastOffset={}",
            EXISTING_KEYS_LOAD_MAX_ITERATIONS, EXISTING_KEYS_PAGE_SIZE, offset);
        }
        throw new BssException("预加载已有技能超过最大分页次数限制，请检查数据或联系管理员");
      }
      Page<SkillSquareExistingKey> page = agentSkillSquareMapper.selectExistingKeysForBulkImportPage(
        new RowBounds(offset, EXISTING_KEYS_PAGE_SIZE));
      if (page.isEmpty()) {
        break;
      }
      for (SkillSquareExistingKey row : page) {
        map.put(row.getSkillCode(), row);
      }
      offset += page.size();
      if (page.size() < EXISTING_KEYS_PAGE_SIZE) {
        break;
      }
    }
    return map;
  }

  /**
   * 处理批量导入 zip 中的单条条目：更新计数器并在路径符合 skills/skillCode/ 目录结构时解析写入 result。
   */
  private static void processBulkImportZipEntry(String rawEntryName,
                                                InputStream in,
                                                int skillsPrefixLen,
                                                Map<String, SkillImportData> result,
                                                BulkImportZipScanCounters scanCounter) throws IOException {
    scanCounter.fileEntryTotal.incrementAndGet();
    String name = rawEntryName.replace('\\', '/');
    if (SkillZipReadUtil.entryPathHasDotDotSegment(name)) {
      scanCounter.skippedDotDot.incrementAndGet();
      return;
    }
    if (!name.startsWith(SkillSquareConsts.ZIP_DIR_SKILLS)) {
      scanCounter.skippedNotUnderSkills.incrementAndGet();
      if (scanCounter.sampleNonSkillsPaths.size() < BulkImportZipScanCounters.SAMPLE_LIMIT) {
        scanCounter.sampleNonSkillsPaths.add(name);
      }
      return;
    }
    String remainder = name.substring(skillsPrefixLen);
    int slash = remainder.indexOf('/');
    if (slash <= 0) {
      scanCounter.skippedSkillsLayout.incrementAndGet();
      return;
    }
    String code = remainder.substring(0, slash);
    String file = remainder.substring(slash + 1);
    scanCounter.matchedLayoutEntries.incrementAndGet();
    resolveImportData(in, result, code, file);
  }

  private Map<String, SkillImportData> collectSkillDataFromZipPath(Path zipPath) throws IOException {
    Map<String, SkillImportData> result = new HashMap<>();
    int prefixLen = SkillSquareConsts.ZIP_DIR_SKILLS.length();
    BulkImportZipScanCounters counters = new BulkImportZipScanCounters();
    try {
      SkillZipReadUtil.walkUtf8ZipFile(zipPath,
        (e, in) -> processBulkImportZipEntry(e.getName(), in, prefixLen, result, counters));
    }
    catch (IOException ex) {
      cleanupBulkPackageTemps(result);
      throw ex;
    }
    logBulkImportZipScanSummary(result, counters);
    return result;
  }

  private void logBulkImportZipScanSummary(Map<String, SkillImportData> result, BulkImportZipScanCounters c) {
    if (logger.isInfoEnabled()) {
      logger.info(
        "技能广场批量导入 zip 条目扫描: fileEntryTotal={}, skippedPathDotDot={}, skippedNotUnderSkillsPrefix={}, "
          + "skippedUnderSkillsButBadLayout={}, matchedSkillsRelativePath={}, distinctSkillCodes={}, "
          + "sampleNonSkillsPaths(first{})={}",
        c.fileEntryTotal.get(), c.skippedDotDot.get(), c.skippedNotUnderSkills.get(), c.skippedSkillsLayout.get(),
        c.matchedLayoutEntries.get(), result.size(), BulkImportZipScanCounters.SAMPLE_LIMIT, c.sampleNonSkillsPaths);
    }
    if (result.isEmpty() && c.fileEntryTotal.get() > 0) {
      if (logger.isWarnEnabled()) {
        logger.warn(
          "批量导入未得到任何技能：当前仅识别以 \"{}\" 开头的路径（区分大小写），且需满足 skills/<skillCode>/meta.json、SKILL.md、package.zip。"
            + " 若压缩包多一层根目录或大小写为 Skills/，将不会被识别。请与系统「导出」生成的目录结构对比。",
          SkillSquareConsts.ZIP_DIR_SKILLS);
      }
    }
    if (!result.isEmpty() && result.size() <= 40) {
      if (logger.isInfoEnabled()) {
        logger.info("技能广场批量导入解析到的 skillCode 列表: {}", result.keySet());
      }
    }
  }

  private void overwritePackageBytes(Long fileInfoId, String skillCode, String version, byte[] bytes) {
    try {
      UploadConfigVO config = new UploadConfigVO();
      config.setOriginalFileName(SkillSquareConsts.ZIP_FILE_PACKAGE);
      config.setSubFolder(SkillSquareConsts.UPLOAD_SUBFOLDER_SQUARE);
      config.setSaveName(buildPackageSaveName(skillCode, version));
      config.setFileSize((long) bytes.length);
      config.setFileType("zip");
      config.setIsPicture(false);
      FileInfoVO newFileInfo = fileStoreService.uploadFile(bytes, config);
      Long updatorId = SessionUtil.getOptionalUserId();
      fileInfoManageMapper.updateFileIdByFileInfoId(
        BaseConsts.PLATFORM_TENANT_ID, fileInfoId, newFileInfo.getFileId(), SkillSquareConsts.ZIP_FILE_PACKAGE, updatorId);
    }
    catch (Exception e) {
      logger.warn("覆盖更新 {} 失败, fileInfoId={}", SkillSquareConsts.ZIP_FILE_PACKAGE, fileInfoId, e);
      throw new BssException("覆盖更新 " + SkillSquareConsts.ZIP_FILE_PACKAGE + " 失败: " + e.getMessage(), e);
    }

  }

  private boolean importOneSkillData(String skillCode, SkillImportData data,
                                     @Nullable Map<String, SkillSquareExistingKey> existingByCode) {
    if (data.meta == null) {
      throw new IllegalArgumentException("缺少 " + SkillSquareConsts.ZIP_FILE_META);
    }
    byte[] packagePayload;
    try {
      packagePayload = readPackagePayload(data);
    }
    catch (IOException e) {
      throw new IllegalArgumentException("读取 " + SkillSquareConsts.ZIP_FILE_PACKAGE + " 失败: " + e.getMessage(), e);
    }
    SkillMetaDTO meta = data.meta;
    AgentSkillSquareEntity existing = null;
    if (existingByCode != null) {
      SkillSquareExistingKey key = existingByCode.get(skillCode);
      if (key != null) {
        existing = new AgentSkillSquareEntity();
        existing.setSkillId(key.getSkillId());
        existing.setFileInfoId(key.getFileInfoId());
      }
    }
    if (existing == null) {
      existing = agentSkillSquareMapper.selectByCode(skillCode);
    }
    if (existing != null) {
      AgentSkillSquareEntity update = new AgentSkillSquareEntity();
      update.setSkillId(existing.getSkillId());
      populateSkillFieldsFromImport(update, skillCode, data);
      attachPackageOnUpdate(existing, update, skillCode, meta, packagePayload);
      agentSkillSquareMapper.updateById(update);
      return false;
    }
    AgentSkillSquareEntity insert = new AgentSkillSquareEntity();
    insert.setSkillId(IDUtils.nextId());
    insert.setSkillCode(skillCode);
    populateSkillFieldsFromImport(insert, skillCode, data);
    insert.setSource(meta.getSource() != null ? meta.getSource() : SkillMetaDTO.DEFAULT_SOURCE);
    insert.setOnlineStatus(BaseConsts.TRUE);
    attachPackageOnInsert(insert, skillCode, meta, packagePayload);
    agentSkillSquareMapper.insert(insert);
    return true;
  }

  private void populateSkillFieldsFromImport(AgentSkillSquareEntity entity, String skillCode, SkillImportData data) {
    SkillMetaDTO meta = data.meta;
    entity.setSkillName(meta.getSkillName() != null ? meta.getSkillName() : skillCode);
    entity.setSkillDesc(meta.getSkillDesc());
    entity.setSkillType(meta.getSkillType() != null ? meta.getSkillType() : SkillMetaDTO.DEFAULT_SKILL_TYPE);
    entity.setTags(normalizeMetaTagsToString(meta.getTags()));
    entity.setVersion(meta.getVersion() != null ? meta.getVersion() : SkillMetaDTO.DEFAULT_VERSION);
    entity.setSkillContent(data.skillSpec != null ? data.skillSpec.getContent() : null);
  }

  private void attachPackageOnUpdate(AgentSkillSquareEntity existing, AgentSkillSquareEntity update,
                                     String skillCode, SkillMetaDTO meta, byte[] packageBytes) {
    if (packageBytes.length == 0) {
      return;
    }
    String version = meta.getVersion() != null ? meta.getVersion() : SkillMetaDTO.DEFAULT_VERSION;
    if (existing.getFileInfoId() != null) {
      overwritePackageBytes(existing.getFileInfoId(), skillCode, version, packageBytes);
    }
    else {
      update.setFileInfoId(uploadPackageBytes(skillCode, version, packageBytes));
    }
  }

  private void attachPackageOnInsert(AgentSkillSquareEntity insert, String skillCode, SkillMetaDTO meta,
                                     byte[] packageBytes) {
    if (packageBytes.length == 0) {
      return;
    }
    String version = meta.getVersion() != null ? meta.getVersion() : SkillMetaDTO.DEFAULT_VERSION;
    insert.setFileInfoId(uploadPackageBytes(skillCode, version, packageBytes));
  }

  @Nullable
  private Long uploadPackageBytes(String skillCode, String version, byte[] bytes) {
    try {
      UploadConfigVO config = new UploadConfigVO();
      config.setOriginalFileName(SkillSquareConsts.ZIP_FILE_PACKAGE);
      config.setSubFolder(SkillSquareConsts.UPLOAD_SUBFOLDER_SQUARE);
      config.setSaveName(buildPackageSaveName(skillCode, version));
      config.setFileSize((long) bytes.length);
      config.setFileType("zip");
      config.setIsPicture(false);
      FileInfoVO fileInfo = fileStoreService.uploadFile(bytes, config);
      return createFileInfoForSquare(fileInfo);
    }
    catch (Exception e) {
      logger.warn("上传 {} 失败", SkillSquareConsts.ZIP_FILE_PACKAGE, e);
      return null;
    }
  }

  private Long createFileInfoForSquare(FileInfoVO fileInfo) {
    Long fileInfoId = IDUtils.nextId();
    FileInfoDTO dto = new FileInfoDTO();
    dto.setFileInfoId(fileInfoId);
    dto.setFileId(fileInfo.getFileId());
    dto.setFileName(fileInfo.getFileName());
    dto.setTenantId(BaseConsts.PLATFORM_TENANT_ID);
    dto.setBusiType(BaseConsts.FILE_BUSI_TYPE_AGENT_SKILL);
    dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
    dto.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    dto.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    fileInfoManageMapper.insertFileInfo(dto);
    return fileInfoId;
  }

  /** 批量导入 zip 扫描过程的计数与采样（仅 {@link #collectSkillDataFromZipPath} 使用）。 */
  private static final class BulkImportZipScanCounters {
    private static final int SAMPLE_LIMIT = 20;
    final AtomicInteger fileEntryTotal = new AtomicInteger(0);
    final AtomicInteger skippedDotDot = new AtomicInteger(0);
    final AtomicInteger skippedNotUnderSkills = new AtomicInteger(0);
    final AtomicInteger skippedSkillsLayout = new AtomicInteger(0);
    final AtomicInteger matchedLayoutEntries = new AtomicInteger(0);
    final List<String> sampleNonSkillsPaths = Collections.synchronizedList(new ArrayList<>());
  }

  private static final class SkillImportData {
    SkillMetaDTO meta;
    byte[] packageBytes;
    @Nullable
    Path packageTempPath;
    AgentSkillSpec skillSpec;

    void discardBulkPackageTemp() {
      if (packageTempPath == null) {
        return;
      }
      Path p = packageTempPath;
      packageTempPath = null;
      try {
        Files.deleteIfExists(p);
      }
      catch (IOException e) {
        logger.warn("删除临时 package 失败: {}", p, e);
      }
    }
  }
}
