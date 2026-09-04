package com.iwhalecloud.bote.service.search.internal.core;

import com.iwhalecloud.bote.service.search.internal.model.SearchResult;
import com.iwhalecloud.bote.service.search.internal.model.Timing;
import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter
public class SearchResultContainer {
  private final List<SearchResult> results;
  private final List<SearchResult> infoboxes;
  private final Set<String> suggestions;
  private final Set<String> corrections;
  private final List<Timing> timings;
  private final int numberOfResults;
  private final String redirectUrl;

  public SearchResultContainer(List<SearchResult> results, List<SearchResult> infoboxes,
                               Set<String> suggestions, Set<String> corrections,
                               List<Timing> timings, int numberOfResults, String redirectUrl) {
    this.results = results;
    this.infoboxes = infoboxes;
    this.suggestions = suggestions;
    this.corrections = corrections;
    this.timings = timings;
    this.numberOfResults = numberOfResults;
    this.redirectUrl = redirectUrl;
  }
}
