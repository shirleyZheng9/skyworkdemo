package com.iwhalecloud.bote.doc.module.person.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.consts.SubjectTypeEnum;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.UserInfo;
import com.iwhalecloud.bote.doc.module.person.dto.share.MyShareDTO;
import com.iwhalecloud.bote.doc.module.person.dto.share.ShareTargetResult;
import com.iwhalecloud.bote.doc.module.person.dto.share.SharedTargetDTO;
import com.iwhalecloud.bote.doc.module.person.dto.share.SharedWithMeDTO;
import com.iwhalecloud.bote.doc.module.person.dto.share.SharedWithMeDataDTO;
import com.iwhalecloud.bote.doc.module.person.mapper.ShareMapper;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPathDTO;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.ControlRoleDict;
import com.iwhalecloud.bote.doc.module.user.service.IDcOrgService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.person.service.IShareService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;

/**
 * 共享模块 Service 实现
 * <p>实现“与我共享”和“我共享的”的分页查询与数据装配。</p>
 *
 * @author lizuyin
 * @since 2025-08-20
 */
@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements IShareService {
  private final ShareMapper shareMapper;

  private final IDocumentService documentService;

  private final IDcUserService dcUserService;

  private final IDcOrgService dcOrgService;

  private final ControlTemplate controlTemplate;

  @Override
  public PageInfo<SharedWithMeDTO> getSharedWithMe(Integer pageNum, Integer pageSize, String fileType, String sortBy,
    String sortOrder, Long tenantId, Long spaceId, String platform) {
    Long userId = SessionUtil.getLoginInfo().getUserId();

    // 数据库查询
    Page<SharedWithMeDTO> page = getSharedWithMePage(userId, pageNum, pageSize, fileType, sortBy, sortOrder, tenantId, spaceId, platform);

    // 权限处理
    SharedWithMeDataDTO processedData = processSharedWithMeData(page, userId);

    // 结果组装
    return buildSharedWithMeResult(page, processedData);
  }

  /**
   * 第一层：数据库查询层
   * 执行数据库查询，获取共享给我的文档分页数据
   */
  private Page<SharedWithMeDTO> getSharedWithMePage(Long userId, Integer pageNum, Integer pageSize, String fileType,
    String sortBy, String sortOrder, Long tenantId, Long spaceId, String platform) {
    RowBounds rowBounds = buildRowBounds(pageNum, pageSize);
    return shareMapper.selectSharedWithMe(userId, fileType, sortBy, sortOrder, rowBounds,
      spaceId, tenantId, platform);
  }

  /**
   * 第二层：数据处理层
   * 处理文档权限和分享者信息
   */
  private SharedWithMeDataDTO processSharedWithMeData(Page<SharedWithMeDTO> page, Long userId) {
    SharedWithMeDataDTO data = new SharedWithMeDataDTO();

    // 收集所有文档ID，用于批量获取权限
    List<String> documentIds = page.stream().map(SharedWithMeDTO::getDocumentId).collect(Collectors.toList());
    data.setDocumentIds(documentIds);

    // 批量获取用户对这些文档的权限
    Map<String, String> documentPermissions = new HashMap<>();
    if (!documentIds.isEmpty()) {
      // 获取文档对应的文档库ID
      Map<String, String> nodeToLibraryMap = documentService.findBatchByDocumentId(documentIds).stream().collect(
        Collectors.toMap(DcDocumentDTO::getDocumentId, DcDocumentDTO::getLibraryId,
          (existing, replacement) -> existing));

      // 为每个文档库批量获取权限
      Map<String, List<String>> libraryToNodesMap = nodeToLibraryMap.entrySet().stream().collect(
        Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));

      for (Map.Entry<String, List<String>> entry : libraryToNodesMap.entrySet()) {
        String libraryId = entry.getKey();
        List<String> nodesInLibrary = entry.getValue();

        ControlRoleDict roleDict = controlTemplate.fetchNodeRole(libraryId, userId, nodesInLibrary);
        for (String nodeId : nodesInLibrary) {
          if (roleDict.containsKey(nodeId)) {
            documentPermissions.put(nodeId, roleDict.get(nodeId).getRoleTag());
          }
        }
      }
    }
    data.setDocumentPermissions(documentPermissions);

    // 获取分享者用户信息
    Set<Long> sharerUserIds = new HashSet<>();
    for (SharedWithMeDTO item : page) {
      if (item.getSharerUserId() != null) {
        sharerUserIds.add(item.getSharerUserId());
      }
    }

    Map<Long, PortalUserDTO> sharerUserMap = new HashMap<>();
    if (!sharerUserIds.isEmpty()) {
      sharerUserMap = dcUserService.findUserMapBatchByIds(new ArrayList<>(sharerUserIds));
    }
    data.setSharerUserMap(sharerUserMap);

    return data;
  }

  /**
   * 第三层：结果组装层
   * 组装最终的 SharedWithMeDTO 列表和分页信息
   */
  private PageInfo<SharedWithMeDTO> buildSharedWithMeResult(Page<SharedWithMeDTO> page, SharedWithMeDataDTO processedData) {
    List<SharedWithMeDTO> resultList = new ArrayList<>();
    for (SharedWithMeDTO item : page) {
      fillLibraryPath(item.getDocumentId(), item);

      // 设置用户对该文档的实际权限
      item.setPermissions(processedData.getDocumentPermissions().get(item.getDocumentId()));

      UserInfo sharer = new UserInfo();
      Long sharerUserId = item.getSharerUserId();
      if (sharerUserId != null) {
        sharer.setUserId(sharerUserId);
        PortalUserDTO sharerUser = processedData.getSharerUserMap().get(sharerUserId);
        sharer.setUsername(sharerUser != null ? sharerUser.getUserName() : null);
      }
      item.setSharer(sharer);
      resultList.add(item);
    }

    PageInfo<SharedWithMeDTO> pageInfo = new PageInfo<>(resultList);
    pageInfo.setPageNum(page.getPageNum());
    pageInfo.setPageSize(page.getPageSize());
    pageInfo.setTotal(page.getTotal());
    pageInfo.setPages(page.getPages());
    pageInfo.setList(resultList);
    return pageInfo;
  }

  @Override
  public PageInfo<MyShareDTO> getMyShares(Integer pageNum, Integer pageSize, String fileType, String sortBy,
    String sortOrder, Long tenantId, Long spaceId, String platform) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    RowBounds rowBounds = buildRowBounds(pageNum, pageSize);

    Page<MyShareDTO> pageDocuments = shareMapper.selectMyShareDocuments(userId, fileType, sortBy, sortOrder, rowBounds,
      spaceId, tenantId, platform);

    if (pageDocuments.isEmpty()) {
      return buildPageInfo(pageDocuments, new ArrayList<>());
    }

    List<String> documentIds = pageDocuments.stream().map(MyShareDTO::getDocumentId).collect(Collectors.toList());

    List<MyShareDTO> allPermissions = shareMapper.selectPermissionsByDocumentIds(documentIds, userId);

    List<MyShareDTO> resultList = processShareDocuments(pageDocuments, allPermissions);

    return buildPageInfo(pageDocuments, resultList);
  }

  /**
   * 为用户类型的分享目标创建SharedTargetDTO对象
   *
   * @param perm 权限记录
   * @param userMap 用户信息映射
   * @return SharedTargetDTO对象，如果用户不存在则返回null
   */
  private SharedTargetDTO createSharedTargetForUser(MyShareDTO perm, Map<Long, PortalUserDTO> userMap) {
    PortalUserDTO user = userMap.get(perm.getSubjectId());
    if (user == null) {
      return null;
    }

    SharedTargetDTO target = new SharedTargetDTO();
    target.setItemId(perm.getSubjectId());
    target.setPermission(perm.getPermissionType());
    target.setType(perm.getSubjectType());
    target.setItemName(user.getUserName());

    if (user.getOrgList() != null && !user.getOrgList().isEmpty()) {
      Optional<OrgDTO> maxLevelOrg = user.getOrgList().stream()
        .max(Comparator.comparingInt(OrgDTO::getOrgLevel));
      maxLevelOrg.ifPresent(org -> target.setItemOrg(org.getOrgName()));
    }

    return target;
  }

  /**
   * 为组织类型的分享目标创建SharedTargetDTO对象
   *
   * @param perm 权限记录
   * @param orgMap 组织信息映射
   * @return SharedTargetDTO对象，如果组织不存在则返回null
   */
  private SharedTargetDTO createSharedTargetForOrg(MyShareDTO perm, Map<Long, OrgDTO> orgMap) {
    OrgDTO org = orgMap.get(perm.getSubjectId());
    if (org == null) {
      return null;
    }

    SharedTargetDTO target = new SharedTargetDTO();
    target.setItemId(perm.getSubjectId());
    target.setPermission(perm.getPermissionType());
    target.setType(perm.getSubjectType());
    target.setItemName(org.getOrgName());

    return target;
  }

  /**
   * 更新最新分享时间
   *
   * @param currentLatest 当前的最新分享时间
   * @param newTime 新的分享时间
   * @return 更新后的最新分享时间
   */
  private Date updateLatestShareTime(Date currentLatest, Date newTime) {
    if (newTime == null) {
      return currentLatest;
    }
    if (currentLatest == null || newTime.after(currentLatest)) {
      return newTime;
    }
    return currentLatest;
  }

  /**
   * 从权限记录列表构建共享目标列表 处理去重、填充用户/组织信息
   *
   * @param permissions 权限记录列表
   * @param userMap 预查询的用户信息映射
   * @param orgMap 预查询的组织信息映射
   * @return 处理后的共享目标列表和最新分享时间
   */
  private ShareTargetResult buildSharedTargetList(List<MyShareDTO> permissions, Map<Long, PortalUserDTO> userMap,
    Map<Long, OrgDTO> orgMap) {
    Map<String, SharedTargetDTO> targetMap = new LinkedHashMap<>();
    Date latestShareTime = null;

    for (MyShareDTO perm : permissions) {
      String key = perm.getSubjectType() + ":" + perm.getSubjectId();

      if (!targetMap.containsKey(key)) {
        SharedTargetDTO target = createSharedTarget(perm, userMap, orgMap);
        if (target != null) {
          targetMap.put(key, target);
          latestShareTime = updateLatestShareTime(latestShareTime, perm.getCreatedTime());
        }
      }
    }

    return new ShareTargetResult(new ArrayList<>(targetMap.values()), latestShareTime);
  }

  /**
   * 根据权限记录的类型创建对应的分享目标对象
   *
   * @param perm 权限记录
   * @param userMap 用户信息映射
   * @param orgMap 组织信息映射
   * @return SharedTargetDTO对象，如果创建失败则返回null
   */
  private SharedTargetDTO createSharedTarget(MyShareDTO perm, Map<Long, PortalUserDTO> userMap,
    Map<Long, OrgDTO> orgMap) {
    if (SubjectTypeEnum.USER.getCode().equals(perm.getSubjectType())) {
      return createSharedTargetForUser(perm, userMap);
    }
    else if (SubjectTypeEnum.ORG.getCode().equals(perm.getSubjectType())) {
      return createSharedTargetForOrg(perm, orgMap);
    }
    return null;
  }

  private RowBounds buildRowBounds(Integer pageNum, Integer pageSize) {
    int num = pageNum;
    int size = pageSize;
    return new RowBounds((num - 1) * size, size);
  }

  private void fillLibraryPath(String documentId, Object target) {
    DocumentPathDTO pathDTO = documentService.getDocumentPath(documentId);
    if (target instanceof SharedWithMeDTO dto) {
      if (pathDTO != null) {
        dto.setLibraryPath(pathDTO.getDocumentPath());
        dto.setLibraryPathCode(pathDTO.getDocumentPathCode());
      }
    }
    else if (target instanceof MyShareDTO dto) {
      if (pathDTO != null) {
        dto.setLibraryPath(pathDTO.getDocumentPath());
        dto.setLibraryPathCode(pathDTO.getDocumentPathCode());
      }
    }
  }

  /**
   * 构建分页信息对象
   *
   * @param page 原始分页数据
   * @param resultList 处理后的结果列表
   * @return 分页信息对象
   */
  private PageInfo<MyShareDTO> buildPageInfo(Page<MyShareDTO> page, List<MyShareDTO> resultList) {
    PageInfo<MyShareDTO> pageInfo = new PageInfo<>(resultList);
    pageInfo.setPageNum(page.getPageNum());
    pageInfo.setPageSize(page.getPageSize());
    pageInfo.setTotal(page.getTotal());
    pageInfo.setPages(page.getPages());
    pageInfo.setList(resultList);
    return pageInfo;
  }

  /**
   * 处理分享文档数据，构建分享目标列表
   *
   * @param pageDocuments 分页文档列表
   * @param allPermissions 所有权限记录
   * @return 处理后的文档列表
   */
  private List<MyShareDTO> processShareDocuments(List<MyShareDTO> pageDocuments, List<MyShareDTO> allPermissions) {
    // 收集所有需要查询的用户ID和组织ID
    Set<Long> userIds = new HashSet<>();
    Set<Long> orgIds = new HashSet<>();
    for (MyShareDTO perm : allPermissions) {
      if (SubjectTypeEnum.USER.getCode().equals(perm.getSubjectType())) {
        userIds.add(perm.getSubjectId());
      }
      else if (SubjectTypeEnum.ORG.getCode().equals(perm.getSubjectType())) {
        orgIds.add(perm.getSubjectId());
      }
    }

    // 批量查询用户和组织信息
    Map<Long, PortalUserDTO> userMap = new HashMap<>();
    Map<Long, OrgDTO> orgMap = new HashMap<>();

    if (!userIds.isEmpty()) {
      userMap = dcUserService.findUserMapBatchByIds(new ArrayList<>(userIds));
    }
    if (!orgIds.isEmpty()) {
      orgMap = dcOrgService.findBatchById(new ArrayList<>(orgIds)).stream()
        .collect(Collectors.toMap(OrgDTO::getOrgId, org -> org, (existing, replacement) -> existing));
    }

    Map<String, List<MyShareDTO>> permissionsByDocument = allPermissions.stream()
      .collect(Collectors.groupingBy(MyShareDTO::getDocumentId));

    List<MyShareDTO> resultList = new ArrayList<>();
    for (MyShareDTO doc : pageDocuments) {
      fillLibraryPath(doc.getDocumentId(), doc);

      List<MyShareDTO> docPermissions = permissionsByDocument.getOrDefault(doc.getDocumentId(), new ArrayList<>());

      ShareTargetResult shareTargetResult = buildSharedTargetList(docPermissions, userMap, orgMap);
      List<SharedTargetDTO> sharedToList = shareTargetResult.getSharedTargets();

      if (sharedToList.isEmpty()) {
        continue;
      }

      doc.setSharedTo(sharedToList);

      Date latestValidShareTime = shareTargetResult.getLatestShareTime();
      if (latestValidShareTime != null) {
        doc.setShareTime(latestValidShareTime);
      }

      resultList.add(doc);
    }

    return resultList;
  }

}


