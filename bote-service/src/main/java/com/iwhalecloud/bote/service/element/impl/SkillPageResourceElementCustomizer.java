package com.iwhalecloud.bote.service.element.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.skill.SkillAttrSpecDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageFuncDTO;
import com.iwhalecloud.bote.mapper.skill.SkillAttrManageMapper;
import com.iwhalecloud.bote.mapper.skill.SkillPageFuncManageMapper;
import com.iwhalecloud.bote.mapper.skill.SkillPageManageMapper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 配置数据实体关系记录 - 技能 - 页面
 *
 * @author chen.linfa
 * @since 2025-04-09
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.SKILL_PAGE)
@SuppressWarnings("PMD.GuardLogStatement")
public class SkillPageResourceElementCustomizer extends AbstractResourceElementCustomizer {

  private static final Logger logger = LoggerFactory.getLogger(SkillPageResourceElementCustomizer.class);
  private final SkillPageManageMapper pageManageMapper;
  private final SkillPageFuncManageMapper pageFuncManageMapper;
  private final SkillAttrManageMapper attrManageMapper;

  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.SKILL_PAGE.getCode();
  }

  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long pageId) {
    SkillPageDTO page = pageManageMapper.getSkillPage(tenantId, pageId);
    List<ResourceElementDTO> elements = new ArrayList<>(createCatalogElement(tenantId, pageId, page.getCatalogItemId()));
    if (page.getFileInfoId() != null) {
      elements.add(createElement(tenantId, pageId, page.getFileInfoId(), DataSyncCodeEnum.PAGE_FILE.getCode()));
    }
    // 解析平台类型的页面，内嵌高代码组件，关联的文件资源
    if (BaseConsts.PAGE_SOURCE_PLATFORM.equals(page.getPageSourceType()) && StringUtils.isNotEmpty(page.getPageTemplateJson())) {
      parsePageTemplateJson(page, elements);
    }
    return elements;
  }

  @SuppressWarnings("unchecked")
  private void parsePageTemplateJson(SkillPageDTO page, List<ResourceElementDTO> elements) {
    Map<String, Object> json = JsonUtil.parseJsonRequired(page.getPageTemplateJson(), new TypeReference<Map<String, Object>>() {
    });

    // 收集资源编码
    ResourceReferences references = new ResourceReferences();

    // 处理数据源关联的API
    List<Map<String, Object>> dataSources = (List<Map<String, Object>>) json.get("dataSources");
    if (CollectionUtils.isNotEmpty(dataSources)) {
      for (Map<String, Object> dataSource : dataSources) {
        String type = MapUtils.getString(dataSource, "type");
        if (!"api".equals(type)) {
          continue;
        }
        String apiId = MapUtils.getString(dataSource, "apiId");
        if (StringUtils.isNotEmpty(apiId)) {
          references.serviceIds.add(apiId);
        }
      }
    }

    // 递归解析JSON结构，查找各种资源引用
    parseJsonObject(json, references);
    // 处理文件资源
    List<Map<String, Object>> comps = (List<Map<String, Object>>) json.get("children");
    for (Map<String, Object> component : CollectionUtils.emptyIfNull(comps)) {
      parseComponent(component, page, elements);
      if (component.containsKey("children")) {
        List<Map<String, Object>> children = (List<Map<String, Object>>) component.get("children");
        for (Map<String, Object> child : CollectionUtils.emptyIfNull(children)) {
          parseComponent(child, page, elements);
        }
      }
    }

    // 根据收集到的编码建立血缘关系
    buildResourceLineages(page, elements, references);
  }

  @SuppressWarnings("unchecked")
  private void parseComponent(Map<String, Object> component, SkillPageDTO page, List<ResourceElementDTO> elements) {
    if (!"highCodeComp".equals(MapUtils.getString(component, "compType"))) {
      return;
    }
    Map<String, Object> compProps = (Map<String, Object>) component.get("compProps");
    if (!compProps.containsKey("fileResources")) {
      return;
    }
    List<Map<String, Object>> fileResources = (List<Map<String, Object>>) compProps.get("fileResources");
    for (Map<String, Object> fileResource : CollectionUtils.emptyIfNull(fileResources)) {
      Long fileInfoId = MapUtils.getLong(fileResource, "fileInfoId");
      if (fileInfoId != null) {
        elements.add(createElement(page.getTenantId(), page.getPageId(), fileInfoId, DataSyncCodeEnum.PAGE_FILE.getCode()));
      }
    }
  }

  /**
   * 递归解析JSON对象，收集各种资源引用
   */
  @SuppressWarnings("unchecked")
  private void parseJsonObject(Object obj, ResourceReferences references) {
    if (obj == null) {
      return;
    }

    if (obj instanceof Map) {
      parseMapObject((Map<String, Object>) obj, references);
    }
    else if (obj instanceof List) {
      parseListObject((List<Object>) obj, references);
    }
  }

  /**
   * 解析Map类型对象
   */
  private void parseMapObject(Map<String, Object> map, ResourceReferences references) {
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      parseMapEntry(map, entry, references);
      // 递归解析值
      parseJsonObject(entry.getValue(), references);
    }
  }

  /**
   * 解析List类型对象
   */
  private void parseListObject(List<Object> list, ResourceReferences references) {
    for (Object item : list) {
      parseJsonObject(item, references);
    }
  }

  /**
   * 解析Map条目
   */
  private void parseMapEntry(Map<String, Object> map, Map.Entry<String, Object> entry, ResourceReferences references) {
    String key = entry.getKey();
    Object value = entry.getValue();

    if ("content".equals(key) && value instanceof String) {
      handleContentField(map, (String) value, references);
    }
    else if ("staticCode".equals(key) && value instanceof String) {
      handleStaticCodeField((String) value, references.staticDataCodes);
    }
  }

  /**
   * 处理content字段
   */
  private void handleContentField(Map<String, Object> map, String content, ResourceReferences references) {
    if (StringUtils.isBlank(content) || !isValidCode(content)) {
      return;
    }

    String code = getCodeFromContext(map);
    if ("pageFunc".equals(code)) {
      references.pageFuncCodes.add(content);
    }
    else if ("page".equals(code)) {
      references.pageIds.add(content);
    }
    else if ("service".equals(code)) {
      references.serviceIds.add(content);
    }
    else if ("oneStep".equals(code)) {
      references.flowIds.add(content);
    }
    else if ("callScene".equals(code)) {
      // 只解析固定选择类型
      if (StringUtils.isNotEmpty(content) && content.contains("@")) {
        String[] split = StringUtils.split(content, "@");
        references.sceneIds.add(split[1]);
      }
    }
  }

  /**
   * 处理staticCode字段
   */
  private void handleStaticCodeField(String staticCode, Set<String> staticDataCodes) {
    if (StringUtils.isNotBlank(staticCode) && isValidCode(staticCode)) {
      staticDataCodes.add(staticCode);
    }
  }

  /**
   * 从上下文中获取code字段的值
   */
  private String getCodeFromContext(Map<String, Object> map) {
    Object codeObj = map.get("code");
    return codeObj instanceof String ? (String) codeObj : null;
  }

  /**
   * 建立所有资源血缘关系
   */
  private void buildResourceLineages(SkillPageDTO page, List<ResourceElementDTO> elements, ResourceReferences references) {
    Long tenantId = page.getTenantId();
    Long pageId = page.getPageId();

    // 建立页面函数血缘关系
    references.pageFuncCodes.parallelStream().forEach(funcCode ->
      buildResourceLineage(tenantId, pageId, funcCode, elements, (tid, code) -> {
        SkillPageFuncDTO pageFunc = findPageFuncByCode(tid, code);
        return pageFunc != null ? pageFunc.getPageFuncId() : null;
      }, DataSyncCodeEnum.SKILL_PAGE_FUNC.getCode())
    );

    // 建立静态数据血缘关系
    references.staticDataCodes.parallelStream().forEach(attrCode ->
      buildResourceLineage(tenantId, pageId, attrCode, elements, (tid, code) -> {
        SkillAttrSpecDTO attrSpec = findAttrSpecByCode(tid, code);
        return attrSpec != null ? attrSpec.getAttrId() : null;
      }, DataSyncCodeEnum.SKILL_ATTR.getCode())
    );

    // 建立页面血缘关系
    references.pageIds.parallelStream().forEach(targetPageId ->
      buildResourceLineage(tenantId, pageId, targetPageId, elements, (tid, code) -> convertToLong(code), DataSyncCodeEnum.SKILL_PAGE.getCode(),
        (tid, rid) -> pageManageMapper.getSkillPage(tid, rid) != null)
    );

    // 建立服务血缘关系
    references.serviceIds.parallelStream().forEach(serviceId ->
      buildResourceLineage(tenantId, pageId, serviceId, elements, (tid, code) -> convertToLong(code), DataSyncCodeEnum.SKILL_SERVICE.getCode())
    );

    // 建立流程血缘关系
    references.flowIds.parallelStream().forEach(flowId ->
      buildResourceLineage(tenantId, pageId, flowId, elements, (tid, code) -> convertToLong(code), DataSyncCodeEnum.SKILL_FLOW.getCode())
    );

    // 建立场景血缘关系
    references.sceneIds.parallelStream().forEach(sceneId ->
      buildResourceLineage(tenantId, pageId, sceneId, elements, (tid, code) -> convertToLong(code), DataSyncCodeEnum.SCENE.getCode())
    );
  }

  /**
   * 建立资源血缘关系通用方法
   */
  private void buildResourceLineage(Long tenantId, Long pageId, String resourceCode, List<ResourceElementDTO> elements,
    BiFunction<Long, String, Long> idResolver, String elementType) {
    buildResourceLineage(tenantId, pageId, resourceCode, elements, idResolver, elementType, (tid, rid) -> true);
  }

  /**
   * 建立资源血缘关系通用方法（带资源存在性验证）
   */
  private void buildResourceLineage(Long tenantId, Long pageId, String resourceCode, List<ResourceElementDTO> elements,
    BiFunction<Long, String, Long> idResolver, String elementType, BiFunction<Long, Long, Boolean> validator) {
    try {
      Long resourceId = idResolver.apply(tenantId, resourceCode);
      if (resourceId != null && validator.apply(tenantId, resourceId)) {
        elements.add(createElement(tenantId, pageId, resourceId, elementType));
      }
    }
    catch (Exception e) {
      logger.error("查找资源失败: resourceCode={}, elementType={}, error={}", resourceCode, elementType, e.getMessage(), e);
    }
  }

  /**
   * 转换字符串为Long类型
   */
  private Long convertToLong(String value) {
    if (StringUtils.isBlank(value)) {
      return null;
    }
    try {
      return Long.parseLong(value);
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * 根据函数编码查找页面函数
   */
  private SkillPageFuncDTO findPageFuncByCode(Long tenantId, String funcCode) {
    // 先查询租户级页面函数
    List<SkillPageFuncDTO> pageFuncs = pageFuncManageMapper.selectSkillPageFuncByCode(tenantId, funcCode);
    if (CollectionUtils.isNotEmpty(pageFuncs)) {
      return pageFuncs.get(0);
    }

    return null;
  }

  /**
   * 根据属性编码查找静态数据
   */
  private SkillAttrSpecDTO findAttrSpecByCode(Long tenantId, String attrCode) {
    // 先查询租户级静态数据
    SkillAttrSpecDTO attrSpec = attrManageMapper.getAttrSpecByCode(attrCode, tenantId);
    if (attrSpec != null) {
      return attrSpec;
    }

    // 如果租户级没有找到，查询平台级静态数据
    if (!BaseConsts.PLATFORM_TENANT_ID.equals(tenantId)) {
      attrSpec = attrManageMapper.getAttrSpecByCode(attrCode, BaseConsts.PLATFORM_TENANT_ID);
      return attrSpec;
    }

    return null;
  }

  /**
   * 验证编码是否有效（简单验证：非空且不包含特殊字符）
   */
  private boolean isValidCode(String code) {
    return StringUtils.isNotBlank(code) && !code.contains("${") && !code.contains("$.") && !code.startsWith("http");
  }

  /**
   * 资源引用收集器，用于统一管理各种资源类型的引用集合
   */
  private static final class ResourceReferences {

    private final Set<String> pageFuncCodes = new HashSet<>();

    private final Set<String> staticDataCodes = new HashSet<>();

    private final Set<String> pageIds = new HashSet<>();

    private final Set<String> serviceIds = new HashSet<>();

    private final Set<String> flowIds = new HashSet<>();

    private final Set<String> sceneIds = new HashSet<>();
  }
}
