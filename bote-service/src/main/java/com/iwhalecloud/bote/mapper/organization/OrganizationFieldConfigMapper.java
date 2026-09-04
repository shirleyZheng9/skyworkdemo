package com.iwhalecloud.bote.mapper.organization;

import com.iwhalecloud.bote.dto.organization.OrganizationFieldConfigDTO;
import com.iwhalecloud.bote.entity.organization.OrganizationFieldConfigEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 组织字段配置 Mapper
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
public interface OrganizationFieldConfigMapper {

  /**
   * 检查字段键名是否已存在
   *
   * @param spaceId 企业空间ID
   * @param fieldKey 字段键名
   * @param configId 排除的配置ID
   * @return 是否存在
   */
  boolean existsFieldKey(@Param("spaceId") Long spaceId, @Param("fieldKey") String fieldKey, @Param("configId") Long configId);

  /**
   * 根据主键获取字段配置
   *
   * @param spaceId 企业空间ID
   * @param configId 配置ID
   * @return 字段配置
   */
  @Nullable
  OrganizationFieldConfigDTO getFieldConfig(@Param("spaceId") Long spaceId, @Param("configId") Long configId);

  /**
   * 根据字段键名获取字段配置
   *
   * @param spaceId 企业空间ID
   * @param fieldKey 字段键名
   * @return 字段配置
   */
  @Nullable
  OrganizationFieldConfigDTO getFieldConfigByKey(@Param("spaceId") Long spaceId, @Param("fieldKey") String fieldKey);

  /**
   * 新增字段配置
   *
   * @param fieldConfig 字段配置
   * @return 影响行数
   */
  int insertFieldConfig(@Param("dto") OrganizationFieldConfigEntity fieldConfig);

  /**
   * 修改字段配置
   *
   * @param fieldConfig 字段配置
   * @return 影响行数
   */
  int updateFieldConfig(@Param("dto") OrganizationFieldConfigDTO fieldConfig);

  /**
   * 删除字段配置
   *
   * @param spaceId 企业空间ID
   * @param configId 配置ID
   * @param updatorId 更新人ID
   * @return 影响行数
   */
  int deleteFieldConfig(@Param("spaceId") Long spaceId, @Param("configId") Long configId, @Param("updatorId") Long updatorId);

  /**
   * 查询企业的所有组织字段配置
   *
   * @param spaceId 企业空间ID
   * @param isEnabled 是否启用（可为空）
   * @param searchContent 查询关键字（可为空）
   * @return 字段配置列表
   */
  List<OrganizationFieldConfigDTO> selectFieldConfigsBySpace(@Param("spaceId") Long spaceId, @Param("isEnabled") String isEnabled, @Param("searchContent") String searchContent);
}
