package com.iwhalecloud.bote.service.search.internal.engine;

import com.iwhalecloud.bote.service.search.adapter.InternalWebSearchAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class EngineRegistry {
  private static final Logger logger = LoggerFactory.getLogger(InternalWebSearchAdapter.class);
  private static final Map<String, SearchEngine> engines = new ConcurrentHashMap<>();
  private static final Map<String, List<SearchEngine>> categories = new ConcurrentHashMap<>();

  public static void register(SearchEngine engine) {
    String name = engine.getName();

    if (engines.containsKey(name)) {
      logger.warn("Engine already registered: {}", name);
      return;
    }

    engines.put(name, engine);

    for (String category : engine.getCategories()) {
      categories.computeIfAbsent(category, k -> new ArrayList<>()).add(engine);
    }

    if (logger.isInfoEnabled()) {
      logger.info("Engine registered: {} with categories: {}", name, engine.getCategories());
    }
  }

  public static SearchEngine get(String name) {
    return engines.get(name);
  }

  public static Map<String, SearchEngine> getEngines() {
    return Collections.unmodifiableMap(engines);
  }
}
