package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.lark.LarkResultDTO;
import com.iwhalecloud.bote.dto.plugin.params.LarkDeleteTablePluginParams;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 飞书删除数据表插件
 *
 * @author qian.sisheng
 * @since 2025-08-25
 */
@Component
public class LarkDeleteTablePlugin extends AbstractLarkPlugin<LarkDeleteTablePluginParams> {

  private final Logger logger = LoggerFactory.getLogger(LarkDeleteTablePlugin.class);
  /** 飞书删除数据表 url */
  private static final String DELETE_TABLE_API = "https://open.feishu.cn/open-apis/bitable/v1/apps/:appToken/tables/:tableId";

  public LarkDeleteTablePlugin() {
    super(LarkDeleteTablePluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_LARK_DELETE_TABLE;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("appId", "应用ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appSecret", "应用密钥", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("url", "多维表格链接地址", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("tableId", "表格ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appToken", "多维表格唯一标识", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("code", "编码", AttrDataType.STRING),
      ParameterSpec.newProperty("msg", "错误信息", AttrDataType.STRING),
      ParameterSpec.newObject("data", "返回对象", Collections.singletonList(ParameterSpec.newProperty("authUrl", "授权链接", AttrDataType.STRING)))));
  }

  @Override
  public void validateParams(LarkDeleteTablePluginParams params) {
    Assert.hasText(params.getAppId(), "appID不能为空");
    Assert.hasText(params.getAppSecret(), "appSecret不能为空");
  }

  @Override
  public Object doRun(LarkDeleteTablePluginParams pluginParams) {
    return executeLarkApiCall(headers -> {
      String url = DELETE_TABLE_API.replace(":appToken", getAppToken(pluginParams)).replace(":tableId", getTableId(pluginParams));
      logger.info("Request delete lark table start: url={}", url);
      HttpEntity<?> requestEntity = new HttpEntity<>(null, headers);
      ResponseEntity<Map<String, Object>> response = HttpUtil.getRestTemplate()
        .exchange(url, HttpMethod.DELETE, requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
        });
      logger.info("Request delete lark table end: response={}", response);
      return response.getBody();
    }, pluginParams, new TypeReference<LarkResultDTO<Void>>() {
    });
  }
}
