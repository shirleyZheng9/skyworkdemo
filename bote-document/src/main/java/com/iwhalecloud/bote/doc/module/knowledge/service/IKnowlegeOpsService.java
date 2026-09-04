package com.iwhalecloud.bote.doc.module.knowledge.service;

import com.github.pagehelper.PageInfo;
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

/**
 * 知识库运营服务接口
 *
 * @author qian.sisheng
 * @since 2026/02/27
 */
public interface IKnowlegeOpsService {

  /**
   * 查询知识库运营总览（头部统计卡片）
   *
   * @param params 查询参数
   * @return 知识库运营总览DTO，含知识库数、文档数、用户提问数、文档贡献者数
   */
  KnowledgeOpsOverviewDTO queryKnowledgeOpsOverview(KnowledgeOpsQueryParams params);

  /**
   * 分页查询知识库引用频次排名
   *
   * @param params 查询参数
   * @return 分页知识库引用频次排名列表
   */
  PageInfo<KnowledgeReferenceDTO> queryKnowledgeReferenceRank(KnowledgeOpsQueryParams params);

  /**
   * 分页查询文档引用频次排名
   *
   * @param params 查询参数
   * @return 分页文档引用频次排名列表
   */
  PageInfo<DocumentReferenceDTO> queryDocumentReferenceRank(KnowledgeOpsQueryParams params);

  /**
   * 分页查询高频议题统计
   *
   * @param params 查询参数
   * @return 分页高频议题列表，name=问题内容，value=出现次数
   */
  PageInfo<KnowledgeOpsHotQuestionDTO> queryHotQuestionRank(KnowledgeOpsQueryParams params);

  /**
   * 查询点踩原因分布统计
   *
   * @param params 查询参数
   * @return 点踩原因分布列表，name=原因，value=次数
   */
  List<BtDcQaRecordItemDto> queryDislikeReasonStats(KnowledgeOpsQueryParams params);

  /**
   * 查询点赞/点踩比例数据
   *
   * @param params 查询参数
   * @return 点赞点踩汇总DTO，含numberOfLikesTotal（点赞数）和stepTotal（点踩数）
   */
  KnowledgeOpsFeedBackRatioDTO queryFeedbackRatio(KnowledgeOpsQueryParams params);

  /**
   * 分页查询文档贡献者排名
   *
   * @param params 查询参数
   * @return 分页文档贡献者排名列表，含uploadCount字段
   */
  PageInfo<KnowledgeOpsContributorDTO> queryDocContributorRank(KnowledgeOpsQueryParams params);

  /**
   * 查询文档贡献按天统计（折线图数据）
   *
   * @param params 查询参数
   * @return 文档贡献按天统计列表，name=日期(yyyyMMdd)，value=上传数量
   */
  List<BtDcQaRecordItemDto> queryDocContributeDailyStats(KnowledgeOpsQueryParams params);

  /**
   * 分页查询用户提问排名
   *
   * @param params 查询参数
   * @return 分页用户提问排名列表，含questionCount字段
   */
  PageInfo<KnowledgeOpsContributorDTO> queryUserQuestionRank(KnowledgeOpsQueryParams params);

  /**
   * 查询用户提问按天统计（折线图数据）
   *
   * @param params 查询参数
   * @return 用户提问按天统计列表，name=日期(yyyyMMdd)，value=提问数量
   */
  List<BtDcQaRecordItemDto> queryUserQuestionDailyStats(KnowledgeOpsQueryParams params);

  /**
   * 分页查询综合排名
   *
   * @param params 查询参数
   * @return 分页综合排名列表，含上传数、点赞数、点踩数、引用次数、综合得分
   */
  PageInfo<KnowledgeOpsComprehensiveDTO> queryComprehensiveRank(KnowledgeOpsQueryParams params);

  /**
   * 分页查询知识库问答记录
   *
   * @param params 查询参数
   * @return 分页知识库问答记录列表
   */
  PageInfo<BtDcQaRecordDTO> queryQaRecordPage(KnowledgeOpsQueryParams params);

  /**
   * 更新反馈操作状态
   *
   * @param qaId         问答记录ID
   * @param operateState 操作状态
   */
  void updateFeedbackOperateState(Long qaId, String operateState);
}
