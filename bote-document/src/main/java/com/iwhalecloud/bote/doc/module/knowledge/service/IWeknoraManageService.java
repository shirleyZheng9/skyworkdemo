package com.iwhalecloud.bote.doc.module.knowledge.service;

import com.iwhalecloud.bote.doc.module.knowledge.dto.WeKnoraTokenDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp.WeKnoraKnowledgeBaseRespDTO;

import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 知识库管理服务
 *
 * @author auto
 * @since 2024-09-20
 */
public interface IWeknoraManageService {

  /**
   * 查询知识库列表
   * @param tenantId 租户id
   * @return 返回知识库列表
   */
  List<WeKnoraKnowledgeBaseRespDTO> queryKnowledgeBases(Long tenantId);

  /**
   * 获取WeKnora Token
   *
   * @param tenantId 租户id
   * @return WeKnora Token
   */
  ResultVO<WeKnoraTokenDTO> getWeKnoraToken(Long tenantId);
}
