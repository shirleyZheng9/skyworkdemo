package com.iwhalecloud.bote.loop.evaluation.domain.entity.event;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggrCalculateEvent {
  private long experimentId;
  private long spaceId;
  private CalculateMode calculateMode;
  private SpecificFieldInfo specificFieldInfo;

  public String getFieldKey() {
    if (specificFieldInfo == null) {
      return "";
    }
    return specificFieldInfo.getFieldKey();
  }

  public FieldType getFieldType() {
    if (specificFieldInfo == null) {
      return null;
    }
    return specificFieldInfo.getFieldType();
  }
}
