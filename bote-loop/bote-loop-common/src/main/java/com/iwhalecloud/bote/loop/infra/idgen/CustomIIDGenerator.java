package com.iwhalecloud.bote.loop.infra.idgen;

import com.google.common.collect.Lists;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CustomIIDGenerator implements IIDGenerator {

  @Override
  public Long genId() {
    return IDUtils.nextId();
  }

  @Override
  public List<Long> genMultiIds(int count) {
    List<Long> ids = Lists.newArrayList();
    for (int i = 0; i < count; i++) {
      ids.add(genId());
    }
    return ids;
  }
}
