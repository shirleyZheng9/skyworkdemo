package com.iwhalecloud.bote.doc.module.knowledge.service;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaDocumentChunkRefernceDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecordDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecordNoticeBoardDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.UpdateBtDcQaRecordDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.BtDcQaRecordNoticeBoardQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.BtDcQaRecordQueryParams;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallResponse;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 问答记录表管理服务
 *
 * @author linmengfan
 * @since 2025-09-13
 */
public interface IBtDcQaRecordManageService {

  /**
   * 查询单个问答记录表表
   *
   * @param qaId 知识库权限表主键
   * @return 问答记录表
   */
  BtDcQaRecordDTO findBtDcQaRecord(Long qaId);

  /**
   * 查询知问答记录表列表（分页）
   *
   * @param queryParams 查询条件
   * @return 问答记录表分页列表
   */
  PageInfo<BtDcQaRecordDTO> queryBtDcQaRecordPage(BtDcQaRecordQueryParams queryParams);

  /**
   * 查询问答引用
   * @param qaId
   * @return
   */
  List<BtDcQaDocumentChunkRefernceDTO> queryBtDcQaDocumentChunkRefernceDTOList(Long qaId, Long tenantId);

  /**
   * 查询知识问答的看板数据
   * @param params
   * @return
   */
  BtDcQaRecordNoticeBoardDTO findNoticeBoardBtDcQaRecord(BtDcQaRecordNoticeBoardQueryParams params);

  /**
   * 更新问答记录的喜好或者原因
   * @param request
   * @return
   */
  ResultVO<Void> updateBtDcQaRecord(UpdateBtDcQaRecordDTO request);

  /**
   * 通过问答id删除问答记录
   * @param qaId
   * @param tenantId
   */
  void deleteBtDcQaRecordInfo(Long qaId, Long tenantId);


  /**
   * 删除知识库文档的时候需要把问答记录也失效了，需要判断有什么文档删除了
   * @param knowledgeId
   * @param tenantId
   */
  void deleteBtDcQaRecordInfoByknowledgeId(Long knowledgeId, Long extSystemId, Long tenantId);

  /**
   * 知识问答飞轮日志记录表
   * @param params
   * @param references
   * @param text
   * @param chatLogId
   * @param timeSpent
   */
  void saveAuestionsAndAnswerRecord(KnowledgeChatParamsDTO params, List<ReferenceDocumentDTO> references, String text, String chatLogId, Long timeSpent);

  void saveAuestionsAndReCallRecord(KnowledgeRecallResponse finalResponse, Long tenantId, Long timeSpent, List<Long> knowledgeIds, String question, Long userId);
}
