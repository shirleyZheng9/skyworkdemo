package com.iwhalecloud.bote.service.skill.impl.helper;

import com.iwhalecloud.bote.cache.AttrSpecCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.base.SimpleAttrDTO;
import com.iwhalecloud.bote.dto.skill.SimplePageTemplateAttrDTO;
import com.iwhalecloud.bote.dto.skill.SimplePageTemplateDTO;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 页面模板 - 辅助工具
 *
 * @author chen.linfa
 * @since 2024-09-28
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.UnusedFormalParameter")
public class ParsePageTemplateHelper {

  private final AttrSpecCache attrSpecCache;

  public Map<String, List<SimpleAttrDTO>> getStaticCodeList(Long tenantId, SimplePageTemplateDTO pageTemplate) {
    // 唯一的静态数据编码列表
    Set<String> attrCodes = new HashSet<>();
    // 处理指令的静态数据
    for (SimplePageTemplateAttrDTO attr : ListUtils.emptyIfNull(pageTemplate.getAttrs())) {
      if (StringUtils.isNotEmpty(attr.getStaticCode())) {
        attrCodes.add(attr.getStaticCode());
      }
    }

    // 处理页面的各个模板的静态数据
    for (SimplePageTemplateDTO children : CollectionUtils.emptyIfNull(pageTemplate.getChildren())) {
      boolean isForm = BaseConsts.PAGE_TEMPLATE_FORM.equals(children.getTamplateType());
      for (SimplePageTemplateAttrDTO attr : ListUtils.emptyIfNull(children.getAttrs())) {
        String staticCode = isForm ? MapUtils.getString(attr.getData(), "staticCode") : attr.getStaticCode();
        if (StringUtils.isNotEmpty(staticCode)) {
          attrCodes.add(staticCode);
        }
      }
    }

    if (attrCodes.isEmpty()) {
      return null;
    }

    Map<String, List<SimpleAttrDTO>> attrMap = new HashMap<>();
    for (String attrCode : attrCodes) {
      List<SimpleAttrDTO> attrValues = attrSpecCache.get(tenantId, attrCode);
      Assert.notEmpty(attrValues, "静态数据不存在，code=" + attrCode);
      attrMap.put(attrCode, attrValues);
    }
    return attrMap;
  }
}
