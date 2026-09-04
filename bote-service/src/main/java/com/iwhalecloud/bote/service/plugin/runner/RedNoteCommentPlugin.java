package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.RedNoteCommentDTO;
import com.iwhalecloud.bote.dto.plugin.params.RedNoteCommentPluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 小红书笔记评论插件
 *
 * @author qian.sisheng
 * @since 2025-07-25
 */
@Component
public class RedNoteCommentPlugin extends AbstractPlugin<RedNoteCommentPluginParams> {

  /** 小红书笔记评论接口 */
  private static final String RED_NOTE_COMMENT_URL = "https://edith.xiaohongshu.com/api/sns/web/v2/comment/page";
  /** 正则匹配 笔记ID 如/explore/abc123, /discovery/item/xyz789 */
  private static final Pattern NOTE_ID_PATTERN = Pattern.compile("/(?:explore|discovery/item)/([a-zA-Z0-9]+)(?:/|\\?|$)");
  /** 正则匹配 xsec_token 如URL参数 ?xsec_token=abc123 */
  private static final Pattern TOKEN_PATTERN = Pattern.compile("[?&]xsec_token=([^&]*)");

  public RedNoteCommentPlugin() {
    super(RedNoteCommentPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_RED_NOTE_COMMENT;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("cookie", "Cookie", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("url", "小红书链接", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> commonChildren = new ArrayList<>();
    commonChildren.add(ParameterSpec.newProperty("createTime", "创建时间", AttrDataType.NUMBER));
    commonChildren.add(ParameterSpec.newProperty("content", "内容", AttrDataType.STRING));
    commonChildren.add(ParameterSpec.newProperty("likeCount", "点赞数", AttrDataType.STRING));
    ParameterSpec subComments = ParameterSpec.newList("subComments", "子评论列表", ParameterSpec.newObject("subComment", "子评论", commonChildren));
    List<ParameterSpec> commentList = new ArrayList<>(commonChildren);
    commentList.add(subComments);
    ParameterSpec comments = ParameterSpec.newList("comments", "评论列表", ParameterSpec.newObject("comment", "评论", commentList));
    return ParameterSpec.newRoot(Collections.singletonList(comments));
  }

  @SuppressWarnings("HttpUrlsUsage")
  @Override
  public void validateParams(RedNoteCommentPluginParams params) {
    Assert.hasText(params.getCookie(), "cookie不能为空");
    Assert.hasText(params.getUrl(), "url不能为空");
    String url = params.getUrl();
    boolean isValidFormat = url.startsWith("https://www.xiaohongshu.com/explore")
      || url.startsWith("http://xhslink.com/")
      || url.startsWith("https://www.xiaohongshu.com/discovery/item/");
    if (!isValidFormat) {
      throw new BssException("URL格式不正确，请提供小红书笔记链接或分享短链接");
    }
  }

  @Override
  public Object doRun(RedNoteCommentPluginParams params) {
    Pair<String, String> pair = parseUrl(params);
    String noteId = pair.getLeft();
    String xsecToken = pair.getRight();
    String url = RED_NOTE_COMMENT_URL + "?note_id=" + noteId + "&xsec_token=" + xsecToken + "&image_formats=jpg,webp,avif";
    ResponseEntity<RedNoteCommentResponse> response = HttpUtil.getRestTemplate()
      .exchange(url, HttpMethod.GET, buildRequestEntity(params), RedNoteCommentResponse.class);
    RedNoteCommentResponse res = response.getBody();
    if (res == null) {
      throw new BssException("获取笔记评论失败");
    }
    if ("-101".equals(res.getCode())) {
      throw new BssException("Cookie已失效，请检查Cookie是否有效");
    }
    if (!"0".equals(res.getCode())) {
      throw new BssException("获取笔记评论失败，请检查链接是否有效");
    }
    List<RedNoteCommentDTO> redNoteComments = new ArrayList<>();
    Map<String, Object> result = new HashMap<>();
    result.put("comments", redNoteComments);
    if (res.getData() == null) {
      return result;
    }
    List<RedNoteCommentComment> comments = res.getData().getComments();
    result.put("comments", buildRedNoteComments(comments));
    return result;
  }

  /**
   * 构建请求参数
   */
  private List<RedNoteCommentDTO> buildRedNoteComments(List<RedNoteCommentComment> comments) {
    if (CollectionUtils.isEmpty(comments)) {
      return Collections.emptyList();
    }
    List<RedNoteCommentDTO> redNoteComments = new ArrayList<>();
    for (RedNoteCommentComment comment : comments) {
      List<RedNoteCommentDTO> subComments = new ArrayList<>();
      RedNoteCommentDTO redNoteComment = new RedNoteCommentDTO();
      redNoteComment.setCreateTime(comment.getCreateTime());
      redNoteComment.setContent(comment.getContent());
      redNoteComment.setLikeCount(comment.getLikeCount());
      for (RedNoteCommentSubComment subComment : comment.getSubComments()) {
        RedNoteCommentDTO redNoteSubComment = new RedNoteCommentDTO();
        redNoteSubComment.setCreateTime(subComment.getCreateTime());
        redNoteSubComment.setContent(subComment.getContent());
        redNoteSubComment.setLikeCount(subComment.getLikeCount());
        subComments.add(redNoteSubComment);
      }
      redNoteComment.setSubComments(subComments);
      redNoteComments.add(redNoteComment);
    }
    return redNoteComments;
  }

  /**
   * 解析链接
   */
  private Pair<String, String> parseUrl(RedNoteCommentPluginParams params) {
    String url = params.getUrl();
    String noteId = "";
    String xsecToken = "";
    // 处理短链接，只请求一次
    if (url.startsWith("http://xhslink.com/")) {
      url = getLongUrl(url, buildRequestEntity(params));
    }
    // 匹配 /explore/
    Matcher noteIdMatcher = NOTE_ID_PATTERN.matcher(url);
    if (noteIdMatcher.find()) {
      noteId = noteIdMatcher.group(1);
    }
    // 提取xsec_token
    Matcher tokenMatcher = TOKEN_PATTERN.matcher(url);
    if (tokenMatcher.find()) {
      xsecToken = tokenMatcher.group(1);
    }
    if (StringUtils.isBlank(noteId) || StringUtils.isBlank(xsecToken)) {
      throw new BssException("链接失效，请输入带有xsec_token的链接或者小红书分享的短链接");
    }
    return Pair.of(noteId, xsecToken);
  }

  /**
   * 获取长链接
   */
  private String getLongUrl(String shortUrl, HttpEntity<?> entity) {
    ResponseEntity<String> response = HttpUtil.getRestTemplate().exchange(shortUrl, HttpMethod.GET, entity, String.class);
    if (response.getStatusCode().is3xxRedirection()) {
      URI location = response.getHeaders().getLocation();
      if (location == null) {
        throw new BssException("链接失效，请输入有效的小红书分享的短链接");
      }
      return location.toString();
    }
    throw new BssException("请输入有效的小红书分享的短链接");
  }

  /**
   * 构建请求体
   */
  private HttpEntity<?> buildRequestEntity(RedNoteCommentPluginParams params) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("User-Agent", PluginConsts.USER_AGENT);
    headers.set("Referer", "https://www.xiaohongshu.com/");
    headers.set("Cookie", params.getCookie());
    return new HttpEntity<>(headers);
  }

  @Setter
  @Getter
  public static class RedNoteCommentResponse {
    /** 状态码 0 成功， -101 未登录 */
    private String code;
    /** 小红书评论接口数据 */
    private RedNoteCommentData data;
  }

  @Getter
  @Setter
  public static class RedNoteCommentData {
    /** 评论列表 */
    private List<RedNoteCommentComment> comments;
  }

  @Getter
  @Setter
  @JsonNaming(SnakeCaseStrategy.class)
  public static class RedNoteCommentComment {
    /** 创建时间 */
    private Long createTime;
    /** 评论内容 */
    private String content;
    /** 点赞数 */
    private String likeCount;
    /** 子评论 */
    private List<RedNoteCommentSubComment> subComments;
  }

  @Getter
  @Setter
  @JsonNaming(SnakeCaseStrategy.class)
  public static class RedNoteCommentSubComment {
    /** 创建时间 */
    private Long createTime;
    /** 评论内容 */
    private String content;
    /** 点赞数 */
    private String likeCount;
  }
}
