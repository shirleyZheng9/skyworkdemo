package com.iwhalecloud.bote.doc.module.document.service.impl;

import com.iwhalecloud.bote.doc.module.document.entity.WorkbookChangesetEntity;
import com.iwhalecloud.bote.doc.module.document.mapper.WorkbookChangesetMapper;
import com.iwhalecloud.bote.doc.module.document.service.IWorkbookChangesetService;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 *
 * @author Aiqing
 * @since 2025/9/2
 */
@Service
@RequiredArgsConstructor
public class WorkbookChangesetServiceImpl implements IWorkbookChangesetService {

  private final WorkbookChangesetMapper workbookChangesetMapper;


  @Override
  public List<WorkbookChangesetEntity> selectByDocumentIdAndRevision(String documentId, Long revision) {
    return workbookChangesetMapper.selectByDocumentIdAndRevision(documentId, revision);
  }

  @Override
  public void save(WorkbookChangesetEntity changesetEntity) {
    changesetEntity.setId(IDUtils.nextId());
    workbookChangesetMapper.insert(changesetEntity);
  }

  @Override
  public Long getCurrentRevision(String documentId) {
    return workbookChangesetMapper.selectMaxRevision(documentId);
  }

  @Override
  public List<WorkbookChangesetEntity> selectByDocumentIdAndRevisionRange(String documentId,
                                                                          Long expectRevision,
                                                                          Long currentRevision) {
    // todo 查询冲突的changeset
    return Collections.emptyList();
  }
}
