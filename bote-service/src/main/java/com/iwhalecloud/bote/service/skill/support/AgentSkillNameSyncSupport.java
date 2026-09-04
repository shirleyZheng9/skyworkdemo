package com.iwhalecloud.bote.service.skill.support;

import com.iwhalecloud.bote.agent.skill.AgentSkillMarkdownParser;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillFileDTO;
import com.iwhalecloud.bote.mapper.base.FileInfoManageMapper;
import com.iwhalecloud.bote.mapper.skill.AgentSkillFileMapper;
import com.iwhalecloud.bote.mapper.skill.AgentSkillMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Agent Skill 名称回填与目录对齐支撑
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
@Component
@RequiredArgsConstructor
public class AgentSkillNameSyncSupport {
  private static final Logger logger = LoggerFactory.getLogger(AgentSkillNameSyncSupport.class);

  private final AgentSkillMapper agentSkillMapper;
  private final AgentSkillFileMapper agentSkillFileMapper;
  private final FileInfoManageMapper fileInfoManageMapper;
  private final IFileStoreService fileStoreService;

  /**
   * 仅回填 skillFileName，不修改目录和 SKILL.md 内容。
   */
  public void fillSkillFileNameOnly(AgentSkillDTO skill) {
    if (skill == null || StringUtils.isNotBlank(skill.getSkillFileName())) {
      return;
    }
    String parsedName = extractSkillName(skill.getTenantId(), skill.getSkillId(), skill.getFileInfoId(), skill.getFileId());
    if (StringUtils.isBlank(parsedName)) {
      return;
    }
    agentSkillMapper.updateAgentSkillFileName(skill.getTenantId(), skill.getSkillId(), parsedName, SessionUtil.getOptionalUserId());
    skill.setSkillFileName(parsedName);
  }

  /**
   * 从数据库、SKILL.md 文件中提取 skill 名称。
   */
  @Nullable
  private String extractSkillName(Long tenantId, Long skillId, Long fileInfoId, Long fileId) {
    String nameFromDb = extractSkillNameInDb(tenantId, skillId);
    if (StringUtils.isNotBlank(nameFromDb)) {
      return nameFromDb;
    }
    if (fileInfoId == null || fileId == null) {
      return null;
    }
    return extractSkillNameByZip(tenantId, fileInfoId);
  }

  /**
   * 从数据库中提取 skill 名称。
   */
  @Nullable
  private String extractSkillNameInDb(Long tenantId, Long skillId) {
    AgentSkillFileDTO skillMdFile = agentSkillFileMapper.selectSkillMdBySkillId(tenantId, skillId);
    if (skillMdFile == null || StringUtils.isBlank(skillMdFile.getFileContent())) {
      return null;
    }
    Pair<Map<String, String>, String> parseResult = AgentSkillMarkdownParser.parse(skillMdFile.getFileContent());
    return MapUtils.getString(parseResult.getLeft(), "name");
  }

  /**
   * 从 SKILL.md 文件中提取 skill 名称。
   */
  @Nullable
  private String extractSkillNameByZip(Long tenantId, Long fileInfoId) {
    FileInfoDTO fileInfo = fileInfoManageMapper.getFileInfo(tenantId, fileInfoId);
    if (fileInfo == null || fileInfo.getFileId() == null) {
      return null;
    }
    FileInfoVO vo = fileStoreService.getFileInfoById(fileInfo.getFileId());
    if (vo == null) {
      return null;
    }
    Path tmpZip = null;
    try (InputStream inputStream = fileStoreService.downloadFileStreamFromCache(vo)) {
      tmpZip = Files.createTempFile("skill-name-", ".zip");
      Files.copy(inputStream, tmpZip, StandardCopyOption.REPLACE_EXISTING);
      return parseSkillNameFromZip(tmpZip);
    }
    catch (Exception e) {
      logger.error("extract SKILL.md name from zip failed, tenantId={}, fileInfoId={}", tenantId, fileInfoId, e);
      return null;
    }
    finally {
      if (tmpZip != null) {
        FileUtils.deleteQuietly(tmpZip.toFile());
      }
    }
  }

  /**
   * 从 zip 文件中解析 skill 名称。
   */
  private String parseSkillNameFromZip(Path zipPath) {
    try {
      byte[] zipBytes = Files.readAllBytes(zipPath);
      return StringUtils.trimToNull(AgentSkillZipSupport.parseStandardLayout(zipBytes).getName());
    }
    catch (IOException e) {
      logger.error("read zip failed: {}", zipPath, e);
      throw new BssException("读取 zip 失败: " + e.getMessage(), e);
    }
  }
}
