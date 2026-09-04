package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.TouTiaoHotListDTO;
import com.iwhalecloud.bote.dto.plugin.params.TouTiaoHotListPluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
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

/**
 * 今日头条热搜插件
 *
 * @author qian.sisheng
 * @since 2025-07-23
 */
@Component
public class TouTiaoHotListPlugin extends AbstractPlugin<TouTiaoHotListPluginParams> {

  /** 今日头条热搜接口 */
  private static final String TOU_TIAO_HOT_LIST_URL = "https://www.toutiao.com/hot-event/hot-board/?origin=toutiao_pc";

  public TouTiaoHotListPlugin() {
    super(TouTiaoHotListPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_TOU_TIAO_HOT_LIST;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return null;
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("title", "标题", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("url", "链接", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("heat", "热度", AttrDataType.STRING));
    ParameterSpec data = ParameterSpec.newObject("data", "数据项", children);
    return ParameterSpec.newRoot(java.util.Collections.singletonList(ParameterSpec.newList("list", "数据列表", data)));
  }

  @Override
  public void validateParams(TouTiaoHotListPluginParams params) {
    // no-op
  }

  @Override
  public Object doRun(TouTiaoHotListPluginParams pluginParams) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("User-Agent", PluginConsts.USER_AGENT);
    headers.set("Referer", "https://www.toutiao.com/");
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<TouTiaoHotListResponse> response = HttpUtil.getRestTemplate()
      .exchange(TOU_TIAO_HOT_LIST_URL, HttpMethod.GET, requestEntity, TouTiaoHotListResponse.class);
    TouTiaoHotListResponse res = response.getBody();
    if (res == null || !"success".equals(res.getStatus())) {
      throw new BssException("获取今日头条热搜失败");
    }
    List<TouTiaoHotListData> dataList = res.getData();
    Map<String, Object> result = new HashMap<>();
    List<TouTiaoHotListDTO> resultList = new ArrayList<>();
    result.put("list", resultList);
    if (CollectionUtils.isEmpty(dataList)) {
      return resultList;
    }
    for (TouTiaoHotListData data : dataList) {
      TouTiaoHotListDTO hotData = new TouTiaoHotListDTO();
      hotData.setTitle(data.getTitle());
      hotData.setUrl(data.getUrl());
      hotData.setHeat(data.getHotValue());
      resultList.add(hotData);
    }
    return result;
  }

  /**
   * 今日头条热搜接口返回数据
   */
  @Getter
  @Setter
  public static class TouTiaoHotListResponse {
    /** 状态 success 成功 */
    private String status;
    /** 数据 */
    private List<TouTiaoHotListData> data;
  }

  /**
   * 今日头条热搜接口返回数据项
   */
  @Getter
  @Setter
  public static class TouTiaoHotListData {
    /** 标题 */
    @JsonProperty("Title")
    private String title;
    /** 链接 */
    @JsonProperty("Url")
    private String url;
    /** 热度 */
    @JsonProperty("HotValue")
    private String hotValue;
  }
}
