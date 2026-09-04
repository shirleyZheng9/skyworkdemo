package com.iwhalecloud.bote.doc.module.control.mapper;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.doc.module.control.model.NodeBaseInfoDTO;
import com.iwhalecloud.bote.doc.module.control.model.SimpleNodeInfo;
import com.iwhalecloud.bote.doc.module.control.vo.NodeInfoTreeVo;
import com.iwhalecloud.bote.doc.module.control.vo.NodeInfoVo;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentNodeTreeDTO;
import com.iwhalecloud.bote.doc.module.document.dto.request.DocFolderSubsQueryParams;
import com.iwhalecloud.bote.doc.module.document.entity.DcDocumentEntity;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 文档节点mapper类
 *
 * @author Aiqing
 * @since 2025/8/18
 */
public interface DocumentNodeMapper {

  /**
   * 查询有序节点树。
   *
   * @param nodeIds 节点ID列表
   * @param userId 用户ID
   * @return NodeInfoTreeVo
   */
  List<NodeInfoTreeVo> selectNodeInfoTreeByNodeIds(@Param("nodeIds") Collection<String> nodeIds,
                                                   @Param("userId") Long userId, @Param("tenantId") Long tenantId, @Param("envTenantId")  Long envTenantId);

  /**
   * 查询节点树DTO。
   *
   * @param parentIds 父节点ID列表
   * @return NodeTreeDTO列表
   */
  List<DocumentNodeTreeDTO> selectDocumentNodeTreeDTOByParentIdIn(@Param("parentIds") Collection<String> parentIds);

  /**
   * 根据节点ID列表查询节点基础信息。
   *
   * @param nodeIds 节点ID集合
   * @return 节点基础信息DTO列表
   */
  List<NodeBaseInfoDTO> selectNodeBaseInfosByNodeIds(@Param("nodeIds") Set<String> nodeIds);


  /**
   * 根据节点ID列表查询节点基础信息
   *
   * @param nodeId 节点ID
   * @return 节点基础信息
   */
  NodeBaseInfoDTO selectNodeBaseInfoByNodeId(@Param("nodeId") String nodeId);

  /**
   * 根据库ID查询根节点ID。
   *
   * @param libraryId 库ID
   * @return 根节点ID
   */
  String selectRootNodeIdByLibraryId(@Param("libraryId") String libraryId, @Param("tenantId")Long tenantId, @Param("spaceId")Long spaceId);

  /**
   * 根据文档节点ID
   *
   * @param nodeIds 文档节点ID集合
   * @param userId 用户ID
   * @return 节点信息
   */
  List<NodeInfoVo> selectNodeInfoByNodeIds(@Param("nodeIds") Set<String> nodeIds, @Param("userId") Long userId);

  /**
   * 根据文档节点ID 查询节点信息
   */
  NodeInfoVo selectNodeInfoByNodeId(@Param("nodeId") String nodeId);

  /**
   * 查询文档节点的权限设置状态
   *
   * @param nodeId 文档节点ID
   * @return 节点信息
   */
  SimpleNodeInfo selectNodeInfoWithPermissionStatus(@Param("nodeId") String nodeId);

  void updatePreNodeIdByJoinSelf(@Param("preNodeId") String preNodeId, @Param("parentId") String parentId);

  /**
   * 查询后一个节点ID
   *
   * @param nodeId 节点ID
   * @return 紧挨的后一个节点ID
   */
  String selectNodeIdByPreNodeId(@Param("nodeId") String nodeId);

  /**
   * 修改前置节点ID
   *
   * @param newPreNodeId 新的前置ID
   * @param originPreNodeId 原始的前置ID
   * @param parentId 父节点ID
   * @return 更新行数
   */
  int updatePreNodeIdBySelf(@Param("newPreNodeId") String newPreNodeId,
                            @Param("originPreNodeId") String originPreNodeId,
                            @Param("parentId") String parentId);

  /**
   * 根据preNodeId查询同级的节点
   *
   * @param parentId 父ID
   * @param preNodeId 前置节点
   * @return 节点ID
   */
  String selectNodeIdByParentIdAndPreNodeId(@Param("parentId") String parentId, @Param("preNodeId") String preNodeId);

  /**
   * 更新节点的前置节点
   *
   * @param newPreNodeId 新的前置节点
   * @param nodeId 节点ID
   */
  void updatePreNodeIdByNodeId(@Param("newPreNodeId") String newPreNodeId, @Param("nodeId") String nodeId);

  /**
   * 跟新节点的前置节点和父节点
   *
   * @param nodeId 节点ID
   * @param parentId 新父节点ID
   * @param newPreNodeId 新前置节点ID
   */
  void updateInfoByNodeId(@Param("nodeId") String nodeId, @Param("parentId") String parentId,
                          @Param("newPreNodeId") String newPreNodeId, @Param("userId") Long userId);

  /**
   * 根据文档库ID查询所有文档节点ID
   *
   * @param libraryId 文档库ID
   * @return 文档节点ID列表
   */
  List<String> selectWithoutRootByLibraryId(@Param("libraryId") String libraryId);

  /**
   * 搜索文档库内文档节点
   *
   * @param libraryId 文档库
   * @param keyword 搜索名称
   * @return 节点ID集合
   */
  List<NodeBaseInfoDTO> searchByNodeName(@Param("libraryId") String libraryId,
                                         @Param("keyword") String keyword,
                                         @Param("limit") int limit);

  DcDocumentEntity selectRootNodeIdDcDocumentEntityByLibraryId(@Param("libraryId") String libraryId, @Param("tenantId")Long tenantId, @Param("spaceId")Long spaceId);

  List<String> selectNodeSubIdsByParentId(@Param("nodeId") String nodeId);

  /**
   * 分页查询文件夹下的子节点信息
   *
   * @param params    查询参数
   * @param userId    当前用户ID
   * @param rowBounds 分页参数
   * @return 子节点分页列表
   */
  Page<NodeInfoTreeVo> selectNodeInfoTreePageByNodeId(@Param("query") DocFolderSubsQueryParams params,
                                                        @Param("userId") Long userId, RowBounds rowBounds);

  /**
   * 统计父节点下的文件夹数量
   *
   * @param parentId 父节点ID
   * @return 文件夹数量
   */
  Long countFolderByParentId(@Param("parentId") String parentId);

  /**
   * 统计父节点下的文档数量（不含文件夹）
   *
   * @param parentId 父节点ID
   * @return 文档数量
   */
  Long countDocByParentId(@Param("parentId") String parentId);
}
