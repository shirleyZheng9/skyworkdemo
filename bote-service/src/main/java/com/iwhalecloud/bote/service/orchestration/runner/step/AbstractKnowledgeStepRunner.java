package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.util.SceneParamUtil;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeExtItem;
import com.iwhalecloud.bote.dto.knowledge.ResourceExtItem;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * 知识库步骤执行器抽象类
 *
 * @author bianjp
 * @since 2025-04-03
 */
public abstract class AbstractKnowledgeStepRunner<T extends AbstractStep> extends AbstractLlmStepRunner<T> {

  /**
   * 从 knowledgeExt 中解析知识库 ID 列表(可能是引用表达式，也可能是 KnowledgeExtItem 列表的JSON字符串)
   */
  protected final List<Long> resolveKnowledgeIdsFormExt(String expr) {
    if (!expr.startsWith("$")) {
      List<KnowledgeExtItem> knowledgeExtItems = JsonUtil.parseJson(expr, new TypeReference<>() {
      });
      return ListUtils.emptyIfNull(knowledgeExtItems).stream()
        .map(KnowledgeExtItem::getKnowledgeId)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    }
    // 解析表达式
    Object value = SceneParamUtil.getParamValue(expr);
    if (ObjectUtils.isEmpty(value)) {
      return Collections.emptyList();
    }
    return Arrays.stream(value.toString().split(",")).map(String::trim).map(Long::parseLong).collect(Collectors.toList());
  }

  /**
   * 从 resourceExt 中解析资源文档列表(可能是引用表达式，也可能是 ResourceExtItem 列表的JSON字符串)
   */
  protected final List<ResourceExtItem> resolveResourceIdsFormExt(String expr) {
    if (!expr.startsWith("$")) {
      List<ResourceExtItem> resourceExtItems = JsonUtil.parseJson(expr, new TypeReference<>() {
      });
      if (CollectionUtils.isNotEmpty(resourceExtItems)) {
        resourceExtItems.forEach(resourceExtItem -> resourceExtItem.setResourceType(KnowledgeConsts.KNOW_BASE_RESOURCE));
      }
      else {
        return Collections.emptyList();
      }
      return resourceExtItems;
    }
    // 解析表达式
    Object value = SceneParamUtil.getParamValue(expr);
    if (ObjectUtils.isEmpty(value)) {
      return Collections.emptyList();
    }
    return JsonUtil.convert(value, new TypeReference<>() {
    });
  }

  protected final List<Long> resolveKnowledgeIds(String expr) {
    Assert.hasText(expr, "知识库 ID 不能为空");
    // 常量值
    if (!expr.startsWith("$")) {
      return resolveIdsByLiteral(expr);
    }
    // 解析表达式
    Object value = SceneParamUtil.getParamValue(expr);
    Assert.isTrue(ObjectUtils.isNotEmpty(value), () -> "知识库 ID 不能为空: " + expr);
    return resolveIds(value);
  }

  protected final List<Long> resolveDocumentIds(String expr) {
    if (StringUtils.isEmpty(expr)) {
      return Collections.emptyList();
    }
    // 常量值
    if (!expr.startsWith("$")) {
      return resolveIdsByLiteral(expr);
    }
    // 解析表达式
    Object value = SceneParamUtil.getParamValue(expr);
    if (ObjectUtils.isEmpty(value)) {
      return Collections.emptyList();
    }
    return resolveIds(value);
  }

  private List<Long> resolveIds(Object value) {
    // 字符串，复用常量值的解析逻辑
    if (value instanceof String) {
      return resolveIdsByLiteral((String) value);
    }
    // 整型，单个 ID
    else if (value instanceof Number) {
      return Collections.singletonList(((Number) value).longValue());
    }
    // 列表
    else if (value instanceof Collection) {
      Set<Long> ids = new LinkedHashSet<>();
      for (Object item : (Collection<?>) value) {
        if (ObjectUtils.isEmpty(item)) {
          continue;
        }
        if (item instanceof String) {
          Assert.isTrue(StringUtils.isNumeric((String) item), () -> "非法的 ID 赋值: item=" + item);
          ids.add(Long.parseLong((String) item));
        }
        else if (item instanceof Number) {
          ids.add(((Number) item).longValue());
        }
        else {
          throw new IllegalArgumentException(String.format("非法的 ID 赋值: type=%s, item=%s", item.getClass().getCanonicalName(), item));
        }
      }
      return new ArrayList<>(ids);
    }
    throw new BssException(String.format("非法的 ID 赋值: type=%s, value=%s", value.getClass().getCanonicalName(), value));
  }

  /**
   * 解析 ID 列表常量值，字符串必须是单个 ID 或逗号分隔的多个 ID
   */
  private static List<Long> resolveIdsByLiteral(String expr) {
    if (!expr.contains(",")) {
      Assert.isTrue(StringUtils.isNumeric(expr), () -> "ID 不合法: " + expr);
      return Collections.singletonList(Long.parseLong(expr));
    }
    String[] pieces = expr.split("\\s*,\\s*");
    return Arrays.stream(pieces).map(piece -> {
      Assert.isTrue(StringUtils.isNumeric(piece), () -> "ID 不合法: " + expr);
      return Long.parseLong(piece);
    }).distinct().collect(Collectors.toList());
  }
}
