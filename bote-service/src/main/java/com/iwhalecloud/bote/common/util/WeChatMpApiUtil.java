package com.iwhalecloud.bote.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.dto.plugin.params.WeChatMpAddMaterialParams.Descriptor;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings("PMD.GuardLogStatement")
public final class WeChatMpApiUtil {
  private static final Logger logger = LoggerFactory.getLogger(WeChatMpApiUtil.class);

  private static final String ERR_CODE = "errcode";
  private static final String ERR_MSG = "errmsg";

  private WeChatMpApiUtil() {
  }

  // 通用POST请求
  private static Map<String, Object> postForWeChat(String url, Object request, String actionDesc) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Object> entity = new HttpEntity<>(request, headers);
      ResponseEntity<String> response = HttpUtil.getRestTemplate().postForEntity(url, entity, String.class);

      if (response.getStatusCode().isError()) {
        logger.error("{}失败，状态码:{}", actionDesc, response.getStatusCode());
        throw new BssException(actionDesc + "失败，状态码:" + response.getStatusCode());
      }
      String body = response.getBody();
      if (StringUtils.isBlank(body)) {
        logger.error("{}失败，响应体为空", actionDesc);
        throw new BssException(actionDesc + "失败，响应体为空");
      }
      Map<String, Object> result = JsonUtil.parseJsonRequired(body, new TypeReference<Map<String, Object>>() {
      });
      checkWeChatResponse(result, actionDesc);
      return result;
    }
    catch (Exception e) {
      logger.error("{}异常:{}", actionDesc, e.getMessage(), e);
      throw new BssException(actionDesc + "异常", e);
    }
  }

  // 校验微信响应
  private static void checkWeChatResponse(Map<String, Object> response, String actionDesc) {
    Object errCode = response.get(ERR_CODE);
    if (errCode != null && !"0".equals(errCode.toString())) {
      throw new BssException(String.format("%s失败，errcode:%s, errmsg:%s", actionDesc, errCode, response.get(ERR_MSG)));
    }
  }

  // ===================== 各接口实现 =====================

  public static Map<String, Object> getStableToken(String appid, String secret, boolean forceRefresh) {
    String url = "https://api.weixin.qq.com/cgi-bin/stable_token";
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("grant_type", "client_credential");
    requestBody.put("appid", appid);
    requestBody.put("secret", secret);
    requestBody.put("force_refresh", forceRefresh);

    Map<String, Object> responseMap = postForWeChat(url, requestBody, "获取微信access_token");
    Map<String, Object> result = new HashMap<>();
    result.put("message", "获取微信access_token成功");
    result.put("accessToken", responseMap.get("access_token"));
    result.put("expiresIn", responseMap.get("expires_in"));
    return result;
  }

  public static Map<String, String> addMaterial(String accessToken, byte[] fileBytes, String materialType,
    Descriptor descriptor, String fileName) {
    String url =
      "https://api.weixin.qq.com/cgi-bin/material/add_material?access_token=" + accessToken + "&type=" + materialType;
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);

      MultipartBodyBuilder builder = new MultipartBodyBuilder();
      Resource mediaResource = new ByteArrayResource(fileBytes) {
        @Override
        public String getFilename() {
          return fileName;
        }
      };
      builder.part("media", mediaResource);
      builder.part("description", JsonUtil.toJsonString(descriptor), MediaType.APPLICATION_JSON);

      HttpEntity<MultiValueMap<String, HttpEntity<?>>> requestEntity = new HttpEntity<>(builder.build(), headers);
      RestTemplate restTemplate = HttpUtil.getRestTemplate();
      ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

      if (response.getStatusCode().isError()) {
        throw new BssException("上传永久素材失败，状态码: " + response.getStatusCode());
      }
      String responseBodyStr = response.getBody();
      if (StringUtils.isBlank(responseBodyStr)) {
        throw new BssException("上传永久素材失败，响应体为空");
      }
      Map<String, Object> result = JsonUtil.parseJsonRequired(responseBodyStr, new TypeReference<Map<String, Object>>() {
      });
      checkWeChatResponse(result, "上传永久素材");

      logger.info("上传永久素材成功，media_id: {}, url: {}", result.get("media_id"), result.get("url"));
      Map<String, String> resultMap = new HashMap<>();
      resultMap.put("message", "上传永久素材成功");
      resultMap.put("mediaId", String.valueOf(result.get("media_id")));
      resultMap.put("mediaUrl", String.valueOf(result.get("url")));
      return resultMap;
    }
    catch (Exception e) {
      logger.error("上传永久素材异常:{}", e.getMessage(), e);
      throw new BssException("上传永久素材异常", e);
    }
  }

  public static Map<String, String> addDraft(String accessToken, Map<String, Object> requestBody) {
    String url = "https://api.weixin.qq.com/cgi-bin/draft/add?access_token=" + accessToken;
    Map<String, Object> responseMap = postForWeChat(url, requestBody, "上传微信公众号草稿");
    String mediaId = String.valueOf(responseMap.get("media_id"));
    logger.info("上传微信公众号草稿成功，media_id: {}", mediaId);

    Map<String, String> resultMap = new HashMap<>();
    resultMap.put("message", "上传微信公众号草稿成功");
    resultMap.put("mediaId", mediaId);
    return resultMap;
  }

  public static Map<String, String> publishDraft(String accessToken, String mediaId) {
    String url = "https://api.weixin.qq.com/cgi-bin/freepublish/submit?access_token=" + accessToken;
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("media_id", mediaId);

    Map<String, Object> responseMap = postForWeChat(url, requestBody, "发布微信公众号草稿");
    logger.info("发布微信公众号草稿成功，mediaId: {}，responseMap: {}", mediaId, responseMap);

    Map<String, String> resultMap = new HashMap<>();
    resultMap.put("message", "发布微信公众号草稿成功");
    resultMap.put("publishId", String.valueOf(responseMap.get("publish_id")));
    resultMap.put("msgDataId", String.valueOf(responseMap.get("msg_data_id")));
    resultMap.put("mediaId", mediaId);
    return resultMap;
  }

  public static Map<String, String> delDraft(String accessToken, String mediaId) {
    String url = "https://api.weixin.qq.com/cgi-bin/draft/delete?access_token=" + accessToken;
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("media_id", mediaId);

    postForWeChat(url, requestBody, "删除微信公众号草稿");
    logger.info("删除微信公众号草稿成功，mediaId: {}", mediaId);

    Map<String, String> resultMap = new HashMap<>();
    resultMap.put("message", "删除微信公众号草稿成功");
    resultMap.put("mediaId", mediaId);
    return resultMap;
  }

  public static Map<String, String> delMaterial(String accessToken, String mediaId) {
    String url = "https://api.weixin.qq.com/cgi-bin/material/del_material?access_token=" + accessToken;
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("media_id", mediaId);

    postForWeChat(url, requestBody, "删除微信公众号素材");
    logger.info("删除微信公众号素材成功，mediaId: {}", mediaId);

    Map<String, String> resultMap = new HashMap<>();
    resultMap.put("message", "删除微信公众号素材成功");
    resultMap.put("mediaId", mediaId);
    return resultMap;
  }

  public static Map<String, String> delPublish(String accessToken, String articleId) {
    String url = "https://api.weixin.qq.com/cgi-bin/freepublish/delete?access_token=" + accessToken;
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("article_id", articleId);

    postForWeChat(url, requestBody, "删除微信公众号发布文章");
    logger.info("删除微信公众号发布文章成功，articleId: {}", articleId);

    Map<String, String> resultMap = new HashMap<>();
    resultMap.put("message", "删除微信公众号发布文章成功");
    resultMap.put("articleId", articleId);
    return resultMap;
  }

  public static Map<String, Object> qryPublishState(String accessToken, String publishId) {
    String url = "https://api.weixin.qq.com/cgi-bin/freepublish/get?access_token=" + accessToken;
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("publish_id", publishId);

    Map<String, Object> responseMap = postForWeChat(url, requestBody, "查询发布状态");
    logger.info("查询发布状态成功，publishId: {}", publishId);

    Map<String, Object> resultMap = new HashMap<>();
    resultMap.put("message", "查询发布状态成功");
    resultMap.put("publishId", publishId);
    resultMap.put("publishStatus", responseMap.get("publish_status"));
    resultMap.put("articleId", responseMap.get("article_id"));
    resultMap.put("articleDetail", responseMap.get("article_detail"));
    return resultMap;
  }

  public static Map<String, Object> qryPublishInfo(String accessToken, String articleId) {
    String url = "https://api.weixin.qq.com/cgi-bin/freepublish/getarticle?access_token=" + accessToken;
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("article_id", articleId);

    Map<String, Object> responseMap = postForWeChat(url, requestBody, "查询发布信息");
    logger.info("查询发布信息成功，articleId: {}", articleId);

    Map<String, Object> resultMap = new HashMap<>();
    resultMap.put("message", "查询发布信息成功");
    resultMap.put("newsItem", responseMap.get("news_item"));
    resultMap.put("articleId", articleId);
    return resultMap;
  }
}
