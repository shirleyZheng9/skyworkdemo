package com.iwhalecloud.bote.service.skill.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.cache.SkillSquareBulkExportCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.consts.SkillSquareConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.common.util.SkillCodeUtils;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.base.SyncFileInfoRequest;
import com.iwhalecloud.bote.dto.portal.SimpleUserDTO;
import com.iwhalecloud.bote.dto.skill.AdminImportRequest;
import com.iwhalecloud.bote.dto.skill.AgentSkillDTO;
import com.iwhalecloud.bote.dto.skill.ImportAllResponse;
import com.iwhalecloud.bote.dto.skill.InstallRequest;
import com.iwhalecloud.bote.dto.skill.InstallResponse;
import com.iwhalecloud.bote.dto.skill.SkillInstallLogVO;
import com.iwhalecloud.bote.dto.skill.SkillSquareAdminDetailVO;
import com.iwhalecloud.bote.dto.skill.SkillSquareAdminItemVO;
import com.iwhalecloud.bote.dto.skill.SkillSquareBulkExportJobStatusVO;
import com.iwhalecloud.bote.dto.skill.SkillSquareDetailVO;
import com.iwhalecloud.bote.dto.skill.SkillSquareItemVO;
import com.iwhalecloud.bote.dto.skill.SkillTypeStatisticsVO;
import com.iwhalecloud.bote.dto.skill.query.SkillInstallLogQueryParams;
import com.iwhalecloud.bote.dto.skill.query.SkillSquareAdminQueryParams;
import com.iwhalecloud.bote.dto.skill.query.SkillSquareQueryParams;
import com.iwhalecloud.bote.entity.skill.AgentSkillInstallLogEntity;
import com.iwhalecloud.bote.entity.skill.AgentSkillSquareEntity;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import com.iwhalecloud.bote.mapper.skill.AgentSkillInstallLogMapper;
import com.iwhalecloud.bote.mapper.skill.AgentSkillMapper;
import com.iwhalecloud.bote.mapper.skill.AgentSkillSquareMapper;
import com.iwhalecloud.bote.service.agent.IAiSkillManageService;
import com.iwhalecloud.bote.service.base.IFileInfoManageService;
import com.iwhalecloud.bote.service.skill.IAgentSkillManageService;
import com.iwhalecloud.bote.service.skill.ISkillSquareImportService;
import com.iwhalecloud.bote.service.skill.ISkillSquareService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

/**
 * SKILL广场服务实现
 *
 * @author skill-square
 * @since 2026-03-18
 */
@Service
@RequiredArgsConstructor
public class SkillSquareServiceImpl implements ISkillSquareService {
  private static final String INSTALL_SOURCE_SQUARE = "square";
  private static final String INSTALL_SOURCE_DIALOGUE = "dialogue";
  private static final String INSTALL_SOURCE_DEVELOP = "develop";

  private static final String ACTION_ENABLE = "enable";
  private static final String ACTION_DISABLE = "disable";
  private static final String ACTION_DELETE = "delete";

  private final AgentSkillSquareMapper agentSkillSquareMapper;
  private final AgentSkillMapper agentSkillMapper;
  private final AgentSkillInstallLogMapper agentSkillInstallLogMapper;
  private final IFileStoreService fileStoreService;
  private final IFileInfoManageService fileInfoManageService;
  private final IAgentSkillManageService agentSkillManageService;
  private static final DateTimeFormatter LOG_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private final ISkillSquareImportService skillSquareImportService;
  private final IAiSkillManageService aiSkillManageService;
  private final SkillSquareBulkExportCache skillSquareBulkExportCache;
  private final UserManageMapper userManageMapper;

  @Lazy
  @Autowired
  private SkillSquareExportAsyncRunner skillSquareExportAsyncRunner;

  private static PageInfo<SkillSquareItemVO> wrapSkillSquarePageInfo(List<SkillSquareItemVO> list, long total, SkillSquareQueryParams params) {
    int pageNum = params.getPageNum() != null ? params.getPageNum() : 1;
    int pageSize = params.getPageSize() != null ? params.getPageSize() : 20;
    PageInfo<SkillSquareItemVO> pageInfo = new PageInfo<>(list);
    pageInfo.setTotal(total);
    pageInfo.setPageNum(pageNum);
    pageInfo.setPageSize(pageSize);
    pageInfo.setPages(pageSize <= 0 ? 0 : (int) ((total + pageSize - 1) / pageSize));
    return pageInfo;
  }

  private static String normalizeSkillVersion(@Nullable String version) {
    String t = StringUtils.trimToNull(version);
    return t != null ? t : "1.0.0";
  }

  @Override
  public PageInfo<SkillSquareItemVO> queryPage(SkillSquareQueryParams params) {
    if (SkillSquareConsts.QUERY_TYPE_HOT.equals(params.getType())) {
      return queryHotSkillPage(params);
    }
    return querySkillSquarePageDefault(params);
  }

  /**
   * 广场列表常规分页（非热门）
   */
  private PageInfo<SkillSquareItemVO> querySkillSquarePageDefault(SkillSquareQueryParams params) {
    Page<AgentSkillSquareEntity> page = agentSkillSquareMapper.selectPage(
      params.getType(),
      params.getKeyword(),
      BaseConsts.TRUE,
      BaseConsts.STATUS_CD_VALID,
      params.getSpaceId(),
      params.getTenantId(),
      SessionUtil.getLoginInfo().getUserId(),
      params.getInstalled(),
      params.buildRowBounds()
    );
    List<SkillSquareItemVO> list = page.stream().map(e -> {
      SkillSquareItemVO vo = new SkillSquareItemVO();
      toItemVO(e, vo);
      return vo;
    }).collect(Collectors.toList());
    // 如果技能是“用户贡献”类型则补充技能拥有者用户名称
    if (CollectionUtils.isNotEmpty(page.getResult()) && "userHub".equals(params.getType())) {
      List<Long> userIdList = page.getResult().stream().map(AgentSkillSquareEntity::getOwnerUserId).toList();
      List<SimpleUserDTO> userDTOList = userManageMapper.getSimpleUserList(userIdList);
      Map<Long, String> userIdNameMap = new HashMap<>();
      userDTOList.forEach(u -> userIdNameMap.put(u.getUserId(), u.getRealName()));
      list.forEach(vo -> vo.setOwnerUserName(userIdNameMap.get(vo.getOwnerUserId())));
    }
    return wrapSkillSquarePageInfo(list, page.getTotal(), params);
  }

  @Override
  public SkillSquareDetailVO getDetail(Long skillId) {
    AgentSkillSquareEntity entity = agentSkillSquareMapper.selectById(skillId);
    if (entity == null || !BaseConsts.TRUE.equals(entity.getOnlineStatus())
      || !BaseConsts.STATUS_CD_VALID.equals(entity.getStatusCd())) {
      return null;
    }
    return toDetailVO(entity);
  }

  /**
   * 构建技能包的 saveName，同一 skillCode+version 存储同一文件
   */
  private static String buildPackageSaveName(String skillCode, @Nullable String version) {
    String safeCode = SkillCodeUtils.toSafeId(skillCode);
    String safeVer = SkillCodeUtils.toSafeId(version != null ? version : "1.0.0");
    return safeCode + "_v" + safeVer + ".zip";
  }


  @Override
  @Transactional
  public ResultVO<InstallResponse> installBySkillCode(String skillCode, Long tenantId, Long botId, String installSource) {
    if (StringUtils.isBlank(skillCode)) {
      return ResultVO.fail("技能编码不能为空");
    }
    AgentSkillSquareEntity square = agentSkillSquareMapper.selectByCode(skillCode.trim());
    if (square == null || !BaseConsts.TRUE.equals(square.getOnlineStatus())
      || !BaseConsts.STATUS_CD_VALID.equals(square.getStatusCd())) {
      return ResultVO.fail("技能不存在或已下架");
    }
    InstallRequest req = new InstallRequest();
    req.setSkillId(square.getSkillId());
    req.setBotId(botId);
    req.setTenantId(tenantId);
    req.setInstallSource(StringUtils.isNotBlank(installSource) ? installSource : INSTALL_SOURCE_DIALOGUE);
    return install(req);
  }

  /**
   * 热门：按安装数排序，总量与扫描范围最多 {@link SkillSquareConsts#HOT_SKILL_QUERY_MAX} 条
   */
  private PageInfo<SkillSquareItemVO> queryHotSkillPage(SkillSquareQueryParams params) {
    int pageNum = params.getPageNum() != null ? params.getPageNum() : 1;
    int pageSize = params.getPageSize() != null ? params.getPageSize() : 20;
    int offset = (pageNum - 1) * pageSize;

    if (offset >= SkillSquareConsts.HOT_SKILL_QUERY_MAX) {
      long rawTotal = agentSkillSquareMapper.countPage(
        SkillSquareConsts.QUERY_TYPE_HOT,
        params.getKeyword(),
        BaseConsts.TRUE,
        BaseConsts.STATUS_CD_VALID
      );
      return wrapSkillSquarePageInfo(
        Collections.emptyList(),
        Math.min(rawTotal, SkillSquareConsts.HOT_SKILL_QUERY_MAX),
        params
      );
    }

    int limit = Math.min(pageSize, SkillSquareConsts.HOT_SKILL_QUERY_MAX - offset);
    Page<AgentSkillSquareEntity> page = agentSkillSquareMapper.selectPage(
      SkillSquareConsts.QUERY_TYPE_HOT,
      params.getKeyword(),
      BaseConsts.TRUE,
      BaseConsts.STATUS_CD_VALID,
      params.getSpaceId(),
      params.getTenantId(),
      SessionUtil.getLoginInfo().getUserId(),
      null,
      new RowBounds(offset, limit)
    );
    List<SkillSquareItemVO> list = page.stream().map(e -> {
      SkillSquareItemVO vo = new SkillSquareItemVO();
      toItemVO(e, vo);
      return vo;
    }).collect(Collectors.toList());
    long total = Math.min(page.getTotal(), SkillSquareConsts.HOT_SKILL_QUERY_MAX);
    return wrapSkillSquarePageInfo(list, total, params);
  }

  @Override
  @Transactional
  public ResultVO<InstallResponse> install(InstallRequest request) {
    Long tenantId = request.getTenantId();
    Long userId = SessionUtil.getLoginInfo().getUserId();
    Long skillId = request.getSkillId();
    Long botId = request.getBotId();
    String installSource = request.getInstallSource();
    if (installSource == null || installSource.isEmpty()) {
      installSource = INSTALL_SOURCE_SQUARE;
    }

    // 1. 校验广场技能存在且上架
    AgentSkillSquareEntity square = agentSkillSquareMapper.selectById(skillId);
    if (square == null || !BaseConsts.TRUE.equals(square.getOnlineStatus())
      || !BaseConsts.STATUS_CD_VALID.equals(square.getStatusCd())) {
      return ResultVO.fail("技能不存在或已下架");
    }

    // 2. AI 门户下安装，同一 空间 + 广场技能 + 用户 仅一条实例
    // 3. 开发中心安装，同个租户下，仅一条实例
    // skill_code 由广场唯一确定，按 skill_version 判断是否已装当前版本或可升级
    AgentSkillDTO existing = agentSkillMapper.selectByTenantBotSkillSquareId(tenantId, skillId, userId, installSource);
    String squareVer = normalizeSkillVersion(square.getVersion());
    if (existing != null) {
      if (normalizeSkillVersion(existing.getSkillVersion()).equals(squareVer)) {
        return ResultVO.fail("您已在该空间下已安装此技能版本，请勿重复安装");
      }
      alignTenantSkillPackageWithSquare(existing, square, tenantId, userId);
      AgentSkillDTO up = new AgentSkillDTO();
      up.setSkillId(existing.getSkillId());
      up.setTenantId(tenantId);
      up.setSkillVersion(square.getVersion());
      up.setSkillName(square.getSkillName());
      up.putFieldUpdateFlag("skillVersion", true);
      up.putFieldUpdateFlag("skillName", true);
      up.setUpdatorId(userId);
      agentSkillMapper.updateAgentSkill(up);
      recordInstallLog(tenantId, userId, botId, skillId, square.getSkillName(), installSource);
      agentSkillSquareMapper.incrementInstallCount(skillId);
      InstallResponse upgraded = new InstallResponse();
      upgraded.setAgentSkillId(existing.getSkillId());
      upgraded.setSkillName(square.getSkillName());
      return ResultVO.success(upgraded);
    }

    Long newSkillId = saveSkillInstance(square, tenantId, botId, skillId, userId, installSource);
    recordInstallLog(tenantId, userId, botId, skillId, square.getSkillName(), installSource);
    agentSkillSquareMapper.incrementInstallCount(skillId);

    InstallResponse response = new InstallResponse();
    response.setAgentSkillId(newSkillId);
    response.setSkillName(square.getSkillName());
    return ResultVO.success(response);
  }

  private void alignTenantSkillPackageWithSquare(AgentSkillDTO existing, AgentSkillSquareEntity square, Long tenantId, Long userId) {
    Assert.notNull(square.getFileInfoId(), "技能包未上传");
    FileInfoDTO platformFi = fileInfoManageService.findFileInfo(BaseConsts.PLATFORM_TENANT_ID, square.getFileInfoId());
    Assert.notNull(platformFi, "技能包文件信息不存在");
    FileInfoDTO tenantFi = fileInfoManageService.findFileInfo(tenantId, existing.getFileInfoId());
    if (tenantFi == null) {
      // 历史数据：file_info_id 曾指向平台租户下的广场包记录
      FileInfoDTO legacy = fileInfoManageService.findFileInfo(BaseConsts.PLATFORM_TENANT_ID, existing.getFileInfoId());
      if (legacy != null) {
        FileInfoDTO newFileInfo = fileInfoManageService.createTenantRefFileInfo(tenantId, legacy.getFileId(), legacy.getFileName(),
          BaseConsts.FILE_BUSI_TYPE_AGENT_SKILL);
        Long newFileInfoId = newFileInfo.getFileInfoId();
        AgentSkillDTO relink = new AgentSkillDTO();
        relink.setSkillId(existing.getSkillId());
        relink.setTenantId(tenantId);
        relink.setFileInfoId(newFileInfoId);
        relink.putFieldUpdateFlag("fileInfoId", true);
        relink.setUpdatorId(userId);
        agentSkillMapper.updateAgentSkill(relink);
        existing.setFileInfoId(newFileInfoId);
        tenantFi = fileInfoManageService.findFileInfo(tenantId, newFileInfoId);
      }
    }
    Assert.notNull(tenantFi, "租户技能包记录不存在");
    if (!Objects.equals(tenantFi.getFileId(), platformFi.getFileId())
      || !Objects.equals(tenantFi.getFileName(), platformFi.getFileName())) {
      fileInfoManageService.syncFileInfoById(tenantId, tenantFi.getFileInfoId(), platformFi.getFileId(), platformFi.getFileName());
    }
  }

  private Long createTenantRefPackageFileInfo(Long tenantId, AgentSkillSquareEntity square) {
    Assert.notNull(square.getFileInfoId(), "技能包未上传");
    Long ownerTenantId = square.getOwnerTenantId() == null ? BaseConsts.PLATFORM_TENANT_ID : square.getOwnerTenantId();
    FileInfoDTO platformFi = fileInfoManageService.findFileInfo(ownerTenantId, square.getFileInfoId());
    Assert.notNull(platformFi, "技能包文件信息不存在");
    Assert.notNull(platformFi.getFileId(), "技能包文件ID不存在");
    byte[] bytes = fileStoreService.downloadFile(platformFi.getFileId());
    Assert.isTrue(bytes != null && bytes.length > 0, "技能包文件下载失败或为空");
    Path tempPath = null;
    try {
      // 与 Agent Skill 上传一致：走校验 + agent-skill 存储；临时文件须以 .zip 命名
      tempPath = Files.createTempFile("square-install-skill-", ".zip");
      Files.write(tempPath, bytes);
      ResultVO<FileInfoVO> uploadResult = agentSkillManageService.uploadAgentSkillFile(tempPath.toFile(), platformFi.getFileName());
      if (!uploadResult.isSuccess()) {
        throw new BssException(StringUtils.defaultIfBlank(uploadResult.getResultMsg(), "技能包上传失败"));
      }
      FileInfoVO uploaded = uploadResult.getResultObject();
      Assert.notNull(uploaded, "技能包上传结果为空");
      return fileInfoManageService.createTenantRefFileInfo(tenantId, uploaded.getFileId(), uploaded.getFileName(),
        BaseConsts.FILE_BUSI_TYPE_AGENT_SKILL).getFileInfoId();
    }
    catch (IOException e) {
      throw new BssException("技能包处理失败: " + e.getMessage(), e);
    }
    finally {
      if (tempPath != null) {
        FileUtils.deleteQuietly(tempPath.toFile());
      }
    }
  }

  private void recordInstallLog(Long tenantId, Long userId, Long botId, Long squareSkillId, String skillName, String installSource) {
    AgentSkillInstallLogEntity logEntity = new AgentSkillInstallLogEntity();
    logEntity.setId(IDUtils.nextId());
    logEntity.setTenantId(tenantId);
    logEntity.setUserId(userId);
    logEntity.setBotId(botId);
    logEntity.setSkillId(squareSkillId);
    logEntity.setSkillName(skillName);
    logEntity.setInstallSource(installSource);
    agentSkillInstallLogMapper.insert(logEntity);
  }

  private Long saveSkillInstance(AgentSkillSquareEntity square, Long tenantId, Long botId, Long skillId, Long userId, String installSource) {
    Long tenantFileInfoId = createTenantRefPackageFileInfo(tenantId, square);

    AgentSkillDTO dto = new AgentSkillDTO();
    dto.setTenantId(tenantId);
    dto.setBotId(botId);
    dto.setSkillName(square.getSkillName());
    dto.setCatalogItemId(-1L);
    dto.setFileInfoId(tenantFileInfoId);
    dto.setSkillVersion(square.getVersion());
    dto.setDataFrom(INSTALL_SOURCE_DEVELOP.equals(installSource) ? "" : KnowledgeConsts.AI_SKILL_DATA_FROM_10A);
    dto.setSkillSquareId(skillId);
    dto.setCreatorId(userId);
    dto.setUpdatorId(userId);
    ResultVO<AgentSkillDTO> agentSkillDTOResultVO = aiSkillManageService.saveAiAgentSkill(dto);
    if (!agentSkillDTOResultVO.isSuccess()) {
      if (BaseErrorConstant.CHECK_NAME.getErrorConstant().getCode().equals(agentSkillDTOResultVO.getResultCode())) {
        throw new BssException("用户名下已存在同名【" + dto.getSkillName() + "】技能！");
      }
      throw new BssException(agentSkillDTOResultVO.getResultMsg());
    }
    return agentSkillDTOResultVO.getResultObject().getSkillId();
  }

  @Override
  @Transactional
  public ResultVO<Long> adminImport(AdminImportRequest request) {
    return skillSquareImportService.importOne(request);
  }

  @Nullable
  private ResultVO<Void> updateSkillPackage(MultipartFile packageFile, AgentSkillSquareEntity entity, AgentSkillSquareEntity update) {
    Assert.isTrue(packageFile.getOriginalFilename() != null
        && SkillSquareConsts.hasAllowedArchiveExtension(packageFile.getOriginalFilename()),
      "package " + SkillSquareConsts.getArchiveExtensionHint());
    ResultVO<FileInfoVO> uploadResult = uploadPackageForSquare(entity.getSkillCode(), entity.getVersion(), packageFile);
    if (!uploadResult.isSuccess()) {
      return ResultVO.fail(uploadResult.getResultMsg());
    }
    FileInfoVO newFileInfo = uploadResult.getResultObject();
    Long oldFileInfoId = entity.getFileInfoId();
    if (oldFileInfoId != null) {
      // 更新已有 fileInfo 记录的 fileId，保持 fileInfoId 不变（避免破坏其他引用）
      fileInfoManageService.syncFileInfoById(
        BaseConsts.PLATFORM_TENANT_ID, oldFileInfoId,
        newFileInfo.getFileId(), newFileInfo.getFileName());
    }
    else {
      // 旧记录不存在时才创建新 fileInfo 并更新关联
      Long newFileInfoId = createFileInfoForSquare(newFileInfo);
      update.setFileInfoId(newFileInfoId);
    }
    return null;
  }

  /**
   * 校验导出 top 参数：{@code null} 表示不限制条数；否则须为 [1, {@link SkillSquareConsts#EXPORT_TOP_MAX}]
   */
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

  private ResultVO<FileInfoVO> uploadPackageForSquare(String skillCode, String version, MultipartFile file) {
    String originalFilename = StringUtils.trimToNull(file.getOriginalFilename());
    Assert.notNull(originalFilename, "文件名称不能为空");
    Assert.isTrue(SkillSquareConsts.hasAllowedArchiveExtension(originalFilename), SkillSquareConsts.getArchiveExtensionHint());
    Assert.isTrue(file.getSize() > 0, "文件不能为空");
    UploadConfigVO config = new UploadConfigVO();
    config.setOriginalFileName(originalFilename);
    config.setSubFolder(SkillSquareConsts.UPLOAD_SUBFOLDER_SQUARE);
    config.setSaveName(buildPackageSaveName(skillCode, version));
    config.setFileSize(file.getSize());
    config.setFileType("zip");
    config.setIsPicture(false);
    try {
      FileInfoVO fileInfo = fileStoreService.uploadFile(file, config);
      return ResultVO.success(fileInfo);
    }
    catch (Exception e) {
      return ResultVO.fail("上传 " + originalFilename + " 失败: " + e.getMessage());
    }
  }

  private Long createFileInfoForSquare(FileInfoVO fileInfo) {
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
    return dto.getFileInfoId();
  }

  @Override
  @Transactional
  public ResultVO<Void> adminUpdateStatus(Long skillId, String action) {
    AgentSkillSquareEntity entity = agentSkillSquareMapper.selectById(skillId);
    if (entity == null) {
      return ResultVO.fail("技能不存在");
    }
    switch (action) {
      case ACTION_ENABLE:
        agentSkillSquareMapper.updateStatus(skillId, BaseConsts.TRUE);
        break;
      case ACTION_DISABLE:
        agentSkillSquareMapper.updateStatus(skillId, BaseConsts.FALSE);
        break;
      case ACTION_DELETE:
        agentSkillSquareMapper.updateStatusCd(skillId, BaseConsts.STATUS_CD_INVALID);
        break;
      default:
        return ResultVO.fail("不支持的 action: " + action + "，应为 enable/disable/delete");
    }
    return ResultVO.success();
  }

  @Override
  public PageInfo<SkillSquareAdminItemVO> adminQueryPage(SkillSquareAdminQueryParams params) {
    RowBounds rowBounds = params.buildRowBounds();
    Page<AgentSkillSquareEntity> page = agentSkillSquareMapper.selectAdminPage(
      params.getKeyword(),
      params.getOnlineStatus(),
      params.getSource(),
      params.getSkillType(),
      BaseConsts.STATUS_CD_VALID,
      rowBounds
    );
    List<SkillSquareAdminItemVO> list = page.stream().map(e -> {
      SkillSquareAdminItemVO vo = new SkillSquareAdminItemVO();
      toItemVO(e, vo);
      vo.setOnlineStatus(e.getOnlineStatus());
      return vo;
    }).collect(Collectors.toList());
    PageInfo<SkillSquareAdminItemVO> pageInfo = new PageInfo<>(list);
    pageInfo.setTotal(page.getTotal());
    pageInfo.setPageNum(params.getPageNum() != null ? params.getPageNum() : 1);
    pageInfo.setPageSize(params.getPageSize() != null ? params.getPageSize() : 20);
    pageInfo.setPages((int) ((page.getTotal() + pageInfo.getPageSize() - 1) / pageInfo.getPageSize()));
    return pageInfo;
  }

  @Override
  @Transactional
  public ResultVO<Void> adminUpdate(Long skillId, String skillName,
                                    @Nullable String skillDesc,
                                    @Nullable MultipartFile packageFile) {
    AgentSkillSquareEntity entity = agentSkillSquareMapper.selectById(skillId);
    if (entity == null || BaseConsts.STATUS_CD_INVALID.equals(entity.getStatusCd())) {
      return ResultVO.fail("技能不存在或已删除");
    }

    AgentSkillSquareEntity update = new AgentSkillSquareEntity();
    update.setSkillId(skillId);
    if (StringUtils.isNotBlank(skillName)) {
      update.setSkillName(skillName);
    }
    if (skillDesc != null) {
      update.setSkillDesc(skillDesc);
    }

    if (packageFile != null && !packageFile.isEmpty()) {
      ResultVO<Void> uploadResult = updateSkillPackage(packageFile, entity, update);
      if (uploadResult != null) {
        return uploadResult;
      }
    }

    agentSkillSquareMapper.updateById(update);
    return ResultVO.success();
  }

  @Override
  public ResultVO<Long> startBulkExportAsync(Integer top) {
    Long userId = SessionUtil.getOptionalUserId();
    if (userId == null) {
      return ResultVO.fail("请先登录");
    }
    try {
      assertExportTopValid(top);
    }
    catch (BssException e) {
      return ResultVO.fail(e.getFailMsg());
    }
    SkillSquareBulkExportJobStatusVO currentJob = skillSquareBulkExportCache.getForUserCurrentJob(userId);
    if (currentJob != null && currentJob.getPhase() != null) {
      SkillSquareBulkExportJobStatusVO.Phase ph = currentJob.getPhase();
      if (ph == SkillSquareBulkExportJobStatusVO.Phase.PENDING
        || ph == SkillSquareBulkExportJobStatusVO.Phase.RUNNING) {
        return ResultVO.fail("您已有进行中的导出任务，请等待完成后再发起");
      }
    }
    int total = (int) agentSkillSquareMapper.countForExport(top);
    if (total <= 0) {
      return ResultVO.fail("没有可导出的上架技能");
    }
    int chunk = SkillSquareConsts.EXPORT_PACKAGE_MAX_RECORDS;
    int totalParts = (total + chunk - 1) / chunk;
    long jobId = IDUtils.nextId();
    skillSquareBulkExportCache.putInitial(jobId, userId, total, totalParts);
    ThreadPools.getCommon().submit(() -> {
      skillSquareExportAsyncRunner.run(jobId, top);
    });
    return ResultVO.success(jobId);
  }

  @Override
  public ResultVO<SkillSquareBulkExportJobStatusVO> getBulkExportJobStatus(Long jobId) {
    Long userId = SessionUtil.getOptionalUserId();
    if (userId == null) {
      return ResultVO.fail("请先登录");
    }
    SkillSquareBulkExportJobStatusVO vo = skillSquareBulkExportCache.getForUser(jobId, userId);
    if (vo == null) {
      return ResultVO.fail("任务不存在或无权查看");
    }
    return ResultVO.success(vo);
  }

  @Override
  public ResultVO<ImportAllResponse> importAll(MultipartFile zipFile) {
    return skillSquareImportService.importAll(zipFile);
  }

  @Override
  public ResultVO<SkillSquareBulkExportJobStatusVO> getBulkExportCurrentJobStatus() {
    Long userId = SessionUtil.getOptionalUserId();
    if (userId == null) {
      return ResultVO.fail("请先登录");
    }
    SkillSquareBulkExportJobStatusVO vo = skillSquareBulkExportCache.getForUserCurrentJob(userId);
    return ResultVO.success(vo);
  }

  @Override
  public ResultVO<Void> clearBulkExportCurrentJob() {
    Long userId = SessionUtil.getOptionalUserId();
    if (userId == null) {
      return ResultVO.fail("请先登录");
    }
    skillSquareBulkExportCache.clearCurrentUserJob(userId);
    return ResultVO.success();
  }

  @Override
  public SkillSquareAdminDetailVO adminGetDetail(Long skillId) {
    AgentSkillSquareEntity entity = agentSkillSquareMapper.selectById(skillId);
    if (entity == null || !BaseConsts.STATUS_CD_VALID.equals(entity.getStatusCd())) {
      return null;
    }
    SkillSquareAdminDetailVO vo = new SkillSquareAdminDetailVO();
    vo.setSkillId(entity.getSkillId());
    vo.setSkillCode(entity.getSkillCode());
    vo.setSkillName(entity.getSkillName());
    vo.setSkillDesc(entity.getSkillDesc());
    vo.setSkillType(entity.getSkillType());
    vo.setTags(entity.getTags());
    vo.setVersion(entity.getVersion());
    vo.setInstallCount(entity.getInstallCount() != null ? entity.getInstallCount() : 0);
    vo.setReadmeContent(entity.getSkillContent());
    vo.setSource(entity.getSource());
    vo.setOnlineStatus(entity.getOnlineStatus());
    return vo;
  }

  @Override
  public PageInfo<SkillInstallLogVO> adminQueryInstallLogs(SkillInstallLogQueryParams params) {
    RowBounds rowBounds = params.buildRowBounds();
    LocalDateTime startTime = StringUtils.isBlank(params.getStartTime()) ? null
      : LocalDateTime.parse(params.getStartTime(), LOG_TIME_FMT);
    LocalDateTime endTime = StringUtils.isBlank(params.getEndTime()) ? null
      : LocalDateTime.parse(params.getEndTime(), LOG_TIME_FMT);
    Page<SkillInstallLogVO> page = agentSkillInstallLogMapper.selectInstallLogPage(
      params.getSkillId(),
      params.getKeyword(),
      params.getTenantId(),
      params.getInstallSource(),
      startTime,
      endTime,
      rowBounds
    );
    PageInfo<SkillInstallLogVO> pageInfo = new PageInfo<>(page);
    pageInfo.setTotal(page.getTotal());
    pageInfo.setPageNum(params.getPageNum() != null ? params.getPageNum() : 1);
    pageInfo.setPageSize(params.getPageSize() != null ? params.getPageSize() : 20);
    pageInfo.setPages((int) ((page.getTotal() + pageInfo.getPageSize() - 1) / pageInfo.getPageSize()));
    return pageInfo;
  }

  private void toItemVO(AgentSkillSquareEntity squareEntity, SkillSquareItemVO vo) {
    vo.setSkillId(squareEntity.getSkillId());
    vo.setSkillCode(squareEntity.getSkillCode());
    vo.setSkillName(squareEntity.getSkillName());
    vo.setSkillDesc(squareEntity.getSkillDesc());
    vo.setSkillType(squareEntity.getSkillType());
    vo.setTags(squareEntity.getTags());
    vo.setVersion(squareEntity.getVersion());
    vo.setInstallCount(squareEntity.getInstallCount() != null ? squareEntity.getInstallCount() : 0);
    vo.setSource(squareEntity.getSource());
    vo.setUpdatedTime(squareEntity.getUpdatedTime() == null ? squareEntity.getCreatedTime() : squareEntity.getUpdatedTime());
    vo.setOwnerUserId(squareEntity.getOwnerUserId());
    vo.setInstalled(squareEntity.getInstalled());
    vo.setAgentSkillId(squareEntity.getAgentSkillId());
  }

  private SkillSquareDetailVO toDetailVO(AgentSkillSquareEntity e) {
    SkillSquareDetailVO vo = new SkillSquareDetailVO();
    vo.setSkillId(e.getSkillId());
    vo.setSkillCode(e.getSkillCode());
    vo.setSkillName(e.getSkillName());
    vo.setSkillDesc(e.getSkillDesc());
    vo.setSkillType(e.getSkillType());
    vo.setTags(e.getTags());
    vo.setVersion(e.getVersion());
    vo.setInstallCount(e.getInstallCount() != null ? e.getInstallCount() : 0);
    vo.setReadmeContent(e.getSkillContent());
    vo.setSource(e.getSource());
    return vo;
  }

  @Override
  public Map<String, Integer> getSkillTypeStatistics(Long spaceId, @Nullable Long tenantId) {
    Map<String, Integer> result = new HashMap<>();
    Integer all = 0;
    // 统计广场上架技能类型数量
    List<SkillTypeStatisticsVO> skillTypeStatistics = agentSkillSquareMapper.countSkillByType();
    for (SkillTypeStatisticsVO statistic : skillTypeStatistics) {
      all += statistic.getCount();
      result.put(statistic.getSkillType(), statistic.getCount());
    }
    result.put("all", all);
    // 统计热门技能数量
    long hotTotal = agentSkillSquareMapper.countPage(SkillSquareConsts.QUERY_TYPE_HOT, null, BaseConsts.TRUE, BaseConsts.STATUS_CD_VALID);
    result.put("hot", (int) Math.min(hotTotal, SkillSquareConsts.HOT_SKILL_QUERY_MAX));
    // 统计我的技能统计数量
    Long userId = SessionUtil.getLoginInfo().getUserId();
    int createdSkillCount = agentSkillMapper.countUserSpaceCreatedSkills(spaceId, tenantId, userId);
    int installSkillCount = agentSkillMapper.countUserSpaceInstalledSkills(spaceId, tenantId, userId);
    result.put("mySelf", createdSkillCount + installSkillCount);
    result.put("created", createdSkillCount);
    result.put("installed", installSkillCount);
    // 补充可能会缺失的技能类型数量
    result.putIfAbsent("platform", 0);
    result.putIfAbsent("userHub", 0);
    result.putIfAbsent("skillHub", 0);
    if (tenantId != null) {
      // 开发中心，统计平台预置的 skill 数量
      result.put("platform", agentSkillMapper.countPlatformSkills());
    }
    return result;
  }

}
