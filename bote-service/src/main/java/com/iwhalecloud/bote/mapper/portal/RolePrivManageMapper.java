package com.iwhalecloud.bote.mapper.portal;

import com.iwhalecloud.bote.dto.portal.RolePrivDTO;
import com.iwhalecloud.bote.dto.portal.SimpleRolePrivDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 角色权限管理
 *
 * @author auto
 * @since 2024-10-14
 */
public interface RolePrivManageMapper {

  /**
   * 批量新增角色权限
   *
   * @param rolePrivs 角色权限列表
   * @return 结果
   */
  int batchInsertRolePriv(@Param("list") List<RolePrivDTO> rolePrivs);

  /**
   * 删除角色授权
   *
   * @param roleCode 权限编码
   * @param privIds 权限 ID 集合
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteRolePriv(@Param("roleCode") String roleCode, @Param("privIds") List<Long> privIds, @Param("updatorId") Long updatorId);

  /**
   * 获取角色权限列表
   *
   * @param roleCode 角色编码
   * @param privType 权限类型
   * @return 角色权限列表
   */
  List<SimpleRolePrivDTO> selectRolePrivList(@Param("roleCode") String roleCode, @Param("privType") String privType);

  /**
   * 查询所有角色权限列表
   */
  List<SimpleRolePrivDTO> selectAllPrivList();

  /**
   * 查询角色的权限 ID 列表
   */
  List<Long> selectPrivIdsByRoleCode(@Param("roleCode") String roleCode);
}
