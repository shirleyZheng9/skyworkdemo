package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.lark.LarkFiledInfoDTO;
import com.iwhalecloud.bote.dto.plugin.lark.LarkResultDTO;
import com.iwhalecloud.bote.dto.plugin.params.LarkTableFieldsPluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * 飞书获取数据表字段插件
 *
 * @author qian.sisheng
 * @since 2025-08-22
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class LarkTableFieldsPlugin extends AbstractLarkPlugin<LarkTableFieldsPluginParams> {
  /** 获取表格字段 url */
  private static final String TABLE_FIELDS_URL = "https://open.feishu.cn/open-apis/bitable/v1/apps/:appToken/tables/:tableId/fields";
  /** 表格字段参数 */
  private static final ClassPathResource resource = new ClassPathResource("/plugin-params/larkTableFields.json");

  public LarkTableFieldsPlugin() {
    super(LarkTableFieldsPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_LARK_TABLE_FIELDS;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("appId", "应用ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appSecret", "应用密钥", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("pageSize", "分页大小", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("pageToken", "分页标记", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appToken", "应用Token", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("tableId", "多维表格中表的唯一标识", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("url", "多维表格链接地址", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    try (InputStream inputStream = resource.getInputStream()) {
      return JsonUtil.parseJsonRequired(inputStream, ParameterSpec.class);
    }
    catch (Exception e) {
      logger.error("Failed to createResponseParameter, message={}", e.getMessage(), e);
      throw new BssException("创建飞书获取数据表字段插件响应参数失败: ", e.getMessage(), e);
    }
  }

  @Override
  public void validateParams(LarkTableFieldsPluginParams params) {
    Assert.hasText(params.getAppId(), "appID不能为空");
    Assert.hasText(params.getAppSecret(), "appSecret不能为空");
  }

  @Override
  public Object doRun(LarkTableFieldsPluginParams pluginParams) {
    return executeLarkApiCall(headers -> {
      // 构建请求参数
      MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
      if (StringUtils.isNotBlank(pluginParams.getPageToken())) {
        requestParams.add("page_token", pluginParams.getPageToken());
      }
      if (StringUtils.isNotBlank(pluginParams.getPageSize())) {
        requestParams.add("page_size", pluginParams.getPageSize());
      }
      // 构建URL
      String url = TABLE_FIELDS_URL.replace(":appToken", getAppToken(pluginParams)).replace(":tableId", getTableId(pluginParams));
      return HttpUtil.get(url, requestParams, new ParameterizedTypeReference<Map<String, Object>>() {
      }, headers);
    }, pluginParams, new TypeReference<LarkResultDTO<TableFieldsResultDTO>>() {
    });
  }

  @Getter
  @Setter
  @ToString
  private static final class TableFieldsResultDTO {
    /** 是否有更多数据 */
    private Boolean hasMore;
    /** 下一页的分页标记 */
    private String pageToken;
    /** 总数 */
    private Integer total;
    /** 表字段列表 */
    private List<LarkFiledInfoDTO> items;
  }
}
