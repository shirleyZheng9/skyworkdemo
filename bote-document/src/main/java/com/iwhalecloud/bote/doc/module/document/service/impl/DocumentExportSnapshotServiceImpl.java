package com.iwhalecloud.bote.doc.module.document.service.impl;

import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.module.document.entity.DocumentExportSnapshotEntity;
import com.iwhalecloud.bote.doc.module.document.mapper.DocumentExportSnapshotMapper;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentExportSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 文档导出快照service 实现
 *
 * @author Aiqing
 * @since 2025/9/29
 */
@Service
@RequiredArgsConstructor
public class DocumentExportSnapshotServiceImpl implements IDocumentExportSnapshotService {

  private final DocumentExportSnapshotMapper documentExportSnapshotMapper;

  @Override
  public void insert(DocumentExportSnapshotEntity exportSnapshotEntity) {
    Long suerId = SessionUtil.getOptionalUserId(DocBaseConsts.DEFAULT_SYSTEM_USER_ID);
    if (exportSnapshotEntity.getCreatorId() == null) {
      exportSnapshotEntity.setCreatorId(suerId);
    }
    if (exportSnapshotEntity.getUpdatorId() != null) {
      exportSnapshotEntity.setUpdatorId(suerId);
    }
    documentExportSnapshotMapper.insert(exportSnapshotEntity);
  }

  @Override
  public DocumentExportSnapshotEntity selectByDocumentIdAndRevision(String documentId, Long revision, String extension) {
    return documentExportSnapshotMapper.selectByDocumentIdAndRevisionAndExtension(documentId, revision, extension);
  }
}
