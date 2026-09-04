package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.ABogusUtil;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.DouYinCommentDTO;
import com.iwhalecloud.bote.dto.plugin.params.DouYinCommentPluginParams;
import com.iwhalecloud.bote.dto.plugin.request.DouYinRequest;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 获取抖音评论插件
 *
 * @author qian.sisheng
 * @since 2025-07-18
 */
@Component
public class DouYinCommentPlugin extends AbstractPlugin<DouYinCommentPluginParams> {

  /** 获取抖音评论列表接口 */
  private static final String GET_COMMENT_URL = "https://www.douyin.com/aweme/v1/web/comment/list/";
  /** 获取 ttwid */
  private static final String GET_TTWID_URL = "https://ttwid.bytedance.com/ttwid/union/register/";
  /** 视频ID正则 */
  private static final Pattern DOUYIN_ID_PATTERN = Pattern.compile("(?:video|note)/([^/?]+)|[?&](?:vid|modal_id)=(\\d+)");
  /** Accept-Language */
  private static final String ACCEPT_LANGUAGE = "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7";
  /** 获取视频信息接口 */
  private static final String DOU_YIN_URL = "https://www.douyin.com/";

  public DouYinCommentPlugin() {
    super(DouYinCommentPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_DOUYIN_COMMENT;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("url", "抖音视频地址", AttrDataType.STRING),
      ParameterSpec.newProperty("pageSize", "每页数量，默认20", AttrDataType.NUMBER),
      ParameterSpec.newProperty("pageNum", "页数，默认0，从0开始", AttrDataType.NUMBER),
      ParameterSpec.newProperty("cookie", "cookie信息", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = Arrays.asList(ParameterSpec.newProperty("text", "评论内容", AttrDataType.STRING),
      ParameterSpec.newProperty("ipLabel", "用户IP", AttrDataType.STRING));
    ParameterSpec comment = ParameterSpec.newObject("comment", "评论", children);
    ParameterSpec comments = ParameterSpec.newList("comments", "评论列表", comment);
    return ParameterSpec.newRoot(Collections.singletonList(comments));
  }

  @Override
  public void validateParams(DouYinCommentPluginParams params) {
    Assert.hasText(params.getUrl(), "url不能为空");
    if (!params.getUrl().startsWith("https://v.douyin.com") && !params.getUrl().startsWith(DOU_YIN_URL)) {
      throw new BssException("URL格式不正确，请提供以下任一格式的链接：\n" +
        "1. https://www.douyin.com/xx\n" +
        "2. 抖音分享短链接： https://v.douyin.com/xx");
    }
  }

  @Override
  public Object doRun(DouYinCommentPluginParams pluginParams) {
    String videoId;
    String ttwid = getTtwid();
    // 抖音短链接重定向到长连接地址
    if (pluginParams.getUrl().startsWith("https://v.douyin.com")) {
      videoId = getDouYinVideoId(getLongUrl(pluginParams.getUrl(), ttwid));
    }
    else {
      videoId = getDouYinVideoId(pluginParams.getUrl());
    }
    if (StringUtils.isEmpty(videoId)) {
      throw new BssException("无法获取视频ID");
    }
    Map<String, Object> result = new HashMap<>();
    result.put("comments", buildComments(fetchVideoComments(videoId, pluginParams, ttwid)));
    return result;
  }

  /**
   * 构建评论
   */
  private List<DouYinCommentDTO> buildComments(List<CommentDTO> comments) {
    if (CollectionUtils.isEmpty(comments)) {
      return Collections.emptyList();
    }
    List<DouYinCommentDTO> douYinComments =  new ArrayList<>();
    for (CommentDTO comment : comments) {
      DouYinCommentDTO douYinComment = new DouYinCommentDTO();
      douYinComment.setText(comment.getText());
      douYinComment.setIpLabel(comment.getIpLabel());
      douYinComments.add(douYinComment);
    }
    return douYinComments;
  }

  /**
   * 获取抖音视频ID
   */
  private static String getDouYinVideoId(String url) {
    if (StringUtils.isEmpty(url)) {
      return "";
    }
    // 匹配 /xx/video(note)/ 后面的数字ID
    Matcher matcher = DOUYIN_ID_PATTERN.matcher(url);
    if (matcher.find()) {
      return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
    }
    return "";
  }

  /**
   * 获取长链接
   */
  private static String getLongUrl(String shortUrl, String cookie) {
    ResponseEntity<String> response = HttpUtil.getRestTemplate().exchange(shortUrl, HttpMethod.GET, buildHttpEntity(null, cookie), String.class);
    if (response.getStatusCode().is3xxRedirection()) {
      URI location = response.getHeaders().getLocation();
      if (location == null) {
        throw new BssException("链接失效，请输入有效的抖音分享短链接");
      }
      return location.toString();
    }
    throw new BssException("请输入有效的抖音分享短链接");
  }

  /**
   * 获取视频评论
   */
  private static List<CommentDTO> fetchVideoComments(String awemeId, DouYinCommentPluginParams pluginParams, String ttwid) {
    DoYinCommentRequest request = new DoYinCommentRequest();
    request.setAwemeId(awemeId);
    request.setCursor(StringUtils.isEmpty(pluginParams.getPageNum()) ? "0" : pluginParams.getPageNum());
    request.setCount(StringUtils.isEmpty(pluginParams.getPageSize()) ? "10" : pluginParams.getPageSize());
    // 构建请求参数
    Map<String, String> params = JsonUtil.convert(request, new TypeReference<Map<String, String>>() {
    });
    // 参数编码
    String paramString = MapUtils.emptyIfNull(params).entrySet().stream()
      .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
      .collect(Collectors.joining("&"));
    String url = GET_COMMENT_URL + "?" + paramString + "&a_bogus=" + ABogusUtil.signDetail(paramString, PluginConsts.USER_AGENT);
    DoYinCommentResponse result = HttpUtil.get(url, null, new ParameterizedTypeReference<DoYinCommentResponse>() {
    }, getHttpHeaders(ttwid));
    if (result == null || !"0".equals(result.getStatusCode())) {
      throw new BssException("获取抖音评论失败");
    }
    return result.getComments();
  }

  /**
   * 获取ttwid
   */
  private static String getTtwid() {
    ResponseEntity<DouYinTtwidResponse> response = HttpUtil.getRestTemplate()
      .exchange(GET_TTWID_URL, HttpMethod.POST, buildHttpEntity(new DouYinTtwidRequest(), null), DouYinTtwidResponse.class);
    DouYinTtwidResponse res = response.getBody();
    if (res == null || !"0".equals(res.getStatusCode())) {
      throw new BssException("获取抖音ttwid失败");
    }
    List<String> cookies = response.getHeaders().get("Set-Cookie");
    if (CollectionUtils.isEmpty(cookies)) {
      throw new BssException("获取抖音ttwid失败");
    }
    return cookies.stream().filter(string -> string.startsWith("ttwid")).findFirst().orElse("").split(";")[0];
  }

  /**
   * 构建HttpEntity
   */
  private static HttpEntity<?> buildHttpEntity(Object body, String cookie) {
    HttpHeaders headers = getHttpHeaders(cookie);
    return new HttpEntity<>(body, headers);
  }

  /**
   * 获取HttpHeaders
   *
   * @param cookie cookie
   * @return HttpHeaders
   */
  private static HttpHeaders getHttpHeaders(String cookie) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.USER_AGENT, PluginConsts.USER_AGENT);
    headers.set(HttpHeaders.ACCEPT_LANGUAGE, ACCEPT_LANGUAGE);
    headers.set(HttpHeaders.REFERER, DOU_YIN_URL);
    if (StringUtils.isNotEmpty(cookie)) {
      headers.set(HttpHeaders.COOKIE, cookie);
    }
    return headers;
  }

  /**
   * 抖音评论请求
   */
  @Getter
  @Setter
  @ToString
  public static final class DoYinCommentRequest extends DouYinRequest {
    /** 视频ID */
    @JsonProperty("aweme_id")
    private String awemeId;
    /** 分页游标 */
    private String cursor;
    /** 每页评论数 */
    private String count;
  }

  /**
   * 抖音评论响应
   */
  @Getter
  @Setter
  @ToString
  public static final class DoYinCommentResponse {
    /** 状态码 */
    @JsonProperty("status_code")
    private String statusCode;
    /** 评论列表 */
    private List<CommentDTO> comments;
  }

  /**
   * 抖音评论 DTO
   */
  @Getter
  @Setter
  @ToString
  public static final class CommentDTO {
    /** 评论内容 */
    private String text;
    /** ip地址 */
    @JsonProperty("ip_label")
    private String ipLabel;
  }

  /**
   * 抖音ttwid请求
   */
  @Getter
  @Setter
  @ToString
  public static final class DouYinTtwidRequest {
    /** 区域 */
    private String region = "cn";
    /** 应用ID */
    private Long aid = 1768L;
    /** 是否需要fid */
    private boolean needFid = false;
    /** 服务 */
    private String service = "www.ixigua.com";
    /** 回调协议 */
    private String cbUrlProtocol = "https";
    /** 是否为联合登录 */
    private boolean union = true;
    /** 迁移信息 */
    @JsonProperty("migrate_info")
    private DouYinTtwidMigrateInfo migrateInfo;
  }

  /**
   * 抖音ttwid信息
   */
  @Getter
  @Setter
  @ToString
  private static final class DouYinTtwidMigrateInfo {
    /** 票据 */
    private String ticket;
    /** 来源 */
    private String source = "node";
  }

  /**
   * 抖音ttwid响应
   */
  @Getter
  @Setter
  @ToString
  @JsonNaming(SnakeCaseStrategy.class)
  public static final class DouYinTtwidResponse {
    /** 状态码 */
    private String statusCode;
    /** 信息 */
    private String message;
    /** ttwid */
    private String redirectUrl;
  }
}
