package com.iwhalecloud.bote.doc.module.document.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.consts.DocumentActionTypeEnum;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentActivityDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentActivityQueryParams;
import com.iwhalecloud.bote.doc.module.document.entity.DocumentActivityEntity;
import com.iwhalecloud.bote.doc.module.document.mapper.DocumentActivityMapper;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentActivityService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 文档动态记录服务实现
 *
 * @author Aiqing
 * @since 2025-08-21
 */
@Service
@RequiredArgsConstructor
public class DocumentActivityServiceImpl implements IDocumentActivityService {

  private static final Logger logger = LoggerFactory.getLogger(DocumentActivityServiceImpl.class);

  private final DocumentActivityMapper documentActivityMapper;
  private final IDcUserService dcUserService;

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public Long recordActivity(String documentId, String libraryId, Long userId, String actionType,
                             String ipAddress, String userAgent, Long tenantId, String documentName, String targetLibraryId) {
    if (StringUtils.isBlank(documentId) || userId == null || StringUtils.isBlank(actionType)) {
      logger.warn("记录文档动态失败，参数不完整: documentId={}, userId={}, actionType={}", documentId, userId, actionType);
      return null;
    }

    DocumentActivityEntity activity = new DocumentActivityEntity();
    activity.setActivityId(IDUtils.nextId());
    activity.setDocumentId(documentId);
    activity.setLibraryId(libraryId);
    activity.setUserId(userId);
    activity.setActionType(actionType);
    activity.setIpAddress(ipAddress);
    activity.setUserAgent(userAgent);
    activity.setCreatorId(userId);
    activity.setCreatedTime(new Date());
    activity.setUpdatedTime(new Date());
    activity.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    activity.setTenantId(tenantId);
    activity.setOldDocumentName(documentName);
    activity.setTargetLibraryId(targetLibraryId);
    int result = documentActivityMapper.insert(activity);
    if (result > 0) {
      logger.debug("记录文档动态成功: activityId={}, documentId={}, actionType={}",
        activity.getActivityId(), documentId, actionType);
      return activity.getActivityId();
    }
    return null;
  }

  @Override
  public PageInfo<DocumentActivityDTO> findActivityPage(DocumentActivityQueryParams queryParams) {
    if (queryParams == null) {
      return new PageInfo<>(new ArrayList<>());
    }
    RowBounds rowBounds = queryParams.buildRowBounds();
    // 查询分页数据
    Page<DocumentActivityDTO> page = documentActivityMapper.selectActivityPage(queryParams, rowBounds); //NOPMD - suppressed CloseResource - 不需要关闭

    // 转换为DTO并设置扩展字段
    PageInfo<DocumentActivityDTO> pageInfo = page.toPageInfo();

    List<Long> optUserIdList = pageInfo.getList().stream()
      .map(DocumentActivityDTO::getUserId)
      .distinct()
      .collect(Collectors.toList());

    Map<Long, PortalUserDTO> userMapBatchByIds = dcUserService.findUserMapBatchByIds(optUserIdList);

    for (DocumentActivityDTO dto : pageInfo.getList()) {
      setExtendedFields(dto);
      if (userMapBatchByIds != null) {
        PortalUserDTO portalUserDTO = userMapBatchByIds.get(dto.getUserId());
        if (portalUserDTO != null) {
          dto.setUserName(portalUserDTO.getUserName());
        }
      }
    }
    return pageInfo;
  }

  @Override
  public Long countDocumentAccess(String documentId, String actionType, LocalDateTime startTime,
                                  LocalDateTime endTime) {
    if (StringUtils.isBlank(documentId) || StringUtils.isBlank(actionType)) {
      return 0L;
    }
    return documentActivityMapper.countByDocumentIdAndActionType(documentId, actionType, startTime, endTime);
  }

  @Override
  public Long countByCondition(String documentId, String libraryId, Long userId, String actionType,
                               LocalDateTime startTime, LocalDateTime endTime) {
    return documentActivityMapper.countByCondition(documentId, libraryId, userId, actionType, startTime, endTime);
  }

  @Override
  public List<DocumentActivityDTO> findByDocumentId(String documentId, Integer limit) {
    DocumentActivityQueryParams queryParams = new DocumentActivityQueryParams();
    queryParams.setPageNum(1);
    queryParams.setPageSize(limit);
    queryParams.setDocumentId(documentId);

    PageInfo<DocumentActivityDTO> activityPage = this.findActivityPage(queryParams);
    return activityPage.getList();
  }

  /**
   * 设置扩展字段
   *
   * @param dto DTO对象
   */
  private void setExtendedFields(DocumentActivityDTO dto) {
    // 设置操作类型名称
    if (StringUtils.isNotBlank(dto.getActionType())) {
      dto.setActionTypeName(DocumentActionTypeEnum.getNameByCode(dto.getActionType()));
    }
  }
}
