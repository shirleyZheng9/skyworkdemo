package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.lark.LarkResultDTO;
import com.iwhalecloud.bote.dto.plugin.params.LarkSearchDocPluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 飞书搜索多维表格类型的文档插件
 *
 * @author qian.sisheng
 * @since 2025-08-26
 */
@Component
public class LarkSearchDocPlugin extends AbstractLarkPlugin<LarkSearchDocPluginParams> {
  /** 批量获取文档信息 url */
  private static final String SEARCH_DOC_URL = "https://open.feishu.cn/open-apis/suite/docs-api/search/object";
  /** 批量获取文档元信息 url*/
  private static final String BATH_QUERY_DOC_INFO_URL = "https://open.feishu.cn/open-apis/drive/v1/metas/batch_query";

  public LarkSearchDocPlugin() {
    super(LarkSearchDocPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_LARK_SEARCH_DOC;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("appId", "应用ID", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("appSecret", "应用密钥", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("searchKey", "指定搜索的关键字。", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("count", "指定搜索返回的文件数量。取值范围为 [0,50]。", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("offset",
      "指定搜索的偏移量，该参数最小为 0，即不偏移。该参数的值与返回的文件数量之和不得大于或等于 200（即 offset + count < 200）。", AttrDataType.INTEGER));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    ParameterSpec docsEntities = ParameterSpec.newList("docsEntities", "包含搜索关键词的文件列表",
      ParameterSpec.newObject("docsEntity", "文档对象",
        Arrays.asList(ParameterSpec.newProperty("docsToken", "文件的 token", AttrDataType.STRING),
          ParameterSpec.newProperty("docsType", "文件类型", AttrDataType.STRING),
          ParameterSpec.newProperty("ownerId", "文件所有者", AttrDataType.STRING),
          ParameterSpec.newProperty("title", "文件标题", AttrDataType.STRING),
           ParameterSpec.newProperty("url", "文件链接", AttrDataType.STRING)))
    );
    return ParameterSpec.newRoot(
      Arrays.asList(ParameterSpec.newProperty("code", "编码", AttrDataType.STRING), ParameterSpec.newProperty("msg", "错误信息", AttrDataType.STRING),
        ParameterSpec.newObject("data", "返回对象", Arrays.asList(docsEntities, ParameterSpec.newProperty("authUrl", "授权链接", AttrDataType.STRING)))));
  }

  @Override
  public void validateParams(LarkSearchDocPluginParams params) {
    Assert.hasText(params.getAppId(), "appID不能为空");
    Assert.hasText(params.getAppSecret(), "appSecret不能为空");
    Assert.hasText(params.getSearchKey(), "searchKey不能为空");
  }

  @Override
  public Object doRun(LarkSearchDocPluginParams pluginParams) {
    return executeLarkApiCall(headers -> {
      // 搜索文档
      Map<String, Object> searchParams = new HashMap<>();
      searchParams.put("search_key", pluginParams.getSearchKey());
      searchParams.put("offset", pluginParams.getOffset());
      searchParams.put("count", pluginParams.getCount());

      Map<String, Object> searchResponse = HttpUtil.post(SEARCH_DOC_URL, searchParams, new ParameterizedTypeReference<Map<String, Object>>() {
      }, headers);
      // 转换搜索结果
      LarkResultDTO<SearchResultDTO> searchResult = snakeCaseMapper.convertValue(searchResponse, new TypeReference<LarkResultDTO<SearchResultDTO>>() {
      });
      if (searchResult == null || searchResult.getData() == null || CollectionUtils.isEmpty(searchResult.getData().getDocsEntities())) {
        return searchResult;
      }
      // 构建批量获取文档信息的请求参数
      List<Map<String, Object>> docTokens = CollectionUtils.emptyIfNull(searchResult.getData().getDocsEntities()).stream().map(doc -> {
        Map<String, Object> docToken = new HashMap<>();
        docToken.put("doc_token", doc.getDocsToken());
        docToken.put("doc_type", doc.getDocsType());
        return docToken;
      }).collect(Collectors.toList());
      Map<String, Object> batchGetDocParams = new HashMap<>();
      batchGetDocParams.put("request_docs", docTokens);
      batchGetDocParams.put("with_url", true);
      // 批量获取文档元信息
      LarkResultDTO<BatchGetDocInfoDTO> metaResult = HttpUtil.post(BATH_QUERY_DOC_INFO_URL, batchGetDocParams,
        new ParameterizedTypeReference<LarkResultDTO<BatchGetDocInfoDTO>>() {
        },  headers);
      if (metaResult == null || metaResult.getData() == null) {
        throw new BssException("批量获取文档信息失败, 响应为空");
      }
      // 将URL信息合并到搜索结果中
      Map<String, String> urlMap = CollectionUtils.emptyIfNull(metaResult.getData().getMetas()).stream()
        .collect(Collectors.toMap(DocsMetaDTO::getDocToken, DocsMetaDTO::getUrl, (existing, replacement) -> existing));
      CollectionUtils.emptyIfNull(searchResult.getData().getDocsEntities()).forEach(doc -> {
        String url = urlMap.get(doc.getDocsToken());
        if (url != null) {
          doc.setUrl(url);
        }
      });
      return searchResult;
    }, pluginParams, null);
  }

  /**
   * 文档搜索结果
   */
  @Setter
  @Getter
  @ToString
  private static final class SearchResultDTO {
    /** 包含搜索关键词的文件列表 */
    private List<DocsDTO> docsEntities;
    /** 结果列表后是否还有数据 */
    private Boolean hasMore;
    /** 包含搜索关键词的文件总数量 */
    private Integer total;
  }

  /**
   * 文档信息
   */
  @Setter
  @Getter
  @ToString
  private static final class DocsDTO {
    /** 文件的 token */
    private String docsToken;
    /** 文件类型 */
    private String docsType;
    /** 文件所有者 */
    private String ownerId;
    /** 文件标题 */
    private String title;
    /** 文件访问地址 */
    private String url;
  }

  /**
   * 批量获取文档信息结果
   */
  @Setter
  @Getter
  @ToString
  private static final class BatchGetDocInfoDTO {
    /** 文档信息 */
    private List<DocsMetaDTO> metas;
  }

  /**
   * 文档信息
   */
  @Setter
  @Getter
  @ToString
  @JsonNaming(SnakeCaseStrategy.class)
  private static final class DocsMetaDTO {
    /** 文件的 token */
    private String docToken;
    /** 文件类型 */
    private String docType;
    /** 文档访问链接 */
    private String url;
  }
}
