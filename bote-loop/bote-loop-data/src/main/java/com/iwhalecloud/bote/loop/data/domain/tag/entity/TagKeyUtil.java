package com.iwhalecloud.bote.loop.data.domain.tag.entity;

import com.iwhalecloud.bote.entity.loop.data.tag.TagKeyEntity;
import com.iwhalecloud.bote.loop.data.infra.repo.tag.convertor.TagConsts;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 标签键工具类
 * 迁移对应关系: Go语言TagKey的Validate和CalculateChangeLogs方法
 * - 功能: 提供标签键相关的工具方法
 * - 方法定义: 各种标签键工具方法
 * <p>
 * Java实现说明:
 * - 对应Go的TagKey结构体中的Validate和CalculateChangeLogs方法
 * - 使用Java静态方法实现工具功能
 * - 提供标签键验证和变更日志计算功能
 * <p>
 * 技术栈迁移:
 * - Go方法 -> Java静态方法
 * - Go结构体 -> Java对象
 * - Go错误处理 -> Java异常处理
 */
public final class TagKeyUtil {

  private TagKeyUtil() {
    // 工具类，禁止实例化
  }

  public static TagKeyEntity toPO(TagKey tagKey) {
    return TagKeyEntity.builder().build();
  }

  /**
   * 验证标签键
   * 迁移对应关系: Go语言TagKey.Validate
   * - 功能: 验证标签键
   * - 参数: tagKey - 标签键对象, spec - 标签规格
   * - 用途: 验证标签键的有效性
   */
  public static void validate(TagKey tagKey, TagSpec spec) {
    validateBasicFields(tagKey);
    validateContent(tagKey);
    validateNameAndDescription(tagKey);
    validateTagValues(tagKey.getTagValues(), spec.getMaxHeight(), spec.getMaxWidth());
  }

  private static void validateBasicFields(TagKey tagKey) {
    if (tagKey == null) {
      throw new BssException("tag key is null");
    }
    if (tagKey.getTagType() == TagType.UNDEFINED) {
      throw new BssException("tag type is undefined");
    }
    if (tagKey.getStatus() == TagStatus.UNDEFINED) {
      tagKey.setStatus(TagStatus.ACTIVE);
    }
    if (tagKey.getTagContentType() == TagContentType.UNDEFINED) {
      throw new BssException("tag content type is undefined");
    }
  }

  private static void validateNameAndDescription(TagKey tagKey) {
    if (tagKey.getTagKeyName().length() > 50) {
      throw new BssException("length of tag name is more than 50");
    }
    if (tagKey.getTagType() == TagType.TAG && !StringUtils.hasText(tagKey.getTagKeyName())) {
      throw new BssException("tag name is empty");
    }
    if (tagKey.getDescription() != null && tagKey.getDescription().length() > 200) {
      throw new BssException("length of tag description is more than 200");
    }
  }

  /**
   * 验证标签内容
   * 迁移对应关系: Go语言TagKey.validateContent
   * - 功能: 验证标签内容
   * - 参数: tagKey - 标签键对象
   * - 返回: 是否有效
   * - 用途: 验证标签内容类型和标签值的匹配性
   */
  private static boolean validateContent(TagKey tagKey) {
    if (tagKey == null) {
      return true;
    }
    switch (tagKey.getTagContentType()) {
      case CONTINUOUS_NUMBER:
      case FREE_TEXT:
        if (!CollectionUtils.isEmpty(tagKey.getTagValues())) {
          throw new BssException("number of tag value is more than 0, content type: " + tagKey.getTagContentType());
        }
        break;
      case BOOLEAN:
        if (CollectionUtils.isEmpty(tagKey.getTagValues()) || tagKey.getTagValues().size() != 2) {
          throw new BssException("number of tag values is illegal, length: " +
            (CollectionUtils.isEmpty(tagKey.getTagValues()) ? 0 : tagKey.getTagValues().size()));
        }
        break;
      default:
        throw new BssException("unsupported tag content type: " + tagKey.getTagContentType());
    }
    return true;
  }

  /**
   * 验证标签值
   * 迁移对应关系: Go语言validateTagValues
   * - 功能: 验证标签值
   * - 参数: tagValues - 标签值列表, maxHeight - 最大高度, maxWidth - 最大宽度
   * - 用途: 验证标签值的有效性和层级结构
   */
  private static void validateTagValues(List<TagValue> tagValues, int maxHeight, int maxWidth) {
    if (CollectionUtils.isEmpty(tagValues)) {
      return;
    }

    List<TagValue> now = new ArrayList<>();
    List<TagValue> fallbacks = new ArrayList<>();
    separateTagValues(tagValues, now, fallbacks);
    validateFallbacks(fallbacks);
    validateTagValueHierarchy(now, maxHeight, maxWidth);
  }

  private static void separateTagValues(List<TagValue> tagValues, List<TagValue> now, List<TagValue> fallbacks) {
    for (TagValue tagValue : tagValues) {
      if (!TagConsts.FALLBACK_TAG_VALUE_DEFAULT_NAME.equals(tagValue.getTagValueName()) && !tagValue.getIsSystem()) {
        now.add(tagValue);
      } else {
        fallbacks.add(tagValue);
      }
    }
  }

  private static void validateFallbacks(List<TagValue> fallbacks) {
    if (fallbacks.size() > 1) {
      throw new BssException("name " + TagConsts.FALLBACK_TAG_VALUE_DEFAULT_NAME + " is duplicate");
    }
  }

  private static void validateTagValueHierarchy(List<TagValue> now, int maxHeight, int maxWidth) {
    List<TagValue> next = new ArrayList<>();
    int height = 0;
    int width = 0;
    Set<String> nameSet = new HashSet<>();

    while (!now.isEmpty()) {
      height++;
      validateHeightLimit(height, maxHeight);
      width = Math.max(width, now.size());
      validateWidthLimit(width, maxWidth);

      validateCurrentLevelTagValues(now, nameSet, next);
      now = next;
      next = new ArrayList<>();
    }
  }

  private static void validateHeightLimit(int height, int maxHeight) {
    if (height > maxHeight) {
      throw new BssException("tag value height exceeds limit: " + maxHeight);
    }
  }

  private static void validateWidthLimit(int width, int maxWidth) {
    if (width > maxWidth) {
      throw new BssException("tag value width exceeds limit: " + maxWidth);
    }
  }

  private static void validateCurrentLevelTagValues(List<TagValue> now, Set<String> nameSet, List<TagValue> next) {
    for (TagValue value : now) {
      validateTagValueName(value);
      validateTagValueUniqueness(value, nameSet);
      nameSet.add(value.getTagValueName());
      addValidChildren(value, next);
    }
  }

  private static void validateTagValueName(TagValue value) {
    if (!StringUtils.hasText(value.getTagValueName())) {
      throw new BssException("there is empty tag value");
    }
  }

  private static void validateTagValueUniqueness(TagValue value, Set<String> nameSet) {
    String tagValueName = value.getTagValueName();
    if (nameSet.contains(tagValueName)) {
      throw new BssException("tag value is duplicated, tag value: " + tagValueName);
    }
  }

  private static void addValidChildren(TagValue value, List<TagValue> next) {
    for (TagValue child : value.getChildren()) {
      if (!TagConsts.FALLBACK_TAG_VALUE_DEFAULT_NAME.equals(child.getTagValueName()) && !child.getIsSystem()) {
        next.add(child);
      }
    }
  }

  /**
   * 计算变更日志
   * 迁移对应关系: Go语言TagKey.CalculateChangeLogs
   * - 功能: 计算变更日志
   * - 参数: tagKey - 当前标签键对象, preTagKey - 之前的标签键对象
   * - 返回: 变更日志列表
   * - 用途: 计算标签键的变更历史
   */
  public static List<ChangeLog> calculateChangeLogs(TagKey tagKey, TagKey preTagKey) {
    if (tagKey == null) {
      throw new BssException("tag key is null");
    }

    List<ChangeLog> result = new ArrayList<>();

    if (preTagKey == null) {
      return handleCreateTagKey(result);
    }

    processTagKeyChanges(tagKey, preTagKey, result);
    processTagValueChanges(tagKey, preTagKey, result);

    return result;
  }

  private static List<ChangeLog> handleCreateTagKey(List<ChangeLog> result) {
    result.add(ChangeLog.builder()
      .changeTarget(TagChangeTargetType.TAG)
      .operation(TagOperationType.CREATE)
      .build());
    return result;
  }

  private static void processTagKeyChanges(TagKey tagKey, TagKey preTagKey, List<ChangeLog> result) {
    checkTagTypeChange(tagKey, preTagKey, result);
    checkStatusChange(tagKey, preTagKey, result);
    checkNameChange(tagKey, preTagKey, result);
    checkDescriptionChange(tagKey, preTagKey, result);
    checkContentTypeChange(tagKey, preTagKey, result);
  }

  private static void checkTagTypeChange(TagKey tagKey, TagKey preTagKey, List<ChangeLog> result) {
    if (tagKey.getTagType() != preTagKey.getTagType()) {
      result.add(ChangeLog.builder()
        .changeTarget(TagChangeTargetType.TAG_TYPE)
        .operation(TagOperationType.UPDATE)
        .beforeValue(preTagKey.getTagType().getValue())
        .afterValue(tagKey.getTagType().getValue())
        .build());
    }
  }

  private static void checkStatusChange(TagKey tagKey, TagKey preTagKey, List<ChangeLog> result) {
    if (tagKey.getStatus() != preTagKey.getStatus()) {
      result.add(ChangeLog.builder()
        .changeTarget(TagChangeTargetType.TAG_STATUS)
        .operation(TagOperationType.UPDATE)
        .beforeValue(preTagKey.getStatus().getValue())
        .afterValue(tagKey.getStatus().getValue())
        .build());
    }
  }

  private static void checkNameChange(TagKey tagKey, TagKey preTagKey, List<ChangeLog> result) {
    if (!Objects.equals(tagKey.getTagKeyName(), preTagKey.getTagKeyName())) {
      result.add(ChangeLog.builder()
        .changeTarget(TagChangeTargetType.TAG_NAME)
        .operation(TagOperationType.UPDATE)
        .beforeValue(preTagKey.getTagKeyName())
        .afterValue(tagKey.getTagKeyName())
        .build());
    }
  }

  private static void checkDescriptionChange(TagKey tagKey, TagKey preTagKey, List<ChangeLog> result) {
    String currentDesc = tagKey.getDescription() != null ? tagKey.getDescription() : "";
    String preDesc = preTagKey.getDescription() != null ? preTagKey.getDescription() : "";
    if (!Objects.equals(currentDesc, preDesc)) {
      result.add(ChangeLog.builder()
        .changeTarget(TagChangeTargetType.TAG_DESCRIPTION)
        .operation(TagOperationType.UPDATE)
        .beforeValue(preDesc)
        .afterValue(currentDesc)
        .build());
    }
  }

  private static void checkContentTypeChange(TagKey tagKey, TagKey preTagKey, List<ChangeLog> result) {
    if (tagKey.getTagContentType() != preTagKey.getTagContentType()) {
      result.add(ChangeLog.builder()
        .changeTarget(TagChangeTargetType.TAG_CONTENT_TYPE)
        .operation(TagOperationType.UPDATE)
        .beforeValue(preTagKey.getTagContentType().getValue())
        .afterValue(tagKey.getTagContentType().getValue())
        .build());
    }
  }

  private static void processTagValueChanges(TagKey tagKey, TagKey preTagKey, List<ChangeLog> result) {
    Map<Long, TagValue> preExistedMap = splitTagValues(preTagKey);
    Map<Long, TagValue> nowExistedMap = splitTagValues(tagKey);
    List<TagValue> nowNewList = getNewTagValues(tagKey);

    processNewTagValues(nowNewList, result);
    processExistingTagValues(preExistedMap, nowExistedMap, result);
  }

  private static void processNewTagValues(List<TagValue> nowNewList, List<ChangeLog> result) {
    for (TagValue v : nowNewList) {
      result.add(ChangeLog.builder()
        .changeTarget(TagChangeTargetType.TAG_VALUE_NAME)
        .operation(TagOperationType.CREATE)
        .afterValue(v.getTagValueName())
        .targetValue(v.getTagValueName())
        .build());
    }
  }

  private static void processExistingTagValues(Map<Long, TagValue> preExistedMap, Map<Long, TagValue> nowExistedMap, List<ChangeLog> result) {
    for (Map.Entry<Long, TagValue> entry : preExistedMap.entrySet()) {
      Long key = entry.getKey();
      TagValue v1 = entry.getValue();
      TagValue v2 = nowExistedMap.get(key);

      if (v2 == null) {
        addDeleteChangeLog(v1, result);
      } else {
        checkTagValueChanges(v1, v2, result);
      }
    }
  }

  private static void addDeleteChangeLog(TagValue v1, List<ChangeLog> result) {
    result.add(ChangeLog.builder()
      .changeTarget(TagChangeTargetType.TAG_VALUE_NAME)
      .operation(TagOperationType.DELETE)
      .beforeValue(v1.getTagValueName())
      .targetValue(v1.getTagValueName())
      .build());
  }

  private static void checkTagValueChanges(TagValue v1, TagValue v2, List<ChangeLog> result) {
    if (!Objects.equals(v1.getTagValueName(), v2.getTagValueName())) {
      result.add(ChangeLog.builder()
        .changeTarget(TagChangeTargetType.TAG_VALUE_NAME)
        .operation(TagOperationType.UPDATE)
        .beforeValue(v1.getTagValueName())
        .afterValue(v2.getTagValueName())
        .targetValue(v2.getTagValueName())
        .build());
    } else if (v1.getStatus() != v2.getStatus()) {
      result.add(ChangeLog.builder()
        .changeTarget(TagChangeTargetType.TAG_VALUE_STATUS)
        .operation(TagOperationType.UPDATE)
        .beforeValue(v1.getStatus().getValue())
        .afterValue(v2.getStatus().getValue())
        .targetValue(v2.getTagValueName())
        .build());
    }
  }

  /**
   * 分割标签值
   * 迁移对应关系: Go语言TagKey.SplitTagValues
   * - 功能: 分割标签值
   * - 参数: tagKey - 标签键对象
   * - 返回: 已存在的标签值映射
   * - 用途: 获取已经入库的TagValue
   */
  private static Map<Long, TagValue> splitTagValues(TagKey tagKey) {
    if (tagKey == null || CollectionUtils.isEmpty(tagKey.getTagValues())) {
      return new HashMap<>();
    }

    Map<Long, TagValue> existedMap = new HashMap<>();
    List<TagValue> current = new ArrayList<>(tagKey.getTagValues());

    while (!current.isEmpty()) {
      List<TagValue> next = new ArrayList<>();
      for (TagValue v : current) {
        if (v.getTagValueId() != null && v.getTagValueId() != 0) {
          existedMap.put(v.getTagValueId(), v);
        }
        next.addAll(v.getChildren());
      }
      current = next;
    }

    return existedMap;
  }

  /**
   * 获取新标签值
   * 迁移对应关系: Go语言TagKey.SplitTagValues
   * - 功能: 获取新标签值
   * - 参数: tagKey - 标签键对象
   * - 返回: 新标签值列表
   * - 用途: 获取新的TagValue
   */
  private static List<TagValue> getNewTagValues(TagKey tagKey) {
    if (tagKey == null || CollectionUtils.isEmpty(tagKey.getTagValues())) {
      return new ArrayList<>();
    }

    List<TagValue> newValues = new ArrayList<>();
    List<TagValue> current = new ArrayList<>(tagKey.getTagValues());

    while (!current.isEmpty()) {
      List<TagValue> next = new ArrayList<>();
      for (TagValue v : current) {
        if (v.getTagValueId() == null || v.getTagValueId() == 0) {
          newValues.add(v);
        }
        next.addAll(v.getChildren());
      }
      current = next;
    }

    return newValues;
  }
}
