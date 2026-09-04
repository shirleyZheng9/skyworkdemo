package com.iwhalecloud.bote.service.skill.support;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.SkillSquareConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillDirDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillFileDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillToolDTO;
import com.iwhalecloud.bote.dto.skill.SimpleAgentSkillToolDTO;
import com.iwhalecloud.bote.mapper.base.FileInfoManageMapper;
import com.iwhalecloud.bote.mapper.skill.AgentSkillDirMapper;
import com.iwhalecloud.bote.mapper.skill.AgentSkillFileMapper;
import com.iwhalecloud.bote.mapper.skill.AgentSkillToolMapper;
import com.iwhalecloud.bote.common.util.ZipUtil;
import com.iwhalecloud.bote.service.skill.impl.helper.AgentSkillTextFileHelper;
import com.iwhalecloud.bote.service.skill.support.AgentSkillZipSupport.AgentSkillZipValidated;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Agent Skill zip 导入、打包、槽位同步等支撑逻辑
 *
 * @author qian.sisheng
 * @since 2026-04-28
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class AgentSkillOrchestrationSupport {
  private static final Logger logger = LoggerFactory.getLogger(AgentSkillOrchestrationSupport.class);


  private final AgentSkillDirMapper agentSkillDirMapper;
  private final AgentSkillFileMapper agentSkillFileMapper;
  private final AgentSkillToolMapper agentSkillToolMapper;
  private final FileInfoManageMapper fileInfoManageMapper;
  private final IFileStoreService fileStoreService;

  /**
   * 从 zip 流导入目录文件数据
   */
  public void importTemplateAgentSkill(InputStream zipInput, Long skillId, Long tenantId, String skillFileName) {
    File tmpDir = null;
    try {
      tmpDir = Files.createTempDirectory("agent-skill-template-").toFile();
      // 解压并单次解析 SKILL.md，后续落库复用快照，避免重复读盘与 parse
      ResultVO<AgentSkillZipValidated> validatedResult = AgentSkillZipSupport.validateAndPareAgentSkillFile(zipInput, tmpDir);
      if (!validatedResult.isSuccess()) {
        throw new BssException(validatedResult.getResultMsg());
      }
      AgentSkillZipValidated validated = validatedResult.getResultObject();
      File skillFile = validated.getSkillMdFile();
      Assert.notNull(skillFile, "缺少 SKILL.md 文件");
      // 导入时根目录名：入参优先，否则用 zip 内已解析的 name
      String rootDirName;
      if (StringUtils.isNotBlank(skillFileName)) {
        rootDirName = skillFileName;
      }
      else {
        rootDirName = MapUtils.getString(validated.getMetadata(), "name");
      }
      saveSkillDirectoryStructure(skillFile.getParentFile(), rootDirName, skillId, tenantId, validated);
    }
    catch (Exception e) {
      logger.error("import agent skill zip failed skillId={}", skillId, e);
      throw new BssException("导入技能包失败，msg=", e.getMessage(), e);
    }
    finally {
      FileUtils.deleteQuietly(tmpDir);
    }
  }

  /**
   * 将标准 Agent Skill zip 解压后的单根目录写入目录/文件表，并允许指定根目录名称。
   *
   */
  public void saveSkillDirectoryStructure(File skillRootDir, String rootDirName, Long skillId, Long tenantId, AgentSkillZipValidated zipValidated)
    throws IOException {
    if (!skillRootDir.isDirectory()) {
      throw new BssException("技能根路径不是目录");
    }
    Long userId = SessionUtil.getOptionalUserId();
    // 根目录名优先使用 SKILL.md 的 name,取不到时回退 zip 原始一级目录名。
    String finalRootDirName = StringUtils.defaultIfBlank(rootDirName, skillRootDir.getName());
    Long rootDirId = insertRootDir(finalRootDirName, skillId, tenantId);
    File[] children = skillRootDir.listFiles();
    if (children == null) {
      return;
    }
    for (File child : children) {
      if (child.isFile()) {
        insertFileRow(child, rootDirId, skillId, tenantId, rootDirName, zipValidated);
      }
      else if (child.isDirectory()) {
        processDirectoryRecursive(child, rootDirId, skillId, tenantId, userId, rootDirName, zipValidated);
      }
    }
  }

  /**
   * 递归处理目录及其子目录
   */
  private void processDirectoryRecursive(File directory, Long parentDirId, Long skillId, Long tenantId, Long userId,
    String rootDirName, AgentSkillZipValidated zipValidated) throws IOException {
    Long currentDirId = insertChildDir(parentDirId, directory.getName(), skillId, tenantId, userId);
    File[] children = directory.listFiles();
    if (children == null) {
      return;
    }
    for (File child : children) {
      if (child.isFile()) {
        insertFileRow(child, currentDirId, skillId, tenantId, rootDirName, zipValidated);
      }
      else if (child.isDirectory()) {
        processDirectoryRecursive(child, currentDirId, skillId, tenantId, userId, rootDirName, zipValidated);
      }
    }
  }

  /**
   * 按数据库目录文件数据生成标准 zip 文件
   */
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public void buildPublishZipFile(Long tenantId, Long skillId, Path outputFile) throws IOException {
    Path staging = Files.createTempDirectory("agent-skill-publish-");
    try {
      List<AgentSkillDirDTO> dirs = agentSkillDirMapper.listActiveBySkillId(tenantId, skillId);
      Long rootDirId = findRootDirId(tenantId, skillId);
      List<AgentSkillFileDTO> files = agentSkillFileMapper.selectActiveBySkillId(tenantId, skillId);
      for (AgentSkillFileDTO file : files) {
        Path target;
        // 根目录文件直接写入根目录，不再创建子目录
        if (rootDirId != null && rootDirId.equals(file.getDirId())) {
          target = staging.resolve(file.getFileName());
        }
        else {
          // 非根目录文件按目录结构写入
          String sub = resolveDirName(dirs, file.getDirId());
          if (StringUtils.isBlank(sub)) {
            target = staging.resolve(file.getFileName());
          }
          else {
            Path subDir = staging.resolve(sub);
            Files.createDirectories(subDir);
            target = subDir.resolve(file.getFileName());
          }
        }
        Files.createDirectories(target.getParent());
        writeFileToPack(tenantId, file, target);
      }
      try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(outputFile))) {
        ZipUtil.zipDirectory(staging, zos);
      }
    }
    finally {
      FileUtils.deleteQuietly(staging.toFile());
    }
  }

  /**
   * 仅解析根目录 SKILL.md 中的槽位并刷新 bt_agent_skill_tool
   */
  public void refreshSkillTool(Long tenantId, Long skillId) {
    Long userId = SessionUtil.getOptionalUserId();
    String skillMdContent = getSkillMdContent(tenantId, skillId);
    // 解析技能SKILL.md中的槽位信息
    Map<String, Map<Long, String>> toolInfos = AgentSkillToolScannerSupport.parseTools(skillMdContent);
    agentSkillToolMapper.deleteBySkillId(tenantId, skillId, userId);
    if (toolInfos.isEmpty()) {
      return;
    }
    List<AgentSkillToolDTO> tools = new ArrayList<>();
    for (Map.Entry<String, Map<Long, String>> e : toolInfos.entrySet()) {
      String toolType = e.getKey();
      for (Map.Entry<Long, String> entry : e.getValue().entrySet()) {
        AgentSkillToolDTO tool = new AgentSkillToolDTO();
        tool.setId(IDUtils.nextId());
        tool.setTenantId(tenantId);
        tool.setSkillId(skillId);
        tool.setToolId(entry.getKey());
        tool.setToolType(toolType);
        tool.setToolName(entry.getValue());
        tool.setStatusCd(BaseConsts.STATUS_CD_VALID);
        tool.setCreatorId(userId);
        tool.setUpdatorId(userId);
        tools.add(tool);
      }
    }
    if (CollectionUtils.isNotEmpty(tools)) {
      agentSkillToolMapper.batchInsertAgentSkillTools(tools);
    }
  }

  /**
   * 获取技能中应用的工具列表
   */
  public List<SimpleAgentSkillToolDTO> getSkillTools(Long tenantId, Long skillId) {
    String skillMdContent = getSkillMdContent(tenantId, skillId);
    if (skillMdContent.isEmpty()) {
      return List.of();
    }
    // 解析技能SKILL.md中的槽位信息
    Map<String, Map<Long, String>> toolInfos = AgentSkillToolScannerSupport.parseTools(skillMdContent);
    List<SimpleAgentSkillToolDTO> tools = new ArrayList<>();
    for (Map.Entry<String, Map<Long, String>> e : toolInfos.entrySet()) {
      String toolType = e.getKey();
      for (Map.Entry<Long, String> entry : e.getValue().entrySet()) {
        SimpleAgentSkillToolDTO tool = new SimpleAgentSkillToolDTO();
        tool.setSkillId(skillId);
        tool.setToolId(entry.getKey());
        tool.setToolType(toolType);
        tool.setToolName(entry.getValue());
        tools.add(tool);
      }
    }
    return tools;
  }

  /**
   * 获取根目录 SKILL.md 内容；不存在时返回空串
   */
  private String getSkillMdContent(Long tenantId, Long skillId) {
    AgentSkillFileDTO agentSkillFile = agentSkillFileMapper.selectSkillMdBySkillId(tenantId, skillId);
    if (agentSkillFile != null) {
      return agentSkillFile.getFileContent();
    }
    return "";
  }

  /**
   * 逻辑清空某技能下目录与文件
   */
  public void deleteSkillFileData(Long tenantId, Long skillId, Long userId) {
    agentSkillFileMapper.deleteAllBySkillId(tenantId, skillId, userId);
    agentSkillDirMapper.deleteAllBySkillId(tenantId, skillId, userId);
    agentSkillToolMapper.deleteBySkillId(tenantId, skillId, userId);
  }

  /**
   * 解析技能根目录
   */
  public Long findRootDirId(Long tenantId, Long skillId) {
    AgentSkillDirDTO root = agentSkillDirMapper.selectRootDirBySkillId(tenantId, skillId);
    return root == null ? null : root.getDirId();
  }

  /**
   * 解析目录名
   */
  private String resolveDirName(List<AgentSkillDirDTO> dirs, Long dirId) {
    for (AgentSkillDirDTO d : dirs) {
      if (d.getDirId().equals(dirId)) {
        Long rootDirId = findRootDirId(d.getTenantId(), d.getSkillId());
        return d.getDirId().equals(rootDirId) ? "" : d.getDirName();
      }
    }
    return "";
  }

  /**
   * 将文件写入打包目录
   */
  private void writeFileToPack(Long tenantId, AgentSkillFileDTO f, Path target) throws IOException {
    if (f.getFileInfoId() != null) {
      FileInfoDTO fi = fileInfoManageMapper.getFileInfo(tenantId, f.getFileInfoId());
      if (fi == null || fi.getFileId() == null) {
        Files.write(target, new byte[0]);
        return;
      }
      FileInfoVO vo = fileStoreService.getFileInfoById(fi.getFileId());
      if (vo == null) {
        Files.write(target, new byte[0]);
        return;
      }
      try (InputStream in = fileStoreService.downloadFileStreamFromCache(vo)) {
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
      }
    }
    else {
      Files.writeString(target, StringUtils.defaultString(f.getFileContent()), StandardCharsets.UTF_8);
    }
  }

  /**
   * 新增根目录
   */
  private Long insertRootDir(String rootDirName, Long skillId, Long tenantId) {
    AgentSkillDirDTO dto = new AgentSkillDirDTO();
    dto.setDirId(IDUtils.nextId());
    dto.setParentDirId(-1L);
    dto.setSkillId(skillId);
    dto.setTenantId(tenantId);
    dto.setDirName(rootDirName);
    dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
    dto.setCreatorId(SessionUtil.getOptionalUserId());
    dto.setUpdatorId(SessionUtil.getOptionalUserId());
    agentSkillDirMapper.insertAgentSkillDir(dto);
    return dto.getDirId();
  }

  /**
   * 新增一级子目录
   */
  private Long insertChildDir(Long parentDirId, String dirName, Long skillId, Long tenantId, Long userId) {
    AgentSkillDirDTO dto = new AgentSkillDirDTO();
    dto.setDirId(IDUtils.nextId());
    dto.setParentDirId(parentDirId);
    dto.setSkillId(skillId);
    dto.setTenantId(tenantId);
    dto.setDirName(dirName);
    dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
    dto.setCreatorId(userId);
    dto.setUpdatorId(userId);
    agentSkillDirMapper.insertAgentSkillDir(dto);
    return dto.getDirId();
  }

  /**
   * 新增文件行
   */
  private void insertFileRow(File file, Long dirId, Long skillId, Long tenantId, String skillFileName,
    AgentSkillZipValidated zipValidated) throws IOException {
    AgentSkillFileDTO dto = new AgentSkillFileDTO();
    dto.setSkillFileId(IDUtils.nextId());
    dto.setDirId(dirId);
    dto.setSkillId(skillId);
    dto.setTenantId(tenantId);
    dto.setFileName(file.getName());
    dto.setFileType(AgentSkillTextFileHelper.normalizeExtension(file.getName()));
    dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
    dto.setCreatorId(SessionUtil.getOptionalUserId());
    dto.setUpdatorId(SessionUtil.getOptionalUserId());
    if (AgentSkillTextFileHelper.isTextFile(file.getName())) {
      // 探测文件编码
      String charset = detectEncoding(file);
      String content = FileUtils.readFileToString(file, charset);
      if (SkillSquareConsts.ZIP_FILE_SKILL_MD.equalsIgnoreCase(file.getName())) {
        String oldSkillName = MapUtils.getString(zipValidated.getMetadata(), "name");
        content = content.replace(oldSkillName, skillFileName);
      }
      dto.setFileContent(content);
      dto.setFileInfoId(null);
    }
    else {
      dto.setFileContent(null);
      UploadConfigVO config = new UploadConfigVO();
      config.setOriginalFileName(file.getName());
      config.setSubFolder("agent-skill-orchestration");
      config.setFileSize(file.length());
      config.setFileType(StringUtils.defaultIfBlank(dto.getFileType(), "bin"));
      config.setIsPicture(false);
      FileInfoVO uploaded = fileStoreService.uploadFile(file, config);
      Long fileInfoPk = IDUtils.nextId();
      FileInfoDTO fi = new FileInfoDTO();
      fi.setFileInfoId(fileInfoPk);
      fi.setFileId(uploaded.getFileId());
      fi.setFileName(uploaded.getFileName());
      fi.setTenantId(tenantId);
      fi.setBusiType(BaseConsts.FILE_BUSI_TYPE_AGENT_SKILL_FILE);
      fi.setStatusCd(BaseConsts.STATUS_CD_VALID);
      fi.setCreatorId(SessionUtil.getOptionalUserId());
      fi.setUpdatorId(SessionUtil.getOptionalUserId());
      fileInfoManageMapper.insertFileInfo(fi);
      dto.setFileInfoId(fileInfoPk);
    }
    agentSkillFileMapper.insertAgentSkillFile(dto);
  }

  /**
   * 检测文件编码
   */
  public String detectEncoding(File file) throws IOException {
    CharsetDetector detector = new CharsetDetector();
    detector.setText(Files.readAllBytes(file.toPath()));
    // 获取最可能的编码
    CharsetMatch match = detector.detect();
    if (match == null) {
      throw new BssException("文件编码格式异常，请确保文件为标准UTF-8编码格式: " + file.getName());
    }
    return match.getName();
  }
}
