package com.iwhalecloud.bote.doc.module.knowledge.service;

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
import java.util.List;
import okhttp3.Headers;

/**
 * 知识中台接口服务
 *
 * @author lxs
 * @since 2025/07/14
 */
public interface IKnowledgePlatformApiService {

  /**
   * 获取知识中台鉴权令牌
   *
   * @return 鉴权令牌
   */
  ResultResponse<KnowledgeTokenDTO> getKnowledgeTokenByUserId(String userId);

  /**
   * 查询我创建的文件夹资源森林树
   *
   * @return 资源树节点列表
   */
  ResultResponse<List<ResourceTreeDTO>> queryMyFolderResourceTree();

  /**
   * 查询其他人共享给我的资源森林树
   *
   * @return 资源树节点列表
   */
  ResultResponse<List<ResourceTreeDTO>> querySharedResourceTree();

  /**
   * 查询我的资源分页列表
   *
   * @param queryRequest 查询参数
   * @return 资源树节点列表
   */
  ResultResponse<List<ResourceTreeDTO>> queryMyResourcePage(ResourceQueryRequest queryRequest);

  /**
   * 查询他人分享给我的资源分页列表
   *
   * @param queryRequest 查询参数
   * @return 资源树节点列表
   */
  ResultResponse<List<ResourceTreeDTO>> querySharedByOtherResourcePage(ResourceQueryRequest queryRequest);

  /**
   * 查询我创建的知识库列表
   *
   * @param queryRequest 查询参数
   * @return 知识库列表
   */
  ResultResponse<List<KnowledgeBasicDTO>> queryMyKnowledgeBaseList(KnowledgeQueryRequest queryRequest);

  /**
   * 查询他人分享给我的知识库列表
   *
   * @param queryRequest 查询参数
   * @return 知识库列表
   */
  ResultResponse<List<KnowledgeBasicDTO>> querySharedByOtherKnowledgeBaseList(KnowledgeQueryRequest queryRequest);

  /**
   * 查询知识库资源列表
   *
   * @param queryRequest 查询参数
   * @return 知识库资源列表
   */
  ResultResponse<List<KnowledgeResourceDTO>> queryKnowResourceWithTagsPage(KnowResourceQueryRequest queryRequest);

  /**
   * 知识召回
   *
   * @param queryRequest 查询参数
   * @return 召回结果
   */
  ResultResponse<KnowledgeSearchResponse> knowledgeSearch(KnowledgeSearchRequest queryRequest);

  /**
   * 知识问答
   *
   * @param queryRequest 查询参数
   * @return 问答结果
   */
  ResultResponse<List<KnowledgeCompleteResponse>> knowledgeComplete(KnowledgeCompleteRequest queryRequest);


  /**
   * 查询文档内容
   *
   * @param resourceType,resourceWid 文档参数
   * @return 文档内容
   */
  ResultResponse<KnowledgeDocContentResponse> knowledgeDocContent(String resourceType, String resourceWid);


  /**
   * 获取流式问答请求头
   *
   * @param queryRequest 查询参数
   * @return 请求头
   */
  Headers getChatStreamHeaders(KnowledgeCompleteRequest queryRequest);
}
