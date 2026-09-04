package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.BiliBiliVideoSearchDTO;
import com.iwhalecloud.bote.dto.plugin.params.BiliBiliVideoSearchPluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 哔哩哔哩视频搜索插件
 *
 * @author qian.sisheng
 * @since 2025-07-24
 */
@Component
public class BiliBiliVideoSearchPlugin extends AbstractPlugin<BiliBiliVideoSearchPluginParams> {

  /** 哔哩哔哩视频搜索 URL */
  private static final String BILIBILI_VEDIO_SEARCH_URL = "https://api.bilibili.com/x/web-interface/wbi/search/all/v2";

  public BiliBiliVideoSearchPlugin() {
    super(BiliBiliVideoSearchPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_BILIBILI_VIDEO_SEARCH;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("cookie", "Cookie", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("keyword", "搜索关键词", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("pageNum", "页码", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("pageSize", "每页数量", AttrDataType.INTEGER));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("title", "视频标题", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("description", "视频描述", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("url", "链接", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("playCount", "播放次数", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("likes", "点赞数", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("bulletComments", "弹幕数", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("comments", "评论数", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("tag", "标签", AttrDataType.STRING));
    ParameterSpec data = ParameterSpec.newObject("data", "数据", children);
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newList("list", "数据列表", data)));
  }

  @Override
  public void validateParams(BiliBiliVideoSearchPluginParams params) {
    Assert.hasText(params.getCookie(), "cookie不能为空");
    Assert.hasText(params.getKeyword(), "关键词不能为空");
  }

  @Override
  public Object doRun(BiliBiliVideoSearchPluginParams pluginParams) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Cookie", pluginParams.getCookie());
    headers.set("User-Agent", PluginConsts.USER_AGENT);
    headers.set("Referer", "https://www.bilibili.com/");
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    if (pluginParams.getPageNum() == null) {
      pluginParams.setPageNum(1);
    }
    if (pluginParams.getPageSize() == null) {
      pluginParams.setPageSize(20);
    }
    String url =
      BILIBILI_VEDIO_SEARCH_URL + "?__refresh__=true&platform=pc&from_spmid=333.337&page" + pluginParams.getPageNum() + "&page_size=" + pluginParams.getPageSize() + "&keyword="
        + pluginParams.getKeyword() + "&wts=" + System.currentTimeMillis();
    ResponseEntity<BiliBiliVideoSearchResponse> response = HttpUtil.getRestTemplate()
      .exchange(url, HttpMethod.GET, requestEntity, BiliBiliVideoSearchResponse.class);
    BiliBiliVideoSearchResponse res = response.getBody();
    if (res == null || !"0".equals(res.getCode())) {
      throw new BssException("获取哔哩哔哩视频失败");
    }
    List<BiliBiliVideoSearchDTO> resultList = new ArrayList<>();
    Map<String, Object> result = new HashMap<>();
    result.put("list", resultList);
    if (res.getData() == null || res.getData().getResult() == null) {
      return result;
    }
    // 获取视频
    BiliBiliVideoSearchItem videoResult = IterableUtils.find(res.getData().getResult(),
      item -> "video".equals(item.getResultType()));
    if (videoResult == null) {
      return result;
    }
    for (BiliBiliVideoInfo videoInfo : CollectionUtils.emptyIfNull(videoResult.getData())) {
      BiliBiliVideoSearchDTO data = new BiliBiliVideoSearchDTO();
      if (StringUtils.isNoneBlank(videoInfo.getTitle())) {
        // 去除标题中的高亮标签
        data.setTitle(videoInfo.getTitle().replaceAll("<em[^>]*>(.*?)</em>", "$1"));
      }
      data.setUrl(videoInfo.getArcurl());
      data.setDescription(videoInfo.getDescription());
      data.setPlayCount(videoInfo.getPlay());
      data.setLikes(videoInfo.getLike());
      data.setBulletComments(videoInfo.getVideoReview());
      data.setComments(videoInfo.getReview());
      data.setTag(videoInfo.getTag());
      resultList.add(data);
    }
    return result;
  }

  /**
   * 响应数据
   */
  @Getter
  @Setter
  public static class BiliBiliVideoSearchResponse {
    /** 状态码 0成功 */
    private String code;
    /** 数据 */
    private BiliBiliVideoSearchData data;
  }

  /**
   * 搜索结果数据
   */
  @Getter
  @Setter
  public static class BiliBiliVideoSearchData {
    /** 搜索结果 */
    private List<BiliBiliVideoSearchItem> result;
  }

  /**
   * 搜索结果
   */
  @Getter
  @Setter
  public static class BiliBiliVideoSearchItem {
    /** 结果类型  video 视频 */
    @JsonProperty("result_type")
    private String resultType;
    /** 搜索结果数据 */
    private List<BiliBiliVideoInfo> data;
  }

  /**
   * 搜索结果数据
   */
  @Getter
  @Setter
  public static class BiliBiliVideoInfo {
    /** 视频标题 */
    private String title;
    /** 视频描述 */
    private String description;
    /** 视频链接 */
    private String arcurl;
    /** 播放量 */
    private Long play;
    /** 点赞数 */
    private Long like;
    /** 弹幕数 */
    @JsonProperty("video_review")
    private Long videoReview;
    /** 评论数 */
    private Long review;
    /** 标签 */
    private String tag;
  }
}
