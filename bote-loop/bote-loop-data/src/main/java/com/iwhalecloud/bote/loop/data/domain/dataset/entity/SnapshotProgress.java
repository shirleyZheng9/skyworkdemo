package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 快照进度实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnapshotProgress {

  @JsonProperty("cursor")
  private String cursor;
}
