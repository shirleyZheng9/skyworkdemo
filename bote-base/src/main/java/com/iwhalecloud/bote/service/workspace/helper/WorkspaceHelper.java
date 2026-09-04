package com.iwhalecloud.bote.service.workspace.helper;

import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import com.iwhalecloud.bote.dto.workspace.WorkspaceDTO;
import com.iwhalecloud.bote.mapper.portal.TenantQueryMapper;
import com.iwhalecloud.bote.mapper.workspace.WorkspaceManageMapper;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 企业空间辅助类
 *
 * @author huangyunming
 * @since 2025-12-23
 */
@Component
@RequiredArgsConstructor
public class WorkspaceHelper {
  private final TenantQueryMapper tenantQueryMapper;
  private final WorkspaceManageMapper workspaceManageMapper;

  /**
   * 初始化空间虚拟租户id
   * @param spaceId 空间id
   * @return 空间DTO
   */
  @Transactional
  public WorkspaceDTO initAiTenantId(Long spaceId) {
    WorkspaceDTO workspaceDTO = workspaceManageMapper.selectWorkspaceById(spaceId);
    if (workspaceDTO != null && workspaceDTO.getSpaceTenantId() == null) {
      TenantDTO tenanWorkSpace = createTenanWorkSpace(workspaceDTO.getSpaceId());
      workspaceDTO.setSpaceTenantId(tenanWorkSpace.getTenantId());
      workspaceManageMapper.updateWorkspace(workspaceDTO);
    }
    return workspaceDTO;
  }

  /**
   * 添加空间虚拟租户id
   * @param spaceId 空间id
   * @return 虚拟租户DTO
   */
  private TenantDTO createTenanWorkSpace(Long spaceId) {
    TenantDTO tenant = new TenantDTO();
    tenant.setTenantId(IDUtils.nextId());
    tenant.setSpaceId(spaceId);
    tenant.setTenantCode(tenant.getTenantId().toString());
    tenant.setTenantName("虚拟租户");
    tenant.setSystemType(CommonConsts.SYSTEM_TYPE_WORKSPACE);
    tenant.setStatusCd(CommonConsts.STATUS_CD_VALID);
    tenantQueryMapper.insertTenant(tenant);
    return tenant;
  }
}
