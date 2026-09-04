package com.iwhalecloud.bote.service.asr.offline.helper;

import cn.xfyun.model.response.lfasr.LfasrResponse;
import cn.xfyun.model.sign.LfasrSignature;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 讯飞语音识别接口
 * <p>由于官方SDK中上传文件地址和查询结果地址为硬编码，无法动态配置，因此采用自定义实现方式</p>
 *
 * @author qian.sisheng
 * @since 2025-10-31
 */
@SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
@SuppressWarnings("PMD.GuardLogStatement")
public final class XfyunApiHelper {

  private static final Logger logger = LoggerFactory.getLogger(XfyunApiHelper.class);
  /** http客户端 */
  private static final OkHttpClient client = ModelHttpClient.getClient();

  private XfyunApiHelper() {
  }

  /**
   * 上传文件
   *
   * @param file 文件
   * @param uploadUrl 上传地址
   * @return 响应结果
   */
  @Nullable
  public static LfasrResponse uploadFile(File file, String uploadUrl, String appId, String secretKey) {
    try {
      Map<String, String> params = new HashMap<>(16);
      params.put("appId", appId);
      params.put("fileSize", String.valueOf(file.length()));
      params.put("fileName", file.getName());
      // 音频真实时长.当前未验证，可随机传一个数字
      params.put("duration", "200");
      // 签名
      LfasrSignature lfasrSignature = new LfasrSignature(appId, secretKey);
      params.put("signa", lfasrSignature.getSigna());
      params.put("ts", lfasrSignature.getTs());
      String finalUrl = buildUrl(uploadUrl, params);
      RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));
      Request request = new Request.Builder().url(finalUrl).post(fileBody).build();
      try (Response response = client.newCall(request).execute()) {
        if (response.body() == null) {
          return null;
        }
        return JsonUtil.parseJson(response.body().string(), LfasrResponse.class);
      }
    }
    catch (Exception e) {
      logger.error("Failed to upload file to xfyun speech recognition service, message={}", e.getMessage(), e);
      throw new BssException("讯飞语音识别异常", e);
    }
  }

  /**
   * 查询结果
   *
   * @param orderId 订单ID
   * @param getResultUrl 查询结果地址
   * @return 响应结果
   */
  @Nullable
  public static LfasrResponse getResult(String orderId, String getResultUrl, String appId, String secretKey) {
    try {
      Map<String, String> params = new HashMap<>(16);
      params.put("orderId", orderId);
      LfasrSignature lfasrSignature = new LfasrSignature(appId, secretKey);
      params.put("signa", lfasrSignature.getSigna());
      params.put("ts", lfasrSignature.getTs());
      params.put("appId", appId);
      // 结果类型 transfer:语音转写
      params.put("resultType", "transfer");
      String finalUrl = buildUrl(getResultUrl, params);
      Request request = new Request.Builder().url(finalUrl).get().build();
      try (Response response = client.newCall(request).execute()) {
        if (response.body() == null) {
          return null;
        }
        return JsonUtil.parseJson(response.body().string(), LfasrResponse.class);
      }
    }
    catch (Exception e) {
      logger.error("Failed to query xfyun speech recognition result, message={}", e.getMessage(), e);
      throw new BssException("查询讯飞语音结果异常", e);
    }
  }

  /**
   * 构建url
   *
   * @param getResultUrl 查询结果地址
   * @param params 参数
   * @return 构建后的url
   * @throws URISyntaxException url异常
   */
  private static String buildUrl(String getResultUrl, Map<String, String>  params) throws URISyntaxException {
    URIBuilder uriBuilder = new URIBuilder(getResultUrl);
    for (Map.Entry<String, String> entry : params.entrySet()) {
      uriBuilder.addParameter(entry.getKey(), entry.getValue());
    }
    return uriBuilder.build().toString();
  }
}
