package com.iwhalecloud.bote.mapper.skill;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.skill.ServiceMockParams;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;

/**
 * 技能：API Mapper
 *
 * @author auto
 * @since 2024-09-15
 */
public interface SkillServiceManageMapper {

  /**
   * 批量新增 API
   *
   * @param list API 列表
   * @return 结果
   */
  int batchInsertSkillService(@Param("list") List<SkillServiceDTO> list);

  /**
   * 新增 API
   *
   * @param service API
   * @return 结果
   */
  int insertSkillService(@Param("dto") SkillServiceDTO service);

  /**
   * 修改 API
   *
   * @param service API
   * @return 结果
   */
  int updateSkillService(@Param("dto") SkillServiceDTO service);

  /**
   * 根据主键获取 API
   */
  SkillServiceDTO getSkillService(@Param("tenantId") Long tenantId, @Param("id") Long serviceId, @Nullable @Param("statusCd") String statusCd);

  /**
   * 根据主键查询 API
   */
  @Nullable
  SkillServiceDTO selectSimpleServiceById(@Param("tenantId") Long tenantId, @Param("id") Long serviceId);

  /**
   * 批量查询 API
   */
  List<SkillServiceDTO> selectSimpleServiceByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> serviceIds);

  /**
   * 获取 API 列表（分页）
   *
   * @param queryParams 查询条件
   * @return API 分页列表
   */
  Page<SkillServiceDTO> selectSkillServicePage(@Param("query") SkillQueryParams queryParams, RowBounds rowBounds);

  /**
   * 删除 API
   */
  int deleteSkillService(@Param("tenantId") Long tenantId, @Param("serviceId") Long serviceId, @Param("updatorId") Long updatorId);

  /**
   * 校验 API 编码唯一性
   *
   * @param service API
   * @return 结果
   */
  Boolean existsServiceCode(@Param("dto") SkillServiceDTO service);

  /**
   * 根据服务编码列表查询服务列表
   *
   * @param serviceCodes 服务编码列表
   * @param tenantId 租户ID
   * @return 服务列表
   */
  List<SkillServiceDTO> selectServiceByServiceCodes(@Param("serviceCodes") List<String> serviceCodes, @Param("tenantId") Long tenantId);

  /**
   * 批量更新服务是否开启/关闭模拟服务
   *
   * @param params Mock参数
   * @return 影响行数
   */
  int updateServiceMock(@Param("params") ServiceMockParams params);

  /**
   * 删除目录下的服务
   */
  int deleteServiceByCatalogId(@Param("tenantId") Long tenantId, @Param("catalogId") Long catalogId, @Param("updatorId") Long updatorId);
}
