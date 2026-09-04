package com.iwhalecloud.bote.doc.module.base.controller;

import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.space.SpaceContextHolder;
import com.iwhalecloud.bote.doc.consts.ContentSourceEnum;
import com.iwhalecloud.bote.doc.consts.DocumentTypeEnum;
import com.iwhalecloud.bote.doc.module.base.dto.BaseChunkRequest;
import com.iwhalecloud.bote.doc.module.base.dto.ConvertToOnlineFileRequest;
import com.iwhalecloud.bote.doc.module.base.dto.FolderStructureCreateRequest;
import com.iwhalecloud.bote.doc.module.base.dto.FolderUploadCacheDTO;
import com.iwhalecloud.bote.doc.module.base.dto.MergeChunkRequest;
import com.iwhalecloud.bote.doc.module.base.dto.UnifiedUploadRequest;
import com.iwhalecloud.bote.doc.module.base.helper.ImportServiceHelper;
import com.iwhalecloud.bote.doc.module.base.service.IFileUploadService;
import com.iwhalecloud.bote.doc.module.control.service.DocumentNodeService;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentTreeDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentFileLogDTO;
import com.iwhalecloud.bote.doc.module.document.service.DocumentChangEventPublisher;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentContributorService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentFileLogService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.module.library.dto.DocumentLibraryDTO;
import com.iwhalecloud.bote.doc.module.library.service.DocumentLibraryService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传管理
 *
 * @author yangran
 * @since 2025-08-25
 */
@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "dc/upload", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "文档中心：文件上传")
public class FileManagerController {

  private final IFileUploadService fileUploadService;
  private final DocumentNodeService documentNodeService;
  private final IDocumentService documentService;
  private final DocumentLibraryService documentLibraryService;
  private final DocumentChangEventPublisher documentChangEventPublisher;
  private final ImportServiceHelper importServiceHelper;
  private final IDocumentContributorService documentContributorService;
  private final IDocumentFileLogService documentFileLogService;

  @Operation(summary = "上传本地文件")
  @PostMapping("localFile")
  public ResultVO<?> uploadFile(@RequestPart("file") MultipartFile file, @Valid UnifiedUploadRequest request) {
    Assert.notNull(file, "文件不能为空");
    String libraryId = request.getLibraryId();
    String fileName = importServiceHelper.resolveFileName(file, request);
    ResultVO<Void> convertValidation = validateConvertToOnlineIfNeeded(request, fileName);
    if (convertValidation != null) {
      return convertValidation;
    }
    if (StringUtils.isEmpty(request.getParentId())) {
      // 默认在根节点下
      request.setParentId(
        documentNodeService.getRootNodeIdByLibraryId(libraryId, request.getTenantId(), request.getSpaceId()));
    }
    if (!DocBaseConsts.TRUE.equals(request.getIsFolderUpload())) {
      // 校验在同级文档库下没有相同的文件名
      checkUploadFileName(fileName, libraryId, request.getParentId(), request.getDocumentId(), request.getConvertToOnline());
    }
    if (request.getSpaceId() == null) {
      request.setSpaceId(TenantIdUtil.getSpaceId(request.getTenantId()));
      SpaceContextHolder.setSpaceId(request.getSpaceId());
    }
    DocumentLibraryDTO documentLibrary = getDocumentLibraryOrThrow(libraryId);
    Long userId = SessionUtil.getLoginInfo().getUserId();
    documentNodeService.checkParentNodeEditPermission(documentLibrary, userId, request.getParentId(), request.getDocumentId());
    // 检查是否启用分片上传
    if (Boolean.TRUE.equals(request.getEnableChunk())) {
      validateChunkRequest(request);
      return fileUploadService.uploadChunk(file, request);
    }

    // 普通文件上传
    ResultVO<DcDocumentDTO> resultVO = fileUploadService.uploadFile(file, request);
    if (resultVO.isSuccess()) {
      DcDocumentDTO dcDocument = resultVO.getResultObject();
      // 添加当前文档上传者为文档贡献者
      if (dcDocument != null && dcDocument.getDocumentId() != null) {
        documentContributorService.batchAddOrUpdateContributors(List.of(dcDocument.getDocumentId()), userId, BigDecimal.ZERO, userId);
      }
      handleUploadSuccess(file, request, libraryId, userId, dcDocument);
    }
    return resultVO;
  }

  @Operation(summary = "按上传日志回退文档文件版本", description = "根据 bt_dc_document_file_log 主键下载该条记录对应的历史文件（文件名为 bt_file.file_name），再以覆盖上传方式更新当前文档")
  @PostMapping("rollbackFromFileLog")
  public ResultVO<Void> rollbackFromFileLog(@RequestParam("id") Long id, @RequestParam("tenantId") Long tenantId) {
    DocumentFileLogDTO logDto = documentFileLogService.getDetailById(id, tenantId);
    Assert.notNull(logDto, "上传日志不存在");
    DcDocumentDTO documentDTO = documentService.findByDocumentId(logDto.getDocumentId());
    Assert.notNull(documentDTO, "文档不存在");
    DocumentLibraryDTO documentLibrary = getDocumentLibraryOrThrow(documentDTO.getLibraryId());
    Long userId = SessionUtil.getLoginInfo().getUserId();
    documentNodeService.checkParentNodeEditPermission(documentLibrary, userId, documentDTO.getParentId(), documentDTO.getDocumentId());
    return fileUploadService.rollbackDocumentFromFileLog(id, tenantId);
  }

  /**
   * 校验文件名是否在同级文档库下存在相同的文件名
   * @param fileName 文件名
   * @param libraryId 文档库id
   * @param parentId 文件夹id
   * @param documentId 文档id
   * @param convertToOnline 是否转在线文档
   */
  private void checkUploadFileName(String fileName, String libraryId, String parentId, String documentId, String convertToOnline) {
    if (DocBaseConsts.TRUE.equals(convertToOnline)) {
      String tempFileName = fileName.substring(0, fileName.lastIndexOf("."));
      if (documentService.existsDocumentByName(libraryId, parentId, documentId, tempFileName, importServiceHelper.getFileType(fileName), ContentSourceEnum.ONLINE.getCode())) {
        throw new BssException("该文档库的同级文件夹下存在相同名字的文档或者文件夹！");
      }
    }
    else {
      if (documentService.existsDocumentByName(libraryId, parentId, documentId, fileName, null, ContentSourceEnum.UPLOAD.getCode())) {
        throw new BssException("该文档库的同级文件夹下存在相同名字的文档或者文件夹！");
      }
    }
  }

  @Operation(summary = "上传文档转换在线文档")
  @PostMapping("convertToOnlineFile")
  public ResultVO<?> convertToOnlineFile(@RequestBody ConvertToOnlineFileRequest request) {
    // 查询文件是否存在
    DcDocumentDTO documentDTO = documentService.findByDocumentId(request.getDocumentId());
    if (documentDTO == null) {
      throw new BssException("文档不存在");
    }
    ResultVO<Void> convertValidation = validateUpConvertToOnlineIfNeeded(documentDTO); // 检查是否可以转为在线文档
    if (convertValidation != null) {
      return convertValidation;
    }

    String libraryId = documentDTO.getLibraryId();
    DocumentLibraryDTO documentLibrary = getDocumentLibraryOrThrow(libraryId);
    Long userId = SessionUtil.getLoginInfo().getUserId();
    documentNodeService.checkParentNodeEditPermission(documentLibrary, userId, documentDTO.getParentId(), documentDTO.getDocumentId());

    documentService.convertToOnlineIfNeeded(DocBaseConsts.TRUE, documentDTO, userId);
    return ResultVO.success();
  }

  /**
   * 校验文件类型是否允许上传
   */
  private ResultVO<Void> checkFileType(String fileType) {
    String fileTypes = BaseSystemParameter.ALLOW_UPLOAD_FILE_CONVERT_TO_ONLINE_TYPE.getValueFromDb();
    String[] allowFileTypes = fileTypes.split(",");
    if (!ArrayUtils.contains(allowFileTypes, fileType)) {
      return BaseErrorConstant.NOT_ALLOWED_UPLOAD_FILE_CONVERT_TO_ONLINE_TYPE.toResult(fileType);
    }
    return ResultVO.success();
  }

  private ResultVO<Void> validateUpConvertToOnlineIfNeeded(DcDocumentDTO documentDTO) {
    if (!ContentSourceEnum.UPLOAD.getCode().equals(documentDTO.getContentSource())) {
      throw new BssException("非上传文件不支持转在线文档！");
    }

    String fileName = documentDTO.getDocumentName();
    String fileType = FilenameUtils.getExtension(fileName);
    Assert.hasText(fileType, "获取文件类型失败");
    ResultVO<Void> validateResult = checkFileType(fileType);
    return validateResult.isSuccess() ? null : validateResult;
  }
  private ResultVO<Void> validateConvertToOnlineIfNeeded(UnifiedUploadRequest request, String fileName) {
    if (!DocBaseConsts.TRUE.equals(request.getConvertToOnline())) {
      return null;
    }
    String fileType = FilenameUtils.getExtension(fileName);
    Assert.hasText(fileType, "获取文件类型失败");
    ResultVO<Void> validateResult = checkFileType(fileType);
    return validateResult.isSuccess() ? null : validateResult;
  }

  private DocumentLibraryDTO getDocumentLibraryOrThrow(String libraryId) {
    DocumentLibraryDTO documentLibrary = documentLibraryService.findByLibraryId(libraryId);
    if (documentLibrary == null) {
      throw new BssException("文档库不存在");
    }
    return documentLibrary;
  }

  private void validateChunkRequest(UnifiedUploadRequest request) {
    Assert.notNull(request.getChunkIndex(), "分片索引不能为空");
    Assert.notNull(request.getTotalChunks(), "总分片数不能为空");
    Assert.hasText(request.getOriginalFileName(), "原始文件名不能为空");
    Assert.notNull(request.getOriginalFileSize(), "原始文件大小不能为空");
    Assert.hasText(request.getFileHash(), "文件哈希值不能为空");
  }

  private void handleUploadSuccess(MultipartFile file, UnifiedUploadRequest request, String libraryId, Long userId,
    DcDocumentDTO documentDTO) {
    publishUploadEventIfNew(request, libraryId, userId, documentDTO);
    importServiceHelper.convertToOnlineIfNeeded(file, request, documentDTO, userId);
  }

  private void publishUploadEventIfNew(UnifiedUploadRequest request, String libraryId, Long userId,
    DcDocumentDTO documentDTO) {
    if (StringUtils.isNotBlank(request.getDocumentId())) {
      return;
    }
    documentChangEventPublisher.publishUploadEvent(libraryId, documentDTO.getDocumentId(), documentDTO.getDocumentName(),
      userId);
  }

  @Operation(summary = "检查文件状态")
  @PostMapping("/chunk/check")
  public ResultVO<Map<String, Object>> checkFileStatus(@RequestBody BaseChunkRequest request) {
    Assert.hasText(request.getFileHash(), "文件哈希值不能为空");
    Assert.hasText(request.getFileName(), "文件名不能为空");
    return fileUploadService.checkFileStatus(request);
  }

  @Operation(summary = "合并分片文件")
  @PostMapping("/chunk/merge")
  public ResultVO<DcDocumentDTO> mergeChunks(@Valid @RequestBody MergeChunkRequest request) {
    Assert.hasText(request.getFileHash(), "文件hash值不能为空");
    Assert.notNull(request.getTotalChunks(), "总分片数不能为空");
    Assert.notNull(request.getFileSize(), "源文件大小不能为空");
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
    ResultVO<DcDocumentDTO> resultVO = fileUploadService.mergeChunks(request);
    // 文件夹上传 不在这里转在线文档
    if (resultVO.isSuccess() && DocBaseConsts.FALSE.equals(request.getIsFolderUpload())) {
      importServiceHelper.convertToOnlineIfNeeded(request.getConvertToOnline(), resultVO.getResultObject(), currentLoginUserId);
      // 上传成功
      if (StringUtils.isBlank(request.getDocumentId())) {
        DcDocumentDTO documentDTO = resultVO.getResultObject();

        documentChangEventPublisher.publishUploadEvent(documentDTO.getLibraryId(), documentDTO.getDocumentId(),
          documentDTO.getDocumentName(), currentLoginUserId);
      }
      // 添加当前用户为文档贡献者
      documentContributorService.batchAddOrUpdateContributors(List.of(resultVO.getResultObject().getDocumentId()),
        currentLoginUserId, BigDecimal.ZERO, currentLoginUserId);
    }
    return resultVO;
  }

  @Operation(summary = "清理失败的分片上传")
  @PostMapping("/chunk/cleanup")
  public ResultVO<Boolean> cleanupFailedUpload(@RequestBody BaseChunkRequest request) {
    Assert.hasText(request.getFileHash(), "文件hash不能为空");
    return fileUploadService.cleanupFailedUpload(request.getFileHash());
  }

  @Operation(summary = "获取文件夹上传进度")
  @GetMapping("folderUploadProgress")
  @ResponseBody
  public ResultVO<FolderUploadCacheDTO> getFolderUploadProgress(@RequestParam("taskId") String taskId) {
    Assert.hasText(taskId, "任务ID不能为空");
    return fileUploadService.getFolderUploadProgress(taskId);
  }

  @Operation(summary = "暂停文件夹上传")
  @PostMapping("pauseFolderUpload")
  @ResponseBody
  public ResultVO<Boolean> pauseFolderUpload(@RequestBody UnifiedUploadRequest request) {
    Assert.hasText(request.getIsFolderUpload(), "任务ID不能为空");
    return fileUploadService.pauseFolderUpload(request.getFolderUploadTaskId());
  }

  @Operation(summary = "恢复文件夹上传")
  @PostMapping("resumeFolderUpload")
  @ResponseBody
  public ResultVO<Boolean> resumeFolderUpload(@RequestBody UnifiedUploadRequest request) {
    Assert.hasText(request.getFolderUploadTaskId(), "任务ID不能为空");
    return fileUploadService.resumeFolderUpload(request.getFolderUploadTaskId());
  }

  @Operation(summary = "取消文件夹上传")
  @PostMapping("cancelFolderUpload")
  @ResponseBody
  public ResultVO<Boolean> cancelFolderUpload(@RequestBody UnifiedUploadRequest request) {
    Assert.hasText(request.getFolderUploadTaskId(), "任务ID不能为空");
    return fileUploadService.cancelFolderUpload(request.getFolderUploadTaskId());
  }

  @Operation(summary = "创建文件夹结构")
  @PostMapping(value = "createFolderStructure", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public ResultVO<DcDocumentTreeDTO> createFolderStructure(@RequestBody @Valid FolderStructureCreateRequest request) {
    Assert.notNull(request, "请求参数不能为空");
    Assert.hasText(request.getLibraryId(), "文档库ID不能为空");
    Assert.hasText(request.getFolderName(), "文件夹名称不能为空");
    ResultVO<DcDocumentTreeDTO> folderStructure = fileUploadService.createFolderStructure(request);
    if (folderStructure.isSuccess()) {
      if (DocBaseConsts.TRUE.equals(request.getConvertToOnline())) {
        List<DcDocumentDTO> dcDocumentDTOS = fileUploadService
          .queryDcDocumentDTOByRootId(folderStructure.getResultObject().getDocumentId());
        dcDocumentDTOS = dcDocumentDTOS.stream()
          .filter(document -> DocumentTypeEnum.WORD.getCode().equals(document.getDocumentType())
            || DocumentTypeEnum.EXCEL.getCode().equals(document.getDocumentType()))
          .toList();
        Long userId = SessionUtil.getLoginInfo().getUserId();
        if (CollectionUtils.isNotEmpty(dcDocumentDTOS)) {
          dcDocumentDTOS.forEach(document -> {
            // 创建在线文档
            importServiceHelper.convertToOnlineIfNeeded(request.getConvertToOnline(), document, userId);
          });
        }
      }
    }
    return folderStructure;
  }
}
