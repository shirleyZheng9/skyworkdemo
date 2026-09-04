package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.skill.SimpleAgentSkillDTO;
import com.iwhalecloud.bote.dto.skill.SimpleAgentSkillToolDTO;
import com.iwhalecloud.bote.mapper.skill.AgentSkillMapper;
import com.iwhalecloud.bote.mapper.skill.AgentSkillToolMapper;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Agent Skill 缓存
 *
 * <p>缓存 key 为 tenantId:skillId</p>
 *
 * @author bianjp
 * @since 2025-02-03
 */
@Component
public final class AgentSkillCache extends AbstractSkillCache<SimpleAgentSkillDTO> {
  private final AgentSkillMapper agentSkillMapper;
  private final AgentSkillToolMapper agentSkillToolMapper;
  private final IFileStoreService fileStoreService;

  public AgentSkillCache(AgentSkillMapper agentSkillMapper, AgentSkillToolMapper agentSkillToolMapper, IFileStoreService fileStoreService) {
    super(CacheConsts.KEY_PREFIX_AGENT_SKILL);
    this.agentSkillMapper = agentSkillMapper;
    this.agentSkillToolMapper = agentSkillToolMapper;
    this.fileStoreService = fileStoreService;
    disableDistributionCache();
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_AGENT_SKILL;
  }

  @Nullable
  @Override
  protected SimpleAgentSkillDTO loadById(Long tenantId, Long id) {
    SimpleAgentSkillDTO skill = agentSkillMapper.selectSimpleAgentSkillById(tenantId, id);
    if (skill != null) {
      loadFileInfo(skill);
      // 查询技能关联的工具，只有租户级技能才会关联工具
      if (!BaseConsts.PLATFORM_TENANT_ID.equals(skill.getTenantId())) {
        skill.setTools(agentSkillToolMapper.selectToolsBySkillId(tenantId, id));
      }
    }
    return skill;
  }

  @Override
  protected Map<Long, SimpleAgentSkillDTO> loadByIds(Long tenantId, List<Long> ids) {
    List<SimpleAgentSkillDTO> skills = agentSkillMapper.selectSimpleAgentSkillsByIds(tenantId, ids);
    Map<Long, SimpleAgentSkillDTO> map = skills.stream()
      .collect(Collectors.toMap(SimpleAgentSkillDTO::getSkillId, Function.identity()));
    skills.parallelStream().forEach(this::loadFileInfo);

    // 查询技能关联的工具，只有租户级技能才会关联工具
    List<SimpleAgentSkillDTO> tenantSkills = skills.stream().filter(skill -> !BaseConsts.PLATFORM_TENANT_ID.equals(skill.getTenantId())).toList();
    if (!tenantSkills.isEmpty()) {
      List<Long> tenantSKillIds = tenantSkills.stream().map(SimpleAgentSkillDTO::getSkillId).toList();
      List<SimpleAgentSkillToolDTO> tools = agentSkillToolMapper.selectToolsBySkillIds(tenantId, tenantSKillIds);
      for (SimpleAgentSkillDTO skill : tenantSkills) {
        skill.setTools(tools.stream().filter(tool -> tool.getSkillId().equals(skill.getSkillId())).toList());
      }
    }

    return map;
  }

  /**
   * 加载技能的文件信息
   */
  @SuppressFBWarnings("WEAK_MESSAGE_DIGEST_MD5")
  @SuppressWarnings("PMD.GuardLogStatement")
  private void loadFileInfo(SimpleAgentSkillDTO skill) {
    // 平台级技能从 classpath 中加载
    if (BaseConsts.PLATFORM_TENANT_ID.equals(skill.getTenantId())) {
      String filePath = "META-INF/resources/skills/" + skill.getSkillCode() + ".zip";
      ClassPathResource resource = new ClassPathResource(filePath);
      if (resource.exists()) {
        skill.setResource(resource);
        // 计算文件的 MD5 校验和
        try (InputStream inputStream = resource.getInputStream()) {
          skill.setMd5sum(DigestUtils.md5Hex(inputStream));
        }
        catch (Exception e) {
          logger.error("Failed to calculate agent skill md5sum: skillId={}, filePath={}", skill.getSkillId(), filePath, e);
        }
        return;
      }
    }

    // 租户级技能从文件服务器加载
    if (skill.getFileId() == null) {
      return;
    }
    FileInfoVO fileInfo = fileStoreService.getFileInfoById(skill.getFileId());
    if (fileInfo == null) {
      return;
    }
    skill.setFileInfo(fileInfo);
    // 计算文件的 MD5 校验和
    try (InputStream inputStream = fileStoreService.downloadFileStreamFromCache(fileInfo)) {
      skill.setMd5sum(DigestUtils.md5Hex(inputStream));
    }
    catch (Exception e) {
      logger.error("Failed to calculate agent skill md5sum: skillId={}, fileId={}", skill.getSkillId(), skill.getFileId(), e);
    }
  }
}
