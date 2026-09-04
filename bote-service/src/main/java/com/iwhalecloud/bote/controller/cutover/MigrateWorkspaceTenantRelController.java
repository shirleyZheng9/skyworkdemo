package com.iwhalecloud.bote.controller.cutover;

import com.google.common.collect.Lists;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.OrganizationConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.dto.organization.OrganizationMemberDTO;
import com.iwhalecloud.bote.dto.portal.SimpleTenantDTO;
import com.iwhalecloud.bote.dto.workspace.WorkspaceDTO;
import com.iwhalecloud.bote.entity.organization.OrgUserRoleEntity;
import com.iwhalecloud.bote.entity.organization.OrganizationMemberEntity;
import com.iwhalecloud.bote.mapper.cutover.CutOverMapper;
import com.iwhalecloud.bote.mapper.organization.OrgUserRoleMapper;
import com.iwhalecloud.bote.mapper.organization.OrganizationMemberMapper;
import com.iwhalecloud.bote.mapper.workspace.WorkspaceManageMapper;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 割接工作空间与租户关系
 *
 * @author chen.linfa
 * @since 2025-10-16
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "cutOver/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Hidden
@IgnoreSign
public class MigrateWorkspaceTenantRelController {

  private final CutOverMapper cutOverMapper;
  private final WorkspaceManageMapper workspaceManageMapper;
  private final OrganizationMemberMapper organizationMemberMapper;
  private final OrgUserRoleMapper orgUserRoleMapper;

  @GetMapping(path = "migrateWorkspaceTenantRelRel", produces = MediaType.TEXT_PLAIN_VALUE)
  public void migrateWorkspace(HttpServletResponse response) throws IOException {
    response.setContentType("text/plain;charset=UTF-8");
    PrintWriter writer = response.getWriter();  //NOPMD - suppressed CloseResource - HTTP 输出流不需要手动关闭
    migrateTenantRel(writer);
    migrateOrgMember(writer);
    migrateOrgUserRole(writer);
  }

  private void migrateTenantRel(PrintWriter writer) {
    long startTime = System.currentTimeMillis();
    addLog(writer, "开始割接工作空间与租户关系\n");
    List<SimpleTenantDTO> tenants = cutOverMapper.selectAllTenants(true);
    long total = tenants.size();
    if (total == 0) {
      addLog(writer, "没有需要处理的租户\n");
      return;
    }
    addLog(writer, "总数: %d\n", total);
    List<WorkspaceDTO> workspaces = new ArrayList<>(tenants.size());
    for (SimpleTenantDTO tenant : tenants) {
      WorkspaceDTO dto = new WorkspaceDTO();
      dto.setSpaceId(tenant.getTenantId());
      dto.setSpaceName(tenant.getTenantName());
      dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
      dto.setCreatorId(tenant.getCreatorId());
      dto.setUpdatorId(tenant.getCreatorId());
      workspaces.add(dto);
    }
    TransactionUtil.executeNew(() -> {
      cutOverMapper.updateTenantSpaceId();
      workspaceManageMapper.batchInsertWorkspace(workspaces);
    });
    addLog(writer, "迁移完成。耗时: %sms, 总数: %s\n", System.currentTimeMillis() - startTime, total);
  }

  private void migrateOrgMember(PrintWriter writer) {
    long startTime = System.currentTimeMillis();
    addLog(writer, "开始割接企业空间下的组织成员\n");
    List<OrganizationMemberDTO> users = cutOverMapper.selectAllOrgMember();
    long total = users.size();
    if (total == 0) {
      addLog(writer, "没有需要处理的组织成员\n");
      return;
    }
    addLog(writer, "总数: %d\n", total);
    List<OrganizationMemberEntity> members = new ArrayList<>(users.size());
    for (OrganizationMemberDTO user : users) {
      OrganizationMemberEntity member = new OrganizationMemberEntity();
      member.setMemberId(Sequences.ORGANIZATION_MEMBER_ID.next());
      member.setUserId(user.getUserId());
      member.setOrgId(user.getOrgId());
      member.setSpaceId(user.getSpaceId());
      member.setMemberRole(OrganizationConsts.MEMBER_ROLE_MEMBER);
      member.setMemberType(OrganizationConsts.MEMBER_TYPE_REGULAR);
      member.setJoinDate(new Date());
      member.setStatusCd(BaseConsts.STATUS_CD_VALID);
      member.setCreatorId(1L);
      member.setUpdatorId(1L);
      members.add(member);
    }

    List<List<OrganizationMemberEntity>> partitionList = Lists.partition(members, 50);
    for (List<OrganizationMemberEntity> list : partitionList) {
      TransactionUtil.executeNew(() -> {
        organizationMemberMapper.batchInsertOrganizationMembers(list);
      });
    }
    addLog(writer, "迁移完成。耗时: %sms, 总数: %s\n", System.currentTimeMillis() - startTime, total);
  }

  private void migrateOrgUserRole(PrintWriter writer) {
    long startTime = System.currentTimeMillis();
    addLog(writer, "开始割接企业空间下的组织成员角色为组织用户角色\n");
    List<OrganizationMemberDTO> orgMemberRole = cutOverMapper.selectAllOrgMemberRole();
    long total = orgMemberRole.size();
    if (total == 0) {
      addLog(writer, "没有需要处理的组织成员角色\n");
      return;
    }

    List<OrgUserRoleEntity> orgUserRoleEntityList = new ArrayList<>();
    // 按空间进行分组
    Map<Long, List<OrganizationMemberDTO>> spaceGroupMap = orgMemberRole.stream().collect(Collectors.groupingBy(OrganizationMemberDTO::getSpaceId));
    for (Map.Entry<Long, List<OrganizationMemberDTO>> spaceEntry : spaceGroupMap.entrySet()) {
      // 按用户ID分组，同一个用户在一个企业空间下会被授权为多个组织成员
      Map<Long, List<OrganizationMemberDTO>> userGroupMap = spaceEntry.getValue().stream().collect(Collectors.groupingBy(OrganizationMemberDTO::getUserId));
      for (Map.Entry<Long, List<OrganizationMemberDTO>> userEntry : userGroupMap.entrySet()) {
        List<OrganizationMemberDTO> orgMemberRoleList = userEntry.getValue();
        // 多个组织成员角色时，优先取 admin 角色，再取 member 角色
        Optional<OrganizationMemberDTO> firstMemberRole = orgMemberRoleList.stream()
          .filter(o -> StringUtils.isNotBlank(o.getMemberRole()))
          .min(Comparator.comparing(OrganizationMemberDTO::getMemberRole));
        if (firstMemberRole.isPresent()) {
          OrganizationMemberDTO memberDTO = firstMemberRole.get();
          OrgUserRoleEntity roleEntity = new OrgUserRoleEntity();
          roleEntity.setOrgRoleId(Sequences.ORG_USER_ROLE_ID.next());
          roleEntity.setUserId(memberDTO.getUserId());
          roleEntity.setOrgRole(OrganizationConsts.MEMBER_ROLE_MEMBER);
          roleEntity.setSpaceId(memberDTO.getSpaceId());
          roleEntity.setStatusCd(BaseConsts.STATUS_CD_VALID);
          roleEntity.setCreatorId(1L);
          roleEntity.setUpdatorId(1L);
          orgUserRoleEntityList.add(roleEntity);
        }
      }
    }

    List<List<OrgUserRoleEntity>> partitionList = Lists.partition(orgUserRoleEntityList, 50);
    for (List<OrgUserRoleEntity> list : partitionList) {
      TransactionUtil.executeNew(() -> {
        orgUserRoleMapper.insertOrgUserRoleBatch(list);
      });
    }

    addLog(writer, "迁移完成。耗时: %sms, 总数: %s\n", System.currentTimeMillis() - startTime, orgUserRoleEntityList.size());
  }

  /**
   * 打印日志到 HTTP 响应
   */
  @SuppressFBWarnings("XSS_SERVLET")
  private void addLog(PrintWriter writer, String msg, Object... args) {
    writer.printf(msg, args);
    writer.flush();
  }
}
