package com.iwhalecloud.bote.controller.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.skill.AdminImportRequest;
import com.iwhalecloud.bote.dto.skill.AdminUpdateStatusRequest;
import com.iwhalecloud.bote.dto.skill.ImportAllResponse;
import com.iwhalecloud.bote.dto.skill.SkillInstallLogVO;
import com.iwhalecloud.bote.dto.skill.SkillSquareAdminDetailVO;
import com.iwhalecloud.bote.dto.skill.SkillSquareAdminItemVO;
import com.iwhalecloud.bote.dto.skill.SkillSquareBulkExportJobStatusVO;
import com.iwhalecloud.bote.dto.skill.query.SkillInstallLogQueryParams;
import com.iwhalecloud.bote.dto.skill.query.SkillSquareAdminQueryParams;
import com.iwhalecloud.bote.service.skill.ISkillSquareService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * SKILL广场管理端控制器
 *
 * @author skill-square
 * @since 2026-03-18
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/skillSquare/admin", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@RestController
@Tag(name = "SKILL广场-管理端")
public class SkillSquareManageController {

  private final ISkillSquareService skillSquareService;

  @Operation(summary = "管理端分页查询技能列表")
  @PostMapping("queryPage")
  public ResultVO<PageInfo<SkillSquareAdminItemVO>> adminQueryPage(
    @RequestBody SkillSquareAdminQueryParams params) {
    return ResultVO.success(skillSquareService.adminQueryPage(params));
  }

  @Operation(summary = "查询技能详情")
  @GetMapping("detail")
  public ResultVO<SkillSquareAdminDetailVO> adminDetail(@RequestParam("skillId") Long skillId) {
    SkillSquareAdminDetailVO detail = skillSquareService.adminGetDetail(skillId);
    if (detail == null) {
      return ResultVO.fail("技能不存在或已删除");
    }
    return ResultVO.success(detail);
  }

  @Operation(summary = "单技能导入", description = "上传 ZIP 技能包导入单条广场技能")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
    schema = @Schema(implementation = AdminImportRequest.class),
    encoding = @Encoding(name = "packageFile", contentType = "application/octet-stream")))
  @PostMapping(value = "import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResultVO<Long> adminImport(@Valid @ModelAttribute AdminImportRequest request) {
    return skillSquareService.adminImport(request);
  }

  @Operation(summary = "单技能编辑")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
    encoding = @Encoding(name = "packageFile", contentType = "application/octet-stream")))
  @PostMapping(value = "update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResultVO<Void> adminUpdate(
    @RequestParam("skillId") Long skillId,
    @RequestParam(value = "skillName", required = false) String skillName,
    @RequestParam(value = "skillDesc", required = false) String skillDesc,
    @RequestParam(value = "packageFile", required = false) MultipartFile packageFile) {
    return skillSquareService.adminUpdate(skillId, skillName, skillDesc, packageFile);
  }

  @Operation(summary = "技能状态更新：上架/下架/删除")
  @PostMapping("updateStatus")
  public ResultVO<Void> adminUpdateStatus(@Valid @RequestBody AdminUpdateStatusRequest request) {
    return skillSquareService.adminUpdateStatus(request.getSkillId(), request.getAction());
  }

  @Operation(summary = "安装记录分页查询", description = "支持按技能、智能体名称关键词、租户、安装来源、时间段筛选")
  @PostMapping("installLogs")
  public ResultVO<PageInfo<SkillInstallLogVO>> adminQueryInstallLogs(@RequestBody SkillInstallLogQueryParams params) {
    return ResultVO.success(skillSquareService.adminQueryInstallLogs(params));
  }

  @Operation(summary = "大批量异步导出", description = "按安装量降序；可选 top；分包上传文件系统，立即返回 jobId；通过 exportAllAsync/status 轮询进度与 bote/file/download?fileId= 下载地址")
  @PostMapping("exportAllAsync")
  public ResultVO<Long> exportAllAsync(@RequestParam(value = "top", required = false) Integer top) {
    return skillSquareService.startBulkExportAsync(top);
  }

  @Operation(summary = "查询当前用户最近一次异步全量导出任务状态", description = "关闭弹窗后再次打开时恢复进度；无任务或已过期时 resultObject 为空")
  @GetMapping("exportAllAsync/current")
  public ResultVO<SkillSquareBulkExportJobStatusVO> exportAllAsyncCurrent() {
    return skillSquareService.getBulkExportCurrentJobStatus();
  }

  @Operation(summary = "清除当前用户与最近一次导出任务的绑定", description = "用于「新任务」后不再自动恢复已结束任务的展示")
  @PostMapping("exportAllAsync/clearCurrent")
  public ResultVO<Void> exportAllAsyncClearCurrent() {
    return skillSquareService.clearBulkExportCurrentJob();
  }

  @Operation(summary = "查询异步全量导出任务状态")
  @GetMapping("exportAllAsync/status")
  public ResultVO<SkillSquareBulkExportJobStatusVO> exportAllAsyncStatus(@RequestParam("jobId") Long jobId) {
    return skillSquareService.getBulkExportJobStatus(jobId);
  }

  @Operation(summary = "全量导入（同步处理）")
  @PostMapping(value = "importAll", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResultVO<ImportAllResponse> importAll(@RequestParam("file") MultipartFile file) {
    return skillSquareService.importAll(file);
  }
}
