package com.iwhalecloud.bote.mapper.portal;

import com.iwhalecloud.bote.dto.portal.TenantSettingInfoDTO;
import com.iwhalecloud.bote.dto.portal.query.TenantQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 租户设置信息管理
 *
 * @author auto
 * @since 2024-12-18
 */
public interface TenantSettingInfoManageMapper {

  /**
   * 根据主键获取租户设置信息
   */
  TenantSettingInfoDTO getTenantSettingInfo(@Param("tenantId") Long tenantId, @Param("id") Long settingId);

  /**
   * 新增租户设置信息
   *
   * @param tenantSettingInfo 租户设置信息
   * @return 结果
   */
  int insertTenantSettingInfo(@Param("dto") TenantSettingInfoDTO tenantSettingInfo);

  /**
   * 修改租户设置信息
   *
   * @param tenantSettingInfo 租户设置信息
   * @return 结果
   */
  int updateTenantSettingInfo(@Param("dto") TenantSettingInfoDTO tenantSettingInfo);

  /**
   * 删除租户设置信息
   *
   * @param settingId 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteTenantSettingInfo(@Param("settingId") Long settingId, @Param("updatorId") Long updatorId);

  /**
   * 获取租户设置信息列表
   *
   * @param tenantId 租户 ID
   * @return 租户设置信息列表
   */
  List<TenantSettingInfoDTO> selectTenantSettingInfoList(@Param("tenantId") Long tenantId);

  /**
   * 根据租户ID和功能类型获取租户设置信息列表
   *
   * @param queryParams 查询参数
   * @return 租户设置信息列表
   */
  List<TenantSettingInfoDTO> selectTenantSettingInfoListByFuncTypes(@Param("query")TenantQueryParams queryParams);

  /**
   * 根据租户ID和功能类型获取租户设置信息
   *
   * @param tenantId 租户ID
   * @param funcType 功能类型
   * @return 租户设置信息
   */
  TenantSettingInfoDTO getTenantSettingInfoByTenantIdAndType(@Param("tenantId") Long tenantId, @Param("funcType") String funcType);

  /**
   * 根据租户ID和botId检查是否存在FuncSwitch类型的设置信息
   *
   * @param tenantId 租户ID
   * @param botId 智能应用ID
   * @return 是否存在FuncSwitch类型的设置信息
   */
  boolean existsFuncSwitchSettingInfo(@Param("tenantId") Long tenantId, @Param("botId") Long botId);

  /**
   * 根据租户ID和设置ID更新func_switch内容
   *
   * @param tenantId 租户ID
   * @param botId 智能应用ID
   * @param settingInfo func_switch内容
   * @return 结果
   */
  int updateFuncSwitchInfo(@Param("tenantId") Long tenantId, @Param("botId") Long botId, @Param("settingInfo") String settingInfo);

  /**
   * 根据功能类型获取租户设置信息列表
   *
   * @param funcType 功能类型
   * @return 租户设置信息列表
   */
  List<TenantSettingInfoDTO> selectTenantSettingInfoByType(@Param("funcType") String funcType);
}
