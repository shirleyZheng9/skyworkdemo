package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.lark.LarkResultDTO;
import com.iwhalecloud.bote.dto.plugin.params.LarkAddRecordsPluginParams;
import com.iwhalecloud.bote.dto.plugin.params.LarkAddRecordsPluginParams.RecordDTO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
 * 飞书多维表格批量添加记录插件
 *
 * @author qian.sisheng
 * @since 2025-08-25
 */
@Component
public class LarkAddRecordsPlugin extends AbstractLarkPlugin<LarkAddRecordsPluginParams> {
  /** 添加记录 url */
  private static final String ADD_RECORDS_URL = "https://open.feishu.cn/open-apis/bitable/v1/apps/:appToken/tables/:tableId/records/batch_create";

  public LarkAddRecordsPlugin() {
    super(LarkAddRecordsPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_LARK_ADD_RECORDS;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("appId", "应用ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appSecret", "应用密钥", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("url", "多维表格链接地址", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("tableId", "表格ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appToken", "多维表格唯一标识", AttrDataType.STRING));
    children.add(ParameterSpec.newList("records", "记录列表",
      ParameterSpec.newObject("record", "记录", Collections.singletonList(ParameterSpec.newProperty("fields", "字段", AttrDataType.STRING)))));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    ParameterSpec records = ParameterSpec.newList("records", "记录列表", ParameterSpec.newObject("record", "记录",
      Arrays.asList(ParameterSpec.newProperty("fields", "字段", AttrDataType.STRING),
        ParameterSpec.newProperty("recordId", "记录ID", AttrDataType.STRING))));
    return ParameterSpec.newRoot(
      Arrays.asList(ParameterSpec.newProperty("code", "编码", AttrDataType.STRING), ParameterSpec.newProperty("msg", "错误信息", AttrDataType.STRING),
        ParameterSpec.newObject("data", "返回对象", Arrays.asList(records, ParameterSpec.newProperty("authUrl", "授权链接", AttrDataType.STRING)))));
  }

  @Override
  public void validateParams(LarkAddRecordsPluginParams params) {
    Assert.hasText(params.getAppId(), "appID不能为空");
    Assert.hasText(params.getAppSecret(), "appSecret不能为空");
    Assert.notEmpty(params.getRecords(), "records不能为空");
  }

  @Override
  public Object doRun(LarkAddRecordsPluginParams pluginParams) {
    return executeLarkApiCall(headers -> {
      String url = ADD_RECORDS_URL.replace(":appToken", getAppToken(pluginParams)).replace(":tableId", getTableId(pluginParams));
      List<RecordDTO> records = pluginParams.getRecords();
      for (RecordDTO record : records) {
        record.setFields(JsonUtil.parseJson((String) record.getFields(), new TypeReference<Map<String, Object>>() {
        }));
      }
      Map<String, Object> request = new HashMap<>();
      request.put("records", records);
      return HttpUtil.post(url, request, new ParameterizedTypeReference<Map<String, Object>>() {
      }, headers);
    }, pluginParams, new TypeReference<LarkResultDTO<AddRecordResultDTO>>() {
    });
  }

  @Getter
  @Setter
  @ToString
  public static final class AddRecordResultDTO {
    /** 记录字段 */
    private List<AddRecordDTO> records;
  }

  @Getter
  @Setter
  @ToString
  public static final class AddRecordDTO {
    /** 记录字段 */
    private Object fields;
    /** 记录 ID */
    private String recordId;

    /**
     * 设置字段，转换成JSON字符串，方便处理
     */
    public void setFields(Object fields) {
      this.fields = JsonUtil.toJsonString(fields);
    }
  }
}
