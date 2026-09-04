package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.WebPageFetchUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.WebPageFetchPluginParams;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网页文本内容提取插件
 *
 * @author zhangJun
 * @since 2025-07-18
 */
@Component
public class WebPageFetchPlugin extends AbstractPlugin<WebPageFetchPluginParams> {
  public WebPageFetchPlugin() {
    super(WebPageFetchPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_WEB_PAGE_FETCH;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("webPageUrl", "网址", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newProperty("content", "网站的内容", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(WebPageFetchPluginParams params) {
    Assert.notNull(params.getWebPageUrl(), "网址不能为空");
  }

  @Override
  public Object doRun(WebPageFetchPluginParams pluginParams) {
    Map<String, Object> params = new HashMap<>();
    params.put("content", WebPageFetchUtil.fetchContent(pluginParams.getWebPageUrl()));
    return params;
  }
}
