package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.ZhiHuHotSearchDTO;
import com.iwhalecloud.bote.dto.plugin.params.ZhiHuHotSearchPluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 获取知乎热搜插件
 *
 * @author qian.sisheng
 * @since 2025-07-23
 */
@Component
public class ZhiHuHotSearchPlugin extends AbstractPlugin<ZhiHuHotSearchPluginParams> {

  /** 知乎热搜接口 */
  private static final String ZHI_HU_HOT_SEARCH_URL = "https://www.zhihu.com/api/v3/feed/topstory/hot-lists/total";

  public ZhiHuHotSearchPlugin() {
    super(ZhiHuHotSearchPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_ZHIHU_HOT_SEARCH;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newProperty("cookie", "cookie数据", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec>  children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("title", "标题", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("url", "链接", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("desc", "描述", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("heat", "热度", AttrDataType.STRING));
    ParameterSpec data = ParameterSpec.newObject("data", "数据项", children);
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newList("list", "数据列表", data)));
  }

  @Override
  public void validateParams(ZhiHuHotSearchPluginParams params) {
    Assert.hasText(params.getCookie(), "cookie不能为空");
  }

  @Override
  public Object doRun(ZhiHuHotSearchPluginParams pluginParams) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("User-Agent", PluginConsts.USER_AGENT);
    headers.set("Referer", "https://www.zhihu.com/");
    headers.set("Cookie", pluginParams.getCookie());
    Map<String, Object> request = new HashMap<>();
    request.put("limit", "50");
    request.put("desktop", "true");
    HttpEntity<?> requestEntity = new HttpEntity<>(request, headers);
    ResponseEntity<ZhiHuHotSearchResponse> response = HttpUtil.getRestTemplate()
      .exchange(ZHI_HU_HOT_SEARCH_URL, HttpMethod.GET, requestEntity, ZhiHuHotSearchResponse.class);
    ZhiHuHotSearchResponse res = response.getBody();
    if (res == null) {
      throw new BssException("获取知乎热搜失败");
    }
    List<ZhiHuHotSearchData> dataList = res.getData();
    List<ZhiHuHotSearchDTO> resultList = new ArrayList<>();
    for (ZhiHuHotSearchData data : CollectionUtils.emptyIfNull(dataList)) {
      ZhiHuHotSearchDTO zhiHuHotSearchData = new ZhiHuHotSearchDTO();
      ZhiHuHotSearchTarget target = data.getTarget();
      if (target != null) {
        zhiHuHotSearchData.setTitle(target.getTitle());
        zhiHuHotSearchData.setUrl(target.getUrl());
        zhiHuHotSearchData.setDesc(target.getExcerpt());
      }
      zhiHuHotSearchData.setHeat(data.getDetailText());
      resultList.add(zhiHuHotSearchData);
    }
    Map<String, Object> result = new HashMap<>();
    result.put("list", resultList);
    return result;
  }

  /**
   * 知乎热搜响应
   */
  @Getter
  @Setter
  public static class ZhiHuHotSearchResponse {
    /** 热搜数据列表 */
    private List<ZhiHuHotSearchData> data;
  }

  /**
   * 知乎热搜结果
   */
  @Getter
  @Setter
  public static class ZhiHuHotSearchData {
    /** 热度 */
    @JsonProperty("detail_text")
    private String detailText;
    /** 详情数据 */
    private ZhiHuHotSearchTarget target;
  }

  /**
   * 详情数据
   */
  @Getter
  @Setter
  public static class ZhiHuHotSearchTarget {
    /** 标题 */
    private String title;
    /** 链接 */
    private String url;
    /** 描述 */
    private String excerpt;
  }
}
