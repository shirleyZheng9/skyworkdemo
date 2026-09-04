package com.iwhalecloud.bote.cache;

import com.github.houbb.sensitive.word.api.IWordAllow;
import com.github.houbb.sensitive.word.api.IWordDeny;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.mapper.base.SensitiveWordMapper;
import com.iwhalecloud.bss.litchi.cache.refresh.Refreshable;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * 敏感词缓存
 *
 * @author bianjp
 * @since 2024-08-02
 */
@Component
@RequiredArgsConstructor
public class SensitiveWordCache implements Refreshable, InitializingBean {
  private final SensitiveWordMapper sensitiveWordMapper;
  private final SensitiveWordBs instance = SensitiveWordBs.newInstance();

  /**
   * 检查文本中是否包含敏感词
   *
   * @param text 文本
   * @return 是否包含敏感词
   */
  public boolean isSensitive(String text) {
    if (StringUtils.isEmpty(text)) {
      return false;
    }
    return instance.contains(text);
  }

  /**
   * 获取文本中的敏感词列表
   *
   * @param text 文本
   * @return 敏感词列表
   */
  public List<String> findSensitiveWords(String text) {
    if (StringUtils.isEmpty(text)) {
      return Collections.emptyList();
    }
    List<String> words = instance.findAll(text);
    if (words.isEmpty()) {
      return Collections.emptyList();
    }
    return words.stream().distinct().collect(Collectors.toList());
  }

  @Override
  public void afterPropertiesSet() {
    updateSensitiveWords();
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_SENSITIVE_WORD;
  }

  @Override
  public boolean isDistributedCacheEnabled() {
    return false;
  }

  @Override
  public void refreshLocalCache() {
    updateSensitiveWords();
  }

  @Override
  public void refreshLocalCache(List<String> keys) {
    updateSensitiveWords();
  }

  @Override
  public void refresh() {
    updateSensitiveWords();
  }

  @Override
  public void refresh(List<String> keys) {
    updateSensitiveWords();
  }

  /**
   * 更新敏感词库
   *
   * <p>使用代码仓库中的默认敏感词库 + 数据库中的敏感词。</p>
   *
   * <p>不使用 houbb/sensitive-word 内置的黑名单({@link com.github.houbb.sensitive.word.support.deny.WordDenys#defaults()})，误报率太高了</p>
   */
  private void updateSensitiveWords() {
    List<String> defaultWhitelistWords = readLines(new ClassPathResource("sensitive-words/allow.txt"));
    List<String> defaultBlacklistWords = readLines(new ClassPathResource("sensitive-words/deny.txt"));
    List<String> whitelistWords = sensitiveWordMapper.selectWhitelistWords();
    List<String> blacklistWords = sensitiveWordMapper.selectBlacklistWords();
    IWordAllow wordAllow = () -> ListUtils.union(defaultWhitelistWords, whitelistWords);
    IWordDeny wordDeny = () -> ListUtils.union(defaultBlacklistWords, blacklistWords);
    instance.wordAllow(wordAllow).wordDeny(wordDeny).init();
  }

  /**
   * 读取文件的所有行，忽略空行和注释
   */
  private static List<String> readLines(ClassPathResource resource) {
    try (InputStream inputStream = resource.getInputStream();
         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      return reader.lines()
        .filter(line -> StringUtils.isNotEmpty(line) && !line.startsWith("#"))
        .collect(Collectors.toList());
    }
    catch (Exception e) {
      throw new IllegalStateException("加载内置敏感词失败: " + e.getMessage(), e);
    }
  }

}
