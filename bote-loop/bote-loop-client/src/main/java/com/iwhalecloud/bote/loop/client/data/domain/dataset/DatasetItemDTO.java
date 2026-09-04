package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集项目数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetItemDTO {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("app_id")
  private Integer appId;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("schema_id")
  private Long schemaId;

  @JsonProperty("item_id")
  private Long itemId;

  @JsonProperty("item_key")
  private String itemKey;

  @JsonProperty("data")
  private List<FieldDataDTO> data;

  @JsonProperty("repeated_data")
  private List<ItemDataDTO> repeatedData;

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("created_at")
  private Long createdAt;

  @JsonProperty("updated_by")
  private String updatedBy;

  @JsonProperty("updated_at")
  private Long updatedAt;

  @JsonProperty("data_omitted")
  private Boolean dataOmitted;
}
