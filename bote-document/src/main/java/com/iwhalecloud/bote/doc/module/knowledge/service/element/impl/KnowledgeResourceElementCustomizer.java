package com.iwhalecloud.bote.doc.module.knowledge.service.element.impl;

import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocumentQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.DocumentManageMapper;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.KnowledgeBaseManageMapper;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.service.element.impl.AbstractResourceElementCustomizer;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * 配置数据实体关系记录 - 知识库
 *
 * @author chen.linfa
 * @since 2025-04-09
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.KNOWLEDGE)
public class KnowledgeResourceElementCustomizer extends AbstractResourceElementCustomizer {

  private final KnowledgeBaseManageMapper knowledgeBaseManageMapper;

  private final DocumentManageMapper documentManageMapper;


  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.KNOWLEDGE.getCode();
  }

  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long knowledgeId) {
    KnowledgeBaseDTO knowledge = knowledgeBaseManageMapper.selectSimpleKnowledgeById(tenantId, knowledgeId);
    if (knowledge == null) {
      throw new IllegalArgumentException("知识库不存在: knowledgeId=" + knowledgeId);
    }
    List<ResourceElementDTO> elements = new ArrayList<>(createCatalogElement(tenantId, knowledgeId, knowledge.getCatalogItemId()));

    // 计算文档关联的附件
    DocumentQueryParams params = new DocumentQueryParams();
    params.setTenantId(tenantId);
    params.setKnowledgeId(knowledgeId);
    List<DocumentDTO> documents = documentManageMapper.selectDocumentList(params);
    for (DocumentDTO dto : CollectionUtils.emptyIfNull(documents)) {
      elements.add(createElement(tenantId, knowledgeId, dto.getFileInfoId(), DataSyncCodeEnum.DOCUMENT_FILE.getCode()));
    }
    return elements;
  }
}
