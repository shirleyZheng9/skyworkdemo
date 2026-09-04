package com.iwhalecloud.bote.service.organization.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.cache.AttrSpecCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.OrganizationConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.ExcelUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.SimpleAttrDTO;
import com.iwhalecloud.bote.dto.organization.BatchAddOrganizationMembersResult;
import com.iwhalecloud.bote.dto.organization.OrganizationDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationMemberDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationMemberImportDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationUserDTO;
import com.iwhalecloud.bote.dto.organization.SimpleOrganizationDTO;
import com.iwhalecloud.bote.dto.organization.query.OrganizationMemberQueryParams;
import com.iwhalecloud.bote.dto.organization.request.BatchAddOrganizationMembersRequest;
import com.iwhalecloud.bote.entity.organization.OrganizationMemberEntity;
import com.iwhalecloud.bote.entity.portal.UserEntity;
import com.iwhalecloud.bote.mapper.organization.OrganizationManageMapper;
import com.iwhalecloud.bote.mapper.organization.OrganizationMemberMapper;
import com.iwhalecloud.bote.mapper.portal.TenantManageMapper;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import com.iwhalecloud.bote.service.organization.IOrgUserRoleService;
import com.iwhalecloud.bote.service.organization.IOrganizationMemberService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

/**
 * 组织成员管理服务实现类
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class OrganizationMemberServiceImpl implements IOrganizationMemberService {

  private static final Logger logger = LoggerFactory.getLogger(OrganizationMemberServiceImpl.class);
  private static final int INITIAL_ROW = 2;
  private static final int BATCH_SIZE = 100;

  private final OrganizationMemberMapper organizationMemberMapper;
  private final OrganizationManageMapper organizationManageMapper;
  private final UserManageMapper userManageMapper;
  private final AttrSpecCache attrSpecCache;
  private final TenantManageMapper tenantManageMapper;
  private final IOrgUserRoleService orgUserRoleService;

  @Override
  @Transactional
  public ResultVO<OrganizationMemberDTO> addOrganizationMember(OrganizationMemberDTO member) {
      // 参数校验
      Assert.notNull(member, "成员信息不能为空");
      Assert.notNull(member.getSpaceId(), "企业空间ID不能为空");
      Assert.notNull(member.getOrgId(), "组织ID不能为空");
      Assert.notNull(member.getUserId(), "用户ID不能为空");
      // 检查用户是否已是组织成员
      if (checkMemberExists(member.getSpaceId(), member.getOrgId(), member.getUserId())) {
        throw new BssException(OrganizationConsts.ERROR_MEMBER_EXISTS);
      }
      // 设置默认值
      setDefaultValues(member);
    // 生成主键ID
    if (member.getMemberId() == null) {
      member.setMemberId(Sequences.ORGANIZATION_MEMBER_ID.next());
    }
    // 转换为Entity进行插入
    OrganizationMemberEntity entity = convertToEntity(member);
    entity.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    int result = organizationMemberMapper.insertOrganizationMember(entity);
    if (result <= 0) {
      throw new BssException("添加组织成员失败");
    }
    // 返回插入后的数据
    member.setMemberId(entity.getMemberId());
      return ResultVO.success(member);
  }

  @Override
  @Transactional
  public ResultVO<BatchAddOrganizationMembersResult> batchAddOrganizationMembers(BatchAddOrganizationMembersRequest request) {
    // 参数校验
    Assert.notNull(request, "批量添加请求不能为空");
    Assert.notNull(request.getSpaceId(), "企业空间ID不能为空");
    Assert.notNull(request.getOrgId(), "组织ID不能为空");
    Assert.notEmpty(request.getUserIds(), "用户ID列表不能为空");
    BatchAddOrganizationMembersResult result = new BatchAddOrganizationMembersResult();
    result.setTotalCount(request.getUserIds().size());
    result.setSuccessMembers(new ArrayList<>());
    result.setFailedUserIds(new ArrayList<>());
    result.setFailedDetails(new ArrayList<>());
    // 去重用户ID列表
    Set<Long> uniqueUserIds = new HashSet<>(request.getUserIds());
    for (Long userId : uniqueUserIds) {
      try {
        // 检查用户是否已是组织成员
        if (checkMemberExists(request.getSpaceId(), request.getOrgId(), userId)) {
          result.getFailedUserIds().add(userId);
          BatchAddOrganizationMembersResult.FailedMemberInfo failedInfo = new BatchAddOrganizationMembersResult.FailedMemberInfo();
          failedInfo.setUserId(userId);
          failedInfo.setReason("用户已是组织成员");
          result.getFailedDetails().add(failedInfo);
          continue;
        }
        // 创建成员DTO
        OrganizationMemberDTO member = new OrganizationMemberDTO();
        member.setSpaceId(request.getSpaceId());
        member.setOrgId(request.getOrgId());
        member.setUserId(userId);
        member.setMemberRole(request.getDefaultMemberRole());
        member.setMemberType(request.getDefaultMemberType());
        member.setDirectManagerId(request.getDefaultDirectManagerId());
        member.setRemark(request.getRemark());
        // 设置默认值
        setDefaultValues(member);
        // 生成主键ID
        member.setMemberId(Sequences.ORGANIZATION_MEMBER_ID.next());
        // 转换为Entity进行插入
        OrganizationMemberEntity entity = convertToEntity(member);
        entity.setCreatorId(SessionUtil.getLoginInfo().getUserId());
        int insertResult = organizationMemberMapper.insertOrganizationMember(entity);
        if (insertResult > 0) {
          member.setMemberId(entity.getMemberId());
          result.getSuccessMembers().add(member);
        }
        else {
          result.getFailedUserIds().add(userId);
          BatchAddOrganizationMembersResult.FailedMemberInfo failedInfo = new BatchAddOrganizationMembersResult.FailedMemberInfo();
          failedInfo.setUserId(userId);
          failedInfo.setReason("数据库插入失败");
          result.getFailedDetails().add(failedInfo);
        }
        // 检查并授权用户为普通成员角色
        orgUserRoleService.addUserOrgRole(request.getSpaceId(), userId, BaseConsts.ORG_ROLE_MEMBER);
      }
      catch (Exception e) {
        // 已成功新增的数据事务不回滚,失败的记录信息返回
        logger.error("批量添加组织成员失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
        result.getFailedUserIds().add(userId);
        BatchAddOrganizationMembersResult.FailedMemberInfo failedInfo = new BatchAddOrganizationMembersResult.FailedMemberInfo();
        failedInfo.setUserId(userId);
        failedInfo.setReason("添加失败: " + e.getMessage());
        result.getFailedDetails().add(failedInfo);
      }
    }
    result.setSuccessCount(result.getSuccessMembers().size());
    result.setFailCount(result.getFailedUserIds().size());
    return ResultVO.success(result);
  }

  @Override
  @Transactional
  public ResultVO<OrganizationMemberDTO> updateOrganizationMember(OrganizationMemberDTO member) {
      // 参数校验
      Assert.notNull(member, "成员信息不能为空");
      Assert.notNull(member.getMemberId(), "成员ID不能为空");
      // 查询原始数据
      OrganizationMemberDTO originalMember = findOrganizationMember(member.getSpaceId(), member.getMemberId());
      if (originalMember == null) {
        throw new BssException(OrganizationConsts.ERROR_MEMBER_NOT_EXISTS);
      }
      if (member.getOrgId() != null && !member.getOrgId().equals(originalMember.getOrgId())) {
        OrganizationDTO organization = organizationManageMapper.getOrganization(member.getSpaceId(), member.getOrgId());
        Assert.notNull(organization, OrganizationConsts.ERROR_ORG_NOT_EXISTS);
      }
      member.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
      int result = organizationMemberMapper.updateOrganizationMember(member);
      if (result <= 0) {
        throw new BssException("更新组织成员失败");
      }
      return ResultVO.success(member);
  }

  private void setDefaultValues(OrganizationMemberDTO member) {
    if (StringUtils.isBlank(member.getMemberRole())) {
      member.setMemberRole(OrganizationConsts.MEMBER_ROLE_MEMBER);
    }
    if (StringUtils.isBlank(member.getMemberType())) {
      member.setMemberType(OrganizationConsts.MEMBER_TYPE_REGULAR);
    }
    if (StringUtils.isBlank(member.getStatusCd())) {
      member.setStatusCd(BaseConsts.STATUS_CD_VALID);
    }
    if (member.getJoinDate() == null) {
      member.setJoinDate(new Date());
    }
  }

  private OrganizationMemberEntity convertToEntity(OrganizationMemberDTO dto) {
    OrganizationMemberEntity entity = new OrganizationMemberEntity();
    entity.setMemberId(dto.getMemberId());
    entity.setUserId(dto.getUserId());
    entity.setOrgId(dto.getOrgId());
    entity.setSpaceId(dto.getSpaceId());
    entity.setMemberRole(dto.getMemberRole());
    entity.setMemberType(dto.getMemberType());
    entity.setJoinDate(dto.getJoinDate());
    entity.setLeaveDate(dto.getLeaveDate());
    entity.setDirectManagerId(dto.getDirectManagerId());
    entity.setStatusCd(dto.getStatusCd());
    // 处理权限列表
    if (dto.getPermissions() != null && !dto.getPermissions().isEmpty()) {
      entity.setPermissions(JsonUtil.toJsonString(dto.getPermissionList()));
    }
    // 处理扩展字段
    if (dto.getExtFieldsMap() != null && !dto.getExtFieldsMap().isEmpty()) {
      entity.setExtFields(JsonUtil.toJsonString(dto.getExtFieldsMap()));
    }
    return entity;
  }

  @Override
  @Nullable
  public OrganizationMemberDTO findOrganizationMember(Long spaceId, Long memberId) {
    return organizationMemberMapper.getOrganizationMember(spaceId, memberId);
  }

  @Override
  @Nullable
  public OrganizationMemberDTO findMemberByOrgAndUser(Long spaceId, Long orgId, Long userId) {
    return organizationMemberMapper.getOrganizationMemberByOrgAndUser(spaceId, orgId, userId);
  }

  @Override
  public List<OrganizationMemberDTO> queryOrganizationMemberList(OrganizationMemberQueryParams queryParams) {
    return organizationMemberMapper.selectOrganizationMemberList(queryParams);
  }

  @Override
  public PageInfo<OrganizationMemberDTO> queryOrganizationMemberPage(OrganizationMemberQueryParams queryParams) {
    //noinspection resource
    PageHelper.startPage(queryParams.getPageNum(), queryParams.getPageSize());
    List<OrganizationMemberDTO> result = organizationMemberMapper.selectOrganizationMemberList(queryParams);
    return new PageInfo<>(result);
  }

  @Override
  public List<OrganizationMemberDTO> queryUserOrganizations(Long spaceId, Long userId) {
    return organizationMemberMapper.selectUserOrganizations(spaceId, userId);
  }

  @Override
  public List<OrganizationMemberDTO> queryUserOrganizationsBatch(Long spaceId, List<Long> userIds) {
    if (CollectionUtils.isEmpty(userIds)) {
      return Collections.emptyList();
    }
    return organizationMemberMapper.selectUserOrganizationsBatch(spaceId, userIds);
  }

  @Override
  public List<OrganizationMemberDTO> queryOrganizationAdmins(Long spaceId, Long orgId) {
    return organizationMemberMapper.selectOrganizationAdmins(spaceId, orgId);
  }

  @Override
  @Transactional
  public ResultVO<Void> updateMemberStatus(Long spaceId, Long memberId, String statusCd) {
      int result = organizationMemberMapper.updateMemberStatus(memberId, statusCd, SessionUtil.getLoginInfo().getUserId());
      if (result <= 0) {
        throw new BssException("更新成员状态失败");
      }
      return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> removeOrganizationMember(Long spaceId, Long memberId) {
    // 查询成员信息
    OrganizationMemberDTO memberDTO = organizationMemberMapper.selectOrgMemberBasicInfo(spaceId, memberId);
    if (memberDTO == null) {
      throw new BssException("组织成员不存在");
    }
    // 移除组织成员
    int result = organizationMemberMapper.deleteOrganizationMember(spaceId, memberId, SessionUtil.getLoginInfo().getUserId());
    if (result <= 0) {
      throw new BssException("移除组织成员失败");
    }
    // 如果用户在当前企业下没有任何组织成员则移除组织成员角色
    if (!organizationMemberMapper.existUserOrgMember(spaceId, memberDTO.getUserId())) {
      orgUserRoleService.delOrgUserRole(spaceId, memberDTO.getUserId());
    }
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> batchRemoveOrganizationMembers(Long spaceId, List<Long> memberIds) {
      if (CollectionUtils.isEmpty(memberIds)) {
        return ResultVO.success();
      }
      int result = organizationMemberMapper.batchDeleteOrganizationMembers(spaceId, memberIds, SessionUtil.getLoginInfo().getUserId());
      if (result <= 0) {
        throw new BssException("批量移除组织成员失败");
      }
      return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<OrganizationMemberImportDTO> batchImportMembers(MultipartFile file, Long orgId, Long spaceId, String defaultMemberRole, String defaultMemberType) {
      // 1. 解析Excel文件
    List<Map<String, Object>> dataList;
    try {
      dataList = parseExcel(file);
    }
    catch (IOException e) {
      return ResultVO.fail("批量导入成员失败，请使用正确的模板进行导入");
    }
    if (!StringUtils.isEmpty((String) dataList.get(0).get("errMsg"))) {
        return ResultVO.fail("批量导入成员失败，请使用正确的模板进行导入");
      }
      // 2. 数据校验
    OrganizationMemberImportDTO importResult = validateData(dataList, defaultMemberRole, defaultMemberType, orgId, spaceId);
      // 3. 批量插入
      if (importResult.getSuccessCount() > 0) {
        batchInsert(importResult, spaceId);
      }
      return ResultVO.success(importResult);
  }

  private List<Map<String, Object>> parseExcel(MultipartFile file) throws IOException {
    // 使用现有的ExcelUtil类解析Excel文件
    String[] colName = {"rowIndex", "userName", "memberRole", "memberType", "orgCode"};
    String fileName = file.getOriginalFilename();
    if (fileName == null) {
      throw new BssException("文件名不能为空");
    }
    String fileExtension = fileName.toLowerCase();
    boolean isXls = fileExtension.endsWith(".xls");
    // 验证文件扩展名
    if (!fileExtension.endsWith(".xls") && !fileExtension.endsWith(".xlsx")) {
      throw new BssException("不支持的文件格式，请使用.xls或.xlsx格式的Excel文件");
    }
    try (InputStream inputStream = file.getInputStream()) {
      return ExcelUtil.getExcelData(inputStream, INITIAL_ROW, isXls, colName);
    }
  }

  private OrganizationMemberImportDTO validateData(List<Map<String, Object>> dataList, String defaultMemberRole, String defaultMemberType, Long orgId, Long tenantId) {
    OrganizationMemberImportDTO result = new OrganizationMemberImportDTO();
    result.setSuccessList(new ArrayList<>());
    result.setFailList(new ArrayList<>());
    Set<String> seenUserNames = new HashSet<>();
    int row = INITIAL_ROW;
    for (Map<String, Object> memberData : dataList) {
      row++;
      if (isRowEmpty(memberData)) {
        continue;
      }
      String rowIndex = (String) memberData.get("rowIndex");
      if (!validateRowIndex(result, memberData, rowIndex, row)) {
        continue;
      }
      String userName = (String) memberData.get("userName");
      if (!validateUserName(result, memberData, userName, rowIndex, tenantId)) {
        continue;
      }
      if (!validateMemberRole(result, memberData, rowIndex, defaultMemberRole)) {
        continue;
      }
      if (!validateMemberType(result, memberData, rowIndex, defaultMemberType)) {
        continue;
      }
      if (!validateDuplicateUserName(result, memberData, userName, rowIndex, seenUserNames)) {
        continue;
      }
      if (!validateOrganization(result, memberData, rowIndex, orgId, tenantId)) {
        continue;
      }
      result.getSuccessList().add(memberData);
    }
    result.setSuccessCount(result.getSuccessList().size());
    result.setFailCount(dataList.size() - result.getSuccessCount());
    result.setTotalCount(dataList.size());
    return result;
  }

  private boolean validateOrganization(OrganizationMemberImportDTO result, Map<String, Object> memberData, String rowIndex, @Nullable Long orgId, Long tenantId) {
    String orgCode = (String) memberData.get("orgCode");
    if (StringUtils.isEmpty(orgCode)) {
      if (orgId != null) {
        memberData.put("orgId", orgId);
        if (organizationMemberMapper.existsOrganizationMember(tenantId, orgId, (Long) memberData.get("userId"))) {
          addErrorToImportResult(result, rowIndex, "此组织已存在该用户", memberData);
          return false;
        }
        return true;
      }
      addErrorToImportResult(result, rowIndex, "组织编码为空", memberData);
      return false;
    }
    OrganizationDTO organization = organizationManageMapper.getOrganizationByCode(tenantId, orgCode);
    if (organization == null || organization.getOrgId() == null) {
      addErrorToImportResult(result, rowIndex, "组织编码不存在", memberData);
      return false;
    }
    if (organizationMemberMapper.existsOrganizationMember(tenantId, organization.getOrgId(), (Long) memberData.get("userId"))) {
      addErrorToImportResult(result, rowIndex, "此组织已存在该用户", memberData);
      return false;
    }
    memberData.put("orgId", organization.getOrgId());
    return true;
  }

  private boolean isRowEmpty(Map<String, Object> data) {
    return StringUtils.isEmpty((String) data.get("userName")) &&
           StringUtils.isEmpty((String) data.get("realName"));
  }

  private boolean validateRowIndex(OrganizationMemberImportDTO result, Map<String, Object> memberData, String rowIndex, int row) {
    if (StringUtils.isEmpty(rowIndex)) {
      addErrorToImportResult(result, "行号: " + row, "缺少序号，已跳过此数据", memberData);
      return false;
    }
    return true;
  }

  private boolean validateUserName(OrganizationMemberImportDTO result, Map<String, Object> memberData, String userName, String rowIndex, Long tenantId) {
    if (StringUtils.isEmpty(userName)) {
      addErrorToImportResult(result, rowIndex, "用户名为空", memberData);
      return false;
    }
    UserEntity userInfo = userManageMapper.getUserByCode(BaseConsts.PORTAL_SYSTEM_CODE_DEFAULT, userName);
    if (userInfo == null || userInfo.getUserId() == null) {
      addErrorToImportResult(result, rowIndex, "用户不存在", memberData);
      return false;
    }
    if (!tenantManageMapper.existsTenantUser(tenantId, userInfo.getUserId())) {
      addErrorToImportResult(result, rowIndex, "用户不在此租户", memberData);
      return false;
    }
    memberData.put("userId", userInfo.getUserId());
    return true;
  }

  private boolean validateMemberRole(OrganizationMemberImportDTO result, Map<String, Object> memberData, String rowIndex, String defaultMemberRole) {
    String memberRole = (String) memberData.get("memberRole");
    if (StringUtils.isEmpty(memberRole)) {
      memberData.put("memberRoleAttrValue", defaultMemberRole);
      return true;
    }
    List<SimpleAttrDTO> attrList = attrSpecCache.get(BaseConsts.PLATFORM_TENANT_ID, OrganizationConsts.ATTR_CODE_ORG_MEMBER_ROLE);
    if (attrList != null) {
      for (SimpleAttrDTO simpleAttrDTO : attrList) {
        if (memberRole.equals(simpleAttrDTO.getAttrValueName())) {
          memberData.put("memberRoleAttrValue", simpleAttrDTO.getAttrValue());
          return true;
        }
      }
    }
    addErrorToImportResult(result, rowIndex, "成员角色不合规", memberData);
    return false;
  }

  private boolean validateMemberType(OrganizationMemberImportDTO result, Map<String, Object> memberData, String rowIndex, String defaultMemberType) {
    String memberType = (String) memberData.get("memberType");
    if (StringUtils.isEmpty(memberType)) {
      memberData.put("memberTypeAttrValue", defaultMemberType);
      return true;
    }
    List<SimpleAttrDTO> attrList = attrSpecCache.get(BaseConsts.PLATFORM_TENANT_ID, OrganizationConsts.ATTR_CODE_ORG_MEMBER_TYPE);
    if (attrList != null) {
      for (SimpleAttrDTO simpleAttrDTO : attrList) {
        if (memberType.equals(simpleAttrDTO.getAttrValueName())) {
          memberData.put("memberTypeAttrValue", simpleAttrDTO.getAttrValue());
          return true;
        }
      }
    }
    addErrorToImportResult(result, rowIndex, "成员类型不合规", memberData);
    return false;
  }

  private boolean validateDuplicateUserName(OrganizationMemberImportDTO result, Map<String, Object> memberData, String userName, String rowIndex, Set<String> seenUserNames) {
    if (seenUserNames.contains(userName)) {
      addErrorToImportResult(result, rowIndex, "用户名重复", memberData);
      return false;
    }
    seenUserNames.add(userName);
    return true;
  }

  private void addErrorToImportResult(OrganizationMemberImportDTO result, String rowIndex, String errorMessage, Map<String, Object> data) {
    result.failPut(rowIndex, errorMessage, data);
  }

  private void batchInsert(OrganizationMemberImportDTO result, Long spaceId) {
    List<Map<String, Object>> successList = result.getSuccessList();
    if (CollectionUtils.isEmpty(successList)) {
      return;
    }
    Long currentUserId = SessionUtil.getLoginInfo().getUserId();
    List<OrganizationMemberEntity> memberEntityList = new ArrayList<>();
    for (Map<String, Object> data : successList) {
      String userName = (String) data.get("userName");
      String rowIndex = (String) data.get("rowIndex");
      if (StringUtils.isBlank(userName)) {
        addErrorToImportResult(result, rowIndex, "用户名不能为空", data);
        continue;
      }
      OrganizationMemberEntity member = new OrganizationMemberEntity();
      member.setMemberId(Sequences.ORGANIZATION_MEMBER_ID.next());
      member.setUserId((Long) data.get("userId"));
      member.setOrgId((Long) data.get("orgId"));
      member.setSpaceId(spaceId);
      member.setMemberRole((String) data.get("memberRole"));
      member.setMemberType((String) data.get("memberType"));
      member.setJoinDate(new Date());
      member.setStatusCd(BaseConsts.STATUS_CD_VALID);
      member.setCreatorId(currentUserId);
      member.setUpdatorId(currentUserId);
      memberEntityList.add(member);
    }
    // 批量插入
    if (CollectionUtils.isNotEmpty(memberEntityList)) {
      for (int i = 0; i < memberEntityList.size(); i += BATCH_SIZE) {
        int end = Math.min(i + BATCH_SIZE, memberEntityList.size());
        List<OrganizationMemberEntity> batch = memberEntityList.subList(i, end);
        // 使用批量插入方法
        organizationMemberMapper.batchInsertOrganizationMembers(batch);
      }
    }
  }

  @Override
  public boolean checkMemberExists(Long spaceId, Long orgId, Long userId) {
    return organizationMemberMapper.existsOrganizationMember(spaceId, orgId, userId);
  }

  @Override
  public int countOrganizationMembers(Long spaceId, Long orgId) {
    return organizationMemberMapper.countOrganizationMembers(spaceId, orgId);
  }

  @Override
  public OrganizationUserDTO queryOrgAndUserList(Long spaceId, @Nullable Long orgId,  @Nullable Boolean excludeRoleUser) {
    // 查询结果
    OrganizationUserDTO resultOrgUserDTO = new OrganizationUserDTO();
    // 组织ID为空时查询企业的根组织信息
    if (orgId == null) {
      OrganizationDTO rootOrg = organizationManageMapper.selectSpaceRootOrg(spaceId);
      Assert.notNull(rootOrg, "企业对应的根组织为空，请联系管理员");
      orgId = rootOrg.getOrgId();
      resultOrgUserDTO.setOrgName(rootOrg.getOrgName());
    }
    resultOrgUserDTO.setOrgId(orgId);
    // 查询组织下的成员
    List<OrganizationUserDTO> orgUserList = organizationMemberMapper.selectOrgUserList(spaceId, orgId, excludeRoleUser);
    resultOrgUserDTO.addOrgUserList(orgUserList);
    // 查询组织下的子组织列表
    List<SimpleOrganizationDTO> subOrgList = organizationManageMapper.selectChildOrganizations(spaceId, orgId);
    if (CollectionUtils.isNotEmpty(subOrgList)) {
      // 统计每个子组织的成员数量
      List<OrganizationUserDTO> children = subOrgList.stream().map(orgDTO -> {
        OrganizationUserDTO subOrgUserDTO = new OrganizationUserDTO();
        subOrgUserDTO.setOrgId(orgDTO.getOrgId());
        subOrgUserDTO.setOrgName(orgDTO.getOrgName());
        subOrgUserDTO.setMemberCount(organizationMemberMapper.countOrgUsers(spaceId, orgDTO.getOrgId(), excludeRoleUser));
        return subOrgUserDTO;
      }).toList();
      resultOrgUserDTO.setChildren(children);
    }
    return resultOrgUserDTO;
  }

  @Override
  @Transactional
  public ResultVO<Void> createOrganizationMember(Long spaceId, Long orgId, Long userId) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    Assert.notNull(orgId, "组织ID不能为空");
    Assert.notNull(userId, "用户ID不能为空");
    // 检查用户是否已在对应组织下
    if (checkMemberExists(spaceId, orgId, userId)) {
      return ResultVO.success();
    }
    // 把用户添加到组织成员
    saveNewOrgMember(spaceId, orgId, userId);
    // 给新成员用户授权普通成员角色
    orgUserRoleService.addUserOrgRole(spaceId, userId, BaseConsts.ORG_ROLE_MEMBER);
    return ResultVO.success();
  }

  @Override
  @Transactional
  public void addUserToRootOrg(Long spaceId, Long userId) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    Assert.notNull(userId, "用户ID不能为空");
    // 查询企业空间对应的根组织ID
    OrganizationDTO rootOrg = organizationManageMapper.selectSpaceRootOrg(spaceId);
    if (rootOrg != null) {
      // 查询用户是否在该企业空间对应的根组织下
      boolean checkResult = checkMemberExists(spaceId, rootOrg.getOrgId(), userId);
      if (!checkResult) {
        // 添加用户到根组织成员
        saveNewOrgMember(spaceId, rootOrg.getOrgId(), userId);
        // 给新成员用户授权普通成员角色
        orgUserRoleService.addUserOrgRole(spaceId, userId, BaseConsts.ORG_ROLE_MEMBER);
      }
    }
  }

  /**
   * 添加新组织成员
   */
  private void saveNewOrgMember(Long spaceId, Long orgId, Long userId) {
    OrganizationMemberEntity orgMemberEntity = new OrganizationMemberEntity();
    orgMemberEntity.setMemberId(Sequences.ORGANIZATION_MEMBER_ID.next());
    orgMemberEntity.setSpaceId(spaceId);
    orgMemberEntity.setOrgId(orgId);
    orgMemberEntity.setUserId(userId);
    orgMemberEntity.setStatusCd(BaseConsts.STATUS_CD_VALID);
    orgMemberEntity.setMemberType("regular");
    orgMemberEntity.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    orgMemberEntity.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    organizationMemberMapper.insertOrganizationMember(orgMemberEntity);
  }

  @Override
  public List<Long> queryUserOrgAndParentOrgIds(Long spaceId, Long userId) {
    // 查询用户所属的全部组织信息
    List<OrganizationMemberDTO> orgMemberList = queryUserOrganizations(spaceId, userId);
    if (CollectionUtils.isNotEmpty(orgMemberList)) {
      // 提取用户所属的组织及其全部父组织ID列表
      Set<Long> orgIdSet = new HashSet<>();
      for (OrganizationMemberDTO orgMember : orgMemberList) {
        // 取所属的组织路径：路径规则是从顶端父组织开始一直到当前组织
        SimpleOrganizationDTO orgInfo = orgMember.getOrganizationInfo();
        if (orgInfo != null && StringUtils.isNotEmpty(orgInfo.getOrgPath())) {
          orgIdSet.addAll(Arrays.stream(orgInfo.getOrgPath().split("\\.")).map(Long::valueOf).toList());
        }
      }
      return orgIdSet.stream().toList();
    }
    return List.of();
  }

  @Override
  public List<OrganizationMemberDTO> queryMemberListByOrgIds(Long spaceId, List<Long> orgIds) {
    return organizationMemberMapper.selectMemberListByOrgIds(spaceId, orgIds);
  }
}
