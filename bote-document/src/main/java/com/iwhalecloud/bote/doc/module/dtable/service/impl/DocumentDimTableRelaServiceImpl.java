package com.iwhalecloud.bote.doc.module.dtable.service.impl;

import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.module.dtable.dto.DimTableCreateResultDTO;
import com.iwhalecloud.bote.doc.module.dtable.entity.DocumentDimTableRelaEntity;
import com.iwhalecloud.bote.doc.module.dtable.mapper.DocumentDimTableRelaMapper;
import com.iwhalecloud.bote.doc.module.dtable.service.DimTableClientService;
import com.iwhalecloud.bote.doc.module.dtable.service.IDocumentDimTableRelaService;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 文档与多维表格关联关系服务实现类
 *
 * @author auto
 * @since 2026-01-09
 */
@Service
@RequiredArgsConstructor
public class DocumentDimTableRelaServiceImpl implements IDocumentDimTableRelaService {

  private final DocumentDimTableRelaMapper documentDimTableRelaMapper;
  private final DimTableClientService dimtableClientService;

  /**
   * 创建文档关联多维表格
   * 1. 调用多维表格服务创建多维表格空间+默认的多维表格
   * 2. 保存文档关联关系
   *
   * @param documentId 文档ID
   * @param userId 当前操作用户ID
   */
  @Override
  @Transactional
  public void createDimTable(String documentId, Long userId) {
    DimTableCreateResultDTO dimTableSpace = dimtableClientService.createDimTableSpace(documentId, userId);
    create(documentId, dimTableSpace, userId);
  }

  @Override
  public DocumentDimTableRelaEntity findByDocumentId(String documentId) {
    List<DocumentDimTableRelaEntity> relaEntities = documentDimTableRelaMapper.selectByDocumentId(documentId);
    return relaEntities == null ? null : relaEntities.getFirst();
  }

  @Override
  @Transactional
  public boolean deleteByDocumentId(String documentId) {
    int result = documentDimTableRelaMapper.deleteByDocumentId(documentId);
    return result > 0;
  }

  private void create(String documentId, DimTableCreateResultDTO resultDTO, Long userId) {
    DocumentDimTableRelaEntity rela = new DocumentDimTableRelaEntity();
    // 设置默认值
    rela.setId(IDUtils.nextId());
    rela.setDocumentId(documentId);
    rela.setTableSpaceId(resultDTO.getSpaceId());
    rela.setRootNodeId(resultDTO.getRootNodeId());
    rela.setCreatorId(userId);
    rela.setUpdatorId(userId);

    rela.setCreatedTime(new Date());
    rela.setUpdatedTime(new Date());
    rela.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    int result = documentDimTableRelaMapper.insert(rela);
    Assert.isTrue(result > 0, "保存多维表格关系失败");
  }

}
