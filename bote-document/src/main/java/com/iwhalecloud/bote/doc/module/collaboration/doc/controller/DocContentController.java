package com.iwhalecloud.bote.doc.module.collaboration.doc.controller;

import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.space.annotation.IgnoreSpace;
import com.iwhalecloud.bote.doc.common.tenant.annotation.IgnoreTenant;
import com.iwhalecloud.bote.doc.consts.DocumentPermConsts;
import com.iwhalecloud.bote.doc.module.collaboration.cache.SocketServerCache;
import com.iwhalecloud.bote.doc.module.collaboration.constant.SocketBizEnum;
import com.iwhalecloud.bote.doc.module.collaboration.doc.dto.NodePublishEvent;
import com.iwhalecloud.bote.doc.module.collaboration.doc.service.NodeJsService;
import com.iwhalecloud.bote.doc.module.collaboration.socket.service.SocketMessageSendService;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.GenericCmdType;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.model.SocketSendInfo;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocContentHistoryDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocContentHistoryInfoDTO;
import com.iwhalecloud.bote.doc.module.document.dto.OnlineDocumentInfoDTO;
import com.iwhalecloud.bote.doc.module.document.service.DocumentChangEventPublisher;
import com.iwhalecloud.bote.doc.module.document.service.IDocContentService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 在线文档接口
 *
 * @author Aiqing
 * @since 2025/9/17
 */
@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "dc/document/docs", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "文档中心：在线文档接口")
public class DocContentController {

  private static final Logger logger = LoggerFactory.getLogger(DocContentController.class);

  private final IDocumentService documentService;
  private final IDocContentService docContentService;
  private final IDcUserService dcUserService;
  private final ControlTemplate controlTemplate;
  private final NodeJsService nodeJsService;
  private final DocumentChangEventPublisher documentChangEventPublisher;
  private final SocketMessageSendService socketMessageSendService;
  private final SocketServerCache socketServerCache;



  @Operation(summary = "查询在线文档的内容")
  @GetMapping("/{documentId}/content")
  public ResultVO<OnlineDocumentInfoDTO> getDocumentContent(@PathVariable String documentId) {
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    if (documentDTO == null) {
      return ResultVO.fail("文档不存在");
    }
    OnlineDocumentInfoDTO documentInfoDTO = docContentService.findContentByDocumentId(documentId);
    documentInfoDTO.setLibraryId(documentDTO.getLibraryId());
    String updatorName = dcUserService.findUserNameById(documentDTO.getUpdatorId());
    documentInfoDTO.setUpdatorName(updatorName);
    return ResultVO.success(documentInfoDTO);
  }

  @IgnoreTenant
  @IgnoreSpace
  @Operation(summary = "查询在线文档历史版本列表")
  @GetMapping("/getDocumentContentVersions")
  public ResultVO<List<DocContentHistoryInfoDTO>> getDocumentContentVersions(@RequestParam("documentId") String documentId) {
    checkDocument(documentId, NodePermission.READ_NODE, DocumentPermConsts.ACCESS_DENIED_CALLBACK);
    return ResultVO.success(docContentService.getDocumentContentVersions(documentId));
  }

  @IgnoreTenant
  @IgnoreSpace
  @Operation(summary = "查询在线文档历史版本内容")
  @GetMapping("/getDocumentContentVersion")
  public ResultVO<DocContentHistoryDTO> getDocumentContentVersion(@RequestParam("documentId") String documentId,
    @RequestParam(name = "id", required = false) Long id) {
    Assert.isTrue(id != null, "id不能为空");
    checkDocument(documentId, NodePermission.READ_NODE, DocumentPermConsts.ACCESS_DENIED_CALLBACK);
    return ResultVO.success(docContentService.getDocumentContentVersion(documentId, id));
  }

  @IgnoreTenant
  @IgnoreSpace
  @Operation(summary = "在线文档使用指定版本的文档内容")
  @PostMapping("/restoreContentVersion/{documentId}/{id}")
  public ResultVO<Void> restoreContentVersion(@PathVariable("documentId") String documentId, @PathVariable("id") Long id) {
    DcDocumentDTO documentDTO = checkDocument(documentId, NodePermission.EDIT_NODE, DocumentPermConsts.EDIT_DENIED_CALLBACK);
    docContentService.restoreContentVersion(documentId, id);
    nodeJsService.refreshDocumentContent(documentId);
    documentChangEventPublisher.publishEditEvent(documentDTO.getLibraryId(), documentId, SessionUtil.getLoginInfo().getUserId());
    // 通知所有连接的客户端重新加载文档内容
    notifyClientsReload(documentId);

    return ResultVO.success();
  }

  /**
   * 通知所有连接到该文档的客户端重新加载文档内容。
   * <p>通过两种方式：1. 系统消息通知前端重新加载；2. Redis 发布协作消息通知 NodeJs 重置文档状态。
   *
   * @param documentId 文档ID
   */
  private void notifyClientsReload(String documentId) {
    try {
      // 方式1: 通过系统消息通知前端强制重置并重新连接Yjs文档
      // 关键：需要前端完全清空本地Yjs状态，然后重新连接获取完整新状态
      Map<String, Object> reloadMessage = new HashMap<>();
      reloadMessage.put("type", "document_reset");
      reloadMessage.put("action", "force_reset_reconnect");  // 强制重置并重连
      reloadMessage.put("resetType", "full");  // 完全重置，清空所有本地状态
      reloadMessage.put("documentId", documentId);
      reloadMessage.put("reason", "version_restored");
      reloadMessage.put("timestamp", System.currentTimeMillis());
      reloadMessage.put("message", "文档内容已恢复，需要完全重置并重新连接以获取最新内容（覆盖而非追加）");

      SocketSendInfo<Map<String, Object>> systemMessage =
        socketMessageSendService.createGenericMessage(GenericCmdType.SYSTEM_MESSAGE, reloadMessage);

      // 通过WebSocket直接通知已订阅的客户端
      int sentCount = socketMessageSendService.sendToBusiness(
        SocketBizEnum.NODEJS_SIDECAR.name(), documentId, systemMessage);

      if (logger.isInfoEnabled()) {
        logger.info("WebSocket通知结果: documentId={}, 已订阅的客户端数量={}", documentId, sentCount);
      }

      // 方式2: 通过Redis发布协作消息，通知NodeJs服务需要重置文档状态
      // 这会让所有订阅了该文档的NodeJs服务实例知道需要重置文档
      // 注意：NodeJs服务会处理这个消息并通知所有连接到它的客户端（包括未在Java层订阅的）
      NodePublishEvent resetEvent = new NodePublishEvent();
      resetEvent.setDocumentId(documentId);
      // 发送一个特殊的重置消息，告诉NodeJs服务需要重置文档状态
      Map<String, Object> resetAction = new HashMap<>();
      resetAction.put("action", "reset_document");
      resetAction.put("reason", "version_restored");
      resetAction.put("timestamp", System.currentTimeMillis());
      resetEvent.setMessage(JsonUtil.toJsonString(resetAction));
      resetEvent.setPublisher("system");

      boolean published = socketServerCache.publish(documentId, resetEvent);
      if (published) {
        if (logger.isInfoEnabled()) {
          logger.info("已通过Redis发布文档重置消息: documentId={} (NodeJs服务会通知所有连接的客户端)", documentId);
        }
      }
      else {
        if (logger.isWarnEnabled()) {
          logger.warn("发布文档重置消息失败: documentId={}", documentId);
        }
      }

      // 说明：如果WebSocket只找到1个连接，但实际有多个浏览器标签页打开，
      // 可能是因为多个标签页共享了同一个WebSocket连接，或者部分连接还未完成订阅。
      // Redis发布消息会通过NodeJs服务转发，确保所有连接到NodeJs的客户端都能收到通知。
    }
    catch (Exception e) {
      // 通知失败不影响恢复操作，只记录日志
      if (logger.isWarnEnabled()) {
        logger.warn("通知客户端重置文档失败: documentId={}, error={}", documentId, e.getMessage(), e);
      }
    }
  }

  private DcDocumentDTO checkDocument(String documentId, NodePermission permission, Consumer<Boolean> resultCallback) {
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    if (documentDTO == null) {
      throw new BssException("文件不存在");
    }
    Long creatorId = SessionUtil.getLoginInfo().getUserId();
    //无查看权限
    controlTemplate.checkNodePermission(documentDTO.getLibraryId(), creatorId, documentId,
      permission, resultCallback);
    return documentDTO;
  }

}
