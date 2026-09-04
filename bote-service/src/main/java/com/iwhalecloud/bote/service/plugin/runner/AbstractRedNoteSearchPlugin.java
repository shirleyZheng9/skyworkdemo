package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.RedNoteNoteDTO;
import com.iwhalecloud.bote.dto.plugin.params.RedNoteSearchPluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;

/**
 * 小红书检索插件
 *
 * @author qian.sisheng
 * @since 2025-07-22
 */
public abstract class AbstractRedNoteSearchPlugin extends AbstractPlugin<RedNoteSearchPluginParams> {
  /** 搜索 URL */
  private static final String SEARCH_URL = "https://edith.xiaohongshu.com/api/sns/web/v1/search/notes";
  /** 笔记详情地址 URL */
  private static final String NOTE_URL = "https://www.xiaohongshu.com/explore/";

  public AbstractRedNoteSearchPlugin() {
    super(RedNoteSearchPluginParams.class);
  }

  /**
   * 获取排序方式，子类实现该方法以返回不同排序策略
   */
  protected abstract String getSortType();


  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("cookie", "Cookie", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("keyword", "搜索关键词", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("total", "查询总数", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("orderByLikedCount", "按点赞数排序", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("likedCount", "点赞数", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("url", "笔记链接", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("title", "笔记标题", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("id", "笔记ID", AttrDataType.STRING));
    ParameterSpec data = ParameterSpec.newObject("data", "数据", children);
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newList("list", "数据列表", data)));
  }

  @Override
  public void validateParams(RedNoteSearchPluginParams params) {
    Assert.hasText(params.getCookie(), "cookie不能为空");
    Assert.hasText(params.getKeyword(), "关键词不能为空");
    if (params.getTotal() != null) {
      Assert.isTrue(params.getTotal() > 0, "查询总数必须大于0");
      Assert.isTrue(params.getTotal() <= 230, "查询总数最大230条");
    }
  }

  @Override
  public Object doRun(RedNoteSearchPluginParams pluginParams) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Cookie", pluginParams.getCookie());
    headers.set("User-Agent", PluginConsts.USER_AGENT);
    headers.set("Referer", "https://www.xuehuishuo.com/");
    RedNoteSearchRequest request = new RedNoteSearchRequest();
    request.setKeyword(pluginParams.getKeyword());
    request.setSort(getSortType());
    // 根据总数计算查询页数
    int pageNum = pluginParams.getTotal() != null ? (int) Math.ceil((double) pluginParams.getTotal() / 20) : 1;
    List<RedNoteNoteDTO> list = new ArrayList<>();
    Map<String, Object> result = new HashMap<>();
    for (int i = 1; i <= pageNum; i++) {
      request.setPage(i);
      RedNoteSearchResponse response = HttpUtil.post(SEARCH_URL, request, new ParameterizedTypeReference<RedNoteSearchResponse>() {
      }, headers);
      if (response == null || !response.isSuccess()) {
        throw new BssException("小红书查询失败");
      }
      if (response.getData() == null) {
        break;
      }
      buildSearchResult(response, list);
      if (!response.getData().isHasMore()) {
        break;
      }
    }
    // 点赞数排序, 不做默认排序，进行综合搜索
    if ("desc".equals(pluginParams.getOrderByLikedCount())) {
      list.sort(Comparator.comparing(RedNoteNoteDTO::getLikedCount, Comparator.nullsLast(Comparator.reverseOrder())));
    }
    if ("asc".equals(pluginParams.getOrderByLikedCount())) {
      list.sort(Comparator.comparing(RedNoteNoteDTO::getLikedCount, Comparator.nullsLast(Comparator.naturalOrder())));
    }
    if (pluginParams.getTotal() != null) {
      list = list.subList(0, Math.min(list.size(), pluginParams.getTotal()));
    }
    result.put("list", list);
    return result;
  }

  /**
   * 构建搜索结果
   */
  private void buildSearchResult(RedNoteSearchResponse response, List<RedNoteNoteDTO> list) {
    for (RedNoteSearchItem item : CollectionUtils.emptyIfNull(response.getData().getItems())) {
      // 忽略热搜模块
      if ("hot_query".equals(item.getModelType())) {
        continue;
      }
      RedNoteNoteDTO note = new RedNoteNoteDTO();
      if (item.getNoteCard() != null) {
        note.setTitle(item.getNoteCard().getDisplayTitle());
        if (item.getNoteCard().getInteractInfo() != null) {
          Long likedCount = item.getNoteCard().getInteractInfo().getLikedCount();
          note.setLikedCount(likedCount != null ? likedCount : 0L);
        }
      }
      note.setUrl(NOTE_URL + item.getId() + "?xsec_token=" + item.getXsecToken() + "&xsec_source=pc_feed");
      note.setId(item.getId());
      list.add(note);
    }
  }

  /**
   * 小红书搜索请求参数
   */
  @Getter
  @Setter
  @JsonNaming(SnakeCaseStrategy.class)
  protected static class RedNoteSearchRequest {
    /** 搜索关键词 */
    private String keyword;
    /** 每页数量  每页数量只能 20 */
    private Integer pageSize = 20;
    /** 页码，默认值1 */
    private Integer page = 1;
    /** 排序 综合：general; 最热门：popularity_descending; 最新：time_descending */
    private String sort;
    /** 笔记类型 默认值0 全部 0; 视频 1; 图片 2; */
    private Integer noteType = 0;
    /** 搜索ID */
    private final String searchId = createSearchId();
    /** 图片格式 */
    private final List<String> imageFormats = Arrays.asList("jpg", "webp", "avif");

    /**
     * 生成搜索ID
     *
     * @return 36进制编码的搜索ID
     */
    private String createSearchId() {
      // 获取当前时间戳（毫秒）
      long timestamp = Instant.now().toEpochMilli();
      // 生成一个 0 到 0x7ffffffe 之间的随机整数
      long randomValue = (long) Math.ceil(0x7ffffffeL * RandomUtils.insecure().randomDouble());
      // 左移 64 位（相当于乘以 2^64）
      BigInteger timestampShifted = BigInteger.valueOf(timestamp).shiftLeft(64);
      // 相加
      BigInteger combined = timestampShifted.add(BigInteger.valueOf(randomValue));
      // 转为 36 进制字符串
      return base36Encode(combined);
    }

    /**
     * 将数字转换为36进制字符串
     *
     * @param number 要转换的数字
     * @return 36进制字符串
     */
    private String base36Encode(BigInteger number) {
      if (number.equals(BigInteger.ZERO)) {
        return "0";
      }
      StringBuilder result = new StringBuilder();
      BigInteger base = BigInteger.valueOf(36);
      while (number.compareTo(BigInteger.ZERO) > 0) {
        BigInteger[] divmod = number.divideAndRemainder(base);
        number = divmod[0];
        int remainder = divmod[1].intValue();
        result.append("0123456789abcdefghijklmnopqrstuvwxyz".charAt(remainder));
      }
      return result.reverse().toString();
    }
  }

  /**
   * 小红书检索响应
   */
  @Getter
  @Setter
  @JsonNaming(SnakeCaseStrategy.class)
  public static class RedNoteSearchResponse {
    /** 搜索结果 */
    private RedNoteSearchData data;
    /** 是否成功 */
    private boolean success;
  }

  /**
   * 笔记列表
   */
  @Getter
  @Setter
  @JsonNaming(SnakeCaseStrategy.class)
  public static class RedNoteSearchData {
    /** 是否有更多数据 */
    private boolean hasMore;
    /** 笔记列表 */
    private List<RedNoteSearchItem> items;
  }

  /**
   * 笔记列表项
   */
  @Getter
  @Setter
  @JsonNaming(SnakeCaseStrategy.class)
  public static class RedNoteSearchItem {
    /** 笔记ID */
    private String id;
    /** 笔记卡片 */
    private NoteCard noteCard;
    /** 小红书xsec_token */
    private String xsecToken;
    /** 笔记类型 hot_query:热搜模块; note:笔记 */
    private String modelType;
  }

  /**
   * 笔记卡片
   */
  @Getter
  @Setter
  @JsonNaming(SnakeCaseStrategy.class)
  public static class NoteCard {
    /** 笔记标题 */
    private String displayTitle;
    /** 笔记信息 */
    private InteractInfo interactInfo;
  }

  /**
   * 笔记信息
   */
  @Getter
  @Setter
  @JsonNaming(SnakeCaseStrategy.class)
  public static class InteractInfo {
    /** 点赞数 */
    private Long likedCount;
  }
}
