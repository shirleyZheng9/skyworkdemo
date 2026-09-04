package com.iwhalecloud.bote.doc.module.collaboration.workbook.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.doc.module.collaboration.cache.WorkbookCollaborationCache;
import com.iwhalecloud.bote.doc.module.collaboration.constant.SocketBizEnum;
import com.iwhalecloud.bote.doc.module.collaboration.constant.SocketConst;
import com.iwhalecloud.bote.doc.module.collaboration.socket.service.SocketMessageSendService;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.UniverCmdType;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.AckEventCollaMsgDTO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.AckEventCollaMsgDTO.AckEvent;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.ChangesetDTO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.ChangesetMutationDTO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.SheetDTO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.socket.UniverCommonDownDTO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.ro.NewChangesRO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.vo.SheetBlockDataVO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.vo.SheetBlockDataVO.BlockData;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.vo.SheetBlockDataVO.RowData;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.vo.WorkbookDetailVO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.vo.WorkbookDetailVO.BlockMeta;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.vo.WorkbookDetailVO.WorkbookSnapshot;
import com.iwhalecloud.bote.doc.module.document.entity.SheetBlockEntity;
import com.iwhalecloud.bote.doc.module.document.entity.WorkbookChangesetEntity;
import com.iwhalecloud.bote.doc.module.document.entity.WorkbookSnapshotEntity;
import com.iwhalecloud.bote.doc.module.document.entity.WorksheetEntity;
import com.iwhalecloud.bote.doc.module.document.service.ISheetBlockService;
import com.iwhalecloud.bote.doc.module.document.service.IWorkbookChangesetService;
import com.iwhalecloud.bote.doc.module.document.service.IWorkbookService;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 在线表格的逻辑处理
 *
 * @author Aiqing
 * @since 2025/9/2
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class WorkbookFacade {

  private static final Logger logger = LoggerFactory.getLogger(WorkbookFacade.class);
  /**
   * 快照保存的间隔changeset数
   */
  private static final Integer SAVE_SHEET_SNAPSHOT_INTERVAL = 50;
  //  private final IDocumentService documentService;
  private final IWorkbookService workbookService;
  private final IWorkbookChangesetService workbookChangesetService;
  private final ISheetBlockService sheetBlockService;
  private final IFileStoreService fileStoreService;
  private final SocketMessageSendService socketMessageSendService;
  private final WorkbookCollaborationCache workbookCollaborationCache;

  private static List<ChangesetDTO> buildChangesets(String documentId,
                                                    List<WorkbookChangesetEntity> changesetEntityList) {
    return changesetEntityList.stream()
      .map(item -> {
        ChangesetDTO changesetDTO = new ChangesetDTO();
        changesetDTO.setUnitID(documentId);
        changesetDTO.setBaseRev(item.getBaseRev());
        changesetDTO.setRevision(item.getRevision());
        changesetDTO.setMutations(JsonUtil.parseJson(item.getMutations(),
          new TypeReference<List<ChangesetMutationDTO>>() {
          }));
        changesetDTO.setSid(item.getSid());
        changesetDTO.setReqId(item.getReqId());
        return changesetDTO;
      }).collect(Collectors.toList());
  }

  private static WorkbookDetailVO.WorkbookSnapshot buildWorkbookSnapshot(String documentId,
                                                                         Long revision,
                                                                         String documentName,
                                                                         List<WorksheetEntity> worksheetEntityList) {
    WorkbookDetailVO.Workbook workbook = new WorkbookDetailVO.Workbook();
    workbook.setUnitID(documentId);
    workbook.setRev(revision);
    // todo 创建人

    workbook.setName(documentName);

    List<String> sheetIdList = worksheetEntityList.stream()
      .map(WorksheetEntity::getSheetId).collect(Collectors.toList());
    workbook.setSheetOrder(sheetIdList);

    Map<String, SheetDTO> sheetDTOMap = worksheetEntityList.stream()
      .collect(Collectors.toMap(WorksheetEntity::getSheetId, item -> {
        SheetDTO sheetDTO = new SheetDTO();
        sheetDTO.setType(0);
        sheetDTO.setId(item.getSheetId());
        sheetDTO.setName(item.getName());
        sheetDTO.setRowCount(item.getRowCount());
        sheetDTO.setColumnCount(item.getColumnCount());
        sheetDTO.setOriginalMeta(item.getOriginalMeta());
        return sheetDTO;
      }, (x, y) -> y));
    Map<String, BlockMeta> blockMetaMap = worksheetEntityList.stream()
      .collect(Collectors.toMap(WorksheetEntity::getSheetId, item -> {
        BlockMeta blockMeta = new BlockMeta();
        blockMeta.setSheetID(item.getSheetId());
        blockMeta.setBlocks(Collections.singletonList(item.getBlockId()));
        return blockMeta;
      }, (x, y) -> y));
    workbook.setSheets(sheetDTOMap);
    workbook.setBlockMeta(blockMetaMap);

    WorkbookSnapshot workbookSnapshot = new WorkbookSnapshot();
    workbookSnapshot.setUnitID(documentId);
    workbookSnapshot.setRev(revision);
    workbookSnapshot.setWorkbook(workbook);
    return workbookSnapshot;
  }

  /**
   * 查询在线表格的详情数据
   *
   * @param documentId 表格ID
   * @return 表格详情
   */
  public WorkbookDetailVO queryWorkbookSnapshot(String documentId) {
    // todo 文档访问鉴权
    // todo 查询文档信息
    String documentName = "测试文档";

    // 1. 查询最大revision的 worksheet snapshot
    WorkbookSnapshotEntity workbookSnapshotEntity = workbookService.queryLastRevisionSnapshot(documentId);
    // 2. 根据snapshotId查询bt_dc_worksheet中所有的sheet信息，关联block
    Long snapshotId = workbookSnapshotEntity.getId();
    List<WorksheetEntity> worksheetEntityList = workbookService.selectWorkbookSheetsBySnapshotId(documentId, snapshotId);
    // 3. 根据revision查询 workbook_changeset，查询出在快照记录后的变更
    Long revision = workbookSnapshotEntity.getRevision();
    List<WorkbookChangesetEntity> changesetEntityList = workbookChangesetService.selectByDocumentIdAndRevision(documentId, revision);
    // 4. 组装数据
    WorkbookSnapshot workbookSnapshot = buildWorkbookSnapshot(documentId, revision, documentName, worksheetEntityList);

    List<ChangesetDTO> changesetList = buildChangesets(documentId, changesetEntityList);

    WorkbookDetailVO detailVO = new WorkbookDetailVO();
    detailVO.setSnapshot(workbookSnapshot);
    detailVO.setChangesets(changesetList);

    detailVO.setError(detailVO.successResult());
    return detailVO;
  }

  /**
   * 查询sheet的数据
   *
   * @param documentId 文档ID
   * @param blockId sheet存储的blockId
   * @return sheet数据
   */
  public SheetBlockDataVO querySheetBlockData(String documentId, Long blockId) {
    // todo 文档访问鉴权
    WorksheetEntity worksheetEntity = workbookService.queryWorksheetByDocumentIdAndBlockId(documentId, blockId);
    if (worksheetEntity == null) {
      return SheetBlockDataVO.emptyData();
    }
    SheetBlockEntity sheetBlockEntity = sheetBlockService.queryByBlockId(blockId);
    if (sheetBlockEntity == null) {
      return SheetBlockDataVO.emptyData();
    }
    String sheetId = sheetBlockEntity.getSheetId();
    Assert.isTrue(Objects.equals(worksheetEntity.getSheetId(), sheetId), "数据参数不一致");

    Long fileId = sheetBlockEntity.getFileId();
    if (fileId == null) {
      logger.warn("表格文件ID字段为空， documentId:{}, blockId:{}", documentId, blockId);
      return SheetBlockDataVO.emptyData();
    }
    byte[] bytes = fileStoreService.downloadFile(fileId);
    String content = new String(Base64.getDecoder().decode(bytes), StandardCharsets.UTF_8);
    BlockData blockData = new BlockData();
    blockData.setId(blockId);
    blockData.setEndRow(sheetBlockEntity.getEndRow());
    blockData.setData(JsonUtil.parseJson(content, RowData.class));

    SheetBlockDataVO dataVO = new SheetBlockDataVO();
    dataVO.setBlock(blockData);
    dataVO.setError(dataVO.successResult());
    return dataVO;
  }


  /**
   * 在线表格数据变更
   * 此方法异步执行，使用socket给事件发生方返回确认信息，并广播此次变更
   *
   * @param documentId 文档ID
   * @param changesRO 请求参数
   */
  @Async
  @SuppressWarnings("PMD.GuardLogStatement")
  public void newChanges(String documentId, NewChangesRO changesRO, Long userId) {
    ChangesetDTO changeset = changesRO.getChangeset();
    logger.info("接收到表格变更请求: documentId={}, baseRev={}, mutations={}",
      documentId, changeset.getBaseRev(), changeset.getMutations().size());
    try {
      // 1. 构建变更消息
      ChangesetMessage changesetMessage = new ChangesetMessage();
      changesetMessage.setDocumentId(documentId);
      changesetMessage.setBaseRev(changeset.getBaseRev());
      changesetMessage.setMutations(changeset.getMutations());
      changesetMessage.setSid(changeset.getSid());
      changesetMessage.setReqId(changeset.getReqId());
      changesetMessage.setTimestamp(System.currentTimeMillis());
      changesetMessage.setUserId(userId);

      // 3. 放入Redis队列
      workbookCollaborationCache.pushToChangesetQueue(documentId, changesetMessage);
    }
    catch (Exception e) {
      logger.error("处理表格变更请求失败: documentId={}", documentId, e);
      // 发送错误确认消息
      sendErrorAck(changeset.getSid(), changeset.getReqId(), "变更处理失败: " + e.getMessage());
    }
  }

//  /**
//   * 消费工作簿变更队列
//   * 此方法应该由定时任务或消息队列消费者调用
//   */
//  @Async
//  public void consumerWorkbookChanges() {
//    // 获取所有活跃的文档队列
//    String pattern = SocketConst.CHANGESET_QUEUE_KEY + "*";
//    Set<String> queueKeys = redisTemplate.keys(pattern);
//    if (CollectionUtils.isEmpty(queueKeys)) {
//      return;
//    }
//    for (String queueKey : queueKeys) {
//      processDocumentChangesets(queueKey);
//    }
//  }

  /**
   * 处理单个文档的变更队列
   */
  public void processDocumentChangesets(String queueKey) {
    try {
      // 从Redis队列中批量获取变更消息
      List<String> messages = workbookCollaborationCache.popChangesetQueue(queueKey, 10);
      if (messages == null || messages.isEmpty()) {
        return;
      }
      String documentId = extractDocumentIdFromQueueKey(queueKey);
      logger.info("开始处理文档变更: documentId={}, 变更数量={}", documentId, messages.size());

      for (String msgObj : messages) {
        ChangesetMessage message = JsonUtil.parseJson(msgObj, ChangesetMessage.class);
        processSingleChangeset(documentId, message);
      }
    }
    catch (Exception e) {
      logger.error("处理文档变更队列失败: queueKey={}", queueKey, e);
    }
  }

  /**
   * 处理单个变更集
   */
  private void processSingleChangeset(String documentId, ChangesetMessage message) {
    try {
      // 2. 使用OT算法解决冲突
      ChangesetMessage resolvedMessage = resolveConflictWithOT(documentId, message);

      // 3. 持久化变更集
      WorkbookChangesetEntity changesetEntity = buildChangesetEntity(documentId, resolvedMessage);
      workbookChangesetService.save(changesetEntity);

      // 4. 检查是否需要创建新快照
      checkAndCreateSnapshot(documentId, changesetEntity.getRevision());

      // 5. 发送确认消息给发起者
      sendSuccessAck(resolvedMessage.getSid(), resolvedMessage.getReqId(), changesetEntity);

      // 6. 广播变更给其他协作者
      broadcastChangeset(documentId, changesetEntity, resolvedMessage.getSid());

      logger.info("变更处理完成: documentId={}, revision={}", documentId, changesetEntity.getRevision());

    }
    catch (Exception e) {
      logger.error("处理单个变更集失败: documentId={}, reqId={}", documentId, message.getReqId(), e);
      sendErrorAck(message.getSid(), message.getReqId(), "变更处理失败: " + e.getMessage());
    }
  }

  /**
   * 使用OT算法解决变更冲突
   */
  private ChangesetMessage resolveConflictWithOT(String documentId, ChangesetMessage message) {
    Long currentRevision = workbookChangesetService.getCurrentRevision(documentId);

    // 如果基础版本与当前版本一致，无需冲突解决
    if (Objects.equals(message.getBaseRev(), currentRevision)) {
      message.setBaseRev(currentRevision);
      return message;
    }

    logger.info("检测到版本冲突，开始OT算法解决: baseRev={}, currentRev={}",
      message.getBaseRev(), currentRevision);

    // 1. 获取冲突区间的所有变更
    List<WorkbookChangesetEntity> conflictChangesets = workbookChangesetService
      .selectByDocumentIdAndRevisionRange(message.getDocumentId(), message.getBaseRev() + 1, currentRevision);
    if (CollectionUtils.isEmpty(conflictChangesets)) {
      return message;
    }

    // 2. 应用OT变换算法
    List<ChangesetMutationDTO> transformedMutations = applyOperationalTransform(message.getMutations(), conflictChangesets);

    // 3. 更新消息
    message.setMutations(transformedMutations);
    message.setBaseRev(currentRevision);

    logger.info("OT算法冲突解决完成: 原始变更数={}, 转换后变更数={}",
      message.getMutations().size(), transformedMutations.size());

    return message;
  }

  /**
   * 应用操作变换算法
   */
  private List<ChangesetMutationDTO> applyOperationalTransform(
    List<ChangesetMutationDTO> mutations,
    List<WorkbookChangesetEntity> conflictChangesets) {

    List<ChangesetMutationDTO> transformedMutations = new ArrayList<>(mutations);

    // 对每个冲突的变更集进行变换
    for (WorkbookChangesetEntity conflictChangeset : conflictChangesets) {
      List<ChangesetMutationDTO> conflictMutations = JsonUtil.parseJson(
        conflictChangeset.getMutations(), new TypeReference<List<ChangesetMutationDTO>>() {
        });

      // 应用变换算法
      transformedMutations = transformMutations(transformedMutations, conflictMutations);
    }

    return transformedMutations;
  }

  /**
   * 变换操作算法核心逻辑
   */
  private List<ChangesetMutationDTO> transformMutations(
    List<ChangesetMutationDTO> mutations,
    List<ChangesetMutationDTO> conflictMutations) {

    List<ChangesetMutationDTO> result = new ArrayList<>();

    for (ChangesetMutationDTO mutation : mutations) {
      ChangesetMutationDTO transformedMutation = mutation;

      // 对每个冲突操作进行变换
      for (ChangesetMutationDTO conflictMutation : conflictMutations) {
        transformedMutation = transformSingleMutation(transformedMutation, conflictMutation);
      }

      if (transformedMutation != null) {
        result.add(transformedMutation);
      }
    }

    return result;
  }

  /**
   * 单个操作的变换逻辑
   */
  private ChangesetMutationDTO transformSingleMutation(
    ChangesetMutationDTO mutation,
    ChangesetMutationDTO conflictMutation) {

    // 简化的OT算法实现
    // 实际项目中需要根据具体的操作类型（插入、删除、修改）实现更复杂的变换逻辑

    if (Objects.equals(mutation.getId(), conflictMutation.getId())) {
      // 如果是相同类型的操作，需要进行位置调整
      return adjustMutationPosition(mutation, conflictMutation);
    }

    return mutation; // 不同类型操作，直接返回原操作
  }

  /**
   * 调整操作位置（简化实现）
   */
  private ChangesetMutationDTO adjustMutationPosition(
    ChangesetMutationDTO mutation,
    ChangesetMutationDTO conflictMutation) {

    // 这里需要根据具体的操作数据结构实现位置调整逻辑
    // 例如：如果是单元格操作，需要调整行列坐标
    logger.debug("调整操作位置: mutation={}, conflict={}", mutation.getId(), conflictMutation.getId());

    return mutation;
  }

  /**
   * 检查并创建新快照
   */
  private void checkAndCreateSnapshot(String documentId, Long currentRevision) {
    try {
      // 获取最后一次快照的版本号
      WorkbookSnapshotEntity lastSnapshot = workbookService.queryLastRevisionSnapshot(documentId);
      Long lastSnapshotRevision = lastSnapshot != null ? lastSnapshot.getRevision() : 0L;

      // 判断是否需要创建新快照
      if (currentRevision - lastSnapshotRevision >= SAVE_SHEET_SNAPSHOT_INTERVAL) {
        logger.info("开始创建新快照: documentId={}, currentRev={}, lastSnapshotRev={}",
          documentId, currentRevision, lastSnapshotRevision);

        createNewSnapshot(documentId, currentRevision);
      }

    }
    catch (Exception e) {
      logger.error("检查快照失败: documentId={}, revision={}", documentId, currentRevision, e);
    }
  }

  /**
   * 创建新快照
   */
  public void createNewSnapshot(String documentId, Long revision) {
//    try {
//      // 1. 保存快照
//      workbookService.saveWorkbookSnapshot(documentId, revision);
//
//      // 2. 复制当前的WorksheetEntity作为新快照的工作表
//      List<WorksheetEntity> currentWorksheets = workbookService.selectWorkbookSheetsByDocumentId(documentId);
//      for (WorksheetEntity worksheet : currentWorksheets) {
//        WorksheetEntity newWorksheet = cloneWorksheetForSnapshot(worksheet, snapshotEntity.getSnapshotId());
//        workbookService.saveWorksheet(newWorksheet);
//      }
//
//      logger.info("新快照创建完成: documentId={}, snapshotId={}, revision={}",
//        documentId, snapshotEntity.getSnapshotId(), revision);
//
//    }
//    catch (Exception e) {
//      logger.error("创建新快照失败: documentId={}, revision={}", documentId, revision, e);
//    }
  }

  /**
   * 克隆工作表用于快照
   */
  public WorksheetEntity cloneWorksheetForSnapshot(WorksheetEntity original, Long snapshotId) {
    WorksheetEntity clone = new WorksheetEntity();
    clone.setDocumentId(original.getDocumentId());
    clone.setSheetId(original.getSheetId());
    clone.setSnapshotId(snapshotId);
    clone.setName(original.getName());
    clone.setRowCount(original.getRowCount());
    clone.setColumnCount(original.getColumnCount());
    clone.setIndex(original.getIndex());
    clone.setBlockId(original.getBlockId());
    clone.setOriginalMeta(original.getOriginalMeta());
    clone.setTenantId(original.getTenantId());
    clone.setStatusCd(original.getStatusCd());
    return clone;
  }

  /**
   * 构建变更集实体
   */
  private WorkbookChangesetEntity buildChangesetEntity(String documentId, ChangesetMessage message) {
    WorkbookChangesetEntity entity = new WorkbookChangesetEntity();
    entity.setDocumentId(documentId);
    entity.setBaseRev(message.getBaseRev());
    entity.setRevision(message.getBaseRev() + 1); // 新版本号
    entity.setMutations(JsonUtil.toJsonString(message.getMutations()));
    entity.setSid(message.getSid());
    entity.setReqId(message.getReqId());
    entity.setUserId(message.getUserId());
    entity.setCreatedTime(new Date());
    return entity;
  }

  /**
   * 从队列Key中提取文档ID
   */
  private String extractDocumentIdFromQueueKey(String queueKey) {
    return queueKey.replace(SocketConst.CHANGESET_QUEUE_KEY, "");
  }

  /**
   * 发送成功确认消息
   */
  private void sendSuccessAck(String sid, Long reqId, WorkbookChangesetEntity changesetEntity) {
    try {
      AckEventCollaMsgDTO ackEventCollaMsgDTO = new AckEventCollaMsgDTO();
      ackEventCollaMsgDTO.setEventID("changeset_ack");

      ChangesetDTO changesetDTO = new ChangesetDTO();
      changesetDTO.setUnitID(changesetEntity.getDocumentId());
      changesetDTO.setBaseRev(changesetEntity.getBaseRev());
      changesetDTO.setRevision(changesetEntity.getRevision());
      changesetDTO.setMutations(JsonUtil.parseJson(changesetEntity.getMutations(),
        new TypeReference<List<ChangesetMutationDTO>>() {
        }));
      changesetDTO.setSid(sid);
      changesetDTO.setReqId(reqId);

      AckEventCollaMsgDTO.AckEvent ackEvent = new AckEvent();
      ackEvent.setCs(changesetDTO);
      ackEventCollaMsgDTO.setCsAckEvent(ackEvent);

      UniverCommonDownDTO commonDownDTO = UniverCommonDownDTO.success(
        UniverCmdType.CHANGESET.getCode(), null, ackEventCollaMsgDTO, null);

      // 发送确认消息给特定会话
      socketMessageSendService.sendToSession(sid, commonDownDTO);

      logger.debug("发送成功确认消息: sid={}, reqId={}, revision={}", sid, reqId, changesetEntity.getRevision());

    }
    catch (Exception e) {
      logger.error("发送成功确认消息失败: sid={}, reqId={}", sid, reqId, e);
    }
  }

  /**
   * 发送错误确认消息
   */
  private void sendErrorAck(String sid, Long reqId, String errorMessage) {
    try {
      UniverCommonDownDTO errorDownDTO = UniverCommonDownDTO.fail(
        UniverCmdType.CHANGESET.getCode(), null, errorMessage, null);
      socketMessageSendService.sendToSession(sid, errorDownDTO);
      logger.debug("发送错误确认消息: sid={}, reqId={}, error={}", sid, reqId, errorMessage);
    }
    catch (Exception e) {
      logger.error("发送错误确认消息失败: sid={}, reqId={}", sid, reqId, e);
    }
  }

  /**
   * 广播变更给其他协作者
   */
  private void broadcastChangeset(String documentId, WorkbookChangesetEntity changesetEntity, String excludeSid) {
    try {
      ChangesetDTO changesetDTO = new ChangesetDTO();
      changesetDTO.setUnitID(documentId);
      changesetDTO.setBaseRev(changesetEntity.getBaseRev());
      changesetDTO.setRevision(changesetEntity.getRevision());
      changesetDTO.setMutations(JsonUtil.parseJson(changesetEntity.getMutations(),
        new TypeReference<List<ChangesetMutationDTO>>() {
        }));
      changesetDTO.setSid(changesetEntity.getSid());
      changesetDTO.setReqId(changesetEntity.getReqId());

      UniverCommonDownDTO broadcastDTO = UniverCommonDownDTO.success(
        UniverCmdType.CHANGESET.getCode(), null, changesetDTO, null);

      // 广播给文档的所有协作者（排除发起者）
      socketMessageSendService.sendToBusinessExcludeSession(SocketBizEnum.WORKSHEET.name(),
        documentId, excludeSid, broadcastDTO);

      logger.debug("广播变更完成: documentId={}, revision={}, excludeSid={}",
        documentId, changesetEntity.getRevision(), excludeSid);

    }
    catch (Exception e) {
      logger.error("广播变更失败: documentId={}, revision={}", documentId, changesetEntity.getRevision(), e);
    }
  }

  /**
   * 变更消息实体
   */
  @Getter
  @Setter
  @ToString
  public static final class ChangesetMessage {

    private String sourceSessionId;
    private String documentId;
    private Long baseRev;
    private List<ChangesetMutationDTO> mutations;
    private String sid;
    private Long reqId;
    private Long userId;
    private Long timestamp;
  }
}
