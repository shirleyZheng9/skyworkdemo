package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.WebPageFetchUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.LinkExtractorPluginParams;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 链接提取插件
 * 用于从文本内容中提取HTTP/HTTPS链接
 *
 * @author zhang.jun
 * @since 2025-08-20
 */
@Component
public class LinkExtractorPlugin extends AbstractPlugin<LinkExtractorPluginParams> {



  public LinkExtractorPlugin() {
    super(LinkExtractorPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_LINK_EXTRACTOR;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("text", "待处理的文本内容", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newProperty("urls", "提取到的链接列表", AttrDataType.ARRAY)));
  }

  @Override
  public void validateParams(LinkExtractorPluginParams params) {
    Assert.notNull(params.getText(), "文本内容不能为空");
  }

  @Override
  public Object doRun(LinkExtractorPluginParams pluginParams) {
    Map<String, Object> result = new HashMap<>();
    List<String> urls = WebPageFetchUtil.extractUrls(pluginParams.getText());
    result.put("urls", urls);
    return result;
  }


}
