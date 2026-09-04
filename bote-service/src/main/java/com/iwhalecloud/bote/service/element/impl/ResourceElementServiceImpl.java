package com.iwhalecloud.bote.service.element.impl;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.base.SimpleElementDTO;
import com.iwhalecloud.bote.mapper.base.ResourceElementMapper;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 配置数据实体关系记录
 *
 * @author chen.linfa
 * @since 2025-07-29
 */
@Service
@RequiredArgsConstructor
public class ResourceElementServiceImpl implements IResourceElementService {

  private final ResourceElementMapper mapper;

  @Override
  public void batchAdd(Long tenantId, Long resourceId, String resourceType, List<Long> elementIds, String elementType) {
    if (CollectionUtils.isEmpty(elementIds)) {
      return;
    }
    List<ResourceElementDTO> elements = new ArrayList<>(elementIds.size());
    Long userId = SessionUtil.getLoginInfo().getUserId();
    for (Long elementId : elementIds) {
      ResourceElementDTO element = new ResourceElementDTO();
      element.setResourceElementId(Sequences.RESOURCE_ELEMENT_ID.next());
      element.setTenantId(tenantId);
      element.setResourceId(resourceId);
      element.setResourceType(resourceType);
      element.setElementId(elementId);
      element.setElementType(elementType);
      element.setStatusCd(BaseConsts.STATUS_CD_VALID);
      element.setCreatorId(userId);
      element.setUpdatorId(userId);
      elements.add(element);
    }
    // 分批插入
    for (List<ResourceElementDTO> partitionedElements : ListUtils.partition(elements, 500)) {
      mapper.batchInsertResourceElement(partitionedElements);
    }
  }

  @Override
  public void remove(Long tenantId, Long resourceId, Long elementId, String elementType) {
    mapper.deleteElementByResourceId(tenantId, resourceId, elementType, elementId);
  }

  @Override
  public Map<String, List<SimpleElementDTO>> queryRelatedResource(Long tenantId, List<Long> resourceIds, String resourceType) {
    Map<String, List<SimpleElementDTO>> list = new HashMap<>();
    List<SimpleElementDTO> elements = new ArrayList<>();
    for (Long resourceId : resourceIds) {
      SimpleElementDTO element = new SimpleElementDTO();
      element.setId(resourceId);
      element.setType(resourceType);
      elements.add(element);
    }
    // 构造原始数据，避免计算过程重复处理
    list.put(resourceType, elements);

    doQueryRelatedResource(list, tenantId, resourceIds, resourceType);
    // 清理原始数据
    list.get(resourceType).removeIf(e -> resourceIds.contains(e.getId()));
    return list;
  }

  @Override
  public boolean existsRelatedResource(Long tenantId, Long elementId, String elementType) {
    return mapper.existsRelatedResource(tenantId, elementId, elementType);
  }

  private void doQueryRelatedResource(Map<String, List<SimpleElementDTO>> list, Long tenantId, List<Long> resourceIds, String resourceType) {
    Map<String, List<SimpleElementDTO>> group = CollectionUtils.emptyIfNull(mapper.selectSimpleElement(tenantId, resourceIds, resourceType)).stream()
      .filter(p -> StringUtils.isNotEmpty(p.getName())).collect(Collectors.groupingBy(SimpleElementDTO::getType));
    if (MapUtils.isNotEmpty(group)) {
      for (Entry<String, List<SimpleElementDTO>> entry : group.entrySet()) {
        List<Long> ids = entry.getValue().stream().map(SimpleElementDTO::getId).collect(Collectors.toList());
        List<SimpleElementDTO> datas = list.get(entry.getKey());
        if (CollectionUtils.isEmpty(datas)) {
          datas = new ArrayList<>();
        }
        List<Long> exists = datas.stream().map(SimpleElementDTO::getId).collect(Collectors.toList());
        datas.addAll(entry.getValue());
        list.put(entry.getKey(), datas);
        // 关联元素作为资源，进行递归处理
        ids.removeAll(exists);
        if (CollectionUtils.isNotEmpty(ids)) {
          doQueryRelatedResource(list, tenantId, ids, entry.getKey());
        }
      }
    }
  }
}
