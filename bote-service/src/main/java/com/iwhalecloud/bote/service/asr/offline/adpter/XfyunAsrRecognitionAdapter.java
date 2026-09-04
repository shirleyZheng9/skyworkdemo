package com.iwhalecloud.bote.service.asr.offline.adpter;

import cn.xfyun.config.LfasrFailTypeEnum;
import cn.xfyun.config.LfasrOrderStatusEnum;
import cn.xfyun.model.response.lfasr.LfasrOrderResult;
import cn.xfyun.model.response.lfasr.LfasrResponse;
import com.google.gson.Gson;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.service.asr.offline.AbstractAsrRecognitionAdapter;
import com.iwhalecloud.bote.service.asr.offline.helper.XfyunApiHelper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.io.File;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 讯飞语音识别
 *
 * @author qian.sisheng
 * @since 2025-10-27
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class XfyunAsrRecognitionAdapter extends AbstractAsrRecognitionAdapter {

  private static final Logger logger = LoggerFactory.getLogger(XfyunAsrRecognitionAdapter.class);

  /** 使用Gson，LfasrOrderResult#Lattice 使用了 @SerializedName("json_1best") */
  private static final Gson GSON = new Gson();
  /** 轮询间隔（毫秒）*/
  private static final int POLL_INTERVAL = 3000;

  @Override
  protected String doRecognize(File file) {
    String appId = SystemParameter.VIDEO_ORC_XFYUN_APP_ID.getValueFromDb();
    String secretKey = SystemParameter.VIDEO_ORC_XFYUN_SECRET.getValueFromDb();
    if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(secretKey)) {
      throw new BssException("讯飞语音appId或讯飞语音secret为空，请检查配置");
    }
    String orderId;
    try {
      // 上传文件
      logger.info("讯飞语音识别开始上传音频文件");
      LfasrResponse response = XfyunApiHelper.uploadFile(file, getRequiredVideoRecognizeUrl(), appId, secretKey);
      if (response == null) {
        throw new BssException("对接讯飞语音识别异常，上传响应为空");
      }
      if (!Objects.equals(response.getCode(), "000000")) {
        throw new BssException("讯飞语音识别异常，上传失败，错误码：" + response.getCode() + "，错误信息：" + response.getDescInfo());
      }
      orderId = response.getContent().getOrderId();
    }
    catch (Exception e) {
      logger.error("Failed to perform xfyun recognition：{}", e.getMessage(), e);
      throw new BssException("讯飞语音识别异常, msg=" + e.getMessage(), e);
    }
    logger.info("讯飞音频上传完成，订单ID：{}", orderId);
    // 查询处理结果
    ResultVO<String> result = waitFinish(orderId, appId, secretKey);
    logger.info("讯飞语音识别结果：{}", result);
    if (!result.isSuccess()) {
      throw new BssException(result.getResultMsg());
    }
    return result.getResultObject();
  }

  /**
   * 等待处理完成
   */
  @SuppressWarnings("BusyWait")
  private ResultVO<String> waitFinish(String orderId, String appId, String secretKey) {
    // 超时时间（毫秒）
    int maxWaitTime = getMaxWaitTime();
    long startTime = System.currentTimeMillis();
    try {
      do {
        // 检查订单状态
        ResultVO<String> result = checkOrderStatus(orderId, appId, secretKey);
        if (result != null) {
          return result;
        }
        Thread.sleep(POLL_INTERVAL);
      }
      while (System.currentTimeMillis() - startTime < maxWaitTime);
    }
    catch (InterruptedException e) {
      logger.error("Failed to poll query xfyun asr state, interrupted: orderId={}", orderId, e);
      Thread.currentThread().interrupt();
      return ResultVO.fail("异常中断：" + e.getMessage());
    }
    catch (BssException | IllegalStateException | IllegalArgumentException e) {
      logger.error("Failed to poll query xfyun asr state: interrupted: orderId={}, error={}", orderId, e.getMessage());
      return ResultVO.fail("异常中断：" + e.getMessage());
    }
    return ResultVO.fail("");
  }

  /**
   * 获取最大等待时间
   */
  private int getMaxWaitTime() {
    Integer maxWaitTime = SystemParameter.VIDEO_ORC_XFYUN_TIMEOUT.getIntegerValueFromDb();
    if (maxWaitTime == null || maxWaitTime <= 0) {
      maxWaitTime = 300;
    }
    maxWaitTime = maxWaitTime * 1000;
    return maxWaitTime;
  }

  /**
   * 检查订单状态
   */
  @Nullable
  @SuppressWarnings("PMD.GuardLogStatement")
  private ResultVO<String> checkOrderStatus(String orderId, String appId, String secretKey) {
    try {
      // 获取转写结果
      String queryResultUrl = SystemParameter.VIDEO_ORC_XFYUN_QUERY_URL.getValueFromDb();
      LfasrResponse response = XfyunApiHelper.getResult(orderId, queryResultUrl, appId, secretKey);
      if (response == null) {
        return ResultVO.fail("查询订单结果失败，响应为空");
      }
      if (!Objects.equals(response.getCode(), "000000")) {
        String failMsg = "查询订单失败，错误码：" + response.getCode() + "，错误信息：{}" + response.getDescInfo();
        return ResultVO.fail(failMsg);
      }
      // 获取订单状态信息
      if (response.getContent() != null && response.getContent().getOrderInfo() != null) {
        int status = response.getContent().getOrderInfo().getStatus();
        // 根据状态进行处理
        LfasrOrderStatusEnum statusEnum = LfasrOrderStatusEnum.getEnum(status);
        if (statusEnum != null) {
          logger.info("订单状态：{}", statusEnum.getValue());
          switch (statusEnum) {
            case FAILED:
              int failType = response.getContent().getOrderInfo().getFailType();
              String failMsg = "订单处理失败，失败原因：" + LfasrFailTypeEnum.getEnum(failType).getValue();
              return ResultVO.fail(failMsg);
            case COMPLETED:
              String content = parseOrderResult(response.getContent().getOrderResult());
              return ResultVO.success(content);
            default:
              // 其他状态继续轮询
              break;
          }
        }
      }
    }
    catch (Exception e) {
      logger.error("Failed to query xfyun asr state: orderId={}, error={}", orderId, e.getMessage(), e);
      return ResultVO.fail(e.getMessage());
    }
    return null;
  }

  /**
   * 解析转写结果
   */
  private String parseOrderResult(String orderResultStr) {
    LfasrOrderResult result = GSON.fromJson(orderResultStr, LfasrOrderResult.class);
    logger.info("讯飞语音识别订单结果：orderResultStr={}", orderResultStr);
    if (result == null) {
      return "";
    }
    StringBuilder text = new StringBuilder();
    // 遍历所有 lattice
    for (LfasrOrderResult.Lattice lattice : CollectionUtils.emptyIfNull(result.getLattice())) {
      LfasrOrderResult.Json1Best json1Best = lattice.getJson1Best();
      if (json1Best == null || json1Best.getSt() == null || json1Best.getSt().getRt() == null) {
        continue;
      }
      StringBuilder rlText = getRlText(json1Best);
      text.append(rlText);
    }
    return text.toString();
  }

  /**
   * 从Json1Best中提取识别结果文本并拼接
   */
  private StringBuilder getRlText(LfasrOrderResult.Json1Best json1Best) {
    StringBuilder rlText = new StringBuilder();
    for (LfasrOrderResult.RecognitionResult rt : json1Best.getSt().getRt()) {
      if (rt.getWs() == null) {
        continue;
      }
      for (LfasrOrderResult.WordResult ws : rt.getWs()) {
        if (ws.getCw() != null && !ws.getCw().isEmpty()) {
          // 获取每个词的识别结果
          String word = ws.getCw().getFirst().getW();
          if (word != null && !word.isEmpty()) {
            rlText.append(word);
          }
        }
      }
    }
    return rlText;
  }

  @Override
  public String getVideoRecognizeType() {
    return "xfyun";
  }
}
