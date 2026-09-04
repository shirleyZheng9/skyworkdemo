package com.iwhalecloud.bote.loop.evaluation.domain.entity.event;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunMode;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Session;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptItemEvalEvent {
  private long spaceId;
  private long exptId;
  private long exptRunId;
  private ExptRunMode exptRunMode;
  private long evalSetItemId;
  private long createAt;
  private int retryTimes;
  private Map<String, String> ext;
  private Session session;
}
