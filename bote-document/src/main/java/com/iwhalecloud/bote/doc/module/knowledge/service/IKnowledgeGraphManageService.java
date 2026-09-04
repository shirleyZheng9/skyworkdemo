package com.iwhalecloud.bote.doc.module.knowledge.service;

import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeGraphTokenDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeGraphDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * knowledgeGraph 管理服务
 *
 * @author qian.sisheng
 * @since 2026-04-14
 */
public interface IKnowledgeGraphManageService {

  /**
   * 查询 knowledgeGraph 知识库列表
   *
   * @param tenantId 租户id
   * @return 知识库列表
   */
  List<KnowledgeGraphDTO> queryKnowledgeGraphList(Long tenantId);

  /**
   * 获取 knowledgeGraph Token
   *
   * @param tenantId 租户id
   * @return token
   */
  ResultVO<KnowledgeGraphTokenDTO> getKnowledgeGraphToken(Long tenantId);
}
