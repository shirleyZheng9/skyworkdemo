package com.iwhalecloud.bote.doc.module.document.service.impl;

import com.iwhalecloud.bote.doc.module.document.dto.DocHistCleanupStatsDTO;
import com.iwhalecloud.bote.doc.module.document.entity.DocumentExportSnapshotEntity;
import com.iwhalecloud.bote.doc.module.document.mapper.DocContentHistoryMapper;
import com.iwhalecloud.bote.doc.module.document.mapper.DocumentActivityMapper;
import com.iwhalecloud.bote.doc.module.document.mapper.DocumentExportSnapshotMapper;
import com.iwhalecloud.bote.doc.module.document.mapper.WorkbookContentHistoryMapper;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentHistoryCleanupService;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 文档历史记录清理服务实现
 *
 * @author lizuyin
 * @since 2025-10-16
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class DocumentHistoryCleanupServiceImpl implements IDocumentHistoryCleanupService {

  private static final Logger logger = LoggerFactory.getLogger(DocumentHistoryCleanupServiceImpl.class);

  private final DocumentActivityMapper documentActivityMapper;

  private final DocContentHistoryMapper docContentHistoryMapper;

  private final WorkbookContentHistoryMapper workbookContentHistoryMapper;

  private final DocumentExportSnapshotMapper documentExportSnapshotMapper;

  private final IFileStoreService fileStoreService;

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void cleanupDocumentHistory() {
    logger.debug("开始清理文档历史记录任务");

    try {
      // 1. 获取需要清理的文档ID
      List<String> updatedDocumentIds = getUpdatedDocumentIds();
      if (CollectionUtils.isEmpty(updatedDocumentIds)) {
        logger.debug("过去一天内没有文档更新，跳过清理任务");
        return;
      }

      logger.debug("找到 {} 个有更新的文档，开始清理历史记录", updatedDocumentIds.size());

      // 2. 执行清理操作
      DocHistCleanupStatsDTO totalStats = processDocumentCleanup(updatedDocumentIds);

      // 3. 记录清理结果
      logCleanupResult(totalStats);

    }
    catch (Exception e) {
      logger.error("文档历史记录清理任务执行失败", e);
      throw e;
    }
  }

  /**
   * 获取过去一天内有更新的文档ID列表
   *
   * @return 文档ID列表
   */
  private List<String> getUpdatedDocumentIds() {
    LocalDateTime endTime = LocalDateTime.now();
    LocalDateTime startTime = endTime.minusDays(1);
    return documentActivityMapper.selectUpdatedDocumentIdsByTimeRange(startTime, endTime);
  }

  /**
   * 处理文档清理流程
   *
   * @param documentIds 需要清理的文档ID列表
   * @return 清理统计信息
   */
  private DocHistCleanupStatsDTO processDocumentCleanup(List<String> documentIds) {
    DocHistCleanupStatsDTO totalStats = new DocHistCleanupStatsDTO();

    for (String documentId : documentIds) {
      try {
        DocHistCleanupStatsDTO documentStats = cleanupSingleDocumentHistory(documentId);
        totalStats.add(documentStats);
      }
      catch (Exception e) {
        logger.error("清理文档 {} 历史记录时发生错误: {}", documentId, e.getMessage(), e);
        // 继续处理其他文档，不因为单个文档失败而终止整个任务
      }
    }

    return totalStats;
  }

  /**
   * 清理单个文档的所有历史记录
   *
   * @param documentId 文档ID
   * @return 清理统计信息
   */
  @Transactional(rollbackFor = Exception.class)
  public DocHistCleanupStatsDTO cleanupSingleDocumentHistory(String documentId) {
    DocHistCleanupStatsDTO stats = new DocHistCleanupStatsDTO();

    // 清理文档内容历史记录，收集被删除的版本号
    List<Long> docContentDeletedRevisions = cleanupDocContentHistory(documentId);
    stats.setDocContentDeleted(docContentDeletedRevisions.size());

    // 清理工作簿内容历史记录，收集被删除的版本号
    List<Long> workbookContentDeletedRevisions = cleanupWorkbookContentHistory(documentId);
    stats.setWorkbookContentDeleted(workbookContentDeletedRevisions.size());

    // 合并去重被删除的版本号列表
    Set<Long> allDeletedRevisions = new HashSet<>();
    allDeletedRevisions.addAll(docContentDeletedRevisions);
    allDeletedRevisions.addAll(workbookContentDeletedRevisions);

    // 清理文档导出快照（包括文件），传入被删除的版本号列表
    DocHistCleanupStatsDTO snapshotStats = cleanupExportSnapshots(documentId, new ArrayList<>(allDeletedRevisions));
    stats.setExportSnapshotsDeleted(snapshotStats.getExportSnapshotsDeleted());
    stats.setFilesDeleted(snapshotStats.getFilesDeleted());

    return stats;
  }

  /**
   * 清理文档内容历史记录
   * 确保历史版本数量不超过15个
   *
   * @param documentId 文档ID
   * @return 被删除的版本号列表
   */
  private List<Long> cleanupDocContentHistory(String documentId) {
    // 查询历史版本总数
    Long totalCount = docContentHistoryMapper.countByDocumentId(documentId);
    if (totalCount == null || totalCount <= 15) {
      logger.debug("文档 {} 的历史版本数量为 {}，不超过15个，无需清理", documentId, totalCount);
      return new ArrayList<>();
    }

    // 查询第15个版本号（按创建时间倒序）
    Long minRevision = docContentHistoryMapper.selectMinRevisionByDocumentId(documentId);
    if (minRevision == null) {
      return new ArrayList<>();
    }

    // 查询所有小于该版本号的记录，收集版本号列表
    List<Long> deletedRevisions = docContentHistoryMapper.selectRevisionsByDocumentIdAndRevisionLessThan(documentId, minRevision);

    // 删除这些记录
    int deletedCount = docContentHistoryMapper.deleteByDocumentIdAndRevisionLessThan(documentId, minRevision);
    logger.debug("文档 {} 清理了 {} 条文档内容历史记录，被删除的版本号: {}", documentId, deletedCount, deletedRevisions);
    return deletedRevisions;
  }

  /**
   * 清理工作簿内容历史记录
   * 确保历史版本数量不超过15个
   *
   * @param documentId 文档ID
   * @return 被删除的版本号列表
   */
  private List<Long> cleanupWorkbookContentHistory(String documentId) {
    // 查询历史版本总数
    Long totalCount = workbookContentHistoryMapper.countByDocumentId(documentId);
    if (totalCount == null || totalCount <= 15) {
      logger.debug("文档 {} 的工作簿历史版本数量为 {}，不超过15个，无需清理", documentId, totalCount);
      return new ArrayList<>();
    }

    // 查询第15个版本号（按创建时间倒序）
    Long minRevision = workbookContentHistoryMapper.selectMinRevisionByDocumentId(documentId);
    if (minRevision == null) {
      return new ArrayList<>();
    }

    // 查询所有小于该版本号的记录，收集版本号列表
    List<Long> deletedRevisions = workbookContentHistoryMapper.selectRevisionsByDocumentIdAndRevisionLessThan(documentId, minRevision);

    // 删除这些记录
    int deletedCount = workbookContentHistoryMapper.deleteByDocumentIdAndRevisionLessThan(documentId, minRevision);
    logger.debug("文档 {} 清理了 {} 条工作簿内容历史记录，被删除的版本号: {}", documentId, deletedCount, deletedRevisions);
    return deletedRevisions;
  }

  /**
   * 清理文档导出快照（包括删除对应的文件）
   * 如果删除的历史版本中存在对应的文件快照，则一并删除
   *
   * @param documentId 文档ID
   * @param deletedRevisions 被删除的历史版本号列表
   * @return 清理统计信息（包含快照记录数和文件数）
   */
  private DocHistCleanupStatsDTO cleanupExportSnapshots(String documentId, List<Long> deletedRevisions) {
    DocHistCleanupStatsDTO stats = new DocHistCleanupStatsDTO();
    int totalDeletedSnapshots = 0;
    int totalDeletedFiles = 0;

    // 如果存在被删除的历史版本，删除对应的快照
    if (CollectionUtils.isNotEmpty(deletedRevisions)) {
      // 查询这些版本号对应的快照记录
      List<DocumentExportSnapshotEntity> snapshotsToDelete = documentExportSnapshotMapper.selectByDocumentIdAndRevisions(
        documentId, deletedRevisions);

      // 删除对应的文件
      int deletedFiles = deleteSnapshotFiles(snapshotsToDelete, documentId);
      totalDeletedFiles += deletedFiles;

      // 删除快照记录
      int deletedSnapshots = documentExportSnapshotMapper.deleteByDocumentIdAndRevisions(documentId, deletedRevisions);
      totalDeletedSnapshots += deletedSnapshots;
      logger.debug("文档 {} 清理了 {} 条与历史版本对应的导出快照记录，删除文件 {} 个", documentId, deletedSnapshots, deletedFiles);
    }

    // 同时保留原有的基于 minRevision 的清理逻辑（用于独立清理场景）
    Long minRevision = documentExportSnapshotMapper.selectMinRevisionByDocumentId(documentId);
    if (minRevision != null) {
      // 查询需要删除的快照记录
      List<DocumentExportSnapshotEntity> snapshotsToDelete = documentExportSnapshotMapper.selectByDocumentIdAndRevisionLessThan(
        documentId, minRevision);

      // 删除对应的文件
      int deletedFiles = deleteSnapshotFiles(snapshotsToDelete, documentId);
      totalDeletedFiles += deletedFiles;

      // 删除快照记录
      int deletedSnapshots = documentExportSnapshotMapper.deleteByDocumentIdAndRevisionLessThan(documentId,
        minRevision);
      totalDeletedSnapshots += deletedSnapshots;
      logger.debug("文档 {} 清理了 {} 条导出快照记录（基于版本号阈值），删除文件 {} 个", documentId, deletedSnapshots, deletedFiles);
    }

    stats.setExportSnapshotsDeleted(totalDeletedSnapshots);
    stats.setFilesDeleted(totalDeletedFiles);
    logger.debug("文档 {} 总共清理了 {} 条导出快照记录，删除文件 {} 个", documentId, totalDeletedSnapshots, totalDeletedFiles);

    return stats;
  }

  /**
   * 删除快照文件
   *
   * @param snapshots 快照记录列表
   * @param documentId 文档ID（用于日志）
   * @return 删除的文件数
   */
  private int deleteSnapshotFiles(List<DocumentExportSnapshotEntity> snapshots, String documentId) {
    int deletedFiles = 0;

    for (DocumentExportSnapshotEntity snapshot : snapshots) {
      if (snapshot.getFileId() != null) {
        try {
          fileStoreService.deleteFile(snapshot.getFileId());
          deletedFiles++;
          logger.debug("删除了快照文件: fileId={}, documentId={}", snapshot.getFileId(), documentId);
        }
        catch (Exception e) {
          logger.warn("删除快照文件失败: fileId={}, documentId={}, error={}", snapshot.getFileId(), documentId,
            e.getMessage());
        }
      }
    }

    return deletedFiles;
  }

  /**
   * 记录清理结果
   *
   * @param stats 清理统计信息
   */
  private void logCleanupResult(DocHistCleanupStatsDTO stats) {
    logger.debug("文档历史记录清理任务完成。统计: 文档内容历史记录删除 {} 条, "
        + "工作簿内容历史记录删除 {} 条, 导出快照记录删除 {} 条, 文件删除 {} 个", stats.getDocContentDeleted(),
      stats.getWorkbookContentDeleted(), stats.getExportSnapshotsDeleted(), stats.getFilesDeleted());
  }
}
