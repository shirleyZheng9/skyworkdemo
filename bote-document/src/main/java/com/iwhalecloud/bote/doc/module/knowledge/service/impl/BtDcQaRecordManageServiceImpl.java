package com.iwhalecloud.bote.doc.module.knowledge.service.impl;

import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.space.SpaceContextHolder;
import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.LibraryRoleEnum;
import com.iwhalecloud.bote.doc.enums.DocSequences;
import com.iwhalecloud.bote.doc.integration.PortalOrgIntegration;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecordKbRelDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DoKbIDAndDocIdDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.ReferenceChunkResponse;
import com.iwhalecloud.bote.doc.module.knowledge.dto.ReferenceChunkResponseData;
import com.iwhalecloud.bote.doc.module.knowledge.dto.SimpleKnowledgeBaseDTO;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.BtDcQaRecordKbRelManageMapper;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.DocumentManageMapper;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.BtDcKbPermissionHelper;
import com.iwhalecloud.bote.doc.module.knowledge.service.IDocChainApiService;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocChainDocumentHelper;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallImageItem;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallTextItem;
import com.iwhalecloud.bote.dto.knowledge.ReferenceChunkDTO;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPathDTO;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaChunkReferenceDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaDocumentChunkRefernceDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecordDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecordItemDto;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecordNoticeBoardDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecordStatisticsDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecorddKnowledgeDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.UpdateBtDcQaRecordDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.BtDcQaRecordNoticeBoardQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.BtDcQaRecordQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.BtDcQaChunkReferenceManageMapper;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.BtDcQaRecordManageMapper;
import com.iwhalecloud.bote.doc.module.knowledge.semantic.IBtDcQaKnowledgeVectorService;
import com.iwhalecloud.bote.doc.module.knowledge.service.IBtDcQaRecordManageService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

import lombok.RequiredArgsConstructor;

/**
 * 问答记录表管理服务实现
 *
 * @author linmengfan
 * @since 2025-09-13
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class BtDcQaRecordManageServiceImpl implements IBtDcQaRecordManageService {
  private static final Logger logger = LoggerFactory.getLogger(BtDcQaRecordManageServiceImpl.class);

  private final BtDcQaRecordManageMapper btDcQaRecordManageMapper;
  private final BtDcQaChunkReferenceManageMapper btDcQaChunkReferenceManageMapper;
  private final BtDcQaRecordKbRelManageMapper btDcQaRecordKbRelManageMapper;
  private final DocumentManageMapper documentManageMapper;
  private final IDcUserService dcUserService;
  private final IDocumentService documentService;
  private final BtDcKbPermissionHelper btDcKbPermissionHelper;
  private final IDocChainApiService docChainApiService;
  private final PortalOrgIntegration portalOrgIntegration;
  private final DocChainDocumentHelper docChainDocumentHelper;
  private final IBtDcQaKnowledgeVectorService btDcQaKnowledgeVectorCore;

  @Override
  public BtDcQaRecordDTO findBtDcQaRecord(Long qaId) {
    return btDcQaRecordManageMapper.getBtDcQaRecord(qaId);
  }

  @Override
  public PageInfo<BtDcQaRecordDTO> queryBtDcQaRecordPage(BtDcQaRecordQueryParams queryParams) {
    queryParams.setUserId(SessionUtil.getLoginInfo().getUserId());
    queryParams.setSuperAdmin(SessionUtil.isSuperAdmin(queryParams.getUserId()));
    // 在这里设置登陆人和部门
    Long spaceId = SpaceContextHolder.getRequiredSpaceId();
    List<OrgDTO> orgList = portalOrgIntegration.queryUserOrgList(spaceId, queryParams.getUserId());
    if (CollectionUtils.isNotEmpty(orgList)) {
      queryParams.setDeptIds(orgList.stream().map(OrgDTO::getOrgId).collect(Collectors.toList()));
    }
    if (DocBaseConsts.AI_PORTAL.equals(queryParams.getPlatform())) {
      queryParams.setTenantId(null);
    }
    RowBounds rowBounds = queryParams.buildRowBounds();
    PageInfo<BtDcQaRecordDTO> pageInfo = btDcQaRecordManageMapper.selectBtDcQaRecordPage(queryParams, rowBounds).toPageInfo();

    // 批量查询并设置关联信息
    if (pageInfo != null && !CollectionUtils.isEmpty(pageInfo.getList())) {
      enrichQaRecordsWithRelatedData(pageInfo.getList(), queryParams);
    }

    return pageInfo;
  }

  @Override
  public List<BtDcQaDocumentChunkRefernceDTO> queryBtDcQaDocumentChunkRefernceDTOList(Long qaId, Long tenantId) {
    List<BtDcQaChunkReferenceDTO> btDcQaChunkReferenceDTOS = btDcQaChunkReferenceManageMapper.selectBtDcQaChunkReferenceList(qaId, tenantId);
    if (CollectionUtils.isEmpty(btDcQaChunkReferenceDTOS)) {
      return Collections.emptyList();
    }

    // 先按照kbId（知识库ID）进行分组
    Map<Long, List<BtDcQaChunkReferenceDTO>> groupedByKbId = btDcQaChunkReferenceDTOS.stream().collect(Collectors.groupingBy(BtDcQaChunkReferenceDTO::getKbId));

    // 对每个知识库内的数据，再按照docId进行分组，并创建BtDcQaDocumentChunkRefernceDTO对象
    return groupedByKbId.values().stream()
      .flatMap(kbChunks -> {
        // 在每个知识库内按docId分组
        Map<Long, List<BtDcQaChunkReferenceDTO>> groupedByDocId = kbChunks.stream().collect(Collectors.groupingBy(BtDcQaChunkReferenceDTO::getDocId));
        // 为每个docId组创建一个BtDcQaDocumentChunkRefernceDTO对象
        return groupedByDocId.entrySet().stream().map(entry -> createDocumentChunkReferenceDTO(entry.getKey(), entry.getValue(), tenantId));
      }).collect(Collectors.toList());
  }

  @Override
  public BtDcQaRecordNoticeBoardDTO findNoticeBoardBtDcQaRecord(BtDcQaRecordNoticeBoardQueryParams params) {
    // 校验并初始化参数
    validateAndInitializeParams(params);

    // 初始化结果对象
    BtDcQaRecordNoticeBoardDTO result = initializeNoticeBoardDTO();

    // 1. 使用SQL统计指定时间范围的数据
    BtDcQaRecordStatisticsDTO statistics = queryStatisticsByTimeType(params);

    // 2. 填充统计数据
    fillStatisticsData(result, statistics);

    // 3. 根据cartType决定使用不同维度的按天统计
    List<BtDcQaRecordItemDto> timeStats = queryStatisticsByCartType(params);
    result.setQaList(timeStats);

    return result;
  }

  /**
   * 校验并初始化查询参数
   */
  private void validateAndInitializeParams(BtDcQaRecordNoticeBoardQueryParams params) {
    // 校验timeType参数
    validateTimeType(params.getTimeType());

    // 校验cartType参数
    validateAndSetCartType(params);

    // 处理租户ID
    if (DocBaseConsts.AI_PORTAL.equals(params.getPlatform())) {
      params.setTenantId(null);
    }

    // 设置用户信息
    params.setUserId(SessionUtil.getLoginInfo().getUserId());
    params.setSuperAdmin(SessionUtil.isSuperAdmin(params.getUserId()));

    // 设置部门信息
    setDepartmentIds(params);
  }

  /**
   * 设置部门ID列表
   */
  private void setDepartmentIds(BtDcQaRecordNoticeBoardQueryParams params) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    Long spaceId = SpaceContextHolder.getRequiredSpaceId();
    List<OrgDTO> orgList = portalOrgIntegration.queryUserOrgList(spaceId, userId);
    if (CollectionUtils.isNotEmpty(orgList)) {
      params.setDeptIds(orgList.stream().map(OrgDTO::getOrgId).collect(Collectors.toList()));
    }
  }

  /**
   * 初始化通知板DTO
   */
  private BtDcQaRecordNoticeBoardDTO initializeNoticeBoardDTO() {
    BtDcQaRecordNoticeBoardDTO result = new BtDcQaRecordNoticeBoardDTO();
    result.setFeedbackData(new ArrayList<>());
    result.setDissatisfactionData(new ArrayList<>());
    return result;
  }

  /**
   * 填充统计数据
   */
  private void fillStatisticsData(BtDcQaRecordNoticeBoardDTO result, BtDcQaRecordStatisticsDTO statistics) {
    if (statistics != null) {
      fillStatisticsWithData(result, statistics);
    } else {
      fillStatisticsWithDefaultValues(result);
    }
  }

  /**
   * 使用统计数据填充结果
   */
  private void fillStatisticsWithData(BtDcQaRecordNoticeBoardDTO result, BtDcQaRecordStatisticsDTO statistics) {
    // 设置基本统计信息
    result.setQuestionsAndAnswersTotal(statistics.getQuestionsAndAnswersTotal());
    result.setHitRate(statistics.getHitRate());
    result.setQuestioners(statistics.getQuestionAnswerUserTotal());

    // 设置反馈数据（点赞/点踩按百分比）
    Long likesTotal = statistics.getNumberOfLikesTotal() != null ? statistics.getNumberOfLikesTotal() : 0L;
    Long stepsTotal = statistics.getStepTotal() != null ? statistics.getStepTotal() : 0L;
    Long feedbackTotal = likesTotal + stepsTotal;

    String dislikePercent = "0";
    String likePercent = "0";
    if (feedbackTotal != 0L) {
      BigDecimal total = new BigDecimal(feedbackTotal);
      dislikePercent = new BigDecimal(stepsTotal)
        .divide(total, 4, RoundingMode.HALF_UP)
        .multiply(new BigDecimal(100))
        .setScale(2, RoundingMode.HALF_UP)
        .toPlainString();
      likePercent = new BigDecimal(likesTotal)
        .divide(total, 4, RoundingMode.HALF_UP)
        .multiply(new BigDecimal(100))
        .setScale(2, RoundingMode.HALF_UP)
        .toPlainString();
    }

    result.getFeedbackData().add(createItemDto("点踩", dislikePercent));
    result.getFeedbackData().add(createItemDto("点赞", likePercent));

    // 设置不满意数据（按百分比）
    Long feedbackReasonCount = statistics.getFeedbackReasonCount() != null ? statistics.getFeedbackReasonCount() : 0L;
    Long unsatisfiedCount = Math.max(0L, stepsTotal - feedbackReasonCount);

    String unsatisfiedPercent = "0";
    String reasonPercent = "0";
    if (stepsTotal != 0L) {
      BigDecimal dislikeTotal = new BigDecimal(stepsTotal);
      unsatisfiedPercent = new BigDecimal(unsatisfiedCount)
        .divide(dislikeTotal, 4, RoundingMode.HALF_UP)
        .multiply(new BigDecimal(100))
        .setScale(2, RoundingMode.HALF_UP)
        .toPlainString();
      reasonPercent = new BigDecimal(feedbackReasonCount)
        .divide(dislikeTotal, 4, RoundingMode.HALF_UP)
        .multiply(new BigDecimal(100))
        .setScale(2, RoundingMode.HALF_UP)
        .toPlainString();
    }

    result.getDissatisfactionData().add(createItemDto("不满", unsatisfiedPercent));
    result.getDissatisfactionData().add(createItemDto("不满原因", reasonPercent));
  }

  /**
   * 使用默认值填充结果
   */
  private void fillStatisticsWithDefaultValues(BtDcQaRecordNoticeBoardDTO result) {
    // 设置基本统计信息默认值
    result.setQuestionsAndAnswersTotal(0L);
    result.setHitRate(BigDecimal.ZERO);
    result.setQuestioners(0L);

    // 设置反馈数据默认值
    result.getFeedbackData().add(createItemDto("点踩", "0"));
    result.getFeedbackData().add(createItemDto("点赞", "0"));

    // 设置不满意数据默认值
    result.getDissatisfactionData().add(createItemDto("不满", "0"));
    result.getDissatisfactionData().add(createItemDto("不满原因", "0"));
  }

  /**
   * 创建数据项DTO
   */
  private BtDcQaRecordItemDto createItemDto(String name, String value) {
    BtDcQaRecordItemDto item = new BtDcQaRecordItemDto();
    item.setName(name);
    item.setValue(value);
    return item;
  }

  @Override
  public ResultVO<Void> updateBtDcQaRecord(UpdateBtDcQaRecordDTO request) {
    BtDcQaRecordDTO recordDTO = new BtDcQaRecordDTO();
    recordDTO.setSessionId(request.getClientId());
    recordDTO.setFeedbackReason(request.getFeedbackReason());
    recordDTO.setFeedbackTime(new Date());
    btDcQaRecordManageMapper.updateBtDcQaRecordReason(recordDTO);
    return ResultVO.success();
  }

  /**
   * 通过问答id删除问答记录
   * @param qaId
   * @param tenantId
   */
  @Override
  public void deleteBtDcQaRecordInfo(Long qaId, Long tenantId) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    if (userId == null) {
      throw new BssException("用户未登录");
    }

    List<Long> deptIds = null;
    Long spaceId = SpaceContextHolder.getRequiredSpaceId();
    List<OrgDTO> orgList = portalOrgIntegration.queryUserOrgList(spaceId, userId);
    if (CollectionUtils.isNotEmpty(orgList)) {
      deptIds = orgList.stream().map(OrgDTO::getOrgId).collect(Collectors.toList());
    }

    if (!SessionUtil.isSuperAdmin(userId)) {
      List<SimpleKnowledgeBaseDTO> knowledgeDtos = btDcQaRecordManageMapper.selectKnowledgeByQaId(qaId, tenantId);
      if (!CollectionUtils.isEmpty(knowledgeDtos)) { // 如果知识库已经失效了那么直接删除吧
        Map<Long, String> longStringMap = btDcKbPermissionHelper
          .batchQuerySimpleKnowledgeBasePermissionType(knowledgeDtos, userId, deptIds);
        if (!MapUtils.isEmpty(longStringMap)) {
          Set<Entry<Long, String>> entries = longStringMap.entrySet();
          boolean permission = false;
          for (Entry<Long, String> entry : entries) {
            if (LibraryRoleEnum.MANAGE.getCode().equals(entry.getValue())
              || LibraryRoleEnum.EDIT.getCode().equals(entry.getValue())) {
              permission = true;
              break;
            }
          }
          if (!permission) {
            throw new BssException("没有相关知识库的管理权限");
          }
        }
        else {
          throw new BssException("没有相关知识库的管理权限");
        }
      }
    }

    btDcQaRecordManageMapper.deleteBtDcQaRecord(qaId, tenantId);
    btDcQaChunkReferenceManageMapper.deleteBtDcQaChunkReference(qaId, tenantId);
    btDcQaRecordKbRelManageMapper.deleteBtDcQaRecordKbRel(qaId, tenantId);
  }

  /**
   * 删除知识库文档的时候需要把问答记录也失效了，需要判断有什么文档删除了
   * @param knowledgeId
   * @param tenantId
   */
  @Transactional
  @Override
  public void deleteBtDcQaRecordInfoByknowledgeId(Long knowledgeId, Long extSystemId, Long tenantId) {
    if (extSystemId == null) {
      return;
    }

    List<BtDcQaChunkReferenceDTO> referenceDTOS = btDcQaChunkReferenceManageMapper.selectBtDcQaChunkReferenceListByknowledgeId(extSystemId, tenantId); // 通过知识库查询引用片段

    if (CollectionUtils.isEmpty(referenceDTOS)) {
      return;
    }

    // 根据qa_id进行分组
    Map<Long, List<BtDcQaChunkReferenceDTO>> groupedByQaId = referenceDTOS.stream().collect(Collectors.groupingBy(BtDcQaChunkReferenceDTO::getQaId));

    // 遍历每个qa_id
    groupedByQaId.forEach((qaId, knowledgeChunkList) -> {
      // 通过qaId和tenantId查询该问答的所有引用片段
      List<BtDcQaChunkReferenceDTO> allChunkList = btDcQaChunkReferenceManageMapper.selectBtDcQaChunkReferenceList(qaId,
        tenantId);

      // 比较数量
      if (allChunkList != null && allChunkList.size() == knowledgeChunkList.size()) {
        // 数量一致，说明该问答的所有引用片段都来自当前知识库，可以删除整个问答记录
        btDcQaRecordManageMapper.deleteBtDcQaRecord(qaId, tenantId);
        btDcQaChunkReferenceManageMapper.deleteBtDcQaChunkReference(qaId, tenantId);
        btDcQaRecordKbRelManageMapper.deleteBtDcQaRecordKbRel(qaId, tenantId);
      }
      else if (allChunkList != null && allChunkList.size() > knowledgeChunkList.size()) {
        // 数量不一致，说明该问答还引用了其他知识库的片段，需要另外处理
        // 按kbId分组，每个分组的值为该知识库下的docId的Set列表
        Map<Long, Set<Long>> kbIdToDocIdsMap = allChunkList.stream().collect(Collectors.groupingBy(
          BtDcQaChunkReferenceDTO::getKbId, Collectors.mapping(BtDcQaChunkReferenceDTO::getDocId, Collectors.toSet())));

        // 通过knowledgeId获取该知识库的docId列表
        Set<Long> docIdSet = kbIdToDocIdsMap.get(knowledgeId);

        if (docIdSet != null && docIdSet.size() == 1 && docIdSet.contains(extSystemId)) {
          // 如果该知识库只有一个文档，并且就是当前要删除的extSystemId
          // 删除问答与知识库的关联关系
          btDcQaRecordKbRelManageMapper.deleteBtDcQaRecordKbRelByKnowledgeId(qaId, knowledgeId, tenantId);
        }

        // 删除该知识库下的引用片段
        btDcQaChunkReferenceManageMapper.deleteBtDcQaChunkReferenceByExtSystemId(extSystemId, tenantId, qaId);
      }
    });
  }

  /**
   * 保存消息记录
   */
  @Transactional
  @Override
  public void saveAuestionsAndAnswerRecord(KnowledgeChatParamsDTO params, List<ReferenceDocumentDTO> references, String text, String chatLogId, Long timeSpent) {
    if (CollectionUtils.isEmpty(references)) {
      return;
    }
    if (StringUtils.isEmpty(params.getQuestion())) {
      return;
    }
    List<BtDcQaRecordDTO> btDcQaRecords = new ArrayList<>();
    List<BtDcQaChunkReferenceDTO> btDcQaChunkReferences = new ArrayList<>();
    List<BtDcQaRecordKbRelDTO> btDcQaRecordKbRels = new ArrayList<>();

    try {
      processStepLog(params, references, text, chatLogId, timeSpent, params.getUserId(), btDcQaRecords, btDcQaChunkReferences, btDcQaRecordKbRels);
    }
    catch (Exception e) {
      logger.error("记录飞轮记录的时候报错", e);
      return;
    }

    saveToDatabase(btDcQaRecords, btDcQaChunkReferences, btDcQaRecordKbRels);
    applyStandardQuestionVectors(btDcQaRecords);
  }

  @Transactional
  @Override
  public void saveAuestionsAndReCallRecord(KnowledgeRecallResponse finalResponse, Long tenantId, Long timeSpent,
    List<Long> knowledgeIds, String question, Long userId) {
    if (StringUtils.isEmpty(question)) {
      return;
    }
    List<BtDcQaRecordDTO> btDcQaRecords = new ArrayList<>();
    List<BtDcQaChunkReferenceDTO> btDcQaChunkReferences = new ArrayList<>();
    List<BtDcQaRecordKbRelDTO> btDcQaRecordKbRels = new ArrayList<>();
    try {
      processReCallStepLog(finalResponse, question, knowledgeIds, tenantId, timeSpent, userId, btDcQaRecords, btDcQaChunkReferences, btDcQaRecordKbRels);
    }
    catch (Exception e) {
      logger.error("记录飞轮记录的时候报错", e);
      return;
    }

    saveToDatabase(btDcQaRecords, btDcQaChunkReferences, btDcQaRecordKbRels);
    applyStandardQuestionVectors(btDcQaRecords);
  }

  private void processReCallStepLog(KnowledgeRecallResponse finalResponse, String question, List<Long> knowledgeIds, Long tenantId, Long timeSpent, Long userId, List<BtDcQaRecordDTO> btDcQaRecords, List<BtDcQaChunkReferenceDTO> btDcQaChunkReferences, List<BtDcQaRecordKbRelDTO> btDcQaRecordKbRels) {
    BtDcQaRecordDTO btDcQaRecordDTO = new BtDcQaRecordDTO();
    btDcQaRecordDTO.setResponseTime(timeSpent);
    btDcQaRecordDTO.setChunkCount(0L);
    btDcQaRecordDTO.setQaId(DocSequences.DC_QA_RECORD_ID.next());
    btDcQaRecordDTO.setQuestion(question);
    btDcQaRecordDTO.setTenantId(tenantId);
    btDcQaRecordDTO.setCreatorId(userId);
    btDcQaRecordDTO.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    btDcQaRecords.add(btDcQaRecordDTO);
    List<Long> docIds = new ArrayList<>();
    if (finalResponse != null) {
      if (!CollectionUtils.isEmpty(finalResponse.getText())) {
        List<Long> collect = finalResponse.getText().stream().map(KnowledgeRecallTextItem::getDocId).map(Long::valueOf)
          .collect(Collectors.toList());
        docIds.addAll(collect);
      }
      if (!CollectionUtils.isEmpty(finalResponse.getImage())) {
        List<Long> collect = finalResponse.getImage().stream().map(KnowledgeRecallImageItem::getDocId)
          .map(Long::valueOf).collect(Collectors.toList());
        docIds.addAll(collect);
      }
      if (!CollectionUtils.isEmpty(docIds)) {
        // 去重
        docIds = docIds.stream().distinct().collect(Collectors.toList());

        List<DoKbIDAndDocIdDTO> docIdDTOS = documentManageMapper.selectDocumentListBydcId(knowledgeIds, docIds,
          tenantId);

        buildKnowledgeBaseRelations(docIdDTOS, btDcQaRecordDTO.getQaId(), userId, btDcQaRecordKbRels, tenantId);
        buildChunkReferences(finalResponse, docIdDTOS, btDcQaRecordDTO, userId, btDcQaChunkReferences, tenantId);
      }
    }




  }

  /**
   * 处理单个步骤日志
   */
  private void processStepLog(KnowledgeChatParamsDTO knowledgeChatParamsDTO, List<ReferenceDocumentDTO> references, String text, String chatLogId, Long timeSpent,
    Long userId, List<BtDcQaRecordDTO> btDcQaRecords, List<BtDcQaChunkReferenceDTO> btDcQaChunkReferences, List<BtDcQaRecordKbRelDTO> btDcQaRecordKbRels) {

    BtDcQaRecordDTO btDcQaRecordDTO = createQaRecord(knowledgeChatParamsDTO, timeSpent);
    btDcQaRecords.add(btDcQaRecordDTO);


    processReferences(references, knowledgeChatParamsDTO.getKnowledgeIds(),
      btDcQaRecordDTO, userId, knowledgeChatParamsDTO.getTenantId(), btDcQaChunkReferences, btDcQaRecordKbRels);

    completeQaRecord(btDcQaRecordDTO, text, chatLogId, userId);
  }

  /**
   * 创建问答记录
   */
  private BtDcQaRecordDTO createQaRecord(KnowledgeChatParamsDTO knowledgeChatParamsDTO, Long timeSpent) {
    BtDcQaRecordDTO btDcQaRecordDTO = new BtDcQaRecordDTO();
    btDcQaRecordDTO.setResponseTime(timeSpent);
    btDcQaRecordDTO.setSessionId(knowledgeChatParamsDTO.getClientId());
    btDcQaRecordDTO.setChunkCount(0L);
    btDcQaRecordDTO.setQaId(DocSequences.DC_QA_RECORD_ID.next());
    btDcQaRecordDTO.setQuestion(knowledgeChatParamsDTO.getQuestion());
    btDcQaRecordDTO.setTenantId(knowledgeChatParamsDTO.getTenantId());
    btDcQaRecordDTO.setBotId(knowledgeChatParamsDTO.getBotId());
    return btDcQaRecordDTO;
  }

  /**
   * 处理引用文档
   */
  private void processReferences(List<ReferenceDocumentDTO> referenceDocuments, List<Long> knowledgeIds,
    BtDcQaRecordDTO btDcQaRecordDTO, Long userId, Long tenantId,
    List<BtDcQaChunkReferenceDTO> btDcQaChunkReferences,
    List<BtDcQaRecordKbRelDTO> btDcQaRecordKbRels) {
    List<Long> docIds = referenceDocuments.stream()
      .map(ReferenceDocumentDTO::getId)
      .map(Long::valueOf)
      .distinct()
      .collect(Collectors.toList());

    List<DoKbIDAndDocIdDTO> docIdDTOS = documentManageMapper.selectDocumentListBydcId(knowledgeIds, docIds, tenantId);

    buildKnowledgeBaseRelations(docIdDTOS, btDcQaRecordDTO.getQaId(), userId, btDcQaRecordKbRels, tenantId);
    buildChunkReferences(referenceDocuments, docIdDTOS, btDcQaRecordDTO, userId, btDcQaChunkReferences, tenantId);
  }

  /**
   * 构建知识库关系
   */
  private void buildKnowledgeBaseRelations(List<DoKbIDAndDocIdDTO> docIdDTOS, Long qaId, Long userId,
    List<BtDcQaRecordKbRelDTO> btDcQaRecordKbRels, Long tenantId) {
    List<Long> kbIds = new ArrayList<>();
    docIdDTOS.forEach(rel -> {
      if (!kbIds.contains(rel.getKnowledgeId())) {
        BtDcQaRecordKbRelDTO btDcQaRecordKbRelDTO = new BtDcQaRecordKbRelDTO();
        btDcQaRecordKbRelDTO.setKbId(rel.getKnowledgeId());
        btDcQaRecordKbRelDTO.setRelId(DocSequences.BT_DC_QA_RECORD_KB_REL_ID.next());
        btDcQaRecordKbRelDTO.setQaId(qaId);
        btDcQaRecordKbRelDTO.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
        btDcQaRecordKbRelDTO.setCreatorId(userId);
        btDcQaRecordKbRelDTO.setTenantId(tenantId);
        kbIds.add(rel.getKnowledgeId());
        btDcQaRecordKbRels.add(btDcQaRecordKbRelDTO);
      }
    });
  }

  /**
   * 构建片段引用
   */
  private void buildChunkReferences(KnowledgeRecallResponse recallResponse, List<DoKbIDAndDocIdDTO> docIdDTOS, BtDcQaRecordDTO btDcQaRecordDTO, Long userId, List<BtDcQaChunkReferenceDTO> btDcQaChunkReferences, Long tenantId) {
    Map<Long, DoKbIDAndDocIdDTO> extSystemIdMap = docIdDTOS.stream().filter(dto -> dto.getExtSystemId() != null).collect(Collectors.toMap(DoKbIDAndDocIdDTO::getExtSystemId, dto -> dto, (existing, replacement) -> existing));

    BigDecimal totalBigDecimal = BigDecimal.ZERO;

    if (!CollectionUtils.isEmpty(recallResponse.getText())) {
      btDcQaRecordDTO.setChunkCount(btDcQaRecordDTO.getChunkCount() + recallResponse.getText().size());
      for (KnowledgeRecallTextItem chunk : recallResponse.getText()) {
        BigDecimal score = new BigDecimal(chunk.getScore());
        totalBigDecimal = totalBigDecimal.add(score);

        BtDcQaChunkReferenceDTO chunkReference = createRecallChunkReference(chunk, btDcQaRecordDTO, userId);
        chunkReference.setTenantId(tenantId);
        setKnowledgeBaseId(chunkReference, extSystemIdMap);
        btDcQaChunkReferences.add(chunkReference);
      }
    }
    if (!CollectionUtils.isEmpty(recallResponse.getImage())) {
      btDcQaRecordDTO.setChunkCount(btDcQaRecordDTO.getChunkCount() + recallResponse.getImage().size());
      for (KnowledgeRecallImageItem chunk : recallResponse.getImage()) {
        BigDecimal score = new BigDecimal(chunk.getScore());
        totalBigDecimal = totalBigDecimal.add(score);

        BtDcQaChunkReferenceDTO chunkReference = createRecallImageChunkReference(chunk, btDcQaRecordDTO, userId);
        chunkReference.setTenantId(tenantId);
        setKnowledgeBaseId(chunkReference, extSystemIdMap);
        btDcQaChunkReferences.add(chunkReference);
      }
    }

    // 计算平均置信度分数，避免除零错误和无限小数
    if (btDcQaRecordDTO.getChunkCount() > 0) {
      btDcQaRecordDTO.setConfidenceScore(totalBigDecimal.divide(new BigDecimal(btDcQaRecordDTO.getChunkCount()), 4, RoundingMode.HALF_UP));
    }
    else {
      btDcQaRecordDTO.setConfidenceScore(BigDecimal.ZERO);
    }
  }

  /**
   * 构建片段引用
   */
  private void buildChunkReferences(List<ReferenceDocumentDTO> referenceDocuments, List<DoKbIDAndDocIdDTO> docIdDTOS, BtDcQaRecordDTO btDcQaRecordDTO, Long userId, List<BtDcQaChunkReferenceDTO> btDcQaChunkReferences, Long tenantId) {
    Map<Long, DoKbIDAndDocIdDTO> extSystemIdMap = docIdDTOS.stream().filter(dto -> dto.getExtSystemId() != null).collect(Collectors.toMap(DoKbIDAndDocIdDTO::getExtSystemId, dto -> dto, (existing, replacement) -> existing));

    BigDecimal totalBigDecimal = BigDecimal.ZERO;

    if (CollectionUtils.isEmpty(referenceDocuments)) {
      btDcQaRecordDTO.setConfidenceScore(BigDecimal.ZERO);
      return;
    }
    try {
      TenantIdUtil.setTenantId(btDcQaRecordDTO.getTenantId());
      for (ReferenceDocumentDTO rd : referenceDocuments) {
        List<ReferenceChunkDTO> chunks = rd.getChunks();
        if (CollectionUtils.isEmpty(chunks)) {
          continue;
        }
        btDcQaRecordDTO.setChunkCount(btDcQaRecordDTO.getChunkCount() + chunks.size());

        for (ReferenceChunkDTO chunk : chunks) {
          BigDecimal score = new BigDecimal(chunk.getScore());
          totalBigDecimal = totalBigDecimal.add(score);

          BtDcQaChunkReferenceDTO chunkReference = createChunkReference(chunk, rd, btDcQaRecordDTO, userId);
          chunkReference.setTenantId(tenantId);
          setKnowledgeBaseId(chunkReference, extSystemIdMap);
          btDcQaChunkReferences.add(chunkReference);
        }
      }
    }
    finally {
      TenantIdUtil.clearThreadLocal();
    }

    // 计算平均置信度分数，避免除零错误和无限小数
    if (btDcQaRecordDTO.getChunkCount() > 0) {
      btDcQaRecordDTO.setConfidenceScore(totalBigDecimal.divide(new BigDecimal(btDcQaRecordDTO.getChunkCount()), 4, RoundingMode.HALF_UP));
    }
    else {
      btDcQaRecordDTO.setConfidenceScore(BigDecimal.ZERO);
    }
  }

  /**
   * 创建片段引用
   */
  private BtDcQaChunkReferenceDTO createRecallImageChunkReference(KnowledgeRecallImageItem chunk,
    BtDcQaRecordDTO btDcQaRecordDTO, Long userId) {
    BtDcQaChunkReferenceDTO chunkReference = new BtDcQaChunkReferenceDTO();
    chunkReference.setChunkContent(chunk.getPath());
    chunkReference.setChunkTitle(chunk.getHeading() == null ? chunk.getDocName() : chunk.getHeading());
    chunkReference.setDocId(Long.valueOf(chunk.getDocId()));
    chunkReference.setKbId(-1L);
    // chunkReference.setReferenceType(rd.getType());
    chunkReference.setChunkId(chunk.getChunkId());
    chunkReference.setQaId(btDcQaRecordDTO.getQaId());
    chunkReference.setDocumentName(chunk.getDocName());
    chunkReference.setRelevanceScore(new BigDecimal(chunk.getScore()));
    chunkReference.setReferenceId(DocSequences.BDC_QA_CHUNK_REFERENCE_ID.next());
    chunkReference.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    chunkReference.setCreatorId(userId);
    return chunkReference;
  }

  /**
   * 创建片段引用
   */
  private BtDcQaChunkReferenceDTO createRecallChunkReference(KnowledgeRecallTextItem chunk,
    BtDcQaRecordDTO btDcQaRecordDTO, Long userId) {
    BtDcQaChunkReferenceDTO chunkReference = new BtDcQaChunkReferenceDTO();
    String chunkTitle = chunk.getHeading() == null ? chunk.getDocName() : chunk.getHeading();
    chunkReference.setChunkTitle(chunkTitle);
    // 确保 chunkContent 不为 null，优先使用 content，如果为空则使用 title
    String chunkContent = StringUtils.isNotBlank(chunk.getContent()) ? chunk.getContent() : chunkTitle;
    chunkReference.setChunkContent(StringUtils.isNotBlank(chunkContent) ? chunkContent : "");
    chunkReference.setDocId(Long.valueOf(chunk.getDocId()));
    chunkReference.setKbId(-1L);
    // chunkReference.setReferenceType(rd.getType());
    chunkReference.setChunkId(chunk.getChunkId());
    chunkReference.setQaId(btDcQaRecordDTO.getQaId());
    chunkReference.setDocumentName(chunk.getDocName());
    chunkReference.setRelevanceScore(new BigDecimal(chunk.getScore()));
    chunkReference.setReferenceId(DocSequences.BDC_QA_CHUNK_REFERENCE_ID.next());
    chunkReference.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    chunkReference.setCreatorId(userId);
    return chunkReference;
  }

  /**
   * 创建片段引用
   */
  private BtDcQaChunkReferenceDTO createChunkReference(ReferenceChunkDTO chunk, ReferenceDocumentDTO rd,
    BtDcQaRecordDTO btDcQaRecordDTO, Long userId) {
    BtDcQaChunkReferenceDTO chunkReference = new BtDcQaChunkReferenceDTO();
    String chunkName = chunk.getName();
    chunkReference.setChunkTitle(chunkName);
    // 先设置默认值，避免为 null
    chunkReference.setChunkContent(StringUtils.isNotBlank(chunkName) ? chunkName : "");
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("doc_id", chunk.getId());
      params.put("chunk_id", chunk.getChunkId());
      ResponseEntity<Object> docChunk = docChainApiService.findDocChunk(params);
      if (docChunk != null) {
        Object body = docChunk.getBody();
        if (body != null) {
          enrichChunkReferenceFromDocChain(body, chunkReference);
        }
      }
    }
    catch (Exception e) {
      logger.error("调用docchain的知识片段失败", e);
    }
    // 确保 chunkContent 不为 null
    if (StringUtils.isBlank(chunkReference.getChunkContent())) {
      chunkReference.setChunkContent(StringUtils.isNotBlank(chunkReference.getChunkTitle())
        ? chunkReference.getChunkTitle()
        : StringUtils.isNotBlank(rd.getName()) ? rd.getName() : "");
    }
    chunkReference.setDocId(Long.valueOf(chunk.getId()));
    chunkReference.setKbId(-1L);
    chunkReference.setReferenceType(rd.getType());
    chunkReference.setChunkId(chunk.getChunkId());
    chunkReference.setQaId(btDcQaRecordDTO.getQaId());
    chunkReference.setDocumentName(rd.getName());
    chunkReference.setRelevanceScore(new BigDecimal(chunk.getScore()));
    chunkReference.setReferenceId(DocSequences.BDC_QA_CHUNK_REFERENCE_ID.next());
    chunkReference.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    chunkReference.setCreatorId(userId);
    return chunkReference;
  }

  /**
   * 从 DocChain 响应中补充片段引用信息
   *
   * @param responseBody DocChain API 响应体
   * @param chunkReference 待补充的片段引用对象
   */
  private void enrichChunkReferenceFromDocChain(Object responseBody, BtDcQaChunkReferenceDTO chunkReference) {
    ReferenceChunkResponse convert = JsonUtil.convert(responseBody, ReferenceChunkResponse.class);

    ReferenceChunkResponseData data = convert.getData();
    if (data == null) {
      return;
    }

    data.convert();
    // 确保 content 不为 null
    String content = data.getContent();
    if (StringUtils.isNotBlank(content)) {
      chunkReference.setChunkContent(content);
    }
    // 如果 heading 不为空，更新 title
    String heading = data.getHeading();
    if (StringUtils.isNotBlank(heading)) {
      chunkReference.setChunkTitle(heading);
    }
  }

  /**
   * 设置知识库ID
   */
  private void setKnowledgeBaseId(BtDcQaChunkReferenceDTO chunkReference,
    Map<Long, DoKbIDAndDocIdDTO> extSystemIdMap) {
    DoKbIDAndDocIdDTO docInfo = extSystemIdMap.get(chunkReference.getDocId());
    if (docInfo != null) {
      chunkReference.setKbId(docInfo.getKnowledgeId());
    }
  }

  /**
   * 完成问答记录
   */
  private void completeQaRecord(BtDcQaRecordDTO btDcQaRecordDTO, String text, String chatLogId, Long userId) {
    btDcQaRecordDTO.setChatLogId(chatLogId);
    btDcQaRecordDTO.setAnswer(text);
    btDcQaRecordDTO.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    btDcQaRecordDTO.setCreatorId(userId);
  }

  /**
   * 库表已落库后做标准问法向量化
   */
  private void applyStandardQuestionVectors(List<BtDcQaRecordDTO> btDcQaRecords) {
    if (CollectionUtils.isEmpty(btDcQaRecords)) {
      return;
    }
    for (BtDcQaRecordDTO record : btDcQaRecords) {
      btDcQaKnowledgeVectorCore.applyStandardQuestion(record.getTenantId(), record.getQaId(), record.getSessionId(), record.getQuestion());
    }
  }

  private void saveToDatabase(List<BtDcQaRecordDTO> btDcQaRecords,
    List<BtDcQaChunkReferenceDTO> btDcQaChunkReferences,
    List<BtDcQaRecordKbRelDTO> btDcQaRecordKbRels) {
    // 确保所有 chunkContent 不为 null，避免数据库约束违反
    if (!btDcQaChunkReferences.isEmpty()) {
      for (BtDcQaChunkReferenceDTO chunkReference : btDcQaChunkReferences) {
        if (StringUtils.isBlank(chunkReference.getChunkContent())) {
          // 如果 chunkContent 为空，使用 chunkTitle 或默认值
          String defaultContent = StringUtils.isNotBlank(chunkReference.getChunkTitle())
            ? chunkReference.getChunkTitle()
            : StringUtils.isNotBlank(chunkReference.getDocumentName())
              ? chunkReference.getDocumentName()
              : "";
          chunkReference.setChunkContent(defaultContent);
          if (logger.isWarnEnabled()) {
            logger.warn("chunkContent 为空，已设置默认值: referenceId={}, qaId={}, docId={}, defaultContent={}",
              chunkReference.getReferenceId(), chunkReference.getQaId(), chunkReference.getDocId(), defaultContent);
          }
        }
      }
    }
    TransactionUtil.execute(() -> {
      if (!btDcQaRecords.isEmpty()) {
        btDcQaRecordManageMapper.batchInsertBtDcQaRecord(btDcQaRecords);
      }
      if (!btDcQaChunkReferences.isEmpty()) {
        btDcQaChunkReferenceManageMapper.batchInsertBtDcQaChunkReference(btDcQaChunkReferences);
      }
      if (!btDcQaRecordKbRels.isEmpty()) {
        btDcQaRecordKbRelManageMapper.batchInsertBtDcQaRecordKbRel(btDcQaRecordKbRels);
      }
    });
  }

  /**
   * 根据时间类型查询统计结果
   *
   * @param params 查询参数
   * @return 统计结果
   */
  private BtDcQaRecordStatisticsDTO queryStatisticsByTimeType(BtDcQaRecordNoticeBoardQueryParams params) {
    if (params.getTimeType() == null) {
      return null;
    }

    // 设置查询时间范围
    Date startTime = calculateStartTime(params.getTimeType());
    if (startTime != null) {
      params.setStartTime(startTime);
    }
    params.setEndTime(new Date());

    return btDcQaRecordManageMapper.selectBtDcQaRecordStatistics(params);
  }


  /**
   * 根据cartType统计维度查询不同的数据
   *
   * @param params 查询参数
   * @return 按天统计数据
   */
  private List<BtDcQaRecordItemDto> queryStatisticsByCartType(BtDcQaRecordNoticeBoardQueryParams params) {
    String cartType = params.getCartType();

    if ("qaCount".equals(cartType)) {
      // 问答次数统计
      return queryDailyQaCountStatistics(params);
    }
    else if ("qaPeoples".equals(cartType)) {
      // 问答人数统计
      return queryDailyQaPeoplesStatistics(params);
    }
    else if ("hitRate".equals(cartType)) {
      // 问答命中率统计
      return queryDailyHitRateStatistics(params);
    }

    // 默认返回问答次数
    return queryDailyQaCountStatistics(params);
  }

  /**
   * 使用SQL按天统计问答次数
   *
   * @param params 查询参数
   * @return 按天统计数据
   */
  private List<BtDcQaRecordItemDto> queryDailyQaCountStatistics(BtDcQaRecordNoticeBoardQueryParams params) {
    // 设置查询时间范围
    Date startTime = calculateStartTime(params.getTimeType());
    if (startTime != null) {
      params.setStartTime(startTime);
    }
    params.setEndTime(new Date());

    List<BtDcQaRecordItemDto> dailyStats = btDcQaRecordManageMapper.selectBtDcQaRecordDailyStatistics(params);

    // 确保返回完整的天数据
    return ensureCompleteDailyData(dailyStats, params.getTimeType());
  }

  /**
   * 使用SQL按天统计问答人数
   *
   * @param params 查询参数
   * @return 按天统计数据
   */
  private List<BtDcQaRecordItemDto> queryDailyQaPeoplesStatistics(BtDcQaRecordNoticeBoardQueryParams params) {
    // 设置查询时间范围
    Date startTime = calculateStartTime(params.getTimeType());
    if (startTime != null) {
      params.setStartTime(startTime);
    }
    params.setEndTime(new Date());

    List<BtDcQaRecordItemDto> dailyStats = btDcQaRecordManageMapper.selectBtDcQaRecordDailyQaPeoplesStatistics(params);

    // 确保返回完整的天数据
    return ensureCompleteDailyData(dailyStats, params.getTimeType());
  }

  /**
   * 使用SQL按天统计问答命中率
   *
   * @param params 查询参数
   * @return 按天统计数据
   */
  private List<BtDcQaRecordItemDto> queryDailyHitRateStatistics(BtDcQaRecordNoticeBoardQueryParams params) {
    // 设置查询时间范围
    Date startTime = calculateStartTime(params.getTimeType());
    if (startTime != null) {
      params.setStartTime(startTime);
    }
    params.setEndTime(new Date());

    List<BtDcQaRecordItemDto> dailyStats = btDcQaRecordManageMapper.selectBtDcQaRecordDailyHitRateStatistics(params);

    // 确保返回完整的天数据
    return ensureCompleteDailyData(dailyStats, params.getTimeType());
  }

  /**
   * 确保返回完整的天数据
   *
   * @param dailyStats SQL查询的按天统计数据
   * @param timeType 时间类型
   * @return 完整的天数据
   */
  private List<BtDcQaRecordItemDto> ensureCompleteDailyData(List<BtDcQaRecordItemDto> dailyStats, String timeType) {
    List<BtDcQaRecordItemDto> result = new ArrayList<>();
    Date currentDate = new Date();

    // 将SQL查询结果转换为Map，便于查找
    Map<String, String> statsMap = new HashMap<>();
    if (!CollectionUtils.isEmpty(dailyStats)) {
      for (BtDcQaRecordItemDto item : dailyStats) {
        statsMap.put(item.getName(), item.getValue());
      }
    }

    // 根据timeType确定需要统计的天数
    int daysToCount = getDaysCountByTimeType(timeType);

    // 从当前日期往前推指定天数
    for (int i = 0; i < daysToCount; i++) {
      Date dayDate = DateUtils.addDays(DateUtils.truncate(currentDate, Calendar.DAY_OF_MONTH), -i);
      String day = DateUtil.format(dayDate, "yyyyMMdd");
      String count = statsMap.getOrDefault(day, "0");

      BtDcQaRecordItemDto item = new BtDcQaRecordItemDto();
      item.setName(day);
      item.setValue(count);
      result.add(item);
    }

    // 按日期排序（从早到晚）
    return result.stream().sorted((a, b) -> a.getName().compareTo(b.getName())).collect(Collectors.toList());
  }

  /**
   * 根据时间类型获取需要统计的天数
   *
   * @param timeType 时间类型
   * @return 天数
   */
  private int getDaysCountByTimeType(String timeType) {
    switch (timeType) {
      case DocBaseConsts.TIME_TYPE_1D: // 今天
        return 1;
      case DocBaseConsts.TIME_TYPE_3D: // 三天
        return 3;
      case DocBaseConsts.TIME_TYPE_1W: // 一周
        return 7;
      case DocBaseConsts.TIME_TYPE_1M: // 一个月
        return 30;
      case DocBaseConsts.TIME_TYPE_1Y: // 一年
        return 365;
      default:
        return 1;
    }
  }

  /**
   * 根据时间类型计算开始时间
   *
   * @param timeType 时间类型：1D-今天，3D-三天，1W-一周，1M-一个月，1Y-一年
   * @return 开始时间
   */
  private Date calculateStartTime(String timeType) {
    Date now = new Date();

    switch (timeType) {
      case DocBaseConsts.TIME_TYPE_1D: // 今天
        return DateUtils.truncate(now, Calendar.DAY_OF_MONTH);
      case DocBaseConsts.TIME_TYPE_3D: // 三天
        return DateUtils.addDays(DateUtils.truncate(now, Calendar.DAY_OF_MONTH), -3);
      case DocBaseConsts.TIME_TYPE_1W: // 一周
        return DateUtils.addWeeks(DateUtils.truncate(now, Calendar.DAY_OF_MONTH), -1);
      case DocBaseConsts.TIME_TYPE_1M: // 一个月
        return DateUtils.addMonths(DateUtils.truncate(now, Calendar.DAY_OF_MONTH), -1);
      case DocBaseConsts.TIME_TYPE_1Y: // 一年
        return DateUtils.addYears(DateUtils.truncate(now, Calendar.DAY_OF_MONTH), -1);
      default:
        return null;
    }
  }

  /**
   * 根据docId和对应的引用片段列表创建BtDcQaDocumentChunkRefernceDTO对象
   *
   * @param docId 文档ID
   * @param chunkReferences 该文档的引用片段列表
   * @return BtDcQaDocumentChunkRefernceDTO对象
   */
  private BtDcQaDocumentChunkRefernceDTO createDocumentChunkReferenceDTO(Long docId, List<BtDcQaChunkReferenceDTO> chunkReferences, Long tenantId) {
    BtDcQaDocumentChunkRefernceDTO dto = new BtDcQaDocumentChunkRefernceDTO();

    // 设置基本信息
    dto.setDocId(docId);
    dto.setChunkCount((long) chunkReferences.size());
    dto.setChunkReferences(chunkReferences);
    if (CollectionUtils.isNotEmpty(chunkReferences)) {
      chunkReferences.forEach(chunkReference -> {
        if (StringUtils.isNotEmpty(chunkReference.getChunkContent())) {
          chunkReference.setChunkContent(docChainDocumentHelper
            .replaceImageUrlsWithEncodedPath(chunkReference.getChunkContent(), tenantId));
        }
      });
    }

    // 从第一个引用片段中获取文档信息（假设同一文档的所有片段具有相同的文档信息）
    if (!CollectionUtils.isEmpty(chunkReferences)) {
      BtDcQaChunkReferenceDTO firstChunk = chunkReferences.get(0);
      dto.setDocName(firstChunk.getDocumentName());
      dto.setDocumentId(firstChunk.getDocumentId());
      dto.setKnowledgeName(firstChunk.getKnowledgeName());
      dto.setKbId(firstChunk.getKbId());
      dto.setLibraryId(firstChunk.getLibraryId());
      dto.setIsExist(firstChunk.getIsExist());
      // 如果有dcDocumentId字段，也可以设置
      dto.setDcDocumentId(firstChunk.getDcDocumentId());
      dto.setContentSource(firstChunk.getContentSource());
      DocumentPathDTO pathDTO = documentService.getDocumentPath(firstChunk.getDcDocumentId());
      if (pathDTO != null) {
        dto.setResourcePath(pathDTO.getDocumentPath());
      }
    }

    return dto;
  }

  /**
   * 为QA记录列表批量设置关联数据（用户信息、知识库信息）
   *
   * @param qaRecords QA记录列表
   */
  private void enrichQaRecordsWithRelatedData(List<BtDcQaRecordDTO> qaRecords, BtDcQaRecordQueryParams queryParams) {
    if (CollectionUtils.isEmpty(qaRecords)) {
      return;
    }

    // 批量查询用户信息
    Map<Long, PortalUserDTO> userMap = batchQueryUserInfo(qaRecords);

    // 批量查询知识库信息
    Map<Long, List<BtDcQaRecorddKnowledgeDTO>> knowledgeMap = batchQueryKnowledgeInfo(qaRecords, queryParams);

    // 设置关联信息到每个QA记录
    qaRecords.forEach(qaRecord -> {
      setUserInfo(qaRecord, userMap);
      setKnowledgeInfo(qaRecord, knowledgeMap);
    });
  }

  /**
   * 批量查询用户信息
   */
  private Map<Long, PortalUserDTO> batchQueryUserInfo(List<BtDcQaRecordDTO> qaRecords) {
    List<Long> creatorIds = qaRecords.stream().map(BtDcQaRecordDTO::getCreatorId).distinct().collect(Collectors.toList());

    return CollectionUtils.isEmpty(creatorIds) ? Collections.emptyMap() : dcUserService.findUserMapBatchByIds(creatorIds);
  }

  /**
   * 批量查询知识库信息
   */
  private Map<Long, List<BtDcQaRecorddKnowledgeDTO>> batchQueryKnowledgeInfo(List<BtDcQaRecordDTO> qaRecords, BtDcQaRecordQueryParams queryParams) {
    List<Long> qaIds = qaRecords.stream().map(BtDcQaRecordDTO::getQaId).collect(Collectors.toList());

    if (CollectionUtils.isEmpty(qaIds)) {
      return Collections.emptyMap();
    }

    // 一次查询获取所有知识库信息
    List<BtDcQaRecorddKnowledgeDTO> allKnowledgeList = btDcQaRecordManageMapper.selectKnowledgeByQaIds(qaIds, queryParams);

    // 按qa_id分组
    return allKnowledgeList.stream().collect(Collectors.groupingBy(BtDcQaRecorddKnowledgeDTO::getQaId));
  }

  /**
   * 设置用户信息
   */
  private void setUserInfo(BtDcQaRecordDTO qaRecord, Map<Long, PortalUserDTO> userMap) {
    PortalUserDTO userInfo = userMap.get(qaRecord.getCreatorId());
    if (userInfo != null) {
      qaRecord.setCreatorName(userInfo.getUserName());
    }
  }

  /**
   * 设置知识库信息
   */
  private void setKnowledgeInfo(BtDcQaRecordDTO qaRecord, Map<Long, List<BtDcQaRecorddKnowledgeDTO>> knowledgeMap) {
    List<BtDcQaRecorddKnowledgeDTO> knowledges = knowledgeMap.get(qaRecord.getQaId());
    qaRecord.setKnowledges(knowledges != null ? knowledges : Collections.emptyList());
  }

  /**
   * 校验timeType参数
   *
   * @param timeType 时间类型参数
   * @throws BssException 如果timeType为空或不在有效范围内
   */
  private void validateTimeType(String timeType) {
    if (timeType == null || timeType.trim().isEmpty()) {
      throw new BssException("时间类型参数不能为空");
    }

    boolean isValidTimeType = DocBaseConsts.TIME_TYPE_1D.equals(timeType) ||
      DocBaseConsts.TIME_TYPE_3D.equals(timeType) ||
      DocBaseConsts.TIME_TYPE_1W.equals(timeType) ||
      DocBaseConsts.TIME_TYPE_1M.equals(timeType) ||
      DocBaseConsts.TIME_TYPE_1Y.equals(timeType);

    if (!isValidTimeType) {
      throw new BssException("时间类型参数无效，只支持：" +
        DocBaseConsts.TIME_TYPE_1D + "（今天）、" +
        DocBaseConsts.TIME_TYPE_3D + "（三天）、" +
        DocBaseConsts.TIME_TYPE_1W + "（一周）、" +
        DocBaseConsts.TIME_TYPE_1M + "（一个月）、" +
        DocBaseConsts.TIME_TYPE_1Y + "（一年）");
    }
  }

  /**
   * 校验并设置cartType参数
   * 如果为空或不在有效范围内，则设置为默认值"qaCount"（问答次数）
   *
   * @param params 查询参数
   */
  private void validateAndSetCartType(BtDcQaRecordNoticeBoardQueryParams params) {
    String cartType = params.getCartType();

    // 如果为空或不在有效范围内，设置为默认值
    if (cartType == null || cartType.trim().isEmpty() ||
      !isValidCartType(cartType)) {
      params.setCartType("qaCount");
    }
  }

  /**
   * 判断cartType是否在有效范围内
   *
   * @param cartType 统计维度类型
   * @return true表示有效，false表示无效
   */
  private boolean isValidCartType(String cartType) {
    return "hitRate".equals(cartType) ||
      "qaCount".equals(cartType) ||
      "qaPeoples".equals(cartType);
  }
}
