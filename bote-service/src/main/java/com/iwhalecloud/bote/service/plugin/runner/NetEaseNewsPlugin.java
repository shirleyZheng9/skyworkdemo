package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.NetEaseNewsPluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 网易新闻热榜插件
 *
 * @author qian.sisheng
 * @since 2025-11-28
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class NetEaseNewsPlugin extends AbstractPlugin<NetEaseNewsPluginParams> {

  private static final Logger logger = LoggerFactory.getLogger(NetEaseNewsPlugin.class);
  /** 网易新闻URL */
  private static final String NET_EASE_URL = "https://news.163.com/";

  public NetEaseNewsPlugin() {
    super(NetEaseNewsPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_NET_EASE_NEWS_HOT;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return null;
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newList("hotRankList", "热点列表",
      ParameterSpec.newObject("item", "热点列表项",
        Arrays.asList(ParameterSpec.newProperty("rank", "排名", AttrDataType.INTEGER),
          ParameterSpec.newProperty("url", "链接", AttrDataType.STRING),
          ParameterSpec.newProperty("title", "标题", AttrDataType.STRING),
          ParameterSpec.newProperty("heat", "热度", AttrDataType.INTEGER))))));
  }

  @Override
  public void validateParams(NetEaseNewsPluginParams params) {
    // 暂无参数
  }

  @Override
  public Object doRun(NetEaseNewsPluginParams pluginParams) {
    String html = HttpUtil.get(NET_EASE_URL, new HashMap<>());
    return Map.of("hotRankList", parseHotRankData(html));
  }

  /**
   * 解析网易新闻热榜数据
   *
   * @param htmlContent 网易新闻HTML内容
   * @return 热搜列表
   */
  public List<HotRankItemDTO> parseHotRankData(String htmlContent) {
    List<HotRankItemDTO> hotRankList = new ArrayList<>();
    try {
      Document doc = Jsoup.parse(htmlContent);
      // 查找目标div元素
      Element hotRankDiv = doc.selectFirst("div.mt35.mod_hot_rank.clearfix");
      if (hotRankDiv == null) {
        return hotRankList;
      }
      // 提取所有的li元素
      List<Element> liElements = hotRankDiv.select("ul li");
      for (int i = 0; i < liElements.size(); i++) {
        Element liElement = liElements.get(i);
        // 获取排名
        Element emElement = liElement.selectFirst("em");
        Integer rank = emElement != null ? Integer.parseInt(emElement.text()) : i + 1;
        // 获取链接和标题
        Element aElement = liElement.selectFirst("a");
        String url = aElement != null ? aElement.attr("href") : "";
        String title = aElement != null ? aElement.attr("title") : "";
        // 获取浏览量
        Element spanElement = liElement.selectFirst("span");
        Integer views = spanElement != null ? Integer.parseInt(spanElement.text()) : 0;
        HotRankItemDTO item = new HotRankItemDTO(rank, url, title, views);
        hotRankList.add(item);
      }
    } catch (Exception e) {
      // 记录错误日志
      logger.error("Failed to parse netEase news hot list: {}", e.getMessage());
      throw new BssException("解析网易热点新闻异常，msg=" + e.getMessage(), e);
    }
    return hotRankList;
  }

  /**
   * 热搜列表项
   *
   * @param rank 排名
   * @param url 链接
   * @param title 标题
   * @param heat 热度
   */
  public record HotRankItemDTO(Integer rank, String url, String title, Integer heat) {
  }
}
