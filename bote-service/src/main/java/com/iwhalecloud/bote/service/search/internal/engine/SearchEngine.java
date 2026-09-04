package com.iwhalecloud.bote.service.search.internal.engine;

import com.iwhalecloud.bote.service.search.internal.model.SearchQuery;
import com.iwhalecloud.bote.service.search.internal.model.SearchResult;

import java.util.List;

public interface SearchEngine {
  String getName();

  List<String> getCategories();

  boolean isPagingSupported();

  double getTimeout();


  HttpRequest buildRequest(SearchQuery query);

  List<SearchResult> parseResponse(String body, int positionOffset) throws Exception;

  default List<SearchResult> search(SearchQuery query) {
    return List.of();
  }

  default double getWeight() {
    return 1.0;
  }

  default boolean isDisabled() {
    return false;
  }
}
