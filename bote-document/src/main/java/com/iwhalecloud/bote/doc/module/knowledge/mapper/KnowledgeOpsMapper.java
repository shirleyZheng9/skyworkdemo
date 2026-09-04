package com.iwhalecloud.bote.doc.module.knowledge.mapper;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecordDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecordItemDto;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentReferenceDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeOpsFeedBackRatioDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeOpsComprehensiveDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeOpsContributorDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeOpsHotQuestionDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeOpsOverviewDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeReferenceDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.KnowledgeOpsQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 知识库运营Mapper
 *
 * @author qian.sisheng
 * @since 2026/02/27
 */
public interface KnowledgeOpsMapper {

  /**
   * 查询知识库运营概览
   *
   * @param query 查询参数
   * @return 知识库运营概览DTO
   */
  KnowledgeOpsOverviewDTO selectKnowledgeOpsOverview(@Param("query") KnowledgeOpsQueryParams query);

  /**
   * 分页查询知识库引用频次排名
   *
   * @param query 查询参数（含租户、时间范围等）
   * @param rowBounds 分页参数
   * @return 知识库引用频次排名列表
   */
  Page<KnowledgeReferenceDTO> selectKnowledgeReference(@Param("query") KnowledgeOpsQueryParams query,
    RowBounds rowBounds);

  /**
   * 分页查询文档引用频次排名
   *
   * @param query 查询参数（含租户、时间范围等）
   * @param rowBounds 分页参数
   * @return 文档引用频次排名列表
   */
  Page<DocumentReferenceDTO> selectDocumentReference(@Param("query") KnowledgeOpsQueryParams query,
    RowBounds rowBounds);

  /**
   * 分页查询高频议题统计
   *
   * @param query 查询参数（含租户、时间范围等）
   * @param rowBounds 分页参数
   * @return 高频议题统计列表，name=问题内容，value=出现次数
   */
  Page<KnowledgeOpsHotQuestionDTO> selectHotQuestionRank(@Param("query") KnowledgeOpsQueryParams query, RowBounds rowBounds);

  /**
   * 分页查询高频标准问题统计
   *
   * @param query 查询参数（含租户、时间范围等）
   * @param rowBounds 分页参数
   * @return 高频标准问题统计列表，name=问题内容，value=出现次数
   */
  Page<KnowledgeOpsHotQuestionDTO> selectHotQuestionRankByStandarQuestion(@Param("query") KnowledgeOpsQueryParams query, RowBounds rowBounds);

  /**
   * 查询点踩记录的 feedback_type 原始字符串列表（逗号分隔，如 "10,20"）
   * 拆分、计数、占比由 Service 层处理
   *
   * @param query 查询参数（含租户、时间范围等）
   * @return feedback_type 原始字符串列表
   */
  List<String> selectDislikeRawFeedbackTypes(@Param("query") KnowledgeOpsQueryParams query);

  /**
   * 查询点赞/点踩比例数据
   *
   * @param query 查询参数（含租户、时间范围等）
   * @return 点赞点踩汇总DTO，含numberOfLikesTotal和stepTotal字段
   */
  KnowledgeOpsFeedBackRatioDTO selectFeedbackRatio(@Param("query") KnowledgeOpsQueryParams query);

  /**
   * 查询文档总数
   *
   * @param tenantId 租户ID
   * @return 文档贡献者总数
   */
  Integer findDocTotal(Long tenantId);

  /**
   * 分页查询文档贡献者排名
   *
   * @param query 查询参数（含租户、时间范围等）
   * @param rowBounds 分页参数
   * @return 文档贡献者排名列表，含uploadCount字段
   */
  Page<KnowledgeOpsContributorDTO> selectDocContributorRank(@Param("query") KnowledgeOpsQueryParams query,
    RowBounds rowBounds);

  /**
   * 查询文档贡献按天统计（折线图数据）
   *
   * @param query 查询参数（含租户、时间范围等）
   * @return 文档贡献按天统计列表，name=日期(yyyyMMdd)，value=上传数量
   */
  List<BtDcQaRecordItemDto> selectDocContributeDailyStats(@Param("query") KnowledgeOpsQueryParams query);

  /**
   * 查询用户提问总数
   *
   * @param tenantId 租户ID
   * @return 用户提问总数
   */
  Integer findUserQuestionTotal(@Param("tenantId") Long tenantId);

  /**
   * 分页查询用户提问排名
   *
   * @param query 查询参数（含租户、时间范围等）
   * @param rowBounds 分页参数
   * @return 用户提问排名列表，含questionCount字段
   */
  Page<KnowledgeOpsContributorDTO> selectUserQuestionRank(@Param("query") KnowledgeOpsQueryParams query,
    RowBounds rowBounds);

  /**
   * 查询用户提问按天统计（折线图数据）
   *
   * @param query 查询参数（含租户、时间范围等）
   * @return 用户提问按天统计列表，name=日期(yyyyMMdd)，value=提问数量
   */
  List<BtDcQaRecordItemDto> selectUserQuestionDailyStats(@Param("query") KnowledgeOpsQueryParams query);

  /**
   * 分页查询综合排名
   *
   * @param query 查询参数（含租户、时间范围等）
   * @param rowBounds 分页参数
   * @return 综合排名列表，含上传数、点赞数、点踩数、引用次数、综合得分
   */
  Page<KnowledgeOpsComprehensiveDTO> selectComprehensiveRank(@Param("query") KnowledgeOpsQueryParams query,
    RowBounds rowBounds);

  /**
   * 分页查询反馈记录
   *
   * @param query 查询参数（含租户、时间范围等）
   * @param rowBounds 分页参数
   * @return 反馈记录列表
   */
  Page<BtDcQaRecordDTO> selectDcQaRecordPage(@Param("query") KnowledgeOpsQueryParams query, RowBounds rowBounds);

  /**
   * 更新反馈记录的操作状态
   *
   * @param qaId 反馈记录ID
   * @param operateState 操作状态
   * @return 影响行数
   */
  int updateFeedbackOperateState(@Param("qaId") Long qaId, @Param("operateState") String operateState);
}
