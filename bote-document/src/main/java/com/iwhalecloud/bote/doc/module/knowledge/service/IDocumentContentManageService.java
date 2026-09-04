package com.iwhalecloud.bote.doc.module.knowledge.service;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentContentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentParameterDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocumentContentQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author qian.sisheng
 * @since 2025-3-12
 */
public interface IDocumentContentManageService {
  /**
   * 保存文档参数
   *
   * @param document 文档
   * @return 结果
   */
  ResultVO<DocumentDTO> saveDocumentParameter(DocumentDTO document);

  /**
   * 查询文档参数列表（分页）
   *
   * @param queryParams 查询条件
   * @return 结果
   */
  PageInfo<DocumentParameterDTO> queryDocumentParameterPage(DocumentContentQueryParams queryParams);

  /**
   * 查询文档参数列表
   *
   * @param queryParams 查询条件
   * @return 结果
   */
  List<DocumentParameterDTO> queryDocumentParameterList(DocumentContentQueryParams queryParams);

  /**
   * 查询文档参数列表
   *
   * @param queryParams 查询条件
   * @return 结果
   */
  List<DocumentContentDTO> queryDocumentContentList(DocumentContentQueryParams queryParams);

  /**
   * 保存文档内容
   *
   * @param document 文档
   * @return 结果
   */
  ResultVO<Void> saveDocumentContent(DocumentDTO document);

  /**
   * 更新文档列内容
   *
   * @param document 文档
   * @return 结果
   */
  ResultVO<Void> updateDocumentContentCellValue(DocumentDTO document);

  /**
   * 删除文档内容
   *
   * @param document 文档
   * @return 结果
   */
  ResultVO<Void> deleteDocumentContent(DocumentDTO document);

  /**
   * 文档内容列表（分页）
   *
   * @param queryParams 查询条件
   * @return 文档内容分页列表
   */
  PageInfo<DocumentContentDTO> queryDocumentContentPage(DocumentContentQueryParams queryParams);

  /**
   * 导入文档内容
   *
   * @param tenantId 租户 ID
   * @param documentId 文档ID
   * @param file 文件
   * @param fileId 文件ID
   * @return 结果
   */
  ResultVO<Void> importDocumentContent(Long tenantId, Long documentId, MultipartFile file, Long fileId, String importType);

  /**
   * 导出文档内容
   *
   * @param tenantId 租户 ID
   * @param documentId 文档ID
   */
  void exportDocumentContent(Long tenantId, Long documentId, HttpServletResponse response);

  /**
   * 发布文档，用于通知更新关联的知识库文档
   *
   * @param tenantId 租户 ID
   * @param documentId 文档 ID
   */
  void publishDocument(Long tenantId, Long documentId);

  /**
   * 导出模板
   *
   * @param tenantId 租户 ID
   * @param documentId 文档 ID
   * @param response 响应
   */
  void exportTemplate(Long tenantId, Long documentId, HttpServletResponse response);

  /**
   * 收集指定的语料问答数据，用于微调评测
   *
   * @return 问答
   */
  List<Map<String, String>> collectCorpusQuestion(String corpusInfo, Long tenantId);

  /**
   * 收集指定的语料问答数据，生成 excel，用于微调评测
   *
   * @return excel 文件
   */
  Pair<File, BigDecimal> createCorpusQuestionFile(String corpusInfo, Long tenantId);

  /**
   * 根据条件查询知识文档列表
   * write by zyt 2025.5.20 11465479
   *
   * @param params 包含查询条件的参数映射，其中可能包括：
   *               - document_id：文档ID，用于指定查询的文档
   *               - tenant_id：租户ID，用于指定查询的租户范围
   * @return 返回一个包含查询结果的列表，每个结果为一个键值对映射
   */
  List<Map<String, Object>> queryDocumentListByCondition(Long documentId, Long tenantId, Map<String, Object> params);

  /**
   * 保存文档信息
   * write by zyt 2025.5.20 11465479
   *
   * @param paramsMap 包含文档信息的键值对映射，其中键是字符串类型，表示文档的各种属性或参数
   * @return 一个包含保存操作结果信息的映射，具体结构和内容取决于实现和使用场景
   */
  Map<String, Object> saveDocument(Long documentId, Long tenantId, Map<String, Object> paramsMap);
}
