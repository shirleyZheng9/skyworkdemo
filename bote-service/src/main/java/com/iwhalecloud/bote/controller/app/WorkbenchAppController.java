package com.iwhalecloud.bote.controller.app;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.annotation.RequestCacheable;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.IconUtil;
import com.iwhalecloud.bote.dto.app.WorkbenchAppDTO;
import com.iwhalecloud.bote.dto.app.WorkbenchAppRelDTO;
import com.iwhalecloud.bote.dto.app.query.WorkbenchAppQueryParams;
import com.iwhalecloud.bote.service.app.IWorkbenchAppService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作台应用 Controller
 *
 * @author tingyun.wang
 * @since 2025-09-08
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/workbenchApp", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "运行态：工作台应用")
public class WorkbenchAppController {

  private final IWorkbenchAppService workbenchAppService;

  @PostMapping("saveWorkbenchApp")
  @Operation(summary = "保存工作台应用")
  public ResultVO<WorkbenchAppDTO> saveWorkbenchApp(@RequestBody WorkbenchAppDTO appDTO) {
    return workbenchAppService.saveWorkbenchApp(appDTO);
  }

  @PostMapping("saveAppSettings")
  @Operation(summary = "保存工作台应用能力配置")
  public ResultVO<Void> saveAppSettings(@RequestBody WorkbenchAppDTO appDTO) {
    workbenchAppService.saveAppSettings(appDTO);
    return ResultVO.success();
  }

  @GetMapping("queryAppSettings")
  @Operation(summary = "查询工作台应用能力配置")
  public ResultVO<WorkbenchAppDTO> queryAppSettings(@RequestParam("workbenchAppId") Long workbenchAppId, @RequestParam("spaceId") Long spaceId) {
    return ResultVO.success(workbenchAppService.queryAppSettings(workbenchAppId, spaceId));
  }

  @PostMapping("saveAuthInfo")
  @Operation(summary = "保存工作台应用授权信息")
  public ResultVO<Void> saveAuthInfo(@RequestBody WorkbenchAppDTO appDTO) {
    workbenchAppService.saveAuthInfo(appDTO);
    return ResultVO.success();
  }

  @GetMapping("queryAuthInfo")
  @Operation(summary = "查询工作台应用授权信息")
  public ResultVO<WorkbenchAppDTO> queryAuthInfo(@RequestParam("workbenchAppId") Long workbenchAppId, @RequestParam("spaceId") Long spaceId) {
    return ResultVO.success(workbenchAppService.queryAuthInfo(workbenchAppId, spaceId));
  }

  @PostMapping("queryWorkbenchAppPage")
  @Operation(summary = "分页查询工作台应用")
  public ResultVO<PageInfo<WorkbenchAppDTO>> queryWorkbenchAppPage(@RequestBody WorkbenchAppQueryParams queryParams) {
    return ResultVO.success(workbenchAppService.queryWorkbenchAppPage(queryParams));
  }

  @PostMapping("queryAuthWorkbenchAppPage")
  @Operation(summary = "查询用户授权工作台应用")
  public ResultVO<PageInfo<WorkbenchAppDTO>> queryAuthWorkbenchAppPage(@RequestBody WorkbenchAppQueryParams queryParams) {
    return ResultVO.success(workbenchAppService.queryAuthWorkbenchAppPage(queryParams));
  }

  @GetMapping("getWorkbenchAppDetail")
  @Operation(summary = "查询工作台应用详情")
  public ResultVO<WorkbenchAppDTO> getWorkbenchAppDetail(@RequestParam("workbenchAppId") Long workbenchAppId, @RequestParam("spaceId") Long spaceId) {
    return ResultVO.success(workbenchAppService.getWorkbenchApp(workbenchAppId, spaceId));
  }

  @GetMapping("deleteWorkbenchApp")
  @Operation(summary = "删除工作台应用")
  public ResultVO<Void> deleteWorkbenchApp(@RequestParam("workbenchAppId") Long workbenchAppId, @RequestParam("spaceId") Long spaceId) {
    return workbenchAppService.deleteWorkbenchApp(workbenchAppId, spaceId);
  }

  @GetMapping("enabledWorkbenchApp")
  @Operation(summary = "启用工作台应用")
  public ResultVO<Void> enabledWorkbenchApp(@RequestParam("workbenchAppId") Long workbenchAppId, @RequestParam("spaceId") Long spaceId) {
    workbenchAppService.enabledWorkbenchApp(workbenchAppId, spaceId);
    return ResultVO.success();
  }

  @GetMapping("disabledWorkbenchApp")
  @Operation(summary = "停用工作台应用")
  public ResultVO<Void> disabledWorkbenchApp(@RequestParam("workbenchAppId") Long workbenchAppId, @RequestParam("spaceId") Long spaceId) {
    workbenchAppService.disabledWorkbenchApp(workbenchAppId, spaceId);
    return ResultVO.success();
  }

  @IgnoreSign
  @IgnoreSession
  @Operation(summary = "获取工作台应用图标")
  @GetMapping(value = "workbenchAppIcon", produces = MediaType.ALL_VALUE)
  @RequestCacheable(sql = "SELECT updated_time FROM bt_workbench_app WHERE workbench_app_id = #{param2}", cacheOnNotFound = true)
  public void getWorkbenchAppIcon(@RequestParam("spaceId") Long spaceId, @RequestParam("workbenchAppId") Long workbenchAppId,
                            HttpServletResponse response) throws IOException {
    Assert.notNull(spaceId, "企业空间 ID 不能为空");
    Assert.notNull(workbenchAppId, "工作台应用 ID 不能为空");
    String appIcon = workbenchAppService.getWorkbenchAppIcon(workbenchAppId, spaceId);
    IconUtil.sendBase64Icon(response, appIcon);
  }

  @GetMapping("getAuthWorkbenchAppDetail")
  @Operation(summary = "查询授权工作台应用详情")
  public ResultVO<WorkbenchAppDTO> getAuthWorkbenchAppDetail(@RequestParam("workbenchAppId") Long workbenchAppId, @RequestParam("spaceId") Long spaceId) {
    return ResultVO.success(workbenchAppService.getAuthWorkbenchAppDetail(workbenchAppId, spaceId));
  }

  @GetMapping("getWorkbenchAppRelInfo")
  @Operation(summary = "查询工作台应用关联应用信息")
  public ResultVO<WorkbenchAppDTO> getWorkbenchAppRelInfo(@RequestParam("workbenchAppId") Long workbenchAppId, @RequestParam("spaceId") Long spaceId) {
    return ResultVO.success(workbenchAppService.queryWorkbenchAppRelInfo(workbenchAppId, spaceId));
  }

  @GetMapping("checkAppPublishedStatus")
  @Operation(summary = "检查应用是否已发布到运行态")
  public ResultVO<WorkbenchAppRelDTO> checkAppPublishedStatus(@RequestParam("tenantId") Long tenantId, @RequestParam("botId") Long botId) {
    return ResultVO.success(workbenchAppService.checkAppPublishedStatus(tenantId, botId));
  }

  @GetMapping("unpublishApp")
  @Operation(summary = "解除发布应用")
  public ResultVO<Void> unpublishApp(@RequestParam("tenantId") Long tenantId, @RequestParam("botId") Long botId) {
    workbenchAppService.unpublishApp(tenantId, botId);
    return ResultVO.success();
  }

}
