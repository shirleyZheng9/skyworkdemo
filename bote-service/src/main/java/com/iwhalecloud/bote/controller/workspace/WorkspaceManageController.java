package com.iwhalecloud.bote.controller.workspace;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.annotation.RequestCacheable;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.IconUtil;
import com.iwhalecloud.bote.dto.workspace.SimpleWorkspaceDTO;
import com.iwhalecloud.bote.dto.workspace.SpaceIdMappingDTO;
import com.iwhalecloud.bote.dto.workspace.WorkspaceDTO;
import com.iwhalecloud.bote.dto.workspace.query.WorkspaceQueryParams;
import com.iwhalecloud.bote.service.workspace.IWorkspaceManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作空间管理 controller
 *
 * @author chen.linfa
 * @since 2025-10-16
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/workspace", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "工作空间：基础管理")
public class WorkspaceManageController {

  private final IWorkspaceManageService workspaceManageService;

  @Operation(summary = "获取工作空间")
  @GetMapping("getWorkspace")
  public ResultVO<WorkspaceDTO> getWorkspace(@RequestParam("spaceId") Long spaceId) {
    Assert.notNull(spaceId, "空间 ID 不能为空");
    return ResultVO.success(workspaceManageService.getWorkspace(spaceId));
  }

  @Operation(summary = "查询工作空间列表(分页)")
  @PostMapping("queryWorkspacePage")
  public ResultVO<PageInfo<WorkspaceDTO>> queryWorkspacePage(@RequestBody WorkspaceQueryParams queryParams) {
    return ResultVO.success(workspaceManageService.queryWorkspacePage(queryParams));
  }

  @Operation(summary = "保存工作空间")
  @PostMapping("saveWorkspace")
  public ResultVO<Long> saveWorkspace(@RequestBody WorkspaceDTO workspace) {
    return workspaceManageService.saveWorkspace(workspace);
  }

  @Operation(summary = "删除工作空间")
  @GetMapping("deleteWorkspace")
  public ResultVO<Void> deleteWorkspace(@RequestParam("spaceId") Long spaceId) {
    return workspaceManageService.deleteWorkspace(spaceId);
  }

  @IgnoreSign
  @IgnoreSession
  @Operation(summary = "获取工作空间图标")
  @GetMapping(value = "getWorkspaceIcon", produces = MediaType.ALL_VALUE)
  @RequestCacheable(sql = "SELECT updated_time FROM bt_workspace WHERE space_id = #{param1}", cacheOnNotFound = true)
  public void getWorkspaceIcon(@RequestParam("spaceId") Long spaceId, HttpServletResponse response) throws IOException {
    Assert.notNull(spaceId, "工作空间 ID 不能为空");
    String icon = workspaceManageService.getWorkspaceIcon(spaceId);
    if (StringUtils.isEmpty(icon)) {
      // 返回默认图标数据
      String path = "assets/avatar/img-space-avatar-default.png";
      ClassPathResource resource = new ClassPathResource(path);
      IconUtil.sendPathResourceIcon(response, resource);
    }
    else {
      IconUtil.sendBase64Icon(response, icon);
    }
  }

  @Operation(summary = "调用外部环境查询工作空间列表(分页)")
  @PostMapping("queryWorkspacePageFromExternal")
  public ResultVO<PageInfo<WorkspaceDTO>> queryWorkspacePageFromExternal(@RequestBody WorkspaceQueryParams queryParams) {
    Assert.notNull(queryParams.getGatewayId(), "网关 ID 不能为空");
    return workspaceManageService.queryWorkspacePageFromExternal(queryParams.getGatewayId(), queryParams, queryParams.getTenantId());
  }

  @Operation(summary = "获取所有工作空间列表")
  @GetMapping("getSimpleWorkspaceList")
  public ResultVO<List<SimpleWorkspaceDTO>> getSimpleWorkspaceList() {
    return ResultVO.success(workspaceManageService.querySimpleWorkspaceList());
  }

  @Operation(summary = "查询空间ID映射信息")
  @GetMapping("querySpaceId")
  public ResultVO<SpaceIdMappingDTO> querySpaceId(
      @RequestParam(value = "spaceId", required = false) Long spaceId,
      @RequestParam(value = "extSpaceId", required = false) String extSpaceId) {
    Assert.isTrue(spaceId != null || StringUtils.isNotEmpty(extSpaceId), "spaceId 和 extSpaceId 至少传一个");
    return ResultVO.success(workspaceManageService.querySpaceId(spaceId, extSpaceId));
  }

}
