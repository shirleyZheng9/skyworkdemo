package com.iwhalecloud.bote.doc.module.knowledge.service.impl;

import com.iwhalecloud.bote.cache.ZhxyTokenCache;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.knowledge.access.platform.DocResourceDTO;
import com.iwhalecloud.bote.dto.knowledge.access.platform.KnowledgeBasicDTO;
import com.iwhalecloud.bote.dto.knowledge.access.platform.KnowledgeResourceDTO;
import com.iwhalecloud.bote.dto.knowledge.access.platform.KnowledgeTokenDTO;
import com.iwhalecloud.bote.dto.knowledge.access.platform.ResourceTreeDTO;
import com.iwhalecloud.bote.dto.knowledge.access.platform.request.KnowResourceQueryRequest;
import com.iwhalecloud.bote.dto.knowledge.access.platform.request.KnowledgeCompleteRequest;
import com.iwhalecloud.bote.dto.knowledge.access.platform.request.KnowledgeQueryRequest;
import com.iwhalecloud.bote.dto.knowledge.access.platform.request.KnowledgeSearchRequest;
import com.iwhalecloud.bote.dto.knowledge.access.platform.request.ResourceQueryRequest;
import com.iwhalecloud.bote.dto.knowledge.access.platform.response.KnowledgeCompleteResponse;
import com.iwhalecloud.bote.dto.knowledge.access.platform.response.KnowledgeDocContentResponse;
import com.iwhalecloud.bote.dto.knowledge.access.platform.response.KnowledgeSearchResponse;
import com.iwhalecloud.bote.dto.knowledge.access.platform.response.ResultResponse;
import com.iwhalecloud.bote.doc.module.knowledge.service.IKnowledgePlatformApiService;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.KnowledgePlatformApiHelper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import okhttp3.Headers;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

/**
 * 调用知识中台接口
 *
 * @author lxs
 * @since 2025/07/14
 */
@Service
@RequiredArgsConstructor
public class KnowledgePlatformApiServiceImpl implements IKnowledgePlatformApiService {

  @Value("${zhxy.apiUrl:}")
  private String apiUrl;

  @Value("${zhxy.defaultUserId:}")
  private String defaultUserId;

  /** 查询我创建的文件夹资源森林树接口地址 */
  private static final String EXCHANGE_TOKEN_API_URL = "/open-api/accessToken";

  /** 查询我创建的文件夹资源森林树接口地址 */
  private static final String MY_FOLDER_RESOURCE_TREE_API_URL = "/open-api/resource/tree/query";

  /** 查询其他人共享给我的资源森林树接口地址 */
  private static final String SHARED_RESOURCE_TREE_API_URL = "/open-api/resource/tree/shared";

  /** 查询我的资源分页列表接口地址 */
  private static final String MY_RESOURCE_PAGE_API_URL = "/open-api/resource/my/page";

  /** 查询他人分享给我的资源分页列表接口地址 */
  private static final String SHARED_BY_OTHER_RESOURCE_PAGE_API_URL = "/open-api/resource/shared/page";

  /** 查询我创建的知识库列表接口地址 */
  private static final String MY_KNOWLEDGE_BASE_LIST_API_URL = "/open-api/tDocAssistant/queryCreateByMe/page";

  /** 查询他人分享给我的知识库列表接口地址 */
  private static final String SHARED_BY_OTHER_KNOWLEDGE_API_URL = "/open-api/tDocAssistant/querySharedByOther/page";

  /** 查询知识库资源列表接口地址 */
  private static final String KNOW_RESOURCE_PAGE_API_URL = "/open-api/doc/assistantResource/queryKnowResourcePage";

  /** 知识召回 */
  private static final String KNOWLEDGE_SEARCH_API_URL = "/open-api/know/search";

  /** 知识问答 */
  private static final String KNOWLEDGE_COMPLETE_API_URL = "/open-api/chat/completions";

  /** 查询文档内容 */
  private static final String KNOWLEDGE_DOC_CONTENT_API_URL = "/open-api/tDocDocchainDocument/docContent";

  private final KnowledgePlatformApiHelper platformApiHelper;
  private final ZhxyTokenCache zhxyTokenCache;

  @Override
  public ResultResponse<KnowledgeTokenDTO> getKnowledgeTokenByUserId(String userId) {
    String pathUrl = EXCHANGE_TOKEN_API_URL + "/" + userId;
    HttpHeaders headers = platformApiHelper.buildHeader(pathUrl);
    return HttpUtil.get(apiUrl + pathUrl, new LinkedMultiValueMap<>(), new ParameterizedTypeReference<ResultResponse<KnowledgeTokenDTO>>() {
    }, headers);
  }

  @Override
  public ResultResponse<List<ResourceTreeDTO>> queryMyFolderResourceTree() {
    String url = apiUrl + MY_FOLDER_RESOURCE_TREE_API_URL;
    HttpHeaders headers = platformApiHelper.buildHeader(MY_FOLDER_RESOURCE_TREE_API_URL);
    setTokenHeader(headers);
    return HttpUtil.get(url, new LinkedMultiValueMap<>(), ParameterizedTypeReference.forType(ResultResponse.class), headers);
  }

  @Override
  public ResultResponse<List<ResourceTreeDTO>> querySharedResourceTree() {
    String url = apiUrl + SHARED_RESOURCE_TREE_API_URL;
    HttpHeaders headers = platformApiHelper.buildHeader(SHARED_RESOURCE_TREE_API_URL);
    setTokenHeader(headers);
    return HttpUtil.get(url, new LinkedMultiValueMap<>(), ParameterizedTypeReference.forType(ResultResponse.class), headers);
  }

  @Override
  public ResultResponse<List<ResourceTreeDTO>> queryMyResourcePage(ResourceQueryRequest queryRequest) {
    String url = apiUrl + MY_RESOURCE_PAGE_API_URL;
    HttpHeaders headers = platformApiHelper.buildHeader(JsonUtil.toJsonString(queryRequest));
    setTokenHeader(headers);
    return HttpUtil.post(url, queryRequest, ParameterizedTypeReference.forType(ResultResponse.class), headers);
  }

  @Override
  public ResultResponse<List<ResourceTreeDTO>> querySharedByOtherResourcePage(ResourceQueryRequest queryRequest) {
    String url = apiUrl + SHARED_BY_OTHER_RESOURCE_PAGE_API_URL;
    HttpHeaders headers = platformApiHelper.buildHeader(JsonUtil.toJsonString(queryRequest));
    setTokenHeader(headers);
    return HttpUtil.post(url, queryRequest, ParameterizedTypeReference.forType(ResultResponse.class), headers);
  }

  @Override
  public ResultResponse<List<KnowledgeBasicDTO>> queryMyKnowledgeBaseList(KnowledgeQueryRequest queryRequest) {
    String url = apiUrl + MY_KNOWLEDGE_BASE_LIST_API_URL;
    HttpHeaders headers = platformApiHelper.buildHeader(JsonUtil.toJsonString(queryRequest));
    setTokenHeader(headers);
    return HttpUtil.post(url, queryRequest, new ParameterizedTypeReference<ResultResponse<List<KnowledgeBasicDTO>>>() {
    }, headers);
  }

  @Override
  public ResultResponse<List<KnowledgeBasicDTO>> querySharedByOtherKnowledgeBaseList(KnowledgeQueryRequest queryRequest) {
    String url = apiUrl + SHARED_BY_OTHER_KNOWLEDGE_API_URL;
    HttpHeaders headers = platformApiHelper.buildHeader(JsonUtil.toJsonString(queryRequest));
    setTokenHeader(headers);
    return HttpUtil.post(url, queryRequest, new ParameterizedTypeReference<ResultResponse<List<KnowledgeBasicDTO>>>() {
    }, headers);
  }

  @Override
  public ResultResponse<List<KnowledgeResourceDTO>> queryKnowResourceWithTagsPage(KnowResourceQueryRequest queryRequest) {
    String url = apiUrl + KNOW_RESOURCE_PAGE_API_URL;
    HttpHeaders headers = platformApiHelper.buildHeader(JsonUtil.toJsonString(queryRequest));
    setTokenHeader(headers);
    return HttpUtil.post(url, queryRequest, new ParameterizedTypeReference<ResultResponse<List<KnowledgeResourceDTO>>>() {
    }, headers);
  }

  @Override
  public ResultResponse<KnowledgeSearchResponse> knowledgeSearch(KnowledgeSearchRequest queryRequest) {
    String url = apiUrl + KNOWLEDGE_SEARCH_API_URL;
    HttpHeaders headers = platformApiHelper.buildHeader(JsonUtil.toJsonString(queryRequest));
    setTokenHeader(headers);
    return HttpUtil.post(url, queryRequest, new ParameterizedTypeReference<ResultResponse<KnowledgeSearchResponse>>() {
    }, headers);
  }

  @Override
  public ResultResponse<List<KnowledgeCompleteResponse>> knowledgeComplete(KnowledgeCompleteRequest queryRequest) {
    String url = apiUrl + KNOWLEDGE_COMPLETE_API_URL;
    HttpHeaders headers = platformApiHelper.buildHeader(JsonUtil.toJsonString(queryRequest));
    setTokenHeader(headers);
    return HttpUtil.post(url, queryRequest, new ParameterizedTypeReference<ResultResponse<List<KnowledgeCompleteResponse>>>() {
    }, headers);
  }

  @Override
  public ResultResponse<KnowledgeDocContentResponse> knowledgeDocContent(String resourceType, String resourceWid) {
    DocResourceDTO docResourceDto = new DocResourceDTO();
    docResourceDto.setResourceType(resourceType);
    docResourceDto.setResourceWid(resourceWid);
    String url = apiUrl + KNOWLEDGE_DOC_CONTENT_API_URL;
    HttpHeaders headers = platformApiHelper.buildHeader(JsonUtil.toJsonString(docResourceDto));
    setTokenHeader(headers);
    return HttpUtil.post(url, docResourceDto,
        new ParameterizedTypeReference<ResultResponse<KnowledgeDocContentResponse>>() {
        }, headers);
  }

  @Override
  public Headers getChatStreamHeaders(KnowledgeCompleteRequest queryRequest) {
    HttpHeaders headers = platformApiHelper.buildHeader(JsonUtil.toJsonString(queryRequest));
    setTokenHeader(headers);
    headers.setAccept(Collections.singletonList(MediaType.TEXT_EVENT_STREAM));
    return Headers.of(headers.toSingleValueMap());
  }

  private void setTokenHeader(HttpHeaders headers) {
    // 获取知识中台用户ID
    String extUserId = SessionUtil.getLoginInfo().getExtUserId();
    // 如果没有(如API免鉴权调用)，则使用默认用户ID
    if (StringUtils.isEmpty(extUserId)) {
      extUserId = defaultUserId;
    }
    if (StringUtils.isEmpty(extUserId)) {
      throw BaseErrorConstant.EXT_USER_NOT_FOUND.toException();
    }
    // 先从缓存获取
    KnowledgeTokenDTO tokenInfo = zhxyTokenCache.query(extUserId);
    // 如果缓存中没有或者失效，则从接口获取
    if (tokenInfo == null) {
      ResultResponse<KnowledgeTokenDTO> knowledgeToken = getKnowledgeTokenByUserId(extUserId);
      if (knowledgeToken.isSuccess()) {
        KnowledgeTokenDTO data = knowledgeToken.getData();
        Pair<String, String> tokenPair = Pair.of(data.getTokenName(), data.getToken());
        headers.set(tokenPair.getLeft(), tokenPair.getRight());
        // 保存到缓存
        zhxyTokenCache.save(extUserId, data, data.getTokenTimeout().intValue());
      }
    }
    else {
      Pair<String, String> tokenPair = Pair.of(tokenInfo.getTokenName(), tokenInfo.getToken());
      headers.set(tokenPair.getLeft(), tokenPair.getRight());
    }
  }

}
