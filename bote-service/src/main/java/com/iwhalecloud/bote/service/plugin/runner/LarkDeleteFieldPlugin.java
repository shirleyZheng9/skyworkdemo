package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.lark.LarkResultDTO;
import com.iwhalecloud.bote.dto.plugin.params.LarkDeleteFieldPluginParams;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 飞书多维表格删除表格字段插件
 *
 * @author qian.sisheng
 * @since 2025-08-26
 */
@Component
public class LarkDeleteFieldPlugin extends AbstractLarkPlugin<LarkDeleteFieldPluginParams> {

  private final Logger logger = LoggerFactory.getLogger(LarkDeleteFieldPlugin.class);
  /** 删除字段 url */
  private static final String DELETE_FIELD_URL = "https://open.feishu.cn/open-apis/bitable/v1/apps/:appToken/tables/:tableId/fields/:fieldId";

  public LarkDeleteFieldPlugin() {
    super(LarkDeleteFieldPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_LARK_DELETE_FIELD;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("appId", "应用ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appSecret", "应用密钥", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("url", "多维表格链接地址", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("tableId", "表格ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appToken", "多维表格唯一标识", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("fieldId", "字段ID", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("code", "编码", AttrDataType.STRING),
      ParameterSpec.newProperty("msg", "错误信息", AttrDataType.STRING), ParameterSpec.newObject("data", "返回对象", Arrays.asList(
          ParameterSpec.newProperty("fieldId", "字段 ID", AttrDataType.STRING),
            ParameterSpec.newProperty("deleted", "是否成功删除", AttrDataType.BOOLEAN),
        ParameterSpec.newProperty("authUrl", "授权链接", AttrDataType.STRING)))));
  }

  @Override
  public void validateParams(LarkDeleteFieldPluginParams params) {
    Assert.hasText(params.getAppId(), "appID不能为空");
    Assert.hasText(params.getAppSecret(), "appSecret不能为空");
    Assert.hasText(params.getFieldId(), "fieldId不能为空");
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public Object doRun(LarkDeleteFieldPluginParams pluginParams) {
    return executeLarkApiCall(headers -> {
      String url = DELETE_FIELD_URL.replace(":appToken", getAppToken(pluginParams)).replace(":tableId", getTableId(pluginParams))
        .replace(":fieldId", pluginParams.getFieldId());
      HttpEntity<?> requestEntity = new HttpEntity<>(null, headers);
      logger.info("Request lark delete table field start, url={}", url);
      ResponseEntity<Map<String, Object>> response = HttpUtil.getRestTemplate()
        .exchange(url, HttpMethod.DELETE, requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
        });
      logger.info("Request lark delete table field end, response={}", response.getBody());
      return response.getBody();
    }, pluginParams, new TypeReference<LarkResultDTO<DeleteFieldResultDTO>>() {
    });
  }

  /**
   * 删除字段结果
   */
  @Getter
  @Setter
  @ToString
  public static final class DeleteFieldResultDTO {
    /** 被删除的字段的 ID */
    private String fieldId;
    /** 字段是否被删除 */
    private String deleted;
  }
}
