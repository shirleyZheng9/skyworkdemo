package com.iwhalecloud.bote.doc.module.document.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.config.properties.FilePreviewConfig;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.consts.ContentSourceEnum;
import com.iwhalecloud.bote.doc.consts.DocumentPermConsts;
import com.iwhalecloud.bote.doc.consts.FilePreviewConvertStatus;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentFileLogDTO;
import com.iwhalecloud.bote.doc.module.document.mapper.DocumentFileLogMapper;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bote.dto.base.DocumentPreviewDTO;
import com.iwhalecloud.bote.dto.base.FilePreviewDTO;
import com.iwhalecloud.bote.service.IFilePreviewService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.HashMap;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 文档预览服务service
 *
 * @author Aiqing
 * @since 2025/9/6
 */
@Service
@RequiredArgsConstructor
public class DocumentPreviewService {
  private static final Logger logger = LoggerFactory.getLogger(DocumentPreviewService.class);

  private final FilePreviewConfig documentFilePreviewConfig;
  private final IDocumentService documentService;
  private final ControlTemplate controlTemplate;
  private final IDcUserService dcUserService;
  private final IFilePreviewService filePreviewService;
  private final DocumentFileLogMapper documentFileLogMapper;

  /**
   * 获取文档关联文件的预览地址
   *
   * @param documentId 文档ID
   * @return 预览地址
   */
  public ResultVO<FilePreviewDTO> getFilePreviewInfo(String documentId, Long id) {
    // 1. 获取文档信息
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    if (documentDTO == null) {
      return ResultVO.fail("文档不存在");
    }
    // 权限检查
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
    controlTemplate.checkNodePermission(documentDTO.getLibraryId(), currentLoginUserId, documentId,
      NodePermission.READ_NODE, DocumentPermConsts.ACCESS_DENIED_CALLBACK);
    // 检查文档类型
    String contentSource = documentDTO.getContentSource();
    if (Objects.equals(contentSource, ContentSourceEnum.ONLINE.getCode())) {
      return ResultVO.fail("在线文档无法预览");
    }

    PortalUserDTO portalUser = dcUserService.findUserById(currentLoginUserId);
    DocumentPreviewDTO documentPreview = new DocumentPreviewDTO();
    documentPreview.setFileInfoId(documentDTO.getFileInfoId());
    documentPreview.setTenantId(documentDTO.getTenantId());
    documentPreview.setDocumentId(documentId);
    documentPreview.setDocumentName(documentDTO.getDocumentName());
    documentPreview.setUserName(portalUser.getUserName());
    documentPreview.setRevision(documentDTO.getRevision());
    if (id != null) {
      DocumentFileLogDTO documentFileLogDTO = documentFileLogMapper.selectDetailById(id, documentDTO.getTenantId());
      if (documentFileLogDTO == null) {
        return ResultVO.fail("文档历史记录不存在！");
      }
      documentPreview.setFileId(documentFileLogDTO.getFileId());
      documentPreview.setDocumentName(documentFileLogDTO.getFileName());
      documentPreview.setRevision(documentFileLogDTO.getRevision());
    }
    return filePreviewService.getFilePreviewInfo(documentPreview);
  }

  /**
   * 查询文档的转换状态
   *
   * @param url 文档URL
   */
  public String getFileConvertStatus(String url) {
    String queryUrl = UriComponentsBuilder.fromUriString(documentFilePreviewConfig.getConvertStatusUrl())
      .queryParam("url", url)
      .build()
      .toUriString();
    String response = HttpUtil.get(queryUrl, new HashMap<>());
    if (StringUtils.isBlank(response)) {
      logger.warn("请求文件转换状态异常，结果为空, url：{}", url);
      return FilePreviewConvertStatus.FAILED.getCode();
    }
    ResultVO<String> resultVO = JsonUtil.parseJson(response, new TypeReference<ResultVO<String>>() {
    });
    if (resultVO == null || !resultVO.isSuccess()) {
      return FilePreviewConvertStatus.FAILED.getCode();
    }
    FilePreviewConvertStatus convertStatus = FilePreviewConvertStatus.getByCode(resultVO.getResultObject());
    if (convertStatus == null) {
      return FilePreviewConvertStatus.FAILED.getCode();
    }
    return convertStatus.getCode();
  }
}
