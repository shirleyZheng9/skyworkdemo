package com.iwhalecloud.bote.doc.module.knowledge.mapper;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecordDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecorddKnowledgeDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecordItemDto;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecordStatisticsDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.SimpleBtDcQaRecordDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.SimpleKnowledgeBaseDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.BtDcQaRecordNoticeBoardQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.BtDcQaRecordQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.semantic.dto.PendingKnowledgeQuestionDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 问答记录表管理
 *
 * @author linmengfan
 * @since 2025-09-13
 */
public interface BtDcQaRecordManageMapper {

  /**
   * 根据主键获取问答记录表
   *
   * @param qaId 问答记录表主键
   * @return 问答记录表
   */
  BtDcQaRecordDTO getBtDcQaRecord(@Param("id") Long qaId);

  /**
   * 新增问答记录表
   *
   * @param btDcQaRecord 问答记录表
   * @return 结果
   */
  int insertBtDcQaRecord(@Param("dto") BtDcQaRecordDTO btDcQaRecord);

  /**
   * 批量新增问答记录表
   *
   * @param btDcQaRecords 问答记录表列表
   * @return 结果
   */
  int batchInsertBtDcQaRecord(@Param("list") List<BtDcQaRecordDTO> btDcQaRecords);

  /**
   * 修改问答记录表
   *
   * @param btDcQaRecord 问答记录表
   * @return 结果
   */
  int updateBtDcQaRecord(@Param("dto") BtDcQaRecordDTO btDcQaRecord);

  /**
   * 修改用户反馈
   *
   * @param btDcQaRecord 问答记录表
   * @return 结果
   */
  int updateUserFeedback(@Param("dto") BtDcQaRecordDTO btDcQaRecord);

  /**
   * 获取问答记录表列表（分页）
   *
   * @param queryParams 查询条件
   * @return 问答记录表分页列表
   */
  Page<BtDcQaRecordDTO> selectBtDcQaRecordPage(@Param("query") BtDcQaRecordQueryParams queryParams, RowBounds rowBounds);


  /**
   * 根据问答ID列表批量查询关联的知识库信息
   *
   * @param qaIds 问答ID列表
   * @return 知识库信息列表（包含qa_id字段用于分组）
   */
  List<BtDcQaRecorddKnowledgeDTO> selectKnowledgeByQaIds(@Param("qaIds") List<Long> qaIds, @Param("query") BtDcQaRecordQueryParams queryParams);

  List<SimpleBtDcQaRecordDTO> selectBtDcQaRecordList(@Param("query")BtDcQaRecordNoticeBoardQueryParams params);

  /**
   * 统计问答记录：问答数、命中率、点赞数、踩数
   *
   * @param params 查询参数
   * @return 统计结果
   */
  BtDcQaRecordStatisticsDTO selectBtDcQaRecordStatistics(@Param("query") BtDcQaRecordNoticeBoardQueryParams params);

  /**
   * 按天统计查询问答次数
   *
   * @param params 查询参数
   * @return 按天统计数据
   */
  List<BtDcQaRecordItemDto> selectBtDcQaRecordDailyStatistics(@Param("query") BtDcQaRecordNoticeBoardQueryParams params);

  /**
   * 按天统计问答人数
   *
   * @param params 查询参数
   * @return 按天统计数据
   */
  List<BtDcQaRecordItemDto> selectBtDcQaRecordDailyQaPeoplesStatistics(@Param("query") BtDcQaRecordNoticeBoardQueryParams params);

  /**
   * 按天统计问答命中率
   *
   * @param params 查询参数
   * @return 按天统计数据
   */
  List<BtDcQaRecordItemDto> selectBtDcQaRecordDailyHitRateStatistics(@Param("query") BtDcQaRecordNoticeBoardQueryParams params);

  /**
   * 失效知识库问答记录
   * @param qaId
   * @param tenantId
   * @return
   */
  int deleteBtDcQaRecord(@Param("qaId")Long qaId, @Param("tenantId")Long tenantId);

  List<SimpleKnowledgeBaseDTO> selectKnowledgeByQaId(@Param("qaId")Long qaId, @Param("tenantId")Long tenantId);

  /**
   * 更新评论
   * @param recordDTO
   * @return
   */
  int updateBtDcQaRecordReason(@Param("dto")BtDcQaRecordDTO recordDTO);

  /**
   * 按会话消息 ID（与 session_id 一致）回写标准问法
   *
   * @param tenantId 租户ID
   * @param sessionId 会话消息 ID
   * @param standardQuestion 标准问法
   */
  int updateStandardQuestionBySession(@Param("tenantId") Long tenantId, @Param("sessionId") String sessionId, @Param("standardQuestion") String standardQuestion);

  /**
   * 按问答主键回写标准问法
   * @param tenantId 租户ID
   * @param qaId 问答主键
   * @param standardQuestion 标准问法
   */
  int updateStandardQuestionByQaId(@Param("tenantId") Long tenantId, @Param("qaId") Long qaId, @Param("standardQuestion") String standardQuestion);

  /**
   * 待补充标准问法的问答记录
   *
   * @param limit 限制数量
   */
  List<PendingKnowledgeQuestionDTO> selectPendingForStandardQuestion(@Param("limit") int limit);
}
