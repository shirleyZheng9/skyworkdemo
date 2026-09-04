package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.lark.LarkResultDTO;
import com.iwhalecloud.bote.dto.plugin.params.LarkGetBitableMetaDataPluginParams;
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
 * 飞书获取多维表格元数据插件
 *
 * @author qian.sisheng
 * @since 2025-08-27
 */
@Component
public class LarkGetBitableMetaDataPlugin extends AbstractLarkPlugin<LarkGetBitableMetaDataPluginParams> {
  /** 查询多维表格元数据 url */
  private static final String QUERY_BITABLE_INFO_URL = "https://open.feishu.cn/open-apis/bitable/v1/apps/:appToken";

  public LarkGetBitableMetaDataPlugin() {
    super(LarkGetBitableMetaDataPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_LARK_GET_BITABLE_META_DATA;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("appId", "应用ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appSecret", "应用密钥", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("url", "多维表格链接地址", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appToken", "多维表格唯一标识", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("appToken", "多维表格唯一标识", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("name", "多维表格的名称", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("revision", "多维表格的版本号", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("isAdvanced", "多维表格是否开启了高级权限", AttrDataType.BOOLEAN));
    children.add(ParameterSpec.newProperty("timeZone", "多维表格的时区", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("formulaType", "多维表格的公式字段类型", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("advanceVersion", "文档高级权限版本", AttrDataType.STRING));
    ParameterSpec app = ParameterSpec.newObject("app", "多维表格元数据", children);
    return ParameterSpec.newRoot(
      Arrays.asList(ParameterSpec.newProperty("code", "编码", AttrDataType.STRING), ParameterSpec.newProperty("msg", "错误信息", AttrDataType.STRING),
        ParameterSpec.newObject("data", "返回对象", Arrays.asList(app, ParameterSpec.newProperty("authUrl", "授权链接", AttrDataType.STRING)))));
  }

  @Override
  public void validateParams(LarkGetBitableMetaDataPluginParams params) {
    Assert.hasText(params.getAppId(), "appID不能为空");
    Assert.hasText(params.getAppSecret(), "appSecret不能为空");
  }

  @Override
  public Object doRun(LarkGetBitableMetaDataPluginParams pluginParams) {
    return executeLarkApiCall(headers -> {
      String url = QUERY_BITABLE_INFO_URL.replace(":appToken", getAppToken(pluginParams));
      return HttpUtil.get(url, null, new ParameterizedTypeReference<Map<String, Object>>() {
      }, headers);
    }, pluginParams, new TypeReference<LarkResultDTO<QueryBitableInfoResultDTO>>() {
    });
  }

  /**
   * 多维表格查询结果
   */
  @Getter
  @Setter
  @ToString
  public static final class QueryBitableInfoResultDTO {
    /** 多维表格元数据 */
    private BitableMetaDataDTO app;
  }

  /**
   * 多维表格元数据
   */
  @Getter
  @Setter
  @ToString
  public static final class BitableMetaDataDTO {
    /** 多维表格的唯一标识 */
    private String appToken;
    /** 多维表格的名称 */
    private String name;
    /** 维表格的版本号 */
    private Integer revision;
    /** 多维表格是否开启了高级权限 */
    private Boolean isAdvanced;
    /** 多维表格的时区 */
    private String timeZone;
    /** 多维表格的公式字段类型 */
    private Integer formulaType;
    /** 文档高级权限版本 */
    private String advanceVersion;
  }
}
