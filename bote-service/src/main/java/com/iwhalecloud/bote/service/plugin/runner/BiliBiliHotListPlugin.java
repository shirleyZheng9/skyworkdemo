package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.BiliBiliHotListPluginParams;
import com.iwhalecloud.bote.dto.plugin.BiliBiliHotListDTO;
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
 * 哔哩哔哩热榜
 *
 * @author qian.sisheng
 * @since 2025-07-24
 */
@Component
public class BiliBiliHotListPlugin extends AbstractPlugin<BiliBiliHotListPluginParams> {

  /** 哔哩哔哩热榜 */
  private static final String BILIBILI_HOT_LIST_URL = "https://api.bilibili.com/x/web-interface/wbi/search/square";
  /** 哔哩哔哩详情 */
  private static final String BILIBILI_DETAIL_URL = "https://search.bilibili.com/all";

  public BiliBiliHotListPlugin() {
    super(BiliBiliHotListPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_BILIBILI_HOT_LIST;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newProperty("total", "查询总数", AttrDataType.INTEGER)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("title", "标题", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("url", "链接", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("heat", "热度", AttrDataType.INTEGER));
    ParameterSpec data = ParameterSpec.newObject("data", "数据项", children);
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newList("list", "数据列表", data)));
  }

  @Override
  public void validateParams(BiliBiliHotListPluginParams params) {
    if (params.getTotal() != null) {
      Assert.isTrue(params.getTotal() > 0, "查询总数必须大于0");
      Assert.isTrue(params.getTotal() <= 50, "查询总数最大50条");
    }
  }

  @Override
  public Object doRun(BiliBiliHotListPluginParams pluginParams) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("User-Agent", PluginConsts.USER_AGENT);
    headers.set("Referer", "https://www.bilibili.com/v/popular/all?spm_id_from=333.1007.0.0");
    if (pluginParams.getTotal() == null) {
      pluginParams.setTotal(10);
    }
    String url = BILIBILI_HOT_LIST_URL + "?web_location=333.934&platform=web&limit=" + pluginParams.getTotal() + "&wts=" + System.currentTimeMillis();
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<BiliBiliHotListResponse> response = HttpUtil.getRestTemplate()
      .exchange(url, HttpMethod.GET, requestEntity, BiliBiliHotListResponse.class);
    BiliBiliHotListResponse res = response.getBody();
    if (res == null || !"0".equals(res.getCode())) {
      throw new BssException("获取哔哩哔哩热榜失败");
    }
    Map<String, Object> result = new HashMap<>();
    List<BiliBiliHotListDTO> resultList = new ArrayList<>();
    result.put("list", resultList);
    if (res.getData() == null || res.getData().getTrending() == null) {
      return result;
    }
    for (HotData hotData : CollectionUtils.emptyIfNull(res.getData().getTrending().getList())) {
      BiliBiliHotListDTO biliBiliHotData = new BiliBiliHotListDTO();
      biliBiliHotData.setTitle(hotData.getShowName());
      biliBiliHotData.setUrl(BILIBILI_DETAIL_URL + "?keyword=" + hotData.getKeyword());
      biliBiliHotData.setHeat(hotData.getHeatScore());
      resultList.add(biliBiliHotData);
    }
    return result;
  }

  /**
   * 哔哩哔哩热榜接口返回结果
   */
  @Setter
  @Getter
  public static class BiliBiliHotListResponse {
    /** 状态码 0成功 */
    private String code;
    /** 数据 */
    private BiliBiliHotListData data;
  }

  /**
   * 热榜列表数据
   */
  @Setter
  @Getter
  public static class BiliBiliHotListData {
    /** 热榜趋势 */
    private Trending trending;
  }

  /**
   * 热榜趋势
   */
  @Setter
  @Getter
  public static class Trending {
    /** 热榜列表项 */
    private List<HotData> list;
  }

  /**
   * 热榜列表项
   */
  @Setter
  @Getter
  public static class HotData {
    /** 搜索关键字 */
    private String keyword;
    /** 标题 */
    @JsonProperty("show_name")
    private String showName;
    /** 热度 */
    @JsonProperty("heat_score")
    private Long heatScore;
  }
}
