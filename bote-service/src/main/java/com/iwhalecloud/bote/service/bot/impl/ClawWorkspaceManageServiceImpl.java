package com.iwhalecloud.bote.service.bot.impl;

import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.bot.ClawWorkspaceDTO;
import com.iwhalecloud.bote.mapper.bot.ClawWorkspaceManageMapper;
import com.iwhalecloud.bote.service.bot.IClawWorkspaceManageService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.stereotype.Service;

/**
 * claw 工作空间管理服务
 *
 * @author chen.linfa
 * @since 2026-04-23
 */
@Service
@RequiredArgsConstructor
public class ClawWorkspaceManageServiceImpl implements IClawWorkspaceManageService {

  private final ClawWorkspaceManageMapper workspaceManageMapper;

  @Override
  public void fillWorkspace(BotSceneDTO scene) {
    List<ClawWorkspaceDTO> workspaces = workspaceManageMapper.selectClawWorkspaceList(scene.getTenantId(), scene.getSceneId(),
      SceneConsts.PROMPT_FILES);
    if (CollectionUtils.isEmpty(workspaces)) {
      workspaces = new ArrayList<>();
    }
    setFileContent(workspaces, SceneConsts.PROMPT_FILE_AGENT, scene.getAgentPrompt(), scene.getTenantId(), scene.getSceneId());
    setFileContent(workspaces, SceneConsts.PROMPT_FILE_PROFILE, scene.getProfilePrompt(), scene.getTenantId(), scene.getSceneId());
    setFileContent(workspaces, SceneConsts.PROMPT_FILE_SOUL, scene.getSoulPrompt(), scene.getTenantId(), scene.getSceneId());
    scene.setWorkspaces(workspaces);
  }

  @Override
  public void flatWorkspace(BotSceneDTO scene) {
    List<ClawWorkspaceDTO> workspaces = workspaceManageMapper.selectClawWorkspaceList(scene.getTenantId(), scene.getSceneId(),
      SceneConsts.PROMPT_FILES);
    if (CollectionUtils.isEmpty(workspaces)) {
      return;
    }
    scene.setWorkspaces(workspaces);

    ClawWorkspaceDTO agent = IterableUtils.find(workspaces, p -> SceneConsts.PROMPT_FILE_AGENT.equals(p.getFileName()));
    scene.setAgentPrompt(Optional.ofNullable(agent).map(ClawWorkspaceDTO::getFileContent).orElse(""));

    ClawWorkspaceDTO profile = IterableUtils.find(workspaces, p -> SceneConsts.PROMPT_FILE_PROFILE.equals(p.getFileName()));
    scene.setProfilePrompt(Optional.ofNullable(profile).map(ClawWorkspaceDTO::getFileContent).orElse(""));

    ClawWorkspaceDTO soul = IterableUtils.find(workspaces, p -> SceneConsts.PROMPT_FILE_SOUL.equals(p.getFileName()));
    scene.setSoulPrompt(Optional.ofNullable(soul).map(ClawWorkspaceDTO::getFileContent).orElse(""));
  }

  private void setFileContent(List<ClawWorkspaceDTO> workspaces, String fileName, String fileContent, Long tenantId, Long sceneId) {
    ClawWorkspaceDTO dto = IterableUtils.find(workspaces, p -> fileName.equals(p.getFileName()));
    if (dto != null) {
      dto.setFileContent(fileContent);
    }
    else {
      dto = new ClawWorkspaceDTO();
      dto.setFileName(fileName);
      dto.setFileContent(fileContent);
      dto.setSceneId(sceneId);
      dto.setTenantId(tenantId);
      workspaces.add(dto);
    }
  }
}
