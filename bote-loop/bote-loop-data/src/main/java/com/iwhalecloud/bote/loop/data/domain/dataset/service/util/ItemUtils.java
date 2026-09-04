package com.iwhalecloud.bote.loop.data.domain.dataset.service.util;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ContentType;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldData;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Item;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemData;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemDataProperties;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemErrorDetail;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemErrorGroup;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemErrorType;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.DatasetWithSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.IndexedItem;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 项目工具类
 * 迁移对应关系: Go语言item_utils.go
 * - 功能: 提供项目相关的工具方法
 * - 方法定义: 各种项目处理方法
 * <p>
 * Java实现说明:
 * - 对应Go的item_utils.go文件
 * - 使用Java静态方法实现工具功能
 * - 提供项目数据处理和验证功能
 * <p>
 * 技术栈迁移:
 * - Go方法 -> Java静态方法
 * - Go切片 -> Java List
 * - Go映射 -> Java Map
 * - Go错误处理 -> Java异常处理
 */
public final class ItemUtils {

  private ItemUtils() {
    // 工具类，禁止实例化
  }

  private static final String DATASET_ITEM_DATA_KEY = "dataset:%d:item:%d:vn:%d";

  public static boolean checkEmpty(DatasetWithSchema ds, Item... items) {
    return ds == null || items == null || items.length == 0;
  }

  /**
   * 清理输入项目
   * 迁移对应关系: Go语言SanitizeInputItem
   * - 功能: 根据模式修剪、填补项目数据内容
   * - 参数: ds - 数据集和模式, items - 项目列表
   * - 用途: 处理用户传入的数据
   */
  public static void sanitizeInputItem(DatasetWithSchema ds, Item... items) {
    if (checkEmpty(ds, items)) {
      return;
    }

    boolean repeatedData = isRepeatedDataEnabled(ds);
    Map<String, FieldSchema> fields = buildFieldMap(ds);
    Map<String, String> nameToKey = buildNameToKeyMap(ds);

    for (Item item : items) {
      if (repeatedData) {
        sanitizeRepeatedDataItem(item, fields, nameToKey);
      } else {
        sanitizeNormalDataItem(item, fields, nameToKey);
      }
    }
  }

  /**
   * 检查是否启用重复数据
   */
  private static boolean isRepeatedDataEnabled(DatasetWithSchema ds) {
    Boolean repeatedDataValue = ds.getDataset().getFeatures() != null
        ? ds.getDataset().getFeatures().getRepeatedData()
        : null;
    return repeatedDataValue != null && repeatedDataValue;
  }

  /**
   * 构建字段映射
   */
  private static Map<String, FieldSchema> buildFieldMap(DatasetWithSchema ds) {
    return ds.getSchema().getAvailableFields().stream()
      .collect(Collectors.toMap(FieldSchema::getKey, f -> f));
  }

  /**
   * 构建名称到键的映射
   */
  private static Map<String, String> buildNameToKeyMap(DatasetWithSchema ds) {
    return ds.getSchema().getAvailableFields().stream()
      .collect(Collectors.toMap(FieldSchema::getName, FieldSchema::getKey));
  }

  /**
   * 清理重复数据项
   */
  private static void sanitizeRepeatedDataItem(Item item, Map<String, FieldSchema> fields, Map<String, String> nameToKey) {
    item.setData(null);
    List<ItemData> repeatedDataList = item.getRepeatedData();
    if (!CollectionUtils.isEmpty(repeatedDataList)) {
      List<ItemData> keepList = new ArrayList<>();
      for (ItemData data : repeatedDataList) {
        List<FieldData> pruned = sanitizeItemData(data.getData(), fields, nameToKey);
        if (!pruned.isEmpty()) {
          data.setData(pruned);
          keepList.add(data);
        }
      }
      item.setRepeatedData(keepList);
    }
  }

  /**
   * 清理普通数据项
   */
  private static void sanitizeNormalDataItem(Item item, Map<String, FieldSchema> fields, Map<String, String> nameToKey) {
    item.setRepeatedData(null);
    item.setData(sanitizeItemData(item.getData(), fields, nameToKey));
  }

  /**
   * 清理项目数据
   * 迁移对应关系: Go语言sanitizeItemData
   * - 功能: 清理项目数据
   * - 参数: data - 字段数据列表, fields - 字段映射, nameToKey - 名称到键的映射
   * - 返回: 清理后的字段数据列表
   * - 用途: 数据清理
   */
  private static List<FieldData> sanitizeItemData(List<FieldData> data, Map<String, FieldSchema> fields, Map<String, String> nameToKey) {
    if (CollectionUtils.isEmpty(data)) {
      return new ArrayList<>();
    }

    List<FieldData> keepList = new ArrayList<>();
    for (FieldData fd : data) {
      String key = fd.getKey();
      if (!StringUtils.hasText(key)) {
        key = nameToKey.get(fd.getName());
        fd.setKey(key);
      }
      FieldSchema schema = fields.get(key);
      if (schema == null) {
        continue;
      }

      fd.setContentType(schema.getContentType());
      sanitizeFieldData(fd, 1);
      if (fd.getDataBytes() == 0) {
        continue;
      }
      castFieldData(schema, fd);

      keepList.add(fd);
    }
    return keepList;
  }

  /**
   * 清理字段数据
   * 迁移对应关系: Go语言sanitizeFieldData
   * - 功能: 清理字段数据
   * - 参数: fd - 字段数据, walkLevel - 遍历级别
   * - 用途: 字段数据清理
   */
  private static void sanitizeFieldData(FieldData fd, int walkLevel) {
    if (fd == null) {
      return;
    }

    switch (fd.getContentType()) {
      case TEXT:
        fd.setParts(null);
        if (StringUtils.hasText(fd.getContent())) {
          fd.setContent(fd.getContent().trim());
        }
        break;
      case IMAGE:
      case AUDIO:
      case VIDEO:
        fd.setContent("");
        fd.setParts(null);
        break;
      case MULTIPART:
        fd.setContent("");
        fd.setAttachments(null);
        if (walkLevel == 0) {
          fd.setParts(null);
        }
        if (!CollectionUtils.isEmpty(fd.getParts())) {
          for (FieldData part : fd.getParts()) {
            sanitizeFieldData(part, walkLevel - 1);
          }
          List<FieldData> validParts = fd.getParts().stream()
            .filter(part -> part.getDataBytes() > 0)
            .collect(Collectors.toList());
          fd.setParts(validParts);
        }
        break;
      default:
        fd.setContent("");
        fd.setParts(null);
        fd.setAttachments(null);
        break;
    }
  }

  /**
   * 转换字段数据
   * 迁移对应关系: Go语言castFieldData
   * - 功能: 将字段数据转换为符合模式的类型
   * - 参数: s - 字段模式, d - 字段数据
   * - 用途: 数据类型转换
   */
  private static void castFieldData(FieldSchema s, FieldData d) {
    if (s.getContentType() != ContentType.TEXT || s.getTextSchema() == null ||
      s.getTextSchema().getSchema() == null || !StringUtils.hasText(d.getContent())) {
      return;
    }

    // 根据文本模式进行类型转换
    // 其他类型转换逻辑
    if ("boolean".equals(s.getTextSchema().getSingleType())) {
      d.setContent(d.getContent().toLowerCase());
    }
  }

  /**
   * 验证项目
   * 迁移对应关系: Go语言ValidateItems
   * - 功能: 校验项目是否符合模式等约束
   * - 参数: ds - 数据集和模式, items - 项目列表
   * - 返回: 有效项目和错误组
   * - 用途: 项目验证
   */
  public static ValidationResult validateItems(DatasetWithSchema ds, List<Item> items) {
    if (CollectionUtils.isEmpty(items)) {
      return new ValidationResult(new ArrayList<>(), new ArrayList<>());
    }

    List<IndexedItem> indexedItems = new ArrayList<>();
    for (int i = 0; i < items.size(); i++) {
      indexedItems.add(IndexedItem.builder()
        .index(i)
        .item(items.get(i))
        .build());
    }

    return validateIndexedItems(ds, indexedItems);
  }

  /**
   * 验证索引项目
   * 迁移对应关系: Go语言ValidateIndexedItems
   * - 功能: 校验索引项目是否符合模式等约束
   * - 参数: ds - 数据集和模式, items - 索引项目列表
   * - 返回: 有效项目和错误组
   * - 用途: 索引项目验证
   */
  public static ValidationResult validateIndexedItems(DatasetWithSchema ds, List<IndexedItem> items) {
    if (CollectionUtils.isEmpty(items)) {
      return new ValidationResult(new ArrayList<>(), new ArrayList<>());
    }

    Map<String, FieldSchema> schemaByKey = ds.getSchema().getAvailableFields().stream()
      .collect(Collectors.toMap(FieldSchema::getKey, f -> f));
    long maxItemSize = ds.getDataset().getSpec().getMaxItemSize();
    Map<ItemErrorType, ItemErrorGroup> errMap = new HashMap<>();

    if (maxItemSize == 0) {
      maxItemSize = Long.MAX_VALUE;
    }

    List<IndexedItem> validItems = new ArrayList<>();
    for (IndexedItem item : items) {
      ItemDataProperties props = item.getItem().getOrBuildProperties();
      if (props.getBytes() > maxItemSize) {
        addErrItem(errMap, item.getIndex(), ItemErrorType.EXCEED_MAX_ITEM_SIZE,
          String.format("size of item %d exceeds max %d", props.getBytes(), maxItemSize));
        continue;
      }

      boolean hasInvalidData = false;
      for (List<FieldData> data : item.getItem().getAllData()) {
        Map<String, FieldData> dm = data.stream()
          .collect(Collectors.toMap(FieldData::getKey, fd -> fd));
        for (Map.Entry<String, FieldSchema> entry : schemaByKey.entrySet()) {
          String key = entry.getKey();
          FieldSchema schema = entry.getValue();
          FieldData field = dm.get(key);
          if (field == null) {
            continue;
          }
          try {
            schema.validateData(field);
          }
          catch (Exception e) {
            hasInvalidData = true;
            addErrItem(errMap, item.getIndex(), ItemErrorType.MISMATCH_SCHEMA,
              String.format("field_name=%s, msg=%s", schema.getName(), e.getMessage()));
          }
        }
      }
      if (!hasInvalidData) {
        validItems.add(item);
      }
    }

    return new ValidationResult(validItems, new ArrayList<>(errMap.values()));
  }

  /**
   * 验证单个项目
   * 迁移对应关系: Go语言ValidateItem
   * - 功能: 校验单个项目是否符合模式等约束
   * - 参数: ds - 数据集和模式, item - 项目
   * - 返回: 无异常表示验证通过
   * - 用途: 单个项目验证
   */
  public static void validateItem(DatasetWithSchema ds, Item item) {
    ValidationResult result = validateItems(ds, List.of(item));
    if (!result.bad().isEmpty()) {
      ItemErrorGroup b = result.bad().getFirst();
      String msg = String.format("reason=%s", b.getType());
      if (!CollectionUtils.isEmpty(b.getDetails())) {
        msg = String.format("reason=%s, message=%s", b.getType(), b.getDetails().getFirst().getMessage());
      }
      throw new BssException("invalid item, " + msg);
    }
  }

  /**
   * 清理输出项目
   * 迁移对应关系: Go语言SanitizeOutputItem
   * - 功能: 根据模式修剪项目内容
   * - 参数: schema - 数据集模式, items - 项目列表
   * - 用途: 处理传给用户的数据
   */
  public static void sanitizeOutputItem(DatasetSchema schema, List<Item> items) {
    if (CollectionUtils.isEmpty(items)) {
      return;
    }

    SchemaInfo schemaInfo = buildSchemaInfo(schema);
    for (Item item : items) {
      sanitizeItemData(item, schemaInfo);
      sanitizeItemRepeatedData(item, schemaInfo);
      fillBackFieldInfo(item, schemaInfo);
    }
  }

  private static SchemaInfo buildSchemaInfo(DatasetSchema schema) {
    List<FieldSchema> fields = schema.getAvailableFields();
    List<String> keys = fields.stream()
      .map(FieldSchema::getKey)
      .collect(Collectors.toList());
    Map<String, FieldSchema> key2Schema = fields.stream()
      .collect(Collectors.toMap(FieldSchema::getKey, f -> f));
    Set<String> keySet = new HashSet<>(keys);

    return new SchemaInfo(keys, key2Schema, keySet);
  }

  private static void sanitizeItemData(Item item, SchemaInfo schemaInfo) {
    if (!CollectionUtils.isEmpty(item.getData())) {
      List<FieldData> filteredData = item.getData().stream()
        .filter(fd -> schemaInfo.keySet().contains(fd.getKey()))
        .collect(Collectors.toList());
      item.setData(filteredData);
    }
  }

  private static void sanitizeItemRepeatedData(Item item, SchemaInfo schemaInfo) {
    if (!CollectionUtils.isEmpty(item.getRepeatedData())) {
      for (ItemData data : item.getRepeatedData()) {
        List<FieldData> filteredData = data.getData().stream()
          .filter(fd -> schemaInfo.keySet().contains(fd.getKey()))
          .collect(Collectors.toList());
        data.setData(filteredData);
      }
    }
  }

  private static void fillBackFieldInfo(Item item, SchemaInfo schemaInfo) {
    for (List<FieldData> data : item.getAllData()) {
      for (FieldData field : data) {
        fillBackSingleField(field, schemaInfo);
      }
    }
  }

  private static void fillBackSingleField(FieldData field, SchemaInfo schemaInfo) {
    FieldSchema fieldSchema = schemaInfo.key2Schema().get(field.getKey());
    if (fieldSchema != null) {
      field.setName(fieldSchema.getName());
      fillBackContentType(field, fieldSchema);
      fillBackFormat(field, fieldSchema);
    }
  }

  private static void fillBackContentType(FieldData field, FieldSchema fieldSchema) {
    if (!StringUtils.hasText(field.getContentType().getValue())) {
      field.setContentType(fieldSchema.getContentType());
    }
  }

  private static void fillBackFormat(FieldData field, FieldSchema fieldSchema) {
    if (!StringUtils.hasText(field.getFormat().getValue())) {
      field.setFormat(fieldSchema.getDefaultFormat());
    }
  }


  /**
   * 格式化数据集项目数据键
   * 迁移对应关系: Go语言FormatDatasetItemDataKey
   * - 功能: 格式化数据集项目数据键
   * - 参数: datasetId - 数据集ID, itemId - 项目ID, vn - 版本号
   * - 返回: 格式化的键
   * - 用途: 生成存储键
   */
  public static String formatDatasetItemDataKey(Long datasetId, Long itemId, Long vn) {
    return String.format(DATASET_ITEM_DATA_KEY, datasetId, itemId, vn);
  }

  /**
   * 添加错误项目
   * 迁移对应关系: Go语言addErrItem内部函数
   * - 功能: 添加错误项目
   * - 参数: errMap - 错误映射, index - 索引, errType - 错误类型, message - 消息
   * - 用途: 错误收集
   */
  private static void addErrItem(Map<ItemErrorType, ItemErrorGroup> errMap, int index, ItemErrorType errType, String message) {
    ItemErrorGroup pre = errMap.get(errType);
    if (pre == null) {
      pre = ItemErrorGroup.builder()
        .type(errType)
        .details(new ArrayList<>())
        .build();
      errMap.put(errType, pre);
    }
    int errorCount = pre.getErrorCount() == null ? 0 : pre.getErrorCount();
    pre.setErrorCount(errorCount + 1);
    pre.getDetails().add(ItemErrorDetail.builder()
      .message(message)
      .index(index)
      .build());
  }

  /**
   * 验证结果
   */
  public record ValidationResult(List<IndexedItem> good, List<ItemErrorGroup> bad) {
  }

  private record SchemaInfo(List<String> keys, Map<String, FieldSchema> key2Schema, Set<String> keySet) {
  }
}
