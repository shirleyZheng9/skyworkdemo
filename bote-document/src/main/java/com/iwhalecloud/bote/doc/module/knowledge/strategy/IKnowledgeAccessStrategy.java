package com.iwhalecloud.bote.doc.module.knowledge.strategy;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.knowledge.access.KnowledgeAccessDTO;
import com.iwhalecloud.bote.dto.knowledge.access.KnowledgeCatalogDTO;
import com.iwhalecloud.bote.dto.knowledge.access.KnowledgeDocumentDTO;
import com.iwhalecloud.bote.dto.knowledge.access.KnowledgeFileDTO;
import com.iwhalecloud.bote.dto.knowledge.access.query.KnowledgeDocumentParams;
import com.iwhalecloud.bote.dto.knowledge.access.query.KnowledgeFileQueryParams;
import com.iwhalecloud.bote.dto.knowledge.access.query.KnowledgeQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 知识库接入策略
 *
 * @author lxs
 * @since 2025/07/14
 */
public interface IKnowledgeAccessStrategy {

  /**
   * 获取接入知识库类型
   *
   * @return 知识库类型
   */
  String getKnowledgeAccessType();

  /**
   * 查询对接知识库目录
   *
   * @return 目录列表
   */
  ResultVO<List<KnowledgeCatalogDTO>> queryKnowledgeCatalog();

  /**
   * 分页查询知识库列表
   *
   * @param queryParams 查询参数
   * @return 知识库列表
   */
  ResultVO<PageInfo<KnowledgeAccessDTO>> queryKnowledgeInfoPage(KnowledgeQueryParams queryParams);

  /**
   * 分页查询知识库文档列表
   *
   * @param queryParams 搜索参数
   * @return 文档列表
   */
  ResultVO<PageInfo<KnowledgeFileDTO>> queryKnowledgeFilePage(KnowledgeFileQueryParams queryParams);

  /**
   * 获取知识库文档
   *
   * @param documentParams 文档参数
   * @return 文档信息
   */
  ResultVO<KnowledgeDocumentDTO> getKnowledgeDocument(KnowledgeDocumentParams documentParams);

  /**
   * 下载知识库文档
   *
   * @param docId 文档ID
   * @param httpServletResponse 响应
   */
  void downloadKnowledgeDoc(Long docId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
}
