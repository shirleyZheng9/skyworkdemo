package com.iwhalecloud.bote.service.portal;

import com.iwhalecloud.bote.dto.portal.SimpleRolePrivDTO;
import com.iwhalecloud.bote.dto.portal.query.RolePrivQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import java.util.Map;
import org.springframework.lang.NonNull;

/**
 * 角色权限管理服务
 *
 * @author chen.linfa
 * @since 2025-10-09
 */
public interface IRolePrivManageService {
  /**
   * 保存角色权限
   *
   * @param params 入参
   * @return 结果
   */
  ResultVO<Void> saveRolePriv(RolePrivQueryParams params);

  /**
   * 删除角色权限
   *
   * @param params 入参
   * @return 结果
   */
  ResultVO<Void> deleteRolePriv(RolePrivQueryParams params);

  /**
   * 查询角色权限列表
   *
   * @param queryParams 查询条件
   * @return 角色权限列表
   */
  List<SimpleRolePrivDTO> queryRolePrivList(RolePrivQueryParams queryParams);

  /**
   * 查询当前用户可访问的门户菜单、组件
   *
   * @param spaceId 空间 ID
   * @param tenantId 租户 ID
   * @return 角色权限
   */
  Map<String, Object> queryPortal(Long spaceId, @NonNull Long tenantId);
}
