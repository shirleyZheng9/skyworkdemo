package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * API 技能参数工具类
 *
 * @author bianjp
 * @since 2024-12-16
 */
public final class ApiParamUtil {
  private ApiParamUtil() {
  }

  /**
   * 构造请求参数结构
   *
   * @param service API 技能
   * @return 请求参数结构
   */
  @Nullable
  public static ParameterSpec buildRequestSpec(SkillServiceDTO service) {
    return buildRequestSpec(service.getPathJson(), service.getHeaderJson(), service.getQueryJson(), service.getBodyJson());
  }

  /**
   * 构造请求参数结构
   *
   * @param pathJson 请求路径参数结构 JSON 字符串
   * @param headerJson 请求头参数结构 JSON 字符串
   * @param queryJson URL 参数结构 JSON 字符串
   * @param bodyJson 请求体参数结构 JSON 字符串
   * @return 请求参数结构
   */
  @Nullable
  public static ParameterSpec buildRequestSpec(@Nullable String pathJson, @Nullable String headerJson, @Nullable String queryJson, @Nullable String bodyJson) {
    List<ParameterSpec> children = new ArrayList<>();
    parseRequestParam(headerJson, "header", "请求头", children);
    parseRequestParam(pathJson, "path", "请求路径参数", children);
    parseRequestParam(queryJson, "query", "URL 参数", children);
    parseRequestParam(bodyJson, "body", "请求头", children);
    return children.isEmpty() ? null : ParameterSpec.newRoot(children);
  }

  /**
   * 解析请求参数
   */
  private static void parseRequestParam(@Nullable String json, String name, String description, List<ParameterSpec> children) {
    if (StringUtils.isEmpty(json)) {
      return;
    }
    ParameterSpec root = JsonUtil.parseJsonRequired(json, ParameterSpec.class);
    root.setName(name);
    root.setDescription(description);
    children.add(root);
  }
}
