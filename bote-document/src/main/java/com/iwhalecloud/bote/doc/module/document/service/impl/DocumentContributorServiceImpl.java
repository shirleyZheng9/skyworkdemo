package com.iwhalecloud.bote.doc.module.document.service.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.integration.PortalOrgIntegration;
import com.iwhalecloud.bote.doc.module.document.dto.request.QueryDocContributorsRequest;
import com.iwhalecloud.bote.doc.module.document.entity.DocumentContributorEntity;
import com.iwhalecloud.bote.doc.module.document.mapper.DocumentContributorMapper;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentContributorService;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentContributeRequestDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentContributorDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文档贡献者服务实现类
 *
 * @author Aiqing
 * @since 2025-09-18
 */
@Service
@RequiredArgsConstructor
public class DocumentContributorServiceImpl implements IDocumentContributorService {

  private final DocumentContributorMapper documentContributorMapper;
  private final PortalOrgIntegration portalOrgIntegration;

  @Override
  public List<DocumentContributorEntity> findByDocumentId(String documentId) {
    return documentContributorMapper.selectByDocumentId(documentId);
  }

  @Override
  public List<DocumentContributorEntity> findByUserId(Long userId) {
    return documentContributorMapper.selectByUserId(userId);
  }

  @Override
  @Transactional
  public boolean create(DocumentContributorEntity contributor) {
    // 设置默认值
    if (contributor.getId() == null) {
      contributor.setId(IDUtils.nextId());
    }
    if (contributor.getCreatedTime() == null) {
      contributor.setCreatedTime(new Date());
    }
    if (contributor.getUpdatedTime() == null) {
      contributor.setUpdatedTime(new Date());
    }
    if (contributor.getStatusCd() == null) {
      contributor.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    }
    if (contributor.getContributorScore() == null) {
      contributor.setContributorScore(BigDecimal.ZERO);
    }

    int result = documentContributorMapper.insert(contributor);
    return result > 0;
  }

  @Override
  @Transactional
  public boolean updateContributorScore(Long id, BigDecimal contributorScore, Long updatorId) {
    int result = documentContributorMapper.updateContributorScore(id, contributorScore, updatorId);
    return result > 0;
  }

  @Override
  public int countByDocumentId(String documentId) {
    return documentContributorMapper.countByDocumentId(documentId);
  }

  @Override
  public List<DocumentContributorEntity> getTopContributorsByDocumentId(String documentId, Integer limit) {
    return documentContributorMapper.selectTopContributorsByDocumentId(documentId, limit);
  }

  @Override
  public List<DocumentContributorEntity> getTopContributorsByUserId(Long userId, Integer limit) {
    return documentContributorMapper.selectTopContributorsByUserId(userId, limit);
  }

  @Override
  @Transactional
  public boolean addOrUpdateContributor(String documentId,
                                        Long userId,
                                        BigDecimal contributorScore,
                                        Long operatorId) {
    // 先查询是否存在记录
    DocumentContributorEntity existing = documentContributorMapper.selectByDocumentIdAndUserId(documentId, userId);

    if (existing != null) {
      // 更新现有记录
      return updateContributorScore(existing.getId(), contributorScore, operatorId);
    }
    else {
      // 创建新记录
      DocumentContributorEntity contributor = new DocumentContributorEntity();
      contributor.setDocumentId(documentId);
      contributor.setUserId(userId);
      contributor.setContributorScore(contributorScore);
      contributor.setCreatorId(operatorId);
      contributor.setUpdatorId(operatorId);
      return create(contributor);
    }
  }

  @Override
  @Transactional
  public void batchAddOrUpdateContributors(List<String> documentIds, Long userId, BigDecimal contributorScore, Long operatorId) {
    if (CollectionUtils.isEmpty(documentIds)) {
      return;
    }
    Long tenantId = TenantContextHolder.getTenantId();
    Date now = new Date();

    // 批量查询已存在的贡献者记录
    List<DocumentContributorEntity> existingContributors = documentContributorMapper.selectByDocumentIdsAndUserId(documentIds, userId);
    Map<String, DocumentContributorEntity> existingMap = existingContributors.stream()
      .collect(Collectors.toMap(DocumentContributorEntity::getDocumentId, contributor -> contributor));

    // 分别处理需要插入和更新的记录
    List<DocumentContributorEntity> toInsert = new ArrayList<>();
    List<Map<String, Object>> toUpdate = new ArrayList<>();

    for (String documentId : documentIds) {
      DocumentContributorEntity existing = existingMap.get(documentId);
      if (existing != null) {
        // 需要更新
        Map<String, Object> update = new HashMap<>();
        update.put("id", existing.getId());
        update.put("contributorScore", contributorScore);
        update.put("updatorId", operatorId);
        toUpdate.add(update);
      } else {
        // 需要插入
        DocumentContributorEntity contributor = new DocumentContributorEntity();
        contributor.setId(IDUtils.nextId());
        contributor.setDocumentId(documentId);
        contributor.setUserId(userId);
        contributor.setContributorScore(contributorScore);
        contributor.setCreatorId(operatorId);
        contributor.setUpdatorId(operatorId);
        contributor.setCreatedTime(now);
        contributor.setUpdatedTime(now);
        contributor.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
        contributor.setTenantId(tenantId);
        toInsert.add(contributor);
      }
    }

    // 批量插入新记录
    if (!toInsert.isEmpty()) {
      documentContributorMapper.batchInsert(toInsert);
    }
    // 批量更新已存在的记录
    if (!toUpdate.isEmpty()) {
      documentContributorMapper.batchUpdateContributorScore(toUpdate);
    }
  }

  @Override
  @Transactional
  public ResultVO<Void> batchAddContributors(DocumentContributeRequestDTO request) {
    List<DocumentContributorEntity> contributors = new ArrayList<>();
    Long operatorId = SessionUtil.getLoginInfo().getUserId();
    List<DocumentContributorEntity> existContributors = documentContributorMapper.selectByDocumentId(
      request.getDocumentId());
    // 获取已存在的贡献者用户ID集合
    Set<Long> existUserIdSet = existContributors.stream()
      .map(DocumentContributorEntity::getUserId)
      .collect(Collectors.toSet());

    Set<Long> pendingUserIds = new HashSet<>();
    if (CollectionUtils.isNotEmpty(request.getUserIds())) {
      pendingUserIds.addAll(request.getUserIds());
    }
    // 根据组织ID获取用户ID集合
    if (CollectionUtils.isNotEmpty(request.getOrgIds())) {
      Set<Long> userIds = portalOrgIntegration.queryUserIdsByOrgIdsWithSubOrgs(request.getSpaceId(),
        request.getOrgIds());
      if (CollectionUtils.isNotEmpty(userIds)) {
        pendingUserIds.addAll(userIds);
      }
    }
    // 过滤已存在的贡献者
    pendingUserIds.removeAll(existUserIdSet);
    Date now = new Date();
    for (Long userId : pendingUserIds) {
      DocumentContributorEntity contributor = new DocumentContributorEntity();
      contributor.setId(IDUtils.nextId());
      contributor.setDocumentId(request.getDocumentId());
      contributor.setUserId(userId);
      contributor.setContributorScore(BigDecimal.ZERO);
      contributor.setCreatorId(operatorId);
      contributor.setUpdatorId(operatorId);
      contributor.setCreatedTime(now);
      contributor.setUpdatedTime(now);
      contributor.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
      contributor.setTenantId(request.getTenantId());
      contributors.add(contributor);
    }
    if (CollectionUtils.isNotEmpty(contributors)) {
      documentContributorMapper.batchInsert(contributors);
    }
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteByDocumentId(String documentId, Long userId) {
    documentContributorMapper.deleteByDocumentIdAndUserId(documentId, userId, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  public PageInfo<DocumentContributorDTO> queryDocumentContributorPage(QueryDocContributorsRequest request) {
    RowBounds rowBounds = request.buildRowBounds();
    // noinspection resource
    return documentContributorMapper.selectDocumentContributorsPage(request, rowBounds).toPageInfo();
  }
}
