package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.lark.LarkResultDTO;
import com.iwhalecloud.bote.dto.plugin.params.LarkCreateTablePluginParams;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 飞书创建多维表格数据表插件
 *
 * @author qian.sisheng
 * @since 2025-08-27
 */
@Component
public class LarkCreateTablePlugin extends AbstractLarkPlugin<LarkCreateTablePluginParams> {
  /** 创建多维表格数据表 url */
  public static final String CREATE_TABLE_URL = "https://open.feishu.cn/open-apis/bitable/v1/apps/:appToken/tables";

  public LarkCreateTablePlugin() {
    super(LarkCreateTablePluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_LARK_CREATE_TABLE;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("appId", "应用ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appSecret", "应用密钥", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("url", "多维表格链接地址", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appToken", "多维表格唯一标识", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("name", "数据表名称", AttrDataType.STRING));
    children.add(ParameterSpec.newList("fields", "数据表的初始字段列表", ParameterSpec.newObject("field", "数据表的初始字段",
      Arrays.asList(ParameterSpec.newProperty("fieldName", "字段名称", AttrDataType.STRING),
        ParameterSpec.newProperty("type", "字段类型", AttrDataType.INTEGER)))));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("code", "编码", AttrDataType.STRING),
      ParameterSpec.newProperty("msg", "错误信息", AttrDataType.STRING), ParameterSpec.newObject("data", "返回对象",
        Arrays.asList(ParameterSpec.newProperty("authUrl", "授权链接", AttrDataType.STRING),
          ParameterSpec.newProperty("tableId", "多维表格数据表的 ID", AttrDataType.STRING),
          ParameterSpec.newProperty("defaultViewId", "默认表格视图的 ID", AttrDataType.STRING),
          ParameterSpec.newProperty("fieldIdList", "数据表初始字段的 ID 列表", AttrDataType.ARRAY)))));
  }

  @Override
  public void validateParams(LarkCreateTablePluginParams params) {
    Assert.hasText(params.getAppId(), "appID不能为空");
    Assert.hasText(params.getAppSecret(), "appSecret不能为空");
    Assert.hasText(params.getName(), "name不能为空");
    Assert.notEmpty(params.getFields(), "fields不能为空");
  }

  @Override
  public Object doRun(LarkCreateTablePluginParams pluginParams) {
    return executeLarkApiCall(headers -> {
      String url = CREATE_TABLE_URL.replace(":appToken", getAppToken(pluginParams));
      Map<String, Object> requestParams = new HashMap<>();
      Map<String, Object> table = new HashMap<>();
      table.put("name", pluginParams.getName());
      table.put("fields", snakeCaseMapper.convertValue(pluginParams.getFields(), new TypeReference<List<Map<String, Object>>>() {
      }));
      requestParams.put("table", table);
      return HttpUtil.post(url, requestParams, new ParameterizedTypeReference<Map<String, Object>>() {
      }, headers);
    }, pluginParams, new TypeReference<LarkResultDTO<CreateTableResultDTO>>() {
    });
  }

  @Getter
  @Setter
  @ToString
  public static final class CreateTableResultDTO {
    /** 多维表格数据表的 ID */
    private String tableId;
    /** 多维表格数据表的默认视图 ID */
    private String defaultViewId;
    /** 数据表初始字段的 ID 列表 */
    private List<String> fieldIdList;
  }
}
