package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 记忆内容工具类
 *
 * <p>用于处理页面、页面函数消息的记忆内容</p>
 *
 * @author bianjp
 * @since 2025-04-10
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class MemoryContentUtil {
  private static final Logger logger = LoggerFactory.getLogger(MemoryContentUtil.class);

  private MemoryContentUtil() {
  }

  /**
   * 记忆内容转为字符串形式，用于存储到数据库
   *
   * @param memoryContent 记忆内容
   * @return 记忆内容字符串
   */
  @Nullable
  public static String toString(@Nullable Object memoryContent) {
    if (memoryContent == null) {
      return null;
    }
    if (memoryContent instanceof Map || memoryContent instanceof Collection) {
      return JsonUtil.toJsonString(memoryContent);
    }
    return memoryContent.toString();
  }

  /**
   * 解析记忆内容，用于做记忆内容的追加
   */
  @Nullable
  public static Object parseMemoryContent(@Nullable String memoryContent) {
    if (StringUtils.isEmpty(memoryContent)) {
      return null;
    }
    if (memoryContent.startsWith("{") || memoryContent.startsWith("[")) {
      Object result = JsonUtil.parseJson(memoryContent, Object.class);
      if (result != null) {
        return result;
      }
    }
    return memoryContent;
  }

  /**
   * 追加记忆内容
   *
   * @param oldContent 旧记忆内容
   * @param content 新记忆内容
   * @return 合并后的记忆内容
   */
  @SuppressWarnings("unchecked")
  public static Object appendMemoryContent(@Nullable Object oldContent, Object content) {
    // 旧记忆内容为空，直接返回新内容
    if (ObjectUtils.isEmpty(oldContent)) {
      return content;
    }
    // 新记忆内容为空，直接返回旧内容
    if (ObjectUtils.isEmpty(content)) {
      return oldContent;
    }

    // 字符串做连接
    if (oldContent instanceof String && content instanceof String) {
      return oldContent + (String) content;
    }
    // Map 做合并
    if (oldContent instanceof Map && content instanceof Map) {
      Map<String, Object> result = new LinkedHashMap<>();
      result.putAll((Map<String, Object>) oldContent);
      result.putAll((Map<String, Object>) content);
      return result;
    }
    // List 做合并
    if (oldContent instanceof List && content instanceof List) {
      return ListUtils.union((List<Object>) oldContent, (List<Object>) content);
    }

    // 类型不兼容时返回新内容
    logger.warn("Unable to merge memory content: oldType={}, newType={}, oldContent={}, newContent={}",
      oldContent.getClass().getCanonicalName(), content.getClass().getCanonicalName(), oldContent, content);
    return content;
  }
}
