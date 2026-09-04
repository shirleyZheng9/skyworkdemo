package com.iwhalecloud.bote.service.skill.security;

import com.iwhalecloud.bote.dto.skill.AgentSkillDirDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillFileDTO;
import com.iwhalecloud.bote.dto.skill.SecurityLineRuleDTO;
import com.iwhalecloud.bote.dto.skill.SecuritySourceRuleDTO;
import com.iwhalecloud.bote.mapper.skill.AgentSkillDirMapper;
import com.iwhalecloud.bote.mapper.skill.AgentSkillFileMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Agent Skill 发布前静态代码安全扫描，规则对齐 OpenClaw {@code src/security/skill-scanner.ts}
 *
 * @author chen.linfa
 * @since 2026-05-09
 */
@Component
@RequiredArgsConstructor
public class AgentSkillSecurityScanner {

  private final AgentSkillDirMapper agentSkillDirMapper;

  private final AgentSkillFileMapper agentSkillFileMapper;

  private static final Set<String> TEST_DIRECTORY_NAMES = Set.of("__fixtures__", "__mocks__", "__tests__", "test", "tests");

  private static final Pattern TEST_FILE_NAME_PATTERN = Pattern.compile("\\.(?:mock|spec|test)\\.[^.]+$", Pattern.CASE_INSENSITIVE);

  /** 与 OpenClaw {@code DEFAULT_MAX_SCAN_FILES} 一致 */
  private static final int DEFAULT_MAX_SCAN_FILES = 500;

  /** 与 OpenClaw {@code DEFAULT_MAX_FILE_BYTES} 一致 */
  private static final int DEFAULT_MAX_FILE_BYTES = 1024 * 1024;

  private static final Set<Integer> STANDARD_PORTS = Set.of(80, 443, 8080, 8443, 3000);

  private static final Pattern CHILD_PROCESS_PATTERN = Pattern.compile("child_process");

  private static final Pattern NETWORK_SEND_CONTEXT_PATTERN = Pattern.compile(
    "\\bfetch\\s*\\(|\\bpost\\s*\\(|\\.\\s*post\\s*\\(|http\\.request\\s*\\(", Pattern.CASE_INSENSITIVE);

  private static final Pattern BENIGN_CHILD_PROCESS_EXEC_PATTERN = Pattern.compile("\\b(?:cp|childProcess|child_process)\\s*\\.\\s*exec\\s*\\(");

  // @formatter:off
  private static final List<SecurityLineRuleDTO> LINE_RULES = List.of(
    SecurityLineRuleDTO.builder()
      .ruleId("dangerous-exec")
      .message("Shell command execution detected (child_process)")
      .pattern(Pattern.compile("\\b(exec|execSync|spawn|spawnSync|execFile|execFileSync)\\s*\\("))
      .requiresContext(CHILD_PROCESS_PATTERN)
      .build(),

    SecurityLineRuleDTO.builder()
      .ruleId("dynamic-code-execution")
      .message("Dynamic code execution detected")
      .pattern(Pattern.compile("\\beval\\s*\\(|new\\s+Function\\s*\\("))
      .build(),

    SecurityLineRuleDTO.builder()
      .ruleId("crypto-mining")
      .message("Possible crypto-mining reference detected")
      .pattern(Pattern.compile("stratum\\+tcp|stratum\\+ssl|coinhive|cryptonight|xmrig", Pattern.CASE_INSENSITIVE))
      .build(),

    SecurityLineRuleDTO.builder()
      .ruleId("suspicious-network")
      .message("WebSocket connection to non-standard port")
      .pattern(Pattern.compile("new\\s+WebSocket\\s*\\(\\s*[\"']wss?://[^\"']*:(\\d+)"))
      .build());

  private static final List<SecuritySourceRuleDTO> SOURCE_RULES = List.of(
    SecuritySourceRuleDTO.builder()
      .ruleId("potential-exfiltration")
      .message("File read combined with network send — possible data exfiltration")
      .pattern(Pattern.compile("readFileSync|readFile"))
      .requiresContext(NETWORK_SEND_CONTEXT_PATTERN)
      .build(),

    SecuritySourceRuleDTO.builder()
      .ruleId("obfuscated-code")
      .message("Hex-encoded string sequence detected (possible obfuscation)")
      .pattern(Pattern.compile("(?:\\\\x[0-9a-fA-F]{2}){6,}"))
      .build(),

    SecuritySourceRuleDTO.builder()
      .ruleId("obfuscated-code")
      .message("Large base64 payload with decode call detected (possible obfuscation)")
      .pattern(Pattern.compile("(?:atob|Buffer\\.from)\\s*\\(\\s*[\"'][A-Za-z0-9+/=]{200,}[\"']"))
      .build(),

    SecuritySourceRuleDTO.builder()
      .ruleId("env-harvesting")
      .message("Environment variable access combined with network send — possible credential harvesting")
      .pattern(Pattern.compile("process\\.env"))
      .requiresContext(NETWORK_SEND_CONTEXT_PATTERN)
      .build());
  // @formatter:on

  public String scan(Long tenantId, Long skillId) {
    List<AgentSkillFileDTO> files = agentSkillFileMapper.selectActiveBySkillId(tenantId, skillId);
    List<AgentSkillDirDTO> dirs = agentSkillDirMapper.listActiveBySkillId(tenantId, skillId);

    List<String> illegalMsgs = new ArrayList<>();
    collectIllegalDirNames(dirs, illegalMsgs);
    scanFileContent(files, illegalMsgs);
    return CollectionUtils.isEmpty(illegalMsgs) ? null : StringUtils.join(illegalMsgs, ",");
  }

  private void collectIllegalDirNames(List<AgentSkillDirDTO> dirs, List<String> illegalMsgs) {
    List<String> dirNames = new ArrayList<>();
    for (AgentSkillDirDTO dir : CollectionUtils.emptyIfNull(dirs)) {
      if (Objects.equals(-1L, dir.getParentDirId())) {
        continue;
      }
      if (!scanRelativePath(dir.getDirName())) {
        dirNames.add(dir.getDirName());
      }
    }
    if (CollectionUtils.isNotEmpty(dirNames)) {
      illegalMsgs.add("非法目录：" + StringUtils.join(dirNames, ","));
    }
  }

  /**
   * 是否应对路径做扫描（排除测试目录、隐藏路径、node_modules 等，对齐 OpenClaw 目录遍历过滤）。
   */
  private boolean scanRelativePath(String dirName) {
    if (dirName.startsWith(".") || "node_modules".equals(dirName)) {
      return false;
    }
    if (TEST_DIRECTORY_NAMES.contains(dirName)) {
      return false;
    }
    return !TEST_FILE_NAME_PATTERN.matcher(dirName).find();
  }

  private void scanFileContent(List<AgentSkillFileDTO> files, List<String> illegalMsgs) {
    if (files.size() > DEFAULT_MAX_SCAN_FILES) {
      illegalMsgs.add("文件个数超过最大限制[" + DEFAULT_MAX_SCAN_FILES + "]");
      return;
    }
    for (AgentSkillFileDTO file : files) {
      if (StringUtils.isEmpty(file.getFileContent())) {
        continue;
      }
      if (file.getFileContent().length() > DEFAULT_MAX_FILE_BYTES) {
        illegalMsgs.add("文件[" + file.getFileName() + "]的内容大小超过最大限制");
      }
      scanSource(file.getFileName(), file.getFileContent(), illegalMsgs);
    }
  }

  private void scanSource(String fileName, String fileContent, List<String> illegalMsgs) {
    String[] lines = fileContent.split("\n", -1);
    String heuristicSource = HeuristicStripper.strip(fileContent);
    String[] heuristicLines = heuristicSource.split("\n", -1);
    collectLineRuleViolations(fileName, fileContent, lines, illegalMsgs);
    collectSourceRuleViolations(fileName, heuristicSource, heuristicLines, illegalMsgs);
  }

  private void collectLineRuleViolations(String fileName, String fileContent, String[] lines, List<String> illegalMsgs) {
    for (SecurityLineRuleDTO rule : LINE_RULES) {
      if (!lineRuleContextSatisfied(rule, fileContent)) {
        continue;
      }
      String violation = findFirstLineRuleViolation(rule, lines);
      if (violation != null) {
        illegalMsgs.add("文件[" + fileName + "]内容不合规：" + violation);
      }
    }
  }

  private boolean lineRuleContextSatisfied(SecurityLineRuleDTO rule, String fileContent) {
    Pattern ctx = rule.getRequiresContext();
    return ctx == null || ctx.matcher(fileContent).find();
  }

  @Nullable
  private String findFirstLineRuleViolation(SecurityLineRuleDTO rule, String[] lines) {
    Pattern pattern = rule.getPattern();
    String message = null;
    for (String line : lines) {
      Matcher matcher = pattern.matcher(line);
      if (!matcher.find()) {
        continue;
      }
      if (shouldSkipLineRuleMatch(rule, line, matcher)) {
        continue;
      }
      if (message == null) {
        message = rule.getMessage();
      }
    }
    return message;
  }

  private boolean shouldSkipLineRuleMatch(SecurityLineRuleDTO rule, String line, Matcher matcher) {
    if ("dangerous-exec".equals(rule.getRuleId()) && isBenignMemberExecMatch(line, matcher)) {
      return true;
    }
    return "suspicious-network".equals(rule.getRuleId()) && isBenignWebSocketPort(matcher);
  }

  private boolean isBenignWebSocketPort(Matcher matcher) {
    try {
      int port = Integer.parseInt(matcher.group(1));
      return STANDARD_PORTS.contains(port);
    }
    catch (NumberFormatException ignored) {
      return true;
    }
  }

  private void collectSourceRuleViolations(String fileName, String heuristicSource, String[] heuristicLines, List<String> illegalMsgs) {
    for (SecuritySourceRuleDTO rule : SOURCE_RULES) {
      Pair<Integer, String> match = findSourceRuleMatch(rule, heuristicSource, heuristicLines);
      if (match == null) {
        continue;
      }
      illegalMsgs.add("文件[" + fileName + "]内容不合规：" + "第" + match.getLeft() + "行，" + match.getRight());
    }
  }

  private boolean isBenignMemberExecMatch(String line, Matcher match) {
    String command = match.group(1);
    if (!"exec".equals(command)) {
      return false;
    }
    int idx = match.start();
    if (idx <= 0 || line.charAt(idx - 1) != '.') {
      return false;
    }
    return !BENIGN_CHILD_PROCESS_EXEC_PATTERN.matcher(line).find();
  }

  @Nullable
  private Pair<Integer, String> findSourceRuleMatch(SecuritySourceRuleDTO rule, String source, String[] heuristicLines) {
    if (!sourceRulePreconditionsMet(rule, source)) {
      return null;
    }
    Integer window = rule.getRequiresContextWindowLines();
    Pair<Integer, String> lineHit = findSourceRuleLineHit(rule, heuristicLines, window);
    if (lineHit != null) {
      return lineHit;
    }
    if (window != null) {
      return null;
    }
    return Pair.of(1, source.substring(0, Math.min(120, source.length())));
  }

  private boolean sourceRulePreconditionsMet(SecuritySourceRuleDTO rule, String source) {
    if (!rule.getPattern().matcher(source).find()) {
      return false;
    }
    Pattern ctx = rule.getRequiresContext();
    return ctx == null || ctx.matcher(source).find();
  }

  @Nullable
  private Pair<Integer, String> findSourceRuleLineHit(SecuritySourceRuleDTO rule, String[] heuristicLines, @Nullable Integer window) {
    Pattern linePattern = rule.getPattern();
    Pattern ctx = rule.getRequiresContext();
    Pair<Integer, String> hit = null;
    for (int i = 0; i < heuristicLines.length; i++) {
      String line = heuristicLines[i];
      if (!linePattern.matcher(line).find()) {
        continue;
      }
      if (ctx != null && window != null && !contextMatchesInWindow(ctx, heuristicLines, i, window)) {
        continue;
      }
      if (hit == null) {
        hit = Pair.of(i + 1, line);
      }
    }
    return hit;
  }

  private boolean contextMatchesInWindow(Pattern ctx, String[] heuristicLines, int lineIndex, int window) {
    int start = Math.max(0, lineIndex - window);
    int end = Math.min(heuristicLines.length, lineIndex + window + 1);
    String windowSource = String.join("\n", Arrays.copyOfRange(heuristicLines, start, end));
    return ctx.matcher(windowSource).find();
  }
}
