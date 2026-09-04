package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.lark.LarkResultDTO;
import com.iwhalecloud.bote.dto.plugin.params.LarkDeleteRecordsPluginParams;
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
 * 飞书批量删除表格数据插件
 *
 * @author qian.sisheng
 * @since 2025-08-26
 */

@Component
public class LarkDeleteRecordsPlugin extends AbstractLarkPlugin<LarkDeleteRecordsPluginParams> {
  /** 批量删除记录 url */
  private static final String BATCH_DELETE_RECORDS_API = "https://open.feishu.cn/open-apis/bitable/v1/apps/:appToken/tables/:tableId/records/batch_delete";

  public LarkDeleteRecordsPlugin() {
    super(LarkDeleteRecordsPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_LARK_DELETE_RECORDS;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("appId", "应用ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appSecret", "应用密钥", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("url", "多维表格链接地址", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("tableId", "表格ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appToken", "多维表格唯一标识", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("records", "删除的多条记录 ID 列表", AttrDataType.ARRAY));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("code", "编码", AttrDataType.STRING),
      ParameterSpec.newProperty("msg", "错误信息", AttrDataType.STRING), ParameterSpec.newObject("data", "返回对象", Arrays.asList(
        ParameterSpec.newList("records", "记录列表", ParameterSpec.newObject("record", "记录",
          Arrays.asList(ParameterSpec.newProperty("recordId", "记录的 ID", AttrDataType.STRING),
            ParameterSpec.newProperty("deleted", "是否成功删除", AttrDataType.BOOLEAN)))),
        ParameterSpec.newProperty("authUrl", "授权链接", AttrDataType.STRING)))));
  }

  @Override
  public void validateParams(LarkDeleteRecordsPluginParams params) {
    Assert.hasText(params.getAppId(), "appID不能为空");
    Assert.hasText(params.getAppSecret(), "appSecret不能为空");
    Assert.notEmpty(params.getRecords(), "records不能为空");
  }

  @Override
  public Object doRun(LarkDeleteRecordsPluginParams pluginParams) {
    return executeLarkApiCall(headers -> {
      String url = BATCH_DELETE_RECORDS_API.replace(":appToken", getAppToken(pluginParams)).replace(":tableId", getTableId(pluginParams));
      Map<String, Object> requestParams = new HashMap<>();
      requestParams.put("records", pluginParams.getRecords());
      return HttpUtil.post(url, requestParams, new ParameterizedTypeReference<Map<String, Object>>() {
      }, headers);
    }, pluginParams, new TypeReference<LarkResultDTO<DeleteRecordResultDTO>>() {
    });
  }

  /**
   * 删除记录结果
   */
  @Setter
  @Getter
  @ToString
  public static class DeleteRecordResultDTO {
    /** 删除的记录列表 */
    private List<RecordResultDTO> records;

  }

  /**
   * 删除记录
   */
  @Setter
  @Getter
  @ToString
  public static final class RecordResultDTO {
    /** 删除的记录ID */
    private String recordId;
    /** 是否成功删除 true：成功删除 false：未删除 */
    private Boolean deleted;
  }
}
