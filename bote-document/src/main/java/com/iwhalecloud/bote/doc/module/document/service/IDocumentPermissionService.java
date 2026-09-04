package com.iwhalecloud.bote.doc.module.document.service;

import com.iwhalecloud.bote.doc.consts.PermissionActionEnum;
import com.iwhalecloud.bote.doc.module.control.base.SubjectBuilder.ControlSubject;
import com.iwhalecloud.bote.doc.module.control.model.ControlRoleInfo;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPermissionDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPermissionDetailDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPermissionUpdateResultDTO;
import com.iwhalecloud.bote.doc.module.document.dto.PermissionDTO;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 *
 * 文档权限控制相关服务方法
 *
 * @author Aiqing
 * @since 2025/8/15
 */
public interface IDocumentPermissionService {


  /**
   * 查询节点的角色配置
   *
   * @param controlIds 节点ID集合
   * @return 节点角色配置
   */
  List<ControlRoleInfo> selectControlDocumentRoleInfoByControlIds(@Param("controlIds") List<String> controlIds);


  List<DocumentPermissionDTO> selectByDocumentId(String documentId);


  /**
   * 查询数据库存在的分配过权限的文档ID
   *
   * @param documentIds 文档ID集合
   * @return 存在的文档ID集合
   */
  List<String> selectExistedControlDocumentId(List<String> documentIds);

  /**
   * 增加文档权限角色
   *
   * @param userId 用户ID
   * @param documentId 文档ID
   * @param subjectRoleMap 角色映射关系
   */
  void addDocumentControlPermission(Long userId, String documentId, Map<ControlSubject, String> subjectRoleMap);

  /**
   * 更新文档权限
   *
   * @param action 动作
   * @param permission 权限数据
   * @param documentId 文档ID
   */
  void updatePermission(PermissionActionEnum action, PermissionDTO permission, String documentId);

  /**
   * 批量更新文档权限
   *
   * @param action 动作
   * @param permissionList 权限集合
   * @param documentId 文档ID
   */
  DocumentPermissionUpdateResultDTO updatePermissionBatch(PermissionActionEnum action, List<PermissionDTO> permissionList, String documentId);

  /**
   * 查询文档的所有权限配置
   *
   * @param documentId 文档ID
   * @return 权限配置信息
   */
  DocumentPermissionDetailDTO queryDocumentPermissionSetWithInherit(String documentId);

  /**
   * 查询某个用户是否有文档库下文档权限
   *
   * @param libraryId 文档库ID
   * @param userId 用户ID
   */
  boolean existUserPermissionByLibraryId(String libraryId, Long userId);

  /**
   * 恢复继承权限模式
   * 删除继承权限转换的独立权限，并将文档权限模式恢复为INHERIT
   *
   * @param documentId 文档ID
   */
  void restoreInheritPermissionMode(String documentId);
}
