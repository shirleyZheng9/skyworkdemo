package com.iwhalecloud.bote.doc.module.document.service.impl;

import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.cache.WorkbookCache;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.constant.DocErrorCodeConsts;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.consts.DocumentPermConsts;
import com.iwhalecloud.bote.doc.module.base.service.impl.FileUploadHelper;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.WorkbookContentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.WorkbookContentHistoryDTO;
import com.iwhalecloud.bote.doc.module.document.dto.WorkbookContentHistoryInfoDTO;
import com.iwhalecloud.bote.doc.module.document.dto.WorkbookLockRequestDTO;
import com.iwhalecloud.bote.doc.module.document.dto.WorkbookLockStatusDTO;
import com.iwhalecloud.bote.doc.module.document.dto.WorkbookSaveRequestDTO;
import com.iwhalecloud.bote.doc.module.document.entity.WorkbookContentEntity;
import com.iwhalecloud.bote.doc.module.document.entity.WorkbookContentHistoryEntity;
import com.iwhalecloud.bote.doc.module.document.mapper.WorkbookContentHistoryMapper;
import com.iwhalecloud.bote.doc.module.document.mapper.WorkbookContentMapper;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.module.document.service.IWorkbookContentService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.BeanUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 工作簿服务实现类
 *
 * @author Aiqing
 * @since 2025-09-26
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class WorkbookContentServiceImpl implements IWorkbookContentService {

  private static final Logger logger = LoggerFactory.getLogger(WorkbookContentServiceImpl.class);
  // 默认锁定时长（分钟）
  private static final int DEFAULT_LOCK_MINUTES = 3;
  // 历史版本保存策略常量
  private static final int HISTORY_SAVE_DELAY_MINUTES = 5;
  private static final int HISTORY_SAVE_INTERVAL_MINUTES = 5;

  private final WorkbookCache workbookCache;
  private final WorkbookContentMapper workbookContentMapper;
  private final WorkbookContentHistoryMapper workbookContentHistoryMapper;
  private final IDocumentService documentService;
  private final IDcUserService dcUserService;
  private final ControlTemplate controlTemplate;
  private final FileUploadHelper fileUploadHelper;
  private final IFileStoreService fileStoreService;

  @Override
  public WorkbookLockStatusDTO getLockStatus(String documentId, Long userId, @Nullable String sessionId) {
    Assert.hasText(documentId, "文档ID不能为空");
    Assert.notNull(userId, "用户ID不能为空");
    if (StringUtils.isBlank(sessionId)) {
      sessionId = String.valueOf(userId);
    }
    // 检查文档是否存在
    checkDocumentExists(documentId);
    // 查询内容的最后版本号
    Long revision = workbookContentMapper.selectLastRevision(documentId);

    // 从缓存获取锁定状态
    WorkbookLockStatusDTO lockStatus = workbookCache.getLockStatus(documentId, userId, sessionId);
    lockStatus.setRevision(revision);
    return lockStatus;
  }

  @Override
  @Transactional
  public WorkbookLockStatusDTO lockWorkbook(WorkbookLockRequestDTO request, Long userId, @Nullable String sessionId) {
    Assert.notNull(request, "锁定请求参数不能为空");
    Assert.hasText(request.getDocumentId(), "文档ID不能为空");
    Assert.hasText(request.getLockAction(), "锁定操作类型不能为空");
    Assert.notNull(userId, "用户ID不能为空");
    if (StringUtils.isBlank(sessionId)) {
      sessionId = String.valueOf(userId);
    }

    String documentId = request.getDocumentId();
    String lockAction = request.getLockAction();

    // 检查文档是否存在
    DcDocumentDTO dcDocumentDTO = checkDocumentExists(documentId);

    // 查询内容的最后版本号
    Long revision = workbookContentMapper.selectLastRevision(documentId);

    // 检查用户是否有编辑权限
    controlTemplate.checkNodePermission(dcDocumentDTO.getLibraryId(), userId, documentId,
      NodePermission.EDIT_NODE, DocumentPermConsts.EDIT_DENIED_CALLBACK);

    if (DocBaseConsts.DOCUMENT_EDIT_ACTION_LOCK.equals(lockAction)) {
      WorkbookLockStatusDTO lockStatusDTO = lockDocument(documentId, userId, sessionId);
      lockStatusDTO.setRevision(revision);
      return lockStatusDTO;
    }
    else if (DocBaseConsts.DOCUMENT_EDIT_ACTION_UNLOCK.equals(lockAction)) {
      WorkbookLockStatusDTO lockStatusDTO = unlockDocument(documentId, userId, sessionId);
      lockStatusDTO.setRevision(revision);
      return lockStatusDTO;
    }
    else {
      throw new BssException(String.valueOf(HttpStatus.BAD_REQUEST.value()), "无效的锁定操作类型: " + lockAction);
    }
  }

  @Override
  @Transactional
  public WorkbookContentDTO saveWorkbook(WorkbookSaveRequestDTO request, Long userId) {
    Assert.notNull(request, "保存请求参数不能为空");
    Assert.hasText(request.getDocumentId(), "文档ID不能为空");
    Assert.hasText(request.getContent(), "工作簿内容不能为空");
    Assert.notNull(request.getRevision(), "版本号不能为空");
    Assert.notNull(userId, "用户ID不能为空");

    String documentId = request.getDocumentId();

    // 检查文档是否存在
    String libraryId = checkDocumentExistsAndReadPermission(documentId, userId);

    // 检查锁定状态
    String sessionId = SessionUtil.getSessionId();
    WorkbookLockStatusDTO lockStatus = workbookCache.getLockStatus(documentId, userId, sessionId);

    if (!lockStatus.getLocked()) {
      throw new BssException("请先获取文档锁，再进行编辑");
    }
    if (!lockStatus.getCanEdit()) {
      throw new BssException(DocErrorCodeConsts.DOCUMENT_LOCKED, "文档已被其他用户或会话锁定，无法保存");
    }
    // 检查版本冲突
    checkVersionConflict(documentId, request.getRevision());

    // 保存工作簿内容
    WorkbookContentDTO workbookContentDTO = saveWorkbookContent(request, userId);
    if (workbookContentDTO != null) {
      workbookContentDTO.setLibraryId(libraryId);
    }
    if (BaseSystemParameter.DOCUMENT_HIS_LOG_ENABLED.getBooleanValueFromDb()) {
      saveContentHistory(request.getDocumentId(), userId);
    }

    return workbookContentDTO;
  }

  @Override
  public WorkbookContentDTO getWorkbookContent(String documentId, Long userId) {
    Assert.hasText(documentId, "文档ID不能为空");
    Assert.notNull(userId, "用户ID不能为空");

    // 检查文档是否存在
    checkDocumentExistsAndReadPermission(documentId, userId);

    // 查询工作簿内容
    WorkbookContentEntity entity = workbookContentMapper.selectByDocumentId(documentId);
    if (entity == null) {
      // 如果不存在，创建初始内容
      entity = createInitialWorkbookContent(documentId, userId);
    }
    // 转换为DTO
    WorkbookContentDTO dto = BeanUtil.copy(entity, WorkbookContentDTO.class);
    // 填充用户信息
    fillUserInfo(dto);
    return dto;
  }

  @Override
  public void saveContentHistory(String documentId, Long updatorId) {
    Assert.hasText(documentId, "文档ID不能为空");
    Assert.notNull(updatorId, "更新人ID不能为空");

    // 查询当前工作簿内容
    WorkbookContentEntity current = workbookContentMapper.selectByDocumentId(documentId);
    if (current == null) {
      logger.warn("工作簿内容不存在，无法保存历史版本 - 文档ID: {}", documentId);
      return;
    }

    // 调用私有方法保存历史版本
    saveContentHistory(current);
  }

  @Override
  public List<WorkbookContentHistoryInfoDTO> getWorkbookVersions(String documentId) {
    if (StringUtils.isBlank(documentId)) {
      return new ArrayList<>();
    }
    List<WorkbookContentHistoryDTO> list = workbookContentHistoryMapper.getWorkbookVersions(documentId);
    if (list == null || list.isEmpty()) {
      list = new ArrayList<>();
      WorkbookContentEntity current = workbookContentMapper.selectByDocumentId(documentId);
      if (current != null) {
        list.add(buildCurrentWorkbookVersionDto(current));
      }
    }
    return groupWorkbookHistoryByCalendarDay(list);
  }

  /**
   * 将当前工作簿内容作为一条版本记录追加（创建时间取内容表更新时间，与历史查询结果一并分组）
   */
  private WorkbookContentHistoryDTO buildCurrentWorkbookVersionDto(WorkbookContentEntity entity) {
    WorkbookContentHistoryDTO dto = new WorkbookContentHistoryDTO();
    PortalUserDTO userDTO = dcUserService.findUserById(entity.getCreatorId());
    if (userDTO != null) {
      dto.setUserName(userDTO.getUserName());
    }
    dto.setContent(entity.getContent());
    dto.setRevision(entity.getRevision());
    dto.setCreatedTime(entity.getUpdatedTime());
    dto.setDocumentId(entity.getDocumentId());
    dto.setTenantId(entity.getTenantId());
    dto.setStatusCd(entity.getStatusCd());
    return dto;
  }

  /**
   * 按自然日分组：{@code revisionName} 当天为「今天」，其余为 {@code uuuu-MM-dd}；今天优先、其余日期从新到旧；组内按版本号倒序。
   */
  private List<WorkbookContentHistoryInfoDTO> groupWorkbookHistoryByCalendarDay(List<WorkbookContentHistoryDTO> list) {
    List<WorkbookContentHistoryInfoDTO> out = new ArrayList<>();
    if (list.isEmpty()) {
      return out;
    }
    ZoneId zone = ZoneId.systemDefault();
    LocalDate today = LocalDate.now(zone);
    Map<LocalDate, List<WorkbookContentHistoryDTO>> byDay = new LinkedHashMap<>();
    for (WorkbookContentHistoryDTO dto : list) {
      LocalDate day = dto.getCreatedTime().toInstant().atZone(zone).toLocalDate();
      byDay.computeIfAbsent(day, k -> new ArrayList<>()).add(dto);
    }
    for (List<WorkbookContentHistoryDTO> dayItems : byDay.values()) {
      sortWorkbookHistoryByRevisionDesc(dayItems);
    }
    List<WorkbookContentHistoryDTO> todayList = byDay.remove(today);
    if (todayList != null && !todayList.isEmpty()) {
      out.add(buildWorkbookHistoryGroup(DocBaseConsts.DOC_HISTORY_GROUP_NAME_TODAY, todayList));
    }
    for (Map.Entry<LocalDate, List<WorkbookContentHistoryDTO>> e : byDay.entrySet()) {
      out.add(buildWorkbookHistoryGroup(e.getKey().format(DocBaseConsts.DOC_HISTORY_OTHER_DAY_GROUP_KEY), e.getValue()));
    }
    return out;
  }

  private WorkbookContentHistoryInfoDTO buildWorkbookHistoryGroup(String revisionName,
    List<WorkbookContentHistoryDTO> data) {
    WorkbookContentHistoryInfoDTO chunk = new WorkbookContentHistoryInfoDTO();
    chunk.setRevisionName(revisionName);
    chunk.setData(data);
    return chunk;
  }

  /** 版本号降序 */
  private void sortWorkbookHistoryByRevisionDesc(List<WorkbookContentHistoryDTO> items) {
    items.sort(Comparator
      .comparing(WorkbookContentHistoryDTO::getRevision, Comparator.nullsLast(Comparator.naturalOrder()))
      .reversed());
  }

  @Override
  @Transactional
  public WorkbookContentDTO restoreContentVersion(String documentId, Long id) {
    WorkbookContentHistoryEntity history = requireWorkbookHistoryVersion(id);
    if (!documentId.equals(history.getDocumentId())) {
      throw new BssException("版本与文档不匹配");
    }
    WorkbookContentEntity dcDocContentEntity = workbookContentMapper.selectByDocumentId(documentId);
    if (dcDocContentEntity == null) {
      throw new BssException("源文件丢失！");
    }
    String contentToSave = readWorkbookHistoryFileContent(history);
    WorkbookSaveRequestDTO request = new WorkbookSaveRequestDTO();
    request.setDocumentId(documentId);
    request.setContent(contentToSave);
    request.setRevision(dcDocContentEntity.getRevision());
    if (dcDocContentEntity.getTenantId() != null) {
      request.setTenantId(dcDocContentEntity.getTenantId());
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    return saveWorkbook(request, userId);
  }

  @Override
  public String getWorkbookHisContent(Long id) {
    return readWorkbookHistoryFileContent(requireWorkbookHistoryVersion(id));
  }

  /**
   * 按历史版本主键查询记录，不存在则抛出业务异常。
   */
  private WorkbookContentHistoryEntity requireWorkbookHistoryVersion(Long id) {
    WorkbookContentHistoryEntity entity = workbookContentHistoryMapper.selectByVersionId(id);
    if (entity == null) {
      throw new BssException("指定版本不存在！");
    }
    return entity;
  }

  /**
   * 从历史版本关联的文件块读取文本内容；空串时使用默认工作簿内容。
   */
  private String readWorkbookHistoryFileContent(WorkbookContentHistoryEntity history) {
    Long fileBlockId = history.getFileBlockId();
    if (fileBlockId == null) {
      throw new BssException("历史版本文件缺失");
    }
    FileInfoVO fileInfo = fileStoreService.getFileInfoById(fileBlockId);
    if (fileInfo == null) {
      throw new BssException("历史版本文件不存在或已删除");
    }
    try (InputStream inputStream = fileStoreService.downloadFileStream(fileInfo)) {
      String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
      return StringUtils.isBlank(content) ? DocBaseConsts.DEFULT_WORK_BOOK_CONTENT : content;
    }
    catch (IOException e) {
      throw new BssException("读取历史版本文件失败: " + e.getMessage(), e);
    }
  }

  private DcDocumentDTO checkDocumentExists(String documentId) {
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    if (documentDTO == null) {
      throw new BssException(DocErrorCodeConsts.NODE_NOT_EXIST, "文档不存在");
    }
    return documentDTO;
  }

  /**
   * 检查文档是否存在
   * 检查文档访问权限
   */
  private String checkDocumentExistsAndReadPermission(String documentId, Long userId) {
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    if (documentDTO == null) {
      throw new BssException(DocErrorCodeConsts.NODE_NOT_EXIST, "文档不存在");
    }
    controlTemplate.checkNodePermission(documentDTO.getLibraryId(), userId, documentDTO.getDocumentId(),
      NodePermission.READ_NODE, DocumentPermConsts.ACCESS_DENIED_CALLBACK);
    return documentDTO.getLibraryId();
  }

  /**
   * 锁定文档
   */
  private WorkbookLockStatusDTO lockDocument(String documentId, Long userId, @NonNull String sessionId) {
    return workbookCache.lockDocument(documentId, userId, sessionId, DEFAULT_LOCK_MINUTES);
  }

  /**
   * 解锁文档
   */
  private WorkbookLockStatusDTO unlockDocument(String documentId, Long userId, @NonNull String sessionId) {
    return workbookCache.unlockDocument(documentId, userId, sessionId);
  }

  /**
   * 检查版本冲突
   */
  private void checkVersionConflict(String documentId, Long requestRevision) {
    WorkbookContentEntity current = workbookContentMapper.selectByDocumentId(documentId);
    if (current != null && !Objects.equals(current.getRevision(), requestRevision)) {
      throw new BssException(DocErrorCodeConsts.DOCUMENT_EDIT_VERSION_CONFLICT,
        "文档版本冲突，请刷新重试，当前版本: " + current.getRevision() + "，请求版本: " + requestRevision);
    }
  }

  /**
   * 保存工作簿内容
   */
  private WorkbookContentDTO saveWorkbookContent(WorkbookSaveRequestDTO request, Long userId) {
    String documentId = request.getDocumentId();
    // 查询当前内容
    WorkbookContentEntity current = workbookContentMapper.selectByDocumentId(documentId);
    if (current == null) {
      // 新建内容
      current = createWorkbookContentEntity(request, userId);
      workbookContentMapper.insert(current);
    }
    else {
      // 检查是否需要保存历史版本
      if (shouldSaveHistory(current, request.getContent())) {
        // 保存历史版本
        WorkbookContentEntity finalCurrent = current;
        // 此处开启新事务，防止分支逻辑导致异常回滚
        TransactionUtil.executeNew(() -> {
          saveContentHistory(finalCurrent);
        });
      }
      // 更新当前内容
      updateWorkbookContent(current, request, userId);
      workbookContentMapper.updateByDocumentId(current);
    }
    // 更新文档的最后修改人
    documentService.updateLastModify(documentId, userId);

    // 转换为DTO并返回
    WorkbookContentDTO dto = BeanUtil.copy(current, WorkbookContentDTO.class);
    fillUserInfo(dto);
    return dto;
  }

  /**
   * 判断是否需要保存历史版本
   *
   * @param current 当前工作簿内容
   * @param newContent 新的内容
   * @return true-需要保存历史版本，false-不需要保存
   */
  private boolean shouldSaveHistory(WorkbookContentEntity current, String newContent) {
    String documentId = current.getDocumentId();
    // 1. 检查内容是否有变化
    if (Objects.equals(current.getContent(), newContent)) {
      return false;
    }

    long currentTimeMs = System.currentTimeMillis();
    // 2. 检查文档创建时间，如果创建未超过5分钟，不保存历史版本
    if (current.getCreatedTime() != null) {
      long createTimeMs = current.getCreatedTime().getTime();
      long minutesFromCreate = (currentTimeMs - createTimeMs) / (1000 * 60);

      if (minutesFromCreate < HISTORY_SAVE_DELAY_MINUTES) {
        logger.trace("文档创建未超过{}分钟，跳过历史版本保存 - 文档ID: {}, 创建时间: {}, 已过时间: {}分钟",
          HISTORY_SAVE_DELAY_MINUTES, documentId, current.getCreatedTime(), minutesFromCreate);
        return false;
      }
    }

    // 3. 检查距离上一次历史版本创建时间，如果未超过5分钟，不保存历史版本
    WorkbookContentHistoryEntity lastHistory = workbookContentHistoryMapper.selectByMaxRevisionAndDocumentId(documentId);
    if (lastHistory != null && lastHistory.getCreatedTime() != null) {
      long lastHistoryTimeMs = lastHistory.getCreatedTime().getTime();
      long minutesFromLastHistory = (currentTimeMs - lastHistoryTimeMs) / (1000 * 60);

      if (minutesFromLastHistory < HISTORY_SAVE_INTERVAL_MINUTES) {
        logger.trace("距离上次历史版本保存未超过{}分钟，跳过历史版本保存 - 文档ID: {}, 上次保存时间: {}, 已过时间: {}分钟",
          HISTORY_SAVE_INTERVAL_MINUTES, documentId, lastHistory.getCreatedTime(), minutesFromLastHistory);
        return false;
      }
    }
    return true;
  }

  /**
   * 创建初始工作簿内容
   */
  private WorkbookContentEntity createInitialWorkbookContent(String documentId, Long userId) {
    WorkbookContentEntity entity = new WorkbookContentEntity();
    entity.setId(IDUtils.nextId());
    entity.setDocumentId(documentId);
    // 空的JSON内容
    entity.setContent(DocBaseConsts.DEFULT_WORK_BOOK_CONTENT);
    entity.setRevision(DocBaseConsts.DOCUMENT_INIT_REVISION);
    entity.setCreatorId(userId);
    entity.setUpdatorId(userId);
    entity.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    workbookContentMapper.insert(entity);
    return entity;
  }

  /**
   * 创建工作簿内容实体
   */
  private WorkbookContentEntity createWorkbookContentEntity(WorkbookSaveRequestDTO request, Long userId) {
    WorkbookContentEntity entity = new WorkbookContentEntity();
    entity.setId(IDUtils.nextId());
    entity.setDocumentId(request.getDocumentId());
    entity.setContent(request.getContent());
    // 新建时版本号为1
    entity.setRevision(DocBaseConsts.DOCUMENT_INIT_REVISION);
    entity.setCreatorId(userId);
    entity.setUpdatorId(userId);
    entity.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    return entity;
  }

  /**
   * 更新工作簿内容
   */
  private void updateWorkbookContent(WorkbookContentEntity entity, WorkbookSaveRequestDTO request, Long userId) {
    entity.setContent(request.getContent());
    // 版本号递增
    entity.setRevision(entity.getRevision() + 1);
    entity.setUpdatorId(userId);
  }

  /**
   * 保存内容历史
   */
  private void saveContentHistory(WorkbookContentEntity current) {
    try {
      String documentId = current.getDocumentId();
      Long revision = current.getRevision();

      // 检查是否已存在相同documentId和version的历史版本记录
      WorkbookContentHistoryEntity existingHistory = workbookContentHistoryMapper.selectByDocumentIdAndRevision(documentId, revision);
      if (existingHistory != null) {
        logger.trace("历史版本已存在，跳过保存 - 文档ID: {}, 版本号: {}", documentId, revision);
        return;
      }

      WorkbookContentHistoryEntity history = new WorkbookContentHistoryEntity();
      history.setId(IDUtils.nextId());
      history.setDocumentId(documentId);

      // 将内容保存到文件系统
      String content = current.getContent();
      if (content == null) {
        // 空内容默认为空JSON
        content = "{}";
      }
      byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);

      ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes);
      String fileName = String.format("workbook_history_%s_v%s.json", documentId, revision);
      FileInfoVO fileInfoVO = fileUploadHelper.uploadFile(inputStream, fileName, (long) contentBytes.length);

      history.setFileBlockId(fileInfoVO.getFileId());
      history.setRevision(revision);
      history.setCreatorId(current.getUpdatorId());
      history.setUpdatorId(current.getUpdatorId());
      history.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
      history.setTenantId(current.getTenantId());

      workbookContentHistoryMapper.insert(history);

      logger.trace("历史版本保存成功 - 文档ID: {}, 版本号: {}, 文件ID: {}, 内容大小: {}字节",
        documentId, revision, fileInfoVO.getFileId(), contentBytes.length);
    }
    catch (Exception e) {
      logger.error("历史版本保存失败 - 文档ID: {}, 版本号: {}",
        current.getDocumentId(), current.getRevision(), e);
      // 此处不抛出异常，只记录日志
    }
  }

  /**
   * 填充用户信息
   */
  private void fillUserInfo(WorkbookContentDTO dto) {
    if (dto.getCreatorId() != null) {
      PortalUserDTO creator = dcUserService.findUserById(dto.getCreatorId());
      if (creator != null) {
        dto.setCreatorName(creator.getUserName());
      }
    }
    if (dto.getUpdatorId() != null) {
      PortalUserDTO updator = dcUserService.findUserById(dto.getUpdatorId());
      if (updator != null) {
        dto.setUpdatorName(updator.getUserName());
      }
    }
  }
}
