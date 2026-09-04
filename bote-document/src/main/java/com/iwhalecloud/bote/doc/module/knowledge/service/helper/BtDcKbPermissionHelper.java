package com.iwhalecloud.bote.doc.module.knowledge.service.helper;

import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.space.SpaceContextHolder;
import com.iwhalecloud.bote.doc.common.tenant.ThreadContextRunner;
import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.LibraryRoleEnum;
import com.iwhalecloud.bote.doc.consts.SubjectTypeEnum;
import com.iwhalecloud.bote.doc.consts.VisibilityScopeEnum;
import com.iwhalecloud.bote.doc.integration.PortalOrgIntegration;
import com.iwhalecloud.bote.doc.module.control.base.ControlRoleDict;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRole;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcKbPermissionDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.SimpleKnowledgeBaseDTO;
import com.iwhalecloud.bote.doc.module.knowledge.entity.BtDcKbPermissionEntity;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.BtDcKbPermissionManageMapper;
import com.iwhalecloud.bote.dto.knowledge.SimpleDocumentDTO;
import com.iwhalecloud.bote.dto.knowledge.SimpleKnowledgeDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@RequiredArgsConstructor
public class BtDcKbPermissionHelper {
  private final BtDcKbPermissionManageMapper btDcKbPermissionManageMapper;
  private final PortalOrgIntegration portalOrgIntegration;
  private final ControlTemplate controlTemplate;


  public Pair<List<String>, List<String>> getTopicAndDocIds(List<SimpleKnowledgeDTO> knowledgeList, @Nullable List<Long> documentIds, Long tenantId) {
    List<SimpleKnowledgeDTO> knowledges = new ArrayList<>(knowledgeList.size());
    List<SimpleDocumentDTO> documents = new ArrayList<>();
    Pair<List<String>, List<String>> listListPair = checkKnowledgeType(knowledgeList);
    if (listListPair != null) {
      return listListPair;
    }
    boolean isAdmin = SessionUtil.isSuperAdmin(SessionUtil.getLoginInfo().getUserId());
    int total = 0;
    for (SimpleKnowledgeDTO knowledge : knowledgeList) {
      Assert.hasLength(knowledge.getTopicId(), () -> "知识库未关联 DocChain 主题: knowledgeId=" + knowledge.getKnowledgeId());
      List<SimpleDocumentDTO> list = CollectionUtils.emptyIfNull(knowledge.getDocuments()).stream().filter(p -> p.getExtSystemId() != null)
        .toList();
      total = total + list.size();
      if (isAdmin) {
        knowledges.add(knowledge);
        documents.addAll(list);
      }
      else {
        // 校验知识库的权限
        if (!checkPermission(knowledge.getKnowledgeId(), knowledge.getOwnerId(), knowledge.getVisibilityScope())) {
          continue;
        }
        knowledges.add(knowledge);
        // 校验文档的权限
        // dcDocumentId 为空数据，兼容存量项目，默认都可使用
        documents.addAll(CollectionUtils.emptyIfNull(list).stream().filter(p -> p.getDcDocumentId() == null).toList());

        // dcDocumentId 不为空，校验文档权限
        List<SimpleDocumentDTO> docs = CollectionUtils.emptyIfNull(list).stream().filter(p -> p.getDcDocumentId() != null).toList();
        documents.addAll(processPermission(docs, tenantId));
      }
    }
    Assert.notEmpty(knowledges, "没有知识库文档权限！");
    List<String> topicIds = knowledges.stream().map(SimpleKnowledgeDTO::getTopicId).toList();
    List<String> docIds = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(documentIds)) {
      // 校验指定的文档是否可用
      for (Long documentId : documentIds) {
        SimpleDocumentDTO document = IterableUtils.find(documents, p -> Objects.equals(documentId, p.getDocumentId()));
        // Assert.notNull(document, "没有知识库文档权限，documentId=" + documentId);
        if (document != null) {
          docIds.add(document.getExtSystemId().toString());
        }
      }
      Assert.notEmpty(docIds, "没有知识库文档权限！");
    }
    else {
      docIds.addAll(documents.stream().map(p -> p.getExtSystemId().toString()).toList());
      // 如果有权限的文档数等于总数，可以不设置 docIds 参数
      if (isAdmin || total == docIds.size()) {
        docIds.clear();
      }
    }
    return Pair.of(topicIds, docIds);
  }

  /**
   * 判断是否为docchain知识库
   * @param knowledgeList 知识库列表
   * @return 返回内容
   */
  private Pair<List<String>, List<String>> checkKnowledgeType(List<SimpleKnowledgeDTO> knowledgeList) {
    if (knowledgeList.isEmpty()) {
      return Pair.of(new ArrayList<>(), new ArrayList<>());
    }
    SimpleKnowledgeDTO simpleKnowledgeDTO = knowledgeList.get(0);
    if (!KnowledgeConsts.KNOWLEDGE_TYPE_DOC_CHAIN.equals(simpleKnowledgeDTO.getKnowledgeType())) {
      return Pair.of(new ArrayList<>(), new ArrayList<>());
    }
    return null;
  }

  /**
   * 收集有权限的文档列表
   */
  private List<SimpleDocumentDTO> processPermission(List<SimpleDocumentDTO> docs, Long tenantId) {
    return ThreadContextRunner.runWithTenant(tenantId, () -> {
      if (CollectionUtils.isEmpty(docs)) {
        return Collections.emptyList();
      }
      List<SimpleDocumentDTO> list = new ArrayList<>();
      Long userId = SessionUtil.getLoginInfo().getUserId();
      // 过滤掉 libraryId 为 null 的文档，这些文档无法查询权限
      Map<String, List<SimpleDocumentDTO>> group = docs.stream()
        .filter(doc -> doc.getLibraryId() != null && !doc.getLibraryId().isEmpty())
        .collect(Collectors.groupingBy(SimpleDocumentDTO::getLibraryId));
      for (Map.Entry<String, List<SimpleDocumentDTO>> entry : group.entrySet()) {
        String libraryId = entry.getKey();
        List<SimpleDocumentDTO> values = entry.getValue();
        List<String> nodeIds = values.stream().map(SimpleDocumentDTO::getDcDocumentId).collect(Collectors.toList());
        ControlRoleDict roleDict = controlTemplate.fetchNodeRole(libraryId, userId, nodeIds);
        // 根据权限结果添加文档ID
        for (SimpleDocumentDTO document : values) {
          ControlRole controlRole = roleDict.get(document.getDcDocumentId());
          if (controlRole != null && controlRole.hasPermission(NodePermission.READ_NODE)) {
            list.add(document);
          }
        }
      }
      return list;
    });
  }

  /**
   * 公开的知识库直接返回true，如果是成员可见的需要判断一下是否有权限，有权限则返回true
   *
   * @param knowledgeId 知识库id
   * @param ownerId 知识库拥有者
   * @param visibilityScope 知识库公开范围
   */
  public boolean checkPermission(Long knowledgeId, Long ownerId, String visibilityScope) {
    if (StringUtils.isEmpty(visibilityScope) || VisibilityScopeEnum.PUBLIC.getCode().equals(visibilityScope)) {
      return Boolean.TRUE;
    }
    String permissionType = queryKnowledgeBasePermissionType(knowledgeId, ownerId);
    return StringUtils.isNotEmpty(permissionType);
  }

  /**
   * 判断具有什么权限
   */
  @Cacheable(value = "knowledgePermission", key = "#knowledgeId + '_' + #userId")
  public String queryKnowledgeBasePermissionType(Long knowledgeId, Long ownerId) {
    Assert.notNull(knowledgeId, "KnowledgeBaseDTO can not be null");
    Long userId = SessionUtil.getLoginInfo().getUserId();
    Assert.notNull(userId, "获取当前登录信息为空");
    if (SessionUtil.isSuperAdmin(userId)) {
      return LibraryRoleEnum.MANAGE.getCode();
    }
    Long spaceId = SpaceContextHolder.getRequiredSpaceId();
    List<OrgDTO> orgList = portalOrgIntegration.queryUserOrgList(spaceId, userId);
    List<Long> orgs = null;
    if (CollectionUtils.isNotEmpty(orgList)) {
      orgs = orgList.stream().map(OrgDTO::getOrgId).collect(Collectors.toList());
    }
    // 检查是否为知识库拥有者
    if (ownerId == null || isKnowledgeBaseOwner(ownerId, userId)) {
      return LibraryRoleEnum.MANAGE.getCode();
    }

    // 获取用户直接权限
    String userPermission = getUserDirectPermission(knowledgeId, userId);
    if (LibraryRoleEnum.MANAGE.getCode().equals(userPermission)) {
      return LibraryRoleEnum.MANAGE.getCode();
    }

    // 处理部门权限
    return getDepartmentPermission(knowledgeId, orgs, userPermission);
  }

  /**
   * 检查是否为知识库拥有者
   */
  private boolean isKnowledgeBaseOwner(Long ownerId, Long userId) {
    return ownerId != null && ownerId.equals(userId);
  }

  /**
   * 获取用户直接权限
   */
  private String getUserDirectPermission(Long knowledgeId, Long userId) {
    BtDcKbPermissionDTO btDcKbPermissionDTO = btDcKbPermissionManageMapper.selectBtDcKbPermissionDTO(knowledgeId,
      userId, SubjectTypeEnum.USER.getCode());
    return btDcKbPermissionDTO != null ? btDcKbPermissionDTO.getPermissionType() : null;
  }

  /**
   * 获取部门权限
   */
  private String getDepartmentPermission(Long knowledgeId, List<Long> deptIds, String userPermission) {
    if (deptIds == null || deptIds.isEmpty()) {
      return userPermission;
    }

    List<BtDcKbPermissionDTO> deptPermissions = btDcKbPermissionManageMapper.selectBtDcKbPermissionDTOs(knowledgeId,
      deptIds);

    if (CollectionUtils.isEmpty(deptPermissions)) {
      if (StringUtils.isAllEmpty(userPermission)) {
        return null;
      }
      return LibraryRoleEnum.EDIT.getCode().equals(userPermission) ? LibraryRoleEnum.EDIT.getCode()
        : LibraryRoleEnum.READ.getCode();
    }

    return getHighestDepartmentPermission(deptPermissions, userPermission);
  }

  /**
   * 获取部门权限中的最高权限
   */
  private String getHighestDepartmentPermission(List<BtDcKbPermissionDTO> deptPermissions, String userPermission) {
    List<String> permissionTypes = deptPermissions.stream().map(BtDcKbPermissionEntity::getPermissionType)
      .collect(Collectors.toList());

    if (permissionTypes.contains(LibraryRoleEnum.MANAGE.getCode())) {
      return LibraryRoleEnum.MANAGE.getCode();
    }

    if (permissionTypes.contains(LibraryRoleEnum.EDIT.getCode())) {
      return LibraryRoleEnum.EDIT.getCode();
    }

    return LibraryRoleEnum.EDIT.getCode().equals(userPermission) ? LibraryRoleEnum.EDIT.getCode()
      : LibraryRoleEnum.READ.getCode();
  }

  public Map<Long, String> batchQuerySimpleKnowledgeBasePermissionType(List<SimpleKnowledgeBaseDTO> knowledgeDtos, Long userId,
    List<Long> deptIds) {
    if (CollectionUtils.isEmpty(knowledgeDtos)) {
      return new HashMap<>();
    }
    // 拥有者
    Map<Long, String> ownerPermissionMap = new HashMap<>();
    List<Long> knowledgeIds = new ArrayList<>();
    for (SimpleKnowledgeBaseDTO knowledgeDto : knowledgeDtos) {
      if (isKnowledgeBaseOwner(knowledgeDto.getOwnerId(), userId)) {
        ownerPermissionMap.put(knowledgeDto.getKnowledgeId(), LibraryRoleEnum.MANAGE.getCode());
      }
      else {
        knowledgeIds.add(knowledgeDto.getKnowledgeId());
      }
    }
    if (knowledgeIds.isEmpty()) {
      return ownerPermissionMap;
    }
    Map<Long, String> result = handlePermissionType(knowledgeIds, userId, deptIds);
    result.putAll(ownerPermissionMap);
    return result;
  }

  public Map<Long, String> batchQueryKnowledgeBasePermissionType(List<KnowledgeBaseDTO> knowledgeDtos, Long userId,
    List<Long> deptIds) {
    if (CollectionUtils.isEmpty(knowledgeDtos)) {
      return new HashMap<>();
    }
    // 拥有者
    Map<Long, String> ownerPermissionMap = new HashMap<>();
    List<Long> knowledgeIds = new ArrayList<>();
    for (KnowledgeBaseDTO knowledgeDto : knowledgeDtos) {
      if (isKnowledgeBaseOwner(knowledgeDto.getOwnerId(), userId)) {
        ownerPermissionMap.put(knowledgeDto.getKnowledgeId(), LibraryRoleEnum.MANAGE.getCode());
      }
      else {
        knowledgeIds.add(knowledgeDto.getKnowledgeId());
      }
    }
    if (knowledgeIds.isEmpty()) {
      return ownerPermissionMap;
    }
    Map<Long, String> result = handlePermissionType(knowledgeIds, userId, deptIds);
    result.putAll(ownerPermissionMap);
    return result;
  }

  /**
   * 批量查询知识库权限类型
   *
   * @param knowledgeIds 知识库ID列表
   * @param userId 用户ID
   * @return 知识库ID -> 权限类型的映射
   */
  public Map<Long, String> handlePermissionType(List<Long> knowledgeIds, Long userId, List<Long> deptIds) {

    Assert.notNull(userId, "用户ID不能为空");

    // 批量查询用户权限
    List<BtDcKbPermissionDTO> userPermissions = btDcKbPermissionManageMapper.batchSelectUserPermissions(knowledgeIds, userId);
    Map<Long, String> userPermissionMap = userPermissions.stream().collect(Collectors.toMap(BtDcKbPermissionDTO::getKbId, BtDcKbPermissionDTO::getPermissionType, (existing, replacement) -> existing));
    // 批量查询部门权限
    Map<Long, String> deptPermissionMap = new HashMap<>();
    if (deptIds != null && !deptIds.isEmpty()) {
      List<BtDcKbPermissionDTO> deptPermissions = btDcKbPermissionManageMapper.batchSelectDeptPermissions(knowledgeIds, deptIds);
      deptPermissionMap = deptPermissions.stream().collect(Collectors.groupingBy(BtDcKbPermissionDTO::getKbId, Collectors.mapping(BtDcKbPermissionDTO::getPermissionType, Collectors.toList()))).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> getHighestPermissionFromList(entry.getValue())));
    }

    // 合并权限结果
    Map<Long, String> result = new HashMap<>();
    for (Long knowledgeId : knowledgeIds) {
      String userPermission = userPermissionMap.get(knowledgeId);
      String deptPermission = deptPermissionMap.get(knowledgeId);

      // 权限优先级：MANAGE > EDIT > READ
      String finalPermission = getHighestPermission(userPermission, deptPermission);
      result.put(knowledgeId, finalPermission);
    }

    return result;
  }

  /**
   * 获取最高权限
   */
  private String getHighestPermission(String userPermission, String deptPermission) {
    if (StringUtils.isEmpty(userPermission) && StringUtils.isEmpty(deptPermission)) {
      return LibraryRoleEnum.READ.getCode();
    }

    if (StringUtils.isEmpty(userPermission)) {
      return deptPermission;
    }

    if (StringUtils.isEmpty(deptPermission)) {
      return userPermission;
    }

    // 比较权限级别
    if (LibraryRoleEnum.MANAGE.getCode().equals(userPermission)
      || LibraryRoleEnum.MANAGE.getCode().equals(deptPermission)) {
      return LibraryRoleEnum.MANAGE.getCode();
    }

    if (LibraryRoleEnum.EDIT.getCode().equals(userPermission)
      || LibraryRoleEnum.EDIT.getCode().equals(deptPermission)) {
      return LibraryRoleEnum.EDIT.getCode();
    }

    return LibraryRoleEnum.READ.getCode();
  }

  /**
   * 从权限列表中获取最高权限
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private String getHighestPermissionFromList(List<String> permissions) {
    if (CollectionUtils.isEmpty(permissions)) {
      return LibraryRoleEnum.READ.getCode();
    }

    if (permissions.contains(LibraryRoleEnum.MANAGE.getCode())) {
      return LibraryRoleEnum.MANAGE.getCode();
    }

    if (permissions.contains(LibraryRoleEnum.EDIT.getCode())) {
      return LibraryRoleEnum.EDIT.getCode();
    }

    return LibraryRoleEnum.READ.getCode();
  }
}
