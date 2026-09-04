package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.DouYinHotListDTO;
import com.iwhalecloud.bote.dto.plugin.params.DouYinHotListPluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * 获取抖音热榜插件
 *
 * @author qian.sisheng
 * @since 2025-07-17
 */
@Component
public class DouYinHotListPlugin extends AbstractPlugin<DouYinHotListPluginParams> {

  /** 获取抖音热榜 url */
  private static final String GET_HOT_TOP_URL = "https://www.douyin.com/aweme/v1/web/hot/search/list";
  /** 抖音热榜详情 url 前缀 */
  private static final String DETAIL_URL_PREFIX = "https://www.douyin.com/search/";

  public DouYinHotListPlugin() {
    super(DouYinHotListPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_DOUYIN_HOT_TOP;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("boardType", "榜单类型，0:抖音热榜; 2:其他榜", AttrDataType.STRING),
      ParameterSpec.newProperty("boardSubType", "2:娱乐榜;4:社会榜;hotspot_challenge:挑战榜; seeding:种草榜;", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("title", "标题", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("url", "链接", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("heat", "热度", AttrDataType.STRING));
    ParameterSpec hotData = ParameterSpec.newObject("hotData", "热榜数据", children);
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newList("hotList", "热榜数据列表", hotData)));
  }

  @Override
  public void validateParams(DouYinHotListPluginParams params) {
    // intentionally left blank
  }

  @Override
  public Object doRun(DouYinHotListPluginParams pluginParams) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("User-Agent", PluginConsts.USER_AGENT);
    headers.set("Referer", "https://www.douyin.com/");
    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    queryParams.add("board_type", pluginParams.getBoardType());
    queryParams.add("board_sub_type", pluginParams.getBoardSubType());
    queryParams.add("version_name", "170400");
    queryParams.add("version_code", "17.4.0");
    DouYinHotTopResponse response = HttpUtil.get(GET_HOT_TOP_URL, queryParams, new ParameterizedTypeReference<DouYinHotTopResponse>() {
    }, headers);
    if (response == null || !"0".equals(response.getStatusCode())) {
      throw new BssException("获取抖音热榜失败");
    }
    Data data = response.getData();
    List<DouYinHotListDTO> hotList = new ArrayList<>();
    if (data != null) {
      for (Word word : CollectionUtils.emptyIfNull(data.getWordList())) {
        DouYinHotListDTO hotTop = new DouYinHotListDTO();
        hotTop.setTitle(word.getWord());
        hotTop.setHeat(word.getHotValue());
        // 抖音热榜详情地址
        hotTop.setUrl(DETAIL_URL_PREFIX + word.getWord() + "?type=general");
        hotList.add(hotTop);
      }
    }
    Map<String, Object> result = new HashMap<>();
    result.put("hotList", hotList);
    return result;
  }

  /**
   * 抖音热榜响应
   */
  @Getter
  @Setter
  @JsonNaming(SnakeCaseStrategy.class)
  public static final class DouYinHotTopResponse {
    /** 热搜榜数据 */
    private Data data;
    /** 状态码 0 成功 */
    private String statusCode;
  }

  /**
   * 热搜榜数据
   */
  @Getter
  @Setter
  @JsonNaming(SnakeCaseStrategy.class)
  public static final class Data {
    /** 榜单列表 */
    private List<Word> wordList;
  }

  /**
   * 榜单
   */
  @Getter
  @Setter
  @JsonNaming(SnakeCaseStrategy.class)
  public static final class Word {
    /** 标题 */
    private String word;
    /** 热度 */
    private String hotValue;
  }
}
