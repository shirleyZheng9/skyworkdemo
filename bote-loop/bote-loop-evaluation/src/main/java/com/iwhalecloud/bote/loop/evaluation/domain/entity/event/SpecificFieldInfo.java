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
public class SpecificFieldInfo {
  private String fieldKey;
  private FieldType fieldType;
}
