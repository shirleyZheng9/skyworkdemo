package com.iwhalecloud.bote.doc.module.document.service.impl;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.cache.DocumentLibraryCache;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.exception.DocumentNodeAccessDenyException;
import com.iwhalecloud.bote.common.lock.DistributedLock;
import com.iwhalecloud.bote.common.lock.DistributedLockFactory;
import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.model.PageParams;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.consts.DocLockConsts;
import com.iwhalecloud.bote.doc.consts.PermissionActionEnum;
import com.iwhalecloud.bote.doc.consts.PermissionModeEnum;
import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.DocRoleEnum;
import com.iwhalecloud.bote.doc.consts.SubjectTypeEnum;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryPermissionDTO;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.SubjectBuilder.ControlSubject;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRole;
import com.iwhalecloud.bote.doc.module.control.model.ControlRoleInfo;
import com.iwhalecloud.bote.doc.module.control.model.NodeBaseInfoDTO;
import com.iwhalecloud.bote.doc.module.control.service.DocumentNodeService;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPermissionDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPermissionDetailDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPermissionSetDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPermissionUpdateResultDTO;
import com.iwhalecloud.bote.doc.module.document.dto.PermissionDTO;
import com.iwhalecloud.bote.doc.module.document.entity.DocumentPermissionEntity;
import com.iwhalecloud.bote.doc.module.document.mapper.DocumentPermissionMapper;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentPermissionService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.module.document.service.helper.DocumentPermissionValidationHelper;
import com.iwhalecloud.bote.doc.module.library.dto.DocumentLibraryDTO;
import com.iwhalecloud.bote.doc.module.library.service.DocumentLibraryPermissionService;
import com.iwhalecloud.bote.doc.module.user.service.IDcOrgService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 文档权限控制处理类
 *
 * @author Aiqing
 * @since 2025/8/15
 */
@Service("documentPermissionServiceImpl")
@RequiredArgsConstructor
public class DocumentPermissionServiceImpl implements IDocumentPermissionService {

  private final DocumentPermissionMapper documentPermissionMapper;
  private final ControlTemplate controlTemplate;
  private final IDocumentService documentService;
  private final DocumentNodeService documentNodeService;
  private final DocumentLibraryPermissionService documentLibraryPermissionService;
  private final IDcUserService dcUserService;
  private final IDcOrgService dcOrgService;
  private final DistributedLockFactory distributedLockFactory;
  private final DocumentLibraryCache documentLibraryCache;
  private final DocumentPermissionValidationHelper permissionValidationHelper;

  private static String buildPermissionMapKey(PermissionDTO permission) {
    return permission.getSubjectType() + "_" + permission.getSubjectId();
  }

  private static String buildPermissionMapKey(String subjectType, Long subjectId) {
    return subjectType + "_" + subjectId;
  }

  @Override
  public List<ControlRoleInfo> selectControlDocumentRoleInfoByControlIds(List<String> controlIds) {
    return documentPermissionMapper.selectControlDocumentRoleInfoByControlIds(controlIds);
  }

  @Override
  public List<DocumentPermissionDTO> selectByDocumentId(String documentId) {
    return documentPermissionMapper.selectByDocumentId(Collections.singletonList(documentId));
  }

  @Override
  public List<String> selectExistedControlDocumentId(List<String> documentIds) {
    return documentPermissionMapper.selectExistedControlDocumentId(documentIds);
  }

  @Override
  @Transactional
  public void addDocumentControlPermission(Long userId, String documentId, Map<ControlSubject, String> subjectRoleMap) {
    subjectRoleMap.forEach((subject, role) -> {
      Long subjectId = subject.getSubjectId();
      SubjectTypeEnum subjectType = subject.getSubjectType();
      DocumentPermissionDTO param = new DocumentPermissionDTO();
      param.setSubjectId(subjectId);
      param.setSubjectType(subjectType.getCode());
      param.setDocumentId(documentId);
      List<DocumentPermissionDTO> result = documentPermissionMapper.selectByParam(documentId, param);
      if (CollectionUtils.isNotEmpty(result)) {
        DocumentPermissionDTO permissionDTO = result.get(0);
        if (DocRoleEnum.getByCode(role).getLevel() < DocRoleEnum.getByCode(permissionDTO.getPermissionType()).getLevel()) {
          permissionDTO.setPermissionType(role);
          permissionDTO.setGrantedBy(userId);
          documentPermissionMapper.update(permissionDTO);
        }
        return;
      }
      DocumentPermissionDTO newPermission = new DocumentPermissionDTO();
      newPermission.setPermissionId(IDUtils.nextId());
      newPermission.setDocumentId(documentId);
      newPermission.setSubjectId(subjectId);
      newPermission.setSubjectType(subjectType.getCode());
      newPermission.setPermissionType(role);
      newPermission.setGrantedBy(userId);
      newPermission.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
      documentPermissionMapper.insert(newPermission);
    });
  }

  @Transactional
  @Override
  public void updatePermission(PermissionActionEnum action, PermissionDTO permission, String documentId) {
    List<PermissionDTO> permissionDTOList = new ArrayList<>();
    permissionDTOList.add(permission);
    this.updatePermissionBatch(action, permissionDTOList, documentId, false);
  }

  @Override
  @Transactional
  public DocumentPermissionUpdateResultDTO updatePermissionBatch(PermissionActionEnum action,
                                                                 List<PermissionDTO> permissionList,
                                                                 String documentId) {
    int successCount = this.updatePermissionBatch(action, permissionList, documentId, true);
    DocumentPermissionUpdateResultDTO resultDTO = new DocumentPermissionUpdateResultDTO();
    resultDTO.setSuccessCount(successCount);
    return resultDTO;
  }

  /**
   * 批量处理权限数据
   *
   * @param action 更新动作
   * @param permissionList 权限更新数据
   * @param documentId 文档ID
   * @param ignoreWhenNoPermission 是否在无权限操作时忽略
   * @return 成功处理的记录数
   */
  private int updatePermissionBatch(PermissionActionEnum action,
                                    List<PermissionDTO> permissionList,
                                    String documentId,
                                    boolean ignoreWhenNoPermission) {
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    if (documentDTO == null) {
      throw new BssException("文档不存在");
    }
    String libraryId = documentDTO.getLibraryId();
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();

    // 查询用户该文档的最大角色，校验授权不能越权
    ControlRole controlRole = controlTemplate.fetchNodeRole(libraryId, currentLoginUserId, documentId);

    DistributedLock lock = distributedLockFactory.getBizLock(DocLockConsts.DOCUMENT_PERMISSION_SAVE_LOCK, documentId);
    if (lock.tryLock()) {
      try {
        // 检查是否能进行权限的修改
        List<DocumentPermissionSetDTO> existPermissionSets =
          checkUpdatePermission(permissionList, action, currentLoginUserId, controlRole, documentDTO, ignoreWhenNoPermission);
        return updatePermissionBatch(action, permissionList, documentDTO, currentLoginUserId, existPermissionSets, controlRole);
      }
      finally {
        lock.unlock();
      }
    }
    else {
      throw new BssException("请稍后重试");
    }
  }

  @Override
  public DocumentPermissionDetailDTO queryDocumentPermissionSetWithInherit(String documentId) {
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    if (documentDTO == null) {
      throw new BssException("文档不存在");
    }
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
    String libraryId = documentDTO.getLibraryId();
    // 检查文档访问权限
    ControlRole controlRole = controlTemplate.fetchNodeRole(libraryId, currentLoginUserId, documentId);
    if (!controlRole.hasPermission(NodePermission.READ_NODE)) {
      throw new DocumentNodeAccessDenyException();
    }

    List<DocumentPermissionSetDTO> result = queryDocumentPermissionSet(documentDTO, currentLoginUserId, controlRole);

    // 填充授权对象信息
    List<DocumentPermissionSetDTO> filteredPermissionList = fillGrantSubjectInfo(result, documentId);
    DocumentPermissionDetailDTO detailDTO = new DocumentPermissionDetailDTO();
    detailDTO.setPermissionMode(documentDTO.getPermissionMode());
    detailDTO.setPermissions(filteredPermissionList);
    return detailDTO;
  }

  private List<DocumentPermissionSetDTO> checkUpdatePermission(List<PermissionDTO> permissionList,
                                                               PermissionActionEnum action,
                                                               Long currentLoginUserId,
                                                               ControlRole controlRole,
                                                               DcDocumentDTO documentDTO,
                                                               boolean ignoreWhenNoPermission) {
    // 1. 前置校验：检查分享权限
    if (!controlRole.hasPermission(NodePermission.SHARE_NODE)) {
      throw new BssException("权限不足, 无法设置此访问权限");
    }

    // 2. 获取现有权限配置
    List<DocumentPermissionSetDTO> existPermissionSets = queryDocumentPermissionSet(documentDTO, currentLoginUserId, controlRole);

    // 3. 过滤掉自己修改自己的操作
    permissionValidationHelper.validateNotSelfModification(permissionList, currentLoginUserId);

    // 4. 根据action类型校验并过滤权限列表
    permissionValidationHelper.filterInvalidPermissions(permissionList, action, currentLoginUserId, controlRole,
      existPermissionSets, ignoreWhenNoPermission);

    return existPermissionSets;
  }

  private List<String> getAvailableRoleSet(String currentUserRole,
                                           DocumentPermissionSetDTO permission) {
    Boolean editable = permission.getEditable();
    if (Boolean.FALSE.equals(editable)) {
      return Collections.emptyList();
    }

    DocRoleEnum currRole = DocRoleEnum.getByCode(currentUserRole);
    if (currRole == null) {
      return Collections.emptyList();
    }
    String permissionType = permission.getPermissionType();
    String inheritPermission = permission.getInheritPermission();
    DocRoleEnum dataRole = StringUtils.isNotBlank(inheritPermission) ? DocRoleEnum.getByCode(inheritPermission) : DocRoleEnum.getByCode(permissionType);

    return Arrays.stream(DocRoleEnum.values())
      .filter(role -> {
        if (role == DocRoleEnum.ANONYMOUS) {
          return false;
        }
        if (Boolean.TRUE.equals(permission.getInherit()) && !Objects.equals(DocRoleEnum.DOC_MANAGE, currRole)) {
          // 继承的权限，当前用户非管理时，可分配的权限范围是： [数据当前的权限等级|继承的权限等级 - 当前用户拥有的权限等级]
          return role.getLevel() >= currRole.getLevel() && role.getLevel() <= dataRole.getLevel();
        }
        return role.getLevel() >= currRole.getLevel();
      })
      .map(DocRoleEnum::getCode)
      .toList();
  }

  /**
   * 恢复继承权限模式
   * 删除继承权限转换的独立权限，并将文档权限模式恢复为INHERIT
   *
   * @param documentId 文档ID
   */
  @Override
  @Transactional
  public void restoreInheritPermissionMode(String documentId) {
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    if (documentDTO == null) {
      throw new BssException("文档不存在");
    }
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();

    // 检查当前文档是否为独立权限模式
    Integer permissionMode = documentDTO.getPermissionMode();
    if (!Objects.equals(permissionMode, PermissionModeEnum.ASSIGN.getCode())) {
      throw new BssException("当前文档不是独立权限模式，无法恢复继承权限");
    }

    // 删除所有继承权限转换的独立权限记录
    documentPermissionMapper.deleteInheritConvertPermissions(documentId, currentLoginUserId);

    // 更新文档的权限模式为继承模式
    documentService.updateDocumentPermissionMode(documentId, PermissionModeEnum.INHERIT.getCode(), currentLoginUserId);
  }

  private int updatePermissionBatch(PermissionActionEnum action,
                                    List<PermissionDTO> permissionList,
                                    DcDocumentDTO documentDTO,
                                    Long currentLoginUserId,
                                    List<DocumentPermissionSetDTO> existPermissionSets,
                                    ControlRole controlRole) {
    // 批量处理前，检查是否需要转换继承权限
    Integer permissionMode = documentDTO.getPermissionMode();
    boolean isInheritMode = Objects.equals(permissionMode, PermissionModeEnum.INHERIT.getCode());
    if (isInheritMode) {
      existPermissionSets = handleInheritData(permissionList, documentDTO, currentLoginUserId,
        existPermissionSets, controlRole, action);
    }
    Map<String, DocumentPermissionSetDTO> existPermissionMap = existPermissionSets.stream()
      .collect(Collectors.toMap(DocumentPermissionServiceImpl::buildPermissionMapKey,
        Function.identity(), (x, y) -> y));

    int successCount = 0;
    for (PermissionDTO permission : permissionList) {
      // 使用批量处理专用的方法，避免重复检查继承权限转换
      switch (action) {
        case REMOVE_MEMBER:
        case BATCH_REMOVE:
          removePermission(permission, documentDTO, currentLoginUserId, existPermissionMap);
          successCount++;
          break;
        default:
          updatePermissionWithAdd(permission, documentDTO, currentLoginUserId, existPermissionMap);
          successCount++;
      }
    }
    return successCount;
  }

  /**
   * 处理继承权限数据
   * 当需要降低继承权限或移除继承权限时，先将继承权限转换为独立权限
   *
   * @param permissionList 要操作的权限列表
   * @param documentDTO 文档信息
   * @param currentLoginUserId 当前登录用户ID
   * @param existPermissionSets 现有的权限配置
   * @param controlRole 控制角色
   * @param action 权限操作类型
   * @return 处理后的权限配置列表
   */
  private List<DocumentPermissionSetDTO> handleInheritData(List<PermissionDTO> permissionList,
                                                           DcDocumentDTO documentDTO,
                                                           Long currentLoginUserId,
                                                           List<DocumentPermissionSetDTO> existPermissionSets,
                                                           ControlRole controlRole,
                                                           PermissionActionEnum action) {
    // 检查是否有任何要操作的权限是继承权限，且需要降级或移除
    boolean hasInheritPermission = hasInheritPermissionNeedConvert(permissionList, existPermissionSets, action);

    // 只有管理员操作，且是将继承权限调低的情况下，才转换为独立权限
    if (hasInheritPermission) {
      // 批量转换前，先转换所有继承权限为独立权限
      convertInheritPermissionToAssign(documentDTO, existPermissionSets);
      // 重新查询权限配置
      return queryDocumentPermissionSet(documentDTO, currentLoginUserId, controlRole);
    }
    return existPermissionSets;
  }

  private List<DocumentPermissionSetDTO> queryDocumentPermissionSet(DcDocumentDTO documentDTO,
                                                                    Long currentLoginUserId,
                                                                    ControlRole currentUserRole) {
    String documentId = documentDTO.getDocumentId();
    String libraryId = documentDTO.getLibraryId();
    // 合并配置
    Map<String, DocumentPermissionSetDTO> existPermissionMap = new LinkedHashMap<>();

    DocumentLibraryDTO documentLibraryDTO = documentLibraryCache.getByLibraryId(libraryId);
    Assert.notNull(documentLibraryDTO, "文档库不存在");
    // 1. 添加文档库所有人作为文档所有者权限
    DocumentPermissionSetDTO ownerPermission = createOwnerPermission(documentLibraryDTO.getOwnerId());
    existPermissionMap.put(buildPermissionMapKey(ownerPermission), ownerPermission);

    Integer permissionMode = documentDTO.getPermissionMode();
    boolean isInherit = Objects.equals(permissionMode, PermissionModeEnum.INHERIT.getCode());
    if (isInherit) {
      // 查询文档库权限配置
      List<LibraryPermissionDTO> libraryPermissionList = documentLibraryPermissionService.selectByLibraryId(libraryId);
      putInheritPermission(documentId, libraryPermissionList, existPermissionMap);
    }

    // 查询文档的权限配置
    List<DocumentPermissionDTO> documentPermissionList = this.selectByDocumentId(documentId);

    // 添加文档直接权限
    documentPermissionList.forEach(permission -> {
      boolean inheritConvert = isInherit && DocBaseConsts.TRUE.equals(permission.getInheritConvert());

      putDocumentPermission(permission, existPermissionMap, false, null, true);
      if (inheritConvert) {
        DocumentPermissionSetDTO permissionSetDTO = existPermissionMap.get(buildPermissionMapKey(permission.getSubjectType(), permission.getSubjectId()));
        permissionSetDTO.setInherit(true);
      }
    });
    List<DocumentPermissionSetDTO> permissionList = new ArrayList<>(existPermissionMap.values());
    // 当前用户拥有的节点最大权限
    String roleTag = currentUserRole.getRoleTag();
    // 设置权限的可编辑状态
    for (DocumentPermissionSetDTO permission : permissionList) {
      // 只有文档所有者、具有管理权限、当前已有权限比记录权限高的用户才能修改权限
      permission.setEditable(permissionValidationHelper.isPermissionEditable(permission, currentLoginUserId, roleTag));
      permission.setRemovable(permissionValidationHelper.isPermissionRemovable(permission, currentLoginUserId, roleTag));
      permission.setRoleSet(getAvailableRoleSet(roleTag, permission));
    }
    return permissionList;
  }

  @Override
  public boolean existUserPermissionByLibraryId(String libraryId, Long userId) {
    List<Long> orgIdList = dcOrgService.queryUserOrgIdList(userId);
    // 此处限制检索一条数据
    PageParams pageParams = new PageParams();
    pageParams.setPageNum(1);
    pageParams.setPageSize(1);
    //noinspection resource
    Page<Long> permissionIds = documentPermissionMapper.selectUserPermissionByLibraryId(libraryId, userId, orgIdList, pageParams.buildRowBounds()); //NOPMD - suppressed CloseResource - 不需要关闭
    return permissionIds.getTotal() > 0;
  }

  private void putInheritPermission(String documentId,
                                    List<LibraryPermissionDTO> libraryPermissionList,
                                    Map<String, DocumentPermissionSetDTO> existPermissionMap) {
    // 查询上级目录的继承权限
    List<NodeBaseInfoDTO> parentPathNodes = documentNodeService.getParentPathNodes(documentId, false);

    // 过滤掉当前节点
    List<NodeBaseInfoDTO> parentNodes = parentPathNodes.stream()
      .filter(item -> !Objects.equals(item.getNodeId(), documentId))
      .toList();

    // 查询上级路径节点的所有权限配置
    if (CollectionUtils.isNotEmpty(parentNodes)) {
      // 由近及远遍历父节点，默认继承离的最近的一个父节点的权限
      List<String> nodeIdList = parentNodes.stream().map(NodeBaseInfoDTO::getNodeId).toList();
      List<DocumentPermissionDTO> parentPermissionBatchList = documentPermissionMapper.selectByDocumentId(nodeIdList);
      Map<String, List<DocumentPermissionDTO>> permissionMap = parentPermissionBatchList.stream()
        .collect(Collectors.groupingBy(DocumentPermissionDTO::getDocumentId));

      for (NodeBaseInfoDTO parentNode : parentNodes) {
        List<DocumentPermissionDTO> nodePermissions = permissionMap.get(parentNode.getNodeId());
        if (CollectionUtils.isNotEmpty(nodePermissions)) {
          nodePermissions.forEach(permission -> {
            putDocumentPermission(permission, existPermissionMap, true, "继承自上级目录", false);
          });
          // 有独立权限，继承链截断
          if (Objects.equals(PermissionModeEnum.ASSIGN.getCode(), parentNode.getPermissionMode())) {
            return;
          }
        }
      }
    }
    // 最后添加文档库权限（继承权限），如果继承链被截断，也不会继承文档库权限
    for (LibraryPermissionDTO libraryPermission : libraryPermissionList) {
      DocumentPermissionSetDTO permissionSet = convertLibraryPermission(libraryPermission);
      String permissionMapKey = buildPermissionMapKey(permissionSet);
      existPermissionMap.putIfAbsent(permissionMapKey, permissionSet);
    }
  }

  /**
   * 检查是否有继承权限需要转换为独立权限
   *
   * @param permissionList 要操作的权限列表
   * @param existPermissionSets 现有的权限配置
   * @param action 权限操作类型
   * @return 是否有继承权限需要转换
   */
  private boolean hasInheritPermissionNeedConvert(List<PermissionDTO> permissionList,
                                                  List<DocumentPermissionSetDTO> existPermissionSets,
                                                  PermissionActionEnum action) {
    // 将现有权限转换为 Map，key 为 subjectType_subjectId，便于快速查找
    Map<String, DocumentPermissionSetDTO> existPermissionMap = existPermissionSets.stream()
      .collect(Collectors.toMap(
        permission -> buildPermissionMapKey(permission.getSubjectType(), permission.getSubjectId()),
        Function.identity(),
        (existing, replacement) -> existing
      ));

    // 检查是否有需要转换的继承权限
    boolean isRemoveAction = permissionValidationHelper.isRemoveAction(action);
    for (PermissionDTO permission : permissionList) {
      String key = buildPermissionMapKey(permission);
      DocumentPermissionSetDTO existPermission = existPermissionMap.get(key);

      if (existPermission != null && isInheritPermissionNeedConvert(permission, existPermission, isRemoveAction)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 判断继承权限是否需要转换为独立权限
   * 当满足以下条件时需要转换：
   * 1. 现有权限是继承权限
   * 2. 现有权限不是所有者权限
   * 3. 新权限级别低于继承权限级别 或 执行移除操作
   *
   * @param newPermission 要设置的新权限
   * @param existPermission 现有权限
   * @param isRemoveAction 是否是移除操作
   * @return 是否需要转换
   */
  private boolean isInheritPermissionNeedConvert(PermissionDTO newPermission,
                                                 DocumentPermissionSetDTO existPermission,
                                                 boolean isRemoveAction) {
    // 不是继承权限，不需要转换
    if (!Boolean.TRUE.equals(existPermission.getInherit())) {
      return false;
    }

    // 是所有者权限，不需要转换
    if (Boolean.TRUE.equals(existPermission.getOwner())) {
      return false;
    }

    // 如果是移除操作，需要转换
    if (isRemoveAction) {
      return true;
    }

    // 比较权限级别：新权限级别 > 继承权限级别（数值越大，权限越低）
    String inheritPermissionType = StringUtils.isNotBlank(existPermission.getInheritPermission())
      ? existPermission.getInheritPermission()
      : existPermission.getPermissionType();
    DocRoleEnum inheritRole = DocRoleEnum.getByCode(inheritPermissionType);
    DocRoleEnum newRole = DocRoleEnum.getByCode(newPermission.getPermissionType());

    return newRole.getLevel() > inheritRole.getLevel();
  }


  private void updatePermissionWithAdd(PermissionDTO permissionDTO,
                                       DcDocumentDTO documentDTO,
                                       Long currentUser,
                                       Map<String, DocumentPermissionSetDTO> existPermissionMap) {
    String documentId = documentDTO.getDocumentId();
    DocumentPermissionDTO param = new DocumentPermissionDTO();
    param.setDocumentId(documentId);
    param.setSubjectId(permissionDTO.getSubjectId());
    param.setSubjectType(permissionDTO.getSubjectType());
    List<DocumentPermissionDTO> existPermissionList = documentPermissionMapper.selectByParam(documentId, param);
    String permissionType = permissionDTO.getPermissionType();
    if (CollectionUtils.isEmpty(existPermissionList)) {
      // 检查是否是继承的权限
      DocumentPermissionSetDTO existPermission = existPermissionMap.get(buildPermissionMapKey(permissionDTO));
      boolean inherit = existPermission != null && Boolean.TRUE.equals(existPermission.getInherit());
      // 新增
      addNewPermission(permissionDTO, documentId, currentUser, permissionType, inherit);
      return;
    }
    // 更新
    DocumentPermissionDTO documentPermissionDTO = existPermissionList.get(0);
    if (Objects.equals(documentPermissionDTO.getPermissionType(), permissionType)) {
      return;
    }
    documentPermissionMapper.updateSubjectPermission(documentPermissionDTO.getPermissionId(),
      permissionType, currentUser);
  }

  private void addNewPermission(PermissionDTO permissionDTO,
                                String documentId,
                                Long currentUser,
                                String permissionType,
                                boolean inherit) {
    DocumentPermissionEntity permissionEntity = new DocumentPermissionEntity();
    permissionEntity.setPermissionId(IDUtils.nextId());
    permissionEntity.setDocumentId(documentId);
    permissionEntity.setSubjectId(permissionDTO.getSubjectId());
    permissionEntity.setSubjectType(permissionDTO.getSubjectType());
    permissionEntity.setPermissionType(permissionType);
    permissionEntity.setGrantedBy(currentUser);
    permissionEntity.setCreatorId(currentUser);
    permissionEntity.setUpdatorId(currentUser);
    permissionEntity.setInheritConvert(inherit ? DocBaseConsts.TRUE : DocBaseConsts.FALSE);
    permissionEntity.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    documentPermissionMapper.insert(permissionEntity);
  }

  private void removePermission(PermissionDTO permissionDTO,
                                DcDocumentDTO documentDTO,
                                Long currentUser,
                                Map<String, DocumentPermissionSetDTO> existPermissionMap) {
    String documentId = documentDTO.getDocumentId();

    DocumentPermissionSetDTO existPermission = existPermissionMap.get(buildPermissionMapKey(permissionDTO));
    if (existPermission == null) {
      return;
    }
    // 直接移除权限
    documentPermissionMapper.removePermission(documentId, permissionDTO.getSubjectId(), permissionDTO.getSubjectType(), currentUser);
  }

  /**
   * 转换继承权限为独立权限
   *
   * @param documentDTO 文档信息
   * @param existPermissionSets 已有的权限配置
   */
  private void convertInheritPermissionToAssign(DcDocumentDTO documentDTO, List<DocumentPermissionSetDTO> existPermissionSets) {
    String documentId = documentDTO.getDocumentId();
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();

    // 获取所有继承的权限（排除所有者权限）
    List<DocumentPermissionSetDTO> inheritPermissions = existPermissionSets.stream()
      .filter(permission -> Boolean.TRUE.equals(permission.getInherit())
        && !Boolean.TRUE.equals(permission.getOwner()))
      .toList();

    for (DocumentPermissionSetDTO inheritPermission : inheritPermissions) {
      // 检查是否已存在相同的直接权限
      DocumentPermissionDTO param = new DocumentPermissionDTO();
      param.setDocumentId(documentId);
      param.setSubjectId(inheritPermission.getSubjectId());
      param.setSubjectType(inheritPermission.getSubjectType());
      List<DocumentPermissionDTO> existPermissionList = documentPermissionMapper.selectByParam(documentId, param);

      if (CollectionUtils.isEmpty(existPermissionList)) {
        // 创建新的独立权限记录
        DocumentPermissionEntity permissionEntity = new DocumentPermissionEntity();
        permissionEntity.setPermissionId(IDUtils.nextId());
        permissionEntity.setDocumentId(documentId);
        permissionEntity.setSubjectId(inheritPermission.getSubjectId());
        permissionEntity.setSubjectType(inheritPermission.getSubjectType());
        permissionEntity.setPermissionType(inheritPermission.getPermissionType());
        permissionEntity.setInheritConvert(DocBaseConsts.TRUE);
        permissionEntity.setGrantedBy(currentLoginUserId);
        permissionEntity.setCreatorId(currentLoginUserId);
        permissionEntity.setUpdatorId(currentLoginUserId);
        permissionEntity.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
        documentPermissionMapper.insert(permissionEntity);
      }
    }
    // 更新文档的权限模式为独立配置
    documentService.updateDocumentPermissionMode(documentId, PermissionModeEnum.ASSIGN.getCode(), currentLoginUserId);
    // 更新对象值，后续使用
    documentDTO.setPermissionMode(PermissionModeEnum.ASSIGN.getCode());
  }

  private List<DocumentPermissionSetDTO> fillGrantSubjectInfo(List<DocumentPermissionSetDTO> result, String documentId) {
    Set<Long> userIdList = result.stream()
      .filter(item -> Objects.equals(item.getSubjectType(), SubjectTypeEnum.USER.getCode()))
      .map(DocumentPermissionSetDTO::getSubjectId)
      .collect(Collectors.toSet());

    List<Long> grantUserIdList = result.stream().map(DocumentPermissionSetDTO::getGrantedBy)
      .filter(Objects::nonNull)
      .distinct()
      .toList();
    userIdList.addAll(grantUserIdList);
    List<PortalUserDTO> userBatchByIds = dcUserService.findUserBatchByIds(new ArrayList<>(userIdList));
    Map<Long, PortalUserDTO> userDTOMap = userBatchByIds.stream()
      .collect(Collectors.toMap(PortalUserDTO::getUserId, Function.identity(), (x, y) -> y));

    // 填充组织信息
    List<Long> orgIdList = result.stream()
      .filter(item -> Objects.equals(item.getSubjectType(), SubjectTypeEnum.ORG.getCode()))
      .map(DocumentPermissionSetDTO::getSubjectId)
      .distinct()
      .collect(Collectors.toList());

    List<OrgDTO> orgList = dcOrgService.findBatchById(orgIdList);
    Map<Long, OrgDTO> orgMap = orgList.stream()
      .collect(Collectors.toMap(OrgDTO::getOrgId, Function.identity(), (x, y) -> y));

    List<DocumentPermissionSetDTO> invalidSubjects = new ArrayList<>();

    List<DocumentPermissionSetDTO> dtoList = result.stream()
      .map(permission -> {
        return fillExtUserInfo(permission, userDTOMap, invalidSubjects, orgMap);
      }).filter(Objects::nonNull).toList();

    // 移除失效的记录
    ThreadPools.getCommon().submit(() -> {
      invalidSubjects.forEach(item -> {
        documentPermissionMapper.removePermission(documentId, item.getSubjectId(), item.getSubjectType(), DocBaseConsts.DEFAULT_SYSTEM_USER_ID);
      });
    });
    return dtoList;
  }

  private DocumentPermissionSetDTO fillExtUserInfo(DocumentPermissionSetDTO permission,
                                                   Map<Long, PortalUserDTO> userDTOMap,
                                                   List<DocumentPermissionSetDTO> invalidSubjects,
                                                   Map<Long, OrgDTO> orgMap) {
    Long subjectId = permission.getSubjectId();
    if (Objects.equals(permission.getSubjectType(), SubjectTypeEnum.USER.getCode())) {
      PortalUserDTO portalUserDTO = userDTOMap.get(subjectId);
      if (portalUserDTO != null) {
        permission.setSubjectName(portalUserDTO.getUserName());
      }
      else {
        invalidSubjects.add(permission);
        return null;
      }
    }
    else if (Objects.equals(permission.getSubjectType(), SubjectTypeEnum.ORG.getCode())) {
      OrgDTO orgDTO = orgMap.get(subjectId);
      if (orgDTO != null) {
        permission.setSubjectName(orgDTO.getOrgName());
      }
      else {
        invalidSubjects.add(permission);
        return null;
      }
    }
    if (permission.getGrantedBy() != null) {
      PortalUserDTO portalUserDTO = userDTOMap.get(permission.getGrantedBy());
      if (portalUserDTO != null) {
        permission.setGrantedByName(portalUserDTO.getUserName());
      }
      if (StringUtils.isBlank(permission.getGrantDesc())) {
        permission.setGrantDesc(String.format("由「%s」添加", permission.getGrantedByName()));
      }
    }
    return permission;
  }

  private void putDocumentPermission(DocumentPermissionDTO documentPermission,
                                     Map<String, DocumentPermissionSetDTO> existPermissionMap,
                                     boolean inherit,
                                     String grantDesc,
                                     boolean override) {
    DocumentPermissionSetDTO permissionSet = convertDocumentPermission(documentPermission);

    String permissionType = documentPermission.getPermissionType();
    // 如果明确设置为无权限的，需要移除
    if (Objects.equals(permissionType, DocRoleEnum.ANONYMOUS.getCode())) {
      existPermissionMap.remove(buildPermissionMapKey(permissionSet));
      return;
    }
    permissionSet.setGrantDesc(grantDesc);
    permissionSet.setInherit(inherit);
    if (inherit) {
      permissionSet.setInheritPermission(permissionType);
    }

    DocumentPermissionSetDTO existPermissionSet = existPermissionMap.get(buildPermissionMapKey(permissionSet));
    if (existPermissionSet != null) {
      // 所有者的权限不能覆盖, 继承自文档库的权限可以覆盖
      boolean isOverridePerm = isOverridePermission(override, existPermissionSet, permissionSet);
      if (isOverridePerm) {
        if (StringUtils.isNotBlank(existPermissionSet.getInheritPermission())) {
          permissionSet.setInheritPermission(existPermissionSet.getInheritPermission());
        }
        existPermissionMap.put(buildPermissionMapKey(permissionSet), permissionSet);
      }
    }
    else {
      existPermissionMap.put(buildPermissionMapKey(permissionSet), permissionSet);
    }
  }

  private boolean isOverridePermission(boolean override,
                                       DocumentPermissionSetDTO existPermissionSet,
                                       DocumentPermissionSetDTO newPermission) {
    if (Boolean.TRUE.equals(newPermission.getInheritFromLibrary())) {
      return true;
    }
    if (Objects.equals(existPermissionSet.getPermissionType(), newPermission.getPermissionType())) {
      return false;
    }
    return override && !Boolean.TRUE.equals(existPermissionSet.getOwner());
  }

  /**
   * 转换文档库权限为文档权限设置
   */
  private DocumentPermissionSetDTO convertLibraryPermission(LibraryPermissionDTO libraryPermission) {
    DocumentPermissionSetDTO permissionSet = new DocumentPermissionSetDTO();
    permissionSet.setSubjectType(libraryPermission.getSubjectType());
    permissionSet.setSubjectId(libraryPermission.getSubjectId());
    permissionSet.setPermissionType(libraryPermission.getPermissionType());
    permissionSet.setGrantedBy(libraryPermission.getGrantedBy());
    permissionSet.setGrantedByName("文档库成员");
    permissionSet.setOwner(false);
    // 标记为继承权限
    permissionSet.setInherit(true);
    permissionSet.setGrantDesc("文档库成员");
    permissionSet.setInheritFromLibrary(true);
    return permissionSet;
  }

  /**
   * 转换文档权限为文档权限设置
   */
  private DocumentPermissionSetDTO convertDocumentPermission(DocumentPermissionDTO documentPermission) {
    DocumentPermissionSetDTO permissionSet = new DocumentPermissionSetDTO();
    permissionSet.setSubjectType(documentPermission.getSubjectType());
    permissionSet.setSubjectId(documentPermission.getSubjectId());
    permissionSet.setPermissionType(documentPermission.getPermissionType());
    permissionSet.setGrantedBy(documentPermission.getGrantedBy());
    permissionSet.setOwner(false);
    return permissionSet;
  }

  /**
   * 创建文档所有者权限
   */
  private DocumentPermissionSetDTO createOwnerPermission(Long ownerId) {
    DocumentPermissionSetDTO ownerPermission = new DocumentPermissionSetDTO();
    ownerPermission.setSubjectType(SubjectTypeEnum.USER.getCode());
    ownerPermission.setSubjectId(ownerId);
    // 所有者默认拥有管理权限
    ownerPermission.setPermissionType(DocRoleEnum.DOC_MANAGE.getCode());
    ownerPermission.setOwner(true);
    ownerPermission.setInherit(false);
    return ownerPermission;
  }
}
