package com.iwhalecloud.bote.controller.agent;

import java.util.List;

import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.agent.AiWorkspaceDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import com.iwhalecloud.bote.service.agent.IAiWorkspaceManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * BoteClaw 配置管理 controller
 *
 * @author linmengfan
 * @since 2026-03-05
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/generalAgent/workspace", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "BoteClaw 配置管理-工作空间")
public class AiWorkspaceGeneralAgentManageController {

  // @formatter:off
  private final IAiWorkspaceManageService workspaceManageService;
  // @formatter:on

  @Operation(summary = "查询单个用户级的提示词", description = "查询单个用户级的提示词")
  @GetMapping("findBtAiWorkspace")
  public ResultVO<AiWorkspaceDTO> findAiWorkspace(@RequestParam(name = "id", required = false) Long id,
                                                  @RequestParam(name = "fileName", required = false) String fileName,
                                                  @RequestParam(name = "spaceId") Long spaceId,
                                                  @RequestParam(name = "botId") Long botId) {

    Assert.isTrue(!(id == null && StringUtils.isEmpty(fileName)), "文件名和id不能同时为空");
    return workspaceManageService.findAiWorkspace(id, fileName, spaceId, botId);
  }

  @Operation(summary = "查询用户级的提示词列表")
  @PostMapping("queryBtAiWorkspaceList")
  public ResultVO<List<AiWorkspaceDTO>> queryAiWorkspaceList(@RequestBody AiQueryParams queryParams) {
    Assert.notNull(queryParams.getSpaceId(), "空间ID不能为空");
    if (queryParams.getBotId() == null) {
      queryParams.setBotId(BaseConsts.BOTE_AI_ID);
    }
    return ResultVO.success(workspaceManageService.queryAiWorkspaceList(queryParams));
  }

  @Operation(summary = "分页查询记忆文件列表")
  @PostMapping("queryMemoryAiWorkspacePage")
  public ResultVO<PageInfo<AiWorkspaceDTO>> queryMemoryAiWorkspacePage(@RequestBody AiQueryParams queryParams) {
    Assert.notNull(queryParams.getSpaceId(), "空间ID不能为空");
    if (queryParams.getBotId() == null) {
      queryParams.setBotId(BaseConsts.BOTE_AI_ID);
    }
    return ResultVO.success(workspaceManageService.queryMemoryAiWorkspacePage(queryParams));
  }

  @Operation(summary = "保存用户级的提示词")
  @PostMapping("saveBtAiWorkspace")
  public ResultVO<AiWorkspaceDTO> saveAiWorkspace(@RequestBody AiWorkspaceDTO workspace) {
    Assert.notNull(workspace.getSpaceId(), "空间ID不能为空");
    Assert.notNull(workspace.getFileName(), "文件名不能为空");
    if (workspace.getBotId() == null) {
      workspace.setBotId(BaseConsts.BOTE_AI_ID);
    }
    return workspaceManageService.saveAiWorkspace(workspace);
  }

  @Operation(summary = "查询系统预置的提示词文件列表")
  @GetMapping("querySystemAiWorkspaceList")
  public ResultVO<List<AiWorkspaceDTO>> querySystemAiWorkspaceList() {
    return ResultVO.success(workspaceManageService.getSystemAiWorkspaceList());
  }

  @Operation(summary = "生成BoteClaw智能体定义提示词")
  @PostMapping("generateBtAiWorkspace")
  public ResultVO<String> generateAiWorkspace(@RequestBody AiQueryParams queryParams) {
    return workspaceManageService.generateAiWorkspace(queryParams);
  }

}
