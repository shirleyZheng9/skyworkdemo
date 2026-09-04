package com.iwhalecloud.bote.service.organization.impl;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.OrganizationConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.organization.OrganizationFieldConfigDTO;
import com.iwhalecloud.bote.entity.organization.OrganizationFieldConfigEntity;
import com.iwhalecloud.bote.mapper.organization.OrganizationFieldConfigMapper;
import com.iwhalecloud.bote.service.organization.IOrganizationFieldConfigService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 组织字段配置管理服务实现类
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
@Service
@RequiredArgsConstructor
public class OrganizationFieldConfigServiceImpl implements IOrganizationFieldConfigService {

  private final OrganizationFieldConfigMapper fieldConfigMapper;

  @Override
  @Transactional
  public ResultVO<OrganizationFieldConfigDTO> saveFieldConfig(OrganizationFieldConfigDTO fieldConfig) {
    // 参数校验
      Assert.notNull(fieldConfig, "字段配置不能为空");
      Assert.notNull(fieldConfig.getSpaceId(), "企业空间ID不能为空");
      Assert.hasText(fieldConfig.getFieldKey(), "字段键名不能为空");
      Assert.hasText(fieldConfig.getFieldName(), "字段名称不能为空");
      Assert.hasText(fieldConfig.getFieldType(), "字段类型不能为空");
      // 检查字段键名是否重复
      if (checkFieldKeyExists(fieldConfig.getSpaceId(), fieldConfig.getFieldKey(), fieldConfig.getConfigId())) {
        throw new BssException(OrganizationConsts.ERROR_FIELD_KEY_EXISTS);
      }
      OrganizationFieldConfigDTO result;
      if (fieldConfig.getConfigId() == null) {
        // 新增字段配置
        result = insertFieldConfig(fieldConfig);
      }
      else {
        // 更新字段配置
        result = updateFieldConfig(fieldConfig);
      }
      return ResultVO.success(result);
  }

  private OrganizationFieldConfigDTO insertFieldConfig(OrganizationFieldConfigDTO fieldConfig) {
    // 设置默认值
    setDefaultValues(fieldConfig);
    // 生成主键ID
    if (fieldConfig.getConfigId() == null) {
      fieldConfig.setConfigId(Sequences.ORGANIZATION_FIELD_CONFIG_ID.next());
    }
    // 转换为Entity进行插入
    OrganizationFieldConfigEntity entity = convertToEntity(fieldConfig);
    entity.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    int result = fieldConfigMapper.insertFieldConfig(entity);
    if (result <= 0) {
      throw new BssException("插入字段配置失败");
    }
    // 返回插入后的数据
    fieldConfig.setConfigId(entity.getConfigId());
    return fieldConfig;
  }

  private OrganizationFieldConfigDTO updateFieldConfig(OrganizationFieldConfigDTO fieldConfig) {
    // 查询原始数据
    OrganizationFieldConfigDTO originalConfig = findFieldConfig(fieldConfig.getSpaceId(), fieldConfig.getConfigId());
    if (originalConfig == null) {
      throw new BssException(OrganizationConsts.ERROR_FIELD_CONFIG_NOT_EXISTS);
    }
    fieldConfig.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    int result = fieldConfigMapper.updateFieldConfig(fieldConfig);
    if (result <= 0) {
      throw new BssException("更新字段配置失败");
    }
    return fieldConfig;
  }

  private void setDefaultValues(OrganizationFieldConfigDTO fieldConfig) {
    if (StringUtils.isBlank(fieldConfig.getStatusCd())) {
      fieldConfig.setStatusCd(BaseConsts.STATUS_CD_VALID);
    }
  }

  private OrganizationFieldConfigEntity convertToEntity(OrganizationFieldConfigDTO dto) {
    OrganizationFieldConfigEntity entity = new OrganizationFieldConfigEntity();
    entity.setConfigId(dto.getConfigId());
    entity.setSpaceId(dto.getSpaceId());
    entity.setFieldKey(dto.getFieldKey());
    entity.setFieldName(dto.getFieldName());
    entity.setFieldType(dto.getFieldType());
    entity.setStatusCd(dto.getStatusCd());
    // 处理字段选项
    if (dto.getFieldOptions() != null && !dto.getFieldOptions().isEmpty()) {
      entity.setFieldOptions(JsonUtil.toJsonString(dto.getFieldOptions()));
    }
    return entity;
  }

  @Override
  @Nullable
  public OrganizationFieldConfigDTO findFieldConfig(Long spaceId, Long configId) {
    return fieldConfigMapper.getFieldConfig(spaceId, configId);
  }

  @Override
  @Nullable
  public OrganizationFieldConfigDTO findFieldConfigByKey(Long spaceId, String fieldKey) {
    return fieldConfigMapper.getFieldConfigByKey(spaceId, fieldKey);
  }

  @Override
  public List<OrganizationFieldConfigDTO> queryFieldConfigsBySpace(Long spaceId, Boolean isEnabled, @Nullable String searchContent) {
    return fieldConfigMapper.selectFieldConfigsBySpace(spaceId, isEnabled ? BaseConsts.STATUS_CD_VALID : BaseConsts.STATUS_CD_INVALID, searchContent);
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteFieldConfig(Long spaceId, Long configId) {
    // 查询字段配置是否存在
      OrganizationFieldConfigDTO fieldConfig = findFieldConfig(spaceId, configId);
      if (fieldConfig == null) {
        throw new BssException(OrganizationConsts.ERROR_FIELD_CONFIG_NOT_EXISTS);
      }
      int result = fieldConfigMapper.deleteFieldConfig(spaceId, configId, SessionUtil.getLoginInfo().getUserId());
      if (result <= 0) {
        throw new BssException("删除字段配置失败");
      }
      return ResultVO.success();
  }

  @Override
  public boolean checkFieldKeyExists(Long spaceId, String fieldKey, Long excludeConfigId) {
    return fieldConfigMapper.existsFieldKey(spaceId, fieldKey, excludeConfigId);
  }

  @Override
  public ResultVO<Void> validateExtFields(Long spaceId, Map<String, Object> extFields) {
    if (extFields.isEmpty()) {
        return ResultVO.success();
      }
      // 获取字段配置
    List<OrganizationFieldConfigDTO> fieldConfigs = queryFieldConfigsBySpace(spaceId, true, null);
      for (Map.Entry<String, Object> entry : extFields.entrySet()) {
        String fieldKey = entry.getKey();
        Object fieldValue = entry.getValue();
        // 查找字段配置
        OrganizationFieldConfigDTO fieldConfig = fieldConfigs.stream()
          .filter(config -> fieldKey.equals(config.getFieldKey()))
          .findFirst()
          .orElse(null);
        if (fieldConfig == null) {
          throw new BssException("未知的扩展字段：" + fieldKey);
        }
        // 验证字段值
        if (!validateFieldValue(fieldConfig, fieldValue)) {
          throw new BssException("字段 '" + fieldConfig.getFieldName() + "' 的值格式不正确");
        }
      }
      return ResultVO.success();
  }

  private boolean validateFieldValue(OrganizationFieldConfigDTO fieldConfig, @Nullable Object fieldValue) {
    if (fieldValue == null) {
      return true; // 允许空值
    }
    String fieldType = fieldConfig.getFieldType();
    switch (fieldType) {
      case "text":
      case "textarea":
      case "select":
        return fieldValue instanceof String;
      case "number":
        return fieldValue instanceof Number;
      case "date":
        return fieldValue instanceof String && ((String) fieldValue).matches("\\d{4}-\\d{2}-\\d{2}");
      case "boolean":
        return fieldValue instanceof Boolean;
      case "multi_select":
        return fieldValue instanceof List;
      default:
        return true; // 未知类型默认通过
    }
  }

  @Override
  public Map<String, OrganizationFieldConfigDTO> getFieldConfigMap(Long spaceId, Boolean isEnabled) {
    List<OrganizationFieldConfigDTO> fieldConfigs = queryFieldConfigsBySpace(spaceId, isEnabled, null);
    Map<String, OrganizationFieldConfigDTO> fieldConfigMap = new HashMap<>();
    for (OrganizationFieldConfigDTO config : fieldConfigs) {
      fieldConfigMap.put(config.getFieldKey(), config);
    }
    return fieldConfigMap;
  }
}
