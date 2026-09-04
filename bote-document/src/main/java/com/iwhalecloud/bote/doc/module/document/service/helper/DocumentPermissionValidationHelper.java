package com.iwhalecloud.bote.doc.module.document.service.helper;

import com.iwhalecloud.bote.doc.consts.PermissionActionEnum;
import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.DocRoleEnum;
import com.iwhalecloud.bote.doc.consts.SubjectTypeEnum;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRole;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRoleManager;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPermissionSetDTO;
import com.iwhalecloud.bote.doc.module.document.dto.PermissionDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import org.springframework.stereotype.Component;

import static com.iwhalecloud.bote.doc.consts.PermissionActionEnum.BATCH_REMOVE;
import static com.iwhalecloud.bote.doc.consts.PermissionActionEnum.BATCH_UPDATE;
import static com.iwhalecloud.bote.doc.consts.PermissionActionEnum.REMOVE_MEMBER;
import static com.iwhalecloud.bote.doc.consts.PermissionActionEnum.UPDATE_MEMBER;

/**
 * 文档权限校验助手类
 * 负责文档权限相关的校验逻辑
 *
 * @author Aiqing
 * @since 2025/11/5
 */
@Component
public class DocumentPermissionValidationHelper {

  /**
   * 校验不能修改自己的权限
   *
   * @param permissionList 权限列表
   * @param currentLoginUserId 当前登录用户ID
   * @throws BssException 如果尝试修改自己的权限
   */
  public void validateNotSelfModification(List<PermissionDTO> permissionList, Long currentLoginUserId) {
    boolean hasSelfModification = permissionList.stream()
      .anyMatch(p -> SubjectTypeEnum.USER.getCode().equals(p.getSubjectType())
        && Objects.equals(p.getSubjectId(), currentLoginUserId));

    if (hasSelfModification) {
      throw new BssException("不能修改自己的权限配置");
    }
  }

  /**
   * 过滤无效的权限变更
   *
   * @param permissionList 权限列表
   * @param action 操作类型
   * @param currentLoginUserId 当前登录用户ID
   * @param controlRole 当前用户角色
   * @param existPermissionSets 已存在的权限配置
   * @param ignoreWhenNoPermission 是否忽略无权限的情况
   */
  public void filterInvalidPermissions(List<PermissionDTO> permissionList,
                                       PermissionActionEnum action,
                                       Long currentLoginUserId,
                                       ControlRole controlRole,
                                       List<DocumentPermissionSetDTO> existPermissionSets,
                                       boolean ignoreWhenNoPermission) {
    // 创建权限校验器
    PermissionValidator validator = createValidator(action, currentLoginUserId, controlRole, existPermissionSets);

    // 使用removeIf来过滤无效权限
    permissionList.removeIf(permission -> {
      PermissionValidationResult result = validator.validate(permission);
      // 校验通过
      if (result.isValid()) {
        return false;
      }
      // 如果不忽略无权限的情况，抛出异常
      if (!ignoreWhenNoPermission) {
        throw new BssException(result.getErrorMessage());
      }
      // 否则过滤掉该权限变更
      return true;
    });
  }


  private boolean checkPermissionEditable(Long currentLoginUserId,
                                          PermissionDTO permission,
                                          List<DocumentPermissionSetDTO> existPermissionSets,
                                          String currUserRole) {
    Optional<DocumentPermissionSetDTO> existPermissionOptional = existPermissionSets.stream()
      .filter(item -> {
        return Objects.equals(item.getSubjectId(), permission.getSubjectId())
          && Objects.equals(item.getSubjectType(), permission.getSubjectType());
      }).findAny();
    if (existPermissionOptional.isPresent()) {
      DocumentPermissionSetDTO perm = existPermissionOptional.get();
      // 校验是否可以编辑
      boolean permissionEditable = this.isPermissionEditable(perm, currentLoginUserId, currUserRole);
      if (!permissionEditable) {
        return false;
      }
      // 校验能够编辑指定哪些权限等级
      List<String> roleSet = perm.getRoleSet();
      return roleSet.contains(permission.getPermissionType());
    }
    return true;
  }

  private boolean checkPermissionRemovable(Long currentLoginUserId,
                                           PermissionDTO permission,
                                           List<DocumentPermissionSetDTO> existPermissionSets,
                                           String currUserRole) {
    Optional<DocumentPermissionSetDTO> existPermissionOptional = existPermissionSets.stream()
      .filter(item -> {
        return Objects.equals(item.getSubjectId(), permission.getSubjectId())
          && Objects.equals(item.getSubjectType(), permission.getSubjectType());
      }).findAny();
    return existPermissionOptional.map(documentPermissionSetDTO ->
        this.isPermissionRemovable(documentPermissionSetDTO, currentLoginUserId, currUserRole))
      .orElse(true);
  }

  /**
   * 根据操作类型创建对应的校验器
   */
  private PermissionValidator createValidator(PermissionActionEnum action,
                                              Long currentLoginUserId,
                                              ControlRole controlRole,
                                              List<DocumentPermissionSetDTO> existPermissionSets) {
    String currUserRole = controlRole.getRoleTag();

    if (isRemoveAction(action)) {
      return permission -> validateRemovePermission(currentLoginUserId, permission,
        existPermissionSets, currUserRole);
    }
    else if (isUpdateAction(action)) {
      return permission -> validateUpdatePermission(currentLoginUserId, permission,
        existPermissionSets, currUserRole);
    }
    else {
      return permission -> validateAddPermission(controlRole, permission);
    }
  }

  /**
   * 判断是否为移除操作
   */
  public boolean isRemoveAction(PermissionActionEnum action) {
    return action == REMOVE_MEMBER || action == BATCH_REMOVE;
  }

  /**
   * 判断是否为更新操作
   */
  public boolean isUpdateAction(PermissionActionEnum action) {
    return action == UPDATE_MEMBER || action == BATCH_UPDATE;
  }

  /**
   * 校验移除权限
   */
  private PermissionValidationResult validateRemovePermission(Long currentLoginUserId,
                                                              PermissionDTO permission,
                                                              List<DocumentPermissionSetDTO> existPermissionSets,
                                                              String currUserRole) {
    boolean removable = checkPermissionRemovable(currentLoginUserId, permission, existPermissionSets, currUserRole);
    return removable
      ? PermissionValidationResult.success()
      : PermissionValidationResult.failure("无权限移除此权限记录");
  }

  /**
   * 校验更新权限
   */
  private PermissionValidationResult validateUpdatePermission(Long currentLoginUserId,
                                                              PermissionDTO permission,
                                                              List<DocumentPermissionSetDTO> existPermissionSets,
                                                              String currUserRole) {
    boolean editable = checkPermissionEditable(currentLoginUserId, permission, existPermissionSets, currUserRole);
    return editable
      ? PermissionValidationResult.success()
      : PermissionValidationResult.failure("权限不足, 无法设置此访问权限");
  }

  /**
   * 校验添加权限
   */
  private PermissionValidationResult validateAddPermission(ControlRole controlRole, PermissionDTO permission) {
    String permissionType = permission.getPermissionType();
    ControlRole roleSet = ControlRoleManager.parseNodeRole(permissionType);

    if (controlRole.isLessThan(roleSet)) {
      return PermissionValidationResult.failure("权限不足, 无法设置此访问权限");
    }

    return PermissionValidationResult.success();
  }

  /**
   * 判断权限是否可编辑
   */
  public boolean isPermissionEditable(DocumentPermissionSetDTO permission,
                                      Long currentUserId,
                                      String roleTag) {
    if (Boolean.TRUE.equals(permission.getOwner())) {
      return false;
    }
    // 当前用户是权限主体本身, 不可编辑
    if (Objects.equals(currentUserId, permission.getSubjectId())
      && Objects.equals(permission.getSubjectType(), SubjectTypeEnum.USER.getCode())) {
      return false;
    }
    int currUserRoleLevel = DocRoleEnum.getByCode(roleTag).getLevel();
    int dataRoleLevel = DocRoleEnum.getByCode(permission.getPermissionType()).getLevel();

    // 有管理权限可编辑，需要当前记录是非管理角色
    if (Objects.equals(roleTag, DocRoleEnum.DOC_MANAGE.getCode())) {
      return true;
    }
    // 当前用户权限等级超过或等于记录权限，可编辑
    return currUserRoleLevel <= dataRoleLevel;
  }

  public boolean isPermissionRemovable(DocumentPermissionSetDTO permission,
                                       Long currentUserId,
                                       String roleTag) {
    // 文档库所有者有移除权限
    if (Boolean.TRUE.equals(permission.getOwner())) {
      return false;
    }
    if (Objects.equals(roleTag, DocRoleEnum.DOC_MANAGE.getCode())) {
      return true;
    }
    // 当前用户是权限主体本身, 不可移除
    if (Objects.equals(currentUserId, permission.getSubjectId())
      && Objects.equals(permission.getSubjectType(), SubjectTypeEnum.USER.getCode())) {
      return false;
    }
    // 继承自上层目录的非管理员不能移除
    if (Boolean.TRUE.equals(permission.getInherit())) {
      return false;
    }

    int currUserRoleLevel = DocRoleEnum.getByCode(roleTag).getLevel();
    int dataRoleLevel = DocRoleEnum.getByCode(permission.getPermissionType()).getLevel();

    // 只能移除自己授权的数据, 且权限不能高过自己
    return Objects.equals(currentUserId, permission.getGrantedBy()) && currUserRoleLevel <= dataRoleLevel;
  }

  /**
   * 权限校验器函数式接口
   */
  @FunctionalInterface
  private interface PermissionValidator {
    PermissionValidationResult validate(PermissionDTO permission);
  }

  /**
   * 权限校验结果
   */
  @Getter
  public static final class PermissionValidationResult {
    private final boolean valid;
    private final String errorMessage;

    private PermissionValidationResult(boolean valid, String errorMessage) {
      this.valid = valid;
      this.errorMessage = errorMessage;
    }

    public static PermissionValidationResult success() {
      return new PermissionValidationResult(true, null);
    }

    public static PermissionValidationResult failure(String errorMessage) {
      return new PermissionValidationResult(false, errorMessage);
    }
  }
}

