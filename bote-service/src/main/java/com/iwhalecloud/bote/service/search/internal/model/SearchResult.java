package com.iwhalecloud.bote.service.search.internal.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
  private String title;
  private String url;
  private String content;
  private String engine;
  private Instant publishedDate;
  private String thumbnail;
  private String iframeSrc;
  private String template;
  private String author;
  private String length;
  private Set<String> engines = new HashSet<>();
  private List<Integer> positions = new ArrayList<>();
  private double score;
  private String category;

  public SearchResult(String title, String url, String content, String engine) {
    this.title = title;
    this.url = url;
    this.content = content;
    this.engine = engine;
    this.engines.add(engine);
  }

  public void addEngine(String engine) {
    this.engines.add(engine);
  }

  public void addPosition(int position) {
    this.positions.add(position);
  }

  @Override
  public int hashCode() {
    return url != null ? url.hashCode() : 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    SearchResult other = (SearchResult) obj;
    if (url == null || other.url == null) {
      return false;
    }
    return normalizeUrl(url).equals(normalizeUrl(other.url));
  }

  private String normalizeUrl(String url) {
    if (url == null) {
      return "";
    }
    url = url.toLowerCase();
    if (url.startsWith("http://")) {
      url = "https://" + url.substring(7);
    }
    int idx = url.indexOf("://");
    if (idx > 0) {
      String scheme = url.substring(0, idx + 3);
      String rest = url.substring(idx + 3);
      rest = rest.replaceAll("/+", "/");
      return scheme + rest;
    }
    return url;
  }
}
