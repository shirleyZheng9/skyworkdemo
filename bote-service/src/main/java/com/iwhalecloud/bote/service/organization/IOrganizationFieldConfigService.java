package com.iwhalecloud.bote.service.organization;

import com.iwhalecloud.bote.dto.organization.OrganizationFieldConfigDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import org.springframework.lang.Nullable;
import java.util.List;
import java.util.Map;

/**
 * 组织字段配置管理服务接口
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
public interface IOrganizationFieldConfigService {

  /**
   * 保存字段配置
   *
   * @param fieldConfig 字段配置
   * @return 保存结果
   */
  ResultVO<OrganizationFieldConfigDTO> saveFieldConfig(OrganizationFieldConfigDTO fieldConfig);

  /**
   * 查询单个字段配置
   *
   * @param spaceId 企业空间ID
   * @param configId 配置ID
   * @return 字段配置
   */
  @Nullable
  OrganizationFieldConfigDTO findFieldConfig(Long spaceId, Long configId);

  /**
   * 根据字段键名查询字段配置
   *
   * @param spaceId 企业空间ID
   * @param fieldKey 字段键名
   * @return 字段配置
   */
  @Nullable
  OrganizationFieldConfigDTO findFieldConfigByKey(Long spaceId, String fieldKey);

  /**
   * 查询租户的所有字段配置
   *
   * @param spaceId 企业空间ID
   * @param isEnabled 是否启用（可为空）
   * @param searchContent 查询关键字（可为空）
   * @return 字段配置列表
   */
  List<OrganizationFieldConfigDTO> queryFieldConfigsBySpace(Long spaceId, Boolean isEnabled, String searchContent);


  /**
   * 删除字段配置
   *
   * @param spaceId 企业空间ID
   * @param configId 配置ID
   * @return 删除结果
   */
  ResultVO<Void> deleteFieldConfig(Long spaceId, Long configId);

  /**
   * 检查字段键名是否存在
   *
   * @param spaceId 企业空间ID
   * @param fieldKey 字段键名
   * @param excludeConfigId 排除的配置ID
   * @return 是否存在
   */
  boolean checkFieldKeyExists(Long spaceId, String fieldKey, Long excludeConfigId);

  /**
   * 验证扩展字段数据
   *
   * @param spaceId 企业空间ID
   * @param extFields 扩展字段数据
   * @return 验证结果
   */
  ResultVO<Void> validateExtFields(Long spaceId, Map<String, Object> extFields);

  /**
   * 获取字段配置的映射（字段键 -> 配置）
   *
   * @param spaceId 企业空间ID
   * @param isEnabled 是否启用
   * @return 字段配置映射
   */
  Map<String, OrganizationFieldConfigDTO> getFieldConfigMap(Long spaceId, Boolean isEnabled);

}
