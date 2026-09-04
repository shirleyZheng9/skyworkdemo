package com.iwhalecloud.bote.service.skill.support;

import com.iwhalecloud.bote.agent.skill.AgentSkillMarkdownParser;
import com.iwhalecloud.bote.agent.skill.AgentSkillMarkdownParser.SkillMdParseResult;
import com.iwhalecloud.bote.agent.skill.AgentSkillSpec;
import com.iwhalecloud.bote.common.consts.SkillSquareConsts;
import com.iwhalecloud.bote.common.util.ZipUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Agent Skill 压缩包校验
 *
 * @author skill-square
 * @since 2026-03-21
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class AgentSkillZipSupport {
  private static final Logger logger = LoggerFactory.getLogger(AgentSkillZipSupport.class);
  /** 技能名称校验规则 */
  public static final Pattern SKILL_NAME_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9\\-]{0,63}$");

  private AgentSkillZipSupport() {
  }

  /**
   * 解压到临时目录并做完整校验
   *
   * @return 校验结果，成功时返回 null
   */
  @Nullable
  public static <T> ResultVO<T> validateAgentSkillFile(InputStream inputStream) {
    File tmpDir;
    try {
      tmpDir = Files.createTempDirectory("check-agent-skill-file-").toFile();
    }
    catch (IOException e) {
      logger.error("Failed to create temp directory", e);
      return ResultVO.fail("创建临时目录失败: " + e.getMessage());
    }
    try {
      return validateAgentSkillFile(inputStream, tmpDir);
    }
    finally {
      FileUtils.deleteQuietly(tmpDir);
    }
  }

  /**
   * 解压到指定目录并做完整校验
   *
   * @return 校验结果，成功时返回 null
   */
  @Nullable
  public static <T> ResultVO<T> validateAgentSkillFile(InputStream inputStream, File targetDir) {
    try {
      List<File> topLevelFiles = ZipUtil.unzip(inputStream, targetDir.toPath());
      Assert.notEmpty(topLevelFiles, "压缩包为空");
      validateAgentSkill(targetDir);
      return null;
    }
    catch (BssException | IllegalArgumentException e) {
      return ResultVO.fail(e);
    }
    catch (Exception e) {
      logger.error("Failed to validate agent skill file", e);
      return ResultVO.fail("校验 Agent Skill 文件失败: " + e.getMessage());
    }
  }

  /**
   * 解压到指定目录并做完整校验，返回 SKILL.md 路径与单次解析结果供导入落库复用。
   */
  public static ResultVO<AgentSkillZipValidated> validateAndPareAgentSkillFile(InputStream inputStream, File targetDir) {
    try {
      List<File> topLevelFiles = ZipUtil.unzip(inputStream, targetDir.toPath());
      Assert.notEmpty(topLevelFiles, "压缩包为空");
      File skillMdFile = resolveValidatedSkillMdFile(targetDir);
      String markdown = readSkillMdAsString(skillMdFile);
      // 解析 SKILL.md 文件内容
      SkillMdParseResult parsed = AgentSkillMarkdownParser.parseSkill(markdown);
      // 对解析结果进行规则校验
      assertSkillMdBusinessRules(parsed.metadata(), parsed.bodyContent());
      return ResultVO.success(new AgentSkillZipValidated(skillMdFile, parsed.metadata(), parsed.bodyContent(), parsed.normalizedMarkdown()));
    }
    catch (BssException | IllegalArgumentException e) {
      return ResultVO.fail(e);
    }
    catch (Exception e) {
      logger.error("Failed to validate agent skill file", e);
      return ResultVO.fail("校验 Agent Skill 文件失败: " + e.getMessage());
    }
  }

  /**
   * 从 zip 内容解析并校验 Agent Skill
   */
  public static AgentSkillSpec parseStandardLayout(byte[] zipBytes) {
    File tmpDir;
    try {
      tmpDir = Files.createTempDirectory("skill-square-agent-layout-").toFile();
    }
    catch (IOException e) {
      throw new IllegalArgumentException("创建临时目录失败: " + e.getMessage(), e);
    }

    try {
      List<File> topLevelFiles = ZipUtil.unzip(new ByteArrayInputStream(zipBytes), tmpDir.toPath());
      Assert.notEmpty(topLevelFiles, "压缩包为空");
      File skillFile = validateAgentSkill(tmpDir);
      return AgentSkillMarkdownParser.parseSkill("", skillFile);
    }
    catch (BssException | IllegalArgumentException e) {
      throw e;
    }
    catch (Exception e) {
      throw new IllegalArgumentException("解析 Agent Skill 压缩包失败: " + e.getMessage(), e);
    }
    finally {
      FileUtils.deleteQuietly(tmpDir);
    }
  }

  /**
   * 校验解压缩后的内容
   *
   * @param unzipRoot 解压根目录
   * @return SKILL.md 文件
   */
  private static File validateAgentSkill(File unzipRoot) {
    File skillFile = resolveValidatedSkillMdFile(unzipRoot);
    validateSkillFile(readSkillMdAsString(skillFile));
    return skillFile;
  }

  /**
   * 在解压根目录下定位并校验 SKILL.md 路径约束，返回 SKILL.md 文件。
   *
   * @param unzipRoot 解压根目录
   * @return 通过布局校验的 SKILL.md 文件
   */
  private static File resolveValidatedSkillMdFile(File unzipRoot) {
    Collection<File> skillFiles = FileUtils.listFiles(unzipRoot, new NameFileFilter(SkillSquareConsts.ZIP_FILE_SKILL_MD), TrueFileFilter.TRUE);
    Assert.notEmpty(skillFiles, "缺少 SKILL.md 文件，支持放在根目录或第一级子目录");
    // 不能有多个 SKILL.md 文件
    if (skillFiles.size() > 1) {
      Path baseDir = unzipRoot.toPath();
      String filePaths = skillFiles.stream()
        // 报错信息中固定使用 / 作为路径分隔符
        .map(f -> baseDir.relativize(f.toPath()).toString().replace(File.separatorChar, '/'))
        .collect(Collectors.joining(", "));
      throw new IllegalArgumentException("不能包含多个 SKILL.md 文件: " + filePaths);
    }

    // 必须放在根目录或第一级子目录
    File skillFile = skillFiles.iterator().next();
    String relativePath = unzipRoot.toPath().relativize(skillFile.toPath()).toString().replace(File.separatorChar, '/');
    int skillFileLevel = StringUtils.countMatches(relativePath, '/');
    if (skillFileLevel > 1) {
      throw new IllegalArgumentException("SKILL.md 必须放在根目录或者第一级子目录: " + relativePath);
    }
    if (skillFileLevel == 1) {
      File[] topLevelFiles = unzipRoot.listFiles();
      Assert.isTrue(topLevelFiles != null && topLevelFiles.length == 1, "SKILL.md 放在第一级子目录时，压缩包根目录下只能有一个子目录");
    }
    return skillFile;
  }

  /**
   * 读取 SKILL.md 全文为字符串
   */
  private static String readSkillMdAsString(File skillFile) {
    try {
      return FileUtils.readFileToString(skillFile, StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      logger.warn("Failed to read SKILL.md: file={}", skillFile.getAbsolutePath(), e);
      throw new BssException("读取 SKILL.md 失败: " + skillFile.getAbsolutePath(), e);
    }
  }

  /**
   * 校验 SKILL.md 文件内容
   */
  public static void validateSkillFile(String markdown) {
    Assert.hasText(markdown, "SKILL.md 文件内容不能为空");
    validateSkillMdParsed(AgentSkillMarkdownParser.parseSkill(markdown));
  }

  /**
   * 对解析结果执行业务规则校验。
   *
   * @param parsed 单次解析结果
   */
  public static void validateSkillMdParsed(SkillMdParseResult parsed) {
    assertSkillMdBusinessRules(parsed.metadata(), parsed.bodyContent());
  }

  /**
   * 基于元数据与正文，校验 SKILL.md 业务规则（与 {@link #validateSkillFile} 中规则一致）。
   *
   * @param metadata   front matter 键值
   * @param bodyContent 正文
   */
  private static void assertSkillMdBusinessRules(Map<String, String> metadata, String bodyContent) {
    String name = MapUtils.getString(metadata, "name");
    String description = MapUtils.getString(metadata, "description");
    Assert.hasLength(name, "SKILL.md 中的元数据缺少 name");
    Assert.hasLength(description, "SKILL.md 中的元数据缺少 description");
    Assert.isTrue(SKILL_NAME_PATTERN.matcher(name).matches(),
      "SKILL.md 元数据中的 name 不合法，必须在 1 到 64 个字符之间，只能包含小写字母、数字、连字符(-)，且不能以连字符开头");
    Assert.isTrue(description.length() <= 1024, "SKILL.md 元数据中的 description 不合法，不能超过 1024 个字符");
    Assert.hasLength(bodyContent, "SKILL.md 正文内容不能为空");
  }

  /**
   * 查找 SKILL.md 文件
   */
  @Nullable
  public static File findSkillFile(File unzipRoot) {
    Collection<File> skillFiles = FileUtils.listFiles(unzipRoot, new NameFileFilter(SkillSquareConsts.ZIP_FILE_SKILL_MD), TrueFileFilter.TRUE);
    if (skillFiles.size() != 1) {
      return null;
    }
    return skillFiles.iterator().next();
  }

  /**
   * 解压并校验通过后得到的 SKILL.md 路径与单次解析结果，供导入落库复用。
   *
   * @author qian.sisheng
   * @since 2026-05-06
   */
  @Getter
  @ToString
  @RequiredArgsConstructor
  public static class AgentSkillZipValidated {
    /** SKILL.md 文件 */
    private final File skillMdFile;
    /** front matter 元数据 */
    private final Map<String, String> metadata;
    /** 正文 */
    private final String bodyContent;
    /** 规范化后的 SKILL.md 全文 */
    private final String normalizedMarkdown;
  }
}
