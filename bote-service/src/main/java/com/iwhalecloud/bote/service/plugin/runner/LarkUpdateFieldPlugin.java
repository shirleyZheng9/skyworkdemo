package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.lark.LarkResultDTO;
import com.iwhalecloud.bote.dto.plugin.params.LarkAddFieldPluginParams.LarkFieldDescriptionDTO;
import com.iwhalecloud.bote.dto.plugin.params.LarkUpdateFieldPluginParams;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
 * 飞书修改字段插件
 *
 * @author qian.sisheng
 * @since 2025-08-26
 */
@Component
public class LarkUpdateFieldPlugin extends AbstractLarkPlugin<LarkUpdateFieldPluginParams> {

  private final Logger logger = LoggerFactory.getLogger(LarkUpdateFieldPlugin.class);
  /** 飞书修改字段 url */
  private static final String UPDATE_FIELD_URL = "https://open.feishu.cn/open-apis/bitable/v1/apps/:appToken/tables/:tableId/fields/:fieldId";

  public LarkUpdateFieldPlugin() {
    super(LarkUpdateFieldPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_LARK_UPDATE_FIELD;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("appId", "飞书应用ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appSecret", "飞书应用密钥", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appToken", "飞书应用Token", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("fieldId", "字段ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("tableId", "数据表ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("url", "表格地址", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("fieldName", "多维表格字段名称", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("type", "字段类型", AttrDataType.STRING));
    children.add(ParameterSpec.newObject("description", "字段描述", Arrays.asList(
      ParameterSpec.newProperty("text", "字段描述内容", AttrDataType.STRING),
      ParameterSpec.newProperty("disableSync", "字段描述是否同步到其他设备", AttrDataType.BOOLEAN)
    )));
    children.add(ParameterSpec.newProperty("uiType", "字段在界面上的展示类型", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("fieldName", "多维表格字段名称", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("type", "字段类型", AttrDataType.STRING));
    children.add(ParameterSpec.newObject("description", "字段描述",
      Arrays.asList(ParameterSpec.newProperty("text", "字段描述内容", AttrDataType.STRING),
        ParameterSpec.newProperty("disableSync", "字段描述是否同步到其他设备", AttrDataType.BOOLEAN))));
    children.add(ParameterSpec.newProperty("isPrimary", "是否是索引列", AttrDataType.BOOLEAN));
    ParameterSpec data = ParameterSpec.newObject("data", "返回对象",
      Collections.singletonList(ParameterSpec.newObject("field", "字段信息", children)));
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("code", "编码", AttrDataType.STRING),
      ParameterSpec.newProperty("msg", "错误信息", AttrDataType.STRING), data));
  }

  @Override
  public void validateParams(LarkUpdateFieldPluginParams params) {
    Assert.hasText(params.getAppId(), "appID不能为空");
    Assert.hasText(params.getAppSecret(), "appSecret不能为空");
    Assert.hasText(params.getFieldId(), "fieldId不能为空");
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public Object doRun(LarkUpdateFieldPluginParams pluginParams) {
    return executeLarkApiCall(headers -> {
      String url = UPDATE_FIELD_URL.replace(":appToken", getAppToken(pluginParams)).replace(":tableId", getTableId(pluginParams))
        .replace(":fieldId", pluginParams.getFieldId());
      Map<String, Object> requestParams = snakeCaseMapper.convertValue(pluginParams, new TypeReference<Map<String, Object>>() {
      });
      logger.info("Request update lark field start: url={}, request={}", url, requestParams);
      ResponseEntity<Map<String, Object>> response = HttpUtil.getRestTemplate()
        .exchange(url, HttpMethod.PUT, new HttpEntity<>(requestParams, headers), new ParameterizedTypeReference<Map<String, Object>>() {
        });
      logger.info("Request update lark field end: response={}", response.getBody());
      return response.getBody();
    }, pluginParams, new TypeReference<LarkResultDTO<UpdateFieldResultDTO>>() {
    });
  }

  /**
   * 飞书修改字段结果
   */
  @Getter
  @Setter
  @ToString
  private static final class UpdateFieldResultDTO {
    /** 字段信息 */
    private FieldDTO field;
  }

  /**
   * 字段信息
   */
  @Getter
  @Setter
  @ToString
  private static final class FieldDTO {
    /** 字段名称 */
    private String fieldName;
    /** 字段类型 */
    private Integer type;
    /** 字段描述 */
    private LarkFieldDescriptionDTO description;
    /** 是否是索引列 */
    private Boolean isPrimary;
    /** 字段在界面上的展示类型 */
    private String uiType;
    /** 字段ID */
    private String fieldId;
  }
}
