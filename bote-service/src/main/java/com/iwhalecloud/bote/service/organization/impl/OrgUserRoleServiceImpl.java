package com.iwhalecloud.bote.service.organization.impl;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.organization.OrgUserRoleDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationUserDTO;
import com.iwhalecloud.bote.dto.organization.query.OrgUserRoleQuery;
import com.iwhalecloud.bote.dto.portal.UserDTO;
import com.iwhalecloud.bote.entity.organization.OrgUserRoleEntity;
import com.iwhalecloud.bote.mapper.organization.OrgUserRoleMapper;
import com.iwhalecloud.bote.mapper.organization.OrganizationMemberMapper;
import com.iwhalecloud.bote.service.organization.IOrgUserRoleService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组织用户角色服务实现
 *
 * @author wangtingyun
 * @since 2025-10-30
 */
@Service
@RequiredArgsConstructor
public class OrgUserRoleServiceImpl implements IOrgUserRoleService {

  private final OrgUserRoleMapper orgUserRoleMapper;
  private final OrganizationMemberMapper orgMemberMapper;

  /** 组织角色编码-名称映射数据 */
  private static final Map<String, String> orgRoleMap = ImmutableMap.of("admin", "管理员", "member", "普通成员");

  @Override
  public PageInfo<OrgUserRoleDTO> queryOrgUserRolePage(OrgUserRoleQuery query) {
    // noinspection resource
    PageInfo<OrgUserRoleDTO> pageInfo = orgUserRoleMapper.selectOrgUserRolePage(query, query.buildRowBounds()).toPageInfo();
    List<OrgUserRoleDTO> orgUserRoleList = pageInfo.getList();
    if (CollectionUtils.isNotEmpty(orgUserRoleList)) {
      // 查询用户所属的全部组织信息
      List<Long> userIdList = orgUserRoleList.stream().map(OrgUserRoleDTO::getUserId).toList();
      List<OrganizationUserDTO> userOrgInfoList = orgMemberMapper.batchSelectUserOrgInfoList(query.getSpaceId(), userIdList);
      Map<Long, List<OrganizationUserDTO>> orgInfoMap = userOrgInfoList.stream().collect(Collectors.groupingBy(OrganizationUserDTO::getUserId));
      // 设置用户所属的全部组织名称
      for (OrgUserRoleDTO orgRoleDTO : orgUserRoleList) {
        List<OrganizationUserDTO> orgInfoList = orgInfoMap.get(orgRoleDTO.getUserId());
        if (CollectionUtils.isNotEmpty(orgInfoList)) {
          // 提取用户所属的全部组织名称，按上级到下级陈列出来
          List<String> orgNameList = orgInfoList.stream()
            .sorted(Comparator.comparing(OrganizationUserDTO::getOrgPath, Comparator.nullsFirst(Comparator.naturalOrder())))
            .map(OrganizationUserDTO::getOrgName).toList();
          orgRoleDTO.setUserOrgNames(String.join("、", orgNameList));
        }
      }
    }
    return pageInfo;
  }

  @Override
  public int countOrgUserRole(OrgUserRoleQuery query) {
    return orgUserRoleMapper.countOrgUserRole(query);
  }

  @Override
  @Transactional
  public void batchAddOrgUserRole(OrgUserRoleDTO roleDTO) {
    Assert.notNull(roleDTO.getSpaceId(), "企业空间ID不能为空");
    Assert.hasText(roleDTO.getOrgRole(), "组织角色不能为空");
    if (CollectionUtils.isEmpty(roleDTO.getAddRoleUserList())) {
      return;
    }
    List<OrgUserRoleEntity> entityList = new ArrayList<>();
    for (UserDTO user : roleDTO.getAddRoleUserList()) {
      // 检查用户是否已在空间下已有组织角色
      OrgUserRoleDTO oldRoleDTO = orgUserRoleMapper.selectOrgUserRole(roleDTO.getSpaceId(), user.getUserId());
      if (oldRoleDTO != null) {
        throw new BssException(String.format("用户【%s】已授权为【%s】角色，请勿重复授权", user.getRealName(), orgRoleMap.get(oldRoleDTO.getOrgRole())));
      }
      entityList.add(createRoleEntity(roleDTO.getSpaceId(), user.getUserId(), roleDTO.getOrgRole()));
    }
    if (!entityList.isEmpty()) {
      orgUserRoleMapper.insertOrgUserRoleBatch(entityList);
    }
  }

  @Override
  @Transactional
  public void delOrgUserRoleById(Long orgRoleId) {
    // 查询用户组织角色信息
    OrgUserRoleDTO userRoleDTO = orgUserRoleMapper.selectOrgUserRoleById(orgRoleId);
    if (userRoleDTO == null) {
      throw new BssException("组织用户角色不存在");
    }
    // 移除用户的组织角色
    orgUserRoleMapper.deleteOrgUserRoleById(orgRoleId);
    // 同步删除用户在企业下的全部组织成员
    orgMemberMapper.deleteUserAllOrgMember(userRoleDTO.getSpaceId(), userRoleDTO.getUserId(), SessionUtil.getLoginInfo().getUserId());
  }

  @Override
  @Transactional
  public void delOrgUserRole(Long spaceId, Long userId) {
    orgUserRoleMapper.deleteBySpaceIdAndUserId(spaceId, userId, SessionUtil.getLoginInfo().getUserId());
  }

  @Override
  @Transactional
  public void batchDelOrgUserRole(Long spaceId, List<Long> userIds) {
    orgUserRoleMapper.batchDeleteOrgUserRole(spaceId, userIds, SessionUtil.getLoginInfo().getUserId());
  }

  @Override
  @Transactional
  public void addUserOrgRole(Long spaceId, Long userId, String orgRole) {
    // 检查用户是否已在空间下已有组织角色
    OrgUserRoleDTO oldRoleDTO = orgUserRoleMapper.selectOrgUserRole(spaceId, userId);
    if (oldRoleDTO != null) {
      return;
    }
    // 添加组织用户角色
    OrgUserRoleEntity roleEntity = createRoleEntity(spaceId, userId, orgRole);
    orgUserRoleMapper.insertOrgUserRoleBatch(Collections.singletonList(roleEntity));
  }

  @Override
  @Transactional
  public void modifyUserOrgRole(OrgUserRoleDTO roleDTO) {
    orgUserRoleMapper.updateUserOrgRole(roleDTO);
  }

  /**
   * 构建组织用户角色实体
   */
  private OrgUserRoleEntity createRoleEntity(Long spaceId, Long userId, String orgRole) {
    OrgUserRoleEntity roleEntity = new OrgUserRoleEntity();
    Long creatorId = SessionUtil.getLoginInfo().getUserId();
    roleEntity.setOrgRoleId(Sequences.ORG_USER_ROLE_ID.next());
    roleEntity.setUserId(userId);
    roleEntity.setOrgRole(orgRole);
    roleEntity.setSpaceId(spaceId);
    roleEntity.setStatusCd(BaseConsts.STATUS_CD_VALID);
    roleEntity.setCreatorId(creatorId);
    roleEntity.setUpdatorId(creatorId);
    return roleEntity;
  }

}
