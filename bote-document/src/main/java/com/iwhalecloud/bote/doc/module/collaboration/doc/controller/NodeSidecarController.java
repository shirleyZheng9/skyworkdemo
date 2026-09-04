package com.iwhalecloud.bote.doc.module.collaboration.doc.controller;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.space.SpaceContextHolder;
import com.iwhalecloud.bote.doc.common.space.annotation.IgnoreSpace;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.common.tenant.annotation.IgnoreTenant;
import com.iwhalecloud.bote.doc.common.utils.ContentTypeUtil;
import com.iwhalecloud.bote.doc.consts.DocumentPermConsts;
import com.iwhalecloud.bote.doc.module.collaboration.doc.ro.DocumentHistorySaveRO;
import com.iwhalecloud.bote.doc.module.collaboration.doc.vo.DocumentHistoryVO;
import com.iwhalecloud.bote.doc.module.collaboration.doc.vo.DocumentRoleVO;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRole;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocContentHistoryDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentAttachmentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentContentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.OnlineDocumentInfoDTO;
import com.iwhalecloud.bote.doc.module.document.service.DocumentChangEventPublisher;
import com.iwhalecloud.bote.doc.module.document.service.IDocContentService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentAttachmentService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * nodejs协同接口
 * todo 增加接口拦截器，校验api key
 *
 * @author Aiqing
 * @since 2025/8/26
 */
@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "dc/sidecar/documents", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "文档中心：nodejs协同接口")
public class NodeSidecarController {

  private static final Logger logger = LoggerFactory.getLogger(NodeSidecarController.class);

  private final IDocContentService documentContentService;
  private final IDocumentService documentService;
  private final ControlTemplate controlTemplate;
  private final DocumentChangEventPublisher documentChangEventPublisher;
  private final IDcUserService dcUserService;
  private final IDocumentAttachmentService documentAttachmentService;


  @Operation(summary = "查询在线文档的内容")
  @GetMapping("/{documentId}/getContent")
  @IgnoreSession
  @IgnoreTenant
  @IgnoreSpace
  public ResultVO<OnlineDocumentInfoDTO> getDocumentContent(@PathVariable String documentId) {
    DcDocumentDTO documentDTO = fetchDocumentWithContextSet(documentId);
    if (documentDTO == null) {
      return ResultVO.fail("文档不存在");
    }
    OnlineDocumentInfoDTO documentInfoDTO = documentContentService.findContentByDocumentId(documentId);
    documentInfoDTO.setLibraryId(documentDTO.getLibraryId());
    String updatorName = dcUserService.findUserNameById(documentDTO.getUpdatorId());
    documentInfoDTO.setUpdatorName(updatorName);
    documentInfoDTO.setDocumentName(documentDTO.getDocumentName());
    return ResultVO.success(documentInfoDTO);
  }

  @Operation(summary = "保存文档内容")
  @PostMapping("/{documentId}/saveContent")
  @IgnoreSession
  @IgnoreTenant
  @IgnoreSpace
  @SuppressWarnings("PMD.GuardLogStatement")
  public ResultVO<Void> saveDocumentContent(@RequestBody DocumentContentDTO contentDTO, @PathVariable String documentId) {
    DcDocumentDTO documentDTO = fetchDocumentWithContextSet(documentId);
    if (documentDTO == null) {
      return ResultVO.fail("文档不存在");
    }
    Long updatorId = contentDTO.getUpdatorId();
    logger.trace("文档内容更新, documentId:{}, updatorId:{},mode:{}", documentId, updatorId, contentDTO.getMode());

    // 再次校验文档写入权限
    controlTemplate.checkNodePermission(documentDTO.getLibraryId(), updatorId, documentId,
      NodePermission.EDIT_NODE, DocumentPermConsts.EDIT_DENIED_CALLBACK);

    boolean saved = documentContentService.saveDocContent(documentId, contentDTO);
    if (saved) {
      documentChangEventPublisher.publishOnLineEditEvent(documentDTO.getLibraryId(), documentId, updatorId, contentDTO.getMode(), CommonConsts.TRUE);
    }
    return ResultVO.success();
  }

  @Operation(summary = "保存在线文档内容历史")
  @PostMapping("/{documentId}/saveHistory")
  @IgnoreSession
  @IgnoreTenant
  @IgnoreSpace
  public ResultVO<Void> saveHistory(@PathVariable String documentId, @RequestBody DocumentHistorySaveRO saveRO) {
    DcDocumentDTO documentDTO = fetchDocumentWithContextSet(documentId);
    if (documentDTO == null) {
      return ResultVO.fail("文档不存在");
    }
    documentContentService.saveContentHistory(documentId, JsonUtil.toJsonString(saveRO.getContent()), saveRO.getUpdatorId());
    return ResultVO.success();
  }

  @Operation(summary = "查询在线文档的最新版本")
  @GetMapping("/{documentId}/getLastHistory")
  @IgnoreSession
  @IgnoreTenant
  @IgnoreSpace
  public ResultVO<DocumentHistoryVO> selectLastDocHistory(@PathVariable String documentId) {
    DcDocumentDTO documentDTO = fetchDocumentWithContextSet(documentId);
    if (documentDTO == null) {
      return ResultVO.fail("文档不存在");
    }
    DocContentHistoryDTO historyDTO = documentContentService.selectLastDocHistory(documentId);
    if (historyDTO == null) {
      return ResultVO.success();
    }
    DocumentHistoryVO historyVO = new DocumentHistoryVO();
    historyVO.setDocumentId(documentId);
    historyVO.setLibraryId(documentDTO.getLibraryId());
    historyVO.setContent(historyDTO.getContent());
    historyVO.setCreatedTime(historyDTO.getCreatedTime());
    historyVO.setUpdatedTime(historyDTO.getUpdatedTime());
    return ResultVO.success(historyVO);
  }

  @Operation(summary = "查询文档节点的角色")
  @GetMapping("/{documentId}/getRole")
  @IgnoreSession
  @IgnoreTenant
  @IgnoreSpace
  public ResultVO<DocumentRoleVO> queryUserDocumentRole(@PathVariable String documentId, @RequestParam Long userId) {
    DcDocumentDTO documentDTO = fetchDocumentWithContextSet(documentId);
    if (documentDTO == null) {
      return ResultVO.fail("文档不存在");
    }
    String libraryId = documentDTO.getLibraryId();
    ControlRole controlRole = controlTemplate.fetchNodeRole(libraryId, userId, documentId);
    String roleTag = controlRole.getRoleTag();
    DocumentRoleVO roleVO = new DocumentRoleVO(roleTag);
    return ResultVO.success(roleVO);
  }

  @Operation(summary = "查询文档的附件文件")
  @GetMapping("downloadAttachmentFile")
  @IgnoreSession
  @IgnoreTenant
  @IgnoreSpace
  public void downloadAttachmentFile(@RequestParam String filePath, HttpServletResponse response) throws IOException {
    DocumentAttachmentDTO attachmentDTO = documentAttachmentService.parseDocumentImageFile(filePath);
    if (attachmentDTO == null) {
      logger.warn("附件不存在, filePath:{}", filePath);
      return;
    }
    String fileName = attachmentDTO.getFileName();
    // 设置响应头
    response.setContentType(ContentTypeUtil.getContentType(fileName));
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString());
    documentAttachmentService.fetchAttachmentFile(attachmentDTO, response.getOutputStream());
  }

  private DcDocumentDTO fetchDocumentWithContextSet(String documentId) {
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    if (documentDTO == null) {
      return null;
    }

    SpaceContextHolder.setIgnore(false);
    SpaceContextHolder.setSpaceId(documentDTO.getSpaceId());
    TenantContextHolder.setIgnore(false);
    TenantContextHolder.setTenantId(documentDTO.getTenantId());
    return documentDTO;
  }
}
