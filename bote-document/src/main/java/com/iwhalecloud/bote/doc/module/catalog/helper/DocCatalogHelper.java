package com.iwhalecloud.bote.doc.module.catalog.helper;

import com.iwhalecloud.bote.entity.base.CatalogEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.enums.DocSequences;
import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.dto.base.query.CatalogQueryParams;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import com.iwhalecloud.bote.mapper.portal.TenantQueryMapper;
import com.iwhalecloud.bote.service.base.ICatalogManageService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class DocCatalogHelper {
  private static final Logger logger = LoggerFactory.getLogger(DocCatalogHelper.class);

  private final ICatalogManageService catalogManageService;
  private final TenantQueryMapper tenantQueryMapper;

  public List<CatalogDTO> queryCatalogTree(CatalogQueryParams params) {
    buildCatalogQueryParams(params);
    List<CatalogDTO> catalogList = catalogManageService.queryCatalogTree(params);

    // 如果查询结果为空，尝试创建预置分组
    catalogList = createPresetCatalogIfNeeded(params, catalogList);

    // 根据不同场景返回结果
    return processQueryResult(params, catalogList);
  }

  /**
   * 构建目录查询参数
   */
  private void buildCatalogQueryParams(CatalogQueryParams params) {
    if (!shouldSetTenantId(params)) {
      params.setSpacetenantId(params.getSpacetenantId());
      params.setTenantId(null);
    }
  }

  /**
   * 判断是否需要设置租户ID
   */
  private boolean shouldSetTenantId(CatalogQueryParams params) {
    return StringUtils.isEmpty(params.getEnterprise()) || DocBaseConsts.TRUE.equals(params.getAddAction())
      || DocBaseConsts.TRUE.equals(params.getEnterprise());
  }

  /**
   * 如果需要，创建预置分组
   */
  private List<CatalogDTO> createPresetCatalogIfNeeded(CatalogQueryParams params, List<CatalogDTO> catalogList) {
    if (shouldCreatePresetCatalog(params, catalogList)) {
      createPresetCatalog(params);
      return catalogManageService.queryCatalogTree(params);
    }
    return catalogList;
  }

  /**
   * 创建预置分组
   */
  private void createPresetCatalog(CatalogQueryParams params) {
    try {
      logger.info("为租户 {} 创建预置分组，目录类型：{}", params.getTenantId(), params.getCatalogType());

      CatalogDTO catalog = new CatalogDTO();
      catalog.setCatalogId(DocSequences.CATALOG_ID.next());
      catalog.setParCatalogId(CatalogConsts.DEFAULT_CATALOG_ITEM_PARENT_ID);
      catalog.setCatalogName("预置分组");
      catalog.setCatalogType(params.getCatalogType());
      catalog.setTenantId(params.getTenantId());
      catalog.setSpaceId(params.getSpaceId());
      catalog.setBotId(params.getBotId());
      catalog.setStatusCd(DocBaseConsts.STATUS_CD_VALID);

      // 获取当前用户ID，如果获取不到则使用默认值
      Long userId = null;
      try {
        userId = SessionUtil.getLoginInfo().getUserId();
      }
      catch (Exception e) {
        logger.warn("无法获取当前登录用户ID，使用默认值", e);
        userId = 1L; // 默认用户ID
      }

      catalog.setCreatorId(userId);
      catalog.setUpdatorId(userId);

      catalogManageService.saveCatalog(catalog);
      logger.info("预置分组创建成功，catalogId：{}", catalog.getCatalogId());
    }
    catch (Exception e) {
      logger.error("创建预置分组失败，租户ID：{}，目录类型：{}", params.getTenantId(), params.getCatalogType(), e);
    }
  }

  /**
   * 判断是否需要创建预置分组
   */
  private boolean shouldCreatePresetCatalog(CatalogQueryParams params, List<CatalogDTO> catalogList) {
    return CollectionUtils.isEmpty(catalogList) && params.getCatalogName() == null && params.getCatalogId() == null
      && params.getTenantId() != null;
  }

  /**
   * 处理查询结果
   */
  private List<CatalogDTO> processQueryResult(CatalogQueryParams params, List<CatalogDTO> catalogList) {
    // 如果是新增操作，直接返回转换结果
    if (DocBaseConsts.TRUE.equals(params.getAddAction()) || DocBaseConsts.TRUE.equals(params.getEnterprise())) {
      return catalogList;
    }

    // 如果是查询所有租户的目录，需要重组结构
    if (shouldReorganizeCatalogTree(params, catalogList)) {
      return reorganizeCatalogTree(catalogList, params);
    }

    return catalogList;
  }

  /**
   * 重组目录树：按租户分组，创建企业知识库和项目知识库两个父级目录
   */
  private List<CatalogDTO> reorganizeCatalogTree(List<CatalogDTO> catalogList, CatalogQueryParams params) {
    // 查询空间下的所有租户
    List<TenantDTO> simpleTenantDTOS = tenantQueryMapper.queryTenantList(params.getSpaceId());
    Map<Long, String> tenantNameMap = simpleTenantDTOS.stream().collect(Collectors.toMap(TenantDTO::getTenantId, TenantDTO::getTenantName));

    // 按 tenantId 分组
    Map<Long, List<CatalogDTO>> groupedByTenant = catalogList.stream().collect(Collectors.groupingBy(CatalogEntity::getTenantId));

    // 2. 创建"项目知识库"父级目录（包含所有非 -99 的租户目录）
    List<CatalogDTO> projectTenantCatalogs = new ArrayList<>();
    groupedByTenant.forEach((tenantId, catalogs) -> {
      if (tenantId != null && !tenantId.equals(params.getSpacetenantId())) {
        // 为每个租户创建一个虚拟目录
        String tenantName = tenantNameMap.get(tenantId);
        if (StringUtils.isNotEmpty(tenantName)) {
          CatalogDTO tenantCatalog = createVirtualCatalog(-tenantId, tenantName, tenantId,
            DocBaseConsts.BUSINESS_PAR_CATALOG_ID_PROJECT);
          catalogs.forEach(s -> {
            if (DocBaseConsts.DEFAULT_CATALOG_ITEM_PARENT_ID.equals(s.getParCatalogId())) {
              s.setParCatalogId(-tenantId);
            }
          });
          tenantCatalog.setChildren(catalogs);
          projectTenantCatalogs.add(tenantCatalog);
        }
      }
    });
    return projectTenantCatalogs;
  }

  /**
   * 创建虚拟目录节点
   */
  private CatalogDTO createVirtualCatalog(Long catalogId, String catalogName, Long tenantId, Long parCatalogId) {
    CatalogDTO catalog = new CatalogDTO();
    catalog.setCatalogId(catalogId);
    catalog.setParCatalogId(parCatalogId);
    catalog.setCatalogName(catalogName);
    catalog.setStatusCd("00A");
    catalog.setTenantId(tenantId);
    return catalog;
  }

  /**
   * 判断是否需要重组目录树
   */
  private boolean shouldReorganizeCatalogTree(CatalogQueryParams params, List<CatalogDTO> catalogList) {
    return params.getTenantId() == null && !CollectionUtils.isEmpty(catalogList);
  }

}
