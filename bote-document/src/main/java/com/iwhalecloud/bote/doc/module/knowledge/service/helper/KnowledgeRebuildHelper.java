package com.iwhalecloud.bote.doc.module.knowledge.service.helper;

import com.iwhalecloud.bote.dto.knowledge.docchain.response.QueryDocmentResponse.DocmentInfo;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.consts.KnowledgeConsts;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.RebuildDocumentGroupDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.RebuildDocumentStatusBucketDTO;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.DocumentManageMapper;

import lombok.RequiredArgsConstructor;

/**
 * 知识库更新学习辅助类
 */
@Component
@RequiredArgsConstructor
public class KnowledgeRebuildHelper {
  private final DocumentManageMapper documentManageMapper;

  private final DocChainConfigHelper docChainConfigHelper;

  /**
   * 处理新文档重构
   */
  public String processNewDocRebuild(KnowledgeBaseDTO dto, Long topicId, RebuildDocumentGroupDTO groups) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    if (!groups.getNewDocuments().isEmpty()) {
      handleNewDocuments(dto, topicId, groups, userId);
    }
    if (!groups.getOldDocuments().isEmpty()) {
      redoDocumentsAwaitingRetry(groups.getOldDocuments(), dto.getTenantId(), topicId);
      updateNewDocumentMeta(groups.getOldDocuments(), userId);
    }
    return "知识更新学习中";
  }

  /**
   * 处理旧文档重构
   */
  public String processOldDocRebuild(KnowledgeBaseDTO dto, Long topicId, List<DocumentDTO> documents) {
    List<Long> fileIds = documents.stream().map(DocumentDTO::getFileId).collect(Collectors.toList());
    Map<Long, Long> mapping = docChainConfigHelper.rebuildDocument(dto.getTenantId(), topicId, fileIds);
    updateDocumentsWithMapping(documents, mapping);
    return "知识更新学习中";
  }

  /**
   * 根据映射更新文档
   */
  private void updateDocumentsWithMapping(List<DocumentDTO> documents, Map<Long, Long> mapping) {
    for (Entry<Long, Long> entry : mapping.entrySet()) {
      DocumentDTO document = IterableUtils.find(documents, p -> Objects.equals(p.getFileId(), entry.getKey()));
      if (document != null) {
        document.setExtSystemId(entry.getValue());
        document.setDocStatus(KnowledgeConsts.DOCUMENT_STATUS_ANALYZING);
        document.setHasUpdate("0");
        document.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
        documentManageMapper.updateDocument(document);
      }
    }
  }

  /**
   * 处理新文档重构
   */
  private void handleNewDocuments(KnowledgeBaseDTO dto, Long topicId, RebuildDocumentGroupDTO groups, Long userId) {
    RebuildDocumentStatusBucketDTO buckets = splitNewDocuments(groups.getNewDocuments());
    List<DocumentDTO> uploads = processValidDocuments(buckets.getValidDocuments());
    redoDocumentsAwaitingRetry(buckets.getRetryDocuments(), dto.getTenantId(), topicId);
    uploadNewDocuments(dto, topicId, uploads);
    updateNewDocumentMeta(groups.getNewDocuments(), userId);
  }

  /**
   * 筛选源文档是否已经归档
   */
  private RebuildDocumentStatusBucketDTO splitNewDocuments(List<DocumentDTO> newDocuments) {
    RebuildDocumentStatusBucketDTO buckets = new RebuildDocumentStatusBucketDTO();
    buckets.setValidDocuments(new ArrayList<>());
    buckets.setRetryDocuments(new ArrayList<>());
    for (DocumentDTO document : newDocuments) {
      if (DocBaseConsts.STATUS_CD_VALID.equals(document.getDocumentStatus())) {
        buckets.getValidDocuments().add(document);
      }
      else {
        buckets.getRetryDocuments().add(document);
      }
    }
    return buckets;
  }

  /**
   * 处理原文档没有归档文档重构
   */
  private List<DocumentDTO> processValidDocuments(List<DocumentDTO> documents) {
    for (DocumentDTO document : documents) {
      document.setErrorMessage(null);
      prepareDocumentForUpload(document);
    }
    return documents;
  }

  /**
   * 文件名发生变化的处理
   */
  private void prepareDocumentForUpload(DocumentDTO document) {
    if (document.getExtSystemId() != null && StringUtils.isNotEmpty(document.getDocumentName())
      && !Objects.equals(document.getDocName(), document.getDocumentName())) {
      if (docChainConfigHelper.existsTopicDocId(document.getTenantId(), document.getTopicId(), document.getExtSystemId())) {
        docChainConfigHelper.deleteDocument(document.getTenantId(), document.getExtSystemId());
      }
      document.setDocName(document.getDocumentName());
    }
  }
  /**
   * 处理原文档归档文档重构
   */
  private void redoDocumentsAwaitingRetry(List<DocumentDTO> documents, Long tenantId, Long topicId) {
    List<DocmentInfo> docmentInfos = docChainConfigHelper.queryDocument(tenantId, topicId);
    if (CollectionUtils.isNotEmpty(docmentInfos)) {
      Map<Long, DocmentInfo> docmentInfoMap = docmentInfos.stream().collect(Collectors.toMap(DocmentInfo::getId, docmentInfo -> docmentInfo));
      for (DocumentDTO document : documents) {
        document.setErrorMessage(null);
        if (docmentInfoMap.get(document.getExtSystemId()) != null) {
          docChainConfigHelper.redoDocument(document.getTenantId(), document.getExtSystemId());
          document.setDocStatus(KnowledgeConsts.DOCUMENT_FROM_CRAWLING);
        }
        else {
          document.setDocStatus(KnowledgeConsts.DOCUMENT_STATUS_FAILED);
          document.setErrorMessage("源文件已删除，且docchain系统中对应知识库的文档已经删除，无法进行学习，请删除该知识文档重新添加知识！");
        }
        document.setParseStartedTime(new Date());
        document.setHasUpdate("0");
      }
    }
    else {
      for (DocumentDTO document : documents) {
        document.setDocStatus(KnowledgeConsts.DOCUMENT_STATUS_FAILED);
        document.setErrorMessage("源文件已删除，且docchain系统中对应知识库的文档已经删除，无法进行学习，请删除该知识文档重新添加知识！");
        document.setParseStartedTime(new Date());
        document.setHasUpdate("0");
      }
    }
  }

  /**
   * 重新上传文件到docchain
   */
  private void uploadNewDocuments(KnowledgeBaseDTO dto, Long topicId, List<DocumentDTO> uploads) {
    if (CollectionUtils.isEmpty(uploads)) {
      return;
    }
    docChainConfigHelper.saveDocumentNew(dto.getTenantId(), topicId, uploads, null);
  }

  /**
   * 更新知识库文档信息
   */
  private void updateNewDocumentMeta(List<DocumentDTO> documents, Long userId) {
    documents.forEach(document -> {
      document.setUpdatorId(userId);
      documentManageMapper.updateDocument(document);
    });
  }
}
