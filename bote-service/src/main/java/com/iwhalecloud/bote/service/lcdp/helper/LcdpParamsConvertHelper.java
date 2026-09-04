package com.iwhalecloud.bote.service.lcdp.helper;

import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.lcdp.LcdpParams;
import com.iwhalecloud.bss.litchi.util.sequence.UUIDUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 灵犀参数转换辅助类
 *
 * @author qian.sisheng
 * @since 2025-06-06
 */
public final class LcdpParamsConvertHelper {

  private static final Map<String, AttrDataType> TYPE_MAP = new HashMap<>();

  static {
    TYPE_MAP.put("1000", AttrDataType.DATE);
    TYPE_MAP.put("1100", AttrDataType.DATETIME);
    TYPE_MAP.put("1200", AttrDataType.STRING);
    TYPE_MAP.put("1300", AttrDataType.NUMBER);
    TYPE_MAP.put("1400", AttrDataType.INTEGER);
    TYPE_MAP.put("1500", AttrDataType.BOOLEAN);
    TYPE_MAP.put("1600", AttrDataType.OBJECT);
    TYPE_MAP.put("1700", AttrDataType.ARRAY);
  }

  private LcdpParamsConvertHelper() {

  }

  /**
   * LcdpParams 转 ParameterSpec
   *
   * @param lcdpParams LcdpParams对象
   * @return ParameterSpec对象
   */
  public static ParameterSpec convert(LcdpParams lcdpParams) {
    // 根节点key为-1，parentKey为null
    return convert(lcdpParams, "-1", true);
  }

  /**
   * 递归转换，带 parentKey
   */
  private static ParameterSpec convert(LcdpParams lcdpParams, String parentKey, boolean isRoot) {
    if (lcdpParams == null) {
      return null;
    }
    String thisKey = isRoot ? "-1" : UUIDUtils.randomFormatUuid();
    AttrDataType attrDataType = mapType(lcdpParams.getType(), lcdpParams.getAttrType());
    Boolean required = "T".equals(lcdpParams.getMustFlag());
    List<ParameterSpec> children = null;
    if (CollectionUtils.isNotEmpty(lcdpParams.getChildren())) {
      children = new ArrayList<>();
      for (LcdpParams child : lcdpParams.getChildren()) {
        children.add(convert(child, thisKey, false));
      }
    }
    // @formatter:off
    return ParameterSpec.builder()
      .name(lcdpParams.getCode())
      .description(lcdpParams.getName())
      .type(attrDataType)
      .required(required)
      .defaultValue(lcdpParams.getDefaultValue())
      .key(thisKey)
      .parentKey(isRoot ? null : parentKey)
      .attrCode(lcdpParams.getCode())
      .children(children)
      .build();
    // @formatter:on
  }

  /**
   * 类型映射
   */
  private static AttrDataType mapType(String type, String attrType) {
    if ("object".equalsIgnoreCase(attrType)) {
      return AttrDataType.OBJECT;
    }
    if ("array".equalsIgnoreCase(attrType)) {
      return AttrDataType.ARRAY;
    }
    if ("field".equalsIgnoreCase(attrType) && StringUtils.isNotBlank(type)) {
      return TYPE_MAP.getOrDefault(type, AttrDataType.ANY);
    }
    return AttrDataType.ANY;
  }
}
