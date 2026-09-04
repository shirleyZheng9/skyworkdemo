package com.iwhalecloud.bote.doc.module.knowledge.service.impl;

import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeGraphDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeGraphTokenDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.rsp.KnowledgeGraphResponse;
import com.iwhalecloud.bote.doc.module.knowledge.service.IKnowledgeGraphManageService;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.KnowledgeGraphClientHelper;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Service;

/**
 * knowledgeGraph 管理服务实现
 *
 * @author qian.sisheng
 * @since 2026-04-14
 */
@Service
@ConditionalOnBooleanProperty(name = "knowledge.knowledgeGraph.enabled")
@RequiredArgsConstructor
public class KnowledgeGraphManageServiceImpl implements IKnowledgeGraphManageService {

  private final KnowledgeGraphClientHelper knowledgeGraphClientHelper;

  @Override
  public List<KnowledgeGraphDTO> queryKnowledgeGraphList(Long tenantId) {
    return knowledgeGraphClientHelper.queryKnowledgeList(tenantId, -1, null).stream().map(this::toSimpleItem).collect(Collectors.toList());
  }

  /**
   * 转换knowledgeGraph列表项
   */
  @SuppressWarnings("unused")
  private KnowledgeGraphDTO toSimpleItem(KnowledgeGraphResponse item) {
    KnowledgeGraphDTO dto = new KnowledgeGraphDTO();
    dto.setKnowledgeId(item.getDatabase());
    dto.setKnowledgeName(item.getDatabase());
    return dto;
  }

  @Override
  public ResultVO<KnowledgeGraphTokenDTO> getKnowledgeGraphToken(Long tenantId) {
    KnowledgeGraphTokenDTO token = new KnowledgeGraphTokenDTO();
    token.setToken(knowledgeGraphClientHelper.getToken(tenantId));
    return ResultVO.success(token);
  }
}
