package com.iwhalecloud.bote.loop.evaluation.domain.entity.event;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptTurnResultFilterEvent {
  private long experimentId;
  private long spaceId;
  private List<Long> itemId;
  private Integer retryTimes;
  private UpsertExptTurnResultFilterType filterType;
}
