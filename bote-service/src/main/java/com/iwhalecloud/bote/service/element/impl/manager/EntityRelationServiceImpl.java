package com.iwhalecloud.bote.service.element.impl.manager;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.query.EntityPageQueryParams;
import com.iwhalecloud.bote.dto.base.query.EntityRelationQueryParams;
import com.iwhalecloud.bote.dto.base.EntityRelationResultDTO;
import com.iwhalecloud.bote.dto.base.EntityInfoDTO;
import com.iwhalecloud.bote.dto.base.EntityRelationGroupDTO;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.mapper.base.ResourceElementMapper;
import com.iwhalecloud.bote.service.element.IEntityInfoQueryService;
import com.iwhalecloud.bote.service.element.IEntityRelationService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

/**
 * 实体关系查询服务实现类
 *
 * @author qian.sisheng
 * @since 2025-12-03
 */
@Service
@RequiredArgsConstructor
public class EntityRelationServiceImpl implements IEntityRelationService {

  private final ResourceElementMapper resourceElementMapper;
  private final IEntityInfoQueryService entityPageQueryService;

  /** 技能类型前缀 */
  private static final String SKILL_PREFIX = "skill";
  /** 技能分组编码 */
  private static final String SKILL_GROUP_CODE = "skill";
  /** 技能分组名称 */
  private static final String SKILL_GROUP_NAME = "技能";
  /** 忽略的元素类型 */
  private static final List<String> IGNORE_ELEMENT_TYPES = List.of(DataSyncCodeEnum.CATALOG.getCode(), DataSyncCodeEnum.DOCUMENT_FILE.getCode(),
    DataSyncCodeEnum.LIBRARY.getCode(), DataSyncCodeEnum.CORPUS.getCode(), DataSyncCodeEnum.A2A_PLATFORM.getCode(),
    DataSyncCodeEnum.PAGE_FILE.getCode(), DataSyncCodeEnum.LABEL.getCode(), DataSyncCodeEnum.USER_EXPERIENCE.getCode());

  @Override
  public EntityRelationResultDTO queryEntityRelationTree(EntityRelationQueryParams queryParam) {
    Long tenantId = queryParam.getTenantId();
    Long id = queryParam.getEntityId();
    String type = queryParam.getEntityType();
    // 查询引用元素
    List<ResourceElementDTO> elements = resourceElementMapper.selectElementByResourceId(tenantId, id, type);
    // 查询被引元素
    List<ResourceElementDTO> resources = resourceElementMapper.selectResourceByElementId(tenantId, id, type);
    EntityRelationResultDTO result = new EntityRelationResultDTO();
    result.setEntityId(id);
    result.setEntityType(type);
    // 构建引用元素分组
    result.setLinkGroups(buildLinkGroupCounts(elements));
    // 构建被引元素分组
    result.setBackLinkGroups(buildBackLinkGroupCounts(resources));
    return result;
  }

  /**
   * 构建引用元素分组统计(统计数量并返回实体ID列表)
   */
  private List<EntityRelationGroupDTO> buildLinkGroupCounts(List<ResourceElementDTO> elements) {
    if (CollectionUtils.isEmpty(elements)) {
      return null;
    }
    return buildGroupCounts(elements, true);
  }

  /**
   * 构建被引元素分组统计(统计数量并返回实体ID列表)
   */
  private List<EntityRelationGroupDTO> buildBackLinkGroupCounts(List<ResourceElementDTO> resources) {
    if (CollectionUtils.isEmpty(resources)) {
      return null;
    }
    return buildGroupCounts(resources, false);
  }

  /**
   * 构建分组统计的通用方法
   *
   * @param resourceElements 资源元素列表
   * @param isLinkGroup 是否为引用分组（true表示引用分组，false表示被引分组）
   * @return 分组统计结果
   */
  private List<EntityRelationGroupDTO> buildGroupCounts(List<ResourceElementDTO> resourceElements, boolean isLinkGroup) {
    List<EntityRelationGroupDTO> resultGroups = new ArrayList<>();
    Map<String, List<ResourceElementDTO>> skillElements = new HashMap<>();
    // 根据分组类型选择过滤条件和分组依据
    Map<String, List<ResourceElementDTO>> groupedByType = resourceElements.stream()
      .filter(r -> !IGNORE_ELEMENT_TYPES.contains(isLinkGroup ? r.getElementType() : r.getResourceType()))
      .collect(Collectors.groupingBy(r -> isLinkGroup ? r.getElementType() : r.getResourceType()));
    // 处理普通类型元素
    processNormalEntities(groupedByType, isLinkGroup, resultGroups, skillElements);
    // 处理技能类型元素
    processSkillEntities(skillElements, isLinkGroup, resultGroups);
    return resultGroups;
  }

  /**
   * 处理普通类型元素
   */
  private void processNormalEntities(Map<String, List<ResourceElementDTO>> groupedByType, boolean isLinkGroup, List<EntityRelationGroupDTO> resultGroups,
    Map<String, List<ResourceElementDTO>> skillElements) {
    for (Entry<String, List<ResourceElementDTO>> entry : groupedByType.entrySet()) {
      String type = entry.getKey();
      List<ResourceElementDTO> elementList = entry.getValue();
      if (type.startsWith(SKILL_PREFIX)) {
        // 收集技能类型元素
        skillElements.put(type, elementList);
      }
      else {
        // 处理非技能类型元素
        List<Long> ids = elementList.stream().map(r -> isLinkGroup ? r.getElementId() : r.getResourceId()).distinct().toList();
        EntityRelationGroupDTO group = createCountGroup(type, DataSyncCodeEnum.getName(type), ids.size(), ids);
        resultGroups.add(group);
      }
    }
  }

  /**
   * 处理技能类型元素
   */
  private void processSkillEntities(Map<String, List<ResourceElementDTO>> skillElements, boolean isLinkGroup, List<EntityRelationGroupDTO> resultGroups) {
    if (MapUtils.isEmpty(skillElements)) {
      return;
    }
    List<EntityRelationGroupDTO> skillSubGroups = new ArrayList<>();
    int totalCount = 0;
    // 为每个技能子类型创建独立的分组
    for (Entry<String, List<ResourceElementDTO>> skillEntry : skillElements.entrySet()) {
      String skillType = skillEntry.getKey();
      List<ResourceElementDTO> skillElementList = skillEntry.getValue();
      List<Long> ids = skillElementList.stream().map(r -> isLinkGroup ? r.getElementId() : r.getResourceId()).distinct().toList();
      totalCount += ids.size();
      EntityRelationGroupDTO subGroup = createCountGroup(skillType, DataSyncCodeEnum.getName(skillType), ids.size(), ids);
      skillSubGroups.add(subGroup);
    }
    // 创建主技能分组，包含子分组
    EntityRelationGroupDTO skillGroup = new EntityRelationGroupDTO();
    skillGroup.setGroupCode(SKILL_GROUP_CODE);
    skillGroup.setGroupName(SKILL_GROUP_NAME);
    skillGroup.setCount(totalCount);
    skillGroup.setSubGroups(skillSubGroups);
    skillGroup.setEntityIds(Collections.emptyList());
    resultGroups.add(skillGroup);
  }

  /**
   * 创建分组(统计数量并返回实体ID列表)
   *
   * @param groupCode 分组编码
   * @param groupName 分组名称
   * @param count 元素数量
   * @param ids 实体ID列表
   * @return 分组对象
   */
  private EntityRelationGroupDTO createCountGroup(String groupCode, String groupName, Integer count, List<Long> ids) {
    EntityRelationGroupDTO group = new EntityRelationGroupDTO();
    group.setGroupCode(groupCode);
    group.setGroupName(groupName);
    group.setCount(count);
    group.setEntityIds(CollectionUtils.emptyIfNull(ids).stream().map(String::valueOf).collect(Collectors.toList()));
    return group;
  }

  @Override
  public List<EntityInfoDTO> queryEntityDetail(EntityRelationQueryParams queryParam) {
    Long tenantId = queryParam.getTenantId();
    List<EntityInfoDTO> entityInfos = new ArrayList<>();
    if (CollectionUtils.isEmpty(queryParam.getEntities())) {
      return entityInfos;
    }
    // 批量查询实体信息
    for (EntityRelationQueryParams entity : queryParam.getEntities()) {
      List<EntityInfoDTO> entityInfoList = entityPageQueryService.getEntityInfo(tenantId, entity.getEntityType(), new HashSet<>(entity.getEntityIds()));
      CollectionUtils.emptyIfNull(entityInfoList).forEach(entityInfo -> entityInfo.setEntityType(entity.getEntityType()));
      entityInfos.addAll(entityInfoList);
    }
    return entityInfos;
  }

  @Override
  public PageInfo<?> queryEntityPage(EntityPageQueryParams queryParam) {
    return entityPageQueryService.queryEntityPage(queryParam);
  }
}
