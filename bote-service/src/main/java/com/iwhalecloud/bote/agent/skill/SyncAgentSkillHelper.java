package com.iwhalecloud.bote.agent.skill;

import com.iwhalecloud.bote.common.enums.SandboxMode;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.ZipUtil;
import com.iwhalecloud.bote.dto.skill.AgentSkillFileDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillTreeNodeDTO;
import com.iwhalecloud.bote.dto.skill.SimpleAgentSkillDTO;
import com.iwhalecloud.bote.sandbox.api.SandboxClient;
import com.iwhalecloud.bote.sandbox.dto.SandboxFileReadResult;
import com.iwhalecloud.bote.sandbox.dto.SandboxFileSearchResult;
import com.iwhalecloud.bote.sandbox.dto.SandboxFileWriteResult;
import com.iwhalecloud.bote.sandbox.dto.SandboxRunRequest;
import com.iwhalecloud.bote.sandbox.dto.SandboxRunResult;
import com.iwhalecloud.bote.service.skill.IAgentSkillManageService;
import com.iwhalecloud.bote.service.skill.support.AgentSkillZipSupport;
import com.iwhalecloud.bote.websocket.context.WebSocketChatContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.socket.BinaryMessage;

/**
 * 同步技能辅助类
 *
 * @author bianjp
 * @since 2026-04-16
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class SyncAgentSkillHelper {
  private static final Logger logger = LoggerFactory.getLogger(SyncAgentSkillHelper.class);
  private static final IFileStoreService fileStoreService = SpringUtil.getBean(IFileStoreService.class);
  private static final IAgentSkillManageService agentSkillManageService = SpringUtil.getBean(IAgentSkillManageService.class);

  private SyncAgentSkillHelper() {
  }

  /**
   * 同步技能到桌面客户端
   *
   * @param userId 用户 ID
   * @param tenantId 租户 ID
   * @param enabledSkills 技能列表
   * @param workDir 工作目录
   * @param clientPathSeparator 路径分隔符（取决于客户端操作系统）
   * @param webSocketChatContext WebSocket 会话上下文
   * @return 技能列表
   */
  public static List<AgentSkillSpec> syncSkills(Long userId, Long tenantId, List<SimpleAgentSkillDTO> enabledSkills,
                                                SandboxMode sandboxMode, SandboxClient sandboxClient, String workDir,
                                                @Nullable String clientPathSeparator, @Nullable WebSocketChatContext webSocketChatContext) {
    List<AgentSkillSpec> skills;
    if (sandboxMode == SandboxMode.CLIENT) {
      assert clientPathSeparator != null;
      assert webSocketChatContext != null;
      skills = syncSkillsToClient(userId, tenantId, enabledSkills, workDir, clientPathSeparator, webSocketChatContext);
    }
    else {
      skills = syncSkillsToSandbox(userId, tenantId, enabledSkills, workDir, sandboxMode == SandboxMode.REMOTE, sandboxClient);
    }
    if (!skills.isEmpty()) {
      fillSkillTools(enabledSkills, skills, StringUtils.defaultIfEmpty(clientPathSeparator, "/"));
    }
    return skills;
  }

  /**
   * 填充技能关联的工具
   */
  private static void fillSkillTools(List<SimpleAgentSkillDTO> enabledSkills, List<AgentSkillSpec> skills, String pathSeparator) {
    for (SimpleAgentSkillDTO skill : enabledSkills) {
      if (CollectionUtils.isEmpty(skill.getTools())) {
        continue;
      }
      for (AgentSkillSpec spec : skills) {
        if (spec.getBaseDir().contains(pathSeparator + skill.getSkillId() + pathSeparator)) {
          spec.setTools(skill.getTools());
        }
      }
    }
  }

  /**
   * 同步技能到桌面客户端
   *
   * @param userId 用户 ID
   * @param tenantId 租户 ID
   * @param enabledSkills 技能列表
   * @param workDir 工作目录
   * @param pathSeparator 路径分隔符（取决于客户端操作系统）
   * @param webSocketChatContext WebSocket 会话上下文
   * @return 技能列表
   */
  private static List<AgentSkillSpec> syncSkillsToClient(Long userId, Long tenantId, List<SimpleAgentSkillDTO> enabledSkills, String workDir, String pathSeparator,
                                                         WebSocketChatContext webSocketChatContext) {
    // 客户的技能目录
    String clientSkillsDir = workDir + pathSeparator + "skills";
    // TODO 实现增量同步（只同步有更新的技能）
    File tmpDir = null;
    List<AgentSkillSpec> skills;
    try {
      // 创建临时目录
      tmpDir = Files.createTempDirectory("bote-agent-skills-").toFile();
      File skillsDir = new File(tmpDir, "skills");
      FileUtils.forceMkdir(skillsDir);

      // 加载预设技能
      List<AgentSkillSpec> presetSkills = new ArrayList<>(PresetAgentSkillsLoader.loadAll(skillsDir));

      // 加载用户技能
      List<AgentSkillSpec> userSkillSpecs = new ArrayList<>();
      for (SimpleAgentSkillDTO agentSkill : enabledSkills) {
        Path unzipRoot = skillsDir.toPath().resolve(agentSkill.getSkillId().toString());
        if (Boolean.TRUE.equals(agentSkill.getDebug())) {
          downloadDebugSkill(unzipRoot.toString(), agentSkill, false, null);
        }
        else {
          try (InputStream inputStream = getSkillFileStream(agentSkill)) {
            ZipUtil.unzip(inputStream, unzipRoot);
          }
        }
        File entryFile = AgentSkillZipSupport.findSkillFile(unzipRoot.toFile());
        Assert.notNull(entryFile, () -> "Agent Skill 的压缩包不正确，缺少 SKILL.md 文件: skillId=" + agentSkill.getSkillId());
        String baseDir = clientSkillsDir + pathSeparator + unzipRoot.relativize(entryFile.toPath()).toString().replace(File.separator, pathSeparator);
        userSkillSpecs.add(AgentSkillMarkdownParser.parseSkill(baseDir, entryFile));
      }
      skills = mergeAgentSkillsByPriority(presetSkills, List.of(), userSkillSpecs);
      AgentSkillSlotReplaceHelper.replaceSkillSlotsAndPersist(userId, tenantId, skills, skillsDir.getAbsolutePath(), false,  null);

      // 构造压缩包
      File zipFile = new File(tmpDir, "skills.zip");
      try (OutputStream outputStream = Files.newOutputStream(zipFile.toPath()); ZipOutputStream zos = new ZipOutputStream(outputStream)) {
        ZipUtil.zipDirectory(skillsDir.toPath(), null, true, zos);
      }

      // 发送压缩包给客户端
      try (FileChannel channel = FileChannel.open(zipFile.toPath(), StandardOpenOption.READ)) {
        MappedByteBuffer byteBuffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
        webSocketChatContext.sendBinaryMessage(new BinaryMessage(byteBuffer));
      }
    }
    catch (BssException e) {
      throw e;
    }
    catch (Exception e) {
      logger.error("Failed to sync agent skills to local: userId={}", userId, e);
      throw new BssException("同步 Agent Skills 到本地失败: " + e.getMessage(), e);
    }
    finally {
      FileUtils.deleteQuietly(tmpDir);
    }
    return skills;
  }

  /**
   * 同步技能到沙箱
   *
   * @param userId 用户 ID
   * @param tenantId 租户 ID
   * @param enabledSkills 技能列表
   * @param workDir 工作目录
   * @param useSandbox 是否使用远程沙箱
   * @return 技能列表
   */
  private static List<AgentSkillSpec> syncSkillsToSandbox(Long userId, Long tenantId, List<SimpleAgentSkillDTO> enabledSkills, String workDir, boolean useSandbox, SandboxClient sandboxClient) {
    String skillsDir = workDir + "/.skills";
    prepareSandboxAgentSkills(userId, enabledSkills, useSandbox, sandboxClient, skillsDir);
    // 预置 + 沙箱内全部 SKILL.md + 按启用列表显式加载（后者覆盖同名，避免扫描顺序/对话生成目录抢占）
    List<AgentSkillSpec> presetSkills = new ArrayList<>(PresetAgentSkillsLoader.loadAll(null));
    List<AgentSkillSpec> sandboxScanSpecs = listSandboxSkillSpecsFromScan(useSandbox, sandboxClient, skillsDir);
    List<AgentSkillSpec> enabledSpecs = loadEnabledAgentSkillSpecsFromSandbox(enabledSkills, useSandbox, sandboxClient, skillsDir);
    List<AgentSkillSpec> skills = mergeAgentSkillsByPriority(presetSkills, sandboxScanSpecs, enabledSpecs);
    AgentSkillSlotReplaceHelper.replaceSkillSlotsAndPersist(userId, tenantId, skills, skillsDir, useSandbox, sandboxClient);
    return skills;
  }

  /**
   * 将用户已启用的技能同步到沙箱并删除已禁用目录（不执行全量 SKILL.md 扫描）
   */
  private static void prepareSandboxAgentSkills(Long userId, List<SimpleAgentSkillDTO> enabledAgentSkills, boolean useSandbox, SandboxClient sandboxClient, String skillsDir) {
    List<String> existingSkillDirList = findExistingAgentSkills(useSandbox, sandboxClient, skillsDir);
    List<String> newSkillDirList = downloadAgentSkills(userId, enabledAgentSkills, useSandbox, sandboxClient, skillsDir);
    List<String> deletedSkillDirList = ListUtils.subtract(existingSkillDirList, newSkillDirList);
    if (!deletedSkillDirList.isEmpty()) {
      if (useSandbox) {
        SandboxRunResult result = sandboxClient.execute(SandboxRunRequest.builder().command("rm -rf " + String.join(" ", deletedSkillDirList)).build());
        if (!result.isSuccess()) {
          logger.warn("Failed to delete agent skills directories: userId={}, error={}", userId, result.getErrorMessage());
        }
      }
      else {
        deletedSkillDirList.stream().map(File::new).forEach(FileUtils::deleteQuietly);
      }
    }
  }

  /**
   * 搜索沙箱下所有 SKILL.md 并解析（含对话生成的技能；同名时优先级低于 {@link #loadEnabledAgentSkillSpecsFromSandbox}）。
   */
  private static List<AgentSkillSpec> listSandboxSkillSpecsFromScan(boolean useSandbox, SandboxClient sandboxClient, String skillsDir) {
    if (!useSandbox) {
      Collection<File> files = FileUtils.listFiles(new File(skillsDir), new NameFileFilter("SKILL.md"), TrueFileFilter.TRUE);
      List<AgentSkillSpec> skills = new ArrayList<>();
      for (File file : files) {
        String baseDir = file.getParentFile().getAbsolutePath();
        skills.add(AgentSkillMarkdownParser.parseSkill(baseDir, file));
      }
      return skills;
    }

    SandboxFileSearchResult searchResult = sandboxClient.searchFiles(skillsDir, "SKILL.md");
    Assert.isTrue(searchResult.isSuccess(), () -> "搜索 Agent Skills 的 SKILL.md 文件失败: " + searchResult.getErrorMessage());
    return ListUtils.emptyIfNull(searchResult.getEntries()).stream()
      .map(path -> {
        SandboxFileReadResult readResult = sandboxClient.readFile(path);
        Assert.isTrue(readResult.isSuccess(), () -> "读取 Agent Skill 的 SKILL.md 文件失败: " + readResult.getErrorMessage());
        String baseDir = Strings.CS.removeEnd(path, "SKILL.md");
        return AgentSkillMarkdownParser.parseSkill(baseDir, readResult.getContent());
      })
      .filter(s -> StringUtils.isNotEmpty(s.getName()) && StringUtils.isNotEmpty(s.getContent()))
      .toList();
  }

  /**
   * 按启用列表从沙箱加载 SKILL.md，与全量扫描顺序无关
   */
  private static List<AgentSkillSpec> loadEnabledAgentSkillSpecsFromSandbox(List<SimpleAgentSkillDTO> enabledAgentSkills, boolean useSandbox, SandboxClient sandboxClient, String skillsDir) {
    if (!useSandbox) {
      return loadEnabledAgentSkillSpecsFromLocal(enabledAgentSkills, skillsDir);
    }

    List<AgentSkillSpec> skills = new ArrayList<>();
    for (SimpleAgentSkillDTO skill : enabledAgentSkills) {
      Long skillId = skill.getSkillId();
      String basePath = skillsDir + "/" + skillId;
      SandboxFileSearchResult searchResult = sandboxClient.searchFiles(basePath, "SKILL.md");
      if (!searchResult.isSuccess() || CollectionUtils.isEmpty(searchResult.getEntries())) {
        logger.warn("Enabled agent skill SKILL.md not found in sandbox: skillId={}", skillId);
        continue;
      }
      String path = searchResult.getEntries().stream()
        .min(Comparator.comparingInt(String::length))
        .orElseThrow();
      SandboxFileReadResult readResult = sandboxClient.readFile(path);
      if (!readResult.isSuccess()) {
        logger.warn("Failed to read enabled agent skill: skillId={}, path={}", skillId, path);
        continue;
      }
      String baseDir = Strings.CS.removeEnd(path, "SKILL.md");
      AgentSkillSpec spec = AgentSkillMarkdownParser.parseSkill(baseDir, readResult.getContent());
      if (StringUtils.isEmpty(spec.getName()) || StringUtils.isEmpty(spec.getContent())) {
        logger.warn("Enabled agent skill skipped (empty name or content): skillId={}", skillId);
        continue;
      }
      skills.add(spec);
    }
    return skills;
  }

  /**
   * 从本地文件系统加载已启用的技能
   */
  private static List<AgentSkillSpec> loadEnabledAgentSkillSpecsFromLocal(List<SimpleAgentSkillDTO> enabledAgentSkills, String skillsDir) {
    List<AgentSkillSpec> skills = new ArrayList<>();
    for (SimpleAgentSkillDTO skill : enabledAgentSkills) {
      Long skillId = skill.getSkillId();
      String basePath = skillsDir + "/" + skillId;

      Collection<File> files = FileUtils.listFiles(new File(basePath), new NameFileFilter("SKILL.md"), TrueFileFilter.TRUE);
      if (files.isEmpty()) {
        continue;
      }
      File file = files.stream().findFirst().orElse(null);
      AgentSkillSpec spec = AgentSkillMarkdownParser.parseSkill(file.getParentFile().getAbsolutePath(), file);
      if (StringUtils.isEmpty(spec.getName()) || StringUtils.isEmpty(spec.getContent())) {
        logger.warn("Enabled agent skill skipped (empty name or content): skillId={}", skillId);
        continue;
      }
      skills.add(spec);
    }
    return skills;
  }

  /**
   * 检查沙箱中已存在的 Agent Skills 目录
   *
   * @return 技能目录列表
   */
  private static List<String> findExistingAgentSkills(boolean useSandbox, SandboxClient sandboxClient, String skillsDir) {
    if (useSandbox) {
      SandboxRunResult result = sandboxClient.execute(SandboxRunRequest.builder().command("find " + skillsDir + " -type d -maxdepth 1 -mindepth 1").build());
      Assert.isTrue(result.isSuccess(), () -> "检查 skills 目录失败: " + result.getErrorMessage());
      String stdout = StringUtils.defaultString(result.getStdout());
      if (!stdout.isEmpty()) {
        // 只保留目录名称为数字(skillId)的技能目录，忽略其它目录（可能是用户通过对话让大模型创建的，不能删除）
        return stdout.lines().filter(path -> StringUtils.isNumeric(StringUtils.substringAfterLast(path, '/'))).toList();
      }
    }
    else {
      File[] dirList = new File(skillsDir).listFiles(File::isDirectory);
      if (dirList != null && dirList.length > 0) {
        return Arrays.stream(dirList)
          .filter(f -> StringUtils.isNumeric(f.getName()))
          .map(File::getAbsolutePath)
          .toList();
      }
    }
    return List.of();
  }

  /**
   * 合并技能（以 SKILL.md 中 {@code name} 为键）：优先级由低到高依次为预置、沙箱全量扫描、用户启用列表显式加载。
   * <p>
   * 全量扫描可能包含对话生成的技能目录，顺序不定；仅依赖扫描会导致同名时误保留非启用条目。
   * 最后一档对同名键使用 {@link Map#put}，保证已启用技能始终覆盖预置与其它沙箱文件。
   * </p>
   */
  private static List<AgentSkillSpec> mergeAgentSkillsByPriority(List<AgentSkillSpec> presetSkills, List<AgentSkillSpec> sandboxScanSkills,
                                                                 List<AgentSkillSpec> enabledSkills) {
    Map<String, AgentSkillSpec> byName = new LinkedHashMap<>();
    for (AgentSkillSpec spec : presetSkills) {
      String name = spec.getName();
      if (StringUtils.isEmpty(name)) {
        continue;
      }
      byName.putIfAbsent(name, spec);
    }
    for (AgentSkillSpec spec : sandboxScanSkills) {
      String name = spec.getName();
      if (StringUtils.isEmpty(name)) {
        continue;
      }
      if (byName.putIfAbsent(name, spec) != null) {
        logger.debug("Agent skill name conflict: skip duplicate in sandbox scan, name={}", name);
      }
    }
    for (AgentSkillSpec spec : enabledSkills) {
      String name = spec.getName();
      if (StringUtils.isEmpty(name)) {
        continue;
      }
      AgentSkillSpec previous = byName.put(name, spec);
      if (previous != null) {
        logger.debug("Agent skill name conflict: prefer enabled agent skill over other source, name={}", name);
      }
    }
    return new ArrayList<>(byName.values());
  }

  /**
   * 下载 Agent Skills 并解压缩
   *
   * @return 技能目录列表
   */
  @SuppressFBWarnings("WEAK_MESSAGE_DIGEST_MD5")
  private static List<String> downloadAgentSkills(Long userId, List<SimpleAgentSkillDTO> agentSkills, boolean useSandbox, SandboxClient sandboxClient, String skillsDir) {
    if (agentSkills.isEmpty()) {
      return List.of();
    }

    if (!useSandbox) {
      // 并行下载
      return agentSkills.parallelStream().map(skill -> {
        String dir = skillsDir + "/" + skill.getSkillId();
        if (Boolean.TRUE.equals(skill.getDebug())) {
          downloadDebugSkill(dir, skill, false, null);
          return dir;
        }
        try (InputStream inputStream = getSkillFileStream(skill)) {
          ZipUtil.unzip(inputStream, Path.of(dir));
          return dir;
        }
        catch (Exception e) {
          logger.error("Failed to download agent skill: userId={}, skillId={}, fileId={}", userId, skill.getSkillId(), skill.getFileId(), e);
          throw new BssException("下载 Agent Skill 文件失败: skillId=" + skill.getSkillId(), e);
        }
      }).toList();
    }

    // 并行下载
    return agentSkills.parallelStream().map(skill -> {
      String dir = skillsDir + "/" + skill.getSkillId();
      if (Boolean.TRUE.equals(skill.getDebug())) {
        downloadDebugSkill(dir, skill, true, sandboxClient);
        return dir;
      }
      String filePath = skillsDir + "/" + skill.getSkillId() + ".zip";
      String checksumFilePath = filePath + ".md5sum";
      // 通过 md5 哈希快速检查文件是否变化，只在变化时重新下载，以避免重复下载、解压缩的开销
      SandboxFileReadResult readResult = sandboxClient.readFile(checksumFilePath);
      if (!readResult.isSuccess() || !skill.getMd5sum().equals(readResult.getContent())) {
        try (InputStream inputStream = getSkillFileStream(skill)) {
          SandboxFileWriteResult writeResult = sandboxClient.writeFile(filePath, inputStream);
          Assert.isTrue(writeResult.isSuccess(), () -> "同步 Agent Skill 文件失败: " + writeResult.getErrorMessage());
          sandboxClient.writeFile(checksumFilePath, skill.getMd5sum());
        }
        catch (Exception e) {
          logger.error("Failed to upload agent skill to sandbox: userId={}, skillId={}, fileId={}", userId, skill.getSkillId(), skill.getFileId(), e);
          throw new BssException("下载 Agent Skill 文件失败: skillId=" + skill.getSkillId(), e);
        }
      }

      // 解压缩
      SandboxRunResult decompressResult = sandboxClient.execute(SandboxRunRequest.builder().command("unzip -o " + filePath + " -d " + dir).build());
      Assert.isTrue(decompressResult.isSuccess(), () -> "解压缩 Agent Skill 压缩包失败: skill=" + skill.getSkillName() + ", error=" + decompressResult.getErrorMessage());
      return dir;
    }).toList();
  }

  /**
   * 下载调试的技能
   */
  private static void downloadDebugSkill(String targetDir, SimpleAgentSkillDTO skill, boolean useSandbox, @Nullable SandboxClient sandboxClient) {
    ResultVO<List<AgentSkillTreeNodeDTO>> queryTreeResult = agentSkillManageService.queryAgentSkillTree(skill.getTenantId(), skill.getSkillId());
    Assert.isTrue(queryTreeResult.isSuccess(), () -> "查询待调试技能失败: " + queryTreeResult.getResultMsg());
    List<AgentSkillTreeNodeDTO> rootNodes = queryTreeResult.getResultObject();
    Assert.notEmpty(rootNodes, "未查到待调试技能的文件");

    // 沙箱
    if (useSandbox) {
      assert sandboxClient != null;
      downloadDebugSkillFilesToSandbox(skill.getTenantId(), sandboxClient, targetDir, rootNodes);
    }
    // 本地目录
    else {
      downloadDebugSkillFilesToLocal(skill.getTenantId(), new File(targetDir), rootNodes);
    }
  }

  /**
   * 下载调试技能的所有文件到本地目录
   */
  private static void downloadDebugSkillFilesToLocal(Long tenantId, File parentDir, List<AgentSkillTreeNodeDTO> nodes) {
    for (AgentSkillTreeNodeDTO node : nodes) {
      File nodeFile = new File(parentDir, node.getNodeName());
      if ("file".equals(node.getNodeType())) {
        ResultVO<AgentSkillFileDTO> result = agentSkillManageService.getAgentSkillFileContent(tenantId, node.getNodeId());
        Assert.isTrue(result.isSuccess(), () -> "查询待调试技能的文件失败: file=" + node.getNodeName() + ", error=" + result.getResultMsg());
        try {
          // FileUtils 会自动创建不存在的目录
          FileUtils.writeStringToFile(nodeFile, result.getResultObject().getFileContent(), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
          logger.error("Failed to write file: tenantId={}, nodeId={}, file={}", tenantId, node.getNodeId(), nodeFile);
          throw new BssException("写入文件失败: " + ExpUtil.getMsg(e), e);
        }
      }
      else if ("dir".equals(node.getNodeType()) && CollectionUtils.isNotEmpty(node.getChildren())) {
        downloadDebugSkillFilesToLocal(tenantId, nodeFile, node.getChildren());
      }
    }
  }

  /**
   * 下载调试技能的所有文件到沙箱中的目录
   */
  private static void downloadDebugSkillFilesToSandbox(Long tenantId, SandboxClient sandboxClient, String parentDir, List<AgentSkillTreeNodeDTO> nodes) {
    // 创建目录
    SandboxRunResult createDirResult = sandboxClient.execute(SandboxRunRequest.builder().command("mkdir -p " + parentDir).build());
    Assert.isTrue(createDirResult.isSuccess(), () -> "创建沙箱目录失败: " + createDirResult.getErrorMessage());

    for (AgentSkillTreeNodeDTO node : nodes) {
      // 沙箱固定是 Linux 系统，路径分隔符使用 /
      String nodePath = parentDir + "/" + node.getNodeName();
      if ("file".equals(node.getNodeType())) {
        ResultVO<AgentSkillFileDTO> result = agentSkillManageService.getAgentSkillFileContent(tenantId, node.getNodeId());
        Assert.isTrue(result.isSuccess(), () -> "查询待调试技能的文件失败: file=" + node.getNodeName() + ", error=" + result.getResultMsg());
        SandboxFileWriteResult writeFileResult = sandboxClient.writeFile(nodePath, result.getResultObject().getFileContent());
        Assert.isTrue(writeFileResult.isSuccess(), () -> "写入沙箱文件失败: " + writeFileResult.getErrorMessage());
      }
      else if ("dir".equals(node.getNodeType()) && CollectionUtils.isNotEmpty(node.getChildren())) {
        downloadDebugSkillFilesToSandbox(tenantId, sandboxClient, nodePath, node.getChildren());
      }
    }
  }

  /**
   * 获取 Agent Skill 文件流
   */
  private static InputStream getSkillFileStream(SimpleAgentSkillDTO skill) throws IOException {
    if (skill.getResource() != null) {
      return skill.getResource().getInputStream();
    }
    return fileStoreService.downloadFileStreamFromCache(skill.getFileInfo());
  }

}
