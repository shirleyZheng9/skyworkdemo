package com.iwhalecloud.bote.service.element.impl;

import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.knowledge.CorpusInfoDTO;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.CorpusInfoManageMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 配置数据实体关系记录 - 语料
 *
 * @author chen.linfa
 * @since 2025-04-09
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.CORPUS)
public class CorpusResourceElementCustomizer extends AbstractResourceElementCustomizer {
  private final CorpusInfoManageMapper corpusInfoManageMapper;

  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.CORPUS.getCode();
  }

  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long corpusId) {
    CorpusInfoDTO corpus = corpusInfoManageMapper.getCorpusInfo(corpusId, tenantId);
    return createCatalogElement(tenantId, corpusId, corpus.getCatalogItemId());
  }
}
