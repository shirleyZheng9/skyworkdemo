package com.iwhalecloud.bote.doc.module.control.service;

import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.DocRoleEnum;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRole;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRoleManager;
import com.iwhalecloud.bote.doc.module.control.dto.RolePermissionConfigDTO;
import com.iwhalecloud.bote.doc.module.control.vo.NodePermissionView;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 角色权限配置服务
 *
 * <p>提供统一的角色权限配置管理，避免在每个节点中重复返回权限对象</p>
 *
 * @author Aiqing
 * @since 2025-08-25
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class RolePermissionConfigService {

  private static final Logger logger = LoggerFactory.getLogger(RolePermissionConfigService.class);

  private static final String CONFIG_VERSION = "v1.0";

  /**
   * 获取所有角色的权限配置
   *
   * @return 角色权限配置
   */
  public RolePermissionConfigDTO getAllRolePermissions() {
    Map<String, NodePermissionView> rolePermissions = new HashMap<>();

    // 添加基础文档角色权限
    addDocumentRolePermissions(rolePermissions);

    RolePermissionConfigDTO config = new RolePermissionConfigDTO();
    config.setRolePermissions(rolePermissions);
    config.setVersion(CONFIG_VERSION);
    config.setTimestamp(System.currentTimeMillis());

    logger.trace("生成角色权限配置，包含 {} 个角色", rolePermissions.size());
    return config;
  }

  /**
   * 添加基础文档角色权限
   */
  private void addDocumentRolePermissions(Map<String, NodePermissionView> rolePermissions) {
    // 匿名角色
    ControlRole anonymousRole = ControlRoleManager.parseNodeRole(DocRoleEnum.ANONYMOUS.getCode());
    rolePermissions.put(anonymousRole.getRoleTag(),
      anonymousRole.permissionToBean(NodePermissionView.class));

    // 只读角色
    ControlRole readerRole = ControlRoleManager.parseNodeRole(DocRoleEnum.DOC_READ.getCode());
    rolePermissions.put(readerRole.getRoleTag(),
      readerRole.permissionToBean(NodePermissionView.class));

    // 下载角色
    ControlRole downloaderRole = ControlRoleManager.parseNodeRole(DocRoleEnum.DOWNLOAD.getCode());
    rolePermissions.put(downloaderRole.getRoleTag(),
      downloaderRole.permissionToBean(NodePermissionView.class));

    // 修订角色
    ControlRole correctionRole = ControlRoleManager.parseNodeRole(DocRoleEnum.DOC_CORRECTION.getCode());
    rolePermissions.put(correctionRole.getRoleTag(),
      correctionRole.permissionToBean(NodePermissionView.class));

    // 编辑角色
    ControlRole editorRole = ControlRoleManager.parseNodeRole(DocRoleEnum.DOC_EDIT.getCode());
    rolePermissions.put(editorRole.getRoleTag(),
      editorRole.permissionToBean(NodePermissionView.class));

    // 管理角色
    ControlRole managerRole = ControlRoleManager.parseNodeRole(DocRoleEnum.DOC_MANAGE.getCode());
    rolePermissions.put(managerRole.getRoleTag(),
      managerRole.permissionToBean(NodePermissionView.class));

    // 文档所有者（创建人）
    ControlRole ownerRole = ControlRoleManager.parseNodeRole(DocRoleEnum.DOC_OWNER.getCode());
    rolePermissions.put(ownerRole.getRoleTag(),
      ownerRole.permissionToBean(NodePermissionView.class));
  }

  /**
   * 获取特定角色的权限配置
   *
   * @param roleTag 角色标识
   * @return 权限配置，如果角色不存在返回null
   */
  public NodePermissionView getRolePermissions(String roleTag) {
    try {
      // 尝试解析基础文档角色
      ControlRole role = ControlRoleManager.parseNodeRole(roleTag);
      return role.permissionToBean(NodePermissionView.class);
    }
    catch (Exception e) {
      logger.warn("无法解析角色权限: {}", roleTag, e);
      return null;
    }
  }
}
