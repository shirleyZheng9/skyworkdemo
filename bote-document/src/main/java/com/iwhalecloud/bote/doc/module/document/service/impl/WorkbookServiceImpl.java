package com.iwhalecloud.bote.doc.module.document.service.impl;

import com.iwhalecloud.bote.doc.module.document.entity.WorkbookSnapshotEntity;
import com.iwhalecloud.bote.doc.module.document.entity.WorksheetEntity;
import com.iwhalecloud.bote.doc.module.document.mapper.WorkbookSnapshotMapper;
import com.iwhalecloud.bote.doc.module.document.mapper.WorksheetMapper;
import com.iwhalecloud.bote.doc.module.document.service.IWorkbookService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 表格快照 Service 实现类
 *
 * @author Aiqing
 * @since 2025-08-15
 */
@Service
@RequiredArgsConstructor
public class WorkbookServiceImpl implements IWorkbookService {

  private final WorkbookSnapshotMapper workbookSnapshotMapper;
  private final WorksheetMapper worksheetMapper;

  @Override
  public WorkbookSnapshotEntity getSnapshotById(Long id) {
    return workbookSnapshotMapper.selectByPrimaryKey(id);
  }

  @Override
  public Long createSnapshot(WorkbookSnapshotEntity snapshot) {
    int result = workbookSnapshotMapper.insert(snapshot);

    Assert.isTrue(result > 0, "创建表格快照失败");
    return snapshot.getId();
  }

  @Override
  public Long createWorksheet(WorksheetEntity worksheet) {
    int result = worksheetMapper.insert(worksheet);
    Assert.isTrue(result > 0, "创建Sheet页失败");
    return worksheet.getId();
  }

  @Override
  public List<WorksheetEntity> selectWorkbookSheetsBySnapshotId(String documentId, Long snapshotId) {
    return worksheetMapper.selectWorkbookSheetsBySnapshotId(documentId, snapshotId);
  }

  @Override
  public WorkbookSnapshotEntity queryLastRevisionSnapshot(String documentId) {
    Long revision = workbookSnapshotMapper.queryLastRevision(documentId);
    return workbookSnapshotMapper.selectByRevisionAndDocumentId(documentId, revision);
  }

  @Override
  public WorksheetEntity queryWorksheetByDocumentIdAndBlockId(String documentId, Long blockId) {
    return worksheetMapper.queryWorksheetByDocumentIdAndBlockId(documentId, blockId);
  }
}
