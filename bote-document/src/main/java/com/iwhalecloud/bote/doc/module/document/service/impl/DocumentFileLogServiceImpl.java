package com.iwhalecloud.bote.doc.module.document.service.impl;

import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentFileLogDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentFileLogInfoDTO;
import com.iwhalecloud.bote.doc.module.document.entity.DcDocumentEntity;
import com.iwhalecloud.bote.doc.module.document.entity.DocumentFileLogEntity;
import com.iwhalecloud.bote.doc.module.document.mapper.DocumentFileLogMapper;
import com.iwhalecloud.bote.doc.module.document.mapper.DocumentMapper;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentFileLogService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.mapper.base.FileInfoManageMapper;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文档上传文件日志服务实现
 *
 * @author bote-doc
 * @since 2026-04-15
 */
@Service
@RequiredArgsConstructor
public class DocumentFileLogServiceImpl implements IDocumentFileLogService {

  private static final Logger logger = LoggerFactory.getLogger(DocumentFileLogServiceImpl.class);

  private final DocumentFileLogMapper documentFileLogMapper;
  private final FileInfoManageMapper fileInfoManageMapper;
  private final DocumentMapper documentMapper;
  private final IDcUserService dcUserService;

  @Override
  @Transactional
  public void append(String documentId, Long fileId, Long revision, Long creatorId, Long tenantId) {
    if (!BaseSystemParameter.DOCUMENT_HIS_LOG_ENABLED.getBooleanValueFromDb()) {
      return;
    }
    insertDocumentFileLog(documentId, fileId, revision, creatorId, tenantId);
  }

  @Override
  @Transactional
  public void appendByFileInfoId(String documentId, Long fileInfoId, Long revision, Long creatorId, Long tenantId) {
    if (StringUtils.isBlank(documentId) || fileInfoId == null || creatorId == null || tenantId == null) {
      logger.warn(
        "Skip document file log, invalid params: documentId={}, fileInfoId={}, creatorId={}, tenantId={}",
        documentId, fileInfoId, creatorId, tenantId);
      return;
    }
    FileInfoDTO fileInfo = fileInfoManageMapper.getFileInfo(tenantId, fileInfoId);
    if (fileInfo == null || fileInfo.getFileId() == null) {
      logger.warn("Skip document file log, file info missing: documentId={}, fileInfoId={}", documentId, fileInfoId);
      return;
    }
    insertDocumentFileLog(documentId, fileInfo.getFileId(), revision, creatorId, tenantId);
  }

  /**
   * 写入 {@code bt_dc_document_file_log}（调用方已确认开关开启）
   */
  private void insertDocumentFileLog(String documentId, Long fileId, Long revision, Long creatorId, Long tenantId) {
    if (StringUtils.isBlank(documentId) || fileId == null || creatorId == null || tenantId == null) {
      logger.warn("Skip document file log, invalid params: documentId={}, fileId={}, creatorId={}, tenantId={}",
        documentId, fileId, creatorId, tenantId);
      return;
    }
    DocumentFileLogEntity log = new DocumentFileLogEntity();
    log.setId(IDUtils.nextId());
    log.setDocumentId(documentId);
    log.setFileId(fileId);
    log.setRevision(revision);
    log.setTenantId(tenantId);
    log.setCreatorId(creatorId);
    log.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    documentFileLogMapper.insert(log);
  }

  @Override
  public DocumentFileLogDTO getDetailById(Long id, Long tenantId) {
    if (id == null || tenantId == null) {
      return null;
    }
    return documentFileLogMapper.selectDetailById(id, tenantId);
  }

  @Override
  public List<DocumentFileLogInfoDTO> listDetailByDocumentId(@Nullable String documentId, @Nullable Long tenantId) {
    if (StringUtils.isBlank(documentId) || tenantId == null) {
      return new ArrayList<>();
    }
    List<DocumentFileLogDTO> list = documentFileLogMapper.selectDetailListByDocumentId(documentId, tenantId);
    if (list == null || list.isEmpty()) {
      list = new ArrayList<>();
      DcDocumentEntity documentEntity = documentMapper.selectByDocumentId(documentId);
      if (documentEntity != null) {
        DocumentFileLogDTO documentFileLogDTO = new DocumentFileLogDTO();
        documentFileLogDTO.setDocumentId(documentEntity.getDocumentId());
        documentFileLogDTO.setFileName(documentEntity.getDocumentName());
        documentFileLogDTO.setRevision(documentEntity.getRevision());
        documentFileLogDTO.setCreatorId(documentEntity.getCreatorId());
        documentFileLogDTO.setTenantId(documentEntity.getTenantId());
        documentFileLogDTO.setCreatedTime(documentEntity.getUpdatedTime());
        PortalUserDTO userDTO = dcUserService.findUserById(documentEntity.getCreatorId());
        if (userDTO != null) {
          documentFileLogDTO.setUserName(userDTO.getUserName());
        }
        list.add(documentFileLogDTO);
      }
    }
    return groupByCalendarDay(list);
  }

  /**
   * 按自然日分组为 {@link DocumentFileLogInfoDTO} 列表：{@code revisionName} 当天为「今天」，其余为 {@code uuuu-MM-dd}；
   * 顺序为今天优先、其余日期从新到旧；每组 {@code data} 内按创建时间倒序。
   */
  private List<DocumentFileLogInfoDTO> groupByCalendarDay(List<DocumentFileLogDTO> list) {
    List<DocumentFileLogInfoDTO> out = new ArrayList<>();
    if (list.isEmpty()) {
      return out;
    }
    ZoneId zone = ZoneId.systemDefault();
    LocalDate today = LocalDate.now(zone);
    Map<LocalDate, List<DocumentFileLogDTO>> byDay = new LinkedHashMap<>();
    for (DocumentFileLogDTO dto : list) {
      LocalDate day = dto.getCreatedTime().toInstant().atZone(zone).toLocalDate();
      byDay.computeIfAbsent(day, k -> new ArrayList<>()).add(dto);
    }
    for (List<DocumentFileLogDTO> dayItems : byDay.values()) {
      sortFileLogsNewestFirst(dayItems);
    }
    List<DocumentFileLogDTO> todayList = byDay.remove(today);
    if (todayList != null && !todayList.isEmpty()) {
      out.add(buildGroup(DocBaseConsts.DOC_HISTORY_GROUP_NAME_TODAY, todayList));
    }
    for (Map.Entry<LocalDate, List<DocumentFileLogDTO>> e : byDay.entrySet()) {
      out.add(buildGroup(e.getKey().format(DocBaseConsts.DOC_HISTORY_OTHER_DAY_GROUP_KEY), e.getValue()));
    }
    return out;
  }

  private static DocumentFileLogInfoDTO buildGroup(String revisionName, List<DocumentFileLogDTO> data) {
    DocumentFileLogInfoDTO chunk = new DocumentFileLogInfoDTO();
    chunk.setRevisionName(revisionName);
    chunk.setData(data);
    return chunk;
  }

  /** 创建时间降序 */
  private static void sortFileLogsNewestFirst(List<DocumentFileLogDTO> items) {
    items.sort(Comparator
      .comparing(DocumentFileLogDTO::getCreatedTime, Comparator.nullsLast(Comparator.naturalOrder()))
      .reversed());
  }
}
