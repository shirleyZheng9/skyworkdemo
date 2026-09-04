package com.iwhalecloud.bote.doc.extend.impl;

import com.iwhalecloud.bote.dto.base.SimpleElementDTO;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * @author chen.linfa
 * @since 2025-11-19
 */
@Component
public class MockResourceElementServiceImpl implements IResourceElementService {
  @Override
  public void batchAdd(Long tenantId, Long resourceId, String resourceType, List<Long> elementIds, String elementType) {
    // 不做任何操作
  }

  @Override
  public void remove(Long tenantId, Long resourceId, Long elementId, String elementType) {
    // 不做任何操作
  }

  @Override
  public Map<String, List<SimpleElementDTO>> queryRelatedResource(Long tenantId, List<Long> resourceIds, String resourceType) {
    return Map.of();
  }

  @Override
  public boolean existsRelatedResource(Long tenantId, Long elementId, String elementType) {
    return false;
  }
}
