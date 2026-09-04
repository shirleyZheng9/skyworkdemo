package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.lark.LarkResultDTO;
import com.iwhalecloud.bote.dto.plugin.params.LarkAddFieldPluginParams;
import com.iwhalecloud.bote.dto.plugin.params.LarkAddFieldPluginParams.LarkFieldDescriptionDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 飞书多维表格数据表添加字段插件
 *
 * @author qian.sisheng
 * @since 2025-08-23
 */
@Component
public class LarkAddFieldPlugin extends AbstractLarkPlugin<LarkAddFieldPluginParams> {
  /** 创建多维表格字段 url */
  private static final String LARK_API_CREATE_BITABLE = "https://open.feishu.cn/open-apis/bitable/v1/apps/:appToken/tables/:tableId/fields";

  public LarkAddFieldPlugin() {
    super(LarkAddFieldPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_LARK_ADD_FIELDS;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("appId", "应用ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appSecret", "应用密钥", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("url", "多维表格链接地址", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("tableId", "表格ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appToken", "多维表格唯一标识", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("fieldName", "字段名称", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("type", "字段类型", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("uiType", "字段在界面上的展示类型", AttrDataType.STRING));
    children.add(ParameterSpec.newObject("description", "字段描述", Arrays.asList(
      ParameterSpec.newProperty("text", "字段描述内容", AttrDataType.STRING),
      ParameterSpec.newProperty("disableSync", "字段描述是否同步到其他设备", AttrDataType.BOOLEAN))));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("fieldId", "多维表格字段 ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("fieldName", "字段名称", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("type", "字段类型", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("uiType", "字段在界面上的展示类型", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("isPrimary", "是否是索引列", AttrDataType.BOOLEAN));
    children.add(ParameterSpec.newProperty("isHidden", "是否是隐藏字段", AttrDataType.BOOLEAN));
    children.add(ParameterSpec.newObject("description", "字段描述", Arrays.asList(ParameterSpec.newProperty("text", "字段描述内容", AttrDataType.STRING),
      ParameterSpec.newProperty("disableSync", "字段描述是否同步到其他设备", AttrDataType.BOOLEAN))));
    ParameterSpec field = ParameterSpec.newObject("field", "字段信息", children);
    return ParameterSpec.newRoot(
      Arrays.asList(ParameterSpec.newProperty("code", "编码", AttrDataType.STRING), ParameterSpec.newProperty("msg", "错误信息", AttrDataType.STRING),
          ParameterSpec.newObject("data", "返回对象", Arrays.asList(field, ParameterSpec.newProperty("authUrl", "授权链接", AttrDataType.STRING)))));
  }

  @Override
  public void validateParams(LarkAddFieldPluginParams params) {
    Assert.hasText(params.getAppId(), "appID不能为空");
    Assert.hasText(params.getAppSecret(), "appSecret不能为空");
    Assert.hasText(params.getFieldName(), "fieldName不能为空");
    Assert.notNull(params.getType(), "type不能为空");

  }

  @Override
  public Object doRun(LarkAddFieldPluginParams pluginParams) {
    return executeLarkApiCall(headers -> {
      Map<String, Object> requestParams = new HashMap<>();
      requestParams.put("field_name", pluginParams.getFieldName());
      requestParams.put("type", pluginParams.getType());
      if (StringUtils.isNotEmpty(pluginParams.getUiType())) {
        requestParams.put("ui_type", pluginParams.getUiType());
      }
      if (pluginParams.getDescription() != null) {
        requestParams.put("description", pluginParams.getDescription());
      }
      String url = LARK_API_CREATE_BITABLE.replace(":appToken", getAppToken(pluginParams)).replace(":tableId", getTableId(pluginParams));
      return HttpUtil.post(url, requestParams, new ParameterizedTypeReference<Map<String, Object>>() {
      }, headers);
    }, pluginParams, new TypeReference<LarkResultDTO<LarkAddFieldResultDTO>>() {
    });
  }

  @Setter
  @Getter
  @ToString
  private static final class LarkAddFieldResultDTO {
    /** 字段 */
    private LarkAddFieldInfoDTO field;
  }

  @Setter
  @Getter
  @ToString
  private static final class LarkAddFieldInfoDTO {
    /** 字段名称 */
    private String fieldName;
    /** 字段类型 */
    private Integer type;
    /** 字段ID */
    private String fieldId;
    /** 是否主键 */
    private Boolean isPrimary;
    /** 字段在界面上的展示类型 */
    private String uiType;
    /** 字段描述 */
    private LarkFieldDescriptionDTO description;
    /** 是否隐藏字段 */
    private Boolean isHidden;
  }
}
