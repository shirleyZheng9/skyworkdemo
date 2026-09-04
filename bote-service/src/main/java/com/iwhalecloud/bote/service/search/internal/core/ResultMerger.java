package com.iwhalecloud.bote.service.search.internal.core;

import com.iwhalecloud.bote.service.search.internal.engine.EngineRegistry;
import com.iwhalecloud.bote.service.search.internal.engine.SearchEngine;
import com.iwhalecloud.bote.service.search.internal.model.SearchResult;
import com.iwhalecloud.bote.service.search.internal.model.Timing;
import com.iwhalecloud.bote.service.search.internal.model.UnresponsiveEngine;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ResultMerger {

  private final Map<Integer, SearchResult> mainResultsMap = new ConcurrentHashMap<>();
  private final List<SearchResult> infoboxes = new ArrayList<>();
  private final Set<String> suggestions = ConcurrentHashMap.newKeySet();
  private final Set<String> corrections = ConcurrentHashMap.newKeySet();
  private final Map<String, Integer> engineResultCounts = new ConcurrentHashMap<>();
  private boolean closed = false;
  @Getter
  private boolean paging = false;
  private final Set<UnresponsiveEngine> unresponsiveEngines = ConcurrentHashMap.newKeySet();
  private final List<Timing> timings = new ArrayList<>();
  @Setter
  @Getter
  private String redirectUrl;

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private List<SearchResult> sortedResults;

  public void extend(String engineName, List<SearchResult> results) {
    if (extracted(results)) {
      return;
    }

    lock.writeLock().lock();
    try {
      int mainCount = 0;

      for (SearchResult result : results) {
        if (extracted(engineName, result)) {
          continue;
        }

        mainCount++;
        mergeMainResult(result);
      }

      SearchEngine engine = EngineRegistry.get(engineName);
      if (engine != null && !paging && engine.isPagingSupported()) {
        paging = true;
      }

      engineResultCounts.put(engineName, mainCount);

    } finally {
      lock.writeLock().unlock();
    }
  }

  private boolean extracted(String engineName, SearchResult result) {
    if (result == null) {
      return true;
    }

    if (result.getEngine() == null || result.getEngine().isEmpty()) {
      result.setEngine(engineName);
    }

    if (result.getTemplate() != null && result.getTemplate().contains("infobox")) {
      mergeInfobox(result);
      return true;
    }

    if (result.getContent() != null && result.getContent().startsWith("suggestion:")) {
      suggestions.add(result.getContent().substring(11));
      return true;
    }

    if (result.getContent() != null && result.getContent().startsWith("correction:")) {
      corrections.add(result.getContent().substring(11));
      return true;
    }
    return false;
  }

  private boolean extracted(List<SearchResult> results) {
    if (closed) {
      return true;
    }

    if (results == null || results.isEmpty()) {
      return true;
    }
    return false;
  }

  private void mergeMainResult(SearchResult result) {
    int hash = result.hashCode();

    SearchResult existing = mainResultsMap.get(hash);
    if (existing == null) {
      result.addPosition(1);
      mainResultsMap.put(hash, result);
      return;
    }

    existing.addEngine(result.getEngine());
    for (int pos : result.getPositions()) {
      existing.addPosition(pos);
    }

    String content = result.getContent();
    String existingContent = existing.getContent();
    int existingContentLength = existingContent != null ? existingContent.length() : 0;
    if (content != null && content.length() > existingContentLength) {
      existing.setContent(content);
    }

    extractResult(result, existing);
  }

  private static void extractResult(SearchResult result, SearchResult existing) {
    if (result.getTitle() != null && (existing.getTitle() == null || result.getTitle().length() > existing.getTitle().length())) {
      existing.setTitle(result.getTitle());
    }

    if (result.getPublishedDate() != null && existing.getPublishedDate() == null) {
      existing.setPublishedDate(result.getPublishedDate());
    }

    if (result.getThumbnail() != null && existing.getThumbnail() == null) {
      existing.setThumbnail(result.getThumbnail());
    }
  }

  private void mergeInfobox(SearchResult newInfobox) {
    for (SearchResult existing : infoboxes) {
      if (existing.getUrl() != null && existing.getUrl().equals(newInfobox.getUrl())) {
        mergeInfoboxData(existing, newInfobox);
        return;
      }
    }
    infoboxes.add(newInfobox);
  }

  private void mergeInfoboxData(SearchResult existing, SearchResult other) {
    if (other.getContent() != null && (existing.getContent() == null || other.getContent().length() > existing.getContent().length())) {
      existing.setContent(other.getContent());
    }

    if (other.getThumbnail() != null) {
      existing.setThumbnail(other.getThumbnail());
    }

    if (other.getAuthor() != null) {
      existing.setAuthor(other.getAuthor());
    }

    Set<String> mergedEngines = new HashSet<>(existing.getEngines());
    mergedEngines.addAll(other.getEngines());
    existing.setEngines(mergedEngines);
  }

  public void close() {
    lock.writeLock().lock();
    try {
      closed = true;

      for (SearchResult result : mainResultsMap.values()) {
        result.setScore(calculateScore(result));
      }

      sortedResults = null;

    } finally {
      lock.writeLock().unlock();
    }
  }

  private double calculateScore(SearchResult result) {
    double weight = 1.0;

    for (String engineName : result.getEngines()) {
      SearchEngine engine = EngineRegistry.get(engineName);
      if (engine != null) {
        weight *= engine.getWeight();
      }
    }

    weight *= result.getPositions().size();

    double score = 0;
    for (int position : result.getPositions()) {
      if (position <= 0) {
        continue;
      }
      score += weight / position;
    }

    return score;
  }

  public List<SearchResult> getOrderedResults() {
    lock.readLock().lock();
    try {
      if (!closed) {
        close();
      }

      if (sortedResults != null) {
        return sortedResults;
      }

      List<SearchResult> results = new ArrayList<>(mainResultsMap.values());

      results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

      sortedResults = results;
      return sortedResults;

    } finally {
      lock.readLock().unlock();
    }
  }

  public int getNumberOfResults() {
    lock.readLock().lock();
    try {
      if (!closed) {
        return 0;
      }

      if (engineResultCounts.isEmpty()) {
        return 0;
      }

      int sum = engineResultCounts.values().stream().mapToInt(Integer::intValue).sum();
      int count = engineResultCounts.size();

      int average = sum / count;

      if (average < getOrderedResults().size()) {
        return 0;
      }

      return average;

    } finally {
      lock.readLock().unlock();
    }
  }

  public void addUnresponsiveEngine(String engineName, String errorType, boolean suspended) {
    SearchEngine engine = EngineRegistry.get(engineName);
    if (engine != null) {
      unresponsiveEngines.add(new UnresponsiveEngine(engineName, errorType, suspended));
    }
  }

  public void addTiming(String engineName, double engineTime, double pageLoadTime) {
    timings.add(new Timing(engineName, engineTime, pageLoadTime));
  }

  public List<Timing> getTimings() {
    lock.readLock().lock();
    try {
      return new ArrayList<>(timings);
    } finally {
      lock.readLock().unlock();
    }
  }

  public Set<UnresponsiveEngine> getUnresponsiveEngines() {
    return new HashSet<>(unresponsiveEngines);
  }

  public List<SearchResult> getInfoboxes() {
    return new ArrayList<>(infoboxes);
  }

  public Set<String> getSuggestions() {
    return new HashSet<>(suggestions);
  }

  public Set<String> getCorrections() {
    return new HashSet<>(corrections);
  }

  public Map<String, Integer> getEngineResultCounts() {
    return new HashMap<>(engineResultCounts);
  }
}
