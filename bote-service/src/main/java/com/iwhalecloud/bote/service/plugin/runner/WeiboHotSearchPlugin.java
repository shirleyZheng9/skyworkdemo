package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.WeiBoHotListDTO;
import com.iwhalecloud.bote.dto.plugin.params.WeiBoHotSearchPluginParams;
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

/**
 * 获取微博热搜插件
 *
 * @author qian.sisheng
 * @since 2025-07-23
 */
@Component
public class WeiboHotSearchPlugin extends AbstractPlugin<WeiBoHotSearchPluginParams> {

  /** 微博热搜接口 */
  private static final String WEI_BO_HOT_SEARCH_URL = "https://www.weibo.com/ajax/side/hotSearch";

  public WeiboHotSearchPlugin() {
    super(WeiBoHotSearchPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_WEI_BO_HOT_SEARCH;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return null;
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("heat", "热度", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("title", "热搜标题", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("sort", "排序", AttrDataType.INTEGER));
    ParameterSpec data = ParameterSpec.newObject("data", "微博热搜项", children);
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newList("list", "微博热搜列表", data)));
  }

  @Override
  public void validateParams(WeiBoHotSearchPluginParams params) {
    // no-op
  }

  @Override
  public Object doRun(WeiBoHotSearchPluginParams pluginParams) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("User-Agent", PluginConsts.USER_AGENT);
    headers.set("Referer", "https://www.weibo.com/");
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<WeiBoHotSearchResponse> response = HttpUtil.getRestTemplate()
      .exchange(WEI_BO_HOT_SEARCH_URL, HttpMethod.GET, requestEntity, WeiBoHotSearchResponse.class);
    WeiBoHotSearchResponse res = response.getBody();
    if (res == null || !"1".equals(res.getOk())) {
      throw new BssException("获取微博热搜失败");
    }
    List<WeiBoHotListDTO> list = new ArrayList<>();
    Map<String, Object> result = new HashMap<>();
    result.put("list", list);
    if (res.getData() == null) {
      return result;
    }
    List<WeiBoHotSearchRealtime> realtimeList = res.getData().getRealtime();
    for (WeiBoHotSearchRealtime weiBoHotSearch : CollectionUtils.emptyIfNull(realtimeList)) {
      WeiBoHotListDTO weiBoHotData = new WeiBoHotListDTO();
      weiBoHotData.setHeat(weiBoHotSearch.getNum());
      weiBoHotData.setTitle(weiBoHotSearch.getWord());
      weiBoHotData.setSort(weiBoHotSearch.getRank());
      list.add(weiBoHotData);
    }
    return result;
  }

  /**
   * 微博热搜接口返回数据
   */
  @Getter
  @Setter
  public static class WeiBoHotSearchResponse {
    /** 状态 成功 1 */
    private String ok;
    /** 数据 */
    private WeiBoHotSearchData data;
  }

  /**
   * 热搜数据
   */
  @Getter
  @Setter
  public static class WeiBoHotSearchData {
    /** 实时热搜数据列表 */
    private List<WeiBoHotSearchRealtime> realtime;
  }

  /**
   * 实时热搜数据
   */
  @Getter
  @Setter
  public static class WeiBoHotSearchRealtime {
    /** 热度 */
    private Long num;
    /** 热搜关键词 */
    private String word;
    /** 热搜排名 */
    private Integer rank;
  }
}
