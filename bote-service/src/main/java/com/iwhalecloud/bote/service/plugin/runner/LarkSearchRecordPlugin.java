package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.lark.LarkResultDTO;
import com.iwhalecloud.bote.dto.plugin.params.LarkSearchRecordPluginParams;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 飞书搜索记录插件
 *
 * @author qian.sisheng
 * @since 2025-08-25
 */
@Component
public class LarkSearchRecordPlugin extends AbstractLarkPlugin<LarkSearchRecordPluginParams> {
  /** 搜索记录 url */
  private static final String SEARCH_RECORD_URL = "https://open.feishu.cn/open-apis/bitable/v1/apps/:appToken/tables/:tableId/records/search";

  public LarkSearchRecordPlugin() {
    super(LarkSearchRecordPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_LARK_SEARCH_RECORD;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("appId", "应用ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appSecret", "应用密钥", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("redirectUrl", "回调地址", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("url", "多维表格链接地址", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("tableId", "表格ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appToken", "多维表格唯一标识", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("userIdType", "用户 ID 类型", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("pageToken", "分页标记", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("pageSize", "每页大小，最大值 500，默认值：20", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("viewId", "多维表格中视图的唯一标识", AttrDataType.STRING));
    children.add(ParameterSpec.newObject("sort", "排序", Arrays.asList(ParameterSpec.newProperty("fieldName", "字段名", AttrDataType.STRING),
      ParameterSpec.newProperty("order", "排序方式", AttrDataType.STRING))));
    children.add(ParameterSpec.newObject("filter", "过滤条件",
      Arrays.asList(ParameterSpec.newProperty("conjunction", "逻辑运算符", AttrDataType.STRING), ParameterSpec.newList("conditions", "过滤条件列表",
        ParameterSpec.newObject("condition", "过滤条件", Arrays.asList(ParameterSpec.newProperty("fieldName", "字段名", AttrDataType.STRING),
          ParameterSpec.newProperty("operator", "运算符", AttrDataType.STRING), ParameterSpec.newList("value", "字段值", null)))))));
    children.add(ParameterSpec.newList("fieldNames", "字段列表", null));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> itemProperty = new ArrayList<>();
    itemProperty.add(ParameterSpec.newProperty("fields", "记录字段", AttrDataType.STRING));
    itemProperty.add(ParameterSpec.newProperty("recordId", "记录 ID", AttrDataType.STRING));
    ParameterSpec item = ParameterSpec.newObject("item", "记录列表", itemProperty);

    ParameterSpec data = ParameterSpec.newObject("data", "返回对象", Arrays.asList(ParameterSpec.newList("items", "数据表列表", item),
      ParameterSpec.newProperty("pageToken", "分页标记，第一次请求不填，后续请求将返回上一页数据", AttrDataType.STRING),
      ParameterSpec.newProperty("total", "总数", AttrDataType.INTEGER),
      ParameterSpec.newProperty("hasMore", "是否有更多数据", AttrDataType.BOOLEAN),
      ParameterSpec.newProperty("authUrl", "授权链接", AttrDataType.STRING)));

    return ParameterSpec.newRoot(Arrays.asList(
      ParameterSpec.newProperty("code", "编码", AttrDataType.STRING),
      ParameterSpec.newProperty("msg", "错误信息", AttrDataType.STRING),
      data
    ));
  }

  @Override
  public void validateParams(LarkSearchRecordPluginParams params) {
    Assert.hasText(params.getAppId(), "appID不能为空");
    Assert.hasText(params.getAppSecret(), "appSecret不能为空");
  }

  @Override
  public Object doRun(LarkSearchRecordPluginParams pluginParams) {
   return executeLarkApiCall(headers -> {
     String url = SEARCH_RECORD_URL.replace(":appToken", getAppToken(pluginParams)).replace(":tableId", getTableId(pluginParams));
      return HttpUtil.post(url, snakeCaseMapper.convertValue(pluginParams, new TypeReference<Map<String, Object>>() {
      }), new ParameterizedTypeReference<Map<String, Object>>() {
      }, headers);
    }, pluginParams, new TypeReference<LarkResultDTO<SearchRecordResultDTO>>() {
    });
  }

  /**
   * 搜索记录结果
   */
  @Setter
  @Getter
  @ToString
  private static final class SearchRecordResultDTO {
    /** 是否还有更多项 */
    private Boolean hasMore;
    /** 分页标记，当 has_more 为 true 时，会同时返回新的 page_token，否则不返回 page_token */
    private String pageToken;
    /** 记录总数 */
    private Integer total;
    /** 记录列表 */
    private List<ItemDTO> items;
  }

  /**
   * 搜索记录字段
   */
  @Setter
  @Getter
  @ToString
  private static final class ItemDTO {
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
