package com.iwhalecloud.bote.doc.module.person.service.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.cache.PinnedDocumentCache;
import com.iwhalecloud.bote.doc.cache.PinnedKnowledgeCache;
import com.iwhalecloud.bote.doc.cache.PinnedLibraryCache;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.common.lock.DistributedLock;
import com.iwhalecloud.bote.common.lock.DistributedLockFactory;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.common.tenant.ThreadContextRunner;
import com.iwhalecloud.bote.doc.consts.DocLockConsts;
import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.LibraryRoleEnum;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.DocumentActivityDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.FrequentLibraryDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.PinResourceRequestDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.PinnedDocumentDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.PinnedKnowledgeDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.PinnedLibraryDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.RecentFileDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.ReorderPinsRequestDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.SearchDocumentDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.UnpinResourceRequestDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.UserInfo;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.query.RecentFileQueryParams;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.query.SearchDocumentQueryParams;
import com.iwhalecloud.bote.doc.module.person.entity.UserHomepagePinEntity;
import com.iwhalecloud.bote.doc.module.person.mapper.HomepageMapper;
import com.iwhalecloud.bote.doc.module.person.mapper.UserHomepagePinMapper;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRole;
import com.iwhalecloud.bote.doc.module.control.service.DocumentNodeService;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPathDTO;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.module.library.service.DocumentLibraryPermissionService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bote.doc.module.person.service.IHomepageService;
import com.iwhalecloud.bote.doc.common.utils.PinLinkedSortHelper;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 首页服务实现
 *
 * @author lizuyin
 * @since 2025-08-15
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class HomepageServiceImpl implements IHomepageService {
  // @formatter:off
  private static final Logger logger = LoggerFactory.getLogger(HomepageServiceImpl.class);
  private final HomepageMapper homepageMapper;
  private final IDocumentService documentService;
  private final DocumentNodeService documentNodeService;
  private final UserHomepagePinMapper userHomepagePinMapper;
  private final PinnedDocumentCache pinnedDocumentCache;
  private final PinnedLibraryCache pinnedLibraryCache;
  private final PinnedKnowledgeCache pinnedKnowledgeCache;
  private final IDcUserService dcUserService;
  private final DistributedLockFactory distributedLockFactory;
  private final ControlTemplate controlTemplate;
  private final DocumentLibraryPermissionService documentLibraryPermissionService;
  private static final Integer RECENT_FILE_MAX_COUNT = 50;
  private static final Integer RECENT_FILE_MAX_PAGE_SIZE = 5;
  // @formatter:on
  @Override
  @Nullable
  public List<PinnedDocumentDTO> getPinnedDocuments(Long spaceId) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    List<PinnedDocumentDTO> pinnedDocuments = pinnedDocumentCache.getPinnedDocuments(userId, spaceId);
    if (CollectionUtils.isNotEmpty(pinnedDocuments)) {
      enrichPinnedDocuments(pinnedDocuments);
      return PinLinkedSortHelper.sortByLinkedList(pinnedDocuments);
    }
    return pinnedDocuments;
  }

  @Override
  public List<PinnedLibraryDTO> getPinnedLibraries(Long spaceId) {
    Long userId = SessionUtil.getLoginInfo().getUserId();

    List<PinnedLibraryDTO> pinnedLibraries = pinnedLibraryCache.getPinnedLibraries(userId, spaceId);
    if (CollectionUtils.isNotEmpty(pinnedLibraries)) {
      enrichPinnedLibraries(pinnedLibraries);
      return PinLinkedSortHelper.sortByLinkedList(pinnedLibraries);
    }
    return pinnedLibraries;
  }

  @Override
  public List<PinnedKnowledgeDTO> getPinnedKnowledge(Long spaceId) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    List<PinnedKnowledgeDTO> pinnedKnowledge = pinnedKnowledgeCache.getPinnedKnowledge(userId, spaceId);
    if (CollectionUtils.isNotEmpty(pinnedKnowledge)) {
      return PinLinkedSortHelper.sortByLinkedList(pinnedKnowledge);
    }
    return pinnedKnowledge;
  }

  @Override
  @Transactional
  public ResultVO<Long> pinResource(PinResourceRequestDTO dto, Long userId) {
    String targetId = dto.getTargetId();
    String targetType = dto.getTargetType();
    Long tenantId = dto.getTenantId();
    Long spaceId = dto.getSpaceId();
    Assert.notNull(targetId, "目标资源ID不能为空");
    Assert.hasText(targetType, "目标类型不能为空");
    Assert.notNull(userId, "用户ID不能为空");

    // 使用分布式锁保护置顶操作，防止并发修改导致的数据不一致
    DistributedLock lock = distributedLockFactory.getBizLock(DocLockConsts.HOMEPAGE_PIN_REORDER_LOCK, userId.toString());

    try {
      // 尝试获取锁，最多等待5秒
      if (!lock.tryLock(5, TimeUnit.SECONDS)) {
        return ResultVO.fail("操作冲突，请稍后重试");
      }

      try {
        UserHomepagePinEntity existingPin = homepageMapper.findPinRecord(userId, targetId, targetType, dto.getTenantId(), dto.getSpaceId());
        if (existingPin != null) {
          if (DocBaseConsts.STATUS_CD_VALID.equals(existingPin.getStatusCd())) {
            return ResultVO.fail("该资源已置顶");
          }
          if (DocBaseConsts.STATUS_CD_INVALID.equals(existingPin.getStatusCd())) {
            homepageMapper.reactivatePinRecord(existingPin.getPinId(), userId, dto.getTenantId());
            afterPinUpdated(userId, targetType, existingPin.getPinId(), tenantId, spaceId);
            return ResultVO.success(existingPin.getPinId());
          }
        }
        UserHomepagePinEntity pinEntity = new UserHomepagePinEntity();
        pinEntity.setPinId(IDUtils.nextId());
        pinEntity.setUserId(userId);
        pinEntity.setTargetId(targetId);
        pinEntity.setTargetType(targetType);
        pinEntity.setPrevPinId(null);
        pinEntity.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
        pinEntity.setCreatorId(userId);
        pinEntity.setUpdatorId(userId);
        pinEntity.setTenantId(dto.getTenantId());
        pinEntity.setSpaceId(dto.getSpaceId());
        userHomepagePinMapper.insert(pinEntity);
        afterPinUpdated(userId, targetType, pinEntity.getPinId(), tenantId, spaceId);
        return ResultVO.success(pinEntity.getPinId());
      }
      finally {
        // 确保锁被释放
        try {
          lock.unlock();
        }
        catch (Exception e) {
          // 记录释放锁失败的警告，但不影响业务结果
          // logger.warn("释放首页置顶锁失败：userId={}, error={}", userId, e.getMessage());
        }
      }
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return ResultVO.fail("操作被中断");
    }
  }

  private void afterPinUpdated(Long userId, String targetType, Long pinId, Long tenantId, Long spaceId) {
    homepageMapper.updatePrevPinIdForNewHead(userId, targetType, pinId);
    PinLinkedSortHelper.enforcePinLimit(userId, targetType, tenantId, spaceId, homepageMapper);
  }

  /**
   * 取消置顶（链表删除）
   */
  @Override
  @Transactional
  public ResultVO<Void> unpinResource(UnpinResourceRequestDTO request, Long userId) {
    String targetId = request.getTargetId();
    String targetType = request.getTargetType();
    Long tenantId = request.getTenantId();
    Long spaceId = request.getSpaceId();
    Assert.notNull(tenantId, "租户ID不能为空");
    Assert.notNull(targetId, "目标资源ID不能为空");
    Assert.hasText(targetType, "目标类型不能为空");
    Assert.notNull(userId, "用户ID不能为空");

    // 使用分布式锁保护取消置顶操作，防止并发修改导致的数据不一致
    DistributedLock lock = distributedLockFactory.getBizLock(DocLockConsts.HOMEPAGE_PIN_REORDER_LOCK, userId.toString());

    try {
      // 尝试获取锁，最多等待5秒
      if (!lock.tryLock(5, TimeUnit.SECONDS)) {
        return ResultVO.fail("操作冲突，请稍后重试");
      }

      try {
        // 通过targetId和targetType查找置顶记录
        UserHomepagePinEntity pinEntity = homepageMapper.findPinRecord(userId, targetId, targetType, tenantId, spaceId);
        if (pinEntity == null || !DocBaseConsts.STATUS_CD_VALID.equals(pinEntity.getStatusCd())) {
          return ResultVO.fail("置顶记录不存在或无权限操作");
        }

        Long pinId = pinEntity.getPinId();
        Long currentPrevPinId = homepageMapper.getCurrentPrevPinId(pinId, userId);
        Long newPrevForTail = (currentPrevPinId != null && currentPrevPinId == 0L) ? null : currentPrevPinId;
        homepageMapper.updatePrevPinIdForMovedNode(pinId, newPrevForTail, userId);
        homepageMapper.deletePinRecord(pinId);

        return ResultVO.success();
      }
      finally {
        // 确保锁被释放
        try {
          lock.unlock();
        }
        catch (Exception e) {
          // 记录释放锁失败的警告，但不影响业务结果
          // logger.warn("释放首页取消置顶锁失败：userId={}, error={}", userId, e.getMessage());
        }
      }
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return ResultVO.fail("操作被中断");
    }
  }

  @Override
  @Transactional
  public ResultVO<String> reorderPins(ReorderPinsRequestDTO request, Long userId) {
    Assert.notNull(request, "请求参数不能为空");
    Assert.notNull(request.getPinId(), "被拖拽的置顶项ID不能为空");
    Assert.notNull(userId, "用户ID不能为空");

    // 使用分布式锁保护置顶项排序操作，防止并发修改导致的数据不一致
    DistributedLock lock = distributedLockFactory.getBizLock(DocLockConsts.HOMEPAGE_PIN_REORDER_LOCK, userId.toString());

    try {
      // 尝试获取锁，最多等待5秒
      if (!lock.tryLock(5, TimeUnit.SECONDS)) {
        return ResultVO.fail("操作冲突，请稍后重试");
      }

      try {
        return execute(request, userId);
      }
      finally {
        // 确保锁被释放
        try {
          lock.unlock();
        }
        catch (Exception e) {
          // 记录释放锁失败的警告，但不影响业务结果
          // logger.warn("释放首页置顶项排序锁失败：userId={}, error={}", userId, e.getMessage());
        }
      }
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return ResultVO.fail("操作被中断");
    }
  }

  private ResultVO<String> execute(ReorderPinsRequestDTO request, Long userId) {
    Long pinId = request.getPinId();
    Long prevPinId = request.getPrevPinId();
    Assert.isTrue(!pinId.equals(prevPinId), "被拖拽的置顶项不能是目标前驱项");
    List<UserHomepagePinEntity> pinRecords = homepageMapper.selectPinRecordsForReorder(userId, pinId, prevPinId);
    UserHomepagePinEntity currentPin = null;
    UserHomepagePinEntity targetPrevPin = null;
    UserHomepagePinEntity tailPin = null;
    for (UserHomepagePinEntity record : pinRecords) {
      if (record.getPinId().equals(pinId)) {
        currentPin = record;
      }
      if (record.getPinId().equals(prevPinId)) {
        targetPrevPin = record;
      }
      if (record.getPrevPinId() != null && record.getPrevPinId().equals(pinId)) {
        tailPin = record;
      }
    }
    if (currentPin == null) {
      return ResultVO.fail("置顶记录不存在或无权限操作");
    }
    if (prevPinId != null && targetPrevPin == null) {
      return ResultVO.fail("目标前驱项不存在或无权限操作");
    }
    Long currentPrevPinId = currentPin.getPrevPinId();
    if (tailPin != null) {
      homepageMapper.updatePrevPinIdForMovedNode(pinId, currentPrevPinId, userId);
    }
    homepageMapper.updatePrevPinIdForTargetPosition(prevPinId, pinId, userId);
    homepageMapper.updatePinOrder(pinId, prevPinId);
    return ResultVO.success(currentPin.getTargetType());
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public List<FrequentLibraryDTO> getFrequentLibraries(Long tenantId, Long spaceId, String platform) {
    if (DocBaseConsts.AI_PORTAL.equals(platform)) {
      tenantId = null;
    }
    Long currentUserId = SessionUtil.getLoginInfo().getUserId();
    List<FrequentLibraryDTO> frequentLibraries = homepageMapper.selectFrequentLibraries(currentUserId, DocBaseConsts.STATUS_CD_VALID, tenantId, spaceId);

    // 过滤掉用户没有权限的共享文档库
    frequentLibraries = frequentLibraries.stream()
      .filter(library -> {
        try {
          LibraryRoleEnum userRole = documentLibraryPermissionService.queryUserLibraryMaxRole(library.getLibraryId(), currentUserId);
          return userRole != null && (userRole.getLevel() <= LibraryRoleEnum.READ.getLevel());
        } catch (Exception e) {
          logger.warn("检查文档库权限失败: libraryId={}, libraryName={}, error={}", library.getLibraryId(), library.getLibraryName(), e.getMessage());
          // 如果权限检查失败，默认保留该文档库
          return true;
        }
      })
      .collect(Collectors.toList());

    if (DocBaseConsts.AI_PORTAL.equals(platform)) {
      // 确保"[我的文档]库"是常驻的
      FrequentLibraryDTO myDocumentsLibrary = getMyDocumentsLibrary(spaceId);
      if (myDocumentsLibrary != null) {
        // 如果查询结果中已经包含"[我的文档]库"，则移除它，避免重复
        frequentLibraries.removeIf(lib -> "我的文档".equals(lib.getLibraryName()));
        // 将"[我的文档]库"添加到列表开头
        frequentLibraries.add(0, myDocumentsLibrary);

        // 如果总数超过8个，则只保留前8个
        if (frequentLibraries.size() > 8) {
          frequentLibraries = frequentLibraries.subList(0, 8);
        }
      }
    }

    // 丰富常用文档库信息
    if (CollectionUtils.isNotEmpty(frequentLibraries)) {
      enrichFrequentLibraries(frequentLibraries);
    }

    return frequentLibraries;
  }

  @Override
  public PageInfo<RecentFileDTO> getRecentFiles(RecentFileQueryParams queryParams) {
    Assert.notNull(queryParams, "查询参数不能为空");
    Long userId = SessionUtil.getLoginInfo().getUserId();
    queryParams.setUserId(userId);
    // 最多查询50条记录，
    if (queryParams.getPageNum() > RECENT_FILE_MAX_PAGE_SIZE) {
      PageInfo<RecentFileDTO> pageInfo = new PageInfo<>();
      pageInfo.setPages(RECENT_FILE_MAX_PAGE_SIZE);
      pageInfo.setPageSize(RECENT_FILE_MAX_PAGE_SIZE);
      return pageInfo;
    }
    queryParams.setPageSize(10);

    PageInfo<RecentFileDTO> pageInfo = homepageMapper.selectRecentFiles(queryParams, queryParams.buildRowBounds())
      .toPageInfo();

    // 丰富最近访问文件信息
    if (CollectionUtils.isNotEmpty(pageInfo.getList())) {
      enrichRecentFiles(pageInfo.getList());
    }
    if (pageInfo.getTotal() > RECENT_FILE_MAX_COUNT) {
      pageInfo.setTotal(RECENT_FILE_MAX_COUNT);
      pageInfo.setPages(RECENT_FILE_MAX_PAGE_SIZE);
      pageInfo.setNavigateLastPage(RECENT_FILE_MAX_PAGE_SIZE);
    }

    return pageInfo;
  }

  @Override
  @Transactional
  public ResultVO<Void> removeFromRecentFiles(String documentId, Boolean deleteSourceAfter) {
    Assert.notNull(documentId, "文档ID不能为空");
    Assert.notNull(deleteSourceAfter, "删除源文件标志不能为空");
    Long userId = SessionUtil.getLoginInfo().getUserId();

    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    Assert.notNull(documentDTO, "文档不存在");
    // 先删除最近访问记录
    if (homepageMapper.deleteRecentFileRecord(userId, documentId) > 0) {
      // 如果需要删除源文件，则获取文档信息并删除源文件
      if (deleteSourceAfter) {
        documentNodeService.deleteDocumentNode(documentDTO.getLibraryId(), userId, documentId);
      }
      return ResultVO.success();
    }
    return ResultVO.fail("记录不存在或无权限操作");
  }

  @Override
  public PageInfo<SearchDocumentDTO> searchDocuments(SearchDocumentQueryParams queryParams) {
    Assert.notNull(queryParams, "搜索参数不能为空");
    Long currentUserId = SessionUtil.getLoginInfo().getUserId();
    queryParams.setUserId(currentUserId);
    List<SearchDocumentDTO> allDocuments;
    if (DocBaseConsts.AI_PORTAL.equals(queryParams.getPlatform())) {
      queryParams.setTenantId(null);
    }
    // 如果关键词为空，查询最近操作的文档
    if (queryParams.getKeyword() == null || queryParams.getKeyword().trim().isEmpty()) {
      allDocuments = getRecentOperationDocuments(currentUserId, queryParams.getType(), queryParams.getLibraryId(), queryParams.getSpaceId(), queryParams.getTenantId());
    }
    else {
      // 查询所有匹配的文档（不分页）
      allDocuments = homepageMapper.searchDocumentsWithoutPaging(queryParams);
    }

    if (CollectionUtils.isEmpty(allDocuments)) {
      return new PageInfo<>(new ArrayList<>());
    }
    // 按文档库分组，批量检查权限
    List<SearchDocumentDTO> authorizedDocuments = filterDocumentsByPermission(allDocuments, currentUserId);
    // 对过滤后的结果进行分页
    return paginateResults(authorizedDocuments, queryParams.getPageNum(), queryParams.getPageSize());
  }

  /**
   * 获取最近操作的文档（访问或编辑）
   *
   * @param userId 用户ID
   * @param type 文档类型（可选）
   * @param libraryId 文档库ID（可选）
   * @return 最近操作的文档列表
   */
  private List<SearchDocumentDTO> getRecentOperationDocuments(Long userId, String type, String libraryId, Long spaceId, Long tenantId) {
    return homepageMapper.selectRecentOperationDocuments(userId, type, libraryId, spaceId, tenantId);
  }

  /**
   * 根据权限过滤文档列表
   *
   * @param documents 文档列表
   * @param userId 用户ID
   * @return 有权限的文档列表
   */
  private List<SearchDocumentDTO> filterDocumentsByPermission(List<SearchDocumentDTO> documents, Long userId) {
    if (CollectionUtils.isEmpty(documents)) {
      return new ArrayList<>();
    }
    // 在主线程中捕获租户ID，避免在异步任务中丢失租户上下文
    Long tenantId = TenantContextHolder.getTenantId();
    Map<String, List<SearchDocumentDTO>> libraryGroupMap = documents.stream().collect(Collectors.groupingBy(SearchDocumentDTO::getLibraryId));
    // 使用并行处理提升权限检查效率
    List<CompletableFuture<List<SearchDocumentDTO>>> futures = libraryGroupMap.entrySet().stream()
        .map(entry -> CompletableFuture.supplyAsync(() -> ThreadContextRunner.runWithTenant(tenantId, () -> {
          String libraryId = entry.getKey();
          List<SearchDocumentDTO> libraryDocuments = entry.getValue();
          List<String> nodeIds = libraryDocuments.stream().map(SearchDocumentDTO::getDocumentId).collect(Collectors.toList());

          // 批量查询权限
          Map<String, ControlRole> roleDict = controlTemplate.fetchNodeRole(libraryId, userId, nodeIds);
          List<SearchDocumentDTO> authorizedDocs = new ArrayList<>();
          for (SearchDocumentDTO document : libraryDocuments) {
            ControlRole controlRole = roleDict.get(document.getDocumentId());
            if (controlRole != null && controlRole.hasPermission(NodePermission.READ_NODE)) {
              authorizedDocs.add(document);
            }
          }
          return authorizedDocs;
        }), ThreadPools.getCommon()))
        .toList();
    // 等待所有权限检查完成并合并结果
    List<SearchDocumentDTO> authorizedDocuments = new ArrayList<>();
    for (CompletableFuture<List<SearchDocumentDTO>> future : futures) {
      try {
        List<SearchDocumentDTO> result = future.get();
        if (result != null) {
          authorizedDocuments.addAll(result);
        }
      } catch (Exception e) {
        logger.warn("权限检查失败: userId={}, error={}", userId, e.getMessage(), e);
      }
    }

    return authorizedDocuments;
  }

  /**
   * 丰富文档信息
   * 生成预览地址和补充用户信息
   *
   * @param documents 文档列表
   */
  private void enrichDocumentInfo(List<SearchDocumentDTO> documents) {
    if (CollectionUtils.isEmpty(documents)) {
      return;
    }
    // 查询最近活动记录
    Map<String, DocumentActivityDTO> activityMap = queryDocumentActivityInfo(documents);
    // 批量查询用户信息
    Map<Long, PortalUserDTO> userMap = batchQueryUserInfo(documents, activityMap);
    for (SearchDocumentDTO document : documents) {
      // 补充创建人信息
      enrichCreatorInfo(document, userMap);
      // 补充最近活动信息
      enrichActivityInfo(document, activityMap, userMap);
    }
  }

  /**
   * 查询文档活动记录信息
   *
   * @param documents 文档列表
   * @return 活动信息映射
   */
  private Map<String, DocumentActivityDTO> queryDocumentActivityInfo(List<SearchDocumentDTO> documents) {
    Map<String, DocumentActivityDTO> activityMap = new HashMap<>();

    if (CollectionUtils.isEmpty(documents)) {
      return activityMap;
    }

    // 收集文档ID
    List<String> documentIds = documents.stream()
        .map(SearchDocumentDTO::getDocumentId)
        .collect(Collectors.toList());

    try {
      // 查询最近浏览记录
      List<DocumentActivityDTO> viewActivities = homepageMapper.selectLastViewActivities(documentIds);
      for (DocumentActivityDTO activity : viewActivities) {
        activityMap.put(activity.getDocumentId() + "_VIEW", activity);
      }

      // 查询最近编辑记录
      List<DocumentActivityDTO> editActivities = homepageMapper.selectLastEditActivities(documentIds);
      for (DocumentActivityDTO activity : editActivities) {
        activityMap.put(activity.getDocumentId() + "_EDIT", activity);
      }
    } catch (Exception e) {
      logger.warn("查询文档活动记录失败: documentIds={}, error={}", documentIds, e.getMessage());
    }

    return activityMap;
  }

  /**
   * 批量查询用户信息
   *
   * @param documents 文档列表
   * @param activityMap 活动信息映射
   * @return 用户信息映射
   */
  private Map<Long, PortalUserDTO> batchQueryUserInfo(List<SearchDocumentDTO> documents, Map<String, DocumentActivityDTO> activityMap) {
    Map<Long, PortalUserDTO> userMap = new HashMap<>();

    // 收集所有需要查询的用户ID
    Set<Long> userIds = new HashSet<>();
    for (SearchDocumentDTO document : documents) {
      if (document.getCreatorId() != null) {
        userIds.add(document.getCreatorId());
      }
    }

    // 从活动记录中收集用户ID
    for (DocumentActivityDTO activity : activityMap.values()) {
      if (activity.getUserId() != null) {
        userIds.add(activity.getUserId());
      }
    }

    // 批量查询用户信息
    if (!userIds.isEmpty()) {
      try {
        userMap = dcUserService.findUserMapBatchByIds(new ArrayList<>(userIds));
      } catch (Exception e) {
        logger.warn("批量查询用户信息失败: userIds={}, error={}", userIds, e.getMessage());
      }
    }

    return userMap;
  }

  /**
   * 补充创建人信息
   *
   * @param document 文档信息
   * @param userMap 用户信息映射
   */
  private void enrichCreatorInfo(SearchDocumentDTO document, Map<Long, PortalUserDTO> userMap) {
    if (document.getCreatorId() != null) {
      PortalUserDTO creator = userMap.get(document.getCreatorId());
      if (creator != null) {
        UserInfo creatorInfo = new UserInfo();
        creatorInfo.setUserId(creator.getUserId());
        creatorInfo.setUsername(creator.getUserName());
        document.setCreator(creatorInfo);
      }
    }
  }

  /**
   * 补充活动信息
   *
   * @param document 文档信息
   * @param activityMap 活动信息映射
   * @param userMap 用户信息映射
   */
  private void enrichActivityInfo(SearchDocumentDTO document, Map<String, DocumentActivityDTO> activityMap, Map<Long, PortalUserDTO> userMap) {
    String documentId = document.getDocumentId();
    // 补充最近浏览信息
    DocumentActivityDTO viewActivity = activityMap.get(documentId + "_VIEW");
    if (viewActivity != null) {
      PortalUserDTO lastViewUser = userMap.get(viewActivity.getUserId());
      if (lastViewUser != null) {
        viewActivity.setUsername(lastViewUser.getUserName());
        document.setViewActivity(viewActivity);
      }
    }
    // 补充最近编辑信息
    DocumentActivityDTO editActivity = activityMap.get(documentId + "_EDIT");
    if (editActivity != null) {
      PortalUserDTO lastEditUser = userMap.get(editActivity.getUserId());
      if (lastEditUser != null) {
        editActivity.setUsername(lastEditUser.getUserName());
        document.setEditActivity(editActivity);
      }
    }
  }

  /**
   * 对结果进行分页
   *
   * @param documents 文档列表
   * @param pageNum 页码
   * @param pageSize 每页大小
   * @return 分页结果
   */
  private PageInfo<SearchDocumentDTO> paginateResults(List<SearchDocumentDTO> documents, Integer pageNum, Integer pageSize) {
    int total = documents.size();
    int num = pageNum == null ? 1 : pageNum;
    int size = pageSize == null ? 20 : pageSize;

    int startIndex = (num - 1) * size;
    int endIndex = Math.min(startIndex + size, total);

    List<SearchDocumentDTO> pageData = new ArrayList<>();
    if (startIndex < total) {
      pageData = documents.subList(startIndex, endIndex);
    }
    // 生成预览地址和补充用户信息
    enrichDocumentInfo(pageData);
    PageInfo<SearchDocumentDTO> pageInfo = new PageInfo<>(pageData);
    pageInfo.setTotal(total);
    pageInfo.setPageNum(num);
    pageInfo.setPageSize(size);
    pageInfo.setPages((total + size - 1) / size);
    pageInfo.setHasNextPage(num < pageInfo.getPages());
    pageInfo.setHasPreviousPage(num > 1);

    return pageInfo;
  }

  /**
   * 为置顶文档补充创建人和路径信息
   * <p>从缓存或数据库获取的置顶文档列表不包含展示所需的创建人和完整路径，本方法根据现有字段补充这些信息，
   * 以保证前端展示一致性。</p>
   *
   * @param pinnedDocuments 置顶文档列表
   */
  private void enrichPinnedDocuments(List<PinnedDocumentDTO> pinnedDocuments) {
    List<String> documentIds = pinnedDocuments.stream().map(PinnedDocumentDTO::getDocumentId).toList();
    Map<String, DocumentPathDTO> documentPathBatch = documentService.getDocumentPathBatch(documentIds);
    for (PinnedDocumentDTO pinnedDocument : pinnedDocuments) {
      // 通过PortalUserIntegration获取创建人信息
      if (pinnedDocument.getCreatorUserId() != null) {
        PortalUserDTO creatorUser = dcUserService.findUserById(pinnedDocument.getCreatorUserId());
        if (creatorUser != null) {
          UserInfo creator = new UserInfo();
          creator.setUserId(creatorUser.getUserId());
          creator.setUsername(creatorUser.getUserName());
          pinnedDocument.setCreator(creator);
        }
      }

      // 获取文档路径信息
      DocumentPathDTO documentPath = documentPathBatch.get(pinnedDocument.getDocumentId());
      if (documentPath != null) {
        pinnedDocument.setLibraryPath(documentPath.getDocumentPath());
        pinnedDocument.setLibraryPathCode(documentPath.getDocumentPathCode());
      }
    }
  }

  /**
   * 为置顶文档库补充最后更新人信息
   *
   * @param pinnedLibraries 置顶文档库列表
   */
  private void enrichPinnedLibraries(List<PinnedLibraryDTO> pinnedLibraries) {
    for (PinnedLibraryDTO pinnedLibrary : pinnedLibraries) {
      // 通过PortalUserIntegration获取最后更新人信息
      if (pinnedLibrary.getLastUpdaterUserId() != null) {
        PortalUserDTO lastUpdaterUser = dcUserService.findUserById(pinnedLibrary.getLastUpdaterUserId());
        if (lastUpdaterUser != null) {
          UserInfo lastUpdater = new UserInfo();
          lastUpdater.setUserId(lastUpdaterUser.getUserId());
          lastUpdater.setUsername(lastUpdaterUser.getUserName());
          pinnedLibrary.setLastUpdater(lastUpdater);
        }
      }
    }
  }

  /**
   * 丰富最近访问文件信息
   *
   * @param recentFiles 最近访问文件列表
   */
  private void enrichRecentFiles(List<RecentFileDTO> recentFiles) {
    List<String> documentIds = recentFiles.stream().map(RecentFileDTO::getDocumentId).toList();
    List<Long> userIds = recentFiles.stream().map(RecentFileDTO::getCreatorUserId).toList();
    Map<String, DocumentPathDTO> documentPathBatch = documentService.getDocumentPathBatch(documentIds);
    Map<Long, PortalUserDTO> userMaps = dcUserService.findUserMapBatchByIds(userIds);
    for (RecentFileDTO recentFile : recentFiles) {
      // 通过PortalUserIntegration获取创建人信息
      PortalUserDTO creatorUser = userMaps.get(recentFile.getCreatorUserId());
      if (creatorUser != null) {
        UserInfo creator = new UserInfo();
        creator.setUserId(creatorUser.getUserId());
        creator.setUsername(creatorUser.getUserName());
        recentFile.setCreator(creator);
      }

      DocumentPathDTO documentPath = documentPathBatch.get(recentFile.getDocumentId());
      if (documentPath != null) {
        recentFile.setLibraryPath(documentPath.getDocumentPath());
        recentFile.setLibraryPathCode(documentPath.getDocumentPathCode());
      }
    }

    // 批量设置权限信息
    Long userId = SessionUtil.getLoginInfo().getUserId();
    setDocumentPermissions(recentFiles, userId);
  }

  /**
   * 批量设置文档权限信息
   *
   * @param recentFiles 最近访问文件列表
   * @param userId 用户ID
   */
  private void setDocumentPermissions(List<RecentFileDTO> recentFiles, Long userId) {
    if (CollectionUtils.isEmpty(recentFiles)) {
      return;
    }
    try {
      // 按文档库分组文档
      Map<String, List<RecentFileDTO>> libraryGroupMap = recentFiles.stream()
          .collect(Collectors.groupingBy(RecentFileDTO::getLibraryId));
      // 批量获取权限
      Map<String, String> documentPermissions = new HashMap<>();
      for (Map.Entry<String, List<RecentFileDTO>> entry : libraryGroupMap.entrySet()) {
        String libraryId = entry.getKey();
        List<RecentFileDTO> libraryFiles = entry.getValue();
        List<String> nodeIds = libraryFiles.stream()
            .map(RecentFileDTO::getDocumentId)
            .collect(Collectors.toList());
        // 批量查询节点权限
        Map<String, ControlRole> roleDict = controlTemplate.fetchNodeRole(libraryId, userId, nodeIds);
        for (String nodeId : nodeIds) {
          ControlRole controlRole = roleDict.get(nodeId);
          if (controlRole != null) {
            documentPermissions.put(nodeId, controlRole.getRoleTag());
          }
        }
      }
      // 设置权限到各个文件
      for (RecentFileDTO recentFile : recentFiles) {
        String permission = documentPermissions.get(recentFile.getDocumentId());
        recentFile.setPermissions(permission);
      }
    }
    catch (Exception e) {
      logger.warn("批量检查文档权限失败: userId={}, error={}", userId, e.getMessage(), e);
      // 如果获取权限失败，设置为默认权限
      for (RecentFileDTO recentFile : recentFiles) {
        recentFile.setPermissions(null);
      }
    }
  }

  /**
   * 丰富常用文档库信息
   *
   * @param frequentLibraries 常用文档库列表
   */
  private void enrichFrequentLibraries(List<FrequentLibraryDTO> frequentLibraries) {
    for (FrequentLibraryDTO frequentLibrary : frequentLibraries) {
      if (frequentLibrary.getLastUpdateTime() == null) {
        frequentLibrary.setLastUpdateTime(frequentLibrary.getCreatedTime());
      }
      // 通过PortalUserIntegration获取最后更新人信息
      if (frequentLibrary.getLastUpdaterUserId() != null) {
        PortalUserDTO lastUpdaterUser = dcUserService.findUserById(frequentLibrary.getLastUpdaterUserId());
        if (lastUpdaterUser != null) {
          UserInfo lastUpdater = new UserInfo();
          lastUpdater.setUserId(lastUpdaterUser.getUserId());
          lastUpdater.setUsername(lastUpdaterUser.getUserName());
          frequentLibrary.setLastUpdater(lastUpdater);
        }
      }
    }
  }

  /**
   * 获取"[我的文档]库"信息
   *
   * @return "[我的文档]库"信息，如果不存在则返回null
   */
  private FrequentLibraryDTO getMyDocumentsLibrary(Long spaceId) {
    // 这里需要根据实际情况查询"[我的文档]库"
    // 假设"[我的文档]库"的library_name为"我的文档"
    FrequentLibraryDTO myDocumentsLibrary = homepageMapper.selectMyDocumentsLibrary(
      SessionUtil.getLoginInfo().getUserId(), DocBaseConsts.STATUS_CD_VALID, spaceId);
    if (myDocumentsLibrary != null) {
      myDocumentsLibrary.setMyDoc(DocBaseConsts.TRUE);
      if (myDocumentsLibrary.getLastUpdateTime() == null) {
        myDocumentsLibrary.setLastUpdateTime(myDocumentsLibrary.getCreatedTime());
      }
    }
    return myDocumentsLibrary;
  }

  /**
   * 租户维度删除置顶数据 参照unpinResource方法实现链表操作
   */
  @Override
  @Transactional
  public ResultVO<Void> unpinResourceByTenant(Long tenantId, String targetId, String targetType, Long spaceId) {
    Assert.notNull(tenantId, "租户ID不能为空");
    Assert.notNull(targetId, "目标资源ID不能为空");
    Assert.hasText(targetType, "目标类型不能为空");

    // 验证是否存在有效的置顶记录
    List<UserHomepagePinEntity> pinEntities = homepageMapper.selectActivePinsByTenantAndResource(tenantId, targetId,
      targetType, spaceId);
    if (CollectionUtils.isEmpty(pinEntities)) {
      return ResultVO.fail("该租户下不存在有效的置顶记录");
    }
    homepageMapper.updatePrevPinIdsForTenantUnpin(tenantId, targetId, targetType);
    homepageMapper.updatePinStatusToInvalidByTenant(tenantId, targetId, targetType);

    return ResultVO.success();
  }
}
