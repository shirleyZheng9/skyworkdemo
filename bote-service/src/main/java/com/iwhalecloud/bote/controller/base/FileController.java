package com.iwhalecloud.bote.controller.base;

import com.alibaba.excel.util.IoUtils;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.FileCompressUtil;
import com.iwhalecloud.bote.common.util.FileTypeUtil;
import com.iwhalecloud.bote.dto.base.FilePreviewRequestParams;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.base.FilePreviewDTO;
import com.iwhalecloud.bote.service.IFilePreviewService;
import com.iwhalecloud.bote.service.base.IFileInfoManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.file.controller.StorageController;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.util.FileStoreUtils;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * 文件上传下载
 * <p>{@link StorageController} 提供了示例，允许扩展个性化功能 </p>
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Controller
@RequestMapping(path = BaseConsts.API_PREFIX + "file", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "基础：文件上传")
@SuppressWarnings("PMD.GuardLogStatement")
public class FileController extends StorageController {

  private final IFileInfoManageService fileInfoManageService;
  private final IFilePreviewService filePreviewService;

  public FileController(IFileStoreService fileStoreService, IFileInfoManageService fileInfoManageService, IFilePreviewService filePreviewService) {
    super(fileStoreService);
    this.fileInfoManageService = fileInfoManageService;
    this.filePreviewService = filePreviewService;
  }

  @GetMapping(path = "file/id/{fileId}", produces = MediaType.ALL_VALUE)
  @Operation(summary = "根据文件 ID 访问服务器文件")
  @Override
  public void file(@Parameter(description = "文件 ID", required = true) @PathVariable("fileId") Long fileId, HttpServletRequest request,
    HttpServletResponse response) throws IOException {
    super.file(fileId, request, response);
  }

  @Operation(summary = "根据文件 ID 获取文件信息")
  @Override
  public ResultVO<FileInfoVO> getFileInfoById(@Parameter(description = "文件 ID", required = true) @RequestParam("fileId") Long fileId) {
    Assert.notNull(fileId, "文件 ID 不能为空");
    return super.getFileInfoById(fileId);
  }

  @Operation(summary = "根据文件 ID 集合获取文件信息")
  @Override
  public ResultVO<List<FileInfoVO>> getFileInfoByIds(@Parameter(description = "文件 ID", required = true) @RequestParam("fileIds") List<Long> fileIds) {
    Assert.notEmpty(fileIds, "文件 ID 不能为空");
    return ResultVO.success(fileStoreService.getFileInfoByIds(fileIds));
  }

  @Operation(summary = "根据文件 ID 下载服务器文件")
  @GetMapping(path = "download", produces = MediaType.ALL_VALUE)
  public void download(@Parameter(description = "文件 ID", required = true) @RequestParam("fileId") Long fileId, HttpServletResponse response)
    throws IOException {
    FileInfoVO fileInfo = fileStoreService.getFileInfoById(fileId);
    if (fileInfo == null) {
      response.sendError(HttpStatus.NOT_FOUND.value(), "文件不存在");
      return;
    }
    long fileSize = fileStoreService.getFileSize(fileInfo);
    if (fileSize < 0) {
      response.sendError(HttpStatus.NOT_FOUND.value(), "文件不存在");
      return;
    }
    response.setContentLengthLong(fileSize);
    response.setContentType(guessMediaType(fileInfo.getFileName()).toString());
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fileInfo.getFileName(), StandardCharsets.UTF_8).build().toString());
    ServletOutputStream outputStream = response.getOutputStream(); //NOPMD - suppressed CloseResource - HTTP 输出流不需要手动关闭
    try (InputStream inputStream = useLocalFileCache()
      ? fileStoreService.downloadFileStreamFromCache(fileInfo)
      : fileStoreService.downloadFileStream(fileInfo)) {
      IoUtils.copy(inputStream, outputStream);
    }
    outputStream.flush();
  }

  @Operation(summary = "根据文件 ID 集合批量下载服务器文件")
  @GetMapping(path = "batchDownload", produces = MediaType.ALL_VALUE)
  public ResponseEntity<?> batchDownload(@Parameter(description = "文件ID列表", required = true) @RequestParam("fileIds") String fileIds) {
    Assert.hasText(fileIds, "文件ID列表不能为空");
    List<String> fileIdList = Arrays.asList(fileIds.split(","));
    // 判断只有单个文件时，不需要压缩
    if (fileIdList.size() == 1) {
      return singleFile(Long.parseLong(fileIdList.get(0)));
    }
    String compressDir = FileCompressUtil.createWorkSpace();
    String fileName = System.currentTimeMillis() + ".zip";
    FileCompressUtil.compress(compressDir, fileName, fileIdList);
    ResultVO<InputStream> result = FileCompressUtil.readFileAsStream(compressDir, fileName);
    return FileCompressUtil.doExport(compressDir, fileName, result);
  }

  private ResponseEntity<?> singleFile(Long fileId) {
    FileInfoVO fileInfo = fileStoreService.getFileInfoById(fileId);
    if (fileInfo == null) {
      throw BaseErrorConstant.FILE_ID_NOT_EXIST.toException(fileId);
    }
    byte[] bytes = fileStoreService.downloadFile(fileId);

    String contentDisposition = ContentDisposition.attachment().filename(fileInfo.getFileName(), StandardCharsets.UTF_8).build().toString();
    return ResponseEntity.ok().contentType(guessMediaType(fileInfo)).header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition).body(bytes);
  }

  @Operation(summary = "上传单个文件")
  @PostMapping(value = "uploadSingleFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseBody
  public ResultVO<FileInfoVO> uploadSingleFile(@RequestParam(name = "botId", required = false) Long botId, MultipartHttpServletRequest request) {
    // 提取出所有文件
    List<MultipartFile> files = request.getMultiFileMap().values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    if (files.isEmpty()) {
      return ResultVO.fail("请求中没有文件");
    }
    MultipartFile file = files.get(0);
    UploadConfigVO config = new UploadConfigVO();

    config.setStoreType(FileStoreUtils.getDefaultStoreType());
    config.setAppId(botId);
    try {
      // 获取文件类型并校验类型是否允许上传
      String fileType = FileTypeUtil.getType(file);
      ResultVO<Void> validateResult = checkFileType(fileType);
      if (!validateResult.isSuccess()) {
        return new ResultVO<>(validateResult);
      }
      config.setOriginalFileName(StringUtils.trimToNull(file.getOriginalFilename()));
      config.setFileSize(file.getSize());
      config.setFileType(fileType);
      FileInfoVO fileInfo = fileStoreService.uploadFile(file, config);
      fileInfo.setIsPicture(FileTypeUtil.isPicture(config.getFileType()));
      return ResultVO.success(fileInfo);
    }
    catch (IOException | RuntimeException e) {
      logger.error("Failed to upload file: name={}, originalFileName={}, size={}", file.getName(), file.getOriginalFilename(), file.getSize(), e);
      return ResultVO.fail("上传 " + file.getOriginalFilename() + " 失败: " + e.getMessage());
    }
  }

  @Operation(summary = "批量上传文件")
  @PostMapping(value = "uploadMultiFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseBody
  public ResultVO<List<FileInfoVO>> uploadMultiFile(@RequestParam(name = "botId", required = false) Long botId, MultipartHttpServletRequest request) {
    // 提取出所有文件
    List<MultipartFile> files = request.getMultiFileMap().values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    if (files.isEmpty()) {
      return ResultVO.fail("请求中没有文件");
    }
    List<FileInfoVO> fileInfos = new ArrayList<>(files.size());
    UploadConfigVO config = new UploadConfigVO();
    config.setStoreType(FileStoreUtils.getDefaultStoreType());
    config.setAppId(botId);
    for (MultipartFile file : files) {
      try {
        config.setSaveName("");
        config.setOriginalFileName(StringUtils.trimToNull(file.getOriginalFilename()));
        config.setFileSize(file.getSize());
        config.setFileType(FileTypeUtil.getType(file));
        FileInfoVO fileInfo = fileStoreService.uploadFile(file, config);
        fileInfo.setIsPicture(FileTypeUtil.isPicture(config.getFileType()));
        fileInfos.add(fileInfo);
      }
      catch (IOException | RuntimeException e) {
        logger.error("Failed to upload file: name={}, originalFileName={}, size={}", file.getName(), file.getOriginalFilename(), file.getSize(), e);
        return ResultVO.fail("上传 " + file.getOriginalFilename() + " 失败: " + e.getMessage());
      }
    }
    return ResultVO.success(fileInfos);
  }

  @Operation(summary = "根据文件信息 ID 下载服务器文件")
  @GetMapping(path = "downloadByFileInfoId", produces = MediaType.ALL_VALUE)
  public void downloadByFileInfoId(@Parameter(description = "文件 ID", required = true) @RequestParam("fileInfoId") Long fileInfoId,
    @Parameter(description = "租户 ID", required = true) @RequestParam("tenantId") Long tenantId,
    HttpServletRequest request, HttpServletResponse response) throws IOException {
    FileInfoDTO fileInfo = fileInfoManageService.findFileInfo(tenantId, fileInfoId);
    if (Objects.isNull(fileInfo)) {
      throw new BssException("文件资源不存在, fileInfoId=" + fileInfoId);
    }
    super.file(fileInfo.getFileId(), request, response);
  }

  /**
   * 校验文件类型是否允许上传
   */
  private ResultVO<Void> checkFileType(String fileType) {
    String fileTypes = SystemParameter.PLATFORM_ALLOW_UPLOAD_FILE_TYPE.getValueFromDb();
    String[] allowFileTypes = fileTypes.split(",");
    if (!ArrayUtils.contains(allowFileTypes, fileType)) {
      return BaseErrorConstant.NOT_ALLOWED_UPLOAD_FILE_TYPE.toResult(fileType);
    }
    return ResultVO.success();
  }

  @Operation(summary = "文件预览服务")
  @PostMapping("previewFile")
  @ResponseBody
  public ResultVO<FilePreviewDTO> previewFile(@RequestBody FilePreviewRequestParams params) {
    return filePreviewService.getFilePreviewInfo(params);
  }

}
