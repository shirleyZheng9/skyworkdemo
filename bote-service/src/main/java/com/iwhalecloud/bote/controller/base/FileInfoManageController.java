package com.iwhalecloud.bote.controller.base;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.base.BatchDeleteFileInfoRequest;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.base.SyncFileInfoRequest;
import com.iwhalecloud.bote.dto.base.query.FileInfoQueryParams;
import com.iwhalecloud.bote.dto.knowledge.query.UploadFileParams;
import com.iwhalecloud.bote.dto.knowledge.query.UpdateFileParams;
import com.iwhalecloud.bote.service.base.IFileInfoManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件信息管理 controller
 *
 * @author auto
 * @since 2024-09-24
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/fileInfo", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "基础：文件管理")
public class FileInfoManageController {

  private final IFileInfoManageService fileInfoManageService;

  @Operation(summary = "查询单个文件信息")
  @GetMapping("findFileInfo")
  public ResultVO<FileInfoDTO> findFileInfo(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam(name = "fileInfoId") Long fileInfoId) {
    Assert.notNull(fileInfoId, "主键 ID 不能为空");
    return ResultVO.success(fileInfoManageService.findFileInfo(tenantId, fileInfoId));
  }

  @Operation(summary = "删除文件信息")
  @GetMapping("deleteFileInfo")
  public ResultVO<Void> deleteFileInfo(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam(name = "fileInfoId") Long fileInfoId) {
    Assert.notNull(fileInfoId, "主键 ID 不能为空");
    return fileInfoManageService.deleteFileInfo(tenantId, fileInfoId);
  }

  @Operation(summary = "批量删除文件信息")
  @PostMapping("batchDeleteFileInfo")
  public ResultVO<Void> batchDeleteFileInfo(@RequestBody BatchDeleteFileInfoRequest request) {
    Assert.notEmpty(request.getFileInfoIds(), "文件信息ID列表不能为空");
    return fileInfoManageService.batchDeleteFileInfo(request.getTenantId(), request.getFileInfoIds());
  }

  @Operation(summary = "查询文件信息列表")
  @PostMapping("queryFileInfoList")
  public ResultVO<List<FileInfoDTO>> queryFileInfoList(@RequestBody FileInfoQueryParams queryParams) {
    return ResultVO.success(fileInfoManageService.queryFileInfoList(queryParams));
  }

  @Operation(summary = "分页查询文件信息")
  @PostMapping("queryFileInfoPage")
  public ResultVO<PageInfo<FileInfoDTO>> queryFileInfoPage(@RequestBody FileInfoQueryParams queryParams) {
    return ResultVO.success(fileInfoManageService.queryFileInfoPage(queryParams));
  }

  @Operation(summary = "重新上传文件")
  @PostMapping("reUploadFile")
  public ResultVO<FileInfoDTO> reUploadFile(@RequestPart("file") MultipartFile file, @RequestParam("fileInfoId") Long fileInfoId,
    @RequestParam(value = "tenantId", required = false) Long tenantId) {
    Assert.notNull(file, "附件不能为空");
    Assert.notNull(fileInfoId, "文件信息 ID 不能为空");
    return fileInfoManageService.reUploadFile(file, fileInfoId, tenantId);
  }

  @Operation(summary = "上传文件")
  @PostMapping("uploadFile")
  ResultVO<FileInfoDTO> uploadFile(@RequestPart("file") MultipartFile file, @RequestPart("query") UploadFileParams query) {
    Assert.notNull(query.getTenantId(), "租户 ID 不能为空");
    Assert.notNull(query.getCatalogItemId(), "目录 ID 不能为空");
    if (StringUtils.isEmpty(query.getBusiType())) {
      query.setBusiType(BaseConsts.FILE_BUSI_TYPE_DOCUMENT);
    }
    return fileInfoManageService.uploadFile(file, query);
  }

  @Operation(summary = "编辑文件")
  @PostMapping("updateFile")
  ResultVO<FileInfoDTO> updateFile(@RequestPart(value = "file", required = false) MultipartFile file, @RequestPart("query") UpdateFileParams query) {
    Assert.notNull(query.getFileInfoId(), "文件信息 ID 不能为空");
    Assert.notNull(query.getTenantId(), "租户 ID 不能为空");
    return fileInfoManageService.updateFile(file, query);
  }

  @Operation(summary = "文件 OCR 识别")
  @PostMapping("ocrFile")
  ResultVO<Object> ocrFile(@RequestPart("file") MultipartFile file) {
    return fileInfoManageService.ocrFile(file);
  }

  @Operation(summary = "同步文件信息")
  @PostMapping("syncFileInfo")
  public ResultVO<FileInfoDTO> syncFileInfo(@RequestBody SyncFileInfoRequest request) {
    return fileInfoManageService.syncFileInfo(request);
  }
}
