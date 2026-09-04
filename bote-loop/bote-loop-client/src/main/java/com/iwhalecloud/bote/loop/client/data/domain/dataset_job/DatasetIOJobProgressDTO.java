package com.iwhalecloud.bote.loop.client.data.domain.dataset_job;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集IO任务进度数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetIOJobProgressDTO {

  @JsonProperty("total")
  private Long total;

  @JsonProperty("processed")
  private Long processed;

  @JsonProperty("added")
  private Long added;

  @JsonProperty("name")
  private String name;

  @JsonProperty("sub_progresses")
  private List<DatasetIOJobProgressDTO> subProgresses;
}
