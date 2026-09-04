package com.iwhalecloud.bote.service.search.internal.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.service.search.internal.engine.EngineRegistry;
import com.iwhalecloud.bote.service.search.internal.engine.HttpClientService;
import com.iwhalecloud.bote.service.search.internal.engine.SearchEngine;
import com.iwhalecloud.bote.service.search.internal.engine.impl.BaiduEngine;
import com.iwhalecloud.bote.service.search.internal.engine.impl.BilibiliEngine;
import com.iwhalecloud.bote.service.search.internal.engine.impl.BingEngine;
import com.iwhalecloud.bote.service.search.internal.engine.impl.So360Engine;
import com.iwhalecloud.bote.service.search.internal.engine.impl.SogouEngine;
import com.iwhalecloud.bote.service.search.internal.model.EngineRef;
import com.iwhalecloud.bote.service.search.internal.model.SearchQuery;
import com.iwhalecloud.bote.service.search.internal.model.SearchResult;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.StreamSupport;

@Getter
@Setter
public class SearchService {
  private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

  private final HttpClientService httpClient;

  private double defaultTimeout = 10.0;
  private Double maxRequestTimeout = null;

    public SearchService() {
        this.httpClient = new HttpClientService();
        initializeEngines();
    }

    private void initializeEngines() {
        BaiduEngine baiduEngine = new BaiduEngine();
        baiduEngine.setHttpClient(httpClient);
        EngineRegistry.register(baiduEngine);

        BilibiliEngine bilibiliEngine = new BilibiliEngine();
        bilibiliEngine.setHttpClient(httpClient);
        EngineRegistry.register(bilibiliEngine);

        SogouEngine sogouEngine = new SogouEngine();
        sogouEngine.setHttpClient(httpClient);
        EngineRegistry.register(sogouEngine);

        So360Engine so360Engine = new So360Engine();
        so360Engine.setHttpClient(httpClient);
        EngineRegistry.register(so360Engine);

        BingEngine bingEngine = new BingEngine();
        bingEngine.setHttpClient(httpClient);
        EngineRegistry.register(bingEngine);

        if (logger.isInfoEnabled()) {
            logger.info("Engines initialized: {}", EngineRegistry.getEngines().keySet());
        }
    }

  public SearchResultContainer search(String rawQuery) {
    List<String> engines = EngineRegistry.getEngines().keySet().stream().toList();
    String params = SystemParameter.WEB_SEARCH_INTERNAL_PARAMS.getValueFromDb();
    if (StringUtils.isNotEmpty(params)) {
      JsonNode node = JsonUtil.readTree(params);
      JsonNode enginesNode = node.get("engines");
      if (enginesNode != null && !enginesNode.isNull() && enginesNode.isArray()) {
        engines = StreamSupport.stream(enginesNode.spliterator(), false).map(JsonNode::asText).toList();
      }
    }
    return search(rawQuery, engines);
  }

  public SearchResultContainer search(String rawQuery, List<String> engines) {
    List<EngineRef> engineRefs = new ArrayList<>();
    for (String engineName : engines) {
      SearchEngine engine = EngineRegistry.get(engineName);
      if (engine != null) {
        engineRefs.add(new EngineRef(engineName, engine.getCategories().get(0)));
      }
    }
    SearchQuery searchQuery = new SearchQuery(
      rawQuery,
      engineRefs,
      1
    );
    return executeSearch(searchQuery);
  }

   private SearchResultContainer executeSearch(SearchQuery searchQuery) {
     ResultMerger resultMerger = new ResultMerger();

     List<SearchEngine> enginesToSearch = prepareEngines(searchQuery);
     if (enginesToSearch.isEmpty()) {
       logger.warn("No engines available for search");
       return createEmptyResultContainer();
     }

     executeEngines(enginesToSearch, searchQuery, defaultTimeout, resultMerger);

     resultMerger.close();

     return createResultContainer(resultMerger);
   }

   private List<SearchEngine> prepareEngines(SearchQuery searchQuery) {
     List<SearchEngine> enginesToSearch = new ArrayList<>();

     for (EngineRef ref : searchQuery.getEngineRefs()) {
       SearchEngine engine = EngineRegistry.get(ref.getName());
       if (engine != null && !engine.isDisabled()) {
         enginesToSearch.add(engine);
       }
     }

     return enginesToSearch;
   }

   private void executeEngines(List<SearchEngine> enginesToSearch, SearchQuery searchQuery, double actualTimeout, ResultMerger resultMerger) {
     List<Future<List<SearchResult>>> futures = submitEngineTasks(enginesToSearch, searchQuery, resultMerger);
     collectResults(enginesToSearch, futures, actualTimeout, resultMerger);
   }

   private List<Future<List<SearchResult>>> submitEngineTasks(List<SearchEngine> enginesToSearch, SearchQuery searchQuery, ResultMerger resultMerger) {
     List<Future<List<SearchResult>>> futures = new ArrayList<>();

     for (SearchEngine engine : enginesToSearch) {
       Future<List<SearchResult>> future = ThreadPools.getCommon().submit(() -> {
         long startTime = System.currentTimeMillis();
         try {
           List<SearchResult> results = searchEngine(engine, searchQuery);
           long duration = (long) ((System.currentTimeMillis() - startTime) / 1000.0);
           resultMerger.addTiming(engine.getName(), duration, duration);
           return results;
         } catch (Exception e) {
           if (logger.isErrorEnabled()) {
             logger.error("Engine {} failed: {}", engine.getName(), e.getMessage());
           }
           resultMerger.addUnresponsiveEngine(engine.getName(), e.getMessage(), false);
           resultMerger.addTiming(engine.getName(),
             (long) ((System.currentTimeMillis() - startTime) / 1000.0), 0);
           return new ArrayList<>();
         }
       });
       futures.add(future);
     }

     return futures;
   }

   private void collectResults(List<SearchEngine> enginesToSearch, List<Future<List<SearchResult>>> futures, double actualTimeout, ResultMerger resultMerger) {
     for (int i = 0; i < futures.size(); i++) {
       SearchEngine engine = enginesToSearch.get(i);
       try {
         List<SearchResult> results = futures.get(i).get((long) actualTimeout, TimeUnit.SECONDS);
         resultMerger.extend(engine.getName(), results);
       } catch (TimeoutException e) {
         if (logger.isWarnEnabled()) {
           logger.warn("Engine {} timed out", engine.getName());
         }
         resultMerger.addUnresponsiveEngine(engine.getName(), "timeout", false);
       } catch (Exception e) {
         if (logger.isErrorEnabled()) {
           logger.error("Engine {} error: {}", engine.getName(), e.getMessage());
         }
         resultMerger.addUnresponsiveEngine(engine.getName(), e.getMessage(), false);
       }
     }
   }

   private SearchResultContainer createEmptyResultContainer() {
     return new SearchResultContainer(new ArrayList<>(), new ArrayList<>(),
       new HashSet<>(), new HashSet<>(), new ArrayList<>(), 0, "");
   }

   private SearchResultContainer createResultContainer(ResultMerger resultMerger) {
     List<SearchResult> orderedResults = resultMerger.getOrderedResults();

     return new SearchResultContainer(
       orderedResults,
       resultMerger.getInfoboxes(),
       resultMerger.getSuggestions(),
       resultMerger.getCorrections(),
       resultMerger.getTimings(),
       resultMerger.getNumberOfResults(),
       resultMerger.getRedirectUrl() != null ? resultMerger.getRedirectUrl() : ""
     );
   }

  private List<SearchResult> searchEngine(SearchEngine engine, SearchQuery query) {
    try {
      return engine.search(query);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Engine {} search error: {}", engine.getName(), e.getMessage());
      }
      throw new BssException("搜索失败:" + e.getMessage(), e);
    }
  }
}
