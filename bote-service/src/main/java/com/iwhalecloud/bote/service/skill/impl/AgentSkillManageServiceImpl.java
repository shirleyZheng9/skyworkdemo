package com.iwhalecloud.bote.service.skill.impl;

import static com.iwhalecloud.bote.service.skill.support.AgentSkillZipSupport.SKILL_NAME_PATTERN;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.agent.skill.AgentSkillMarkdownParser;
import com.iwhalecloud.bote.agent.skill.AgentSkillMarkdownParser.SkillMdParseResult;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.consts.SkillSquareConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.FreemarkerUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.base.query.SkillGenerateParams;
import com.iwhalecloud.bote.dto.bot.SquareBotSkillRowDTO;
import com.iwhalecloud.bote.dto.ontology.OntoSceneDTO;
import com.iwhalecloud.bote.dto.ontology.OntologyActionDTO;
import com.iwhalecloud.bote.dto.ontology.OntologyAppDTO;
import com.iwhalecloud.bote.dto.ontology.OntologyObjectDTO;
import com.iwhalecloud.bote.dto.ontology.OntologyRuleDTO;
import com.iwhalecloud.bote.dto.ontology.query.OntologyQueryParams;
import com.iwhalecloud.bote.dto.skill.AgentSkillDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillDirDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillFileDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillPublishResultDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillTreeNodeDTO;
import com.iwhalecloud.bote.dto.skill.query.AgentSkillQueryParams;
import com.iwhalecloud.bote.mapper.base.FileInfoManageMapper;
import com.iwhalecloud.bote.mapper.skill.AgentSkillDirMapper;
import com.iwhalecloud.bote.mapper.skill.AgentSkillFileMapper;
import com.iwhalecloud.bote.mapper.skill.AgentSkillMapper;
import com.iwhalecloud.bote.service.base.IFileInfoManageService;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bote.service.skill.IAgentSkillManageService;
import com.iwhalecloud.bote.service.skill.impl.helper.AgentSkillTextFileHelper;
import com.iwhalecloud.bote.service.skill.impl.helper.OntologyApiHelper;
import com.iwhalecloud.bote.service.skill.support.AgentSkillNameSyncSupport;
import com.iwhalecloud.bote.service.skill.support.AgentSkillOrchestrationSupport;
import com.iwhalecloud.bote.service.skill.security.AgentSkillSecurityScanner;
import com.iwhalecloud.bote.service.skill.support.AgentSkillZipSupport;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

/**
 * Agent Skill 管理服务
 *
 * @author bianjp
 * @since 2026-02-03
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class AgentSkillManageServiceImpl implements IAgentSkillManageService {
  private static final Logger logger = LoggerFactory.getLogger(AgentSkillManageServiceImpl.class);

  /** 模板 zip 类路径 */
  public static final String TEMPLATE_ZIP_CLASSPATH = "META-INF/resources/bote/files/zip/agentSkillTemplate.zip";

  private final AgentSkillMapper agentSkillMapper;
  private final AgentSkillDirMapper agentSkillDirMapper;
  private final AgentSkillFileMapper agentSkillFileMapper;
  private final AgentSkillOrchestrationSupport agentSkillOrchestrationSupport;
  private final AgentSkillNameSyncSupport agentSkillNameSyncSupport;
  private final IResourceElementService resourceElementService;
  private final IFileInfoManageService fileInfoManageService;
  private final FileInfoManageMapper fileInfoManageMapper;
  private final IFileStoreService fileStoreService;
  private final OntologyApiHelper ontologyApiHelper;
  private final AgentSkillSecurityScanner agentSkillSecurityScanner;

  @Override
  public AgentSkillDTO findAgentSkill(Long tenantId, Long skillId) {
    AgentSkillDTO skill = agentSkillMapper.selectAgentSkillById(tenantId, skillId);
    Assert.notNull(skill, () -> "Agent Skill 不存在: " + skillId);
    if (StringUtils.isEmpty(skill.getSkillFileName()) && StringUtils.isEmpty(skill.getDataFrom())) {
      agentSkillNameSyncSupport.fillSkillFileNameOnly(skill);
    }
    return skill;
  }

  @Override
  @Transactional
  public ResultVO<AgentSkillDTO> saveAgentSkill(AgentSkillDTO skill) {
    Assert.notNull(skill.getTenantId(), "租户 ID 不能为空");
    Assert.hasLength(skill.getSkillName(), "技能名称不能为空");

    boolean isDeveloper = StringUtils.isEmpty(skill.getPlatform()) || BaseConsts.PLATFORM_DEVELOPER.equals(skill.getPlatform());
    if (isDeveloper) {
      Assert.isTrue(SKILL_NAME_PATTERN.matcher(skill.getSkillFileName()).matches(),
        "agentSkill编码不合法,必须在 1 到 64 个字符之间,只能包含小写字母、数字、连字符(-),且不能以连字符开头");
    }

    AgentSkillDTO old = skill.getSkillId() != null ? findAgentSkill(skill.getTenantId(), skill.getSkillId()) : null;
    Long loginUserId = SessionUtil.getLoginInfo().getUserId();

    // 校验技能名称唯一性
    if ((old == null || !Objects.equals(old.getSkillName(), skill.getSkillName()))
      && agentSkillMapper.existsSkillName(skill.getTenantId(), skill.getBotId(), skill.getSkillName(), skill.getDataFrom(), loginUserId)) {
      return BaseErrorConstant.CHECK_NAME.toResult(skill.getSkillName());
    }

    // 保存文件信息并准备数据
    Long oldFileInfoId = saveFileInfo(skill, old);
    prepareSkillData(skill, old);

    DataDifference<AgentSkillDTO> difference = DataDifferenceStarter.computeSaveAndLog(old, skill, false, skill.getTenantId(), OperClassEnum.AGENT_SKILL);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    AgentSkillDTO saved = difference.getToSaveData();
    // 删除旧的文件
    if (oldFileInfoId != null) {
      fileInfoManageService.deleteFileInfo(skill.getTenantId(), oldFileInfoId);
      // 如果是更新且上传了新文件，需要先删除旧的目录和文件数据
      if (isDeveloper) {
        agentSkillOrchestrationSupport.deleteSkillFileData(saved.getTenantId(), saved.getSkillId(), SessionUtil.getOptionalUserId());
      }
    }
    // 初始化技能
    initializeSkill(saved, isDeveloper, loginUserId);
    return ResultVO.success(saved);
  }

  /**
   * 准备技能数据
   */
  private void prepareSkillData(AgentSkillDTO skill, @Nullable AgentSkillDTO old) {
    skill.setStatusCd(BaseConsts.STATUS_CD_VALID);
    if (old != null) {
      skill.setCreatorId(old.getCreatorId());
      skill.setCreatedTime(old.getCreatedTime());
      updateRootDirName(skill, old);
    }
    else {
      skill.setSkillCode(IDUtils.nextId() + "");
    }
  }

  /**
   * 初始化技能（开发中心或本体场景）
   */
  private void initializeSkill(AgentSkillDTO saved, boolean isDeveloper, Long loginUserId) {
    // 如果不是本体场景技能且是开发中心，则初始化AgentSkill
    if (!BaseConsts.SKILL_TEMPLATE_TYPE_ONTOLOGY_SCENE.equals(saved.getSkillTemplateType()) && isDeveloper) {
      checkAndInitAgentSkill(saved.getTenantId(), saved.getSkillId());
    }
    // 本体场景技能：保存后初始化根目录与 SKILL.md
    initOntologySceneSkill(saved, loginUserId);
  }

  /**
   * 更新根目录名称
   */
  private void updateRootDirName(AgentSkillDTO skill, AgentSkillDTO old) {
    if (!Objects.equals(old.getSkillFileName(), skill.getSkillFileName())) {
      // 更新根目录名称
      AgentSkillDirDTO rootDir = agentSkillDirMapper.selectRootDirBySkillId(skill.getTenantId(), skill.getSkillId());
      if (rootDir != null) {
        rootDir.setDirName(skill.getSkillFileName());
        rootDir.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
        agentSkillDirMapper.updateAgentSkillDir(rootDir);
      }
    }
  }

  /**
   * 本体场景技能初始化：创建根目录与 SKILL.md
   */
  private void initOntologySceneSkill(AgentSkillDTO agentSkill, Long loginUserId) {
    // 本体场景技能：保存后初始化根目录与 SKILL.md，文件内容来自AI生成。
    if (!BaseConsts.SKILL_TYPE_ONLINE.equals(agentSkill.getSkillType()) || !BaseConsts.SKILL_TEMPLATE_TYPE_ONTOLOGY_SCENE.equals(agentSkill.getSkillTemplateType())) {
      return;
    }
    if (agentSkillDirMapper.existDir(agentSkill.getTenantId(), agentSkill.getSkillId()) > 0) {
      return;
    }
    SkillGenerateParams params = JsonUtil.parseJsonRequired(agentSkill.getSkillExtJson(), SkillGenerateParams.class);
    OntologyQueryParams ontologyQueryParams = new OntologyQueryParams();
    ontologyQueryParams.setTenantId(agentSkill.getTenantId());
    ontologyQueryParams.setAppId(params.getAppId());
    ontologyQueryParams.setSceneId(params.getSceneId());
    OntoSceneDTO ontologySceneDetail = ontologyApiHelper.getOntologySceneDetail(ontologyQueryParams);
    params.setStepContent(ontologySceneDetail.getStepContent());
    params.setSkillName(agentSkill.getSkillFileName());
    String content = processTemplate(params);
    if (StringUtils.isBlank(content)) {
      throw new BssException("生成本体场景 SKILL.md 内容为空");
    }
    // 1. 创建根目录
    AgentSkillDirDTO rootDir = new AgentSkillDirDTO();
    rootDir.setDirId(IDUtils.nextId());
    rootDir.setParentDirId(-1L);
    rootDir.setSkillId(agentSkill.getSkillId());
    rootDir.setTenantId(agentSkill.getTenantId());
    rootDir.setDirName(agentSkill.getSkillFileName());
    rootDir.setStatusCd(BaseConsts.STATUS_CD_VALID);
    rootDir.setCreatorId(loginUserId);
    rootDir.setUpdatorId(loginUserId);
    agentSkillDirMapper.insertAgentSkillDir(rootDir);
    // 2. 创建根目录下 SKILL.md
    AgentSkillFileDTO skillMdFile = new AgentSkillFileDTO();
    skillMdFile.setSkillFileId(IDUtils.nextId());
    skillMdFile.setDirId(rootDir.getDirId());
    skillMdFile.setSkillId(agentSkill.getSkillId());
    skillMdFile.setTenantId(agentSkill.getTenantId());
    skillMdFile.setFileName(SkillSquareConsts.ZIP_FILE_SKILL_MD);
    skillMdFile.setFileType("md");
    skillMdFile.setFileContent(content);
    skillMdFile.setFileInfoId(null);
    skillMdFile.setStatusCd(BaseConsts.STATUS_CD_VALID);
    skillMdFile.setCreatorId(loginUserId);
    skillMdFile.setUpdatorId(loginUserId);
    agentSkillFileMapper.insertAgentSkillFile(skillMdFile);
    agentSkillOrchestrationSupport.refreshSkillTool(agentSkill.getTenantId(), agentSkill.getSkillId());
    autoUploadAgentSkill(agentSkill);
  }


  /**
   * 自动上传技能
   */
  private void autoUploadAgentSkill(AgentSkillDTO skill) {
    Path tmp = null;
    Long skillId = skill.getSkillId();
    try {
      tmp = Files.createTempFile("agent-skill-pub-", ".zip");
      agentSkillOrchestrationSupport.buildPublishZipFile(skill.getTenantId(), skillId, tmp);
      String zipFileName = skill.getSkillFileName() + ".zip";
      // 上传并发布
      uploadAndPublish(skill, skill.getTenantId(), skillId, tmp.toFile(), zipFileName);
    }
    catch (IOException e) {
      logger.error("build publish zip failed skillId={}", skillId, e);
    }
    finally {
      FileUtils.deleteQuietly(tmp != null ? tmp.toFile() : null);
    }
  }

  /**
   * 处理模板
   */
  private String processTemplate(SkillGenerateParams params) {
    String template = SystemParameter.ONTOLOGY_SCENE_TEMPLATE_PROMPT.getValueFromDb();
    Map<String, Object> prompt = new HashMap<>(4);
    prompt.put("name", params.getSkillName());
    prompt.put("appId", params.getAppId());
    prompt.put("sceneId", params.getSceneId());
    prompt.put("sceneName", params.getSceneName());
    prompt.put("stepContent", params.getStepContent());
    return FreemarkerUtil.process(template, prompt);
  }

  /**
   * 保存 bt_file_info，返回需要删除的旧 file_info_id
   */
  @Nullable
  private Long saveFileInfo(AgentSkillDTO skill, @Nullable AgentSkillDTO old) {
    if (skill.getFileId() != null) {
      FileInfoVO fileInfo = fileStoreService.getFileInfoById(skill.getFileId());
      Assert.notNull(fileInfo, () -> "文件不存在: id=" + skill.getFileId());
      Assert.isTrue(BaseConsts.STATUS_CD_VALID.equals(fileInfo.getStatusCd()), () -> "文件已被删除，请重新上传: id=" + skill.getFileId());
      // 文件没有变化，不需要更新
      if (old != null && Objects.equals(old.getFileId(), skill.getFileId())) {
        // 不要求前端传递 file_info_id
        skill.setFileInfoId(old.getFileInfoId());
        return null;
      }
      // 新文件，或者文件有变化
      Long fileInfoId = IDUtils.nextId();
      FileInfoDTO dto = new FileInfoDTO();
      dto.setFileInfoId(fileInfoId);
      dto.setFileId(fileInfo.getFileId());
      dto.setFileName(fileInfo.getFileName());
      dto.setTenantId(skill.getTenantId());
      dto.setBusiType(BaseConsts.FILE_BUSI_TYPE_AGENT_SKILL);
      dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
      dto.setCreatorId(SessionUtil.getLoginInfo().getUserId());
      dto.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
      fileInfoManageMapper.insertFileInfo(dto);
      skill.setFileInfoId(fileInfoId);
    }
    return old != null ? old.getFileInfoId() : null;
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteAgentSkill(Long tenantId, Long skillId) {
    AgentSkillDTO skill = agentSkillMapper.selectAgentSkillById(tenantId, skillId);
    if (skill == null) {
      return ResultVO.fail("Agent Skill 不存在");
    }
    if (CommonConsts.PLATFORM_TENANT_ID.equals(skill.getTenantId())) {
      return ResultVO.fail("平台级Agent Skill不允许删除");
    }
    if (resourceElementService.existsRelatedResource(tenantId, skillId, DataSyncCodeEnum.AGENT_SKILL.getCode())) {
      return ResultVO.fail("页面函数已存在关联配置数据，不允许删除");
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    // 删除技能文件数据
    agentSkillOrchestrationSupport.deleteSkillFileData(tenantId, skillId, userId);
    if (skill.getFileInfoId() != null && skill.getSkillSquareId() == null) {
      fileInfoManageService.deleteFileInfo(tenantId, skill.getFileInfoId());
    }
    agentSkillMapper.deleteAgentSkill(tenantId, skillId, userId);
    if (skill.getFileInfoId() != null && skill.getSkillSquareId() != null) {
      fileInfoManageService.softDeleteFileInfoRecordOnly(tenantId, skill.getFileInfoId());
    }
    ResourceElementFactory.get(OperClassEnum.AGENT_SKILL.name()).clear(tenantId, skillId);
    return ResultVO.success();
  }

  @Override
  public PageInfo<AgentSkillDTO> queryAgentSkillPage(AgentSkillQueryParams queryParams) {
    //noinspection resource
    return agentSkillMapper.selectAgentSkillPage(queryParams, queryParams.buildRowBounds()).toPageInfo();
  }

  @Override
  public List<SquareBotSkillRowDTO> listInstalledSkillVersionForSquareBots(Long tenantId, Long skillSquareId, List<Long> botIds) {
    if (tenantId == null || skillSquareId == null || CollectionUtils.isEmpty(botIds)) {
      return Collections.emptyList();
    }
    return agentSkillMapper.selectInstalledSkillVersionForSquareBots(tenantId, skillSquareId, botIds);
  }

  @Override
  public List<Long> listInstalledSkillSquareIds(Long tenantId, Long botId) {
    if (tenantId == null || botId == null) {
      return Collections.emptyList();
    }
    return agentSkillMapper.selectInstalledSkillSquareIdsByBot(tenantId, botId);
  }

  @Override
  public PageInfo<AgentSkillDTO> getUserSpaceSkillPage(AgentSkillQueryParams params) {
    params.setUserId(SessionUtil.getLoginInfo().getUserId());
    // 查询用户创建的技能
    if (params.getIsCreated()) {
      // noinspection resource
      return agentSkillMapper.selectUserSpaceCreatedSkillPage(params, params.buildRowBounds()).toPageInfo();
    }
    // 查询用户安装的技能
    // noinspection resource
    return agentSkillMapper.selectUserSpaceInstalledSkillPage(params, params.buildRowBounds()).toPageInfo();
  }

  @Override
  public ResultVO<FileInfoVO> uploadAgentSkillFile(MultipartFile file) {
    String originalFilename = StringUtils.trimToNull(file.getOriginalFilename());
    Path tempFile = null;
    try {
      tempFile = Files.createTempFile("agent-skill-", ".zip");
      file.transferTo(tempFile.toFile());
      return uploadAgentSkillFile(tempFile.toFile(), originalFilename);
    }
    catch (IOException e) {
      throw new BssException("文件处理异常: " + e.getMessage(), e);
    }
    finally {
      if (tempFile != null) {
        try {
          Files.deleteIfExists(tempFile);
        }
        catch (IOException e) {
          logger.warn("删除临时文件失败: {}", tempFile, e);
        }
      }
    }
  }

  @Override
  public ResultVO<FileInfoVO> uploadAgentSkillFile(File file, String originalFilename) {
    Assert.notNull(originalFilename, "文件名称不能为空");
    Assert.isTrue(originalFilename.endsWith(".zip"), "文件扩展名必须为 .zip");
    Assert.isTrue(file.length() > 0, "文件不能为空");
    // 校验文件内容
    try (InputStream inputStream = Files.newInputStream(file.toPath())) {
      ResultVO<FileInfoVO> validateResult = AgentSkillZipSupport.validateAgentSkillFile(inputStream);
      if (validateResult != null) {
        return validateResult;
      }
    }
    catch (IOException e) {
      logger.error("Failed to read uploaded file", e);
      return ResultVO.fail("读取上传文件失败: " + e.getMessage());
    }

    UploadConfigVO config = new UploadConfigVO();
    config.setOriginalFileName(originalFilename);
    config.setSubFolder("agent-skill");
    config.setFileSize(file.length());
    config.setFileType("zip");
    config.setIsPicture(false);
    try {
      FileInfoVO fileInfo = fileStoreService.uploadFile(file, config);
      return ResultVO.success(fileInfo);
    }
    catch (Exception e) {
      logger.error("Failed to upload file: name={}, originalFileName={}, size={}", file.getName(), originalFilename, file.length(), e);
      return ResultVO.fail("上传 " + originalFilename + " 失败: " + e.getMessage());
    }
  }

  @Override
  @Transactional
  public ResultVO<AgentSkillDirDTO> saveAgentSkillDir(AgentSkillDirDTO dir) {
    Assert.notNull(dir.getTenantId(), "租户 ID 不能为空");
    Assert.notNull(dir.getSkillId(), "技能 ID 不能为空");
    Assert.hasLength(dir.getDirName(), "目录名称不能为空");
    if (dir.getParentDirId() == null) {
      dir.setParentDirId(-1L);
    }
    findAgentSkill(dir.getTenantId(), dir.getSkillId());
    AgentSkillDirDTO old = dir.getDirId() == null ? null : agentSkillDirMapper.selectByDirId(dir.getTenantId(), dir.getDirId());
    if (old != null && agentSkillDirMapper.existDirName(dir.getTenantId(), dir.getSkillId(), dir.getDirName(), dir.getDirId()) > 0) {
      return ResultVO.fail("同技能下已存在同名目录: " + dir.getDirName());
    }
    // 如果是根目录名称有更新，则更新技能文件名称
    if (old != null && old.getParentDirId() == -1 && !Objects.equals(old.getDirName(), dir.getDirName())) {
      Assert.isTrue(SKILL_NAME_PATTERN.matcher(dir.getDirName()).matches(),
        "根目录名称不合法，必须在 1 到 64 个字符之间，只能包含小写字母、数字、连字符(-)，且不能以连字符开头");
      agentSkillMapper.updateAgentSkillFileName(dir.getTenantId(), dir.getSkillId(), dir.getDirName(), SessionUtil.getOptionalUserId());
    }
    dir.setStatusCd(BaseConsts.STATUS_CD_VALID);
    DataDifference<AgentSkillDirDTO> difference = DataDifferenceStarter.computeSave(old, dir, false, dir.getTenantId());
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteAgentSkillDir(Long tenantId, Long skillId, Long dirId) {
    AgentSkillDirDTO dir = agentSkillDirMapper.selectByDirId(tenantId, dirId);
    if (dir == null) {
      return ResultVO.fail("目录不存在");
    }
    if (!Objects.equals(dir.getSkillId(), skillId)) {
      return ResultVO.fail("目录不属于指定技能");
    }
    Long rootDirId = agentSkillOrchestrationSupport.findRootDirId(tenantId, skillId);
    if (rootDirId != null && rootDirId.equals(dirId)) {
      return ResultVO.fail("根目录不可删除");
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    agentSkillFileMapper.deleteAllByDirId(tenantId, dirId, userId);
    agentSkillDirMapper.deleteByDirId(tenantId, dirId, userId);
    agentSkillOrchestrationSupport.refreshSkillTool(tenantId, dir.getSkillId());
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<AgentSkillFileDTO> saveAgentSkillFile(AgentSkillFileDTO file) {
    findAgentSkill(file.getTenantId(), file.getSkillId());
    AgentSkillDirDTO dir = agentSkillDirMapper.selectByDirId(file.getTenantId(), file.getDirId());
    ResultVO<AgentSkillFileDTO> result = validateAgentSkillFile(file, dir);
    if (!result.isSuccess()) {
      return result;
    }
    AgentSkillFileDTO old = file.getSkillFileId() == null ? null : agentSkillFileMapper.selectBySkillFileId(file.getTenantId(), file.getSkillFileId());
    // 切换本体场景更新本体相关数据
    if (StringUtils.isNotEmpty(file.getSkillExtJson())) {
      agentSkillMapper.updateAgentSkillExtJson(file.getTenantId(), file.getSkillId(), file.getSkillExtJson(), SessionUtil.getOptionalUserId());
    }
    DataDifference<AgentSkillFileDTO> difference = DataDifferenceStarter.computeSave(old, file, false, file.getTenantId());
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    // 如果文件名是 SKILL.md 且是新增或内容有更新，则刷新技能工具
    if (SkillSquareConsts.ZIP_FILE_SKILL_MD.equalsIgnoreCase(file.getFileName()) && (old == null || !Objects.equals(old.getFileContent(),
      file.getFileContent()))) {
      // 刷新技能工具
      agentSkillOrchestrationSupport.refreshSkillTool(file.getTenantId(), file.getSkillId());
    }
    AgentSkillFileDTO saved = difference.getToSaveData();
    saved.setTextEditable(true);
    return ResultVO.success(saved);
  }

  /**
   * 校验技能文件
   */
  private ResultVO<AgentSkillFileDTO> validateAgentSkillFile(AgentSkillFileDTO file, @Nullable AgentSkillDirDTO dir) {
    if (dir == null) {
      return ResultVO.fail("目录不存在");
    }
    file.setFileType(AgentSkillTextFileHelper.normalizeExtension(file.getFileName()));
    if (agentSkillFileMapper.existByFileNameInDir(file.getTenantId(), file.getDirId(), file.getFileName(), file.getSkillFileId()) > 0) {
      return ResultVO.fail("同目录下已存在同名文件: " + file.getFileName());
    }
    // 校验 SKILL.md 内容
    if (SkillSquareConsts.ZIP_FILE_SKILL_MD.equalsIgnoreCase(file.getFileName())) {
      ResultVO<Void> skillMdOk = validateSkillMdContent(file.getFileContent(), file.getTenantId(), file.getSkillId());
      if (!skillMdOk.isSuccess()) {
        return ResultVO.fail(skillMdOk.getResultMsg());
      }
    }
    return ResultVO.success();
  }

  /**
   * 校验 SKILL.md 文本元数据与正文
   */
  private ResultVO<Void> validateSkillMdContent(String content, Long tenantId, Long skillId) {
    if (StringUtils.isBlank(content)) {
      return ResultVO.fail("SKILL.md 内容不能为空");
    }
    AgentSkillDirDTO agentSkillRootDir = agentSkillDirMapper.selectRootDirBySkillId(tenantId, skillId);
    // 解析 SKILL.md
    SkillMdParseResult result = AgentSkillMarkdownParser.parseSkill(content);
    // 校验 SKILL.md 解析结果
    AgentSkillZipSupport.validateSkillMdParsed(result);
    String name = MapUtils.getString(result.metadata(), "name");
    if (!Objects.equals(agentSkillRootDir.getDirName(), name)) {
      return ResultVO.fail("SKILL.md 中的 name（" + name + "）与根目录名称（" + agentSkillRootDir.getDirName() + "）不一致，请修改后再保存文件");
    }
    AgentSkillZipSupport.validateSkillFile(content);
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<AgentSkillFileDTO> uploadAgentSkillFile(MultipartFile multipartFile, Long tenantId, Long skillId, Long dirId) {
    findAgentSkill(tenantId, skillId);
    String originalFilename = multipartFile.getOriginalFilename();
    if (StringUtils.isEmpty(originalFilename)) {
      return ResultVO.fail("文件名称不能为空");
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    // 校验文件名
    if (agentSkillFileMapper.existByFileNameInDir(tenantId, dirId, originalFilename, null) > 0) {
      return ResultVO.fail("同目录下已存在同名文件: " + originalFilename);
    }
    // 如果是 SKILL.md 文件，则校验目录
    Path tempFile = null;
    try {
      boolean textFile = AgentSkillTextFileHelper.isTextFile(originalFilename);
      AgentSkillFileDTO dto = buildSkillFileDTO(multipartFile, tenantId, skillId, dirId, userId);
      // 处理文本文件上传
      if (textFile) {
        handleTextFileUpload(dto, multipartFile, originalFilename, tenantId, skillId);
      }
      else {
        tempFile = handleBinaryFileUpload(dto, multipartFile, originalFilename, tenantId, userId);
      }
      agentSkillFileMapper.insertAgentSkillFile(dto);
      dto.setTextEditable(textFile);
      return ResultVO.success(dto);
    }
    catch (Exception e) {
      logger.error("upload skill binary file failed", e);
      throw new BssException("上传失败: " + e.getMessage(), e);
    }
    finally {
      if (tempFile != null) {
        FileUtils.deleteQuietly(tempFile.toFile());
      }
    }
  }

  /**
   * 构建技能文件DTO
   */
  private AgentSkillFileDTO buildSkillFileDTO(MultipartFile multipartFile, Long tenantId, Long skillId, Long dirId, Long userId) {
    String originalFilename = multipartFile.getOriginalFilename();
    AgentSkillFileDTO dto = new AgentSkillFileDTO();
    dto.setSkillFileId(IDUtils.nextId());
    dto.setDirId(dirId);
    dto.setSkillId(skillId);
    dto.setTenantId(tenantId);
    dto.setFileName(originalFilename);
    dto.setFileType(AgentSkillTextFileHelper.normalizeExtension(originalFilename));
    dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
    dto.setCreatorId(userId);
    dto.setUpdatorId(userId);
    return dto;
  }

  /**
   * 处理文本文件上传
   */
  private void handleTextFileUpload(AgentSkillFileDTO dto, MultipartFile multipartFile, String originalFilename, Long tenantId, Long skillId) throws IOException {
    dto.setFileContent(new String(multipartFile.getBytes(), StandardCharsets.UTF_8));
    dto.setFileInfoId(null);
    if (SkillSquareConsts.ZIP_FILE_SKILL_MD.equalsIgnoreCase(originalFilename)) {
      agentSkillOrchestrationSupport.refreshSkillTool(tenantId, skillId);
    }
  }

  /**
   * 处理二进制文件上传
   */
  private Path handleBinaryFileUpload(AgentSkillFileDTO dto, MultipartFile multipartFile, String originalFilename, Long tenantId, Long userId) throws IOException {
    String extension = AgentSkillTextFileHelper.normalizeExtension(originalFilename);
    Path tempFile = Files.createTempFile("agent-skill-file-", extension);
    multipartFile.transferTo(tempFile.toFile());
    File up = tempFile.toFile();

    UploadConfigVO config = new UploadConfigVO();
    config.setOriginalFileName(originalFilename);
    config.setSubFolder("agent-skill-file");
    config.setFileSize(up.length());
    config.setFileType(extension);
    config.setIsPicture(false);

    FileInfoVO uploaded = fileStoreService.uploadFile(up, config);
    Long fileInfoPk = IDUtils.nextId();
    FileInfoDTO fi = new FileInfoDTO();
    fi.setFileInfoId(fileInfoPk);
    fi.setFileId(uploaded.getFileId());
    fi.setFileName(uploaded.getFileName());
    fi.setTenantId(tenantId);
    fi.setBusiType(BaseConsts.FILE_BUSI_TYPE_AGENT_SKILL_FILE);
    fi.setStatusCd(BaseConsts.STATUS_CD_VALID);
    fi.setCreatorId(userId);
    fi.setUpdatorId(userId);
    fileInfoManageMapper.insertFileInfo(fi);

    dto.setFileContent(null);
    dto.setFileInfoId(fileInfoPk);
    return tempFile;
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteAgentSkillFile(Long tenantId, Long skillId, Long skillFileId) {
    AgentSkillFileDTO agentSkillFile = agentSkillFileMapper.selectBySkillFileId(tenantId, skillFileId);
    if (agentSkillFile == null) {
      return ResultVO.fail("文件不存在");
    }
    if (!Objects.equals(agentSkillFile.getSkillId(), skillId)) {
      return ResultVO.fail("文件不属于指定技能");
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    if (agentSkillFile.getFileInfoId() != null) {
      fileInfoManageService.deleteFileInfo(tenantId, agentSkillFile.getFileInfoId());
    }
    agentSkillFileMapper.deleteBySkillFileId(tenantId, skillFileId, userId);
    if (SkillSquareConsts.ZIP_FILE_SKILL_MD.equalsIgnoreCase(agentSkillFile.getFileName())) {
      agentSkillOrchestrationSupport.refreshSkillTool(tenantId, agentSkillFile.getSkillId());
    }
    return ResultVO.success();
  }

  @Override
  public ResultVO<AgentSkillFileDTO> getAgentSkillFileContent(Long tenantId, Long skillFileId) {
    AgentSkillFileDTO agentSkillFile = agentSkillFileMapper.selectBySkillFileId(tenantId, skillFileId);
    if (agentSkillFile == null) {
      return ResultVO.fail("文件不存在");
    }
    agentSkillFile.setTextEditable(AgentSkillTextFileHelper.isTextFile(agentSkillFile.getFileName()));
    return ResultVO.success(agentSkillFile);
  }

  @Override
  public ResultVO<List<AgentSkillTreeNodeDTO>> queryAgentSkillTree(Long tenantId, Long skillId) {
    findAgentSkill(tenantId, skillId);
    List<AgentSkillDirDTO> dirs = agentSkillDirMapper.listActiveBySkillId(tenantId, skillId);
    Map<Long, AgentSkillTreeNodeDTO> nodeMap = new LinkedHashMap<>();
    for (AgentSkillDirDTO dir : dirs) {
      AgentSkillTreeNodeDTO node = new AgentSkillTreeNodeDTO();
      node.setNodeId(dir.getDirId());
      node.setParentId(dir.getParentDirId());
      node.setNodeName(dir.getDirName());
      node.setNodeType("dir");
      nodeMap.put(node.getNodeId(), node);
    }

    // 文件节点并入统一树节点
    for (AgentSkillDirDTO dir : dirs) {
      List<AgentSkillFileDTO> files = agentSkillFileMapper.selectActiveMetaByDirId(tenantId, dir.getDirId());
      AgentSkillTreeNodeDTO dirNode = nodeMap.get(dir.getDirId());
      if (dirNode == null) {
        continue;
      }
      for (AgentSkillFileDTO file : files) {
        AgentSkillTreeNodeDTO fileNode = new AgentSkillTreeNodeDTO();
        fileNode.setNodeId(file.getSkillFileId());
        fileNode.setParentId(dir.getDirId());
        fileNode.setNodeName(file.getFileName());
        fileNode.setNodeType("file");
        fileNode.setFileType(file.getFileType());
        fileNode.setTextEditable(AgentSkillTextFileHelper.isTextFile(file.getFileName()));
        dirNode.getChildren().add(fileNode);
      }
    }

    List<AgentSkillTreeNodeDTO> tree = new ArrayList<>();
    for (AgentSkillTreeNodeDTO node : nodeMap.values()) {
      Long parentId = node.getParentId();
      AgentSkillTreeNodeDTO parentNode = parentId == null ? null : nodeMap.get(parentId);
      if (parentNode != null) {
        parentNode.getChildren().add(node);
      }
      else {
        tree.add(node);
      }
    }
    return ResultVO.success(tree);
  }

  @Override
  @Transactional
  public ResultVO<Void> checkAndInitAgentSkill(Long tenantId, Long skillId) {
    AgentSkillDTO skill = findAgentSkill(tenantId, skillId);
    if (agentSkillDirMapper.existDir(tenantId, skillId) > 0) {
      return ResultVO.success();
    }
    try {
      // 如果有 zip 文件，则先解析 zip 文件
      if (skill.getFileInfoId() != null && skill.getFileId() != null) {
        FileInfoDTO fi = fileInfoManageMapper.getFileInfo(tenantId, skill.getFileInfoId());
        if (fi == null || fi.getFileId() == null) {
          return ResultVO.fail("技能 zip 文件信息不存在，无法解析");
        }
        FileInfoVO vo = fileStoreService.getFileInfoById(fi.getFileId());
        if (vo == null) {
          return ResultVO.fail("技能 zip 文件不存在");
        }
        Path tmpZip = Files.createTempFile("skill-legacy-", ".zip");
        try (InputStream inputStream = fileStoreService.downloadFileStreamFromCache(vo)) {
          Files.copy(inputStream, tmpZip, StandardCopyOption.REPLACE_EXISTING);
        }
        try (InputStream in = Files.newInputStream(tmpZip)) {
          agentSkillOrchestrationSupport.importTemplateAgentSkill(in, skillId, tenantId, skill.getSkillFileName());
        }
        finally {
          Files.deleteIfExists(tmpZip);
        }
      }
      else {
        ClassPathResource resource = new ClassPathResource(TEMPLATE_ZIP_CLASSPATH);
        try (InputStream in = resource.getInputStream()) {
          agentSkillOrchestrationSupport.importTemplateAgentSkill(in, skillId, tenantId, skill.getSkillFileName());
          agentSkillNameSyncSupport.fillSkillFileNameOnly(skill);
        }
        autoUploadAgentSkill(skill);
      }
      agentSkillOrchestrationSupport.refreshSkillTool(tenantId, skillId);
      return ResultVO.success();
    }
    catch (IOException e) {
      logger.error("checkAndInit skill tree failed skillId={}", skillId, e);
      throw new BssException("初始化技能树失败: msg=", e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public ResultVO<AgentSkillPublishResultDTO> publishAgentSkill(Long tenantId, Long skillId) {
    AgentSkillDTO skillRow = findAgentSkill(tenantId, skillId);
    // 验证并获取发布所需根目录和 SKILL.md 文件
    ResultVO<Pair<AgentSkillDirDTO, AgentSkillFileDTO>> publishContext = validatePublishContext(tenantId, skillId);
    if (!publishContext.isSuccess()) {
      return ResultVO.fail(publishContext.getResultMsg());
    }
    AgentSkillDirDTO root = publishContext.getResultObject().getLeft();
    Long rootDirId = root.getDirId();
    // 解析发布包名称
    String packName = getPackName(tenantId, rootDirId);
    String zipFileName = packName + ".zip";
    // 生成 ZIP 文件到临时文件
    Path tmp = null;
    try {
      tmp = Files.createTempFile("agent-skill-pub-", ".zip");
      agentSkillOrchestrationSupport.buildPublishZipFile(tenantId, skillId, tmp);
      // 上传并发布
      return uploadAndPublish(skillRow, tenantId, skillId, tmp.toFile(), zipFileName);
    }
    catch (IOException e) {
      logger.error("build publish zip failed skillId={}", skillId, e);
      throw new BssException("生成发布包失败: msg=", e.getMessage(), e);
    }
    finally {
      if (tmp != null) {
        FileUtils.deleteQuietly(tmp.toFile());
      }
    }
  }

  /**
   * 校验发布前置数据并返回根目录与 SKILL.md。
   */
  private ResultVO<Pair<AgentSkillDirDTO, AgentSkillFileDTO>> validatePublishContext(Long tenantId, Long skillId) {
    AgentSkillDirDTO root = agentSkillDirMapper.selectRootDirBySkillId(tenantId, skillId);
    if (root == null || root.getDirId() == null) {
      logger.warn("publish skill failed: missing root dir, tenantId={}, skillId={}", tenantId, skillId);
      return ResultVO.fail("缺少根目录，无法发布技能");
    }
    AgentSkillFileDTO skillMdFile = agentSkillFileMapper.selectSkillMdBySkillId(tenantId, skillId);
    if (skillMdFile == null || StringUtils.isBlank(skillMdFile.getFileContent())) {
      return ResultVO.fail("缺少 SKILL.md 文件");
    }
    // 解析 SKILL.md 文件
    SkillMdParseResult result = AgentSkillMarkdownParser.parseSkill(skillMdFile.getFileContent());
    AgentSkillZipSupport.validateSkillMdParsed(result);
    String name = MapUtils.getString(result.metadata(), "name");
    if (!Objects.equals(root.getDirName(), name)) {
      return ResultVO.fail("SKILL.md 中的 name（" + name + "）与根目录名称（" + root.getDirName() + "）不一致，请修改后保存文件再保存skill包");
    }
    // 安全扫描
    String illegalMsg = agentSkillSecurityScanner.scan(tenantId, skillId);
    if (StringUtils.isNotEmpty(illegalMsg)) {
      return ResultVO.fail(illegalMsg);
    }
    return ResultVO.success(Pair.of(root, skillMdFile));
  }

  /**
   * 上传 ZIP 并完成发布流程
   */
  private ResultVO<AgentSkillPublishResultDTO> uploadAndPublish(AgentSkillDTO skillRow, Long tenantId, Long skillId, File zipFile,
    String zipFileName) {
    try {
      // 上传文件
      ResultVO<FileInfoVO> uploadResult = uploadAgentSkillFile(zipFile, zipFileName);
      if (!uploadResult.isSuccess()) {
        return ResultVO.fail(uploadResult.getResultMsg());
      }
      // 保存文件信息并更新技能
      FileInfoVO uploaded = uploadResult.getResultObject();
      Long userId = SessionUtil.getLoginInfo().getUserId();
      Long newFileInfoId = IDUtils.nextId();
      saveFileInfo(newFileInfoId, tenantId, userId, uploaded);
      agentSkillMapper.updateAgentSkillFileInfo(tenantId, skillId, newFileInfoId, userId);
      // 清理旧文件
      cleanupOldFileInfo(skillRow, tenantId);
      // 返回发布结果
      AgentSkillPublishResultDTO result = new AgentSkillPublishResultDTO();
      result.setFileInfoId(newFileInfoId);
      result.setZipFileName(zipFileName);
      result.setPublishedTime(new Date());
      return ResultVO.success(result);
    }
    catch (Exception e) {
      logger.error("publish agent skill failed skillId={}", skillId, e);
      throw new BssException("发布失败: msg=", e.getMessage(), e);
    }
  }

  /**
   * 保存文件信息
   */
  private void saveFileInfo(Long fileInfoId, Long tenantId, Long userId, FileInfoVO uploaded) {
    FileInfoDTO fileInfo = new FileInfoDTO();
    fileInfo.setFileInfoId(fileInfoId);
    fileInfo.setFileId(uploaded.getFileId());
    fileInfo.setFileName(uploaded.getFileName());
    fileInfo.setTenantId(tenantId);
    fileInfo.setBusiType(BaseConsts.FILE_BUSI_TYPE_AGENT_SKILL);
    fileInfo.setStatusCd(BaseConsts.STATUS_CD_VALID);
    fileInfo.setCreatorId(userId);
    fileInfo.setUpdatorId(userId);
    fileInfoManageMapper.insertFileInfo(fileInfo);
  }

  /**
   * 清理旧文件信息
   */
  private void cleanupOldFileInfo(AgentSkillDTO skillRow, Long tenantId) {
    Long oldFileInfoId = skillRow.getFileInfoId();
    if (oldFileInfoId == null) {
      return;
    }
    try {
      if (skillRow.getSkillSquareId() == null) {
        fileInfoManageService.deleteFileInfo(tenantId, oldFileInfoId);
      }
      else {
        fileInfoManageService.softDeleteFileInfoRecordOnly(tenantId, oldFileInfoId);
      }
    }
    catch (Exception e) {
      logger.error("cleanup old file info failed, tenantId={}, oldFileInfoId={}", tenantId, oldFileInfoId, e);
    }
  }

  /**
   * 解析发布包名称
   */
  private String getPackName(Long tenantId, Long rootDirId) {
    AgentSkillDirDTO rootDir = agentSkillDirMapper.selectByDirId(tenantId, rootDirId);
    return rootDir != null ? rootDir.getDirName() : "agent-skill";
  }

  @Override
  public PageInfo<OntologyAppDTO> queryOntologyAppPage(OntologyQueryParams queryParams) {
    return ontologyApiHelper.queryOntologyAppPage(queryParams);
  }

  @Override
  public PageInfo<OntoSceneDTO> queryOntologyScenePage(OntologyQueryParams queryParams) {
    return ontologyApiHelper.queryOntologyScenePage(queryParams);
  }

  @Override
  public OntoSceneDTO getOntologySceneDetail(OntologyQueryParams queryParams) {
    return ontologyApiHelper.getOntologySceneDetail(queryParams);
  }

  @Override
  public PageInfo<OntologyRuleDTO> queryOntologyRulePage(OntologyQueryParams queryParams) {
    return ontologyApiHelper.queryOntologyRulePage(queryParams);
  }

  @Override
  public PageInfo<OntologyObjectDTO> queryOntologyObjectPage(OntologyQueryParams queryParams) {
    return ontologyApiHelper.queryOntologyObjectPage(queryParams);
  }

  @Override
  public PageInfo<OntologyActionDTO> queryOntologyActionPage(OntologyQueryParams queryParams) {
    return ontologyApiHelper.queryOntologyActionPage(queryParams);
  }
}
