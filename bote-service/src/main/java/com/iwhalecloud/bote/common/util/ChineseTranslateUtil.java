package com.iwhalecloud.bote.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.iwhalecloud.bote.cache.ModelClientCache;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.Assert;

/**
 * 中文翻译英文 - 工具类
 *
 * @author chen.linfa
 * @since 2024-09-10
 */
public final class ChineseTranslateUtil {
  private static final Logger logger = LoggerFactory.getLogger(ChineseTranslateUtil.class);

  private ChineseTranslateUtil() {
  }

  /** 翻译结果缓存，key 为中文，value 为英文。不区分翻译方式，修改翻译方式配置后需要刷新缓存（通过平台参数管理页面修改会自动刷新） */
  private static final Cache<@NonNull String, @NonNull String> translationCache = CacheBuilder.newBuilder()
    .maximumSize(3000)
    .expireAfterWrite(Duration.ofHours(6))
    .build();

  /**
   * 清空翻译缓存
   */
  public static void clearCache() {
    translationCache.invalidateAll();
  }

  /**
   * 批量翻译
   *
   * @param chineseTexts 中文字符串列表
   * @return 翻译结果，key 为中文，value 为英文
   */
  @SuppressWarnings("unused")
  public static Map<String, String> batchTranslate(Collection<String> chineseTexts) {
    if (CollectionUtils.isEmpty(chineseTexts)) {
      return Collections.emptyMap();
    }
    Map<String, String> result = new HashMap<>(chineseTexts.size());
    // 优先取缓存，找出缓存中不存在的中文列表
    List<String> missingChineseTexts = new ArrayList<>();
    for (String text : chineseTexts) {
      String translation = translationCache.getIfPresent(text);
      if (translation != null) {
        result.put(text, translation);
      }
      else {
        missingChineseTexts.add(text);
      }
    }
    if (missingChineseTexts.isEmpty()) {
      return result;
    }

    translateByStrategy(result, missingChineseTexts);
    return result;
  }

  private static void translateByStrategy(Map<String, String> result, List<String> missingChineseTexts) {
    // 调用翻译接口
    String strategy = SystemParameter.TRANSLATE_STRATEGY.getValueFromDb();
    // 大模型
    if ("llm".equalsIgnoreCase(strategy)) {
      for (String text : missingChineseTexts) {
        String translatedText = translateByLlm(text);
        result.put(text, translatedText);
        translationCache.put(text, translatedText);
      }
    }
    // LibreTranslate
    else if ("libre".equalsIgnoreCase(strategy)) {
      List<String> translatedTexts = translateByLibre(missingChineseTexts);
      for (int i = 0; i < missingChineseTexts.size(); i++) {
        result.put(missingChineseTexts.get(i), translatedTexts.get(i));
        translationCache.put(missingChineseTexts.get(i), translatedTexts.get(i));
      }
    }
    // 拼音
    else {
      for (String text : missingChineseTexts) {
        String pinyin = translateToPinyin(text);
        result.put(text, pinyin);
        translationCache.put(text, pinyin);
      }
    }
  }

  /**
   * 翻译
   *
   * @param chinese 中文字符串
   * @return 英文
   */
  public static String translate(String chinese) {
    String strategy = SystemParameter.TRANSLATE_STRATEGY.getValueFromDb();
    if ("llm".equalsIgnoreCase(strategy)) {
      return translateByLlm(chinese);
    }
    else if ("libre".equalsIgnoreCase(strategy)) {
      return translateByLibre(Collections.singletonList(chinese)).get(0);
    }
    return translateToPinyin(chinese);
  }

  /**
   * 调用大模型进行中英翻译
   */
  public static String translateByLlm(String chinese) {
    String template = SystemParameter.CHINESE_TRANSLATE_PROMPT.getValueFromDb();
    Long modelId = Long.valueOf(SystemParameter.TRANSLATE_LARGE_MODEL_ID.getValueFromDb());

    Map<String, Object> data = new HashMap<>(4);
    data.put("content", chinese);
    String prompt = FreemarkerUtil.process(template, data);
    // 调用大模型
    ChatCompletionRequest request = ChatCompletionRequest.builder().addUserMessage(prompt).build();
    LlmClient modelClient = SpringUtil.getBean(ModelClientCache.class).getLlmClient(TenantIdUtil.getTenantId(), modelId);
    ChatCompletionResponse response = modelClient.chatCompletion(request);
    String content = response.getMessageContent();
    content = content.replace("```json", "");
    content = content.replace("```", "");
    Map<String, Object> map = JsonUtil.parseJson(content, new TypeReference<Map<String, Object>>() {
    });
    return MapUtils.isEmpty(map) ? content : MapUtils.getString(map, "content");
  }

  /**
   * 调用 LibreTranslate 翻译接口
   */
  @SuppressWarnings("unchecked")
  private static List<String> translateByLibre(List<String> chineseTexts) {
    String url = SystemParameter.TRANSLATE_LIBRE_URL.getValueFromDb();
    Assert.hasLength(url, "未配置 LibreTranslate 接口地址");
    // 是否是批量翻译
    boolean isBatch = chineseTexts.size() > 1;
    Map<String, Object> params = new HashMap<>(16);
    params.put("q", isBatch ? chineseTexts : chineseTexts.get(0));
    params.put("source", "zh");
    params.put("target", "en");
    params.put("format", "text");
    params.put("alternatives", 3);
    params.put("api_key", "");
    Map<String, Object> data = HttpUtil.post(url, params, new ParameterizedTypeReference<Map<String, Object>>() {
    });
    Assert.isTrue(MapUtils.isNotEmpty(data) && data.containsKey("translatedText"), "调用 LibreTranslate 接口失败");

    if (isBatch) {
      List<String> translatedTexts = (List<String>) data.get("translatedText");
      Assert.isTrue(chineseTexts.size() == translatedTexts.size(), "LibreTranslate 翻译失败，返回数量不一致");
      List<List<String>> alternativesList = (List<List<String>>) data.get("alternatives");
      List<String> translatedTextResult = new ArrayList<>();
      for (int i = 0; i < translatedTexts.size(); i++) {
        // 翻译出错时，返回 alternatives 的第一条数据
        if (Strings.CI.startsWith(translatedTexts.get(i), "Could not close temporary folder: %s")) {
          List<String> alternatives = alternativesList.get(i);
          translatedTextResult.add(alternatives.get(0));
        }
        else {
          translatedTextResult.add(translatedTexts.get(i));
        }
      }
      return translatedTextResult;
    }
    // 翻译出错时，返回 alternatives 的第一条数据
    if (Strings.CI.startsWith(MapUtils.getString(data, "translatedText"), "Could not close temporary folder: %s")) {
      List<String> alternatives = (List<String>) data.get("alternatives");
      return Collections.singletonList(alternatives.get(0));
    }
    return Collections.singletonList(MapUtils.getString(data, "translatedText"));
  }

  /**
   * 中文转拼音
   */
  public static String translateToPinyin(String chinese) {
    StringBuilder message = new StringBuilder();
    for (String str : chinese.split("\\s+")) {
      message.append(str.toUpperCase().equals(str) ? str.toLowerCase() : str).append(" ");
    }

    StringBuilder sb = new StringBuilder();
    char[] chars = message.toString().trim().toCharArray();
    HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
    defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
    defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    boolean isUppercase = false;
    try {
      for (char c : chars) {
        if (isChineseCharacter(c)) {
          String v = PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat)[0];
          sb.append(isUppercase ? StringUtils.capitalize(v) : v);
          isUppercase = false;
        }
        else if (CharUtils.isAsciiAlpha(c) || CharUtils.isAsciiNumeric(c)) {
          // 忽略特殊符号
          sb.append(isUppercase ? Character.toUpperCase(c) : c);
          isUppercase = false;
        }
        else {
          isUppercase = true;
        }
      }
    }
    catch (Exception e) {
      logger.error("Failed to translate pinyin, chinese={}", chinese, e);
      return message.toString();
    }
    return sb.toString();
  }

  /**
   * 使用Unicode编码范围来精确判断字符是否为中文字符
   *
   * @param c 字符
   * @return 是否为中文汉字
   */
  private static boolean isChineseCharacter(char c) {
    // \u4e00-\u9fff：CJK统一汉字（最常用的中文字符）
    // \u3400-\u4dbf：CJK扩展A（生僻字）
    // \u3100-\u312f：注音符号
    // \uf900-\ufaff：CJK兼容汉字
    return (c >= '\u4e00' && c <= '\u9fff') ||
           (c >= '\u3400' && c <= '\u4dbf') ||
           (c >= '\u3100' && c <= '\u312f') ||
           (c >= '\uf900' && c <= '\ufaff');
  }
}
