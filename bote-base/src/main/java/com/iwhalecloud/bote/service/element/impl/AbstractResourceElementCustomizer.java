package com.iwhalecloud.bote.service.element.impl;

import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.mapper.base.CatalogManageMapper;
import com.iwhalecloud.bote.mapper.base.ResourceElementMapper;
import com.iwhalecloud.bote.service.element.IResourceElementCustomizer;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 配置数据实体关系记录 - 抽象类
 *
 * @author chen.linfa
 * @since 2025-04-09
 */
public abstract class AbstractResourceElementCustomizer implements IResourceElementCustomizer {

  protected static final ResourceElementMapper mapper = SpringUtil.getBean(ResourceElementMapper.class);
  protected static final CatalogManageMapper catalogManageMapper = SpringUtil.getBean(CatalogManageMapper.class);
  protected static final JdbcTemplate jdbcTemplate = SpringUtil.getBean(JdbcTemplate.class);

  @Override
  public void submit(Long tenantId, Long resourceId) {
    // 第一步，清理旧实体关系
    mapper.deleteElementByResourceId(tenantId, resourceId, null, null);

    // 第二步，计算新的实体关系
    List<ResourceElementDTO> elements = compute(tenantId, resourceId);

    // 第三步，保存实体关系
    if (CollectionUtils.isNotEmpty(elements)) {
      // 分批插入
      for (List<ResourceElementDTO> partitionedElements : ListUtils.partition(elements, 500)) {
        mapper.batchInsertResourceElement(partitionedElements);
      }
    }
  }

  @Override
  public void clear(Long tenantId, Long resourceId) {
    mapper.deleteElementByResourceId(tenantId, resourceId, null, null);
  }

  /**
   * 获取配置对应的资源类型
   *
   * @return 资源类型
   */
  protected abstract String getResourceType();

  /**
   * 按照不同配置功能，计算实体关系
   *
   * @param tenantId 租户 ID
   * @param resourceId 资源 ID
   * @return 实体关系
   */
  protected abstract List<ResourceElementDTO> compute(Long tenantId, Long resourceId);

  /**
   * 构造实体关系
   *
   * @param tenantId 租户 ID
   * @param resourceId 资源 ID
   * @param elementId 实体 ID
   * @param elementType 实体类型
   * @return 实体关系
   */
  protected ResourceElementDTO createElement(Long tenantId, Long resourceId, Long elementId, String elementType) {
    ResourceElementDTO element = new ResourceElementDTO();
    element.setResourceElementId(IDUtils.nextId());
    element.setTenantId(tenantId);
    element.setResourceId(resourceId);
    element.setResourceType(getResourceType());
    element.setElementId(elementId);
    element.setElementType(elementType);
    element.setStatusCd(CommonConsts.STATUS_CD_VALID);
    element.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    element.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    return element;
  }

  /**
   * 构造实体关系（目录）
   * <p>收集 bt_catalog_item 表数据，包括父目录</p>
   *
   * @param tenantId 租户 ID
   * @param resourceId 资源 ID
   * @param catalogId 目录 ID
   * @return 实体关系
   */
  protected List<ResourceElementDTO> createCatalogElement(Long tenantId, Long resourceId, Long catalogId) {
    List<ResourceElementDTO> elements = new ArrayList<>();
    CatalogDTO catalog = catalogManageMapper.getCatalog(tenantId, catalogId);
    if (catalog == null) {
      return elements;
    }
    List<Long> paths = new ArrayList<>();
    paths.add(catalogId);
    if (StringUtils.isNotEmpty(catalog.getCatalogPath())) {
      paths.addAll(Arrays.stream(catalog.getCatalogPath().split(",")).map(Long::valueOf).collect(Collectors.toList()));
    }
    for (Long path : paths) {
      elements.add(createElement(tenantId, resourceId, path, DataSyncCodeEnum.CATALOG.getCode()));
    }
    return elements;
  }
}
