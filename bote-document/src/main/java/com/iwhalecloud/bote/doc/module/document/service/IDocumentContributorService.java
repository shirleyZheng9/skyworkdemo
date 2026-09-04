package com.iwhalecloud.bote.doc.module.document.service;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.doc.module.document.dto.request.QueryDocContributorsRequest;
import com.iwhalecloud.bote.doc.module.document.entity.DocumentContributorEntity;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentContributeRequestDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentContributorDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.math.BigDecimal;
import java.util.List;

/**
 * 文档贡献者服务接口
 *
 * @author Aiqing
 * @since 2025-09-18
 */
public interface IDocumentContributorService {

  /**
   * 根据文档ID查询所有贡献者
   *
   * @param documentId 文档ID
   * @return 贡献者列表
   */
  List<DocumentContributorEntity> findByDocumentId(String documentId);

  /**
   * 根据用户ID查询贡献记录
   *
   * @param userId 用户ID
   * @return 贡献记录列表
   */
  List<DocumentContributorEntity> findByUserId(Long userId);

  /**
   * 创建贡献者记录
   *
   * @param contributor 贡献者记录
   * @return 是否成功
   */
  boolean create(DocumentContributorEntity contributor);

  /**
   * 更新贡献值
   *
   * @param id 记录ID
   * @param contributorScore 贡献值
   * @param updatorId 更新人
   * @return 是否成功
   */
  boolean updateContributorScore(Long id, BigDecimal contributorScore, Long updatorId);

  /**
   * 统计文档贡献者数量
   *
   * @param documentId 文档ID
   * @return 贡献者数量
   */
  int countByDocumentId(String documentId);

  /**
   * 查询文档贡献者得分排名
   *
   * @param documentId 文档ID
   * @param limit 限制数量
   * @return 贡献者排名列表
   */
  List<DocumentContributorEntity> getTopContributorsByDocumentId(String documentId, Integer limit);

  /**
   * 查询用户贡献得分排名
   *
   * @param userId 用户ID
   * @param limit 限制数量
   * @return 贡献记录排名列表
   */
  List<DocumentContributorEntity> getTopContributorsByUserId(Long userId, Integer limit);

  /**
   * 添加或更新贡献者记录
   * 如果记录存在则更新得分，否则创建新记录
   *
   * @param documentId 文档ID
   * @param userId 用户ID
   * @param contributorScore 贡献得分
   * @param operatorId 操作人ID
   * @return 是否成功
   */
  boolean addOrUpdateContributor(String documentId,
                                 Long userId,
                                 BigDecimal contributorScore,
                                 Long operatorId);

  /**
   * 批量添加或更新贡献者记录
   *
   * @param documentIds 文档ID列表
   * @param userId 用户ID
   * @param contributorScore 贡献得分
   * @param operatorId 操作人ID
   */
  void batchAddOrUpdateContributors(List<String> documentIds, Long userId, BigDecimal contributorScore, Long operatorId);

  /**
   * 批量添加贡献者记录
   *
   * @param request 贡献者记录
   * @return 是否成功
   */
  ResultVO<Void> batchAddContributors(DocumentContributeRequestDTO request);

  /**
   * 根据文档ID和用户ID删除贡献者记录
   *
   * @param documentId 文档ID
   * @param userId 用户ID
   */
  ResultVO<Void> deleteByDocumentId(String documentId, Long userId);

  /**
   * 根据文档ID和用户ID删除贡献者记录
   *
   * @param request 查询条件
   * @return 删除结果
   */
  PageInfo<DocumentContributorDTO> queryDocumentContributorPage(QueryDocContributorsRequest request);
}
