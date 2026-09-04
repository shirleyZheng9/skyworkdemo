package com.iwhalecloud.bote.controller.organization;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.organization.OrganizationDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationTreeNodeDTO;
import com.iwhalecloud.bote.dto.organization.SimpleOrganizationDTO;
import com.iwhalecloud.bote.dto.organization.query.OrganizationQueryParams;
import com.iwhalecloud.bote.dto.organization.request.OrganizationSearchRequest;
import com.iwhalecloud.bote.service.organization.IOrganizationManageService;
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

/**
 * 组织管理控制器
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/organization", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "组织管理")
public class OrganizationManageController {

  private final IOrganizationManageService organizationManageService;

  @Operation(summary = "保存组织")
  @PostMapping("saveOrganization")
  public ResultVO<OrganizationDTO> saveOrganization(@RequestBody @Valid OrganizationDTO organization) {
    return organizationManageService.saveOrganization(organization);
  }

  @Operation(summary = "查询单个组织")
  @GetMapping("findOrganization")
  public ResultVO<OrganizationDTO> findOrganization(
    @Parameter(description = "企业空间ID") @RequestParam(value = "spaceId", required = false) Long spaceId,
    @Parameter(description = "组织ID") @RequestParam("orgId") Long orgId) {
    Assert.notNull(orgId, "组织ID不能为空");
    return ResultVO.success(organizationManageService.findOrganization(spaceId, orgId));
  }

  @Operation(summary = "根据组织编码查询组织")
  @GetMapping("findOrganizationByCode")
  public ResultVO<OrganizationDTO> findOrganizationByCode(
    @Parameter(description = "企业空间ID") @RequestParam(value = "spaceId", required = false) Long spaceId,
    @Parameter(description = "组织编码") @RequestParam("orgCode") String orgCode) {
    Assert.hasText(orgCode, "组织编码不能为空");
    return ResultVO.success(organizationManageService.findOrganizationByCode(spaceId, orgCode));
  }

  @Operation(summary = "查询组织列表")
  @PostMapping("queryOrganizationList")
  public ResultVO<List<SimpleOrganizationDTO>> queryOrganizationList(@RequestBody OrganizationQueryParams queryParams) {
    return ResultVO.success(organizationManageService.queryOrganizationList(queryParams));
  }

  @Operation(summary = "分页查询组织列表")
  @PostMapping("queryOrganizationPage")
  public ResultVO<PageInfo<OrganizationDTO>> queryOrganizationPage(@RequestBody OrganizationQueryParams queryParams) {
    return ResultVO.success(organizationManageService.queryOrganizationPage(queryParams));
  }

  @Operation(summary = "高级搜索组织")
  @PostMapping("searchOrganizations")
  public ResultVO<PageInfo<OrganizationDTO>> searchOrganizations(@RequestBody @Valid OrganizationSearchRequest searchRequest) {
    return ResultVO.success(organizationManageService.searchOrganizations(searchRequest));
  }

  @Operation(summary = "查询组织树结构")
  @GetMapping("queryOrganizationTree")
  public ResultVO<List<OrganizationTreeNodeDTO>> queryOrganizationTree(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "根组织ID") @RequestParam(value = "rootOrgId", required = false) Long rootOrgId,
    @Parameter(description = "最大层级") @RequestParam(value = "maxLevel", defaultValue = "10") Integer maxLevel) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    return ResultVO.success(organizationManageService.queryOrganizationTree(spaceId, rootOrgId, maxLevel));
  }

  @Operation(summary = "按名称搜索查询组织树结构")
  @GetMapping("queryOrganizationTreeByName")
  public ResultVO<List<OrganizationTreeNodeDTO>> queryOrganizationTreeByName(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "组织名称") @RequestParam(value = "orgName", required = false) String orgName,
    @Parameter(description = "根组织ID") @RequestParam(value = "rootOrgId", required = false) Long rootOrgId,
    @Parameter(description = "最大层级") @RequestParam(value = "maxLevel", defaultValue = "10") Integer maxLevel) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    return ResultVO.success(organizationManageService.queryOrganizationTreeByName(spaceId, orgName, rootOrgId, maxLevel));
  }

  @Operation(summary = "查询子组织列表")
  @GetMapping("queryChildOrganizations")
  public ResultVO<List<SimpleOrganizationDTO>> queryChildOrganizations(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "上级组织ID") @RequestParam("parentOrgId") Long parentOrgId) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    Assert.notNull(parentOrgId, "上级组织ID不能为空");
    return ResultVO.success(organizationManageService.queryChildOrganizations(spaceId, parentOrgId));
  }

  @Operation(summary = "更新组织状态")
  @PostMapping("updateOrganizationStatus")
  public ResultVO<Void> updateOrganizationStatus(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "组织ID") @RequestParam("orgId") Long orgId,
    @Parameter(description = "状态") @RequestParam("statusCd") String statusCd) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    Assert.notNull(orgId, "组织ID不能为空");
    Assert.hasText(statusCd, "状态不能为空");
    return organizationManageService.updateOrganizationStatus(spaceId, orgId, statusCd);
  }


  @Operation(summary = "检查组织编码是否存在")
  @GetMapping("checkOrgCodeExists")
  public ResultVO<Boolean> checkOrgCodeExists(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "组织编码") @RequestParam("orgCode") String orgCode,
    @Parameter(description = "排除的组织ID") @RequestParam(value = "excludeOrgId", required = false) Long excludeOrgId) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    Assert.hasText(orgCode, "组织编码不能为空");
    return ResultVO.success(organizationManageService.checkOrgCodeExists(spaceId, orgCode, excludeOrgId));
  }

  @Operation(summary = "统计组织成员数量")
  @PostMapping("countOrganizationMembers")
  public ResultVO<List<OrganizationDTO>> countOrganizationMembers(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "组织ID列表") @RequestBody List<Long> orgIds) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    Assert.notEmpty(orgIds, "组织ID列表不能为空");
    return ResultVO.success(organizationManageService.countOrganizationMembers(spaceId, orgIds));
  }

  @Operation(summary = "逻辑删除组织")
  @PostMapping("deleteOrganization")
  public ResultVO<Void> deleteOrganization(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "组织ID") @RequestParam("orgId") Long orgId) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    Assert.notNull(orgId, "组织ID不能为空");
    return organizationManageService.deleteOrganization(spaceId, orgId);
  }
}
