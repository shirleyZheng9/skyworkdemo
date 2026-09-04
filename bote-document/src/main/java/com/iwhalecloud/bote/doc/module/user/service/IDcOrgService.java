package com.iwhalecloud.bote.doc.module.user.service;

import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.model.OrgTreeDTO;
import java.util.List;

/**
 *
 * 门户的组织信息查询
 *
 * @author Aiqing
 * @since 2025/8/18
 */
public interface IDcOrgService {

  /**
   * 根据Id查询单个组织信息
   *
   * @param orgId 组织ID
   * @return 单个组织信息
   */
  OrgDTO findById(Long orgId);

  /**
   * 根据Id查询组织路径
   *
   * @param orgId 组织ID
   * @return 组织信息
   */
  List<OrgDTO> findOrgPathById(Long orgId);

  /**
   * 批量查询组织
   *
   * @param orgIdList 组织ID
   * @return 组织信息
   */
  List<OrgDTO> findBatchById(List<Long> orgIdList);


  /**
   * 查询用户所属的组织信息
   *
   * @param userId 用户ide
   * @return 组织信息
   */
  List<OrgDTO> queryUserOrgList(Long userId);

  /**
   * 查询用户所属的组织ID
   *
   * @param userId 用户ID
   * @return 组织ID
   */
  List<Long> queryUserOrgIdList(Long userId);


  /**
   * 查询所有组织机构信息
   *
   * @return 组织列表
   */
  List<OrgTreeDTO> queryAllOrgTreeList();

  /**
   * 获取组织子组织列表
   *
   * @param orgId 组织ID
   * @return 子组织列表
   */
  List<OrgDTO> getSubOrgList(Long orgId);

  /**
   * 根据关键词模糊搜索组织
   *
   * @param keyWord 搜索关键词（组织名称）
   * @param spaceId 企业空间ID
   * @return 组织信息列表
   */
  List<OrgDTO> searchOrgsByKeyword(String keyWord, Long spaceId);
}
