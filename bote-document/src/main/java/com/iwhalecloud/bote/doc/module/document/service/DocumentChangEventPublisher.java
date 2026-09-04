package com.iwhalecloud.bote.doc.module.document.service;

import com.iwhalecloud.bassc.basiccenter.util.ext.HttpUtils;
import com.iwhalecloud.bote.doc.common.space.SpaceContextHolder;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.consts.DocumentActionTypeEnum;
import com.iwhalecloud.bote.doc.listener.event.DocChangeEventMessage;
import com.iwhalecloud.bss.litchi.disruptor.DisruptorUtil;
import com.iwhalecloud.bss.litchi.util.IPUtil;
import java.util.Date;
import org.springframework.stereotype.Service;

/**
 * 文档变化事件推送
 *
 * @author Aiqing
 * @since 2025/9/13
 */
@Service
public class DocumentChangEventPublisher {


  private static void publishEvent(String libraryId,
                                   String documentId,
                                   String documentName,
                                   Long optUserId,
                                   DocumentActionTypeEnum actionType,
                                   Long spaceId,
                                   String mode,
                                   String onlineEditing,
                                   String targetLibraryId) {
    DocChangeEventMessage changeEventMessage = new DocChangeEventMessage();
    changeEventMessage.setLibraryId(libraryId);
    changeEventMessage.setChangeType(actionType);
    changeEventMessage.setDocumentId(documentId);
    changeEventMessage.setDocumentName(documentName);
    changeEventMessage.setUpdatorId(optUserId);
    changeEventMessage.setUpdatedTime(new Date());
    changeEventMessage.setTenantId(TenantContextHolder.getTenantId());
    changeEventMessage.setSpaceId(spaceId);
    changeEventMessage.setClientIp(IPUtil.getClientIP(HttpUtils.getRequest()));
    changeEventMessage.setOnLineEditing(onlineEditing);
    changeEventMessage.setTargetLibraryId(targetLibraryId);
    changeEventMessage.setMode(mode);
    DisruptorUtil.getInstance().produce(changeEventMessage);
  }

  /**
   * 推送文档创建事件
   *
   * @param libraryId 文档库ID
   * @param documentId 文档ID
   * @param documentName 文档名称
   * @param optUserId 操作用户
   */
  public void publishCreateEvent(String libraryId, String documentId, String documentName, Long optUserId) {
    publishEvent(libraryId, documentId, documentName, optUserId, DocumentActionTypeEnum.CREATE, null, null, null, null);
  }

  /**
   * 推送文档上传事件
   */
  public void publishUploadEvent(String libraryId, String documentId, String documentName, Long optUserId) {
    publishEvent(libraryId, documentId, documentName, optUserId, DocumentActionTypeEnum.UPLOAD, null, null, null, null);
  }

  /**
   * 推送文档上传事件
   */
  public void publishReUploadEvent(String libraryId, String documentId, String documentName, Long optUserId) {
    publishEvent(libraryId, documentId, documentName, optUserId, DocumentActionTypeEnum.RE_UPLOAD, null, null, null, null);
  }

  /**
   * 推送文档移 进事件
   */
  public void publishDocumentMovedInEvent(String libraryId, String documentId, String documentName, Long optUserId, String targetLibraryId) {
    publishEvent(libraryId, documentId, documentName, optUserId, DocumentActionTypeEnum.MOVE_IN, null, null, null, targetLibraryId);
  }

  /**
   * 推送文档移 进事件
   */
  public void publishDocumentMovedOutEvent(String libraryId, String documentId, String documentName, Long optUserId, String targetLibraryId) {
    publishEvent(libraryId, documentId, documentName, optUserId, DocumentActionTypeEnum.MOVE_OUT, null, null, null, targetLibraryId);
  }

  /**
   * 推送文档上传事件
   */
  public void publishRenamedEvent(String libraryId, String documentId, String documentName, Long optUserId) {
    publishEvent(libraryId, documentId, documentName, optUserId, DocumentActionTypeEnum.DOCUMENT_RENAMED, null, null, null, null);
  }

  /**
   * 推送文档查看事件
   *
   * @param libraryId 文档库ID
   * @param documentId 文档ID
   * @param optUserId 操作用户
   */
  public void publishViewEvent(String libraryId, String documentId, Long optUserId) {
    publishEvent(libraryId, documentId, null, optUserId, DocumentActionTypeEnum.VIEW, SpaceContextHolder.getSpaceId(), null, null, null);
  }

  /**
   * 推送文档编辑事件
   *
   * @param libraryId 文档库ID
   * @param documentId 文档ID
   * @param optUserId 操作用户
   */
  public void publishEditEvent(String libraryId, String documentId, Long optUserId) {
    publishEvent(libraryId, documentId, null, optUserId, DocumentActionTypeEnum.EDIT, SpaceContextHolder.getSpaceId(), null, null, null);
  }

  /**
   * 推送文档编辑事件在线文档
   *
   * @param libraryId 文档库ID
   * @param documentId 文档ID
   * @param optUserId 操作用户
   */
  public void publishOnLineEditEvent(String libraryId, String documentId, Long optUserId, String mode, String onlineEditing) {
    publishEvent(libraryId, documentId, null, optUserId, DocumentActionTypeEnum.EDIT, SpaceContextHolder.getSpaceId(), mode, onlineEditing, null);
  }

  /**
   * 推送文档删除事件
   *
   * @param libraryId 文档库ID
   * @param documentId 文档ID
   * @param documentName 文档名称
   * @param optUserId 操作用户
   * @param spaceId 企业空间ID
   */
  public void publishDeleteEvent(String libraryId, String documentId, String documentName, Long optUserId, Long spaceId) {
    publishEvent(libraryId, documentId, null, optUserId, DocumentActionTypeEnum.DELETE, spaceId, null, null, null);
  }
}
