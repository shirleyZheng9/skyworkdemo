package com.iwhalecloud.bote.controller.organization;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.organization.BatchAddOrganizationMembersResult;
import com.iwhalecloud.bote.dto.organization.CreateOrgMemberDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationMemberDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationMemberImportDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationUserDTO;
import com.iwhalecloud.bote.dto.organization.query.OrganizationMemberQueryParams;
import com.iwhalecloud.bote.dto.organization.request.BatchAddOrganizationMembersRequest;
import com.iwhalecloud.bote.service.organization.IOrganizationMemberService;
import com.iwhalecloud.bote.service.portal.IUserManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 组织成员管理控制器
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/organization/member", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "组织成员管理")
public class OrganizationMemberController {

  private final IOrganizationMemberService organizationMemberService;
  private final IUserManageService userManageService;

  @Operation(summary = "添加组织成员")
  @PostMapping("addOrganizationMember")
  public ResultVO<OrganizationMemberDTO> addOrganizationMember(@RequestBody @Valid OrganizationMemberDTO member) {
    return organizationMemberService.addOrganizationMember(member);
  }

  @Operation(summary = "批量添加组织成员")
  @PostMapping("batchAddOrganizationMembers")
  public ResultVO<BatchAddOrganizationMembersResult> batchAddOrganizationMembers(@RequestBody @Valid BatchAddOrganizationMembersRequest request) {
    return organizationMemberService.batchAddOrganizationMembers(request);
  }

  @Operation(summary = "修改组织成员")
  @PostMapping("updateOrganizationMember")
  public ResultVO<OrganizationMemberDTO> updateOrganizationMember(@RequestBody @Valid OrganizationMemberDTO member) {
    Assert.notNull(member.getMemberId(), "成员ID不能为空");
    return organizationMemberService.updateOrganizationMember(member);
  }

  @Operation(summary = "查询单个组织成员")
  @GetMapping("findOrganizationMember")
  public ResultVO<OrganizationMemberDTO> findOrganizationMember(
    @Parameter(description = "企业空间ID") @RequestParam(value = "spaceId", required = false) Long spaceId,
    @Parameter(description = "成员ID") @RequestParam("memberId") Long memberId) {
    Assert.notNull(memberId, "成员ID不能为空");
    return ResultVO.success(organizationMemberService.findOrganizationMember(spaceId, memberId));
  }

  @Operation(summary = "根据组织和用户查询成员信息")
  @GetMapping("findMemberByOrgAndUser")
  public ResultVO<OrganizationMemberDTO> findMemberByOrgAndUser(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "组织ID") @RequestParam("orgId") Long orgId,
    @Parameter(description = "用户ID") @RequestParam("userId") Long userId) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    Assert.notNull(orgId, "组织ID不能为空");
    Assert.notNull(userId, "用户ID不能为空");
    return ResultVO.success(organizationMemberService.findMemberByOrgAndUser(spaceId, orgId, userId));
  }

  @Operation(summary = "查询组织成员列表")
  @PostMapping("queryOrganizationMemberList")
  public ResultVO<List<OrganizationMemberDTO>> queryOrganizationMemberList(@RequestBody OrganizationMemberQueryParams queryParams) {
    return ResultVO.success(organizationMemberService.queryOrganizationMemberList(queryParams));
  }

  @Operation(summary = "分页查询组织成员列表")
  @PostMapping("queryOrganizationMemberPage")
  public ResultVO<PageInfo<OrganizationMemberDTO>> queryOrganizationMemberPage(@RequestBody OrganizationMemberQueryParams queryParams) {
    return ResultVO.success(organizationMemberService.queryOrganizationMemberPage(queryParams));
  }

  @Operation(summary = "查询用户所属的组织列表")
  @GetMapping("queryUserOrganizations")
  public ResultVO<List<OrganizationMemberDTO>> queryUserOrganizations(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "用户ID") @RequestParam("userId") Long userId) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    Assert.notNull(userId, "用户ID不能为空");
    return ResultVO.success(organizationMemberService.queryUserOrganizations(spaceId, userId));
  }

  @Operation(summary = "查询组织的管理员列表")
  @GetMapping("queryOrganizationAdmins")
  public ResultVO<List<OrganizationMemberDTO>> queryOrganizationAdmins(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "组织ID") @RequestParam("orgId") Long orgId) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    Assert.notNull(orgId, "组织ID不能为空");
    return ResultVO.success(organizationMemberService.queryOrganizationAdmins(spaceId, orgId));
  }

  @Operation(summary = "更新成员状态")
  @PostMapping("updateMemberStatus")
  public ResultVO<Void> updateMemberStatus(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "成员ID") @RequestParam("memberId") Long memberId,
    @Parameter(description = "状态") @RequestParam("statusCd") String statusCd) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    Assert.notNull(memberId, "成员ID不能为空");
    Assert.hasText(statusCd, "状态不能为空");
    return organizationMemberService.updateMemberStatus(spaceId, memberId, statusCd);
  }

  @Operation(summary = "移除组织成员")
  @PostMapping("removeOrganizationMember")
  public ResultVO<Void> removeOrganizationMember(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "成员ID") @RequestParam("memberId") Long memberId) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    Assert.notNull(memberId, "成员ID不能为空");
    return organizationMemberService.removeOrganizationMember(spaceId, memberId);
  }

  @Operation(summary = "批量移除组织成员")
  @PostMapping("batchRemoveOrganizationMembers")
  public ResultVO<Void> batchRemoveOrganizationMembers(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "成员ID列表") @RequestBody List<Long> memberIds) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    Assert.notEmpty(memberIds, "成员ID列表不能为空");
    return organizationMemberService.batchRemoveOrganizationMembers(spaceId, memberIds);
  }

  @Operation(summary = "批量导入成员")
  @PostMapping("batchImportMembers")
  public ResultVO<OrganizationMemberImportDTO> batchImportMembers(
      @Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file,
      @Parameter(description = "组织ID") @RequestParam(value = "orgId", required = false) Long orgId,
      @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
      @Parameter(description = "默认成员角色") @RequestParam(value = "defaultMemberRole", required = false, defaultValue = "member") String defaultMemberRole,
      @Parameter(description = "默认成员类型") @RequestParam(value = "defaultMemberType", required = false, defaultValue = "regular") String defaultMemberType) {
    return organizationMemberService.batchImportMembers(file, orgId, spaceId, defaultMemberRole, defaultMemberType);
  }

  @Operation(summary = "检查用户是否已是组织成员")
  @GetMapping("checkMemberExists")
  public ResultVO<Boolean> checkMemberExists(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "组织ID") @RequestParam("orgId") Long orgId,
    @Parameter(description = "用户ID") @RequestParam("userId") Long userId) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    Assert.notNull(orgId, "组织ID不能为空");
    Assert.notNull(userId, "用户ID不能为空");
    boolean exists = organizationMemberService.checkMemberExists(spaceId, orgId, userId);
    return ResultVO.success(exists);
  }

  @Operation(summary = "统计组织成员数量")
  @GetMapping("countOrganizationMembers")
  public ResultVO<Integer> countOrganizationMembers(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "组织ID") @RequestParam("orgId") Long orgId) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    Assert.notNull(orgId, "组织ID不能为空");
    int count = organizationMemberService.countOrganizationMembers(spaceId, orgId);
    return ResultVO.success(count);
  }

  @Operation(summary = "查询组织和成员列表")
  @GetMapping("queryOrgAndUserList")
  public ResultVO<OrganizationUserDTO> queryOrgAndUserList(
    @RequestParam("spaceId") Long spaceId,
    @RequestParam(value = "orgId", required = false) Long orgId,
    @RequestParam(value = "excludeRoleUser", required = false) Boolean excludeRoleUser) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    return ResultVO.success(organizationMemberService.queryOrgAndUserList(spaceId, orgId, excludeRoleUser));
  }

  @Operation(summary = "新增组织成员")
  @PostMapping("createOrganizationMember")
  public ResultVO<CreateOrgMemberDTO> createOrganizationMember(@RequestBody CreateOrgMemberDTO orgMemberDTO) {
    return userManageService.createUserAndMember(orgMemberDTO);
  }

}
