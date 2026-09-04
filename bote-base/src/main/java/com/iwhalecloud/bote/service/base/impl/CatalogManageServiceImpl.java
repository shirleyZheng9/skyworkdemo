package com.iwhalecloud.bote.service.base.impl;

import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.dto.base.query.CatalogQueryParams;
import com.iwhalecloud.bote.mapper.base.CatalogManageMapper;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 目录管理服务
 *
 * @author chen.linfa
 * @since 2024-08-02
 */
@Service
@RequiredArgsConstructor
public class CatalogManageServiceImpl implements ICatalogManageService {

  private final CatalogManageMapper catalogManageMapper;
  private final IResourceElementService resourceElementService;

  @Override
  @Transactional
  public ResultVO<CatalogDTO> saveCatalog(CatalogDTO catalog) {
    Assert.notNull(catalog.getTenantId(), "租户 ID 不能为空");
    Assert.hasLength(catalog.getCatalogType(), "目录类型不能为空");
    Assert.hasLength(catalog.getCatalogName(), "目录名称不能为空");
    if (catalog.getParCatalogId() == null) {
      catalog.setParCatalogId(CatalogConsts.DEFAULT_CATALOG_ITEM_PARENT_ID);
    }
    catalog.setStatusCd(CommonConsts.STATUS_CD_VALID);
    setCatalogPath(catalog);
    CatalogDTO old = catalog.getCatalogId() == null ? null : getCatalog(catalog.getTenantId(), catalog.getCatalogId());
    // 新增目录，或修改目录名称时，检查同级下是否已存在同名目录
    if (old == null || !catalog.getCatalogName().equals(old.getCatalogName())) {
      if (catalogManageMapper.existsCatalogName(catalog.getTenantId(), catalog.getParCatalogId(), catalog.getCatalogType(), catalog.getCatalogName(), catalog.getSpaceId())) {
        return ResultVO.fail("已存在同名目录");
      }
    }
    DataDifference<CatalogDTO> difference = DataDifferenceStarter.computeSave(old, catalog, false, catalog.getTenantId());
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteCatalog(Long tenantId, Long catalogId) {
    if (resourceElementService.existsRelatedResource(tenantId, catalogId, DataSyncCodeEnum.CATALOG.getCode())) {
      return ResultVO.fail("目录已存在关联配置数据，不允许删除");
    }
    catalogManageMapper.deleteCatalog(tenantId, catalogId, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  public List<CatalogDTO> queryCatalogTree(CatalogQueryParams params) {
    List<CatalogDTO> catalogs = catalogManageMapper.selectCatalogList(params);
    if (CollectionUtils.isEmpty(catalogs)) {
      return catalogs;
    }
    List<Long> rootCatalogIds = new ArrayList<>();
    Set<Long> catalogIds = new HashSet<>();
    for (CatalogDTO catalog : catalogs) {
      catalogIds.add(catalog.getCatalogId());
      if (StringUtils.isNotEmpty(catalog.getCatalogPath())) {
        List<Long> ids = Arrays.stream(catalog.getCatalogPath().split(",")).map(Long::valueOf).toList();
        catalogIds.addAll(ids);
        rootCatalogIds.add(ids.getFirst());
      }
      else {
        rootCatalogIds.add(catalog.getCatalogId());
      }
    }

    // 查询分类下的所有目录数据
    params.setCatalogName(null);
    List<CatalogDTO> allCatalogs = catalogManageMapper.selectCatalogList(params).stream().filter(p -> catalogIds.contains(p.getCatalogId()))
      .collect(Collectors.toList());

    // 构造目录树
    List<CatalogDTO> list = new ArrayList<>();
    for (CatalogDTO catalog : allCatalogs) {
      if (rootCatalogIds.contains(catalog.getCatalogId())) {
        list.add(catalog);
        treeCatalog(catalog, allCatalogs);
      }
    }
    return list;
  }

  @Override
  public CatalogDTO getCatalog(Long tenantId, Long catalogId) {
    return catalogManageMapper.getCatalog(tenantId, catalogId);
  }

  private void treeCatalog(CatalogDTO catalog, List<CatalogDTO> catalogs) {
    List<CatalogDTO> children = catalogs.stream().filter(p -> Objects.equals(catalog.getCatalogId(), p.getParCatalogId()))
      .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(children)) {
      return;
    }
    catalog.setChildren(children);
    for (CatalogDTO child : children) {
      treeCatalog(child, catalogs);
    }
  }

  @Override
  public List<Long> queryChildrenCatalogIds(Long tenantId, @Nullable Long catalogId, String catalogType) {
    return queryChildrenCatalogIds(tenantId, null, catalogId, catalogType);
  }

  @Override
  public List<Long> queryChildrenCatalogIds(Long tenantId, @Nullable Long spaceId, @Nullable Long catalogId, String catalogType) {
    if (catalogId == null) {
      return Collections.emptyList();
    }
    List<Long> ids = new ArrayList<>();
    ids.add(catalogId);
    ids.addAll(catalogManageMapper.selectChildrenCatalogIds(tenantId, spaceId, catalogId.toString(), catalogType));
    return ids;
  }

  private void setCatalogPath(CatalogDTO catalog) {
    if (Objects.equals(catalog.getParCatalogId(), CatalogConsts.DEFAULT_CATALOG_ITEM_PARENT_ID)) {
      return;
    }
    CatalogDTO parent = catalogManageMapper.getCatalog(catalog.getTenantId(), catalog.getParCatalogId());
    if (parent != null) {
      String catalogPath = StringUtils.isEmpty(parent.getCatalogPath())
        ? parent.getCatalogId().toString()
        : parent.getCatalogPath() + "," + parent.getCatalogId();
      catalog.setCatalogPath(catalogPath);
    }
  }
}
