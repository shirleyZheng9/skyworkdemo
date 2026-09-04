package com.iwhalecloud.bote.service.reply.service;

import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.config.properties.DocChainProperties;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocChainLoginHelper;
import com.vladsch.flexmark.html.UriContentResolver;
import com.vladsch.flexmark.html.UriContentResolverFactory;
import com.vladsch.flexmark.html.renderer.LinkResolverBasicContext;
import com.vladsch.flexmark.html.renderer.LinkStatus;
import com.vladsch.flexmark.html.renderer.ResolvedContent;
import com.vladsch.flexmark.util.ast.Node;
import java.net.URI;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * DocChain 图片解析器工厂类
 *
 * @author bianjp
 * @since 2026-01-07
 */
@Component
public class DocChainImageResolverFactory implements UriContentResolverFactory {
  private final DocChainImageResolver resolver;

  public DocChainImageResolverFactory(DocChainProperties docChainProperties, DocChainLoginHelper docChainLoginHelper) {
    this.resolver = new DocChainImageResolver(docChainProperties, docChainLoginHelper);
  }

  @Override
  @Nullable
  public Set<Class<?>> getAfterDependents() {
    return null;
  }

  @Override
  @Nullable
  public Set<Class<?>> getBeforeDependents() {
    return null;
  }

  @Override
  public boolean affectsGlobalScope() {
    return false;
  }

  @Override
  public UriContentResolver apply(LinkResolverBasicContext context) {
    return resolver;
  }

  /**
   * DocChain 图片解析器
   */
  @SuppressWarnings("ClassCanBeRecord")
  private static final class DocChainImageResolver implements UriContentResolver {
    private static final Logger logger = LoggerFactory.getLogger(DocChainImageResolver.class);

    private final DocChainProperties docChainProperties;
    private final DocChainLoginHelper docChainLoginHelper;

    public DocChainImageResolver(DocChainProperties docChainProperties, DocChainLoginHelper docChainLoginHelper) {
      this.docChainProperties = docChainProperties;
      this.docChainLoginHelper = docChainLoginHelper;
    }

    @Override
    @SuppressWarnings("PMD.GuardLogStatement")
    public ResolvedContent resolveContent(Node node, LinkResolverBasicContext context, ResolvedContent content) {
      // 示例: /llmdoc/v1/doc/read?doc_id=69883&read_format=path&path=media/img_in_image_box_407_532_767_993.jpg
      String url = content.getResolvedLink().getUrl();
      // 不处理非 DocChain 图片
      if (!url.contains("v1/doc/read?") || !url.contains("read_format=path")) {
        return content;
      }

      Long tenantId = TenantIdUtil.getTenantId();
      try {
        URI uri = UriComponentsBuilder.fromUriString(docChainProperties.getReadDocumentApiUrl(tenantId))
          // 解码参数值以避免重复转义
          .queryParams(HttpUtil.extractQueryParams(url))
          // query 参数需要转义
          .encode()
          .build()
          .toUri();
        HttpHeaders headers = docChainLoginHelper.buildHeader(tenantId);
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> responseEntity = HttpUtil.getRestTemplate().exchange(uri, HttpMethod.GET, requestEntity, byte[].class);
        return content.withStatus(LinkStatus.VALID).withContent(responseEntity.getBody());
      }
      // 获取图片失败时忽略异常（可能 DocChain 侧已经删除了图片），避免影响文档生成
      catch (HttpStatusCodeException e) {
        logger.warn("Failed to get DocChain image: tenantId={}, url={}, status={}", tenantId, url, e.getStatusCode().value());
        return content.withStatus(LinkStatus.INVALID);
      }
      catch (Exception e) {
        logger.warn("Failed to get DocChain image: tenantId={}, url={}", tenantId, url, e);
        return content.withStatus(LinkStatus.INVALID);
      }
    }
  }
}
