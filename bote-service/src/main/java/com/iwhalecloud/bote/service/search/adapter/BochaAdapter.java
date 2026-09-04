package com.iwhalecloud.bote.service.search.adapter;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.search.WebPageInfo;
import com.iwhalecloud.bote.dto.search.params.BoChaParamsDTO;
import com.iwhalecloud.bote.dto.search.request.BochaSearchRequest;
import com.iwhalecloud.bote.dto.search.response.BochaSearchResponse;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * 联网搜索适配器 - 博查
 *
 * @author chen.linfa
 * @since 2025-03-13
 */
@Component
public class BochaAdapter implements IWebSearchAdapter {

  private static final Logger logger = LoggerFactory.getLogger(BochaAdapter.class);

  @Override
  public String getStrategyType() {
    return BaseConsts.WEB_SEARCH_BOCHA;
  }

  @Override
  public ResultVO<List<WebPageInfo>> search(String query) {
    String params = SystemParameter.WEB_SEARCH_BOCHA_PARAMS.getValueFromDb();
    if (StringUtils.isEmpty(params)) {
      return ResultVO.fail("联网搜索: 博查参数配置为空，请联系系统管理员");
    }
    BoChaParamsDTO boChaParams = JsonUtil.parseJson(params, BoChaParamsDTO.class);
    if (boChaParams == null) {
      return ResultVO.fail("联网搜索: 博查参数配置异常，请联系系统管理员");
    }
    if (StringUtils.isAnyEmpty(boChaParams.getUrl(), boChaParams.getToken())) {
      return ResultVO.fail("联网搜索: 博查参数配置异常，请联系系统管理员");
    }
    try {
      // 构建请求参数
      BochaSearchRequest request = new BochaSearchRequest();
      request.setFreshness(boChaParams.getFreshness());
      request.setSummary(boChaParams.getSummary());
      request.setCount(boChaParams.getCount());
      request.setQuery(query);
      HttpHeaders headers = new HttpHeaders();
      headers.add("Authorization", "Bearer " + boChaParams.getToken());
      HttpEntity<?> requestEntity = new HttpEntity<>(request, headers);

      ResponseEntity<BochaSearchResponse> responseEntity = HttpUtil.getRestTemplate()
        .exchange(boChaParams.getUrl(), HttpMethod.POST, requestEntity, new ParameterizedTypeReference<BochaSearchResponse>() {
        });
      BochaSearchResponse res = responseEntity.getBody();
      if (res != null) {
        if (!res.isSuccess()) {
          return ResultVO.fail(res.getMsg());
        }
        return ResultVO.success(res.getData().getWebPages().getValue());
      }
      return ResultVO.fail("联网搜索内容为空");
    }
    catch (Exception e) {
      logger.error("博查联网搜索请求失败: query={}", query, e);
      return ResultVO.fail("联网搜索请求失败: " + e.getMessage());
    }
  }
}
