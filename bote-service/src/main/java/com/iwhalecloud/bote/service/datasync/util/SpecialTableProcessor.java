package com.iwhalecloud.bote.service.datasync.util;

import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.dto.base.LabelDTO;
import com.iwhalecloud.bote.dto.base.query.CatalogQueryParams;
import com.iwhalecloud.bote.dto.base.query.LabelQueryParams;
import com.iwhalecloud.bote.dto.model.SimpleLargeModelDTO;
import com.iwhalecloud.bote.mapper.base.CatalogManageMapper;
import com.iwhalecloud.bote.mapper.base.LabelManageMapper;
import com.iwhalecloud.bote.service.model.ILargeModelManageService;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.iwhalecloud.bote.common.consts.DataSyncConsts.FIELD_CATALOG_ID;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.FIELD_CATALOG_NAME;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.FIELD_CATALOG_TYPE;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.FIELD_LABEL_ID;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.FIELD_LABEL_NAME;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.FIELD_LABEL_TYPE;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.FIELD_MODEL_CODE;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.FIELD_MODEL_ID;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.FIELD_PAR_CATALOG_ID;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.FIELD_TO_REMOVE;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.PATH_SEPARATOR;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.PRESET_CATALOG_NAME;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.ROOT_CATALOG_PARENT_ID;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_CATALOG;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_LABEL;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_LIBRARY_LARGE_MODEL;

/**
 * 特殊表处理器 - 处理目录、标签、大模型等特殊表的业务逻辑
 *
 * @author yangran
 * @since 2025-8-15
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class SpecialTableProcessor {
  private static final Logger logger = LoggerFactory.getLogger(SpecialTableProcessor.class);

  /**
   * 处理目录记录的特殊逻辑
   */
  public static void processCatalogRecords(List<Map<String, Object>> records, Long resetTenantId, Map<String, Map<Long, Long>> mappings) {
    if (CollectionUtils.isEmpty(records) || resetTenantId == null) {
      return;
    }

    try {
      Map<Long, Long> catalogMappings = mappings.computeIfAbsent(TABLE_BT_CATALOG, k -> new HashMap<>());
      Map<String, CatalogDTO> targetCatalogStructureMap = queryTargetCatalogStructure(resetTenantId);

      for (Map<String, Object> record : records) {
        processSingleCatalogRecord(record, records, catalogMappings, targetCatalogStructureMap, resetTenantId, mappings);
      }
    }
    catch (Exception e) {
      logger.error("处理目录记录时发生异常: {}", e.getMessage(), e);
    }
  }

  /**
   * 处理大模型记录的特殊逻辑
   */
  public static void processModelRecords(List<Map<String, Object>> records, Long resetTenantId, Map<String, Map<Long, Long>> mappings) {
    if (CollectionUtils.isEmpty(records) || resetTenantId == null) {
      return;
    }

    try {
      ILargeModelManageService modelService = SpringUtil.getBean(ILargeModelManageService.class);
      List<SimpleLargeModelDTO> targetModels = modelService.queryLargeModelList(resetTenantId, null, null);
      Map<String, SimpleLargeModelDTO> targetModelMap = buildModelMap(targetModels);

      for (Map<String, Object> record : records) {
        processModelRecord(record, targetModelMap, mappings);
      }
    }
    catch (Exception e) {
      logger.error("处理大模型记录时发生异常: {}", e.getMessage(), e);
    }
  }

  /**
   * 处理标签记录的特殊逻辑
   */
  public static void processLabelRecords(List<Map<String, Object>> records, Long resetTenantId, Map<String, Map<Long, Long>> mappings) {
    if (CollectionUtils.isEmpty(records) || resetTenantId == null) {
      return;
    }

    try {
      LabelManageMapper labelMapper = SpringUtil.getBean(LabelManageMapper.class);
      List<LabelDTO> targetLabels = labelMapper.selectLabelList(createLabelQueryParams(resetTenantId, null));
      Map<String, LabelDTO> targetLabelMap = buildLabelMap(targetLabels);

      for (Map<String, Object> record : records) {
        processLabelRecord(record, targetLabelMap, mappings);
      }
    }
    catch (Exception e) {
      logger.error("处理标签记录时发生异常: {}", e.getMessage(), e);
    }
  }

  /**
   * 处理API网关和数据源记录的特殊逻辑
   */
  public static void processApiGatewayAndDataSourceRecords(List<Map<String, Object>> records, String tableCode) {
    try {
      for (Map<String, Object> record : records) {
        record.put(FIELD_TO_REMOVE, true);
      }
      logger.debug("跳过复制 {} 表记录，API网关和数据源不需要复制", tableCode);
    }
    catch (Exception e) {
      logger.error("处理API网关和数据源记录时发生异常: {}", e.getMessage(), e);
    }
  }

  private static void processSingleCatalogRecord(Map<String, Object> record, List<Map<String, Object>> allRecords, Map<Long, Long> catalogMappings,
    Map<String, CatalogDTO> targetCatalogStructureMap, Long resetTenantId, Map<String, Map<Long, Long>> mappings) {
    String catalogName = MapUtils.getString(record, FIELD_CATALOG_NAME);
    String catalogType = MapUtils.getString(record, FIELD_CATALOG_TYPE);
    Long parCatalogId = MapUtils.getLong(record, FIELD_PAR_CATALOG_ID);
    Long sourceCatalogId = MapUtils.getLong(record, FIELD_CATALOG_ID);

    if (StringUtils.isEmpty(catalogName) || StringUtils.isEmpty(catalogType) || sourceCatalogId == null) {
      return;
    }

    if (catalogMappings.containsKey(sourceCatalogId)) {
      return;
    }

    if (isPresetCatalog(catalogName, parCatalogId)) {
      handlePresetCatalog(record, resetTenantId, mappings);
      return;
    }

    String sourcePath = buildSourceCatalogPath(record, allRecords);
    String sourceStructureKey = catalogType + "#" + sourcePath;

    logger.debug("处理目录记录: 原ID: {}, 目录名称: {}, 目录类型: {}, 父目录ID: {}, 完整路径: {}", sourceCatalogId, catalogName, catalogType,
      parCatalogId, sourcePath);

    CatalogDTO targetCatalog = targetCatalogStructureMap.get(sourceStructureKey);

    if (targetCatalog != null) {
      catalogMappings.put(sourceCatalogId, targetCatalog.getCatalogId());
      record.put(FIELD_TO_REMOVE, true);
      logger.debug("找到匹配的目录，原ID: {}, 新ID: {}, 结构键: {}", sourceCatalogId, targetCatalog.getCatalogId(), sourceStructureKey);
    }
    else {
      logger.debug("未找到匹配的目录，原ID: {}, 结构键: {}, 目录名称: {}", sourceCatalogId, sourceStructureKey, catalogName);
    }
  }

  private static Map<String, CatalogDTO> queryTargetCatalogStructure(Long targetTenantId) {
    Map<String, CatalogDTO> catalogStructureMap = new HashMap<>();

    try {
      CatalogManageMapper catalogMapper = SpringUtil.getBean(CatalogManageMapper.class);
      CatalogQueryParams queryParams = new CatalogQueryParams();
      queryParams.setTenantId(targetTenantId);

      List<CatalogDTO> targetCatalogs = catalogMapper.selectCatalogList(queryParams);

      if (CollectionUtils.isNotEmpty(targetCatalogs)) {
        for (CatalogDTO catalog : targetCatalogs) {
          String targetPath = buildTargetCatalogPath(catalog, targetCatalogs);
          String structureKey = catalog.getCatalogType() + "#" + targetPath;
          catalogStructureMap.put(structureKey, catalog);

          logger.debug("添加目标目录结构键: {} -> 目录ID: {}, 目录名称: {}, 目录类型: {}", structureKey, catalog.getCatalogId(),
            catalog.getCatalogName(), catalog.getCatalogType());
        }
      }
    }
    catch (Exception e) {
      logger.error("查询目标租户目录结构失败: {}", e.getMessage(), e);
    }

    return catalogStructureMap;
  }

  private static String buildTargetCatalogPath(CatalogDTO catalog, List<CatalogDTO> allCatalogs) {
    String catalogName = catalog.getCatalogName();
    Long parCatalogId = catalog.getParCatalogId();

    if (parCatalogId != null && parCatalogId.equals(ROOT_CATALOG_PARENT_ID)) {
      return catalogName;
    }

    if (parCatalogId != null && !parCatalogId.equals(ROOT_CATALOG_PARENT_ID)) {
      CatalogDTO parent = allCatalogs.stream().filter(c -> c.getCatalogId().equals(parCatalogId)).findFirst().orElse(null);

      if (parent != null) {
        String parentName = parent.getCatalogName();
        if (StringUtils.isNotEmpty(parentName)) {
          return parentName + PATH_SEPARATOR + catalogName;
        }
      }
    }

    return catalogName;
  }

  private static boolean isPresetCatalog(String catalogName, Long parCatalogId) {
    return PRESET_CATALOG_NAME.equals(catalogName) && parCatalogId != null && parCatalogId.equals(ROOT_CATALOG_PARENT_ID);
  }

  private static void handlePresetCatalog(Map<String, Object> record, Long resetTenantId, Map<String, Map<Long, Long>> mappings) {
    try {
      CatalogManageMapper catalogMapper = SpringUtil.getBean(CatalogManageMapper.class);
      String catalogType = MapUtils.getString(record, FIELD_CATALOG_TYPE);

      CatalogQueryParams queryParams = new CatalogQueryParams();
      queryParams.setTenantId(resetTenantId);
      queryParams.setCatalogName(PRESET_CATALOG_NAME);
      queryParams.setCatalogType(catalogType);

      List<CatalogDTO> targetCatalogs = catalogMapper.selectCatalogList(queryParams);

      if (CollectionUtils.isNotEmpty(targetCatalogs)) {
        Long oldId = MapUtils.getLong(record, FIELD_CATALOG_ID);
        Long newId = targetCatalogs.get(0).getCatalogId();

        Map<Long, Long> catalogMappings = mappings.get(TABLE_BT_CATALOG);
        if (catalogMappings != null) {
          catalogMappings.put(oldId, newId);
        }

        record.put(FIELD_TO_REMOVE, true);
        logger.debug("找到匹配的预置分组，类型: {}, 原ID: {}, 新ID: {}", catalogType, oldId, newId);
      }
    }
    catch (Exception e) {
      logger.error("处理预置分组时发生异常: {}", e.getMessage(), e);
    }
  }

  private static String buildSourceCatalogPath(Map<String, Object> record, List<Map<String, Object>> allRecords) {
    String catalogName = MapUtils.getString(record, FIELD_CATALOG_NAME);
    Long parCatalogId = MapUtils.getLong(record, FIELD_PAR_CATALOG_ID);

    if (parCatalogId != null && parCatalogId.equals(ROOT_CATALOG_PARENT_ID)) {
      return catalogName;
    }

    if (parCatalogId != null && !parCatalogId.equals(ROOT_CATALOG_PARENT_ID)) {
      Map<String, Object> parentRecord = allRecords.stream().filter(r -> parCatalogId.equals(MapUtils.getLong(r, FIELD_CATALOG_ID))).findFirst()
        .orElse(null);

      if (parentRecord != null) {
        String parentName = MapUtils.getString(parentRecord, FIELD_CATALOG_NAME);
        if (StringUtils.isNotEmpty(parentName)) {
          return parentName + PATH_SEPARATOR + catalogName;
        }
      }
    }

    return catalogName;
  }

  private static Map<String, SimpleLargeModelDTO> buildModelMap(List<SimpleLargeModelDTO> targetModels) {
    Map<String, SimpleLargeModelDTO> targetModelMap = new HashMap<>();
    if (CollectionUtils.isNotEmpty(targetModels)) {
      for (SimpleLargeModelDTO targetModel : targetModels) {
        if (StringUtils.isNotEmpty(targetModel.getModelCode())) {
          targetModelMap.put(targetModel.getModelCode(), targetModel);
        }
      }
    }
    return targetModelMap;
  }

  private static void processModelRecord(Map<String, Object> record, Map<String, SimpleLargeModelDTO> targetModelMap,
    Map<String, Map<Long, Long>> mappings) {
    String modelCode = MapUtils.getString(record, FIELD_MODEL_CODE);

    if (StringUtils.isNotEmpty(modelCode)) {
      SimpleLargeModelDTO targetModel = targetModelMap.get(modelCode);

      if (targetModel != null) {
        Long oldId = MapUtils.getLong(record, FIELD_MODEL_ID);
        Long newId = targetModel.getModelId();

        Map<Long, Long> modelMappings = mappings.get(TABLE_BT_LIBRARY_LARGE_MODEL);
        if (modelMappings != null) {
          modelMappings.put(oldId, newId);
        }
        record.put(FIELD_TO_REMOVE, true);
      }
    }
  }

  private static Map<String, LabelDTO> buildLabelMap(List<LabelDTO> targetLabels) {
    Map<String, LabelDTO> targetLabelMap = new HashMap<>();
    if (CollectionUtils.isNotEmpty(targetLabels)) {
      for (LabelDTO targetLabel : targetLabels) {
        if (StringUtils.isNotEmpty(targetLabel.getLabelName()) && StringUtils.isNotEmpty(targetLabel.getLabelType())) {
          String key = targetLabel.getLabelName() + "#" + targetLabel.getLabelType();
          targetLabelMap.put(key, targetLabel);
        }
      }
    }
    return targetLabelMap;
  }

  private static void processLabelRecord(Map<String, Object> record, Map<String, LabelDTO> targetLabelMap, Map<String, Map<Long, Long>> mappings) {
    String labelName = MapUtils.getString(record, FIELD_LABEL_NAME);
    String labelType = MapUtils.getString(record, FIELD_LABEL_TYPE);

    if (StringUtils.isNotEmpty(labelName) && StringUtils.isNotEmpty(labelType)) {
      String key = labelName + "#" + labelType;
      LabelDTO targetLabel = targetLabelMap.get(key);

      if (targetLabel != null) {
        Long oldId = MapUtils.getLong(record, FIELD_LABEL_ID);
        Long newId = targetLabel.getLabelId();

        Map<Long, Long> labelMappings = mappings.get(TABLE_BT_LABEL);
        if (labelMappings != null) {
          labelMappings.put(oldId, newId);
        }

        record.put(FIELD_TO_REMOVE, true);
      }
    }
  }

  private static LabelQueryParams createLabelQueryParams(Long tenantId, String labelName) {
    LabelQueryParams params = new LabelQueryParams();
    params.setTenantId(tenantId);
    params.setLabelName(labelName);
    return params;
  }

  private SpecialTableProcessor() {
  }
}
