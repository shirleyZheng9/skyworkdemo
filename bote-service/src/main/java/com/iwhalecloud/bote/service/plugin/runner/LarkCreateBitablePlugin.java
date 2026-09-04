package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.lark.LarkResultDTO;
import com.iwhalecloud.bote.dto.plugin.params.LarkCreateBitablePluginParams;
import java.util.Arrays;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 飞书多维表格插件
 *
 * @author auto
 * @since 2025-07-18
 */
@Component
public class  LarkCreateBitablePlugin extends AbstractLarkPlugin<LarkCreateBitablePluginParams> {
  /** 飞创建多维表格 url */
  private static final String LARK_API_CREATE_BITABLE = "https://open.feishu.cn/open-apis/bitable/v1/apps";

  public LarkCreateBitablePlugin() {
    super(LarkCreateBitablePluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_CREATE_LARK_BITABLE;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("appId", "应用ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appSecret", "应用密钥", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("name", "多维表格名称", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("folderToken", "文件夹token(可选)", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("timeZone", "文档时区", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("authUrl", "授权链接", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appToken", "多维表格唯一标识", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("name", "多维表格的名称", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("folderToken", "多维表格归属文件夹", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("url", "多维表格的 URL 链接", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("defaultTableId", "默认创建的数据表 ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("timeZone", "文档时区", AttrDataType.STRING));
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("code", "编码", AttrDataType.STRING), ParameterSpec.newProperty("msg", "错误信息", AttrDataType.STRING),
      ParameterSpec.newObject("data", "返回对象", Arrays.asList(ParameterSpec.newProperty("authUrl", "授权链接", AttrDataType.STRING),
        ParameterSpec.newObject("app", "多维表格信息", children)))));
  }

  @Override
  public void validateParams(LarkCreateBitablePluginParams params) {
    Assert.hasText(params.getAppId(), "appID不能为空");
    Assert.hasText(params.getAppSecret(), "appSecret不能为空");
  }

  @Override
  public Object doRun(LarkCreateBitablePluginParams params) {
    return executeLarkApiCall(headers -> {
      Map<String, Object> requestParams = new HashMap<>();
      requestParams.put("name", params.getName());
      if (StringUtils.isNotBlank(params.getFolderToken())) {
        requestParams.put("folder_token", params.getFolderToken());
      }
      return HttpUtil.post(LARK_API_CREATE_BITABLE, requestParams, new ParameterizedTypeReference<Map<String, Object>>() {
      }, headers);
    }, params, new TypeReference<LarkResultDTO<BitableResultDTO>>() {
    });
  }

  @Getter
  @Setter
  @ToString
  private static final class BitableResultDTO {
    /** 返回响应体 */
    private BitableDTO app;
  }

  @Getter
  @Setter
  @ToString
  private static final class BitableDTO {
    /** 多维表格的唯一标识 app_token */
    private String appToken;
    /** 多维表格的名称 */
    private String name;
    /** 多维表格归属文件夹 */
    private String folderToken;
    /** 多维表格的 URL 链接 */
    private String url;
    /** 默认创建的数据表 ID */
    private String defaultTableId;
    /** 文档时区 */
    private String timeZone;
  }
}
