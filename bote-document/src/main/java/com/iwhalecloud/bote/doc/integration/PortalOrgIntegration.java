package com.iwhalecloud.bote.doc.integration;

import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.model.OrgTreeDTO;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * 门户的组织信息查询
 *
 * @author Aiqing
 * @since 2025/8/18
 */
public interface PortalOrgIntegration {

  /**
   * 根据Id查询单个组织信息
   *
   * @param spaceId 企业空间ID
   * @param orgId 组织ID
   * @return 单个组织信息
   */
  OrgDTO findById(Long spaceId, Long orgId);

  /**
   * 批量查询组织
   *
   * @param spaceId 企业空间ID
   * @param orgIdList 组织ID
   * @return 组织信息
   */
  List<OrgDTO> findBatchById(Long spaceId, List<Long> orgIdList);


  /**
   * 查询用户所属的组织信息
   *
   * @param spaceId 企业空间ID
   * @param userId 用户ID
   * @return 组织信息
   */
  List<OrgDTO> queryUserOrgList(Long spaceId, Long userId);

  /**
   * 批量查询用户所属的组织信息
   *
   * @param spaceId 企业空间ID
   * @param userIds 用户ID列表
   * @return 用户ID到组织信息列表的映射
   */
  Map<Long, List<OrgDTO>> queryUserOrgListBatch(Long spaceId, List<Long> userIds);


  /**
   * 查询所有组织机构信息
   *
   * @param spaceId 企业空间ID
   * @return 组织列表
   */
  List<OrgTreeDTO> queryAllOrgTreeList(Long spaceId);

  /**
   * 获取组织子组织列表
   *
   * @param spaceId 企业空间ID
   * @param orgId 组织ID
   * @return 子组织列表
   */
  List<OrgDTO> getSubOrgList(Long spaceId, Long orgId);

  /**
   * 根据关键词模糊搜索组织
   *
   * @param keyWord 搜索关键词（组织名称）
   * @param spaceId 企业空间ID
   * @return 组织信息列表
   */
  List<OrgDTO> searchOrgsByKeyword(String keyWord, Long spaceId);

  /**
   * 根据组织路径查询组织路径上的所有组织
   *
   * @param spaceId 企业空间ID
   * @param orgPath 组织路径（用.分割的组织ID路径）
   * @return 组织列表（按路径顺序排序，从根到叶子）
   */
  List<OrgDTO> queryOrganizationsByPath(Long spaceId, String orgPath);

  /**
   * 根据组织ID列表查询组织成员ID列表（包含子组织）
   *
   * @param spaceId 企业空间ID
   * @param orgIds 组织ID列表
   * @return 组织成员ID列表
   */
  Set<Long> queryUserIdsByOrgIdsWithSubOrgs(Long spaceId, List<Long> orgIds);
}
