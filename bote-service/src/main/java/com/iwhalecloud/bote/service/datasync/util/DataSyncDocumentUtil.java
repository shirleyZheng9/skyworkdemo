package com.iwhalecloud.bote.service.datasync.util;

import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_DC_DOCUMENT;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_DC_DOCUMENT_LIBRARY;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_DC_DOC_CONTENT;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_DC_WORKBOOK_CONTENT;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_DOCUMENT;

import com.google.common.collect.Lists;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.jdbc.LowerCaseColumnMapRowMapper;
import com.iwhalecloud.bote.doc.consts.DocumentTypeEnum;
import com.iwhalecloud.bote.doc.common.utils.DcIdUtils;
import com.iwhalecloud.bote.dto.datasync.DataSyncTableDefinition;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 文档中心数据同步工具类
 *
 * @author chen.linfa
 * @since 2024-10-22
 */
public final class DataSyncDocumentUtil {

  private static final JdbcTemplate jdbcTemplate = SpringUtil.getBean(JdbcTemplate.class);

  private DataSyncDocumentUtil() { }

  /**
   * 收集文档中心数据
   */
  public static void collectDocumentTables(DataSyncTableDefinition definition, DataSyncParams params) {
    String dataConfigCode = definition.getDataConfigCode();
    String tableCode = definition.getTableCode();
    if ((DataSyncCodeEnum.KNOWLEDGE.getCode().equals(dataConfigCode) && TABLE_BT_DOCUMENT.equals(tableCode)) || (
      DataSyncCodeEnum.LIBRARY.getCode().equals(dataConfigCode) && TABLE_BT_DC_DOCUMENT_LIBRARY.equals(tableCode))) {
      List<Map<String, Object>> dataRecords = definition.getDataRecords();
      if (CollectionUtils.isEmpty(dataRecords)) {
        return;
      }
      // 初始化文档中心数据收集列表
      if (params.getDocumentCenterDefinitions() == null) {
        params.setDocumentCenterDefinitions(new ArrayList<>());
      }
      // 根据不同的场景收集相关数据
      if (DataSyncCodeEnum.KNOWLEDGE.getCode().equals(dataConfigCode)) {
        collectKnowledgeBaseDocumentData(dataRecords, params);
      }
      else {
        collectLibraryDocumentData(dataRecords, params);
      }
    }
  }

  /**
   * 处理文档中心数据
   */
  public static List<DataSyncTableDefinition> processDocumentCenterData(DataSyncParams params) {
    List<DataSyncTableDefinition> documentCenterDefinitions = params.getDocumentCenterDefinitions();
    if (CollectionUtils.isEmpty(documentCenterDefinitions)) {
      return new ArrayList<>();
    }
    // 如果是跨租户复制，需要处理ID重新生成
    if (params.isCopy()) {
      handleCrossTenantIdRegeneration(documentCenterDefinitions, params);
    }
    // 去重
    deduplicateDataRecords(documentCenterDefinitions);
    // 重置时间字段格式
    DataSyncDateUtil.toDate(documentCenterDefinitions);
    return documentCenterDefinitions;
  }

  /**
   * 收集知识库文档相关数据
   */
  private static void collectKnowledgeBaseDocumentData(List<Map<String, Object>> dataRecords, DataSyncParams params) {
    if (CollectionUtils.isEmpty(dataRecords)) {
      return;
    }

    // 收集所有dc_document_id
    List<String> dcDocumentIds = extractDcDocumentIds(dataRecords);
    if (CollectionUtils.isEmpty(dcDocumentIds)) {
      return;
    }

    // 批量查询bt_dc_document表
    List<Map<String, Object>> allDcDocuments = queryDcDocumentsByDocumentIds(dcDocumentIds, params.getTenantId());

    // 收集所有library_id
    List<String> libraryIds = extractLibraryIds(allDcDocuments);
    if (CollectionUtils.isEmpty(libraryIds)) {
      return;
    }

    // 批量查询相关表
    List<Map<String, Object>> allLibraries = queryDcDocumentLibrariesByLibraryIds(libraryIds, params.getTenantId());
    List<Map<String, Object>> allRootDocuments = queryRootDocumentsByLibraryIds(libraryIds, params.getTenantId());

    // 收集所有document_id
    List<String> documentIds = extractDocumentIds(allDcDocuments);

    // 批量查询文档内容
    List<Map<String, Object>> allDocContents = queryDocContentsByDocumentIds(documentIds, params.getTenantId());
    List<Map<String, Object>> allWorkbookContents = queryWorkbookContentsByDocumentIds(documentIds, params.getTenantId());

    // 创建映射关系
    Map<String, List<Map<String, Object>>> libraryIdToRootDocuments = createLibraryToRootDocumentsMapping(allRootDocuments);

    // 添加根节点文档和库数据
    addRootDocumentsAndLibraries(params, allRootDocuments, allLibraries);

    // 处理文档的parent_id关系
    updateDocumentParentIds(allDcDocuments, libraryIdToRootDocuments);

    // 添加文档内容数据
    addDocumentContents(params, allDocContents, allWorkbookContents);

    // 添加所有文档数据
    addToDocumentCenterDefinitions(params, TABLE_BT_DC_DOCUMENT, allDcDocuments);
  }

  /**
   * 提取dc_document_id列表
   */
  private static List<String> extractDcDocumentIds(List<Map<String, Object>> dataRecords) {
    return dataRecords.stream()
      .map(record -> record.get("dc_document_id"))
      .filter(Objects::nonNull)
      .map(Object::toString)
      .distinct()
      .collect(Collectors.toList());
  }

  /**
   * 提取library_id列表
   */
  private static List<String> extractLibraryIds(List<Map<String, Object>> allDcDocuments) {
    return allDcDocuments.stream()
      .map(doc -> doc.get("library_id"))
      .filter(Objects::nonNull)
      .map(Object::toString)
      .distinct()
      .collect(Collectors.toList());
  }

  /**
   * 提取document_id列表
   */
  private static List<String> extractDocumentIds(List<Map<String, Object>> allDcDocuments) {
    return allDcDocuments.stream()
      .map(doc -> doc.get("document_id"))
      .filter(Objects::nonNull)
      .map(Object::toString)
      .distinct()
      .collect(Collectors.toList());
  }

  /**
   * 创建库ID到根文档的映射关系
   */
  private static Map<String, List<Map<String, Object>>> createLibraryToRootDocumentsMapping(List<Map<String, Object>> allRootDocuments) {
    return allRootDocuments.stream()
      .collect(Collectors.groupingBy(root -> root.get("library_id").toString()));
  }

  /**
   * 添加根节点文档和库数据
   */
  private static void addRootDocumentsAndLibraries(DataSyncParams params, List<Map<String, Object>> allRootDocuments, List<Map<String, Object>> allLibraries) {
    // 添加所有根节点文档（避免重复）
    addUniqueRecords(params, "bt_dc_document", allRootDocuments, "document_id");

    // 添加所有library数据（避免重复）
    addUniqueRecords(params, "bt_dc_document_library", allLibraries, "library_id");
  }

  /**
   * 更新文档的parent_id关系
   */
  private static void updateDocumentParentIds(List<Map<String, Object>> allDcDocuments, Map<String, List<Map<String, Object>>> libraryIdToRootDocuments) {
    for (Map<String, Object> dcDocument : allDcDocuments) {
      Object libraryId = dcDocument.get("library_id");
      if (libraryId == null) {
        continue;
      }

      String libraryIdStr = libraryId.toString();
      List<Map<String, Object>> rootDocuments = libraryIdToRootDocuments.get(libraryIdStr);
      if (CollectionUtils.isNotEmpty(rootDocuments)) {
        Object rootDocumentId = rootDocuments.get(0).get("document_id");
        if (rootDocumentId != null) {
          dcDocument.put("parent_id", rootDocumentId.toString());
        }
      }
    }
  }

  /**
   * 添加文档内容数据
   */
  private static void addDocumentContents(DataSyncParams params, List<Map<String, Object>> allDocContents, List<Map<String, Object>> allWorkbookContents) {
    // 添加所有文档内容（避免重复）
    addUniqueRecords(params, TABLE_BT_DC_DOC_CONTENT, allDocContents, "document_id");
    // 添加所有工作簿内容（避免重复）
    addUniqueRecords(params, TABLE_BT_DC_WORKBOOK_CONTENT, allWorkbookContents, "document_id");
  }

  /**
   * 添加唯一记录到文档中心定义
   */
  private static void addUniqueRecords(DataSyncParams params, String tableCode, List<Map<String, Object>> records, String idField) {
    Set<String> addedIds = new HashSet<>();
    for (Map<String, Object> record : records) {
      Object id = record.get(idField);
      if (id != null && !addedIds.contains(id.toString())) {
        addToDocumentCenterDefinitions(params, tableCode, List.of(record));
        addedIds.add(id.toString());
      }
    }
  }


  /**
   * 收集文档库相关数据
   */
  private static void collectLibraryDocumentData(List<Map<String, Object>> dataRecords, DataSyncParams params) {
    if (CollectionUtils.isEmpty(dataRecords)) {
      return;
    }

    // 收集所有library_id
    List<String> libraryIds = dataRecords.stream().map(record -> record.get("library_id")).filter(Objects::nonNull).map(Object::toString).distinct()
      .collect(Collectors.toList());

    if (CollectionUtils.isEmpty(libraryIds)) {
      return;
    }

    // 批量查询bt_dc_document表
    List<Map<String, Object>> allDcDocuments = queryDcDocumentsByLibraryIds(libraryIds, params.getTenantId());

    // 收集所有document_id
    List<String> documentIds = allDcDocuments.stream().map(doc -> doc.get("document_id")).filter(Objects::nonNull).map(Object::toString).distinct()
      .collect(Collectors.toList());

    // 批量查询文档内容
    List<Map<String, Object>> allDocContents = queryDocContentsByDocumentIds(documentIds, params.getTenantId());
    List<Map<String, Object>> allWorkbookContents = queryWorkbookContentsByDocumentIds(documentIds, params.getTenantId());


    // 先添加所有文档内容（避免重复）
    Set<String> addedDocContentIds = new HashSet<>();
    for (Map<String, Object> docContent : allDocContents) {
      Object documentId = docContent.get("document_id");
      if (documentId != null && !addedDocContentIds.contains(documentId.toString())) {
        addToDocumentCenterDefinitions(params, TABLE_BT_DC_DOC_CONTENT, List.of(docContent));
        addedDocContentIds.add(documentId.toString());
      }
    }

    Set<String> addedWorkbookContentIds = new HashSet<>();
    for (Map<String, Object> workbookContent : allWorkbookContents) {
      Object documentId = workbookContent.get("document_id");
      if (documentId != null && !addedWorkbookContentIds.contains(documentId.toString())) {
        addToDocumentCenterDefinitions(params, TABLE_BT_DC_WORKBOOK_CONTENT, List.of(workbookContent));
        addedWorkbookContentIds.add(documentId.toString());
      }
    }
    // 添加所有文档数据
    addToDocumentCenterDefinitions(params, TABLE_BT_DC_DOCUMENT, allDcDocuments);
    // 添加原始的library数据（这些数据来自bt_dc_document_library表）
    addToDocumentCenterDefinitions(params, TABLE_BT_DC_DOCUMENT_LIBRARY, dataRecords);
  }

  /**
   * 添加到文档中心定义列表
   */
  private static void addToDocumentCenterDefinitions(DataSyncParams params, String tableCode, List<Map<String, Object>> dataRecords) {
    if (CollectionUtils.isEmpty(dataRecords)) {
      return;
    }

    // 查找是否已存在该表的定义
    DataSyncTableDefinition existingDefinition = params.getDocumentCenterDefinitions().stream().filter(def -> tableCode.equals(def.getTableCode()))
      .findFirst().orElse(null);

    if (existingDefinition == null) {
      // 合并数据, 只有不存在的时候才需要，因为前面已经存储了
      // existingDefinition.getDataRecords().addAll(dataRecords);
      // }
      // else {
      // 文档中心表有定义，数据是自定义的查询逻辑，这里将处理后的数据放进去
      Optional<DataSyncTableDefinition> first = params.getDefinitions().stream().filter(
          definition -> definition.getTableCode().equals(tableCode) && DataSyncCodeEnum.LIBRARY.getCode().equals(definition.getDataConfigCode()))
        .findFirst();
      if (first.isPresent()) {
        DataSyncTableDefinition newDefinition = first.get();
        newDefinition.setDataRecords(new ArrayList<>(dataRecords));
        params.getDocumentCenterDefinitions().add(newDefinition);
      }
    }
  }

  /**
   * 批量根据document_id查询bt_dc_document表
   */
  private static List<Map<String, Object>> queryDcDocumentsByDocumentIds(List<String> documentIds, Long tenantId) {
    if (CollectionUtils.isEmpty(documentIds)) {
      return new ArrayList<>();
    }

    List<Map<String, Object>> allResults = new ArrayList<>();
    int batchSize = 1000;

    // 分批处理，每批最多1000条
    for (List<String> batch : Lists.partition(documentIds, batchSize)) {
      String sql = "SELECT * FROM bt_dc_document WHERE document_id IN (" + StringUtils.repeat("?", ", ", batch.size()) + ") AND tenant_id = ?";
      List<Object> params = new ArrayList<>(batch);
      params.add(tenantId);
      List<Map<String, Object>> batchResults = jdbcTemplate.query(sql, new LowerCaseColumnMapRowMapper(), params.toArray());
      allResults.addAll(batchResults);
    }

    return allResults;
  }

  /**
   * 批量根据library_id查询bt_dc_document表
   */
  private static List<Map<String, Object>> queryDcDocumentsByLibraryIds(List<String> libraryIds, Long tenantId) {
    if (CollectionUtils.isEmpty(libraryIds)) {
      return new ArrayList<>();
    }

    List<Map<String, Object>> allResults = new ArrayList<>();
    int batchSize = 1000;

    // 分批处理，每批最多1000条
    for (List<String> batch : Lists.partition(libraryIds, batchSize)) {
      String sql = "SELECT * FROM bt_dc_document WHERE library_id IN (" + StringUtils.repeat("?", ", ", batch.size()) + ") AND tenant_id = ?";
      List<Object> params = new ArrayList<>(batch);
      params.add(tenantId);
      List<Map<String, Object>> batchResults = jdbcTemplate.query(sql, new LowerCaseColumnMapRowMapper(), params.toArray());
      allResults.addAll(batchResults);
    }

    return allResults;
  }

  /**
   * 批量根据library_id查询bt_dc_document_library表
   */
  private static List<Map<String, Object>> queryDcDocumentLibrariesByLibraryIds(List<String> libraryIds, Long tenantId) {
    if (CollectionUtils.isEmpty(libraryIds)) {
      return new ArrayList<>();
    }

    List<Map<String, Object>> allResults = new ArrayList<>();
    int batchSize = 1000;

    // 分批处理，每批最多1000条
    for (List<String> batch : Lists.partition(libraryIds, batchSize)) {
      String sql = "SELECT * FROM bt_dc_document_library WHERE library_id IN (" + StringUtils.repeat("?", ", ", batch.size()) + ") AND tenant_id = ? AND visibility_scope = 'PUBLIC' ";
      List<Object> params = new ArrayList<>(batch);
      params.add(tenantId);
      List<Map<String, Object>> batchResults = jdbcTemplate.query(sql, new LowerCaseColumnMapRowMapper(), params.toArray());
      allResults.addAll(batchResults);
    }

    return allResults;
  }

  /**
   * 批量根据library_id查询根节点文档
   */
  private static List<Map<String, Object>> queryRootDocumentsByLibraryIds(List<String> libraryIds, Long tenantId) {
    if (CollectionUtils.isEmpty(libraryIds)) {
      return new ArrayList<>();
    }

    List<Map<String, Object>> allResults = new ArrayList<>();
    int batchSize = 1000;

    // 分批处理，每批最多1000条
    for (List<String> batch : Lists.partition(libraryIds, batchSize)) {
      String sql = "SELECT * FROM bt_dc_document WHERE library_id IN (" + StringUtils.repeat("?", ", ", batch.size())
        + ") AND parent_id = '0' AND document_type = 'ROOT' AND tenant_id = ?";
      List<Object> params = new ArrayList<>(batch);
      params.add(tenantId);
      List<Map<String, Object>> batchResults = jdbcTemplate.query(sql, new LowerCaseColumnMapRowMapper(), params.toArray());
      allResults.addAll(batchResults);
    }

    return allResults;
  }

  /**
   * 批量根据document_id查询bt_dc_doc_content表
   */
  private static List<Map<String, Object>> queryDocContentsByDocumentIds(List<String> documentIds, Long tenantId) {
    if (CollectionUtils.isEmpty(documentIds)) {
      return new ArrayList<>();
    }

    List<Map<String, Object>> allResults = new ArrayList<>();
    int batchSize = 1000;

    // 分批处理，每批最多1000条
    for (List<String> batch : Lists.partition(documentIds, batchSize)) {
      String sql = "SELECT * FROM bt_dc_doc_content WHERE document_id IN (" + StringUtils.repeat("?", ", ", batch.size()) + ") AND tenant_id = ?";
      List<Object> params = new ArrayList<>(batch);
      params.add(tenantId);
      List<Map<String, Object>> batchResults = jdbcTemplate.query(sql, new LowerCaseColumnMapRowMapper(), params.toArray());
      allResults.addAll(batchResults);
    }
    return allResults;
  }

  /**
   * 批量根据document_id查询bt_dc_workbook_content表
   */
  private static List<Map<String, Object>> queryWorkbookContentsByDocumentIds(List<String> documentIds, Long tenantId) {
    if (CollectionUtils.isEmpty(documentIds)) {
      return new ArrayList<>();
    }

    List<Map<String, Object>> allResults = new ArrayList<>();
    int batchSize = 1000;

    // 分批处理，每批最多1000条
    for (List<String> batch : Lists.partition(documentIds, batchSize)) {
      String sql =
        "SELECT * FROM bt_dc_workbook_content WHERE document_id IN (" + StringUtils.repeat("?", ", ", batch.size()) + ") AND tenant_id = ?";
      List<Object> params = new ArrayList<>(batch);
      params.add(tenantId);
      List<Map<String, Object>> batchResults = jdbcTemplate.query(sql, new LowerCaseColumnMapRowMapper(), params.toArray());
      allResults.addAll(batchResults);
    }

    return allResults;
  }



  /**
   * 处理跨租户复制的ID重新生成
   */
  private static void handleCrossTenantIdRegeneration(List<DataSyncTableDefinition> documentCenterDefinitions, DataSyncParams params) {
    // 收集所有需要检查的ID
    Set<Long> allDocIds = new HashSet<>();
    Map<Long, String> documentIdMap = new HashMap<>();
    Set<Long> allLibraryIds = new HashSet<>();
    Map<Long, String> libraryIdMap = new HashMap<>();
    boolean isBtDcDocument = false;
    boolean isBtDcDocumentLibeary = false;
    for (DataSyncTableDefinition definition : documentCenterDefinitions) {
      List<Map<String, Object>> dataRecords = definition.getDataRecords();
      if (CollectionUtils.isEmpty(dataRecords)) {
        continue;
      }
      if (TABLE_BT_DC_DOCUMENT.equalsIgnoreCase(definition.getTableCode())) {
        isBtDcDocument = true;
      }
      if (TABLE_BT_DC_DOCUMENT_LIBRARY.equalsIgnoreCase(definition.getTableCode())) {
        isBtDcDocumentLibeary = true;
      }

      for (Map<String, Object> record : dataRecords) {
        collectDocumentIdData(isBtDcDocument, record, allDocIds, documentIdMap);
        // 收集library_id
        collectLibraryIdData(isBtDcDocumentLibeary, record, allLibraryIds, libraryIdMap);
      }
      isBtDcDocument = false;
      isBtDcDocumentLibeary = false;
    }
    // 批量检查目标租户下是否已存在这些ID
    Map<Long, String> existingDocumentIds = checkExistingDocumentIds(new ArrayList<>(allDocIds), params.getResetTenantId());
    Map<Long, String> existingLibraryIds = checkExistingLibraryIds(new ArrayList<>(allLibraryIds), params.getResetTenantId());
    // 创建ID映射关系
    Map<String, String> documentIdMapping = new HashMap<>();
    Map<String, String> libraryIdMapping = new HashMap<>();
    // 为不存在的ID生成新的ID
    for (Entry<Long, String> entry : documentIdMap.entrySet()) {
      if (!existingDocumentIds.containsKey(entry.getKey())) {
        // 需要生成新的document_id
        // 根据文档类型生成对应的ID
        String newDocumentId = generateNewDocumentId(entry.getValue(), documentCenterDefinitions);
        documentIdMapping.put(entry.getValue(), newDocumentId);
      }
      else {
        documentIdMapping.put(entry.getValue(), existingDocumentIds.get(entry.getKey()));
      }
    }
    for (Entry<Long, String> entry : libraryIdMap.entrySet()) {
      if (!existingLibraryIds.containsKey(entry.getKey())) {
        // 需要生成新的library_id
        String newLibraryId = DcIdUtils.createLibraryId();
        libraryIdMapping.put(entry.getValue(), newLibraryId);
      }
      else {
        libraryIdMapping.put(entry.getValue(), existingLibraryIds.get(entry.getKey()));
      }
    }
    // 更新所有相关记录中的ID
    updateDocumentCenterIds(documentCenterDefinitions, documentIdMapping, libraryIdMapping);
    // 更新bt_document表中的dc_document_id
    updateBtDocumentDcDocumentIds(params, documentIdMapping);
  }

  private static void collectDocumentIdData(boolean isBtDcDocument, Map<String, Object> record, Set<Long> allDocIds,
    Map<Long, String> documentIdMap) {
    if (isBtDcDocument) {
      // 收集document_id
      Object documentId = record.get("document_id");
      if (documentId != null) {
        // 收集id
        Object id = record.get("id");
        if (id != null) {
          Long idL = Long.valueOf(id.toString());
          allDocIds.add(idL);
          documentIdMap.put(idL, documentId.toString());
        }
      }
    }
  }

  private static void collectLibraryIdData(boolean isBtDcDocumentLibeary, Map<String, Object> record, Set<Long> allLibraryIds,
    Map<Long, String> libraryIdMap) {
    if (isBtDcDocumentLibeary) {
      // 收集library_id
      Object libraryId = record.get("library_id");
      if (libraryId != null) {
        // 收集id
        Object id = record.get("id");
        if (id != null) {
          Long idL = Long.valueOf(id.toString());
          allLibraryIds.add(idL);
          libraryIdMap.put(idL, libraryId.toString());
        }
      }
    }
  }

  /**
   * 批量检查目标租户下是否已存在document_id
   */
  private static Map<Long, String> checkExistingDocumentIds(List<Long> ids, Long targetTenantId) {
    if (CollectionUtils.isEmpty(ids)) {
      return new HashMap<>();
    }

    Map<Long, String> existingIds = new HashMap<>();
    int batchSize = 1000;

    for (List<Long> batch : Lists.partition(ids, batchSize)) {
      String sql =
        "SELECT id,document_id FROM bt_dc_document WHERE id IN (" + StringUtils.repeat("?", ", ", batch.size()) + ") and status_cd='00A' AND tenant_id = ?";
      List<Object> params = new ArrayList<>(batch);
      params.add(targetTenantId);

      List<Map<String, Object>> results = jdbcTemplate.query(sql, new LowerCaseColumnMapRowMapper(), params.toArray());
      for (Map<String, Object> result : results) {
        existingIds.put(Long.valueOf(result.get("id").toString()), result.get("document_id").toString());
      }
    }
    return existingIds;
  }

  /**
   * 批量检查目标租户下是否已存在library_id
   */
  private static Map<Long, String> checkExistingLibraryIds(List<Long> libraryIds, Long targetTenantId) {
    if (CollectionUtils.isEmpty(libraryIds)) {
      return new HashMap<>();
    }

    Map<Long, String> existingIds = new HashMap<>();
    int batchSize = 1000;

    for (List<Long> batch : Lists.partition(libraryIds, batchSize)) {
      String sql =
        "SELECT id,library_id FROM bt_dc_document_library WHERE id IN (" + StringUtils.repeat("?", ", ", batch.size()) + ") and status_cd='00A' AND tenant_id = ?";
      List<Object> params = new ArrayList<>(batch);
      params.add(targetTenantId);

      List<Map<String, Object>> results = jdbcTemplate.query(sql, new LowerCaseColumnMapRowMapper(), params.toArray());
      for (Map<String, Object> result : results) {
        existingIds.put(Long.valueOf(result.get("id").toString()), result.get("library_id").toString());
      }
    }

    return existingIds;
  }

  /**
   * 根据原document_id和文档类型生成新的document_id
   */
  private static String generateNewDocumentId(String originalDocumentId, List<DataSyncTableDefinition> definitions) {
    // 查找原document_id对应的文档类型
    String documentType = findDocumentType(originalDocumentId, definitions);
    if (documentType != null) {
      DocumentTypeEnum typeEnum = DocumentTypeEnum.getByCode(documentType);
      if (typeEnum != null) {
        return DcIdUtils.createDocumentId(typeEnum);
      }
    }
    // 如果找不到文档类型，使用默认的NODE类型
    return DcIdUtils.createDocumentId(DocumentTypeEnum.FILE);
  }

  /**
   * 查找document_id对应的文档类型
   */
  private static String findDocumentType(String documentId, List<DataSyncTableDefinition> definitions) {
    for (DataSyncTableDefinition definition : definitions) {
      if (!"bt_dc_document".equals(definition.getTableCode())) {
        continue;
      }

      List<Map<String, Object>> dataRecords = definition.getDataRecords();
      if (CollectionUtils.isEmpty(dataRecords)) {
        continue;
      }
      for (Map<String, Object> record : dataRecords) {
        Object recordDocumentId = record.get("document_id");
        if (documentId.equals(recordDocumentId != null ? recordDocumentId.toString() : null)) {
          Object documentType = record.get("document_type");
          return documentType != null ? documentType.toString() : null;
        }
      }
    }
    return null;
  }

  /**
   * 更新文档中心数据中的ID
   */
  private static void updateDocumentCenterIds(List<DataSyncTableDefinition> definitions, Map<String, String> documentIdMapping,
    Map<String, String> libraryIdMapping) {


    for (DataSyncTableDefinition definition : definitions) {
      List<Map<String, Object>> dataRecords = definition.getDataRecords();
      if (CollectionUtils.isEmpty(dataRecords)) {
        continue;
      }
      for (Map<String, Object> record : dataRecords) {
        // 更新document_id
        Object documentId = record.get("document_id");
        if (documentId != null) {
          String newDocumentId = documentIdMapping.get(documentId.toString());
          if (newDocumentId != null) {
            record.put("document_id", newDocumentId);
          }
        }
        // 更新library_id
        Object libraryId = record.get("library_id");
        if (libraryId != null) {
          String newLibraryId = libraryIdMapping.get(libraryId.toString());
          if (newLibraryId != null) {
            record.put("library_id", newLibraryId);
          }
        }
        // 更新parent_id（如果是document_id）
        Object parentId = record.get("parent_id");
        if (parentId != null) {
          String newParentId = documentIdMapping.get(parentId.toString());
          if (newParentId != null) {
            record.put("parent_id", newParentId);
          }
        }
      }
    }
  }

  /**
   * 更新bt_document表中的dc_document_id
   */
  private static void updateBtDocumentDcDocumentIds(DataSyncParams params, Map<String, String> documentIdMapping) {
    if (MapUtils.isEmpty(documentIdMapping)) {
      return;
    }

    List<Map<String, Object>> btDocumentRecords = collectBtDocumentRecords(params, documentIdMapping);
    if (CollectionUtils.isEmpty(btDocumentRecords)) {
      return;
    }

    executeBatchUpdate(btDocumentRecords, documentIdMapping, params.getResetTenantId());
  }

  /**
   * 收集需要更新的bt_document记录
   */
  private static List<Map<String, Object>> collectBtDocumentRecords(DataSyncParams params, Map<String, String> documentIdMapping) {
    List<Map<String, Object>> btDocumentRecords = new ArrayList<>();

    for (DataSyncTableDefinition definition : params.getDefinitions()) {
      if (!"bt_document".equals(definition.getTableCode())) {
        continue;
      }

      List<Map<String, Object>> dataRecords = definition.getDataRecords();
      if (CollectionUtils.isEmpty(dataRecords)) {
        continue;
      }

      for (Map<String, Object> record : dataRecords) {
        Object dcDocumentId = record.get("dc_document_id");
        if (dcDocumentId != null && documentIdMapping.containsKey(dcDocumentId.toString())) {
          btDocumentRecords.add(record);
        }
      }
    }

    return btDocumentRecords;
  }

  /**
   * 批量执行UPDATE语句
   */
  private static void executeBatchUpdate(List<Map<String, Object>> btDocumentRecords, Map<String, String> documentIdMapping, Long tenantId) {
    int batchSize = 1000;
    for (List<Map<String, Object>> batch : Lists.partition(btDocumentRecords, batchSize)) {
      List<Object[]> updateArgs = buildUpdateArgs(batch, documentIdMapping, tenantId);
      if (CollectionUtils.isNotEmpty(updateArgs)) {
        String updateSql = "UPDATE bt_document SET dc_document_id = ? WHERE tenant_id = ? AND document_id = ?";
        jdbcTemplate.batchUpdate(updateSql, updateArgs);
      }
    }
  }

  /**
   * 构建更新参数
   */
  private static List<Object[]> buildUpdateArgs(List<Map<String, Object>> batch, Map<String, String> documentIdMapping, Long tenantId) {
    List<Object[]> updateArgs = new ArrayList<>();
    for (Map<String, Object> record : batch) {
      Object documentId = record.get("document_id");
      Object dcDocumentId = record.get("dc_document_id");
      if (documentId != null && dcDocumentId != null) {
        String newDcDocumentId = documentIdMapping.get(dcDocumentId.toString());
        if (newDcDocumentId != null) {
          updateArgs.add(new Object[] {newDcDocumentId, tenantId, documentId});
        }
      }
    }
    return updateArgs;
  }

  /**
   * 对每个表定义中的dataRecords按照id属性去重
   */
  private static void deduplicateDataRecords(List<DataSyncTableDefinition> documentCenterDefinitions) {
    if (CollectionUtils.isEmpty(documentCenterDefinitions)) {
      return;
    }

    for (DataSyncTableDefinition definition : documentCenterDefinitions) {
      List<Map<String, Object>> dataRecords = definition.getDataRecords();
      if (CollectionUtils.isEmpty(dataRecords)) {
        continue;
      }

      // 使用LinkedHashMap保持顺序，以id为key进行去重
      Map<String, Map<String, Object>> uniqueRecords = new LinkedHashMap<>();
      for (Map<String, Object> record : dataRecords) {
        Object id = record.get("id");
        if (id != null) {
          String idStr = id.toString();
          // 如果id已存在，保留第一个记录（或者可以根据需要选择保留最后一个）
          uniqueRecords.putIfAbsent(idStr, record);
        } else {
          // 如果id为null，直接添加到结果中
          uniqueRecords.put(null, record);
        }
      }

      // 更新dataRecords为去重后的结果
      definition.setDataRecords(new ArrayList<>(uniqueRecords.values()));
    }
  }

}
