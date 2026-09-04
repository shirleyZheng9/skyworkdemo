package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.cache.AttrSpecCache;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.base.SimpleAttrDTO;
import com.iwhalecloud.bote.dto.plugin.params.AttrDataTransformParams;
import com.iwhalecloud.bote.dto.skill.SkillAttrValueDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 数据转换插件执行器
 *
 * @author zyt
 * @since 2025-06-30
 */
@Component
public class AttrDataTransformPlugin extends AbstractPlugin<AttrDataTransformParams> {

  private final AttrSpecCache attrSpecCache;

  public AttrDataTransformPlugin(AttrSpecCache attrSpecCache) {
    super(AttrDataTransformParams.class);
    this.attrSpecCache = attrSpecCache;
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.ATTR_DATA_TRANSFORM;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("attrCode", "数据编码", AttrDataType.STRING));
    children.add(ParameterSpec.newList("inputValue", "要转换的值【数组】（要转换的值，支持单个或多个值）",
      ParameterSpec.newProperty("element", "输入值", AttrDataType.STRING)));
    children.add(ParameterSpec.newProperty("transformDirection", "转换方向（name_to_value-属性值名称转属性值，value_to_name-属性值转属性值名称）", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    // 返回result对象，result下放数组
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newList("result", "转换结果（值数组）",
      ParameterSpec.newProperty("element", "数组元素", AttrDataType.STRING)));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(AttrDataTransformParams params) {
    Assert.hasText(params.getAttrCode(), "attrCode不能为空");
    Assert.notNull(params.getInputValue(), "inputValue不能为null");
    Assert.hasText(params.getTransformDirection(), "transformDirection不能为空");

    // 校验transformDirection的合法性
    String transformDirection = params.getTransformDirection();
    if (!"name_to_value".equals(transformDirection) && !"value_to_name".equals(transformDirection)) {
      throw new BssException("转换方向错误，请检查【1、name_to_value 表示属性值名称转换为属性值\n" + "2、value_to_name 表示属性值转换为属性值名称】");
    }
  }

  @Override
  public Object doRun(AttrDataTransformParams params) {
    String attrCode = params.getAttrCode();
    List<String> inputValues = params.getInputValue();
    String transformDirection = params.getTransformDirection();

    // 如果inputValue为空数组，直接返回空结果
    if (CollectionUtils.isEmpty(inputValues)) {
      return Collections.singletonMap("result", Collections.emptyList());
    }

    // 获取当前租户ID
    Long tenantId = TenantIdUtil.getTenantId();

    // 使用AttrSpecCache查询静态数据
    List<SimpleAttrDTO> simpleAttrList = attrSpecCache.get(tenantId, attrCode);
    if (CollectionUtils.isEmpty(simpleAttrList)) {
      return Collections.singletonMap("result", Collections.emptyList());
    }

    // 直接查找匹配的属性值并返回转换结果
    List<String> resultValues = findMatchedValues(simpleAttrList, inputValues, transformDirection);

    // 返回值数组格式的结果
    return Collections.singletonMap("result", resultValues);
  }

  /**
   * 查找匹配的属性值并直接返回转换结果
   */
  private List<String> findMatchedValues(List<SimpleAttrDTO> attrValues, List<String> inputValues, String transformDirection) {
    List<String> resultValues = new ArrayList<>();

    boolean isNameToValue = "name_to_value".equals(transformDirection);
    boolean isValueToName = "value_to_name".equals(transformDirection);

    for (SimpleAttrDTO simpleAttr : attrValues) {
      SkillAttrValueDTO attrValue = new SkillAttrValueDTO();
      attrValue.setAttrValueName(simpleAttr.getAttrValueName());
      attrValue.setAttrValue(simpleAttr.getAttrValue());

      for (String inputValue : inputValues) {
        if (isNameToValue) {
          // 属性值名称 → 属性值：匹配属性值名称，返回属性值
          if (Objects.equals(attrValue.getAttrValueName(), inputValue)) {
            resultValues.add(attrValue.getAttrValue());
          }
        } else if (isValueToName) {
          // 属性值 → 属性值名称：匹配属性值，返回属性值名称
          if (Objects.equals(attrValue.getAttrValue(), inputValue)) {
            resultValues.add(attrValue.getAttrValueName());
          }
        }
      }
    }
    return resultValues;
  }
}
