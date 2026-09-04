package com.iwhalecloud.bote.service.model.helper;

import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.config.properties.TiangongAiGatewayProperties;
import com.iwhalecloud.bote.dto.model.gateway.AimarketAiKeyResponse;
import com.iwhalecloud.bote.dto.model.gateway.AimarketModelPageResponse;
import com.iwhalecloud.bote.dto.model.gateway.AimarketModelRecord;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

/**
 * 天工 AI 市场 / 额度内部接口客户端
 *
 * @author jiangm
 * @since 2026-08-06
 */
@Component
@RequiredArgsConstructor
public class TiangongAiMarketClient {
  private static final Logger logger = LoggerFactory.getLogger(TiangongAiMarketClient.class);

  private static final String MODEL_PAGE_PATH = "/api/aimarket/model/page";
  /** 额度内部接口路径（挂在 quota-base-url 下） */
  private static final String AI_KEY_PATH = "/developer/quota/inner/queryaikey";
  /** 与额度侧一致：AES/ECB/PKCS5Padding，密钥为 SHA-256(authToken) */
  private static final String AI_KEY_CIPHER = "AES/ECB/PKCS5Padding";

  private final TiangongAiGatewayProperties properties;

  /**
   * 分页拉取全部已上架模型（status=1）
   */
  public List<AimarketModelRecord> listAllActiveModels() {
    assertConfigured();
    int pageSize = Math.max(1, properties.getPageSize());
    List<AimarketModelRecord> all = new ArrayList<>();
    long current = 1;
    long pages = 1;
    while (current <= pages) {
      AimarketModelPageResponse response = pageModels(current, pageSize);
      if (response.getData() == null) {
        break;
      }
      if (CollectionUtils.isNotEmpty(response.getData().getRecords())) {
        for (AimarketModelRecord record : response.getData().getRecords()) {
          if (record != null && Integer.valueOf(1).equals(record.getStatus())) {
            all.add(record);
          }
        }
      }
      Long totalPages = response.getData().getPages();
      pages = totalPages == null || totalPages < 1 ? 1 : totalPages;
      current++;
    }
    logger.info("Pulled {} active aimarket models", all.size());
    return all;
  }

  /**
   * 查询企业已开通 AI Key 列表（返回已解密明文）。
   * <p>enterpriseId 为外系统空间 ID（extSpaceId），不是 Bote spaceId。</p>
   * <p>接口 data 中为 AES-256(ECB) Base64 密文，密钥由 {@code bote.tg.ai-gateway.token} 的 SHA-256 派生。</p>
   */
  public List<String> listAiKeys(Long enterpriseId) {
    Assert.notNull(enterpriseId, "enterpriseId(extSpaceId) 不能为空");
    String quotaBase = resolveQuotaBaseUrl();
    Assert.hasText(quotaBase, "未配置 bote.tg.ai-gateway.quota-base-url（或 base-url）");
    Assert.hasText(properties.getToken(), "未配置 bote.tg.ai-gateway.token，无法解密 AI Key");
    String url = trimTrailingSlash(quotaBase) + AI_KEY_PATH + "?enterpriseId=" + enterpriseId;
    AimarketAiKeyResponse response = HttpUtil.get(url, (MultiValueMap<String, String>) null,
      new ParameterizedTypeReference<>() {
      }, authHeaders());
    if (response == null) {
      throw new BssException("查询企业 AI Key 失败，响应为空");
    }
    if (response.getCode() == null || response.getCode() != 0) {
      throw new BssException("查询企业 AI Key 失败: " + StringUtils.defaultString(response.getMsg()));
    }
    String token = properties.getToken().trim();
    List<String> plainKeys = new ArrayList<>();
    for (String cipher : CollectionUtils.emptyIfNull(response.getData())) {
      if (StringUtils.isBlank(cipher)) {
        continue;
      }
      String plain = decryptAiKey(token, cipher.trim());
      if (StringUtils.isNotBlank(plain)) {
        plainKeys.add(plain);
      }
      else {
        logger.warn("Decrypt AI key failed for enterpriseId={}, skip one cipher entry", enterpriseId);
      }
    }
    return plainKeys;
  }

  private AimarketModelPageResponse pageModels(long current, int size) {
    String url = trimTrailingSlash(properties.getBaseUrl()) + MODEL_PAGE_PATH + "?current=" + current + "&size=" + size;
    Map<String, Object> body = new HashMap<>(2);
    body.put("current", current);
    body.put("size", size);
    AimarketModelPageResponse response = HttpUtil.post(url, body, new ParameterizedTypeReference<>() {
    }, authHeaders());
    if (response == null) {
      throw new BssException("查询 aimarket 模型分页失败，响应为空");
    }
    if (response.getCode() == null || response.getCode() != 0) {
      throw new BssException("查询 aimarket 模型分页失败: " + StringUtils.defaultString(response.getMsg()));
    }
    return response;
  }

  private HttpHeaders authHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    // 外部接口要求 Authorization 为裸 token，不要加 Bearer 前缀
    String token = properties.getToken();
    if (StringUtils.isNotBlank(token)) {
      headers.set(HttpHeaders.AUTHORIZATION, token.trim());
    }
    return headers;
  }

  private void assertConfigured() {
    Assert.hasText(properties.getBaseUrl(), "未配置 bote.tg.ai-gateway.base-url");
  }

  private String resolveQuotaBaseUrl() {
    if (StringUtils.isNotBlank(properties.getQuotaBaseUrl())) {
      return properties.getQuotaBaseUrl().trim();
    }
    return StringUtils.trimToEmpty(properties.getBaseUrl());
  }

  /**
   * 使用 authToken 的 SHA-256 作为 AES-256 密钥，ECB/PKCS5 解密 Base64 密文。
   */
  @Nullable
  @SuppressFBWarnings({"CIPHER_INTEGRITY", "ECB_MODE"})
  @SuppressWarnings("java:S5542")
  static String decryptAiKey(String authToken, String cipherText) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] keyBytes = digest.digest(authToken.getBytes(StandardCharsets.UTF_8));
      Cipher cipher = Cipher.getInstance(AI_KEY_CIPHER);
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"));
      byte[] plain = cipher.doFinal(Base64.decodeBase64(cipherText));
      return new String(plain, StandardCharsets.UTF_8);
    }
    catch (Exception e) {
      logger.error("AES decrypt AI key error", e);
      return null;
    }
  }

  private static String trimTrailingSlash(String url) {
    if (StringUtils.isBlank(url)) {
      return "";
    }
    return StringUtils.removeEnd(url.trim(), "/");
  }
}
