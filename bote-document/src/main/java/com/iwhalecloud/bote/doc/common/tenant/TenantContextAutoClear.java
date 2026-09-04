package com.iwhalecloud.bote.doc.common.tenant;

import com.iwhalecloud.bote.doc.common.context.AutoClear;
import com.iwhalecloud.bote.doc.common.space.SpaceContextHolder;
import org.springframework.stereotype.Component;

/**
 * 租户上下文自动清理
 *
 * @author Aiqing
 * @since 2025/9/15
 */
@Component
public class TenantContextAutoClear implements AutoClear {
  @Override
  public void clear() {
    TenantContextHolder.clear();
    SpaceContextHolder.clear();
  }
}
