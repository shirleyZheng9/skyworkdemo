package com.iwhalecloud.bote.doc.module.knowledge.service.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.consts.PermissionActionEnum;
import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.LibraryRoleEnum;
import com.iwhalecloud.bote.doc.consts.SubjectTypeEnum;
import com.iwhalecloud.bote.doc.enums.DocSequences;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcKbPermissionDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseBatchPermissionRequestDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseBatchPermissionRequestDTO.PermissionMemberDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBasePermissionRequestDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBasePermissionRequestDTO.KnowledgeBasePermissionDataDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.BtDcKbPermissionQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.BtDcKbPermissionManageMapper;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.KnowledgeBaseManageMapper;
import com.iwhalecloud.bote.doc.module.knowledge.service.IBtDcKbPermissionManageService;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.BtDcKbPermissionHelper;
import com.iwhalecloud.bote.doc.module.user.service.IDcOrgService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 知识库权限表管理服务实现
 *
 * @author linmengfan
 * @since 2025-08-23
 */
@Service
@RequiredArgsConstructor
public class BtDcKbPermissionManageServiceImpl implements IBtDcKbPermissionManageService {

  private final BtDcKbPermissionManageMapper btDcKbPermissionManageMapper;

  private final KnowledgeBaseManageMapper knowledgeBaseManageMapper;

  private final IDcUserService dcUserService;

  private final IDcOrgService dcOrgService;

  private final BtDcKbPermissionHelper bthDcKbPermissionHelper;

  /**
   * 验证并获取知识库信息
   */
  private KnowledgeBaseDTO validateAndGetKnowledgeBase(Long tenantId, Long kbId) {
    KnowledgeBaseDTO knowledge = knowledgeBaseManageMapper.selectSimpleKnowledgeById(tenantId, kbId);
    Assert.notNull(knowledge, () -> "知识库不存在， knowledgeId=" + kbId);
    return knowledge;
  }

  /**
   * 判断是否为知识库拥有者
   */
  private boolean isOwner(KnowledgeBaseDTO knowledge, String subjectType, Long subjectId) {
    // 如果是用户类型且用户ID与知识库创建者相同，则为拥有者
    return "USER".equals(subjectType) && knowledge.getOwnerId() != null
      && knowledge.getOwnerId().equals(subjectId);
  }

  /**
   * 获取权限类型权重，用于排序 权重越小优先级越高：MANAGE(1) > EDIT(2) > READ(3)
   */
  private int getPermissionTypeWeight(String permissionType) {
    if (permissionType == null) {
      return 999; // 未知权限类型排在最后
    }

    switch (permissionType) {
      case "MANAGE":
        return 1;
      case "EDIT":
        return 2;
      case "READ":
        return 3;
      default:
        return 999; // 其他权限类型排在最后
    }
  }

  /**
   * 获取权限列表
   */
  private List<BtDcKbPermissionDTO> getPermissionList(BtDcKbPermissionQueryParams queryParams) {
    List<BtDcKbPermissionDTO> permissions = btDcKbPermissionManageMapper.selectBtDcKbPermissionList(queryParams);
    return CollectionUtils.isEmpty(permissions) ? new ArrayList<>() : permissions;
  }

  /**
   * 添加拥有者权限
   */
  private void addOwnerPermission(List<BtDcKbPermissionDTO> permissions, KnowledgeBaseDTO knowledge) {
    BtDcKbPermissionDTO ownerPermission = new BtDcKbPermissionDTO();
    ownerPermission.setPermissionType(LibraryRoleEnum.MANAGE.getCode());
    ownerPermission.setIsOwner(Boolean.TRUE);
    ownerPermission.setSubjectId(knowledge.getOwnerId());
    ownerPermission.setSubjectType(SubjectTypeEnum.USER.getCode());
    permissions.add(ownerPermission);
  }

  /**
   * 填充用户和组织信息
   */
  private void fillUserAndOrgInfo(List<BtDcKbPermissionDTO> permissions) {
    permissions.forEach(permission -> {
      fillGrantedByInfo(permission);
      fillSubjectInfo(permission);
    });
  }

  /**
   * 填充授权人信息
   */
  private void fillGrantedByInfo(BtDcKbPermissionDTO permission) {
    PortalUserDTO userInfoGranted = dcUserService.findUserById(permission.getGrantedBy());
    if (userInfoGranted != null) {
      permission.setGrantedByName(userInfoGranted.getUserName());
    }
  }

  /**
   * 填充主体信息
   */
  private void fillSubjectInfo(BtDcKbPermissionDTO permission) {
    if (SubjectTypeEnum.USER.getCode().equals(permission.getSubjectType())) {
      fillUserSubjectInfo(permission);
    }
    else {
      fillOrgSubjectInfo(permission);
    }
  }

  /**
   * 填充用户主体信息
   */
  private void fillUserSubjectInfo(BtDcKbPermissionDTO permission) {
    PortalUserDTO userInfo = dcUserService.findUserById(permission.getSubjectId());
    if (userInfo != null) {
      permission.setSubjectName(userInfo.getUserName());
    }
  }

  /**
   * 填充组织主体信息
   */
  private void fillOrgSubjectInfo(BtDcKbPermissionDTO permission) {
    OrgDTO orgDTO = dcOrgService.findById(permission.getSubjectId());
    if (orgDTO != null) {
      permission.setSubjectName(orgDTO.getOrgName());
    }
  }

  /**
   * 对权限列表进行排序
   */
  private void sortPermissions(List<BtDcKbPermissionDTO> permissions) {
    if (permissions.size() > 1) {
      permissions.sort(this::comparePermissions);
    }
  }

  /**
   * 比较两个权限的排序顺序
   */
  private int comparePermissions(BtDcKbPermissionDTO p1, BtDcKbPermissionDTO p2) {
    // 首先按isOwner排序，true的排在前面
    int ownerComparison = compareByOwner(p1.getIsOwner(), p2.getIsOwner());
    if (ownerComparison != 0) {
      return ownerComparison;
    }

    // 如果isOwner相同，则按permissionType权重排序
    return compareByPermissionType(p1.getPermissionType(), p2.getPermissionType());
  }

  /**
   * 按拥有者状态比较
   */
  private int compareByOwner(Boolean isOwner1, Boolean isOwner2) {
    if (Boolean.TRUE.equals(isOwner1) && !Boolean.TRUE.equals(isOwner2)) {
      return -1; // p1排在前面
    }
    if (!Boolean.TRUE.equals(isOwner1) && Boolean.TRUE.equals(isOwner2)) {
      return 1; // p2排在前面
    }
    return 0; // 相等
  }

  /**
   * 按权限类型比较
   */
  private int compareByPermissionType(String permissionType1, String permissionType2) {
    int weight1 = getPermissionTypeWeight(permissionType1);
    int weight2 = getPermissionTypeWeight(permissionType2);
    return Integer.compare(weight1, weight2); // 权重小的排在前面
  }

  @Override
  public List<BtDcKbPermissionDTO> queryBtDcKbPermissionList(BtDcKbPermissionQueryParams queryParams) {
    // 1. 判断知识库是否存在
    KnowledgeBaseDTO knowledgeBase = validateAndGetKnowledgeBase(queryParams.getTenantId(), queryParams.getKbId());

    // 2. 获取权限列表
    List<BtDcKbPermissionDTO> btDcKbPermissionDTOS = getPermissionList(queryParams);

    // 3. 添加拥有者权限
    addOwnerPermission(btDcKbPermissionDTOS, knowledgeBase);

    // 4. 填充用户和组织信息
    fillUserAndOrgInfo(btDcKbPermissionDTOS);

    // 5. 排序
    sortPermissions(btDcKbPermissionDTOS);

    return btDcKbPermissionDTOS;
  }

  @Override
  public PageInfo<BtDcKbPermissionDTO> queryBtDcKbPermissionPage(BtDcKbPermissionQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    return btDcKbPermissionManageMapper.selectBtDcKbPermissionPage(queryParams, rowBounds).toPageInfo();
  }

  @Transactional
  @Override
  public ResultVO<Void> updateKbPermissions(Long knowledgeId, KnowledgeBasePermissionRequestDTO request) {

    KnowledgeBaseDTO knowledgeBaseDTO = validateLibraryAndPermissions(knowledgeId, request.getTenantId());
    validateKbOwner(knowledgeBaseDTO, request.getData().getSubjectType(), request.getData().getSubjectId());
    validatePermissionType(request);
    executePermissionAction(knowledgeId, request, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Transactional
  @Override
  public ResultVO<Void> batchKbPermissions(Long knowledgeId, KnowledgeBaseBatchPermissionRequestDTO request) {
    // 验证文档库存在性和用户权限
    KnowledgeBaseDTO knowledgeBaseDTO = validateLibraryAndPermissions(knowledgeId, request.getTenantId());

    // 验证所有成员的权限设置合法性
    validateBatchMembers(request.getMembers(), knowledgeBaseDTO);

    // 验证批量操作类型
    validateBatchActionType(request);

    try {
      PermissionActionEnum action = PermissionActionEnum.valueOf(request.getAction());
      executeBatchPermissionAction(knowledgeId, request.getMembers(), SessionUtil.getLoginInfo().getUserId(),
        action);
      return ResultVO.success();
    }
    catch (Exception e) {
      throw new BssException("批量权限设置失败，请稍后重试: " + e.getMessage(), e);
    }
  }

  private void validateKbOwner(KnowledgeBaseDTO knowledgeBaseDTO, String subjectType, Long subjectId) {
    if (isOwner(knowledgeBaseDTO, subjectType, subjectId)) {
      throw new BssException("所有者权限不能变更！");
    }
  }

  /**
   * 执行批量权限操作
   */
  private void executeBatchPermissionAction(Long knowledgeId, List<PermissionMemberDTO> members, Long userId,
                                            PermissionActionEnum action) {
    switch (action) {
      case BATCH_ADD:
        batchAddPermissions(knowledgeId, members, userId);
        break;
      case BATCH_UPDATE:
        batchUpdatePermissions(knowledgeId, members, userId);
        break;
      case BATCH_REMOVE:
        batchRemovePermissions(knowledgeId, members, userId);
        break;
      default:
        throw new BssException("不支持的操作类型: " + action.getCode());
    }
  }

  /**
   * 批量添加权限
   */
  private void batchAddPermissions(Long knowledgeId, List<PermissionMemberDTO> members, Long userId) {
    List<BtDcKbPermissionDTO> permissionsToAdd = new ArrayList<>();

    for (PermissionMemberDTO member : members) {
      // 检查权限主体是否已存在
      validateMemberNotExists(knowledgeId, member.getSubjectId(), member.getSubjectType());

      // 创建权限实体
      BtDcKbPermissionDTO permission = createPermissionEntity(knowledgeId, member.getPermissionType(),
        member.getSubjectId(), member.getSubjectType(), userId);
      permissionsToAdd.add(permission);
    }

    // 批量插入权限
    if (!permissionsToAdd.isEmpty()) {
      int result = btDcKbPermissionManageMapper.batchInsertBtDcKbPermission(permissionsToAdd);
      if (result <= 0) {
        throw new BssException("批量添加权限失败");
      }
    }
  }

  /**
   * 批量更新权限
   */
  private void batchUpdatePermissions(Long knowledgeId, List<PermissionMemberDTO> members, Long userId) {
    for (PermissionMemberDTO member : members) {
      // 检查权限主体是否存在
      BtDcKbPermissionDTO existingPermission = btDcKbPermissionManageMapper.selectBtDcKbPermissionDTO(knowledgeId,
        member.getSubjectId(), member.getSubjectType());

      if (existingPermission == null) {
        throw new BssException("权限主体不存在，无法更新: " + member.getSubjectType() + "-" + member.getSubjectId());
      }

      // 更新权限类型
      existingPermission.setPermissionType(member.getPermissionType());
      existingPermission.setUpdatorId(userId);

      int result = btDcKbPermissionManageMapper.updateBtDcKbPermission(existingPermission);
      if (result <= 0) {
        throw new BssException("更新权限失败: " + member.getSubjectType() + "-" + member.getSubjectId());
      }
    }
  }

  /**
   * 批量移除权限
   */
  private void batchRemovePermissions(Long knowledgeId, List<PermissionMemberDTO> members, Long userId) {
    List<Long> permissionIdsToDelete = new ArrayList<>();

    for (PermissionMemberDTO member : members) {
      // 检查权限主体是否存在
      // 检查权限主体是否存在
      BtDcKbPermissionDTO existingPermission = btDcKbPermissionManageMapper.selectBtDcKbPermissionDTO(knowledgeId,
        member.getSubjectId(), member.getSubjectType());

      if (existingPermission == null) {
        throw new BssException("权限主体不存在，无法移除: " + member.getSubjectType() + "-" + member.getSubjectId());
      }

      permissionIdsToDelete.add(existingPermission.getPermissionId());
    }

    // 批量删除权限
    if (!permissionIdsToDelete.isEmpty()) {
      int result = btDcKbPermissionManageMapper.batchDeletePermissions(permissionIdsToDelete, userId);
      if (result <= 0) {
        throw new BssException("批量删除权限失败");
      }
    }
  }

  /**
   * 验证批量成员权限设置合法性
   */
  private void validateBatchMembers(List<PermissionMemberDTO> members, KnowledgeBaseDTO knowledgeBaseDTO) {
    for (PermissionMemberDTO member : members) {
      validateKbOwner(knowledgeBaseDTO, member.getSubjectType(), member.getSubjectId());
      // 验证主体类型
      if (!isValidSubjectType(member.getSubjectType())) {
        throw new BssException("无效的主体类型: " + member.getSubjectType());
      }

      // 验证主体ID
      if (member.getSubjectId() == null) {
        throw new BssException("主体ID不能为空");
      }

      // 验证权限类型（对于添加和更新操作）
      if (member.getPermissionType() != null && !isValidPermissionType(member.getPermissionType())) {
        throw new BssException("无效的权限类型: " + member.getPermissionType());
      }
    }
  }

  /**
   * 验证主体类型是否合法
   */
  private boolean isValidSubjectType(String subjectType) {
    for (SubjectTypeEnum subjectTypeEnum : SubjectTypeEnum.values()) {
      if (subjectTypeEnum.getCode().equals(subjectType)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 验证权限类型是否合法
   */
  private boolean isValidPermissionType(String permissionType) {
    for (LibraryRoleEnum role : LibraryRoleEnum.values()) {
      if (role.getCode().equals(permissionType)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 验证批量操作类型
   */
  private void validateBatchActionType(KnowledgeBaseBatchPermissionRequestDTO request) {
    try {
      PermissionActionEnum action = PermissionActionEnum.valueOf(request.getAction());
      if (action != PermissionActionEnum.BATCH_ADD && action != PermissionActionEnum.BATCH_UPDATE
        && action != PermissionActionEnum.BATCH_REMOVE) {
        throw new BssException("不支持的批量操作类型: " + request.getAction());
      }
    }
    catch (IllegalArgumentException e) {
      throw new BssException("无效的批量操作类型: " + request.getAction(), e);
    }
  }

  /**
   * 验证文档库存在性和用户权限
   */
  private KnowledgeBaseDTO validateLibraryAndPermissions(Long knowledgeId, Long tenantId) {
    KnowledgeBaseDTO knowledgeBaseDTO = validateAndGetKnowledgeBase(tenantId, knowledgeId);
    String s = bthDcKbPermissionHelper.queryKnowledgeBasePermissionType(knowledgeBaseDTO.getKnowledgeId(), knowledgeBaseDTO.getOwnerId());
    if (!LibraryRoleEnum.MANAGE.getCode().equals(s)) {
      throw new BssException("权限不足，只有知识库所有者和管理者可以修改权限设置");
    }
    return knowledgeBaseDTO;
  }

  /**
   * 验证权限类型
   */
  private void validatePermissionType(KnowledgeBasePermissionRequestDTO request) {
    PermissionActionEnum action = PermissionActionEnum.valueOf(request.getAction());
    if (action != PermissionActionEnum.REMOVE_MEMBER) {
      if (StringUtils.isBlank(request.getData().getPermissionType())) {
        throw new BssException("权限类型不能为空");
      }
      boolean isValidPermission = false;
      for (LibraryRoleEnum role : LibraryRoleEnum.values()) {
        if (role.getCode().equals(request.getData().getPermissionType())) {
          isValidPermission = true;
          break;
        }
      }
      if (!isValidPermission) {
        throw new BssException("无效的权限类型: " + request.getData().getPermissionType());
      }
    }
  }

  /**
   * 执行权限操作
   */
  private void executePermissionAction(Long knowledgeId, KnowledgeBasePermissionRequestDTO request, Long userId) {
    try {
      PermissionActionEnum action = PermissionActionEnum.valueOf(request.getAction());
      processPermissionAction(knowledgeId, request, userId, action);
    }
    catch (Exception e) {
      throw new BssException("权限设置失败，请稍后重试: " + e.getMessage(), e);
    }
  }

  /**
   * 处理具体的权限操作
   */
  private void processPermissionAction(Long knowledgeId, KnowledgeBasePermissionRequestDTO request, Long userId,
                                       PermissionActionEnum action) {
    switch (action) {
      case ADD_MEMBER:
        addLibraryMember(knowledgeId, request.getData(), userId);
        break;
      case UPDATE_MEMBER:
        updateLibraryMember(knowledgeId, request.getData(), userId);
        break;
      case REMOVE_MEMBER:
        removeLibraryMember(knowledgeId, request.getData(), userId);
        break;
      default:
        throw new BssException("不支持的操作类型: " + action.getCode());
    }
  }

  /**
   * 添加文档库成员
   */
  private void addLibraryMember(Long knowledgeId, KnowledgeBasePermissionDataDTO data, Long userId) {
    // 检查权限主体是否已存在
    validateMemberNotExists(knowledgeId, data.getSubjectId(), data.getSubjectType());

    // 创建并保存权限记录
    BtDcKbPermissionDTO permission = createPermissionEntity(knowledgeId, data.getPermissionType(), data.getSubjectId(),
      data.getSubjectType(), userId);
    savePermissionEntity(permission, "添加成员失败");
  }

  /**
   * 更新文档库成员权限
   */
  private void updateLibraryMember(Long knowledgeId, KnowledgeBasePermissionDataDTO data, Long userId) {
    // 检查权限主体是否存在
    BtDcKbPermissionDTO existingPermission = validateMemberExists(knowledgeId, data);

    // 更新权限类型
    updatePermissionEntity(existingPermission, data, userId);
  }

  /**
   * 移除文档库成员
   */
  private void removeLibraryMember(Long knowledgeId, KnowledgeBasePermissionDataDTO data, Long userId) {
    // 检查权限主体是否存在
    BtDcKbPermissionDTO existingPermission = validateMemberExists(knowledgeId, data);
    existingPermission.setUpdatorId(userId);
    // 删除权限记录
    deletePermissionEntity(existingPermission);
  }

  /**
   * 验证成员不存在
   */
  private void validateMemberNotExists(Long knowledgeId, Long subjectId, String subjectType) {
    BtDcKbPermissionDTO existingPermission = btDcKbPermissionManageMapper.selectBtDcKbPermissionDTO(knowledgeId,
      subjectId, subjectType);

    if (existingPermission != null) {
      throw new BssException("权限主体已存在，无法重复添加: " + subjectType + "-" + subjectId);
    }
  }

  /**
   * 验证成员存在
   */
  private BtDcKbPermissionDTO validateMemberExists(Long knowledgeId, KnowledgeBasePermissionDataDTO data) {
    BtDcKbPermissionDTO existingPermission = btDcKbPermissionManageMapper.selectBtDcKbPermissionDTO(knowledgeId,
      data.getSubjectId(), data.getSubjectType());

    if (existingPermission == null) {
      throw new BssException("权限主体不存在，无法操作");
    }

    return existingPermission;
  }

  /**
   * 创建权限实体
   */
  private BtDcKbPermissionDTO createPermissionEntity(Long knowledgeId, String permissionType, Long subjectId,
                                                     String subjectType, Long currentUserId) {
    BtDcKbPermissionDTO permission = new BtDcKbPermissionDTO();
    permission.setPermissionId(DocSequences.BT_DC_KB_PERMISSION_ID.next());
    permission.setKbId(knowledgeId);
    permission.setGrantedBy(currentUserId);
    permission.setCreatorId(currentUserId);
    permission.setUpdatorId(currentUserId);
    permission.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    permission.setPermissionType(permissionType);
    permission.setSubjectId(subjectId);
    permission.setSubjectType(subjectType);
    return permission;
  }

  /**
   * 保存权限实体
   */
  private void savePermissionEntity(BtDcKbPermissionDTO permission, String errorMessage) {
    int result = btDcKbPermissionManageMapper.insertBtDcKbPermission(permission);
    if (result <= 0) {
      throw new BssException(errorMessage);
    }
  }

  /**
   * 更新权限实体
   */
  private void updatePermissionEntity(BtDcKbPermissionDTO permission, KnowledgeBasePermissionDataDTO data,
                                      Long userId) {
    permission.setPermissionType(data.getPermissionType());
    permission.setUpdatorId(userId);

    int result = btDcKbPermissionManageMapper.updateBtDcKbPermission(permission);
    if (result <= 0) {
      throw new BssException("更新成员权限失败");
    }
  }

  /**
   * 删除权限实体
   */
  private void deletePermissionEntity(BtDcKbPermissionDTO permission) {
    int result = btDcKbPermissionManageMapper.deleteByPrimaryKey(permission);
    if (result <= 0) {
      throw new BssException("移除成员失败");
    }
  }
}
