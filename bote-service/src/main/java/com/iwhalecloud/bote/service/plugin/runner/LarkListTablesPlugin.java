package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.lark.LarkResultDTO;
import com.iwhalecloud.bote.dto.plugin.params.LarkListTablesPluginParams;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * 飞书获取表格列表插件
 *
 * @author qian.sisheng
 * @since 2025-08-22
 */
@Component
public class LarkListTablesPlugin extends AbstractLarkPlugin<LarkListTablesPluginParams> {
  /** 获取数据表 url */
  private static final String LARK_API_LIST_TABLES = "https://open.feishu.cn/open-apis/bitable/v1/apps/:appToken/tables";

  public LarkListTablesPlugin() {
    super(LarkListTablesPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_LARK_LIST_TABLES;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("appId", "应用ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appSecret", "应用密钥", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("url", "多维表格链接地址", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appToken", "多维表格唯一标识", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("pageSize", "分页大小", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("pageToken", "分页标记", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> tableProperties = new ArrayList<>();
    tableProperties.add(ParameterSpec.newProperty("tableId", "数据表ID", AttrDataType.STRING));
    tableProperties.add(ParameterSpec.newProperty("name", "数据表名称", AttrDataType.STRING));
    tableProperties.add(ParameterSpec.newProperty("revision", "数据表的版本号", AttrDataType.STRING));
    ParameterSpec item   = ParameterSpec.newObject("item", "数据表信息", tableProperties);
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
  public void validateParams(LarkListTablesPluginParams params) {
    Assert.hasText(params.getAppId(), "appID不能为空");
    Assert.hasText(params.getAppSecret(), "appSecret不能为空");
  }

  @Override
  public Object doRun(LarkListTablesPluginParams pluginParams) {
    return executeLarkApiCall(headers -> {
      MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
      requestParams.add("page_token", pluginParams.getPageToken());
      requestParams.add("page_size", pluginParams.getPageSize());
      String url = LARK_API_LIST_TABLES.replace(":appToken", getAppToken(pluginParams));
      return HttpUtil.get(url, requestParams, new ParameterizedTypeReference<Map<String, Object>>() {
      }, headers);
    }, pluginParams, new TypeReference<LarkResultDTO<ListTablesResultDTO>>() {
    });
  }

  @Setter
  @Getter
  @ToString
  private static final class ListTablesResultDTO {
    /** 是否有更多数据 */
    private Boolean hasMore;
    /** 下一页的分页标记 */
    private String pageToken;
    /** 总数 */
    private Integer total;
    /** 表格列表 */
    private List<LarkTableDTO> items;
  }

  @Setter
  @Getter
  @ToString
  private static final class LarkTableDTO {
    /** 表格ID */
    private String tableId;
    /** 表格名称 */
    private String name;
    /** 数据表的版本号。对数据表进行修改时更新，如新增、删除记录，修改数据表名称等，初始为 1，每次更新+1 */
    private Integer revision;
  }
}
