package com.iwhalecloud.bote.service.search.internal.engine;

import com.iwhalecloud.bote.service.search.internal.model.SearchQuery;
import com.iwhalecloud.bote.service.search.internal.model.SearchResult;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public abstract class BaseEngineAdapter implements SearchEngine {
  protected static final Logger logger = LoggerFactory.getLogger(BaseEngineAdapter.class);

  protected String name;
  protected List<String> categories = List.of("general");
  protected boolean pagingSupported = true;
  protected double timeout = 10.0;
  protected double weight = 1.0;
  protected boolean disabled = false;
  protected HttpClientService httpClient;

  public List<SearchResult> search(SearchQuery query) {
    try {
      HttpRequest request = buildRequest(query);
      HttpResponse response = httpClient.execute(request);

      if (response.getStatusCode() == 302 && response.getRedirectUrl() != null) {
        if (response.getRedirectUrl().contains("wappass.baidu.com")) {
          throw new BssException("Baidu captcha detected");
        }
      }

      if (response.getBody() != null && response.getStatusCode() == 200) {
        return parseResponse(response.getBody(), (query.getPage() - 1) * getResultsPerPage());
      }

      if (logger.isWarnEnabled()) {
        logger.warn("Engine {} returned status code: {}", name, response.getStatusCode());
      }
      return new ArrayList<>();

    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Engine {} search failed: {}", name, e.getMessage());
      }
      return new ArrayList<>();
    }
  }

  protected int getResultsPerPage() {
    return 10;
  }
}
