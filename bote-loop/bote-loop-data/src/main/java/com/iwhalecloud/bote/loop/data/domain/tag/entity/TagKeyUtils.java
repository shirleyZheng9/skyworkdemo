package com.iwhalecloud.bote.loop.data.domain.tag.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 标签键工具类
 * 迁移对应关系: Go语言entity.TagKey相关方法
 * - 功能: 提供标签键相关的工具方法
 * - 方法定义: 各种标签键操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的entity.TagKey结构体方法
 * - 使用Java静态方法提供工具功能
 * - 包含标签键操作相关方法
 * <p>
 * 技术栈迁移:
 * - Go方法 -> Java静态方法
 * - Go切片操作 -> Java列表操作
 * - Go map操作 -> Java Map操作
 */
public final class TagKeyUtils {

  private TagKeyUtils() {
    // 工具类，禁止实例化
  }

  /**
   * 设置版本号
   * 迁移对应关系: Go语言entity.TagKey.SetVersionNum
   * - 功能: 为标签键及其子标签值设置版本号
   * - 参数: versionNum - 版本号
   * - 用途: 版本管理
   */
  public static void setVersionNum(TagKey tagKey, Integer versionNum) {
    if (tagKey == null) {
      return;
    }
    tagKey.setVersionNum(versionNum);
    setVersionNumForTagValues(tagKey.getTagValues(), versionNum);
  }

  /**
   * 递归设置标签值版本号
   * 迁移对应关系: Go语言entity.TagKey.SetVersionNum中的递归逻辑
   * - 功能: 递归设置标签值版本号
   * - 参数: tagValues - 标签值列表, versionNum - 版本号
   * - 用途: 版本管理
   */
  private static void setVersionNumForTagValues(List<TagValue> tagValues, Integer versionNum) {
    if (tagValues == null || tagValues.isEmpty()) {
      return;
    }
    for (TagValue tagValue : tagValues) {
      if (tagValue != null) {
        tagValue.setVersionNum(versionNum);
        setVersionNumForTagValues(tagValue.getChildren(), versionNum);
      }
    }
  }

  /**
   * 设置空间ID
   * 迁移对应关系: Go语言entity.TagKey.SetSpaceID
   * - 功能: 为标签键及其子标签值设置空间ID
   * - 参数: spaceId - 空间ID
   * - 用途: 多租户隔离
   */
  public static void setSpaceId(TagKey tagKey, Long spaceId) {
    if (tagKey == null) {
      return;
    }
    tagKey.setSpaceId(spaceId);
    setSpaceIdForTagValues(tagKey.getTagValues(), spaceId);
  }

  /**
   * 递归设置标签值空间ID
   * 迁移对应关系: Go语言entity.TagKey.SetSpaceID中的递归逻辑
   * - 功能: 递归设置标签值空间ID
   * - 参数: tagValues - 标签值列表, spaceId - 空间ID
   * - 用途: 多租户隔离
   */
  private static void setSpaceIdForTagValues(List<TagValue> tagValues, Long spaceId) {
    if (tagValues == null || tagValues.isEmpty()) {
      return;
    }
    for (TagValue tagValue : tagValues) {
      if (tagValue != null) {
        tagValue.setSpaceId(spaceId);
        setSpaceIdForTagValues(tagValue.getChildren(), spaceId);
      }
    }
  }

  /**
   * 设置应用ID
   * 迁移对应关系: Go语言entity.TagKey.SetAppID
   * - 功能: 为标签键及其子标签值设置应用ID
   * - 参数: appId - 应用ID
   * - 用途: 多应用隔离
   */
  public static void setAppId(TagKey tagKey, Integer appId) {
    if (tagKey == null) {
      return;
    }
    tagKey.setAppId(appId);
    setAppIdForTagValues(tagKey.getTagValues(), appId);
  }

  /**
   * 递归设置标签值应用ID
   * 迁移对应关系: Go语言entity.TagKey.SetAppID中的递归逻辑
   * - 功能: 递归设置标签值应用ID
   * - 参数: tagValues - 标签值列表, appId - 应用ID
   * - 用途: 多应用隔离
   */
  private static void setAppIdForTagValues(List<TagValue> tagValues, Integer appId) {
    if (tagValues == null || tagValues.isEmpty()) {
      return;
    }
    for (TagValue tagValue : tagValues) {
      if (tagValue != null) {
        tagValue.setAppId(appId);
        setAppIdForTagValues(tagValue.getChildren(), appId);
      }
    }
  }

  /**
   * 分离标签值
   * 迁移对应关系: Go语言entity.TagKey.SplitTagValues
   * - 功能: 获取已经入库的TagValue和新TagValue
   * - 参数: tagKey - 标签键
   * - 返回: 已存在标签值映射和新标签值列表
   * - 用途: 区分新增和更新
   */
  public static TagValueSplitResult splitTagValues(TagKey tagKey) {
    if (tagKey == null || tagKey.getTagValues() == null) {
      return new TagValueSplitResult(new HashMap<>(), new ArrayList<>());
    }

    Map<Long, TagValue> existedMap = new HashMap<>();
    List<TagValue> newValues = new ArrayList<>();

    splitTagValuesRecursive(tagKey.getTagValues(), existedMap, newValues);

    return new TagValueSplitResult(existedMap, newValues);
  }

  /**
   * 递归分离标签值
   * 迁移对应关系: Go语言entity.TagKey.SplitTagValues中的递归逻辑
   * - 功能: 递归分离标签值
   * - 参数: tagValues - 标签值列表, existedMap - 已存在映射, newValues - 新值列表
   * - 用途: 区分新增和更新
   */
  private static void splitTagValuesRecursive(List<TagValue> tagValues,
                                              Map<Long, TagValue> existedMap,
                                              List<TagValue> newValues) {
    if (tagValues == null || tagValues.isEmpty()) {
      return;
    }

    for (TagValue tagValue : tagValues) {
      if (tagValue != null) {
        if (tagValue.getTagValueId() != null && tagValue.getTagValueId() > 0) {
          existedMap.put(tagValue.getTagValueId(), tagValue);
        }
        else {
          newValues.add(tagValue);
        }
        splitTagValuesRecursive(tagValue.getChildren(), existedMap, newValues);
      }
    }
  }

  /**
   * 标签值分离结果
   * 迁移对应关系: Go语言entity.TagKey.SplitTagValues的返回值
   * - 功能: 存储标签值分离结果
   * - 字段定义: 已存在映射和新值列表
   */
  public static class TagValueSplitResult {
    private final Map<Long, TagValue> existedMap;
    private final List<TagValue> newValues;

    public TagValueSplitResult(Map<Long, TagValue> existedMap, List<TagValue> newValues) {
      this.existedMap = existedMap;
      this.newValues = newValues;
    }

    public Map<Long, TagValue> getExistedMap() {
      return existedMap;
    }

    public List<TagValue> getNewValues() {
      return newValues;
    }
  }
}
