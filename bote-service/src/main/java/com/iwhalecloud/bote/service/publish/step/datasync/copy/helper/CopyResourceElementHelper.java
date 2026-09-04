package com.iwhalecloud.bote.service.publish.step.datasync.copy.helper;

import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 复制血缘关系复制类
 *
 * @author chen.linfa
 * @since 2025-08-12
 */
@Component
public class CopyResourceElementHelper {

  /**
   * 根据增量数据，收集相关血缘关系
   */
  public List<Map<String, Object>> compute(Map<String, String> codeAndIds, List<Map<String, Object>> allElements) {
    Map<String, List<Map<String, Object>>> group = allElements.stream().collect(Collectors.groupingBy(p -> MapUtils.getString(p, "resource_type")));
    List<Map<String, Object>> list = new ArrayList<>();
    // 按照同步节点逐个处理
    for (DataSyncCodeEnum type : DataSyncCodeEnum.values()) {
      if (!codeAndIds.containsKey(type.getCode()) || StringUtils.isEmpty(codeAndIds.get(type.getCode()))) {
        continue;
      }
      List<Long> values = Arrays.stream(codeAndIds.get(type.getCode()).split("/")).filter(StringUtils::isNumeric).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
      List<Map<String, Object>> elements = CollectionUtils.emptyIfNull(group.get(type.getCode())).stream()
        .filter(p -> values.contains(MapUtils.getLong(p, "resource_id"))).collect(Collectors.toList());
      if (CollectionUtils.isEmpty(elements)) {
        continue;
      }
      list.addAll(elements);
      // 按照元素类型逐个处理
      Map<String, List<Map<String, Object>>> map = elements.stream().collect(Collectors.groupingBy(p -> MapUtils.getString(p, "element_type")));
      for (Entry<String, List<Map<String, Object>>> entry : map.entrySet()) {
        Set<Long> ids = new HashSet<>();
        ids.addAll(entry.getValue().stream().map(p -> MapUtils.getLong(p, "element_id")).collect(Collectors.toList()));
        if (codeAndIds.containsKey(entry.getKey())) {
          // 原有数据
          ids.addAll(Arrays.stream(codeAndIds.get(entry.getKey()).split("/")).filter(StringUtils::isNotEmpty).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList()));
        }
        codeAndIds.put(entry.getKey(), StringUtils.join(ids, "/"));
      }
    }
    return list;
  }

  /**
   * 重置主键场景，血缘关系重新计算
   */
  public void resetId(Map<String, Map<Long, Long>> primaryKeyMappings, List<Map<String, Object>> elements) {
    for (Map<String, Object> element : elements) {
      Long resourceId = MapUtils.getLong(element, "resource_id");
      String resourceType = MapUtils.getString(element, "resource_type");
      Long elementId = MapUtils.getLong(element, "element_id");
      String elementType = MapUtils.getString(element, "element_type");
      // ID 重新生成
      element.put("resource_element_id", IDUtils.nextId());
      element.put("resource_id", getNewId(resourceId, resourceType, primaryKeyMappings));
      element.put("element_id", getNewId(elementId, elementType, primaryKeyMappings));
    }
  }

  private Long getNewId(Long id, String code, Map<String, Map<Long, Long>> primaryKeyMappings) {
    String tableCode = DataSyncCodeEnum.getTableCode(code);
    if (StringUtils.isEmpty(tableCode) || !primaryKeyMappings.containsKey(tableCode)) {
      return id;
    }
    Map<Long, Long> map = primaryKeyMappings.get(tableCode);
    return MapUtils.isNotEmpty(map) && map.containsKey(id) ? map.get(id) : id;
  }
}
