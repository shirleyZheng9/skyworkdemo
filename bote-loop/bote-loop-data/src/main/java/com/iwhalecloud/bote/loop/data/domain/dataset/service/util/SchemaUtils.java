package com.iwhalecloud.bote.loop.data.domain.dataset.service.util;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Dataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldStatus;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 模式工具类
 * 迁移对应关系: Go语言schema_utils.go
 * - 功能: 提供模式相关的工具方法
 * - 方法定义: 各种模式处理方法
 * <p>
 * Java实现说明:
 * - 对应Go的schema_utils.go文件
 * - 使用Java静态方法实现工具功能
 * - 提供模式验证、合并和生成功能
 * <p>
 * 技术栈迁移:
 * - Go方法 -> Java静态方法
 * - Go切片 -> Java List
 * - Go映射 -> Java Map
 * - Go错误处理 -> Java异常处理
 */
public final class SchemaUtils {

  private SchemaUtils() {
    // 工具类，禁止实例化
  }

  private static final Pattern IDENTITY_REGEXP = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{0,63}$");

  /**
   * 为字段生成key
   * 迁移对应关系: Go语言genFieldKeys
   * - 功能: 为字段生成唯一的key，确保key不冲突
   * - 参数: fields - 字段列表
   * - 用途: 字段key生成和冲突避免
   */
  public static void genFieldKeys(List<FieldSchema> fields) {
    if (CollectionUtils.isEmpty(fields)) {
      return;
    }

    Set<String> seen = new HashSet<>();
    // 添加已存在的key
    fields.stream()
      .map(FieldSchema::getKey)
      .filter(StringUtils::hasText)
      .forEach(seen::add);
    seen.add("key"); // 保留字段

    for (FieldSchema field : fields) {
      if (StringUtils.hasText(field.getKey())) {
        continue;
      }

      String prefix = "key";
      if (StringUtils.hasText(field.getName()) && IDENTITY_REGEXP.matcher(field.getName()).matches()) {
        prefix = field.getName();
      }
      field.setKey(generateUniqueKey(prefix, seen));
    }
  }

  /**
   * 生成唯一的key
   * 迁移对应关系: Go语言genKey内部函数
   * - 功能: 生成不冲突的key
   * - 参数: prefix - 前缀, seen - 已存在的key集合
   * - 返回: 唯一的key
   * - 用途: key冲突避免
   */
  private static String generateUniqueKey(String prefix, Set<String> seen) {
    String key = prefix;
    int suffix = 1;
    while (seen.contains(key)) {
      key = prefix + "_" + suffix;
      suffix++;
    }
    seen.add(key);
    return key;
  }

  /**
   * 验证模式
   * 迁移对应关系: Go语言validateSchema
   * - 功能: 验证数据集模式的有效性
   * - 参数: dataset - 数据集, fields - 字段列表
   * - 返回: 无异常表示验证通过
   * - 用途: 模式有效性验证
   */
  public static void validateSchema(Dataset dataset, List<FieldSchema> fields) {
    validateDatasetNotNull(dataset);
    List<FieldSchema> availFields = getAvailableFields(fields);
    validateAvailableFields(availFields);
    validateFieldBasicInfo(fields);
    validateFieldKeyFormat(fields);
    validateFieldUniqueness(availFields, fields);
    validateFieldCountLimit(dataset, availFields);
    validateMultiModalSupport(dataset, availFields);
  }

  private static void validateDatasetNotNull(Dataset dataset) {
    if (dataset == null) {
      throw new BssException("dataset is null");
    }
    if (dataset.getSpec() == null) {
      throw new BssException("dataset spec is null, dataset_id=" + dataset.getId());
    }
    if (dataset.getFeatures() == null) {
      throw new BssException("dataset features is null, dataset_id=" + dataset.getId());
    }
  }

  private static List<FieldSchema> getAvailableFields(List<FieldSchema> fields) {
    return fields.stream()
      .filter(FieldSchema::isAvailable)
      .toList();
  }

  private static void validateAvailableFields(List<FieldSchema> availFields) {
    if (availFields.isEmpty()) {
      throw new BssException("no available fields");
    }
  }

  private static void validateFieldBasicInfo(List<FieldSchema> fields) {
    List<FieldSchema> emptyFields = fields.stream()
      .filter(f -> !StringUtils.hasText(f.getName()) || !StringUtils.hasText(f.getKey()))
      .toList();
    if (!emptyFields.isEmpty()) {
      throw new BssException("field name or key is empty");
    }
  }

  private static void validateFieldKeyFormat(List<FieldSchema> fields) {
    List<FieldSchema> badKeyFields = fields.stream()
      .filter(f -> !IDENTITY_REGEXP.matcher(f.getKey()).matches())
      .toList();
    if (!badKeyFields.isEmpty()) {
      throw new BssException("field key is not valid: " +
        badKeyFields.stream().map(FieldSchema::getKey).toList());
    }
  }

  private static void validateFieldUniqueness(List<FieldSchema> availFields, List<FieldSchema> fields) {
    validateFieldNameUniqueness(availFields);
    validateFieldKeyUniqueness(fields);
  }

  private static void validateFieldNameUniqueness(List<FieldSchema> availFields) {
    List<String> names = availFields.stream()
      .map(FieldSchema::getName)
      .collect(Collectors.toList());
    List<String> dupNames = findDuplicates(names);
    if (!dupNames.isEmpty()) {
      throw new BssException("field name duplicated: " + dupNames);
    }
  }

  private static void validateFieldKeyUniqueness(List<FieldSchema> fields) {
    List<String> keys = fields.stream()
      .map(FieldSchema::getKey)
      .collect(Collectors.toList());
    List<String> dupKeys = findDuplicates(keys);
    if (!dupKeys.isEmpty()) {
      throw new BssException("field key duplicated: " + dupKeys);
    }
  }

  private static void validateFieldCountLimit(Dataset dataset, List<FieldSchema> availFields) {
    if (dataset.getSpec().getMaxFieldCount() > 0 && availFields.size() > dataset.getSpec().getMaxFieldCount()) {
      throw new BssException("field_count " + availFields.size() + " exceed column_limit " + dataset.getSpec().getMaxFieldCount());
    }
  }

  private static void validateMultiModalSupport(Dataset dataset, List<FieldSchema> availFields) {
    if (dataset.getFeatures().getMultiModal() != null && !dataset.getFeatures().getMultiModal()) {
      List<FieldSchema> multiModalFields = availFields.stream()
        .filter(f -> f.getContentType().isMultiModal())
        .toList();
      if (!multiModalFields.isEmpty()) {
        List<String> fieldNames = multiModalFields.stream()
          .map(FieldSchema::getName)
          .toList();
        throw new BssException("multi_modal is not enabled, fields=" + fieldNames);
      }
    }
  }

  /**
   * 检查模式兼容性
   * 迁移对应关系: Go语言schemaCompatible
   * - 功能: 检查两个模式是否兼容
   * - 参数: preFields - 前一版本字段, curFields - 当前版本字段
   * - 返回: true表示兼容
   * - 用途: 模式兼容性检查
   */
  public static boolean schemaCompatible(List<FieldSchema> preFields, List<FieldSchema> curFields) {
    if (CollectionUtils.isEmpty(preFields) || CollectionUtils.isEmpty(curFields)) {
      return true;
    }

    List<FieldSchema> availableCurFields = curFields.stream()
      .filter(FieldSchema::isAvailable)
      .toList();

    Map<String, FieldSchema> preMap = preFields.stream()
      .collect(Collectors.toMap(FieldSchema::getKey, f -> f));

    for (FieldSchema cur : availableCurFields) {
      FieldSchema pre = preMap.get(cur.getKey());
      if (pre != null && !pre.isCompatibleWith(cur)) {
        return false;
      }
    }

    return true;
  }

  /**
   * 合并模式
   * 迁移对应关系: Go语言mergeSchema
   * - 功能: 合并上一版本和当前版本的字段
   * - 参数: dataset - 数据集, preSchema - 前一版本模式, fields - 当前版本字段
   * - 返回: 合并后的字段列表
   * - 用途: 模式版本合并
   */
  public static List<FieldSchema> mergeSchema(Dataset dataset, DatasetSchema preSchema, List<FieldSchema> fields) {
    if (CollectionUtils.isEmpty(fields)) {
      return new ArrayList<>();
    }

    List<FieldSchema> availableFields = fields.stream()
      .filter(FieldSchema::isAvailable)
      .toList();

    Map<String, FieldSchema> preMap = new HashMap<>();
    if (preSchema != null && !CollectionUtils.isEmpty(preSchema.getFields())) {
      preMap = preSchema.getFields().stream()
        .collect(Collectors.toMap(FieldSchema::getKey, f -> f));
    }

    List<FieldSchema> result = new ArrayList<>();

    // 添加当前版本的字段
    for (FieldSchema cur : availableFields) {
      cur.setStatus(FieldStatus.AVAILABLE);
      preMap.remove(cur.getKey());
      result.add(cur);
    }

    // 添加前一版本中已删除的字段
    for (FieldSchema pre : preMap.values()) {
      FieldSchema field = new FieldSchema();
      copyFieldSchema(pre, field);
      field.setStatus(FieldStatus.DELETED);
      result.add(field);
    }

    genFieldKeys(result);
    validateSchema(dataset, result);
    return result;
  }

  /**
   * 为数据集创建新模式
   * 迁移对应关系: Go语言newSchemaOfDataset
   * - 功能: 为数据集创建新的模式对象
   * - 参数: dataset - 数据集, fields - 字段列表
   * - 返回: 新的模式对象
   * - 用途: 模式对象创建
   */
  public static DatasetSchema newSchemaOfDataset(Dataset dataset, List<FieldSchema> fields) {
    if (dataset == null) {
      return null;
    }

    boolean immutable = false;
    if (dataset.getFeatures() != null) {
      immutable = !dataset.getFeatures().getEditSchema();
    }

    DatasetSchema schema = new DatasetSchema();
    schema.setId(dataset.getSchemaId());
    schema.setAppId(dataset.getAppId());
    schema.setSpaceId(dataset.getSpaceId());
    schema.setDatasetId(dataset.getId());
    schema.setFields(fields);
    schema.setImmutable(immutable);
    schema.setCreatedBy(dataset.getCreatedBy());
    schema.setUpdatedBy(dataset.getCreatedBy());
    return schema;
  }

  /**
   * 查找重复元素
   * 迁移对应关系: Go语言gslice.Dup
   * - 功能: 查找列表中的重复元素
   * - 参数: list - 列表
   * - 返回: 重复元素列表
   * - 用途: 重复元素检查
   */
  private static List<String> findDuplicates(List<String> list) {
    Map<String, Long> counts = list.stream()
      .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

    return counts.entrySet().stream()
      .filter(entry -> entry.getValue() > 1)
      .map(Map.Entry::getKey)
      .collect(Collectors.toList());
  }

  /**
   * 复制字段模式
   * 迁移对应关系: Go语言copier.Copy
   * - 功能: 复制字段模式对象
   * - 参数: source - 源对象, target - 目标对象
   * - 用途: 对象复制
   */
  private static void copyFieldSchema(FieldSchema source, FieldSchema target) {
    if (source == null || target == null) {
      return;
    }
    BeanUtils.copyProperties(source, target);
  }
}
