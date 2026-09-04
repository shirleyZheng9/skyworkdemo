package com.iwhalecloud.bote.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.iwhalecloud.bote.config.properties.ElasticSearchProperties;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import java.net.URI;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;

/**
 * ElasticSearch自动配置类
 */
@AutoConfiguration
@EnableConfigurationProperties(ElasticSearchProperties.class)
@ConditionalOnBooleanProperty("bote.elasticsearch.enabled")
@RequiredArgsConstructor
@SuppressFBWarnings("REDOS")
public class ElasticSearchAutoConfiguration  {

  private final ElasticSearchProperties elasticSearchProperties;

  private static final Pattern INDEX_NAME_PATTERN = Pattern.compile("^[a-z](?:[a-z0-9_-]+)?(?:\\.[a-z0-9_-]+)*$");


  @Bean
  @Primary
  public ElasticsearchClient elasticsearchClient() {
    if (!isValidNameSpace()) {
      throw new BssException("ElasticSearch 命名空间不合法！");
    }
    if (elasticSearchProperties.getUrl() == null || elasticSearchProperties.getUrl().isEmpty()) {
      throw new BssException("ElasticSearch URI不能为空！");
    }
    // 构建主机列表
    HttpHost[] hosts = buildHosts();

    RestClient restClient = RestClient.builder(hosts)  //NOPMD - suppressed CloseResource - 不能关闭
      .setHttpClientConfigCallback(httpAsyncClientBuilder -> {
        // 认证配置
        boolean needAuth = StringUtils.isNotEmpty(elasticSearchProperties.getUsername())
          && StringUtils.isNotEmpty(elasticSearchProperties.getPassword());
        if (needAuth) {
          BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
          credentialsProvider.setCredentials(AuthScope.ANY,
            new UsernamePasswordCredentials(elasticSearchProperties.getUsername(), elasticSearchProperties.getPassword()));
          httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }

        // SSL配置
//        boolean sslEnabled = isSslEnabled();
        if (Boolean.TRUE.equals(elasticSearchProperties.getUnsafeSsl())) {
          try {
            SSLContext sslContext = SSLContextBuilder.create()
              .loadTrustMaterial(null, new TrustAllStrategy())
              .build();
            httpAsyncClientBuilder.setSSLContext(sslContext);
            httpAsyncClientBuilder.setSSLHostnameVerifier(new NoopHostnameVerifier());
          } catch (Exception e) {
            throw new RuntimeException("SSL配置失败", e);
          }
        }

        return httpAsyncClientBuilder;
      })
      .build();

    // 创建传输层和客户端
    ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
    return new ElasticsearchClient(transport);
  }

  /**
   * 检查命名空间是否合法
   */
  private Boolean isValidNameSpace() {
    return INDEX_NAME_PATTERN.matcher(elasticSearchProperties.getNamespace()).matches();
  }

  /**
   * 构建主机列表
   */
  private HttpHost[] buildHosts() {
    return elasticSearchProperties.getUrl().stream()
      .map(uri -> {
        try {
          URI parsedUri = URI.create(uri);
          String scheme = parsedUri.getScheme();
          String host = parsedUri.getHost();
          int port = parsedUri.getPort();
          return new HttpHost(host, port, scheme);
        }
        catch (Exception e) {
          throw new IllegalArgumentException("无效的ElasticSearch URI: " + uri, e);
        }
      })
      .toArray(HttpHost[]::new);
  }

}
