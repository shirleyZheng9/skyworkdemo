package com.iwhalecloud.bote.service.model.helper;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.model.request.CommonRequest;
import com.iwhalecloud.bote.dto.model.request.EvalRequest;
import com.iwhalecloud.bote.dto.model.request.FinetuneRequest;
import com.iwhalecloud.bote.dto.model.request.PredictRequest;
import com.iwhalecloud.bote.dto.model.response.PredictResponse;
import com.iwhalecloud.bote.dto.model.response.PredictResponse.PredictInfo;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.math.BigDecimal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 小模型推理、微调训练辅助类
 *
 * @author chen.linfa
 * @since 2025-02-18
 */
@Component
public class SmallModelAnswerHelper {

  // @formatter:off
  /** 部署接口地址 */
  private final String loadApiUrl;
  /** 下线接口地址 */
  private final String unloadApiUrl;
  /** 删除接口地址 */
  private final String deleteApiUrl;

  /** 推理接口地址 */
  private final String chatApiUrl;

  /** 微调接口地址 */
  private final String finetuneApiUrl;
  /** 微调中断接口地址 */
  private final String finetuneCancelApiUrl;

  /** 评测接口地址 */
  private final String evalApiUrl;
  /** 评测中断接口地址 */
  private final String evalCancelApiUrl;
  // @formatter:on

  public SmallModelAnswerHelper(Environment environment) {
    String baseUrl = StringUtils.stripEnd(environment.getProperty("slm.chat.apiUrl"), "/");
    if (StringUtils.isNotEmpty(baseUrl)) {
      this.loadApiUrl = baseUrl + "/v1/load";
      this.unloadApiUrl = baseUrl + "/v1/unload";
      this.deleteApiUrl = baseUrl + "/v1/delete";
      this.chatApiUrl = baseUrl + "/v1/predict";
    }
    else {
      this.loadApiUrl = null;
      this.unloadApiUrl = null;
      this.deleteApiUrl = null;
      this.chatApiUrl = null;
    }
    baseUrl = StringUtils.stripEnd(environment.getProperty("slm.finetune.apiUrl"), "/");
    if (StringUtils.isNotEmpty(baseUrl)) {
      this.finetuneApiUrl = baseUrl + "/v1/finetune";
      this.finetuneCancelApiUrl = baseUrl + "/v1/finetune/cancel";
      this.evalApiUrl = baseUrl + "/v1/eval";
      this.evalCancelApiUrl = baseUrl + "/v1/eval/cancel";
    }
    else {
      this.finetuneApiUrl = null;
      this.finetuneCancelApiUrl = null;
      this.evalApiUrl = null;
      this.evalCancelApiUrl = null;
    }
  }

  public ResultVO<Void> load(CommonRequest request) {
    Assert.notNull(loadApiUrl, "未开启小模型推理");
    HttpEntity<?> requestEntity = new HttpEntity<>(request, new HttpHeaders());
    ResponseEntity<ResultVO<Void>> responseEntity = HttpUtil.getRestTemplate()
      .exchange(loadApiUrl, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<ResultVO<Void>>() {
      });
    ResultVO<Void> result = responseEntity.getBody();
    if (result != null) {
      return result;
    }
    return ResultVO.fail("部署接口调用异常：" + loadApiUrl);
  }

  public ResultVO<Void> unload(CommonRequest request) {
    Assert.notNull(unloadApiUrl, "未开启小模型推理");
    HttpEntity<?> requestEntity = new HttpEntity<>(request, new HttpHeaders());
    ResponseEntity<ResultVO<Void>> responseEntity = HttpUtil.getRestTemplate()
      .exchange(unloadApiUrl, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<ResultVO<Void>>() {
      });
    ResultVO<Void> result = responseEntity.getBody();
    if (result != null) {
      return result;
    }
    return ResultVO.fail("下线接口调用异常：" + unloadApiUrl);
  }

  public ResultVO<Void> delete(CommonRequest request) {
    Assert.notNull(deleteApiUrl, "未开启小模型推理");
    HttpEntity<?> requestEntity = new HttpEntity<>(request, new HttpHeaders());
    ResponseEntity<ResultVO<Void>> responseEntity = HttpUtil.getRestTemplate()
      .exchange(deleteApiUrl, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<ResultVO<Void>>() {
      });
    ResultVO<Void> result = responseEntity.getBody();
    if (result != null) {
      return result;
    }
    return ResultVO.fail("删除接口调用异常：" + deleteApiUrl);
  }

  public ResultVO<PredictInfo> chat(PredictRequest request) {
    Assert.notNull(chatApiUrl, "未开启小模型推理");
    if (request.getThreshold() == null) {
      request.setThreshold(new BigDecimal("0.18"));
    }
    if (request.getMaxLength() == null) {
      request.setMaxLength(128);
    }
    HttpEntity<?> requestEntity = new HttpEntity<>(request, new HttpHeaders());
    ResponseEntity<PredictResponse> responseEntity = HttpUtil.getRestTemplate()
      .exchange(chatApiUrl, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<PredictResponse>() {
      });
    PredictResponse res = responseEntity.getBody();
    if (res != null) {
      if (res.isSuccess()) {
        return ResultVO.success(res.getResultObject());
      }
      return ResultVO.fail(res.getResultMsg());
    }
    return ResultVO.fail("推理接口调用异常：" + chatApiUrl);
  }

  public ResultVO<Void> finetune(FinetuneRequest request) {
    Assert.notNull(finetuneApiUrl, "未开启小模型微调训练");
    HttpEntity<?> requestEntity = new HttpEntity<>(request, new HttpHeaders());
    ResponseEntity<ResultVO<Void>> responseEntity = HttpUtil.getRestTemplate()
      .exchange(finetuneApiUrl, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<ResultVO<Void>>() {
      });
    ResultVO<Void> result = responseEntity.getBody();
    if (result != null) {
      return result;
    }
    return ResultVO.fail("微调接口调用异常：" + finetuneApiUrl);
  }

  public ResultVO<Void> eval(EvalRequest request) {
    Assert.notNull(evalApiUrl, "未开启小模型微调训练");
    HttpEntity<?> requestEntity = new HttpEntity<>(request, new HttpHeaders());
    ResponseEntity<ResultVO<Void>> responseEntity = HttpUtil.getRestTemplate()
      .exchange(evalApiUrl, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<ResultVO<Void>>() {
      });
    ResultVO<Void> result = responseEntity.getBody();
    if (result != null) {
      return result;
    }
    return ResultVO.fail("评测接口调用异常：" + evalApiUrl);
  }

  public ResultVO<Void> cancel(CommonRequest request, String type) {
    String url = BaseConsts.PUBLISH_TYPE_FINETUNE.equals(type) ? finetuneCancelApiUrl : evalCancelApiUrl;
    Assert.notNull(url, "未开启小模型微调训练");
    HttpEntity<?> requestEntity = new HttpEntity<>(request, new HttpHeaders());
    ResponseEntity<ResultVO<Void>> responseEntity = HttpUtil.getRestTemplate()
      .exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<ResultVO<Void>>() {
      });
    ResultVO<Void> result = responseEntity.getBody();
    if (result != null) {
      return result;
    }
    return ResultVO.fail("取消接口调用异常：" + url);
  }
}
