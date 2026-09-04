package com.iwhalecloud.bote.doc.module.document.mapper;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.doc.module.document.dto.request.QueryDocContributorsRequest;
import com.iwhalecloud.bote.doc.module.document.entity.DocumentContributorEntity;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentContributorDTO;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 文档贡献者相关数据库操作
 *
 * @author Aiqing
 * @since 2025-09-18
 */
public interface DocumentContributorMapper {

  /**
   * 根据主键查询
   */
  DocumentContributorEntity selectById(@Param("id") Long id);

  /**
   * 根据文档ID和用户ID查询
   */
  DocumentContributorEntity selectByDocumentIdAndUserId(@Param("documentId") String documentId,
                                                        @Param("userId") Long userId);

  /**
   * 根据文档ID查询所有贡献者
   */
  List<DocumentContributorEntity> selectByDocumentId(@Param("documentId") String documentId);

  /**
   * 根据用户ID查询贡献记录
   */
  List<DocumentContributorEntity> selectByUserId(@Param("userId") Long userId);

  /**
   * 插入记录
   */
  int insert(@Param("contributor") DocumentContributorEntity contributor);

  /**
   * 更新贡献者得分
   */
  int updateContributorScore(@Param("id") Long id,
                             @Param("contributorScore") BigDecimal contributorScore,
                             @Param("updatorId") Long updatorId);

  /**
   * 批量插入记录
   *
   * @param contributors 贡献者实体列表
   * @return 插入数量
   */
  int batchInsert(@Param("contributors") List<DocumentContributorEntity> contributors);

  /**
   * 批量查询文档ID和用户ID对应的贡献者记录
   *
   * @param documentIds 文档ID列表
   * @param userId 用户ID
   * @return 贡献者记录列表
   */
  List<DocumentContributorEntity> selectByDocumentIdsAndUserId(@Param("documentIds") List<String> documentIds, @Param("userId") Long userId);

  /**
   * 批量更新贡献者得分
   *
   * @param updates 更新列表，每个元素包含 id, contributorScore, updatorId
   */
  void batchUpdateContributorScore(@Param("updates") List<java.util.Map<String, Object>> updates);

  /**
   * 统计文档贡献者数量
   */
  int countByDocumentId(@Param("documentId") String documentId);

  /**
   * 查询文档贡献者得分排名
   */
  List<DocumentContributorEntity> selectTopContributorsByDocumentId(@Param("documentId") String documentId,
                                                                    @Param("limit") Integer limit);

  /**
   * 查询用户贡献得分排名
   */
  List<DocumentContributorEntity> selectTopContributorsByUserId(@Param("userId") Long userId,
                                                                @Param("limit") Integer limit);
  /**
   * 根据文档ID和用户ID删除记录
   */
  int deleteByDocumentIdAndUserId(@Param("documentId") String documentId, @Param("userId") Long userId, @Param("updatorId") Long updatorId);

  /**
   * 分页查询文档贡献者
   */
  Page<DocumentContributorDTO> selectDocumentContributorsPage(@Param("query") QueryDocContributorsRequest query,
    RowBounds rowBounds);
}
