package com.iwhalecloud.bote.doc.module.knowledge.cache;

import com.iwhalecloud.bote.cache.AbstractSkillCache;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocChainConfigHelper;
import com.iwhalecloud.bote.dto.knowledge.SimpleDocumentDTO;
import com.iwhalecloud.bote.dto.knowledge.SimpleKnowledgeDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.response.QueryDocmentResponse;
import com.iwhalecloud.bote.dto.knowledge.docchain.response.QueryDocmentResponse.DocmentInfo;
import com.iwhalecloud.bote.mapper.knowledge.KnowledgeQueryMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 知识库缓存
 *
 * @author bianjp
 * @since 2025-04-24
 */
@Component
public final class KnowledgeCache extends AbstractSkillCache<SimpleKnowledgeDTO> {

  private final KnowledgeQueryMapper knowledgeQueryMapper;
  private final DocChainConfigHelper docChainConfigHelper;

  public KnowledgeCache(KnowledgeQueryMapper knowledgeQueryMapper, DocChainConfigHelper docChainConfigHelper) {
    super(CacheConsts.KEY_PREFIX_KNOWLEDGE);
    this.knowledgeQueryMapper = knowledgeQueryMapper;
    this.docChainConfigHelper = docChainConfigHelper;
    disableDistributionCache();
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_KNOWLEDGE;
  }

  @Override
  protected SimpleKnowledgeDTO loadById(Long tenantId, Long id) {
    SimpleKnowledgeDTO knowledge = knowledgeQueryMapper.selectSimpleKnowledgeById(tenantId, id);
    Assert.notNull(knowledge, "Knowledge not found");
    knowledge.parseExtConfig();
    setDocuments(knowledge, tenantId, knowledgeQueryMapper.selectSimpleDocumentById(tenantId, knowledge.getKnowledgeId()));
    return knowledge;
  }

  @Override
  protected Map<Long, SimpleKnowledgeDTO> loadByIds(Long tenantId, List<Long> ids) {
    List<SimpleKnowledgeDTO> knowledges = knowledgeQueryMapper.selectSimpleKnowledgeByIds(tenantId, ids);
    if (CollectionUtils.isEmpty(knowledges)) {
      return Collections.emptyMap();
    }
    Map<Long, List<SimpleDocumentDTO>> group = CollectionUtils.emptyIfNull(knowledgeQueryMapper.selectSimpleDocumentByIds(tenantId, ids)).stream()
      .collect(Collectors.groupingBy(SimpleDocumentDTO::getKnowledgeId));
    for (SimpleKnowledgeDTO knowledge : knowledges) {
      knowledge.parseExtConfig();
      setDocuments(knowledge, tenantId, group.get(knowledge.getKnowledgeId()));
    }
    return knowledges.stream().collect(Collectors.toMap(SimpleKnowledgeDTO::getKnowledgeId, Function.identity()));
  }

  private void setDocuments(SimpleKnowledgeDTO knowledge, Long tenantId, List<SimpleDocumentDTO> documents) {
    if (knowledge.isRelated()) {
      // 关联类型的知识库，查询 docchain API 收集文档数据，方便知识问答、检索时的权限过滤
      QueryDocmentResponse.DocmentPageInfo docmentInfos = docChainConfigHelper.queryDocumentPage(tenantId, Long.valueOf(knowledge.getTopicId()), null,
        1, 99);
      if (docmentInfos != null) {
        List<SimpleDocumentDTO> list = new ArrayList<>();
        for (DocmentInfo info : CollectionUtils.emptyIfNull(docmentInfos.getList())) {
          SimpleDocumentDTO dto = new SimpleDocumentDTO();
          dto.setDocumentId(info.getId());
          dto.setExtSystemId(info.getId());
          list.add(dto);
        }
        knowledge.setDocuments(list);
      }
    }
    else {
      knowledge.setDocuments(documents);
    }
  }
}
